package net.manub.embeddedkafka.schemaregistry

package object streams {

  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams instead",
    "6.2.0"
  )
  type EmbeddedKafkaStreams =
    io.github.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.streams.EmbeddedStreamsConfigImpl instead",
    "6.2.0"
  )
  type EmbeddedStreamsConfigImpl =
    io.github.embeddedkafka.schemaregistry.streams.EmbeddedStreamsConfigImpl

  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams instead",
    "6.2.0"
  )
  val EmbeddedKafkaStreams =
    io.github.embeddedkafka.schemaregistry.streams.EmbeddedKafkaStreams

}
