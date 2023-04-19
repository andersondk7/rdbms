import sbt.*


object Dependencies {

  private val cats_version = "2.8.0"
  private val postgres_driver_version = "42.6.0"
  private val hikaricp_version = "2.8.0"
  private val slick_version = "3.4.1"
  private val pureConfig_version = "0.17.2"


  private val catsCore = "org.typelevel" %% "cats-core" % cats_version
  private val slick = "com.typesafe.slick" %% "slick" % slick_version
  private val connectionPool = "com.typesafe.slick" %% "slick-hikaricp" % slick_version
  private val postgresDriver = "org.postgresql" % "postgresql" % postgres_driver_version
  private val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfig_version
  private val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
  private val logBack = "ch.qos.logback" % "logback-classic" % "1.3.6"
  private val scalatic = "org.scalactic" %% "scalactic" % "3.2.13"
  private val scalaTest = "org.scalatest" %% "scalatest" % "3.2.13" % "test"

  val rdbmsDeps = Seq(
  catsCore,
  slick,
  connectionPool,
  postgresDriver,
  pureConfig,
  logging,
  logBack,
  scalatic,
  scalaTest
  )
}
