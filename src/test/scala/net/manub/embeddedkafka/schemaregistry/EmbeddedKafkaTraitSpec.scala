package net.manub.embeddedkafka.schemaregistry

import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel

class EmbeddedKafkaTraitSpec
    extends EmbeddedKafkaSpecSupport
    with EmbeddedKafka {

  "the withRunningKafka method" should {
    "start a Schema Registry server on a specified port" in {
      implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(schemaRegistryPort = 12345)

      withRunningKafka {
        schemaRegistryIsAvailable(12345)
      }
    }
  }

  "the withRunningKafkaOnFoundPort method" should {

    "start a Schema Registry server on an available port if 0" in {
      val userDefinedConfig: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(schemaRegistryPort = 0)
      withRunningKafkaOnFoundPort(userDefinedConfig) { actualConfig =>
        schemaRegistryIsAvailable(actualConfig.schemaRegistryPort)
      }
    }

    "start and stop Kafka, Zookeeper, and Schema Registry successfully on non-zero ports" in {
      val userDefinedConfig =
        EmbeddedKafkaConfig(
          kafkaPort = 12345,
          zooKeeperPort = 12346,
          schemaRegistryPort = 12347,
          avroCompatibilityLevel = AvroCompatibilityLevel.NONE)

      val actualConfig = withRunningKafkaOnFoundPort(userDefinedConfig) {
        actualConfig =>
          actualConfig shouldBe userDefinedConfig
          everyServerIsAvailable(actualConfig)
          actualConfig
      }
      noServerIsAvailable(actualConfig)
    }
  }

  private def everyServerIsAvailable(config: EmbeddedKafkaConfig): Unit = {
    kafkaIsAvailable(config.kafkaPort)
    schemaRegistryIsAvailable(config.schemaRegistryPort)
    zookeeperIsAvailable(config.zooKeeperPort)
  }

  private def noServerIsAvailable(config: EmbeddedKafkaConfig): Unit = {
    kafkaIsNotAvailable(config.kafkaPort)
    schemaRegistryIsNotAvailable(config.schemaRegistryPort)
    zookeeperIsNotAvailable(config.zooKeeperPort)
  }
}
