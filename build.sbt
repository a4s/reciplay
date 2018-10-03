name := """reciplay"""
organization := "a4s"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.13.0"
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.13.0-play26"
libraryDependencies += "org.reactivemongo" %% "reactivemongo-play-json" % "0.13.0-play26"

