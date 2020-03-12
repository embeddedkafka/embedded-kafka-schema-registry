package net.manub.embeddedkafka.schemaregistry.streams

import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel
import net.manub.embeddedkafka.Codecs._
import net.manub.embeddedkafka.ConsumerExtensions._
import net.manub.embeddedkafka.TestAvroClass
import net.manub.embeddedkafka.schemaregistry.avro.Codecs._
import net.manub.embeddedkafka.schemaregistry.avro.AvroSerdes
import net.manub.embeddedkafka.schemaregistry.EmbeddedKafkaConfig
import net.manub.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams._
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{Consumed, KStream, Produced}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExampleKafkaStreamsSpec extends AnyWordSpec with Matchers {
  implicit val config: EmbeddedKafkaConfig =
    EmbeddedKafkaConfig(
      kafkaPort = 7000,
      zooKeeperPort = 7001,
      schemaRegistryPort = 7002,
      avroCompatibilityLevel = AvroCompatibilityLevel.FULL
    )

  val (inTopic, outTopic) = ("in", "out")

  val stringSerde: Serde[String] = Serdes.String
  val avroSerde: Serde[TestAvroClass] =
    AvroSerdes.specific[TestAvroClass]()

  "A Kafka streams test using Schema Registry" should {
    "support kafka streams and specific record" in {
      val streamBuilder = new StreamsBuilder
      val stream: KStream[String, TestAvroClass] =
        streamBuilder.stream(inTopic, Consumed.`with`(stringSerde, avroSerde))

      stream.to(outTopic, Produced.`with`(stringSerde, avroSerde))

      runStreams(Seq(inTopic, outTopic), streamBuilder.build()) {
        implicit val avroSerializer: Serializer[TestAvroClass] =
          avroSerde.serializer

        implicit val avroDeserializer: Deserializer[TestAvroClass] =
          avroSerde.deserializer

        publishToKafka(inTopic, "hello", TestAvroClass("world"))
        publishToKafka(inTopic, "foo", TestAvroClass("bar"))
        publishToKafka(inTopic, "baz", TestAvroClass("yaz"))
        withConsumer[String, TestAvroClass, Unit] { consumer =>
          val consumedMessages: Stream[(String, TestAvroClass)] =
            consumer.consumeLazily(outTopic)
          consumedMessages.take(2) should be(
            Seq(
              "hello" -> TestAvroClass("world"),
              "foo"   -> TestAvroClass("bar")
            )
          )
          val h :: _ = consumedMessages.drop(2).toList
          h should be("baz" -> TestAvroClass("yaz"))
        }
      }
    }

    "support kafka streams and generic record" in {
      val schema: Schema = TestAvroClass.SCHEMA$
      val recordWorld: GenericRecord =
        new GenericRecordBuilder(schema).set("name", "world").build()
      val recordBar: GenericRecord =
        new GenericRecordBuilder(schema).set("name", "bar").build()
      val recordYaz: GenericRecord =
        new GenericRecordBuilder(schema).set("name", "yaz").build()

      val genericAvroSerde = AvroSerdes.generic()

      val streamBuilder = new StreamsBuilder
      val stream: KStream[String, GenericRecord] =
        streamBuilder.stream(
          inTopic,
          Consumed.`with`(stringSerde, genericAvroSerde)
        )

      stream.to(outTopic, Produced.`with`(stringSerde, genericAvroSerde))

      runStreams(Seq(inTopic, outTopic), streamBuilder.build()) {
        implicit val genericAvroSerializer: Serializer[GenericRecord] =
          genericAvroSerde.serializer

        implicit val genericAvroDeserializer: Deserializer[GenericRecord] =
          genericAvroSerde.deserializer

        publishToKafka(inTopic, "hello", recordWorld)
        publishToKafka(inTopic, "foo", recordBar)
        publishToKafka(inTopic, "baz", recordYaz)
        withConsumer[String, GenericRecord, Unit] { consumer =>
          val consumedMessages: Stream[(String, GenericRecord)] =
            consumer.consumeLazily[(String, GenericRecord)](outTopic)
          consumedMessages.take(2) should be(
            Seq("hello" -> recordWorld, "foo" -> recordBar)
          )
          val h :: _ = consumedMessages.drop(2).toList
          h should be("baz" -> recordYaz)
        }
      }
    }

    "allow support creating custom consumers" in {
      val streamBuilder = new StreamsBuilder
      val stream: KStream[String, TestAvroClass] =
        streamBuilder.stream(inTopic, Consumed.`with`(stringSerde, avroSerde))

      stream.to(outTopic, Produced.`with`(stringSerde, avroSerde))

      runStreams(Seq(inTopic, outTopic), streamBuilder.build()) {
        implicit val avroSerializer: Serializer[TestAvroClass] =
          avroSerde.serializer

        implicit val avroDeserializer: Deserializer[TestAvroClass] =
          avroSerde.deserializer

        publishToKafka(inTopic, "hello", TestAvroClass("world"))
        publishToKafka(inTopic, "foo", TestAvroClass("bar"))

        val records =
          withConsumer[String, TestAvroClass, Seq[(String, TestAvroClass)]](
            _.consumeLazily[(String, TestAvroClass)](outTopic).take(2)
          )

        records should be(
          Seq("hello" -> TestAvroClass("world"), "foo" -> TestAvroClass("bar"))
        )
      }
    }
  }
}
