name := """play-java-akka-redis-pubsub"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

libraryDependencies += "biz.paluch.redis" % "lettuce" % "4.1.1.Final"

fork in test := true