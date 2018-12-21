# embedded-kafka-schema-registry

[![Build Status](https://travis-ci.org/embeddedkafka/embedded-kafka-schema-registry.svg?branch=master)](https://travis-ci.org/embeddedkafka/embedded-kafka-schema-registry)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7cb0fdc1aec14d26b1e9954c129b93fe?branch=master)](https://www.codacy.com/app/francescopellegrini/embedded-kafka-schema-registry)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)

A library that provides an in-memory [Confluent Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html) instance to run your tests against.

Relies on the [embedded-kafka](https://github.com/embeddedkafka/embedded-kafka) library.

## Version compatibility matrix

embedded-kafka-schema-registry is available on Maven Central, compiled for both Scala 2.11 and 2.12.

Currently there's no support for Scala 2.13-Mx as Confluent artifacts are not published for these versions.

Versions match the version of Confluent Schema Registry they're built against.

## How to use

* In your `build.sbt` file add the following resolver: `resolvers += "confluent" at "https://packages.confluent.io/maven/"`
* In your `build.sbt` file add the following dependency: `"io.github.embeddedkafka" %% "embedded-kafka-schema-registry" % "5.1.0" % "test"`
* Have your class extend the `EmbeddedKafka` trait (from the `net.manub.embeddedkafka.schemaregistry` package).
* Enclose the code that needs a running instance of Kafka within the `withRunningKafka` closure.
* Provide an implicit `EmbeddedKafkaConfigImpl` (from the same package mentioned before).

```scala
class MySpec extends WordSpec with EmbeddedKafka {

  "runs with embedded kafka and Schema Registry" should {

    "work" in {
      implicit val config = EmbeddedKafkaConfigImpl()

      withRunningKafka {
        // ... code goes here
      }
    }
  }
}
```

* A Schema Registry server will be started and automatically shutdown at the end of the test.

## Utility methods

The `net.manub.embeddedkafka.avro.schemaregistry` package object provides useful implicit converters for testing with Avro and Schema Registry.

## Using streams

* For most of the cases have your class extend the `EmbeddedKafkaStreamsAllInOne` trait (from the `net.manub.embeddedkafka.schemaregistry.streams` package). This offers both streams management and easy creation of consumers for asserting resulting messages in output/sink topics.
* If you only want to use the streams management without the test consumers just have the class extend the `EmbeddedKafkaStreams` trait (from the same package mentioned before).
* Build your own `Topology` and use `runStreams` to test it.
* Have a look at the [example test](src/test/scala/net/manub/embeddedkafka/schemaregistry/streams/ExampleKafkaStreamsSpec.scala).
