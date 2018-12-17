import sbtrelease.Version

val embeddedKafkaVersion = "2.1.0-SNAPSHOT"
val confluentVersion = "5.0.1"
val akkaVersion = "2.5.18"

lazy val publishSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra :=
    <scm>
      <url>https://github.com/embeddedkafka/embedded-kafka-schema-registry</url>
      <connection>scm:git:git@github.com:embeddedkafka/embedded-kafka-schema-registry.git</connection>
    </scm>
      <developers>
        <developer>
          <id>manub</id>
          <name>Emanuele Blanco</name>
          <url>http://twitter.com/manub</url>
        </developer>
      </developers>
)

lazy val releaseSettings = Seq(
  releaseVersionBump := Version.Bump.Minor,
  releaseCrossBuild := true
)

lazy val commonSettings = Seq(
  organization := "io.github.embeddedkafka",
  scalaVersion := "2.12.7",
  crossScalaVersions := Seq("2.12.7", "2.11.12"),
  homepage := Some(url("https://github.com/embeddedkafka/embedded-kafka-schema-registry")),
  parallelExecution in Test := false,
  logBuffered in Test := false,
  fork in Test := true,
  javaOptions ++= Seq("-Xms512m", "-Xmx2048m"),
  scalacOptions += "-deprecation",
  scalafmtOnCompile := true
)

lazy val commonLibrarySettings = libraryDependencies ++= Seq(
  "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion,
  "io.confluent" % "kafka-avro-serializer" % confluentVersion,
  "io.confluent" % "kafka-schema-registry" % confluentVersion,
  "io.confluent" % "kafka-schema-registry" % confluentVersion classifier "tests",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)

lazy val root = (project in file("."))
  .settings(name := "embedded-kafka-schema-registry")
  .settings(publishSettings: _*)
  .settings(commonSettings: _*)
  .settings(commonLibrarySettings)
  .settings(releaseSettings: _*)
  .settings(resolvers ++= Seq(
    "confluent" at "https://packages.confluent.io/maven/"
  ))
