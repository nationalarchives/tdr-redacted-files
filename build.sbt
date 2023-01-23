import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "uk.gov.nationalarchives"

lazy val root = (project in file("."))
  .settings(
    name := "tdr-redacted-files",
    libraryDependencies ++= Seq(
      backendCheckUtils,
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
