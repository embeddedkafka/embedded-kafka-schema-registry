package net.manub.embeddedkafka.schemaregistry

import io.confluent.kafka.serializers.{
  AbstractKafkaSchemaSerDeConfig,
  KafkaAvroDeserializer,
  KafkaAvroDeserializerConfig,
  KafkaAvroSerializer
}
import net.manub.embeddedkafka.Codecs._
import net.manub.embeddedkafka.schemaregistry.EmbeddedKafka._
import net.manub.embeddedkafka.schemaregistry.EmbeddedKafkaConfig.defaultConfig
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class EmbeddedKafkaSpec
    extends EmbeddedKafkaSpecSupport
    with BeforeAndAfterAll {
  val consumerPollTimeout: FiniteDuration = 5.seconds

  implicit val serializer: Serializer[TestAvroClass] = {
    val props = Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> s"http://localhost:${defaultConfig.schemaRegistryPort}"
    )

    val ser = new KafkaAvroSerializer
    ser.configure(props.asJava, false)
    ser.asInstanceOf[Serializer[TestAvroClass]]
  }

  implicit val deserializer: Deserializer[TestAvroClass] = {
    val props = Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> s"http://localhost:${defaultConfig.schemaRegistryPort}",
      KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG   -> true.toString
    )

    val ser = new KafkaAvroDeserializer
    ser.configure(props.asJava, false)
    ser.asInstanceOf[Deserializer[TestAvroClass]]
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedKafka.start()
  }

  override def afterAll(): Unit = {
    EmbeddedKafka.stop()
    super.afterAll()
  }

  "the publishToKafka method" should {
    "publish synchronously a message to Kafka storing its schema into Schema Registry" in {
      val message = TestAvroClass("name")
      val topic   = "publish_test_topic"
      publishToKafka(topic, message)

      val record = consumeFirstMessageFrom[TestAvroClass](topic)

      record shouldBe message
    }

    "publish synchronously a message with String key to Kafka storing its schema into Schema Registry" in {
      val key     = "key"
      val message = TestAvroClass("name")
      val topic   = "publish_test_topic_string_key"

      publishToKafka(topic, key, message)

      val (recordKey, recordValue) =
        consumeFirstKeyedMessageFrom[String, TestAvroClass](topic)

      recordKey shouldBe key
      recordValue shouldBe message
    }

    "publish synchronously a batch of messages with String keys to Kafka storing its schema into Schema Registry" in {
      val key1     = "key1"
      val message1 = TestAvroClass("name")
      val key2     = "key2"
      val message2 = TestAvroClass("other name")
      val topic    = "publish_test_topic_batch_string_key"

      val messages = List((key1, message1), (key2, message2))

      publishToKafka(topic, messages)

      val records =
        consumeNumberKeyedMessagesFrom[String, TestAvroClass](topic, number = 2)

      records.size shouldBe 2

      val (record1Key, record1Value) :: (record2Key, record2Value) :: Nil =
        records

      record1Key shouldBe key1
      record1Value shouldBe message1

      record2Key shouldBe key2
      record2Value shouldBe message2
    }
  }

  "the consumeFirstMessageFrom method" should {
    "return a message published to a topic reading its schema from Schema Registry" in {
      val message = TestAvroClass("name")
      val topic   = "consume_test_topic"

      publishToKafka(topic, message)

      val record = consumeFirstMessageFrom[TestAvroClass](topic)

      record shouldBe message
    }
  }

  "the consumeFirstKeyedMessageFrom method" should {
    "return a message with String key published to a topic reading its schema from Schema Registry" in {
      val key     = "greeting"
      val message = TestAvroClass("name")
      val topic   = "consume_test_topic"

      publishToKafka(topic, key, message)

      val (k, m) = consumeFirstKeyedMessageFrom[String, TestAvroClass](topic)
      k shouldBe key
      m shouldBe message
    }
  }

  "the consumeNumberMessagesFromTopics method" should {
    "consume from multiple topics reading messages schema from Schema Registry" in {
      val topicMessagesMap = Map(
        "topic1" -> List(TestAvroClass("name")),
        "topic2" -> List(TestAvroClass("other name"))
      )

      topicMessagesMap.foreach {
        case (topic, messages) =>
          messages.foreach(publishToKafka(topic, _))
      }

      val consumedMessages =
        consumeNumberMessagesFromTopics[TestAvroClass](
          topicMessagesMap.keySet,
          topicMessagesMap.values.map(_.size).sum
        )

      consumedMessages.toMap shouldEqual topicMessagesMap
    }
  }

  "the consumeNumberKeyedMessagesFromTopics method" should {
    "consume from multiple topics reading messages schema from Schema Registry" in {
      val topicMessagesMap =
        Map(
          "topic1" -> List(("m1", TestAvroClass("name"))),
          "topic2" -> List(
            ("m2a", TestAvroClass("other name")),
            ("m2b", TestAvroClass("even another name"))
          )
        )

      topicMessagesMap.foreach {
        case (topic, keyedMessages) =>
          keyedMessages.foreach { case (k, v) => publishToKafka(topic, k, v) }
      }

      val consumedMessages =
        consumeNumberKeyedMessagesFromTopics[String, TestAvroClass](
          topicMessagesMap.keySet,
          topicMessagesMap.values.map(_.size).sum
        )

      consumedMessages.toMap shouldEqual topicMessagesMap
    }
  }
}
