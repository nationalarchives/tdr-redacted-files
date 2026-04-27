package uk.gov.nationalarchives

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1, TableFor2}
import uk.gov.nationalarchives.BackendCheckUtils._
import uk.gov.nationalarchives.RedactedFileMatcher._

import java.util.UUID

class RedactedFileMatcherTest extends AnyFlatSpec with TableDrivenPropertyChecks {

  private def toFile(fileName: String): File =
    File(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "standard", "1", "checksum", fileName, FileCheckResults(Nil, Nil, Nil))

  private def toFiles(fileNames: List[String]): List[File] = fileNames.map(toFile)

  val validTestData: TableFor2[List[String], Map[String, String]] = Table(
    ("files", "expectedPairs"),
    (List("MyDocument.updated_R.docx", "MyDocument.updated.docx"), Map("MyDocument.updated_R.docx" -> "MyDocument.updated.docx")),
    (List("DTP__R.docx", "DTP_.docx"), Map("DTP__R.docx" -> "DTP_.docx")),
    (List("x.txt", "x_R1.txt", "x_R2.txt"), Map("x_R1.txt" -> "x.txt", "x_R2.txt" -> "x.txt")),
    (List("x.txt", "x_R.txt", "x_R2.txt"), Map("x_R.txt" -> "x.txt", "x_R2.txt" -> "x.txt")),
    (List("Anothe_R13_R14Redacted_R15.docx", "Anothe_R13_R14Redacted.docx"), Map("Anothe_R13_R14Redacted_R15.docx" -> "Anothe_R13_R14Redacted.docx")),
    (List("Anothe_Redacted_R.docx", "Anothe_Redacted.docx"), Map("Anothe_Redacted_R.docx" -> "Anothe_Redacted.docx")),
    (List("DTP_R.docx", "DTP.docx"), Map("DTP_R.docx" -> "DTP.docx")),
    (List("DTP_R200.docx", "DTP.docx"), Map("DTP_R200.docx" -> "DTP.docx")),
    (List("Anothe_RRedacted_R1.docx", "Anothe_RRedacted.docx"), Map("Anothe_RRedacted_R1.docx" -> "Anothe_RRedacted.docx")),
    (List("Test_R1.txt", "Test_R2.txt", "Test_R4.txt", "Test.txt"), Map("Test_R1.txt" -> "Test.txt", "Test_R2.txt" -> "Test.txt", "Test_R4.txt" -> "Test.txt")),
    (List("DTP.docx", "DTP_R.pdf"), Map("DTP_R.pdf" -> "DTP.docx")),
    (List("DTP.docx", "DTP_R1.docx", "DTP_R2.pdf"), Map("DTP_R1.docx" -> "DTP.docx", "DTP_R2.pdf" -> "DTP.docx")),
    (List("DTP.pdf", "DTP_R1.docx", "DTP_R2.pdf"), Map("DTP_R1.docx" -> "DTP.pdf", "DTP_R2.pdf" -> "DTP.pdf")),
    (List("DTP.pdf", "DTP_R.docx"), Map("DTP_R.docx" -> "DTP.pdf")),
    (List("DTP", "DTP_R.png"), Map("DTP_R.png" -> "DTP")),
    (List("DTP", "DTP_R1.png", "DTP_R2.docx"), Map("DTP_R1.png" -> "DTP", "DTP_R2.docx" -> "DTP")),
    (List("MyDocument.updated", "MyDocument.updated_R.png"), Map("MyDocument.updated_R.png" -> "MyDocument.updated")),
    (List("DTP_R", "DTP"), Map("DTP_R" -> "DTP")),
    (List("DTP_R", "DTP.docx"), Map("DTP_R" -> "DTP.docx")),
    (List("DTP_R1", "DTP_R2", "DTP.docx"), Map("DTP_R1" -> "DTP.docx", "DTP_R2" -> "DTP.docx")),
  )

  val noRedactionTestData: TableFor1[List[String]] = Table(
    "files",
    List("DTP.docx_R", "DTP.docx"),
    List("DTP.docx_Ra", "DTP.docx"),
    List("DTP.updated.docx_R", "DTP.updated.docx"),
    List("DTP_r.docx", "DTP.docx"),
    List("DTP", "DTP.docx"),
    List("DTP_R .docx", "DTP.docx"),
    List("DTP_R.updated.docx", "DTP.updated.docx"),
    List("DTPR", "DTPR.docx"),
  )

  val redactionErrors: TableFor2[List[String], List[String]] = Table(
    ("files", "expectedErrors"),
    (List("DTP_R1.docx"), List(NoOriginalFile)),
    (List("DTP_R.docx"), List(NoOriginalFile)),
    (List("/dir1/DTP_R.docx", "/dir1/dir2/DTP.docx"), List(NoOriginalFile)),
    (List("/dir1/dir2/DTP_R.docx", "/dir1/DTP.docx"), List(NoOriginalFile)),
    (List("/dir1/dir2/DTP_R.docx", "/dir1/dir3/DTP.docx"), List(NoOriginalFile)),
    (List("DTP__R.docx", "DTP.docx"), List(NoOriginalFile)),
    (List("DTP.docx", "DTP_R.docx", "DTP_R.pdf"), List(DuplicateFileName, DuplicateFileName)),
    (List("DTP.docx", "DTP.pdf", "DTP_R1.docx"), List(AmbiguousOriginalFile)),
    (List("DTP", "DTP.docx", "DTP_R.png"), List(AmbiguousOriginalFile)),
    (List("DTP_R"), List(NoOriginalFile)),
    (List("DTP_R", "DTP_R.docx", "DTP.pdf"), List(DuplicateFileName, DuplicateFileName)),
  )

  forAll(validTestData) { (fileNames, expectedPairs) =>
    "getRedactedFiles" should s"return matched pairs for ${fileNames.mkString(", ")}" in {
      val results = getRedactedFiles(toFiles(fileNames))
      val pairs = results.collect { case pair: RedactedFilePairs => pair }
      val errors = results.collect { case error: RedactedErrors => error }

      errors shouldBe empty
      pairs.size should equal(expectedPairs.size)
      pairs.foreach(pair => expectedPairs(pair.redactedFilePath) should equal(pair.originalFilePath))
    }
  }

  forAll(noRedactionTestData) { fileNames =>
    "getRedactedFiles" should s"return no results for ${fileNames.mkString(", ")}" in {
      val results = getRedactedFiles(toFiles(fileNames))
      results shouldBe empty
    }
  }

  forAll(redactionErrors) { (fileNames, expectedErrors) =>
    "getRedactedFiles" should s"return expected errors for ${fileNames.mkString(", ")}" in {
      val results = getRedactedFiles(toFiles(fileNames))
      val errors = results.collect { case error: RedactedErrors => error }

      errors.size should equal(expectedErrors.size)
      errors.map(_.cause) should contain theSameElementsAs expectedErrors
    }
  }
}
