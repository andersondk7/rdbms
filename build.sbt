import Dependencies._

val scala_version = "2.13.10"

organization := "org.dka"
version      := "0.1.0"
scalaVersion := "2.13.10"


lazy val root = project
  .in(file("."))
  .settings(
    name := "bookBiz",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala_version,
    libraryDependencies ++= rdbmsDeps
  )
