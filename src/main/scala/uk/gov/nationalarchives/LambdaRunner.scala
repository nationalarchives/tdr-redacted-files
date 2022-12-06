package uk.gov.nationalarchives

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

object LambdaRunner extends App {
//  val body =
//    """{"results": [{"fileId": "079bc416-180c-45cc-a943-7c6d63c21d57","originalPath": "/a/path/file.txt"}]}"""
  val body =
"""
  |{
  |  "results": [
  |    {
  |      "fileId": "20d80488-d247-47cf-8687-be26de2558b5",
  |      "originalPath": "smallfile/subfolder/subfolder-nested/subfolder-nested-1.txt",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368632,
  |            "fileId": "20d80488-d247-47cf-8687-be26de2558b5"
  |          }
  |        },
  |        {
  |          "fileId": "20d80488-d247-47cf-8687-be26de2558b5",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": "txt",
  |              "identificationBasis": "Extension",
  |              "puid": "x-fmt/111"
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "20d80488-d247-47cf-8687-be26de2558b5",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "26d1b5d9-305f-48a9-bdbd-16f17bafaefe",
  |      "originalPath": "smallfile/subfolder/subfolder-nested/subfolder-nested-2.txt",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368594,
  |            "fileId": "26d1b5d9-305f-48a9-bdbd-16f17bafaefe"
  |          }
  |        },
  |        {
  |          "fileId": "26d1b5d9-305f-48a9-bdbd-16f17bafaefe",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": "txt",
  |              "identificationBasis": "Extension",
  |              "puid": "x-fmt/111"
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "26d1b5d9-305f-48a9-bdbd-16f17bafaefe",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "5f940343-6915-41ec-9f6d-18958e4b1b3e",
  |      "originalPath": "smallfile/index5",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368565,
  |            "fileId": "5f940343-6915-41ec-9f6d-18958e4b1b3e"
  |          }
  |        },
  |        {
  |          "fileId": "5f940343-6915-41ec-9f6d-18958e4b1b3e",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "5f940343-6915-41ec-9f6d-18958e4b1b3e",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "e68368e7-3d45-455b-a7a9-f73d7e30245b",
  |      "originalPath": "smallfile/index3",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368680,
  |            "fileId": "e68368e7-3d45-455b-a7a9-f73d7e30245b"
  |          }
  |        },
  |        {
  |          "fileId": "e68368e7-3d45-455b-a7a9-f73d7e30245b",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "e68368e7-3d45-455b-a7a9-f73d7e30245b",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "4139e3d9-ed59-473a-9d88-3879d44a56d8",
  |      "originalPath": "smallfile/subfolder/subfolder1.txt",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368632,
  |            "fileId": "4139e3d9-ed59-473a-9d88-3879d44a56d8"
  |          }
  |        },
  |        {
  |          "fileId": "4139e3d9-ed59-473a-9d88-3879d44a56d8",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": "txt",
  |              "identificationBasis": "Extension",
  |              "puid": "x-fmt/111"
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "4139e3d9-ed59-473a-9d88-3879d44a56d8",
  |            "sha256Checksum": "be2d5c8027fd9144be7e8dbf106bbdfbb0837e1b321748aa204a66a2431069de"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "087b6391-96c8-42b0-baef-6462a59b663b",
  |      "originalPath": "smallfile/index4",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368700,
  |            "fileId": "087b6391-96c8-42b0-baef-6462a59b663b"
  |          }
  |        },
  |        {
  |          "fileId": "087b6391-96c8-42b0-baef-6462a59b663b",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "087b6391-96c8-42b0-baef-6462a59b663b",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "58d3c8d1-4798-4c56-8db1-d076d2faa324",
  |      "originalPath": "smallfile/index",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368778,
  |            "fileId": "58d3c8d1-4798-4c56-8db1-d076d2faa324"
  |          }
  |        },
  |        {
  |          "fileId": "58d3c8d1-4798-4c56-8db1-d076d2faa324",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "58d3c8d1-4798-4c56-8db1-d076d2faa324",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "84296921-b50a-4b55-ba64-2654bc639522",
  |      "originalPath": "smallfile/index2",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368802,
  |            "fileId": "84296921-b50a-4b55-ba64-2654bc639522"
  |          }
  |        },
  |        {
  |          "fileId": "84296921-b50a-4b55-ba64-2654bc639522",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "84296921-b50a-4b55-ba64-2654bc639522",
  |            "sha256Checksum": "676d060b18128b1cec7deb55e60ca86432ef48c0af977a155375802e011d16a6"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "ad2611f2-68f2-4526-b0ec-58e99542b86e",
  |      "originalPath": "smallfile/index6",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368807,
  |            "fileId": "ad2611f2-68f2-4526-b0ec-58e99542b86e"
  |          }
  |        },
  |        {
  |          "fileId": "ad2611f2-68f2-4526-b0ec-58e99542b86e",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "ad2611f2-68f2-4526-b0ec-58e99542b86e",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "33a61f4e-149c-4f6b-a3fe-924b4f306d07",
  |      "originalPath": "smallfile/index7",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368927,
  |            "fileId": "33a61f4e-149c-4f6b-a3fe-924b4f306d07"
  |          }
  |        },
  |        {
  |          "fileId": "33a61f4e-149c-4f6b-a3fe-924b4f306d07",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "33a61f4e-149c-4f6b-a3fe-924b4f306d07",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "59ac4cb3-da4a-48ea-b5ba-157d5f50a3b2",
  |      "originalPath": "smallfile/index8",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368942,
  |            "fileId": "59ac4cb3-da4a-48ea-b5ba-157d5f50a3b2"
  |          }
  |        },
  |        {
  |          "fileId": "59ac4cb3-da4a-48ea-b5ba-157d5f50a3b2",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "59ac4cb3-da4a-48ea-b5ba-157d5f50a3b2",
  |            "sha256Checksum": "87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7"
  |          }
  |        }
  |      ]
  |    },
  |    {
  |      "fileId": "723ca828-a7be-45c0-b95c-fbd3c098f464",
  |      "originalPath": "smallfile/empty/notempty/file",
  |      "consignmentId": "cedba409-c806-439f-8982-943afb03c85a",
  |      "userId": "030cf12c-8d5d-46b9-b86a-38e0920d0e1a",
  |      "results": [
  |        {
  |          "antivirus": {
  |            "software": "yara",
  |            "softwareVersion": "4.2.0",
  |            "databaseVersion": "$LATEST",
  |            "result": "",
  |            "datetime": 1670333368881,
  |            "fileId": "723ca828-a7be-45c0-b95c-fbd3c098f464"
  |          }
  |        },
  |        {
  |          "fileId": "723ca828-a7be-45c0-b95c-fbd3c098f464",
  |          "software": "Droid",
  |          "softwareVersion": "6.6.0-rc2",
  |          "binarySignatureFileVersion": "109",
  |          "containerSignatureFileVersion": "20221102",
  |          "method": "pronom",
  |          "matches": [
  |            {
  |              "extension": null,
  |              "identificationBasis": "",
  |              "puid": null
  |            }
  |          ]
  |        },
  |        {
  |          "checksum": {
  |            "fileId": "723ca828-a7be-45c0-b95c-fbd3c098f464",
  |            "sha256Checksum": "800376ce11b0659bd222e73c1ace166a68cf0c459aa20a867ffb6c225065b629"
  |          }
  |        }
  |      ]
  |    }
  |  ]
  |}
  |
  |""".stripMargin
  val baos = new ByteArrayInputStream(body.getBytes())
  val output = new ByteArrayOutputStream()
  new Lambda().run(baos, output)
  val res = output.toByteArray.map(_.toChar).mkString
  println(res)
}
