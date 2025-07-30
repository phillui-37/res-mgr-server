name := "res-mgr-server"
organization := "xyz.kgy.production"
version := "1.0-SNAPSHOT"

scalaVersion := "3.7.1"

javaOptions ++= Seq("-Dfile.encoding=UTF-8")

// Enable the sbt-native-packager plugin
enablePlugins(JavaAppPackaging)

val ZioVersion = "2.1.20"
val ZioHttpVersion = "3.3.3"
val ZioJsonVersion = "0.7.44"
val ZioLoggingVersion = "2.5.1"
val LogbackVersion = "1.5.18"
val CatsVersion = "2.13.0"
val SlickVersion = "3.6.1"
val HikariCPVersion = "6.0.0"
val FlywayVersion = "10.10.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZioVersion,
  "dev.zio" %% "zio-http" % ZioHttpVersion,
  "dev.zio" %% "zio-json" % ZioJsonVersion,
  "dev.zio" %% "zio-logging" % ZioLoggingVersion,
  "dev.zio" %% "zio-logging-slf4j2" % ZioLoggingVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
  "com.zaxxer" % "HikariCP" % HikariCPVersion,
  "org.postgresql" % "postgresql" % "42.7.7",
  "org.typelevel" %% "cats-core" % CatsVersion,
  "org.typelevel" %% "cats-kernel" % CatsVersion,
  "dev.zio" %% "zio-test" % ZioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % ZioVersion % Test,
  "org.flywaydb" % "flyway-core" % FlywayVersion,
  "org.flywaydb" % "flyway-database-postgresql" % FlywayVersion,
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
