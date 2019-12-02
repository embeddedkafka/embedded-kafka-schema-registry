package net.manub.embeddedkafka.schemaregistry.streams

import net.manub.embeddedkafka.schemaregistry.{
  EmbeddedKafkaConfig,
  EmbeddedKafka
}
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsSupport

trait EmbeddedKafkaStreams
    extends EmbeddedKafkaStreamsSupport[EmbeddedKafkaConfig]
    with EmbeddedKafka {
  override protected[embeddedkafka] val streamsConfig =
    new EmbeddedStreamsConfigImpl
}
