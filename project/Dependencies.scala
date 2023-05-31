import sbt.*

object Dependencies {

  private val anorm_version = "2.7.0"
  private val cats_version = "2.9.0"
  private val circe_version = "0.14.5"
  private val config_version = "1.4.2"
  private val hikaricp_version = "2.8.0"
  private val logback_version = "1.4.6"
  private val postgres_driver_version = "42.6.0"
  private val scalalogging_version = "3.9.5"
  private val scalactic_version = "3.2.15"
  private val scalatest_version = "3.2.15"
  private val slick_version = "3.4.1"
  private val zio_version = "2.0.13"
  private val zio_http_version = "3.0.0-RC2"

  private val catsCore = "org.typelevel" %% "cats-core" % cats_version

  private val circeCore = "io.circe" %% "circe-core" % circe_version
  private val circeGeneric = "io.circe" %% "circe-generic" % circe_version
  private val circeParser = "io.circe" %% "circe-parser" % circe_version

  private val logging = "com.typesafe.scala-logging" %% "scala-logging" % scalalogging_version

  private val scalatic = "org.scalactic" %% "scalactic" % scalactic_version
  private val scalaTest = "org.scalatest" %% "scalatest" % scalatest_version % "it,test"

  // anorm libs
  private val anorm = "org.playframework.anorm" %% "anorm" % anorm_version

  // java libs
  private val config = "com.typesafe" % "config" % config_version

  private val hikaricp = "com.zaxxer" % "HikariCP" % "5.0.1"
  private val postgresDriver = "org.postgresql" % "postgresql" % postgres_driver_version
  private val logBack = "ch.qos.logback" % "logback-classic" % logback_version

  // slick libs
  private val connectionPool = "com.typesafe.slick" % "slick-hikaricp_2.13" % slick_version
  private val slick = "com.typesafe.slick" % "slick_2.13" % slick_version

  // zio libs
  private val zio = "dev.zio" %% "zio" % zio_version
  private val zioTest = "dev.zio" %% "zio-test" % zio_version % Test

  private val zioHttp = "dev.zio" % "zio-http_3" % zio_http_version
  private val zioHttpTest = "dev.zio" % "zio-http_3" % zio_http_version % Test

  private val zioTestSbt = "dev.zio" %% "zio-test-sbt" % zio_version % Test

  val anormDependencies: Seq[ModuleID] = Seq(
    anorm,
    catsCore,
    circeCore,
    circeGeneric,
    circeParser,
    config,
    hikaricp,
    logBack,
    logging,
    postgresDriver,
    scalatic,
    scalaTest
  )

  val commonDependencies: Seq[ModuleID] = Seq(
    catsCore,
    config,
    logging,
    logBack,
    circeCore,
    circeGeneric,
    circeParser,
    scalaTest
  )

  val dbDependencies: Seq[ModuleID] = Seq(
    catsCore,
    logging,
    logBack,
    scalaTest
  )

  val slickDependencies: Seq[ModuleID] = Seq(
    catsCore,
    circeCore,
    circeGeneric,
    circeParser,
    connectionPool,
    logBack,
    logging,
    postgresDriver,
    scalatic,
    scalaTest,
    slick
  )

  val zioDependencies: Seq[ModuleID] = Seq(
    circeCore,
    circeGeneric,
    circeParser,
    scalatic,
    scalaTest,
    zio,
    zioHttp,
    zioTest,
    zioTestSbt,
    zioHttpTest
  )
}
