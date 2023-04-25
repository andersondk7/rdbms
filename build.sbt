import Dependencies._

ThisBuild / organization := "org.dka"
ThisBuild / version      := "0.1.2"
ThisBuild / scalaVersion := "2.13.10"

//lazy val CustomConfig = config("custom") extend Runtime

lazy val rdbms = project
  .in(file("rdbms"))
  .settings(
    libraryDependencies ++= rdbmsDeps
  )

lazy val customSettings: Seq[Def.Setting[_]] = Seq(
  flywayUser := sys.env.getOrElse("BZ_USER", "defaultUser"),
  flywayPassword := sys.env.getOrElse("BZ_PASSWORD", "defaultPassword"),
  flywayUrl := s"jdbc:postgresql://localhost:5432/book_biz?&currentSchema=${sys.env.getOrElse("BZ_SCHEMA", "public")}",
  flywayLocations += "filesystem:./flyway/src/main/resources/db/migration"
)

lazy val flyway = (project in file("."))
  .settings( customSettings )
  .enablePlugins(FlywayPlugin)

lazy val root = project.in(file("."))
  .aggregate(rdbms)