package net.manub.embeddedkafka.schemaregistry.avro

import io.confluent.kafka.serializers.{
  AbstractKafkaSchemaSerDeConfig,
  KafkaAvroDeserializerConfig,
  KafkaAvroDeserializer => ConfluentKafkaAvroDeserializer,
  KafkaAvroSerializer => ConfluentKafkaAvroSerializer
}
import net.manub.embeddedkafka.schemaregistry.EmbeddedKafkaConfig
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.{Serde, Serdes}

import scala.collection.JavaConverters._

@deprecated(
  "Avro-related classes will be removed soon",
  since = "5.5.0"
)
object AvroSerdes {

  protected def configForSchemaRegistry(
      implicit config: EmbeddedKafkaConfig
  ): Map[String, Object] =
    Map(
      AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> s"http://localhost:${config.schemaRegistryPort}"
    )

  protected def specificAvroReaderConfigForSchemaRegistry(
      implicit config: EmbeddedKafkaConfig
  ): Map[String, Object] =
    configForSchemaRegistry ++ Map(
      KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG -> true.toString
    )

  def specific[T <: SpecificRecord](
      isKey: Boolean = false,
      extraConfig: Map[String, Object] = Map.empty
  )(
      implicit config: EmbeddedKafkaConfig
  ): Serde[T] =
    serdeFrom[T](
      configForSchemaRegistry ++ extraConfig,
      specificAvroReaderConfigForSchemaRegistry ++ extraConfig, //need this to support SpecificRecord
      isKey
    )

  def generic(
      isKey: Boolean = false,
      extraConfig: Map[String, Object] = Map.empty
  )(
      implicit config: EmbeddedKafkaConfig
  ): Serde[GenericRecord] =
    serdeFrom[GenericRecord](
      configForSchemaRegistry ++ extraConfig,
      configForSchemaRegistry ++ extraConfig,
      isKey
    )

  private def serdeFrom[T](
      serConfig: Map[String, Object],
      deserConfig: Map[String, Object],
      isKey: Boolean
  ): Serde[T] = {
    val ser = new ConfluentKafkaAvroSerializer
    ser.configure(serConfig.asJava, isKey)
    val deser = new ConfluentKafkaAvroDeserializer
    deser.configure(deserConfig.asJava, isKey)

    Serdes.serdeFrom(ser, deser).asInstanceOf[Serde[T]]
  }
}
