package net.manub.embeddedkafka

import net.manub.embeddedkafka.schemaregistry.avro.AvroSerdes
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

package object schemaregistry {
  // SpecificRecord
  @deprecated("Use AvroSerdes.specific instead", since = "2.4.1")
  implicit def specificAvroValueSerde[T <: SpecificRecord](
      implicit config: EmbeddedKafkaConfig
  ): Serde[T] =
    AvroSerdes.specific[T]()

  @deprecated("Use AvroSerdes.specific instead", since = "2.4.1")
  implicit def specificAvroValueSerializer[T <: SpecificRecord](
      implicit config: EmbeddedKafkaConfig
  ): Serializer[T] =
    AvroSerdes.specific[T]().serializer

  @deprecated("Use AvroSerdes.specific instead", since = "2.4.1")
  implicit def specificAvroValueDeserializer[T <: SpecificRecord](
      implicit config: EmbeddedKafkaConfig
  ): Deserializer[T] =
    AvroSerdes.specific[T]().deserializer

  // GenericRecord
  @deprecated("Use AvroSerdes.generic instead", since = "2.4.1")
  implicit def genericAvroValueSerde(
      implicit config: EmbeddedKafkaConfig
  ): Serde[GenericRecord] =
    AvroSerdes.generic()

  @deprecated("Use AvroSerdes.generic instead", since = "2.4.1")
  implicit def genericAvroValueSerializer(
      implicit config: EmbeddedKafkaConfig
  ): Serializer[GenericRecord] =
    AvroSerdes.generic().serializer

  @deprecated("Use AvroSerdes.generic instead", since = "2.4.1")
  implicit def genericAvroValueDeserializer(
      implicit config: EmbeddedKafkaConfig
  ): Deserializer[GenericRecord] =
    AvroSerdes.generic().deserializer

}
