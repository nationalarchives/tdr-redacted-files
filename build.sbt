import Dependencies._


ThisBuild / scalaVersion     := "3.9.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "uk.gov.nationalarchives"

lazy val root = (project in file("."))
  .settings(
    name := "tdr-redacted-files",
    libraryDependencies ++= Seq(
      backendCheckUtils,
      tdrStatuses,
      circeCore,
      circeParser,
      circeGeneric,
      scalaTest % Test,
      wiremock % Test
    ),
    assembly / assemblyJarName := "redacted-files.jar"
  )

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

(assembly / assemblyOutputPath) := Def.uncached {
  baseDirectory.value / "target" / "scala-2.13" / (assembly / assemblyJarName).value
}

(Test / fork) := true
(Test / javaOptions) += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.conf"
(Test / envVars) := Map("AWS_ACCESS_KEY_ID" -> "test", "AWS_SECRET_ACCESS_KEY" -> "test", "S3_ENDPOINT" -> "http://localhost:9005")
