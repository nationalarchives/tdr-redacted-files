import sbt._


object Dependencies {
  private val circeVersion = "0.14.5"

  lazy val backendCheckUtils = "uk.gov.nationalarchives" %% "tdr-backend-checks-utils" % "0.1.20"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.16"
  lazy val wiremock = "com.github.tomakehurst" % "wiremock" % "3.0.0"
}
