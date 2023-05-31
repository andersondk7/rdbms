//  https://github.com/sbt/sbt-native-packager
 addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.7")

// https://github.com/sbt/sbt-git
// addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

// https://github.com/scalameta/sbt-scalafmt/releases
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "7.4.0")

// cross compiling ...
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.9.0")
addSbtPlugin("com.indoorvivants" % "sbt-commandmatrix" % "0.0.5")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

libraryDependencies += "org.postgresql" % "postgresql" % "42.6.0"

