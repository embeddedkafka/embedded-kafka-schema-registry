import sbtrelease.Version

val embeddedKafkaVersion = "2.3.0"
val confluentVersion = "5.3.1"
val akkaVersion = "2.5.25"

lazy val publishSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  publishArtifact in Test := false,
  // https://github.com/sbt/sbt/issues/3570#issuecomment-432814188
  updateOptions := updateOptions.value.withGigahorse(false),
  developers := List(
    Developer(
      "manub",
      "Emanuele Blanco",
      "emanuele.blanco@gmail.com",
      url("http://twitter.com/manub")
    )
  )
)

import ReleaseTransformations._

lazy val releaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  releaseVersionBump := Version.Bump.Minor,
  releaseCrossBuild := true
)

lazy val commonSettings = Seq(
  organization := "io.github.embeddedkafka",
  scalaVersion := "2.12.9",
  crossScalaVersions := Seq("2.12.9", "2.11.12"),
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
  "org.slf4j" % "slf4j-log4j12" % "1.7.28" % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
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
    "confluent" at "https://packages.confluent.io/maven/",
    Resolver.sonatypeRepo("snapshots")
  ))
