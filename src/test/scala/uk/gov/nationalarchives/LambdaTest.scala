package uk.gov.nationalarchives

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{anyUrl, get, ok, put, urlEqualTo}
import com.github.tomakehurst.wiremock.http.RequestMethod
import io.circe.Printer
import io.circe.Printer.noSpaces
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.auto._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1, TableFor2}
import uk.gov.nationalarchives.BackendCheckUtils._

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID
import scala.jdk.CollectionConverters._

class LambdaTest extends AnyFlatSpec with TableDrivenPropertyChecks with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    wiremockS3Server.stubFor(put(anyUrl()).willReturn(ok()))
    wiremockS3Server.start()
  }

  override def afterAll(): Unit = {
    wiremockS3Server.stop()
  }

  val validTestData: TableFor2[List[String], Map[String, String]] = Table(
    ("files", "result"),
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
  )

  val noRedactionTestData: TableFor1[List[String]] = Table(
    "files",
    List("DTP.docx_R", "DTP.docx"),
    List("DTP.docx_Ra", "DTP.docx"),
    List("DTP.updated.docx_R", "DTP.updated.docx"),
    List("DTP_r.docx", "DTP.docx"),
    List("DTP_R", "DTP"),
    List("DTP", "DTP.docx"),
    List("DTP_R .docx", "DTP.docx"),
    List("DTP_R.updated.docx", "DTP.updated.docx"),
    List("DTPR", "DTPR.docx"),
  )

  val redactionErrors: TableFor2[List[String], List[String]] = Table(
    ("files", "errors"),
    (List("DTP_R1.docx"), List("NoOriginalFile")),
    (List("DTP_R.docx"), List("NoOriginalFile")),
    (List("/dir1/DTP_R.docx", "/dir1/dir2/DTP.docx"), List("NoOriginalFile")),
    (List("/dir1/dir2/DTP_R.docx", "/dir1/DTP.docx"), List("NoOriginalFile")),
    (List("/dir1/dir2/DTP_R.docx", "/dir1/dir3/DTP.docx"), List("NoOriginalFile")),
    (List("DTP__R.docx", "DTP.docx"), List("NoOriginalFile")),
    (List("DTP.docx", "DTP_R.docx", "DTP_R.pdf"), List("DuplicateFileName", "DuplicateFileName")),
    (List("DTP.docx, DTP.pdf, DTP_R1.docx, DTP_R2.pdf"), List("AmbiguousOriginalFile")),
  )


  forAll(validTestData) { (files, results) =>
    "run" should s"return the expected original file path ${files.mkString(",")}" in {
      val result = runLambda(files)

      result.redactedFiles.size should equal(results.size)
      result.redactedFiles.foreach(file => {
        results(file.redactedFilePath) should equal(file.originalFilePath)
      })
    }
  }

  forAll(noRedactionTestData) { files =>
    "run" should s"return no results for files ${files.mkString(",")}" in {
      val result = runLambda(files)
      result.redactedFiles.size should equal(0)
      result.errors.size should equal(0)
    }
  }

  forAll(redactionErrors) { (files, errors) =>
    "run" should s"return the expected errors ${files.mkString(",")}" in {
      val result = runLambda(files)

      errors.size should equal(result.errors.size)
      result.errors.map(_.cause).forall(errors.contains)
    }
  }

  val wiremockS3Server = new WireMockServer(9005)

  private def runLambda(files: List[String]): RedactedResults = {
    val s3Input = setupS3(files)
    val baos = new ByteArrayInputStream(s3Input.getBytes())
    val output = new ByteArrayOutputStream()
    new Lambda().run(baos, output)
    output.toByteArray.map(_.toChar).mkString

    wiremockS3Server.getAllServeEvents.asScala.find(_.getRequest.getMethod == RequestMethod.PUT)
      .flatMap(ev => {
        val bodyString = ev.getRequest.getBodyAsString.split("\r\n")(1)
        decode[Input](bodyString).toOption
      }).get.redactedResults
  }

  private def setupS3(files: List[String]): String = {
    val mappedFiles = files
      .map(fileName => File(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "standard", "1", "checksum", fileName, Some("source-bucket"), Some("object/key"), FileCheckResults(Nil, Nil, Nil)))
    val inputJson = Input(mappedFiles, RedactedResults(Nil, Nil), StatusResult(Nil))
      .asJson.printWith(Printer.noSpaces)
    val s3Input = S3Input("testKey", "testBucket")
    putJsonFile(s3Input, inputJson).asJson.printWith(noSpaces)
  }

  private def putJsonFile(s3Input: S3Input, inputJson: String): S3Input = {
    wiremockS3Server
      .stubFor(get(urlEqualTo(s"/${s3Input.bucket}/${s3Input.key}")).willReturn(ok(inputJson)))
    s3Input
  }
}
