package io.github.embeddedkafka.schemaregistry.streams

import io.confluent.kafka.serializers.{
  AbstractKafkaSchemaSerDeConfig,
  KafkaAvroDeserializer,
  KafkaAvroDeserializerConfig,
  KafkaAvroSerializer
}
import io.github.embeddedkafka.Codecs._
import io.github.embeddedkafka.schemaregistry.{
  EmbeddedKafkaConfig,
  TestAvroClass
}
import io.github.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams._
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{Consumed, KStream, Produced}
import org.scalatest.Assertion
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class ExampleKafkaStreamsSpec
    extends AnyWordSpec
    with Matchers
    with Eventually {
  implicit val config: EmbeddedKafkaConfig =
    EmbeddedKafkaConfig(
      kafkaPort = 7000,
      controllerPort = 7001,
      schemaRegistryPort = 7002
    )

  private def avroSerde[T](props: Map[String, AnyRef]): Serde[T] = {
    val ser = new KafkaAvroSerializer
    ser.configure(props.asJava, false)
    val deser = new KafkaAvroDeserializer
    deser.configure(props.asJava, false)

    Serdes.serdeFrom(
      ser.asInstanceOf[Serializer[T]],
      deser.asInstanceOf[Deserializer[T]]
    )
  }

  val (inTopic, outTopic) = ("in", "out")

  val stringSerde: Serde[String]              = Serdes.String
  val specificAvroSerde: Serde[TestAvroClass] = {
    val props = Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> s"http://localhost:${config.schemaRegistryPort}",
      KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG -> true.toString
    )

    avroSerde(props)
  }
  val genericAvroSerde: Serde[GenericRecord] = {
    val props = Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> s"http://localhost:${config.schemaRegistryPort}"
    )
    avroSerde(props)
  }

  "A Kafka streams test using Schema Registry" should {
    "support kafka streams and specific record" in {
      val streamBuilder                          = new StreamsBuilder
      val stream: KStream[String, TestAvroClass] =
        streamBuilder.stream(
          inTopic,
          Consumed.`with`(stringSerde, specificAvroSerde)
        )

      stream.to(outTopic, Produced.`with`(stringSerde, specificAvroSerde))

      runStreams(Seq(inTopic, outTopic), streamBuilder.build()) {
        implicit val avroSerializer: Serializer[TestAvroClass] =
          specificAvroSerde.serializer

        implicit val avroDeserializer: Deserializer[TestAvroClass] =
          specificAvroSerde.deserializer

        publishToKafka(inTopic, "hello", TestAvroClass("world"))
        publishToKafka(inTopic, "foo", TestAvroClass("bar"))
        publishToKafka(inTopic, "baz", TestAvroClass("yaz"))

        val firstTwoMessages =
          consumeNumberKeyedMessagesFrom[String, TestAvroClass](outTopic, 2)

        firstTwoMessages should be(
          Seq("hello" -> TestAvroClass("world"), "foo" -> TestAvroClass("bar"))
        )

        val thirdMessage =
          consumeFirstKeyedMessageFrom[String, TestAvroClass](outTopic)

        thirdMessage should be("baz" -> TestAvroClass("yaz"))
      }
    }

    "support kafka streams and generic record" in {
      val recordWorld: GenericRecord =
        new GenericRecordBuilder(TestAvroClass.avroSchema)
          .set("name", "world")
          .build()
      val recordBar: GenericRecord =
        new GenericRecordBuilder(TestAvroClass.avroSchema)
          .set("name", "bar")
          .build()
      val recordYaz: GenericRecord =
        new GenericRecordBuilder(TestAvroClass.avroSchema)
          .set("name", "yaz")
          .build()

      val streamBuilder                          = new StreamsBuilder
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

        val firstTwoMessages =
          consumeNumberKeyedMessagesFrom[String, GenericRecord](outTopic, 2)

        firstTwoMessages should be(
          Seq("hello" -> recordWorld, "foo" -> recordBar)
        )

        val thirdMessage =
          consumeFirstKeyedMessageFrom[String, GenericRecord](outTopic)

        thirdMessage should be("baz" -> recordYaz)
      }
    }

    "allow support creating custom consumers" in {
      implicit val patienceConfig: PatienceConfig =
        PatienceConfig(5.seconds, 100.millis)

      implicit val avroSerializer: Serializer[TestAvroClass] =
        specificAvroSerde.serializer

      implicit val avroDeserializer: Deserializer[TestAvroClass] =
        specificAvroSerde.deserializer

      val streamBuilder                          = new StreamsBuilder
      val stream: KStream[String, TestAvroClass] =
        streamBuilder.stream(
          inTopic,
          Consumed.`with`(stringSerde, specificAvroSerde)
        )

      stream.to(outTopic, Produced.`with`(stringSerde, specificAvroSerde))

      runStreams(Seq(inTopic, outTopic), streamBuilder.build()) {
        publishToKafka(inTopic, "hello", TestAvroClass("world"))
        publishToKafka(inTopic, "foo", TestAvroClass("bar"))

        withConsumer[String, TestAvroClass, Assertion] { consumer =>
          consumer.subscribe(java.util.Collections.singleton(outTopic))

          eventually {
            val records = consumer
              .poll(java.time.Duration.ofMillis(100.millis.toMillis))
              .asScala
              .map(r => (r.key, r.value))

            records should be(
              Seq(
                "hello" -> TestAvroClass("world"),
                "foo"   -> TestAvroClass("bar")
              )
            )
          }
        }
      }
    }
  }
}
