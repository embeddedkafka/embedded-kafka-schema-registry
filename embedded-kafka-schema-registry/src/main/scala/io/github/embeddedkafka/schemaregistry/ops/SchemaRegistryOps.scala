package io.github.embeddedkafka.schemaregistry.ops

import java.net.{ServerSocket, URI}
import java.util.Properties

import io.confluent.kafka.schemaregistry.rest.{
  SchemaRegistryConfig,
  SchemaRegistryRestApplication
}
import io.confluent.rest.RestConfig
import io.github.embeddedkafka.EmbeddedServer
import io.github.embeddedkafka.ops.RunningServersOps
import io.github.embeddedkafka.schemaregistry.{EmbeddedKafkaConfig, EmbeddedSR}

import scala.jdk.CollectionConverters._

/**
  * Trait for Schema Registry-related actions.
  * Relies on `io.confluent.kafka.schemaregistry.rest.SchemaRegistryRestApplication`.
  */
trait SchemaRegistryOps {

  private[embeddedkafka] def startSchemaRegistry(
      schemaRegistryPort: Int,
      kafkaPort: Int,
      customProperties: Map[String, String]
  ): SchemaRegistryRestApplication = {
    def findAvailablePort: Int = {
      val server = new ServerSocket(0)
      val port   = server.getLocalPort
      server.close()
      port
    }

    val actualSchemaRegistryPort =
      if (schemaRegistryPort == 0) findAvailablePort else schemaRegistryPort

    val restAppProperties = customProperties ++ Map(
      RestConfig.LISTENERS_CONFIG                              -> s"http://localhost:$actualSchemaRegistryPort",
      SchemaRegistryConfig.KAFKASTORE_BOOTSTRAP_SERVERS_CONFIG -> s"localhost:$kafkaPort"
    )

    val restApp = new SchemaRegistryRestApplication(
      new SchemaRegistryConfig(map2Properties(restAppProperties))
    )
    restApp.start()
    restApp
  }

  private[this] def map2Properties(map: Map[String, AnyRef]): Properties = {
    val props = new Properties
    props.putAll(map.asJava)
    props
  }
}

/**
  * [[SchemaRegistryOps]] extension relying on `RunningServersOps` for
  * keeping track of running [[EmbeddedSR]] instances.
  */
trait RunningSchemaRegistryOps {
  this: SchemaRegistryOps with RunningServersOps =>

  def startSchemaRegistry(implicit config: EmbeddedKafkaConfig): EmbeddedSR = {
    val restApp = EmbeddedSR(
      startSchemaRegistry(
        config.schemaRegistryPort,
        config.kafkaPort,
        config.customSchemaRegistryProperties
      )
    )
    runningServers.add(restApp)
    restApp
  }

  /**
    * Stops all in memory Schema Registry instances.
    */
  def stopSchemaRegistry(): Unit =
    runningServers.stopAndRemove(isEmbeddedSR)

  private[embeddedkafka] def isEmbeddedSR(server: EmbeddedServer): Boolean =
    server.isInstanceOf[EmbeddedSR]

  private[embeddedkafka] def schemaRegistryPort(
      restApp: SchemaRegistryRestApplication
  ): Int = {
    val listeners = restApp.getConfiguration.originalProperties
      .getProperty(RestConfig.LISTENERS_CONFIG)
    URI.create(listeners).getPort
  }

}
