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

  private val anorm = "org.playframework.anorm" %% "anorm" % anorm_version
  private val catsCore = "org.typelevel" %% "cats-core" % cats_version
  private val circeCore = "io.circe" %% "circe-core" % circe_version
  private val circeGeneric = "io.circe" %% "circe-generic" % circe_version
  private val circeParser = "io.circe" %% "circe-parser" % circe_version
  private val logging = "com.typesafe.scala-logging" %% "scala-logging" % scalalogging_version
  private val scalatic = "org.scalactic" %% "scalactic" % scalactic_version
  private val scalaTest = "org.scalatest" %% "scalatest" % scalatest_version % "it,test"

  // java libs
  private val config = "com.typesafe" % "config" % config_version
//  private val hikaricp = "com.zaxxer" % "HikariCP" % hikaricp_version
  private val hikaricp = "com.zaxxer" % "HikariCP" % "5.0.1"
  private val postgresDriver = "org.postgresql" % "postgresql" % postgres_driver_version
  private val logBack = "ch.qos.logback" % "logback-classic" % logback_version

  // slick libs
  private val connectionPool = "com.typesafe.slick" % "slick-hikaricp_2.13" % slick_version
  private val slick = "com.typesafe.slick" % "slick_2.13" % slick_version

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
}
