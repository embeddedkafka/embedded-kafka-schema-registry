import sbtrelease.Version

parallelExecution in ThisBuild := false

val embeddedKafkaVersion = "2.5.0"
val confluentVersion = "5.5.0"

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

lazy val confluentResolvers = Seq(
  "confluent" at "https://packages.confluent.io/maven/",
  // Since v5.5.0
  "jitpack" at "https://jitpack.io"
)

lazy val commonSettings = Seq(
  organization := "io.github.embeddedkafka",
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.12.10", "2.13.1"),
  homepage := Some(url("https://github.com/embeddedkafka/embedded-kafka-schema-registry")),
  resolvers ++= confluentResolvers,
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
  scalafmtOnCompile := true,
  coverageMinimum := 80
)

// Exclude any transitive Kafka dependency to prevent runtime errors.
// They tend to evict Apache's since their version is greater
lazy val confluentArtifacts = Seq(
  "io.confluent" % "kafka-avro-serializer" % confluentVersion,
  "io.confluent" % "kafka-schema-registry" % confluentVersion
).map(_ excludeAll ExclusionRule().withOrganization("org.apache.kafka"))

lazy val commonLibrarySettings = libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-log4j12" % "1.7.30" % Test,
  "org.scalatest" %% "scalatest" % "3.1.2" % Test
) ++ confluentArtifacts

lazy val root = (project in file("."))
  .settings(name := "embedded-kafka-schema-registry-root")
  .settings(commonSettings: _*)
  .settings(publishArtifact := false)
  .settings(releaseSettings: _*)
  .settings(skip in publish := true)
  .aggregate(embeddedKafkaSchemaRegistry, kafkaStreams)

lazy val embeddedKafkaSchemaRegistry = (project in file("embedded-kafka-schema-registry"))
  .settings(name := "embedded-kafka-schema-registry")
  .settings(publishSettings: _*)
  .settings(commonSettings: _*)
  .settings(commonLibrarySettings)
  .settings(libraryDependencies ++= Seq(
    "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion,
  ))
  .settings(releaseSettings: _*)

lazy val kafkaStreams = (project in file("kafka-streams"))
  .settings(name := "embedded-kafka-schema-registry-streams")
  .settings(publishSettings: _*)
  .settings(commonSettings: _*)
  .settings(commonLibrarySettings)
  .settings(releaseSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion
  ))
  .dependsOn(embeddedKafkaSchemaRegistry)
