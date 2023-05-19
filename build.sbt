import Dependencies._

lazy val scala213 = "2.13.10"
lazy val scala322 = "3.2.2"
lazy val supportedScalaVersions = List(scala213, scala322)

ThisBuild / organization := "org.dka.rdbms"
ThisBuild / version := "0.4.8"
ThisBuild / scalaVersion := scala322
val ideScala = scala322

lazy val anorm = project
  .in(file("anorm"))
  .configs(IntegrationTest)
  .settings(
    scalaVersion := scala322,
    libraryDependencies ++= anormDependencies,
    Defaults.itSettings
  )
  .dependsOn(common)

lazy val common = project
  .in(file("common"))
  .configs(IntegrationTest)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= commonDependencies,
    Defaults.itSettings
  )

lazy val db = project
  .in(file("db"))
  .configs(IntegrationTest)
  .enablePlugins(FlywayPlugin)
  .settings(
    scalaVersion := scala322,
    libraryDependencies ++= dbDependencies,
    flywaySettings,
    Defaults.itSettings
  )
  .dependsOn(common)

lazy val slick = project
  .in(file("slick"))
  .configs(IntegrationTest)
  .settings(
    scalaVersion := scala213,
    libraryDependencies ++= slickDependencies,
    Defaults.itSettings
  )
  .dependsOn(common)

lazy val flywaySettings: Seq[Def.Setting[_]] = Seq(
  flywayUser := sys.env.getOrElse("BZ_USER", "defaultUser"),
  flywayPassword := sys.env.getOrElse("BZ_PASSWORD", "defaultPassword"),
  flywayUrl := s"jdbc:postgresql://localhost:5432/book_biz?&currentSchema=${sys.env.getOrElse("BZ_SCHEMA", "public")}"
)

lazy val rdbms = project
  .in(file("."))
  .configs(IntegrationTest)
  .aggregate(
    common,
    db,
//    slick,
    anorm
  )
  .settings(
    crossScalaVersions := Nil,
    Defaults.itSettings
  )
