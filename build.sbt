import AssemblyKeys._

assemblySettings

name := "schale"

version := "1.0"

// http://www.scala-sbt.org/0.13/docs/Cross-Build.html#Cross-Building+a+Project
crossScalaVersions := Seq("2.10.4", "2.11.7")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

jarName in assembly := "schale.jar"

test in assembly := {}
