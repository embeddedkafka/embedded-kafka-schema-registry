package io.github.embeddedkafka.schemaregistry

import io.confluent.kafka.schemaregistry.client.{
  CachedSchemaRegistryClient,
  SchemaRegistryClientConfig
}
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.rest.RestConfig
import io.github.embeddedkafka.schemaregistry.EmbeddedKafka._
import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig.SchemaRegistryRestAuth
import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaSpecSupport.{
  Available,
  NotAvailable
}
import org.scalatest.Assertion

import scala.jdk.CollectionConverters._

class EmbeddedKafkaTraitSpec extends EmbeddedKafkaSpecSupport {
  "the withRunningKafka method" should {
    "start a Schema Registry server on a specified port" in {
      implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(schemaRegistryPort = 12345)

      withRunningKafka {
        expectedServerStatus(12345, Available)
      }
    }
  }

  "the withRunningKafkaOnFoundPort method" should {
    "start a Schema Registry server on an available port if 0" in {
      val userDefinedConfig: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(schemaRegistryPort = 0)
      withRunningKafkaOnFoundPort(userDefinedConfig) { actualConfig =>
        expectedServerStatus(actualConfig.schemaRegistryPort, Available)
      }
    }

    "start and stop Kafka, Zookeeper, and Schema Registry successfully on non-zero ports" in {
      val userDefinedConfig =
        EmbeddedKafkaConfig(
          kafkaPort = 12345,
          zooKeeperPort = 12346,
          schemaRegistryPort = 12347
        )

      val actualConfig = withRunningKafkaOnFoundPort(userDefinedConfig) {
        actualConfig =>
          actualConfig shouldBe userDefinedConfig
          everyServerIsAvailable(actualConfig)
          actualConfig
      }
      noServerIsAvailable(actualConfig)
    }

    "start a Schema Registry server with Basic authentication enabled" in {
      val userDefinedConfig: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(
          schemaRegistryPort = 0,
          customSchemaRegistryProperties = Map(
            RestConfig.AUTHENTICATION_METHOD_CONFIG -> RestConfig.AUTHENTICATION_METHOD_BASIC,
            RestConfig.AUTHENTICATION_ROLES_CONFIG -> "god"
          ),
          schemaRegistryRestAuth = SchemaRegistryRestAuth.Basic(credentials =
            Set(
              SchemaRegistryRestAuth.Basic.UserCredential(
                username = "user",
                password = "pass",
                roles = Set("god")
              )
            )
          )
        )

      withRunningKafkaOnFoundPort(userDefinedConfig) { actualConfig =>
        expectedServerStatus(actualConfig.schemaRegistryPort, Available)

        val unauthorizedClient = new CachedSchemaRegistryClient(
          s"http://localhost:${actualConfig.schemaRegistryPort}",
          1
        )

        val caught = intercept[RestClientException](
          unauthorizedClient.getAllSubjects
        )
        caught.getStatus shouldBe 401

        val client = new CachedSchemaRegistryClient(
          s"http://localhost:${actualConfig.schemaRegistryPort}",
          1,
          Map(
            SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE -> "USER_INFO",
            SchemaRegistryClientConfig.USER_INFO_CONFIG -> "user:pass"
          ).asJava
        )

        client.getAllSubjects.asScala shouldBe empty
      }
    }

    "start a Schema Registry server with Bearer authentication enabled" in {
      val userDefinedConfig: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(
          schemaRegistryPort = 0,
          customSchemaRegistryProperties = Map(
            RestConfig.AUTHENTICATION_METHOD_CONFIG -> RestConfig.AUTHENTICATION_METHOD_BEARER,
            RestConfig.AUTHENTICATION_ROLES_CONFIG -> "god"
          ),
          schemaRegistryRestAuth = SchemaRegistryRestAuth.Bearer(credentials =
            Set(
              SchemaRegistryRestAuth.Bearer.TokenCredential(
                token = "token_secret",
                roles = Set("god")
              )
            )
          )
        )

      withRunningKafkaOnFoundPort(userDefinedConfig) { actualConfig =>
        expectedServerStatus(actualConfig.schemaRegistryPort, Available)

        val unauthorizedClient = new CachedSchemaRegistryClient(
          s"http://localhost:${actualConfig.schemaRegistryPort}",
          1
        )

        val caught = intercept[RestClientException](
          unauthorizedClient.getAllSubjects
        )
        caught.getStatus shouldBe 401

        val client = new CachedSchemaRegistryClient(
          s"http://localhost:${actualConfig.schemaRegistryPort}",
          1,
          Map(
            SchemaRegistryClientConfig.BEARER_AUTH_CREDENTIALS_SOURCE -> "STATIC_TOKEN",
            SchemaRegistryClientConfig.BEARER_AUTH_TOKEN_CONFIG -> "token_secret"
          ).asJava
        )

        client.getAllSubjects.asScala shouldBe empty
      }
    }
  }

  private def everyServerIsAvailable(config: EmbeddedKafkaConfig): Assertion = {
    expectedServerStatus(config.kafkaPort, Available)
    expectedServerStatus(config.schemaRegistryPort, Available)
    expectedServerStatus(config.zooKeeperPort, Available)
  }

  private def noServerIsAvailable(config: EmbeddedKafkaConfig): Assertion = {
    expectedServerStatus(config.kafkaPort, NotAvailable)
    expectedServerStatus(config.schemaRegistryPort, NotAvailable)
    expectedServerStatus(config.zooKeeperPort, NotAvailable)
  }
}
