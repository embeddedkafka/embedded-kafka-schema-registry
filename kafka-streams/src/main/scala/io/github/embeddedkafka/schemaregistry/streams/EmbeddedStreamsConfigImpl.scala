package io.github.embeddedkafka.schemaregistry.streams

import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig
import io.github.embeddedkafka.streams.EmbeddedStreamsConfig

final class EmbeddedStreamsConfigImpl
    extends EmbeddedStreamsConfig[EmbeddedKafkaConfig] {
  override def config(streamName: String, extraConfig: Map[String, AnyRef])(
      implicit kafkaConfig: EmbeddedKafkaConfig
  ): Map[String, AnyRef] =
    baseStreamConfig(streamName) ++ extraConfig
}
