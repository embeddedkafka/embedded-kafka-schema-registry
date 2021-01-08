package io.github.embeddedkafka.schemaregistry

import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaSpecSupport.{
  Available,
  NotAvailable
}

import java.nio.file.Files

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

        expectedServerStatus(7002, Available)
        expectedServerStatus(7000, Available)
        expectedServerStatus(7001, Available)

        expectedServerStatus(8002, Available)
        expectedServerStatus(8000, Available)
        expectedServerStatus(8001, Available)

        EmbeddedKafka.stop(firstBroker)

        expectedServerStatus(7002, NotAvailable)
        expectedServerStatus(7000, NotAvailable)
        expectedServerStatus(7001, NotAvailable)

        expectedServerStatus(8002, Available)
        expectedServerStatus(8000, Available)
        expectedServerStatus(8001, Available)

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

        expectedServerStatus(13542, Available)
        expectedServerStatus(12345, Available)
        expectedServerStatus(54321, Available)

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

        EmbeddedKafka.startZooKeeper(
          Files.createTempDirectory("zookeeper-test-logs")
        )
        EmbeddedKafka.startKafka(Files.createTempDirectory("kafka-test-logs"))
        EmbeddedKafka.startSchemaRegistry

        EmbeddedKafka.isRunning shouldBe true
        EmbeddedKafka.stop()
        EmbeddedKafka.isRunning shouldBe false
      }

      "return false when only Kafka and Zookeeper are running" in {
        implicit val config: EmbeddedKafkaConfig =
          EmbeddedKafkaConfig()

        EmbeddedKafka.startZooKeeper(
          Files.createTempDirectory("zookeeper-test-logs")
        )
        EmbeddedKafka.startKafka(Files.createTempDirectory("kafka-test-logs"))
        EmbeddedKafka.startSchemaRegistry
        EmbeddedKafka.stopSchemaRegistry()
        EmbeddedKafka.isRunning shouldBe false
        EmbeddedKafka.stop()
        EmbeddedKafka.isRunning shouldBe false
      }
    }
  }
}
