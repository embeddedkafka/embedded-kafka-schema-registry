package net.manub.embeddedkafka.schemaregistry

import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel
import net.manub.embeddedkafka.{
  EmbeddedKafkaConfig => OriginalEmbeddedKafkaConfig
}

trait EmbeddedKafkaConfig extends OriginalEmbeddedKafkaConfig {
  def schemaRegistryPort: Int
  def avroCompatibilityLevel: AvroCompatibilityLevel
}

case class EmbeddedKafkaConfigImpl(
    kafkaPort: Int,
    zooKeeperPort: Int,
    schemaRegistryPort: Int,
    avroCompatibilityLevel: AvroCompatibilityLevel,
    customBrokerProperties: Map[String, String],
    customProducerProperties: Map[String, String],
    customConsumerProperties: Map[String, String]
) extends EmbeddedKafkaConfig {
  override val numberOfThreads: Int = 3
}

object EmbeddedKafkaConfig {
  implicit val defaultConfig: EmbeddedKafkaConfig = apply()

  def apply(
      kafkaPort: Int = 6001,
      zooKeeperPort: Int = 6000,
      schemaRegistryPort: Int = 6002,
      avroCompatibilityLevel: AvroCompatibilityLevel =
        AvroCompatibilityLevel.NONE,
      customBrokerProperties: Map[String, String] = Map.empty,
      customProducerProperties: Map[String, String] = Map.empty,
      customConsumerProperties: Map[String, String] = Map.empty
  ): EmbeddedKafkaConfig =
    EmbeddedKafkaConfigImpl(kafkaPort,
                            zooKeeperPort,
                            schemaRegistryPort,
                            avroCompatibilityLevel,
                            customBrokerProperties,
                            customProducerProperties,
                            customConsumerProperties)
}
