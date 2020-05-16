package net.manub.embeddedkafka.schemaregistry.ops

import java.net.{ServerSocket, URI}
import java.util.Properties

import io.confluent.kafka.schemaregistry.rest.{
  SchemaRegistryConfig,
  SchemaRegistryRestApplication
}
import io.confluent.rest.RestConfig
import net.manub.embeddedkafka.EmbeddedServer
import net.manub.embeddedkafka.ops.RunningServersOps
import net.manub.embeddedkafka.schemaregistry.{EmbeddedKafkaConfig, EmbeddedSR}

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

    val props = new Properties
    props.putAll(customProperties.asJava)
    props.put(
      RestConfig.LISTENERS_CONFIG,
      s"http://localhost:$actualSchemaRegistryPort"
    )
    props.put(
      SchemaRegistryConfig.KAFKASTORE_BOOTSTRAP_SERVERS_CONFIG,
      s"localhost:$kafkaPort"
    )

    val restApp = new SchemaRegistryRestApplication(props)
    restApp.start()
    restApp
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
