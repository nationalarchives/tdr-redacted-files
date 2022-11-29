package uk.gov.nationalarchives

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

object LambdaRunner extends App {
  val body =
    """
      |[
      |  {
      |    "fileId": "079bc416-180c-45cc-a943-7c6d63c21d57",
      |    "originalPath": "/a/path/file.txt"
      |  },
      |  {
      |    "fileId": "9e31f5f3-7240-4802-9442-766307fc9501",
      |    "originalPath": "/a/path/file_R1.txt"
      |  },
      |  {
      |    "fileId": "2cfc0597-53d4-4a10-aa5a-e49be49aaa9b",
      |    "originalPath": "/a/path/file2_R.txt"
      |  },
      |  {
      |    "fileId": "4ad0037f-45ae-410a-9e0f-31bece7cef85",
      |    "originalPath": "/another/path/file3_R.txt"
      |  },
      |  {
      |    "fileId": "6de7cc09-0bf2-4216-ae78-29b8f9ef6220",
      |    "originalPath": "/another/path/file3.txt"
      |  },
      |  {
      |    "fileId": "13671f42-5b15-4e55-95e9-607185b84bbd",
      |    "originalPath": "/another/path/file3.doc"
      |  },
      |  {
      |    "fileId": "37845ce7-7bce-4411-a569-608cbd2071e7",
      |    "originalPath": "/a/path/file4_R.doc"
      |  },
      |  {
      |    "fileId": "4509ffee-69d2-48da-b771-070a4d3a376d",
      |    "originalPath": "/a/path/file4_R.pdf"
      |  },
      |  {
      |    "fileId": "8cb1078a-f990-4875-81e1-c4120fdd01f2",
      |    "originalPath": "/a/path/file5.pdf"
      |  }
      |]
      |""".stripMargin
  val baos = new ByteArrayInputStream(body.getBytes())
  val output = new ByteArrayOutputStream()
  new Lambda().run(baos, output)
  val res = output.toByteArray.map(_.toChar).mkString
  println(res)
}
