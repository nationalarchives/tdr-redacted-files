package uk.gov.nationalarchives

import uk.gov.nationalarchives.BackendCheckUtils._

import java.nio.file.Paths
import java.util.UUID

object RedactedFileMatcher {
  val NoOriginalFile = "NoOriginalFile"
  val AmbiguousOriginalFile = "AmbiguousOriginalFile"
  val DuplicateFileName = "DuplicateFileName"

  private val RedactedFilePattern = "_R\\d*?$".r

  def getRedactedFiles(files: List[File]): List[RedactedResult] = {
    val filesInDirectories = files
      .groupBy(file => directory(file.originalPath))
      .map { case (directoryPath, filesInDirectory) => FilesInDirectory(directoryPath, filesInDirectory.map(toFileName)) }

    filesInDirectories.flatMap { directoryFiles =>
      val redactedFiles = directoryFiles.files.filter(isRedactedFile)

      val redactedFilesWithDuplicateNames = redactedFiles
        .groupBy(file => file.fileNameNoExtension)
        .map { case (fileNameNoExtension, files) => RedactedFilesByName(fileNameNoExtension, files) }
        .filter(redactedFilesByName => redactedFilesByName.hasDuplicates)
        .flatMap(redactedFilesByName => redactedFilesByName.files)
        .toList

      val duplicateErrors = redactedFilesWithDuplicateNames.map(dup => RedactedErrors(dup.fileId, DuplicateFileName))

      val nonDuplicateRedactedFiles = redactedFiles.diff(redactedFilesWithDuplicateNames)

      val matchedResults = nonDuplicateRedactedFiles.map { redactedFile =>
        val originalFileName = originalNameFor(redactedFile)
        val originalFiles = directoryFiles.files.filter(fileInDirectory => isOriginalFile(fileInDirectory, originalFileName))
        originalFiles match {
          case head :: Nil => RedactedFilePairs(head.fileId, head.filePath, redactedFile.fileId, redactedFile.filePath)
          case Nil => RedactedErrors(redactedFile.fileId, NoOriginalFile)
          case _ => RedactedErrors(redactedFile.fileId, AmbiguousOriginalFile)
        }
      }

      matchedResults ++ duplicateErrors
    }.toList
  }

  private def toFileName(file: File): FileName = {
    val name = Paths.get(file.originalPath).getFileName.toString
    FileName(file.fileId, file.originalPath, name, removeExtension(name))
  }

  private def directory(path: String): String = Option(Paths.get(path).getParent).map(_.toString).getOrElse("")

  private def removeExtension(fileName: String): String = {
    val extensionSeparator = fileName.lastIndexOf('.')
    if (extensionSeparator > 0 && extensionSeparator < fileName.length - 1) {
      fileName.substring(0, extensionSeparator)
    } else {
      fileName
    }
  }


  private def isRedactedFile(file: FileName): Boolean = {
    RedactedFilePattern.findFirstIn(file.fileNameNoExtension).isDefined
  }

  private def originalNameFor(redactedFile: FileName): String = {
    redactedFile.fileNameNoExtension.substring(0, redactedFile.fileNameNoExtension.lastIndexOf("_R"))
  }

  private def isOriginalFile(file: FileName, originalFileName: String): Boolean = {
    file.fileNameNoExtension == originalFileName || file.fileName == originalFileName
  }

  private case class FilesInDirectory(directoryPath: String, files: List[FileName])
  private case class RedactedFilesByName(fileNameNoExtension: String, files: List[FileName]) {
    def hasDuplicates: Boolean = files.size > 1
  }
  private case class FileName(fileId: UUID, filePath: String, fileName: String, fileNameNoExtension: String)
}
