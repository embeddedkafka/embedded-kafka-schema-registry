package net.manub.embeddedkafka.schemaregistry.streams

import net.manub.embeddedkafka.schemaregistry.{
  EmbeddedKafkaConfig,
  EmbeddedKafka
}
import net.manub.embeddedkafka.streams.EmbeddedStreamsConfig

final class EmbeddedStreamsConfigImpl
    extends EmbeddedStreamsConfig[EmbeddedKafkaConfig] {

  override def config(streamName: String, extraConfig: Map[String, AnyRef])(
      implicit kafkaConfig: EmbeddedKafkaConfig): Map[String, AnyRef] =
    baseStreamConfig(streamName) ++ EmbeddedKafka.configForSchemaRegistry ++ extraConfig

}
