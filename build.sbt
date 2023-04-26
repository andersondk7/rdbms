import Dependencies._

ThisBuild / organization := "org.dka"
ThisBuild / version      := "0.1.8"
ThisBuild / scalaVersion := "2.13.10"


lazy val root = project
  .in(file("."))
  .enablePlugins(FlywayPlugin)
  .settings(
    libraryDependencies ++= rdbmsDeps,
    customSettings
  )

lazy val customSettings: Seq[Def.Setting[_]] = Seq(
  flywayUser := sys.env.getOrElse("BZ_USER", "defaultUser"),
  flywayPassword := sys.env.getOrElse("BZ_PASSWORD", "defaultPassword"),
  flywayUrl := s"jdbc:postgresql://localhost:5432/book_biz?&currentSchema=${sys.env.getOrElse("BZ_SCHEMA", "public")}"
)

