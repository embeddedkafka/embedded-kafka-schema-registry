import Dependencies._
import sbtrelease.Version

ThisBuild / parallelExecution := false

lazy val commonSettings = Seq(
  organization := "io.github.embeddedkafka",
  scalaVersion := Versions.Scala,
  crossScalaVersions := Seq(Versions.Scala212, Versions.Scala)
)

lazy val compileSettings = Seq(
  Compile / compile := (Compile / compile)
    .dependsOn(
      Compile / scalafmtSbt,
      Compile / scalafmtAll
    )
    .value,
  libraryDependencies ++= Common.confluentDeps ++ Common.testDeps,
  resolvers ++= CustomResolvers.resolvers,
  javaOptions ++= Seq("-Xms512m", "-Xmx2048m"),
  scalacOptions -= "-Xfatal-warnings"
)

lazy val coverageSettings = Seq(
  coverageMinimum := 80,
  coverageFailOnMinimum := false
)

lazy val publishSettings = Seq(
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/embeddedkafka/embedded-kafka-schema-registry")),
  Test / publishArtifact := false,
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

lazy val testSettings = Seq(
  Test / fork := true,
  Test / logBuffered := false,
  Test / parallelExecution := false
)

lazy val root = (project in file("."))
  .settings(name := "embedded-kafka-schema-registry-root")
  .settings(commonSettings: _*)
  .settings(compileSettings: _*)
  .settings(coverageSettings: _*)
  .settings(publishSettings: _*)
  .settings(releaseSettings: _*)
  .settings(testSettings: _*)
  .settings(publishArtifact := false)
  .settings(publish / skip := true)
  .aggregate(embeddedKafkaSchemaRegistry, kafkaStreams)

lazy val embeddedKafkaSchemaRegistry =
  (project in file("embedded-kafka-schema-registry"))
    .settings(name := "embedded-kafka-schema-registry")
    .settings(commonSettings: _*)
    .settings(compileSettings: _*)
    .settings(coverageSettings: _*)
    .settings(publishSettings: _*)
    .settings(releaseSettings: _*)
    .settings(testSettings: _*)
    .settings(libraryDependencies ++= EmbeddedKafkaSchemaRegistry.prodDeps)

lazy val kafkaStreams = (project in file("kafka-streams"))
  .settings(name := "embedded-kafka-schema-registry-streams")
  .settings(commonSettings: _*)
  .settings(compileSettings: _*)
  .settings(coverageSettings: _*)
  .settings(publishSettings: _*)
  .settings(releaseSettings: _*)
  .settings(testSettings: _*)
  .settings(libraryDependencies ++= KafkaStreams.prodDeps)
  .dependsOn(embeddedKafkaSchemaRegistry)
