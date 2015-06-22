name := "resumable-play-example"

version := "1.0"

scalaVersion := "2.10.5"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

fork in run := true