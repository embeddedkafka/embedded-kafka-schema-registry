import sbtrelease.Version

val embeddedKafkaVersion = "2.4.1.1"
val confluentVersion = "5.4.1"

lazy val publishSettings = Seq(
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  publishArtifact in Test := false,
  developers := List(
    Developer(
      "manub",
      "Emanuele Blanco",
      "emanuele.blanco@gmail.com",
      url("https://twitter.com/manub")
    ),
    Developer(
      "francescopellegrini",
      "Francesco Pellegrini",
      "francesco.pelle@gmail.com",
      url("https://github.com/francescopellegrini")
    ),
    Developer(
      "NeQuissimus",
      "Tim Steinbach",
      "steinbach.tim@gmail.com",
      url("https://github.com/NeQuissimus")
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
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.12.10", "2.11.12"),
  homepage := Some(url("https://github.com/embeddedkafka/embedded-kafka-schema-registry")),
  parallelExecution in Test := false,
  logBuffered in Test := false,
  fork in Test := true,
  javaOptions ++= Seq("-Xms512m", "-Xmx2048m"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "utf8",
    "-Xlint:missing-interpolator",
    "-Xlint:private-shadow",
    "-Xlint:type-parameter-shadow",
    "-Ywarn-dead-code",
    "-Ywarn-unused"
  ),
  scalafmtOnCompile := true
)

lazy val commonLibrarySettings = libraryDependencies ++= Seq(
  "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion,
  // Exclude any transitive 2.12-specific dependency
  "io.confluent" % "kafka-avro-serializer" % confluentVersion exclude("org.apache.kafka", "kafka_2.12"),
  "io.confluent" % "kafka-schema-registry" % confluentVersion exclude("org.apache.kafka", "kafka_2.12"),
  "io.confluent" % "kafka-schema-registry" % confluentVersion classifier "tests" exclude("org.apache.kafka", "kafka_2.12"),
  "org.slf4j" % "slf4j-log4j12" % "1.7.30" % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
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
