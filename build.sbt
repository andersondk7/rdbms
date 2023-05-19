import Dependencies.*
import commandmatrix._

lazy val scala213 = "2.13.10"
lazy val scala322 = "3.2.2"
lazy val supportedScalaVersions = List(scala213, scala322)

ThisBuild / organization := "org.dka.rdbms"
ThisBuild / version := "0.4.8"
ThisBuild / scalaVersion := scala322

lazy val flywaySettings: Seq[Def.Setting[_]] = Seq(
  flywayUser := sys.env.getOrElse("BZ_USER", "defaultUser"),
  flywayPassword := sys.env.getOrElse("BZ_PASSWORD", "defaultPassword"),
  flywayUrl := s"jdbc:postgresql://localhost:5432/book_biz?&currentSchema=${sys.env.getOrElse("BZ_SCHEMA", "public")}"
)

inThisBuild(
  Seq(
    commands ++= CrossCommand.single(
      "test",
      matrices = Seq(common, db, anorm),
      dimensions = Seq(
        Dimension.scala(scala322),
        Dimension.platform()
      )
    )
  )
)

lazy val common = (projectMatrix in file("common"))
  .configs(IntegrationTest)
  .settings(
    libraryDependencies ++= commonDependencies,
    Defaults.itSettings
  )
  .jvmPlatform(scalaVersions = supportedScalaVersions)


lazy val anorm = (projectMatrix in file("anorm"))
  .configs(IntegrationTest)
  .settings(
    name := "anorm",
    libraryDependencies ++= anormDependencies,
    Defaults.itSettings
  )
  .dependsOn(common)
  .jvmPlatform(scalaVersions = Seq(scala322))

lazy val db = (projectMatrix in file("db"))
  .configs(IntegrationTest)
  .enablePlugins(FlywayPlugin)
  .settings(
    libraryDependencies ++= dbDependencies,
    flywaySettings,
    Defaults.itSettings
  )
  .dependsOn(common)
  .jvmPlatform(scalaVersions = Seq(scala322))

lazy val slick = (projectMatrix in file("slick"))
  .configs(IntegrationTest)
  .settings(
    scalaVersion := scala213,
    libraryDependencies ++= slickDependencies,
    Defaults.itSettings
  )
  .dependsOn(common)
  .jvmPlatform(scalaVersions = Seq(scala213))

lazy val rdbms = project
  .in(file("."))
  .configs(IntegrationTest)
  .aggregate(common.projectRefs ++ anorm.projectRefs ++ db.projectRefs ++ slick.projectRefs: _*)
  .settings(
    Defaults.itSettings
  )
