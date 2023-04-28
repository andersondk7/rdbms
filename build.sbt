import Dependencies._

ThisBuild / organization := "org.dka.rdbms"
ThisBuild / version      := "0.2.2-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"



lazy val common = project
  .in(file("common"))
  .settings(
    libraryDependencies ++= commonDeps
  )


lazy val slick = project
  .in(file("slick"))
  .enablePlugins(FlywayPlugin)
  .settings(
    libraryDependencies ++= slickDeps,
    flywaySettings
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
    slick
  )

