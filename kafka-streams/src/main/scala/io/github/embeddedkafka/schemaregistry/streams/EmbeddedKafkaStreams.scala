package io.github.embeddedkafka.schemaregistry.streams

import io.github.embeddedkafka.schemaregistry.{
  EmbeddedKafkaConfig,
  EmbeddedKafka
}
import io.github.embeddedkafka.streams.EmbeddedKafkaStreamsSupport

trait EmbeddedKafkaStreams
    extends EmbeddedKafkaStreamsSupport[EmbeddedKafkaConfig]
    with EmbeddedKafka {
  override protected[embeddedkafka] val streamsConfig =
    new EmbeddedStreamsConfigImpl
}

object EmbeddedKafkaStreams extends EmbeddedKafkaStreams
