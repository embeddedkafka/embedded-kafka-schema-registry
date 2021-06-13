package net.manub.embeddedkafka

package object schemaregistry {

  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig instead",
    "6.2.0"
  )
  type EmbeddedKafkaConfig =
    io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfigImpl instead",
    "6.2.0"
  )
  type EmbeddedKafkaConfigImpl =
    io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfigImpl
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedKafka instead",
    "6.2.0"
  )
  type EmbeddedKafka = io.github.embeddedkafka.schemaregistry.EmbeddedKafka
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedSR instead",
    "6.2.0"
  )
  type EmbeddedSR = io.github.embeddedkafka.schemaregistry.EmbeddedSR
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedKWithSR instead",
    "6.2.0"
  )
  type EmbeddedKWithSR = io.github.embeddedkafka.schemaregistry.EmbeddedKWithSR

  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.Codecs instead",
    "6.2.0"
  )
  val Codecs = io.github.embeddedkafka.schemaregistry.Codecs
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig instead",
    "6.2.0"
  )
  val EmbeddedKafkaConfig =
    io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig
  @deprecated(
    "Use io.github.embeddedkafka.schemaregistry.EmbeddedKafka instead",
    "6.2.0"
  )
  val EmbeddedKafka = io.github.embeddedkafka.schemaregistry.EmbeddedKafka

}
