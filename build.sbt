import Dependencies._

lazy val scala213 = "2.13.10"
lazy val scala322 = "3.2.2"
lazy val supportedScalaVersions = List(scala213, scala322)

ThisBuild / organization := "org.dka.rdbms"
ThisBuild / version      := "0.4.5-SNAPSHOT"
ThisBuild / scalaVersion := scala213

lazy val common = project
  .in(file("common"))
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= commonDeps
  )


lazy val db = project
  .in(file("db"))
  .enablePlugins(FlywayPlugin)
  .settings(
    libraryDependencies ++= dbDeps,
    crossScalaVersions := Seq(scala322),
      flywaySettings
  )
  .dependsOn(common)

lazy val slick = project
  .in(file("slick"))
  .settings(
    crossScalaVersions := List(scala213),
    libraryDependencies ++= slickDeps,
  )
  .dependsOn(common)

lazy val flywaySettings: Seq[Def.Setting[_]] = Seq(
  flywayUser := sys.env.getOrElse("BZ_USER", "defaultUser"),
  flywayPassword := sys.env.getOrElse("BZ_PASSWORD", "defaultPassword"),
  flywayUrl := s"jdbc:postgresql://localhost:5432/book_biz?&currentSchema=${sys.env.getOrElse("BZ_SCHEMA", "public")}"
)

lazy val rdbms = project
  .in(file("."))
  .aggregate(
    common,
    db,
    slick
  )
  .settings(
    crossScalaVersions := Nil
  )

