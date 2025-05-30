package io.github.embeddedkafka.schemaregistry

import io.github.embeddedkafka.schemaregistry.EmbeddedKafka._
import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaSpecSupport.{
  Available,
  NotAvailable
}
import org.scalatest.Assertion

class EmbeddedKafkaTraitSpec extends EmbeddedKafkaSpecSupport {
  "the withRunningKafka method" should {
    "start a Schema Registry server on a specified port" in {
      implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(schemaRegistryPort = 12345)

      withRunningKafka {
        expectedServerStatus(12345, Available)
      }
    }
  }

  "the withRunningKafkaOnFoundPort method" should {
    "start a Schema Registry server on an available port if 0" in {
      val userDefinedConfig: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(schemaRegistryPort = 0)
      withRunningKafkaOnFoundPort(userDefinedConfig) { actualConfig =>
        expectedServerStatus(actualConfig.schemaRegistryPort, Available)
      }
    }

    "start and stop Kafka Broker, Kafka Controller, and Schema Registry successfully on non-zero ports" in {
      val userDefinedConfig =
        EmbeddedKafkaConfig(
          kafkaPort = 12345,
          controllerPort = 12346,
          schemaRegistryPort = 12347
        )

      val actualConfig = withRunningKafkaOnFoundPort(userDefinedConfig) {
        actualConfig =>
          actualConfig shouldBe userDefinedConfig
          everyServerIsAvailable(actualConfig)
          actualConfig
      }
      noServerIsAvailable(actualConfig)
    }
  }

  private def everyServerIsAvailable(config: EmbeddedKafkaConfig): Assertion = {
    expectedServerStatus(config.kafkaPort, Available)
    expectedServerStatus(config.schemaRegistryPort, Available)
    expectedServerStatus(config.controllerPort, Available)
  }

  private def noServerIsAvailable(config: EmbeddedKafkaConfig): Assertion = {
    expectedServerStatus(config.kafkaPort, NotAvailable)
    expectedServerStatus(config.schemaRegistryPort, NotAvailable)
    expectedServerStatus(config.controllerPort, NotAvailable)
  }
}
