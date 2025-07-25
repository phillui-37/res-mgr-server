import play.sbt.PlayScala

name := "res-mgr-server"
organization := "xyz.kgy.production"
version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "3.7.1"

val PekkoVersion = "1.1.5"
val CatsVersion = "2.13.0"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "com.typesafe.slick" %% "slick" % "3.6.1",
  "org.postgresql" % "postgresql" % "42.7.7",
  "org.typelevel" %% "cats-core" % CatsVersion,
  "org.typelevel" %% "cats-kernel" % CatsVersion,
)

// Adds additional packages into Twirl
// TwirlKeys.templateImports += "xyz.kgy.production.controllers._"

// Adds additional packages into conf/routes
// RoutesKeys.routesImport += "xyz.kgy.production.binders._"
