package uk.gov.nationalarchives

import io.circe.Printer.noSpaces
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import uk.gov.nationalarchives.BackendCheckUtils._

import java.io.{InputStream, OutputStream}
import scala.io.Source

class Lambda {

  private val backendChecksUtils = BackendCheckUtils(sys.env.getOrElse("S3_ENDPOINT", "https://s3.eu-west-2.amazonaws.com"))

  private def writeResult(input: Input, s3Input: S3Input): Either[Throwable, S3Input] = {
    val result = RedactedFileMatcher.getRedactedFiles(input.results).foldLeft(RedactedResults(Nil, Nil))((res, redactedResult) => {
      redactedResult match {
        case errors: RedactedErrors => res.copy(errors = errors :: res.errors)
        case pairs: RedactedFilePairs => res.copy(redactedFiles = pairs :: res.redactedFiles)
        case _ => res
      }
    })
    val output = Input(input.results, result, input.statuses).asJson.printWith(noSpaces)
    backendChecksUtils.writeResultJson(s3Input.key, s3Input.bucket, output)
  }

  def run(inputStream: InputStream, outputStream: OutputStream): Unit = {
    val input = Source.fromInputStream(inputStream).getLines().mkString
    val output = (for {
      s3Input <- decode[S3Input](input)
      input <- backendChecksUtils.getResultJson(s3Input.key, s3Input.bucket)
      result <- writeResult(input, s3Input)
    } yield {
      result
    }) match {
      case Left(err) => throw err
      case Right(result) => result.asJson.printWith(noSpaces)
    }

    outputStream.write(output.getBytes())
  }
}

