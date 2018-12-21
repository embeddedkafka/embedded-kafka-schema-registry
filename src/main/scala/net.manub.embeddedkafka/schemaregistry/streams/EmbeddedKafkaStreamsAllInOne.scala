package net.manub.embeddedkafka.schemaregistry.streams

import net.manub.embeddedkafka.schemaregistry.EmbeddedKafkaConfig
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOneSupport

/** Convenience trait exposing [[EmbeddedKafkaStreams.runStreams]]
  * as well as [[net.manub.embeddedkafka.Consumers]] api for easily creating and
  * querying consumers.
  *
  * @see [[EmbeddedKafkaStreams]]
  * @see [[net.manub.embeddedkafka.Consumers]]
  */
trait EmbeddedKafkaStreamsAllInOne
    extends EmbeddedKafkaStreamsAllInOneSupport[EmbeddedKafkaConfig]
    with EmbeddedKafkaStreams
