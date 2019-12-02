package net.manub.embeddedkafka.schemaregistry

import scala.reflect.io.Directory

class EmbeddedKafkaObjectSpec extends EmbeddedKafkaSpecSupport {
  "the EmbeddedKafka object" when {
    "invoking the start and stop methods" should {
      "start and stop a specific Kafka along with Schema Registry" in {
        val firstBroker = EmbeddedKafka.start()(
          EmbeddedKafkaConfig(
            kafkaPort = 7000,
            zooKeeperPort = 7001,
            schemaRegistryPort = 7002
          )
        )
        EmbeddedKafka.start()(
          EmbeddedKafkaConfig(
            kafkaPort = 8000,
            zooKeeperPort = 8001,
            schemaRegistryPort = 8002
          )
        )

        schemaRegistryIsAvailable(7002)
        kafkaIsAvailable(7000)
        zookeeperIsAvailable(7001)

        schemaRegistryIsAvailable(8002)
        kafkaIsAvailable(8000)
        zookeeperIsAvailable(8001)

        EmbeddedKafka.stop(firstBroker)

        schemaRegistryIsNotAvailable(7002)
        kafkaIsNotAvailable(7000)
        zookeeperIsNotAvailable(7001)

        schemaRegistryIsAvailable(8002)
        kafkaIsAvailable(8000)
        zookeeperIsAvailable(8001)

        EmbeddedKafka.stop()
      }

      "start and stop Kafka, Zookeeper, and Schema Registry on different specified ports using an implicit configuration" in {
        implicit val config: EmbeddedKafkaConfig =
          EmbeddedKafkaConfig(
            kafkaPort = 12345,
            zooKeeperPort = 54321,
            schemaRegistryPort = 13542
          )

        EmbeddedKafka.start()

        schemaRegistryIsAvailable(13542)
        kafkaIsAvailable(12345)
        zookeeperIsAvailable(54321)

        EmbeddedKafka.stop()
      }
    }

    "invoking the isRunning method" should {
      "return true when Schema Registry, Kafka, and Zookeeper are all running" in {
        implicit val config: EmbeddedKafkaConfig =
          EmbeddedKafkaConfig()

        EmbeddedKafka.start()
        EmbeddedKafka.isRunning shouldBe true
        EmbeddedKafka.stop()
        EmbeddedKafka.isRunning shouldBe false
      }

      "return true when Schema Registry, Kafka, and Zookeeper are all running, if started separately" in {
        implicit val config: EmbeddedKafkaConfig =
          EmbeddedKafkaConfig()

        EmbeddedKafka.startZooKeeper(Directory.makeTemp("zookeeper-test-logs"))
        EmbeddedKafka.startKafka(Directory.makeTemp("kafka-test-logs"))
        EmbeddedKafka.startSchemaRegistry

        EmbeddedKafka.isRunning shouldBe true
        EmbeddedKafka.stop()
        EmbeddedKafka.isRunning shouldBe false
      }

      "return false when only Kafka and Zookeeper are running" in {
        implicit val config: EmbeddedKafkaConfig =
          EmbeddedKafkaConfig()

        EmbeddedKafka.startZooKeeper(Directory.makeTemp("zookeeper-test-logs"))
        EmbeddedKafka.startKafka(Directory.makeTemp("kafka-test-logs"))
        EmbeddedKafka.isRunning shouldBe false
        EmbeddedKafka.stop()
        EmbeddedKafka.isRunning shouldBe false
      }
    }
  }
}
