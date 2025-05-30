package io.github.embeddedkafka.schemaregistry

import java.nio.file.{Files, Path}

import io.github.embeddedkafka.ops.{EmbeddedKafkaOps, RunningEmbeddedKafkaOps}
import io.github.embeddedkafka.schemaregistry.ops.{
  RunningSchemaRegistryOps,
  SchemaRegistryOps
}
import io.github.embeddedkafka.{EmbeddedKafkaSupport, EmbeddedServer, ServerOps}

trait EmbeddedKafka
    extends EmbeddedKafkaSupport[EmbeddedKafkaConfig]
    with EmbeddedKafkaOps[EmbeddedKafkaConfig, EmbeddedKWithSR]
    with SchemaRegistryOps {
  override private[embeddedkafka] def baseConsumerConfig(
      implicit config: EmbeddedKafkaConfig
  ): Map[String, Object] =
    defaultConsumerConfig ++ config.customConsumerProperties

  override private[embeddedkafka] def baseProducerConfig(
      implicit config: EmbeddedKafkaConfig
  ): Map[String, Object] =
    defaultProducerConf ++ config.customProducerProperties

  override private[embeddedkafka] def withRunningServers[T](
      config: EmbeddedKafkaConfig,
      kafkaLogsDir: Path
  )(body: EmbeddedKafkaConfig => T): T = {
    val (broker, controller) =
      startKafka(
        config.kafkaPort,
        config.controllerPort,
        config.customBrokerProperties,
        kafkaLogsDir
      )

    val actualBrokerPort     = EmbeddedKafka.kafkaPort(broker)
    val actualControllerPort = EmbeddedKafka.controllerPort(controller)

    val restApp = startSchemaRegistry(
      config.schemaRegistryPort,
      actualBrokerPort,
      config.customSchemaRegistryProperties
    )

    val configWithUsedPorts = EmbeddedKafkaConfig(
      actualBrokerPort,
      actualControllerPort,
      EmbeddedKafka.schemaRegistryPort(restApp),
      config.customBrokerProperties,
      config.customProducerProperties,
      config.customConsumerProperties,
      config.customSchemaRegistryProperties
    )

    try {
      body(configWithUsedPorts)
    } finally {
      restApp.stop()
      // In combined mode, we want to shut down the broker first, since the controller may be
      // needed for controlled shutdown. Additionally, the controller shutdown process currently
      // stops the raft client early on, which would disrupt broker shutdown.
      broker.shutdown()
      controller.shutdown()
      broker.awaitShutdown()
      controller.awaitShutdown()
    }
  }
}

object EmbeddedKafka
    extends EmbeddedKafka
    with RunningEmbeddedKafkaOps[EmbeddedKafkaConfig, EmbeddedKWithSR]
    with RunningSchemaRegistryOps {
  override def start()(
      implicit config: EmbeddedKafkaConfig
  ): EmbeddedKWithSR = {
    val kafkaLogsDir = Files.createTempDirectory("kafka-logs")

    val (broker, controller) = startKafka(
      kafkaPort = config.kafkaPort,
      controllerPort = config.controllerPort,
      customBrokerProperties = config.customBrokerProperties,
      kafkaLogDir = kafkaLogsDir
    )

    val actualBrokerPort     = EmbeddedKafka.kafkaPort(broker)
    val actualControllerPort = EmbeddedKafka.controllerPort(controller)

    val restApp = EmbeddedSR(
      startSchemaRegistry(
        config.schemaRegistryPort,
        actualBrokerPort,
        config.customSchemaRegistryProperties
      )
    )

    val configWithUsedPorts = EmbeddedKafkaConfigImpl(
      kafkaPort = actualBrokerPort,
      controllerPort = actualControllerPort,
      schemaRegistryPort = EmbeddedKafka.schemaRegistryPort(restApp.app),
      customBrokerProperties = config.customBrokerProperties,
      customProducerProperties = config.customProducerProperties,
      customConsumerProperties = config.customConsumerProperties,
      customSchemaRegistryProperties = config.customSchemaRegistryProperties
    )

    val server = EmbeddedKWithSR(
      broker = broker,
      controller = controller,
      app = restApp,
      logsDirs = kafkaLogsDir,
      configWithUsedPorts
    )

    runningServers.add(server)
    server
  }

  override def isRunning: Boolean =
    runningServers.list
      .toFilteredSeq[EmbeddedKWithSR](s =>
        // Need to consider both independently-started Schema Registry and
        // all-in-one Kafka with SR
        isEmbeddedKWithSR(s) || isEmbeddedSR(s)
      )
      .nonEmpty

  private[embeddedkafka] def isEmbeddedKWithSR(
      server: EmbeddedServer
  ): Boolean =
    server.isInstanceOf[EmbeddedKWithSR]
}
