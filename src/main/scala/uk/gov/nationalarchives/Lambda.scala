package uk.gov.nationalarchives

import io.circe.Printer.noSpaces
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import uk.gov.nationalarchives.BackendCheckUtils._
import uk.gov.nationalarchives.Lambda._

import java.io.{InputStream, OutputStream}
import java.util.UUID
import scala.io.Source

class Lambda {

  private val backendChecksUtils = BackendCheckUtils(sys.env.getOrElse("S3_ENDPOINT", "https://s3.eu-west-2.amazonaws.com"))

  private def getRedactedFiles(files: List[File]): List[RedactedResult] = {
    val directoryToFileNames = files
      .groupBy(_.originalPath.split("/").dropRight(1).mkString("/"))
      .view.mapValues(_.map(file => {
      val fileName = file.originalPath.split("/").last
      val fileNameNoExt = fileName.split("\\.").dropRight(1).mkString(".")
      FileName(file.fileId, file.originalPath, fileName, fileNameNoExt)
    })).toMap

    directoryToFileNames.flatMap {
      case (_, filesInDirectory) =>
        val redactedFiles = filesInDirectory
          .filter(file => "_R\\d*?$".r.findAllIn(file.fileNameNoExtension).hasNext)

        val redactedFilesWithDuplicateNames = redactedFiles
          .groupBy(file => file.fileNameNoExtension)
          .filter(_._2.size > 1).values.flatten.toList

        val nonDuplicateRedactedFiles = redactedFiles.diff(redactedFilesWithDuplicateNames)

        nonDuplicateRedactedFiles.map(redactedFile => {
          val originalFiles = filesInDirectory.filter(fileInDirectory => {
            fileInDirectory.fileNameNoExtension == redactedFile.fileNameNoExtension.substring(0, redactedFile.fileNameNoExtension.lastIndexOf("_R"))
          })
          originalFiles.length match {
            case 1 => RedactedFilePairs(originalFiles.head.fileId, originalFiles.head.filePath, redactedFile.fileId, redactedFile.filePath)
            case 0 => RedactedErrors(redactedFile.fileId, "NoOriginalFile")
            case _ => RedactedErrors(redactedFile.fileId, "AmbiguousOriginalFile")
          }
        }) ++ redactedFilesWithDuplicateNames.map(dup => RedactedErrors(dup.fileId, "DuplicateFileName"))
    }.toList
  }

  private def writeResult(input: Input, s3Input: S3Input): Either[Throwable, S3Input] = {
    val result = getRedactedFiles(input.results).foldLeft(RedactedResults(Nil, Nil))((res, redactedResult) => {
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

object Lambda {
  private case class FileName(fileId: UUID, filePath: String, fileName: String, fileNameNoExtension: String)
}
