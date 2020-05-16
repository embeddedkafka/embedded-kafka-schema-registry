package net.manub.embeddedkafka.schemaregistry.streams

import io.confluent.kafka.serializers.{
  AbstractKafkaSchemaSerDeConfig,
  KafkaAvroDeserializer,
  KafkaAvroDeserializerConfig,
  KafkaAvroSerializer
}
import net.manub.embeddedkafka.Codecs._
import net.manub.embeddedkafka.ConsumerExtensions._
import net.manub.embeddedkafka.schemaregistry.Codecs._
import net.manub.embeddedkafka.schemaregistry.{
  EmbeddedKafkaConfig,
  TestAvroClass
}
import net.manub.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams._
import org.apache.avro.generic.{GenericRecord, GenericRecordBuilder}
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{Consumed, KStream, Produced}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

class ExampleKafkaStreamsSpec extends AnyWordSpec with Matchers {
  implicit val config: EmbeddedKafkaConfig =
    EmbeddedKafkaConfig(
      kafkaPort = 7000,
      zooKeeperPort = 7001,
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

  val stringSerde: Serde[String] = Serdes.String
  val specificAvroSerde: Serde[TestAvroClass] = {
    val props = Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> s"http://localhost:${config.schemaRegistryPort}",
      KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG   -> true.toString
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
      val streamBuilder = new StreamsBuilder
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
        withConsumer[String, TestAvroClass, Assertion] { consumer =>
          val consumedMessages =
            consumer.consumeLazily[(String, TestAvroClass)](outTopic)
          consumedMessages.take(2).toList should be(
            Seq(
              "hello" -> TestAvroClass("world"),
              "foo"   -> TestAvroClass("bar")
            )
          )
          val h :: _ = consumedMessages.drop(2).toList
          h shouldBe "baz" -> TestAvroClass("yaz")
        }
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
        withConsumer[String, GenericRecord, Assertion] { consumer =>
          val consumedMessages =
            consumer.consumeLazily[(String, GenericRecord)](outTopic)
          consumedMessages.take(2).toList should be(
            Seq("hello" -> recordWorld, "foo" -> recordBar)
          )
          val h :: _ = consumedMessages.drop(2).toList
          h shouldBe "baz" -> recordYaz
        }
      }
    }

    "allow support creating custom consumers" in {
      val streamBuilder = new StreamsBuilder
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
