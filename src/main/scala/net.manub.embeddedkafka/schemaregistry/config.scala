package net.manub.embeddedkafka.schemaregistry

import io.confluent.kafka.schemaregistry.CompatibilityLevel
import net.manub.embeddedkafka.{
  EmbeddedKafkaConfig => OriginalEmbeddedKafkaConfig
}

trait EmbeddedKafkaConfig extends OriginalEmbeddedKafkaConfig {
  def schemaRegistryPort: Int
  def compatibilityLevel: CompatibilityLevel
}

case class EmbeddedKafkaConfigImpl(
    kafkaPort: Int,
    zooKeeperPort: Int,
    schemaRegistryPort: Int,
    compatibilityLevel: CompatibilityLevel,
    customBrokerProperties: Map[String, String],
    customProducerProperties: Map[String, String],
    customConsumerProperties: Map[String, String]
) extends EmbeddedKafkaConfig {
  override val numberOfThreads: Int = 3
}

object EmbeddedKafkaConfig {
  lazy val defaultSchemaRegistryPort = 6002

  implicit val defaultConfig: EmbeddedKafkaConfig = apply()

  def apply(
      kafkaPort: Int = OriginalEmbeddedKafkaConfig.defaultKafkaPort,
      zooKeeperPort: Int = OriginalEmbeddedKafkaConfig.defaultZookeeperPort,
      schemaRegistryPort: Int = defaultSchemaRegistryPort,
      compatibilityLevel: CompatibilityLevel = CompatibilityLevel.NONE,
      customBrokerProperties: Map[String, String] = Map.empty,
      customProducerProperties: Map[String, String] = Map.empty,
      customConsumerProperties: Map[String, String] = Map.empty
  ): EmbeddedKafkaConfig =
    EmbeddedKafkaConfigImpl(
      kafkaPort,
      zooKeeperPort,
      schemaRegistryPort,
      compatibilityLevel,
      customBrokerProperties,
      customProducerProperties,
      customConsumerProperties
    )
}
