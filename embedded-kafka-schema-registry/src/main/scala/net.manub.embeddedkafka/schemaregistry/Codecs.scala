package net.manub.embeddedkafka.schemaregistry

import org.apache.kafka.clients.consumer.ConsumerRecord

object Codecs {
  implicit def stringKeyGenericValueCrDecoder[V]
      : ConsumerRecord[String, V] => (String, V) =
    cr => (cr.key, cr.value)
  implicit def genericValueCrDecoder[V]: ConsumerRecord[String, V] => V =
    _.value
  implicit def stringKeyGenericValueTopicCrDecoder[V]
      : ConsumerRecord[String, V] => (String, String, V) =
    cr => (cr.topic, cr.key, cr.value)
}
