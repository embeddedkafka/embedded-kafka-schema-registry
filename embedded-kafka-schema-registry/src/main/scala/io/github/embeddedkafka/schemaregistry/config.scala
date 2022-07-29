package io.github.embeddedkafka.schemaregistry

import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig.SchemaRegistryRestAuth
import io.github.embeddedkafka.{
  EmbeddedKafkaConfig => OriginalEmbeddedKafkaConfig
}

trait EmbeddedKafkaConfig extends OriginalEmbeddedKafkaConfig {
  def schemaRegistryPort: Int
  def customSchemaRegistryProperties: Map[String, String]
  def schemaRegistryRestAuth: SchemaRegistryRestAuth
}

case class EmbeddedKafkaConfigImpl(
    kafkaPort: Int,
    zooKeeperPort: Int,
    schemaRegistryPort: Int,
    customBrokerProperties: Map[String, String],
    customProducerProperties: Map[String, String],
    customConsumerProperties: Map[String, String],
    customSchemaRegistryProperties: Map[String, String],
    schemaRegistryRestAuth: SchemaRegistryRestAuth
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
      customSchemaRegistryProperties: Map[String, String] = Map.empty,
      schemaRegistryRestAuth: SchemaRegistryRestAuth =
        SchemaRegistryRestAuth.None
  ): EmbeddedKafkaConfig =
    EmbeddedKafkaConfigImpl(
      kafkaPort,
      zooKeeperPort,
      schemaRegistryPort,
      customBrokerProperties,
      customProducerProperties,
      customConsumerProperties,
      customSchemaRegistryProperties,
      schemaRegistryRestAuth
    )

  sealed trait SchemaRegistryRestAuth
  object SchemaRegistryRestAuth {
    final case object None extends SchemaRegistryRestAuth

    sealed trait Some extends SchemaRegistryRestAuth
    final case class Basic(credentials: Set[Basic.UserCredential]) extends Some
    object Basic {
      final case class UserCredential(
          username: String,
          password: String,
          roles: Set[String] = Set.empty
      )
    }
    final case class Bearer(credentials: Set[Bearer.TokenCredential])
        extends Some
    object Bearer {
      final case class TokenCredential(
          token: String,
          roles: Set[String] = Set.empty
      )
    }
  }
}
