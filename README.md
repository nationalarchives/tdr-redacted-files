# TDR Redacted Files

This lambda is passed a list of file paths and if there is a file matching the redacted file pattern, either finds the
original file or returns an error.
If the file does not match the redacted file pattern, it returns nothing.

Given the following input:

```json
[
  {
    "fileId": "079bc416-180c-45cc-a943-7c6d63c21d57",
    "originalPath": "/a/path/file.txt"
  },
  {
    "fileId": "9e31f5f3-7240-4802-9442-766307fc9501",
    "originalPath": "/a/path/file_R1.txt"
  },
  {
    "fileId": "2cfc0597-53d4-4a10-aa5a-e49be49aaa9b",
    "originalPath": "/a/path/file2_R.txt"
  },
  {
    "fileId": "4ad0037f-45ae-410a-9e0f-31bece7cef85",
    "originalPath": "/another/path/file3_R.txt"
  },
  {
    "fileId": "6de7cc09-0bf2-4216-ae78-29b8f9ef6220",
    "originalPath": "/another/path/file3.txt"
  },
  {
    "fileId": "13671f42-5b15-4e55-95e9-607185b84bbd",
    "originalPath": "/another/path/file3.doc"
  },
  {
    "fileId": "f3a6f37e-c0fb-4fdd-b5a0-fe6dd31e57cb",
    "originalPath": "/a/path/file4_R.doc"
  },
  {
    "fileId": "4509ffee-69d2-48da-b771-070a4d3a376d",
    "originalPath": "/a/path/file4_R.pdf"
  },
  {
    "fileId": "8cb1078a-f990-4875-81e1-c4120fdd01f2",
    "originalPath": "/a/path/file5.pdf"
  }
]

```
It will group the files by directory
```scala
Map(
  "/a/path" -> List("/a/path/file.txt", "/a/path/file_R1.txt", "/a/path/file2_R.txt", "/a/path/file4_R.doc", "a/path/file4_R.pdf", "/a/path/file5.pdf"), 
  "/another/path" -> List("/another/path/file3_R.txt", "/another/path/file3.txt", "/another/path/file3.doc")  
)
```
----
For the `/a/path` directory, it will filter out any file which matches the pattern `_R\d*?$` without its file extension. This returns:

```scala
"/a/path/file4_R.pdf"
"/a/path/file4_R.doc"
"/a/path/file2_R.txt"
"/a/path/file_R1.txt"
```

It will then filter any redacted file names with the same name ignoring the file extension. This gives:
```scala
"/a/path/file4_R.pdf"
"/a/path/file4_R.doc"
```
These are returned with the error `DuplicateFileName`

The remaining redacted files are checked against the non redacted files for original file matches.

`file2_R.txt` needs to have a matching file called `file2.xxx` but this isn't in the original array so this returns an error of `NoOriginalFile`

`file_R1.txt` needs to have a matching file called `file.xxx` This is in the original array so this is returned as a matched pair.

-----
For the `/another/path` folder, this redacted file is found:
```scala
"/another/path/file3_R.txt"
```
There is only one so there is no duplicate, so it then checks the original file list for a match. 
We are looking for a file called `file3.xxx` There are two files which match this, `file3.txt` and `file3.doc` 
We can't tell which of these was the original file, so we return an `AmbiguousOriginalFile` error.

The lambda then returns this json:

```json
{
  "redactedFiles": [
    {
      "originalFileId": "079bc416-180c-45cc-a943-7c6d63c21d57",
      "originalFilePath": "/a/path/file.txt",
      "redactedFileId": "9e31f5f3-7240-4802-9442-766307fc9501",
      "redactedFilePath": "/a/path/file_R1.txt"
    }
  ],
  "errors": [
    {
      "fileId": "2cfc0597-53d4-4a10-aa5a-e49be49aaa9b",
      "cause": "NoOriginalFile"
    },
    {
      "fileId": "f3a6f37e-c0fb-4fdd-b5a0-fe6dd31e57cb",
      "cause": "DuplicateFileName"
    },
    {
      "fileId": "4509ffee-69d2-48da-b771-070a4d3a376d",
      "cause": "DuplicateFileName"
    },
    {
      "fileId": "4ad0037f-45ae-410a-9e0f-31bece7cef85",
      "cause": "AmbiguousOriginalFile"
    }
  ]
}
```
There is a [LambdaRunner](src/main/scala/uk/gov/nationalarchives/LambdaRunner.scala) class which will take a json string and run the Lambda. This can be used to test various inputs. 
