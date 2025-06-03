package io.github.embeddedkafka.schemaregistry

import java.nio.file.Path
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryRestApplication
import io.github.embeddedkafka.{EmbeddedServer, EmbeddedServerWithKafka}
import kafka.server.{BrokerServer, ControllerServer}

import scala.reflect.io.Directory

/**
  * An instance of an embedded Schema Registry app.
  *
  * @param app
  *   the Schema Registry app.
  */
case class EmbeddedSR(app: SchemaRegistryRestApplication)
    extends EmbeddedServer {

  /**
    * Shuts down the app.
    */
  override def stop(clearLogs: Boolean = false): Unit = app.stop()
}

case class EmbeddedKWithSR(
    broker: BrokerServer,
    controller: ControllerServer,
    app: EmbeddedSR,
    logsDirs: Path,
    config: EmbeddedKafkaConfig
) extends EmbeddedServerWithKafka {
  override def stop(clearLogs: Boolean): Unit = {
    app.stop()

    // In combined mode, we want to shut down the broker first, since the controller may be
    // needed for controlled shutdown. Additionally, the controller shutdown process currently
    // stops the raft client early on, which would disrupt broker shutdown.
    broker.shutdown()
    controller.shutdown()
    broker.awaitShutdown()
    controller.awaitShutdown()

    if (clearLogs) {
      val _ = Directory(logsDirs.toFile).deleteRecursively()
    }
  }
}
