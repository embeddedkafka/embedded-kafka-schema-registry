# embedded-kafka-schema-registry

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.embeddedkafka/embedded-kafka-schema-registry_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.embeddedkafka/embedded-kafka-schema-registry_2.12)
![Build](https://github.com/embeddedkafka/embedded-kafka-schema-registry/workflows/Build/badge.svg)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7cb0fdc1aec14d26b1e9954c129b93fe?branch=master)](https://www.codacy.com/app/francescopellegrini/embedded-kafka-schema-registry)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

A library that provides in-memory instances of both Kafka and [Confluent Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html) to run your tests against.

Relies on the [embedded-kafka](https://github.com/embeddedkafka/embedded-kafka) library.

## Version compatibility matrix

embedded-kafka-schema-registry is available on Maven Central, compiled for Scala 2.12.

Support for Scala 2.11 was dropped by Apache in Kafka v2.5.0.

Currently there's no support for Scala 2.13 as Confluent artifacts are not published for such version.

Versions match the version of Confluent Schema Registry they're built against.

## How to use

* In your `build.sbt` file add the following resolvers:

```scala
resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/",
  "jitpack" at "https://jitpack.io"
)
```

* In your `build.sbt` file add the following dependency (replace `x.x.x` with the appropriate version): `"io.github.embeddedkafka" %% "embedded-kafka-schema-registry" % "x.x.x" % Test`
* Have your class extend the `EmbeddedKafka` trait (from the `net.manub.embeddedkafka.schemaregistry` package).
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

* In-memory Zookeeper, Kafka, and Schema Registry will be instantiated respectively on port 6000, 6001, and 6002 and automatically shutdown at the end of the test.

## Utility methods

~~The `net.manub.embeddedkafka.avro.schemaregistry` package object provides useful implicit converters for testing with Avro and Schema Registry.~~

The implicit Avro serdes have been deprecated. Please use `AvroSerdes` instead.

## Using streams

* For most of the cases have your class extend the `EmbeddedKafkaStreams` trait (from the `net.manub.embeddedkafka.schemaregistry.streams` package). This offers both streams management and easy creation of consumers for asserting resulting messages in output/sink topics.
* Use `EmbeddedKafkaStreams.runStreams` and `EmbeddedKafka.withConsumer` and `EmbeddedKafka.withProducer`. This allows you to create your own consumers of custom types as seen in the [example test](src/test/scala/net/manub/embeddedkafka/schemaregistry/streams/ExampleKafkaStreamsSpec.scala).
