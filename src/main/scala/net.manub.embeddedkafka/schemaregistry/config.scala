package net.manub.embeddedkafka.schemaregistry

import net.manub.embeddedkafka.{
  EmbeddedKafkaConfig => OriginalEmbeddedKafkaConfig
}

trait EmbeddedKafkaConfig extends OriginalEmbeddedKafkaConfig {
  def schemaRegistryPort: Int
  def customSchemaRegistryProperties: Map[String, String]
}

case class EmbeddedKafkaConfigImpl(
    kafkaPort: Int,
    zooKeeperPort: Int,
    schemaRegistryPort: Int,
    customBrokerProperties: Map[String, String],
    customProducerProperties: Map[String, String],
    customConsumerProperties: Map[String, String],
    customSchemaRegistryProperties: Map[String, String]
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
      customBrokerProperties: Map[String, String] = Map.empty,
      customProducerProperties: Map[String, String] = Map.empty,
      customConsumerProperties: Map[String, String] = Map.empty,
      customSchemaRegistryProperties: Map[String, String] = Map.empty
  ): EmbeddedKafkaConfig =
    EmbeddedKafkaConfigImpl(
      kafkaPort,
      zooKeeperPort,
      schemaRegistryPort,
      customBrokerProperties,
      customProducerProperties,
      customConsumerProperties,
      customSchemaRegistryProperties
    )
}
