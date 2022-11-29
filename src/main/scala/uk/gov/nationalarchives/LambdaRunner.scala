package uk.gov.nationalarchives

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

object LambdaRunner extends App {
  val body =
    """[{"fileId": "079bc416-180c-45cc-a943-7c6d63c21d57","originalPath": "/a/path/file.txt"}]"""
  val baos = new ByteArrayInputStream(body.getBytes())
  val output = new ByteArrayOutputStream()
  new Lambda().run(baos, output)
  val res = output.toByteArray.map(_.toChar).mkString
  println(res)
}
