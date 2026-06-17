package uk.gov.nationalarchives

import uk.gov.nationalarchives.BackendCheckUtils._

import java.nio.file.Paths
import java.util.UUID

object RedactedFileMatcher {
  val noOriginalFileError = "NoOriginalFile"
  val ambiguousOriginalFileError = "AmbiguousOriginalFile"
  val duplicateFileNameError = "DuplicateFileName"

  private val RedactedFilePattern = "^(.+?)(?:_R|_Redacted|_redacted)\\d*$".r

  def getRedactedFiles(files: List[File]): List[RedactedResult] = {
    val filesInDirectories = files
      .groupBy(file => directory(file.originalPath))
      .map { case (directoryPath, filesInDirectory) => FilesInDirectory(directoryPath, filesInDirectory.map(toFileName)) }

    filesInDirectories.flatMap { directoryFiles =>
      val redactedFiles = directoryFiles.files.filter(isRedactedFile)

      val redactedFilesWithDuplicateNames: Seq[FileName] = redactedFiles
        .groupBy(file => file.fileNameNoExtension)
        .map { case (fileNameNoExtension, files) => RedactedFilesByName(fileNameNoExtension, files) }
        .filter(redactedFilesByName => redactedFilesByName.hasDuplicates)
        .flatMap(redactedFilesByName => redactedFilesByName.files)
        .toList

      val duplicateErrors: Seq[RedactedFilePairs] = redactedFilesWithDuplicateNames.map(dup =>
        RedactedFilePairs(None, duplicateFileNameError, dup.fileId, dup.filePath))

      val nonDuplicateRedactedFiles: Seq[FileName] = redactedFiles.diff(redactedFilesWithDuplicateNames)

      val matchedResults: Seq[RedactedResult] = nonDuplicateRedactedFiles.map { redactedFile =>
        val originalFileName = originalNameFor(redactedFile)
        val originalFiles = directoryFiles.files.filter(fileInDirectory => isOriginalFile(fileInDirectory, originalFileName))
        originalFiles match {
          case head :: Nil => RedactedFilePairs(Some(head.fileId), head.filePath, redactedFile.fileId, redactedFile.filePath)
          case Nil => RedactedFilePairs(None, noOriginalFileError, redactedFile.fileId, redactedFile.filePath)
          case _ => RedactedFilePairs(None, ambiguousOriginalFileError, redactedFile.fileId, redactedFile.filePath)
        }
      }

      matchedResults ++ duplicateErrors
    }.toList
  }

  private def toFileName(file: File): FileName = {
    val name = Paths.get(file.originalPath).getFileName.toString
    FileName(file.fileId, file.originalPath, name, removeExtension(name))
  }

  private def removeExtension(fileName: String): String = {
    val extensionSeparator = fileName.lastIndexOf('.')
    if (extensionSeparator > 0 && extensionSeparator < fileName.length - 1) {
      fileName.substring(0, extensionSeparator)
    } else {
      fileName
    }
  }

  private def directory(path: String): String = Option(Paths.get(path).getParent).map(_.toString).getOrElse("")

  private def isRedactedFile(file: FileName): Boolean = {
    file.fileNameNoExtension match {
      case RedactedFilePattern(_) => true
      case _ => false
    }
  }

  private def originalNameFor(redactedFile: FileName): String = {
    redactedFile.fileNameNoExtension match {
      case RedactedFilePattern(originalName) => originalName
      case _ => throw new IllegalArgumentException(s"${redactedFile.fileName} does not match redacted file pattern")
    }
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
