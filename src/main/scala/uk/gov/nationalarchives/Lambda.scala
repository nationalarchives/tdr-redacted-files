package uk.gov.nationalarchives

import io.circe.Printer.noSpaces
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.generic.auto._
import uk.gov.nationalarchives.Lambda._

import java.io.{InputStream, OutputStream}
import java.util.UUID
import scala.io.Source

class Lambda {

  def getRedactedFiles(files: List[File]): List[RedactedResult] = {
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

  def run(inputStream: InputStream, outputStream: OutputStream): Unit = {
    val input = Source.fromInputStream(inputStream).getLines().mkString
    val output = decode[Input](input).map(input => {
      getRedactedFiles(input.results).foldLeft(Result(Nil,  Nil))((res, redactedResult) => {
        redactedResult match {
          case errors: RedactedErrors => res.copy(errors = errors :: res.errors)
          case pairs: RedactedFilePairs => res.copy(redactedFiles = pairs :: res.redactedFiles)
          case _ => res
        }
      })
    }) match {
      case Left(err) => throw err
      case Right(result) => result.asJson.printWith(noSpaces)
    }

    outputStream.write(output.getBytes())
  }
}
object Lambda {
  trait RedactedResult

  case class Result(redactedFiles: List[RedactedFilePairs], errors: List[RedactedErrors])

  case class RedactedErrors(fileId: UUID, cause: String) extends RedactedResult

  case class RedactedFilePairs(originalFileId: UUID, originalFilePath: String, redactedFileId: UUID, redactedFilePath: String) extends RedactedResult

  case class FileName(fileId: UUID, filePath: String, fileName: String, fileNameNoExtension: String)

  case class File(fileId: UUID, originalPath: String)

  case class Input(results: List[File])
}
