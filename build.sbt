import Dependencies._

ThisBuild / organization := "org.dka"
ThisBuild / version      := "0.1.2-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"


lazy val root = project
  .in(file("."))
  .settings(
    name := "bookBiz",
    libraryDependencies ++= rdbmsDeps
  )
