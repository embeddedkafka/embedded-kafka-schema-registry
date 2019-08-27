package net.manub.embeddedkafka.schemaregistry

import java.net.InetSocketAddress

import net.manub.embeddedkafka.{
  Connection,
  EmbeddedKafkaSpecSupport => OriginalEmbeddedKafkaSpecSupport,
  TcpClient
}

import scala.concurrent.duration._

abstract class EmbeddedKafkaSpecSupport
    extends OriginalEmbeddedKafkaSpecSupport {

  def schemaRegistryIsAvailable(schemaRegistryPort: Int = 6002): Unit = {
    system.actorOf(
      TcpClient.props(
        new InetSocketAddress("localhost", schemaRegistryPort),
        testActor
      )
    )
    expectMsg(1.second, Connection.Success)
  }

  def schemaRegistryIsNotAvailable(schemaRegistryPort: Int = 6002): Unit = {
    system.actorOf(
      TcpClient.props(
        new InetSocketAddress("localhost", schemaRegistryPort),
        testActor
      )
    )
    expectMsg(1.second, Connection.Failure)
  }

}
