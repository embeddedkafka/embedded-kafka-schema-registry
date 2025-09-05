# embedded-kafka-schema-registry

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.embeddedkafka/embedded-kafka-schema-registry_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.embeddedkafka/embedded-kafka-schema-registry_2.13)
[![Test](https://github.com/embeddedkafka/embedded-kafka-schema-registry/actions/workflows/test.yml/badge.svg)](https://github.com/embeddedkafka/embedded-kafka-schema-registry/actions/workflows/test.yml)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ea9c18f6d1f547a599d76102cd0e709a)](https://www.codacy.com/gh/embeddedkafka/embedded-kafka-schema-registry)
[![Codacy Coverage Badge](https://api.codacy.com/project/badge/Coverage/ea9c18f6d1f547a599d76102cd0e709a)](https://www.codacy.com/gh/embeddedkafka/embedded-kafka-schema-registry)
[![Mergify Status](https://img.shields.io/endpoint.svg?url=https://api.mergify.com/v1/badges/embeddedkafka/embedded-kafka-schema-registry&style=flat)](https://mergify.io)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

A library that provides in-memory instances of both Kafka and [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html) to run your tests against.

Relies on the [embedded-kafka](https://github.com/embeddedkafka/embedded-kafka) library.

## Version compatibility matrix

The library available on Maven Central.

Versions match the version of Confluent Schema Registry they're built against.

| embedded-kafka-schema-registry version | Confluent Schema Registry version | embedded-kafka & Kafka Kafka version | Scala versions  | Java version |
|----------------------------------------|-----------------------------------|--------------------------------------|-----------------|--------------|
| 8.0.0                                  | 8.0.0                             | 4.0.x                                | 2.13, 3.3       | 17+          |
| 7.9.2                                  | 7.9.2                             | 3.9.x                                | 2,12, 2.13, 3.3 | 17+          |
| 7.9.1                                  | 7.9.1                             | 3.9.x                                | 2,12, 2.13, 3.3 | 17+          |
| 7.9.0                                  | 7.9.0                             | 3.9.x                                | 2,12, 2.13, 3.3 | 8+           |
| 7.8.0                                  | 7.8.0                             | 3.8.x                                | 2,12, 2.13, 3.3 | 8+           |
| 7.7.0                                  | 7.7.0                             | 3.7.x                                | 2,12, 2.13, 3.3 | 8+           |

## Important known limitation (prior to Kafka v2.8.0)

[Prior to v2.8.0](https://github.com/apache/kafka/pull/10174) Kafka core was inlining the Scala library, so you couldn't use a different Scala **patch** version than [what Kafka used to compile its jars](https://github.com/apache/kafka/blob/trunk/gradle/dependencies.gradle#L30)!

## Breaking change: new package name

From v6.2.0 onwards package name has been updated to reflect the library group id (i.e. `io.github.embeddedkafka`).

Aliases to the old package name have been added, along with a one-time [Scalafix rule](https://github.com/embeddedkafka/embedded-kafka-scalafix) to ensure the smoothest migration.

## embedded-kafka-schema-registry

### How to use

* In your `build.sbt` file add the following resolvers:

```scala
resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/"
)
```

* In your `build.sbt` file add the following dependency (replace `x.x.x` with the appropriate version): `"io.github.embeddedkafka" %% "embedded-kafka-schema-registry" % "x.x.x" % Test`
* Have your class extend the `EmbeddedKafka` trait (from the `io.github.embeddedkafka.schemaregistry` package).
* Enclose the code that needs a running instance of Kafka within the `withRunningKafka` closure.
* Provide an implicit `EmbeddedKafkaConfigImpl` (from the same package mentioned before).

```scala
class MySpec extends AnyWordSpecLike with Matchers with EmbeddedKafka {

  "runs with embedded kafka and Schema Registry" should {

    "work" in {
      implicit val config = EmbeddedKafkaConfig()

      withRunningKafka {
        // ... code goes here
      }
    }
  }
}
```

* Kafka Broker, Kafka Controller and Schema Registry will be instantiated respectively on port 6001, 6002, and 6003 and automatically shutdown at the end of the test.

## embedded-kafka-schema-registry-streams

A library that builds on top of `embedded-kafka-schema-registry` to offer easy testing of [Kafka Streams](https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams) with Confluent Schema Registry.

It takes care of instantiating and starting your streams as well as closing them after running your test-case code.

### How to use

* In your `build.sbt` file add the following dependency (replace `x.x.x` with the appropriate version): `"io.github.embeddedkafka" %% "embedded-kafka-schema-registry-streams" % "x.x.x" % Test`
* For most of the cases have your class extend the `EmbeddedKafkaStreams` trait (from the `io.github.embeddedkafka.schemaregistry.streams` package). This offers both streams management and easy creation of consumers for asserting resulting messages in output/sink topics.
* Use `EmbeddedKafkaStreams.runStreams` and `EmbeddedKafka.withConsumer` and `EmbeddedKafka.withProducer`. This allows you to create your own consumers of custom types as seen in the [example test](kafka-streams/src/test/scala/io/github/embeddedkafka/schemaregistry/streams/ExampleKafkaStreamsSpec.scala).
