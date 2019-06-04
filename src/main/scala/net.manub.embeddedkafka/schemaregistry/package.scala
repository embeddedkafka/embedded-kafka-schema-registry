package net.manub.embeddedkafka

import io.confluent.kafka.serializers.{
  KafkaAvroDeserializer => ConfluentKafkaAvroDeserializer,
  KafkaAvroSerializer => ConfluentKafkaAvroSerializer
}
import net.manub.embeddedkafka.schemaregistry.EmbeddedKafka.{
  configForSchemaRegistry,
  specificAvroReaderConfigForSchemaRegistry
}
import net.manub.embeddedkafka.schemaregistry.{
  EmbeddedKafkaConfig => EmbeddedKafkaSRConfig
}
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.{
  Deserializer,
  Serde,
  Serdes,
  Serializer
}

import scala.collection.JavaConverters._

package object schemaregistry {
  // SpecificRecord
  implicit def specificAvroValueSerde[T <: SpecificRecord](
      implicit config: EmbeddedKafkaSRConfig): Serde[T] = {
    serdeFrom[T](
      configForSchemaRegistry,
      specificAvroReaderConfigForSchemaRegistry, //need this to support SpecificRecord
      isKey = false)
  }

  implicit def specificAvroValueSerializer[T <: SpecificRecord](
      implicit config: EmbeddedKafkaSRConfig): Serializer[T] = {
    specificAvroValueSerde[T].serializer
  }

  implicit def specificAvroValueDeserializer[T <: SpecificRecord](
      implicit config: EmbeddedKafkaSRConfig): Deserializer[T] = {
    specificAvroValueSerde[T].deserializer
  }

  // GenericRecord
  implicit def genericAvroValueSerde(
      implicit config: EmbeddedKafkaSRConfig): Serde[GenericRecord] = {
    serdeFrom[GenericRecord](configForSchemaRegistry,
                             configForSchemaRegistry,
                             isKey = false)
  }

  implicit def genericAvroValueSerializer(
      implicit config: EmbeddedKafkaSRConfig): Serializer[GenericRecord] = {
    genericAvroValueSerde.serializer
  }

  implicit def genericAvroValueDeserializer(
      implicit config: EmbeddedKafkaSRConfig): Deserializer[GenericRecord] = {
    genericAvroValueSerde.deserializer
  }

  private def serdeFrom[T](serConfig: Map[String, Object],
                           deserConfig: Map[String, Object],
                           isKey: Boolean) = {
    val ser = new ConfluentKafkaAvroSerializer
    ser.configure(serConfig.asJava, isKey)
    val deser = new ConfluentKafkaAvroDeserializer
    deser.configure(deserConfig.asJava, isKey)

    Serdes.serdeFrom(ser, deser).asInstanceOf[Serde[T]]
  }
}
