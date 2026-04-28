# TDR Redacted Files

This lambda is passed an object with an S3 key and bucket.
It gets that object from S3 which returns a json object.
The results key in that json is a list of file paths and if there is a file matching the redacted file pattern, either finds the
original file or returns an error.
If the file does not match the redacted file pattern, it returns nothing.
The example input here is only part of the full json object but these are the only fields checked. 

Given the following input:

```json
{
  "results": [
    {
      "fileId": "<file-id-1>",
      "originalPath": "/a/path/file.txt"
    },
    {
      "fileId": "<file-id-2>",
      "originalPath": "/a/path/file_R1.txt"
    },
    {
      "fileId": "<file-id-3>",
      "originalPath": "/a/path/file2_R.txt"
    },
    {
      "fileId": "<file-id-4>",
      "originalPath": "/another/path/file3_R.txt"
    },
    {
      "fileId": "<file-id-5>",
      "originalPath": "/another/path/file3.txt"
    },
    {
      "fileId": "<file-id-6>",
      "originalPath": "/another/path/file3.doc"
    },
    {
      "fileId": "<file-id-7>",
      "originalPath": "/a/path/file4_R.doc"
    },
    {
      "fileId": "<file-id-8>",
      "originalPath": "/a/path/file4_R.pdf"
    },
    {
      "fileId": "<file-id-9>",
      "originalPath": "/a/path/file5.pdf"
    },
    {
      "fileId": "<file-id-10>",
      "originalPath": "/a/path/file6"
    },
    {
      "fileId": "<file-id-11>",
      "originalPath": "/a/path/file6_R.png"
    },
    {
      "fileId": "<file-id-12>",
      "originalPath": "/a/path/file7.docx"
    },
    {
      "fileId": "<file-id-13>",
      "originalPath": "/a/path/file7_R"
    }
  ]
}

```
It will group the files by directory
```scala
Map(
  "/a/path" -> List("/a/path/file.txt", "/a/path/file_R1.txt", "/a/path/file2_R.txt", "/a/path/file4_R.doc", "/a/path/file4_R.pdf", "/a/path/file5.pdf", "/a/path/file6", "/a/path/file6_R.png", "/a/path/file7.docx", "/a/path/file7_R"), 
  "/another/path" -> List("/another/path/file3_R.txt", "/another/path/file3.txt", "/another/path/file3.doc")  
)
```
----
For the `/a/path` directory, it will filter out any file whose name (without extension, if present) matches the pattern `_R\d*?$`. This returns:

```scala
"/a/path/file4_R.pdf"
"/a/path/file4_R.doc"
"/a/path/file2_R.txt"
"/a/path/file_R1.txt"
"/a/path/file6_R.png"
"/a/path/file7_R"
```

It will then filter any redacted file names with the same name ignoring the file extension. This gives:
```scala
"/a/path/file4_R.pdf"
"/a/path/file4_R.doc"
```
These are returned with the error `DuplicateFileName`

The remaining redacted files are checked against the non redacted files for original file matches.

`file2_R.txt` needs to have a matching file called `file2.xxx` or `file2` but this isn't in the original array so this returns an error of `NoOriginalFile`

`file_R1.txt` needs to have a matching file called `file.xxx` or `file` This is in the original array so this is returned as a matched pair.

`file6_R.png` needs to have a matching file called `file6.xxx` or `file6`. The extensionless file `file6` is in the original array so this is returned as a matched pair.

`file7_R` has no extension. Its name without extension is still `file7_R` which matches the redacted pattern. It needs a matching file called `file7.xxx` or `file7`. `file7.docx` is in the original array so this is returned as a matched pair.

-----
For the `/another/path` folder, this redacted file is found:
```scala
"/another/path/file3_R.txt"
```
There is only one so there is no duplicate, so it then checks the original file list for a match. 
We are looking for a file called `file3.xxx` or `file3` There are two files which match this, `file3.txt` and `file3.doc` 
We can't tell which of these was the original file, so we return an `AmbiguousOriginalFile` error.

The lambda then returns this json:

```json
{
  "redactedFiles": [
    {
      "originalFileId": "<file-id-1>",
      "originalFilePath": "/a/path/file.txt",
      "redactedFileId": "<file-id-2>",
      "redactedFilePath": "/a/path/file_R1.txt"
    },
    {
      "originalFileId": "<file-id-10>",
      "originalFilePath": "/a/path/file6",
      "redactedFileId": "<file-id-11>",
      "redactedFilePath": "/a/path/file6_R.png"
    },
    {
      "originalFileId": "<file-id-12>",
      "originalFilePath": "/a/path/file7.docx",
      "redactedFileId": "<file-id-13>",
      "redactedFilePath": "/a/path/file7_R"
    }
  ],
  "errors": [
    {
      "fileId": "<file-id-3>",
      "cause": "NoOriginalFile"
    },
    {
      "fileId": "<file-id-7>",
      "cause": "DuplicateFileName"
    },
    {
      "fileId": "<file-id-8>",
      "cause": "DuplicateFileName"
    },
    {
      "fileId": "<file-id-4>",
      "cause": "AmbiguousOriginalFile"
    }
  ]
}
```
There is a [LambdaRunner](src/main/scala/uk/gov/nationalarchives/LambdaRunner.scala) class which will take a json string and run the Lambda. This can be used to test various inputs. 
