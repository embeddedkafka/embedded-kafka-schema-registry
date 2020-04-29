package net.manub.embeddedkafka.schemaregistry.ops

import java.net.ServerSocket
import java.util.Properties

import io.confluent.kafka.schemaregistry.{CompatibilityLevel, RestApp}
import net.manub.embeddedkafka.EmbeddedServer
import net.manub.embeddedkafka.ops.RunningServersOps
import net.manub.embeddedkafka.schemaregistry.{EmbeddedKafkaConfig, EmbeddedSR}

/**
  * Trait for Schema Registry-related actions.
  * Relies on [[RestApp]].
  */
trait SchemaRegistryOps {

  /**
    * Starts a Schema Registry instance.
    *
    * @param schemaRegistryPort the port to run Schema Registry on, if 0 an available port will be used
    * @param zooKeeperPort      the port ZooKeeper is running on
    * @param compatibilityLevel the schema compatibility level
    * @param properties         additional [[Properties]]
    */
  def startSchemaRegistry(
      schemaRegistryPort: Int,
      zooKeeperPort: Int,
      compatibilityLevel: CompatibilityLevel = CompatibilityLevel.NONE,
      properties: Properties = new Properties
  ): RestApp = {
    def findAvailablePort: Int = {
      val server = new ServerSocket(0)
      val port   = server.getLocalPort
      server.close()
      port
    }

    val actualSchemaRegistryPort =
      if (schemaRegistryPort == 0) findAvailablePort else schemaRegistryPort

    val server = new RestApp(
      actualSchemaRegistryPort,
      s"localhost:$zooKeeperPort",
      "_schemas",
      compatibilityLevel.name,
      properties
    )
    server.start()
    server
  }
}

/**
  * [[SchemaRegistryOps]] extension relying on [[RunningServersOps]] for
  * keeping track of running [[EmbeddedSR]] instances.
  *
  * @see [[RunningServersOps]]
  */
trait RunningSchemaRegistryOps {
  this: SchemaRegistryOps with RunningServersOps =>

  def startSchemaRegistry(implicit config: EmbeddedKafkaConfig): EmbeddedSR = {
    val restApp = EmbeddedSR(
      startSchemaRegistry(
        config.schemaRegistryPort,
        config.zooKeeperPort,
        config.compatibilityLevel
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
}
