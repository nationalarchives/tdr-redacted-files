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
import uk.gov.nationalarchives.BackendCheckUtils._
import uk.gov.nationalarchives.RedactedFileMatcher._

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID
import scala.jdk.CollectionConverters._

class LambdaTest extends AnyFlatSpec with BeforeAndAfterAll {
  val wiremockS3Server = new WireMockServer(9005)

  override def beforeAll(): Unit = {
    wiremockS3Server.stubFor(put(anyUrl()).willReturn(ok()))
    wiremockS3Server.start()
  }

  override def afterAll(): Unit = {
    wiremockS3Server.stop()
  }

  "run" should "read from S3, match redacted files and write results back to S3" in {
    val files = List("DTP_R.docx", "DTP.docx", "file_R1.txt")
    val result = runLambda(files)

    result.redactedFiles.size should equal(1)
    result.redactedFiles.head.redactedFilePath should equal("DTP_R.docx")
    result.redactedFiles.head.originalFilePath should equal("DTP.docx")
    result.errors.size should equal(1)
    result.errors.head.cause should equal(noOriginalFileError)
  }

  "run" should "handle extensionless original files through S3 round-trip" in {
    val files = List("DTP", "DTP_R.png")
    val result = runLambda(files)

    result.redactedFiles.size should equal(1)
    result.redactedFiles.head.originalFilePath should equal("DTP")
    result.redactedFiles.head.redactedFilePath should equal("DTP_R.png")
    result.errors shouldBe empty
  }

  "run" should "handle extensionless redacted files through S3 round-trip" in {
    val files = List("DTP.docx", "DTP_R")
    val result = runLambda(files)

    result.redactedFiles.size should equal(1)
    result.redactedFiles.head.originalFilePath should equal("DTP.docx")
    result.redactedFiles.head.redactedFilePath should equal("DTP_R")
    result.errors shouldBe empty
  }

  private def runLambda(files: List[String]): RedactedResults = {
    wiremockS3Server.resetRequests()
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
    val s3Input = S3Input(s"testKey-${UUID.randomUUID()}", "testBucket")
    putJsonFile(s3Input, inputJson).asJson.printWith(noSpaces)
  }

  private def putJsonFile(s3Input: S3Input, inputJson: String): S3Input = {
    wiremockS3Server
      .stubFor(get(urlEqualTo(s"/${s3Input.bucket}/${s3Input.key}")).willReturn(ok(inputJson)))
    s3Input
  }
}
