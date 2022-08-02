package io.github.embeddedkafka.schemaregistry.application

import io.confluent.kafka.schemaregistry.rest.{
  SchemaRegistryConfig,
  SchemaRegistryRestApplication
}
import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig.SchemaRegistryRestAuth
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.security.authentication.{
  BasicAuthenticator,
  LoginAuthenticator
}

import scala.jdk.CollectionConverters._

/**
  * The [[SchemaRegistryRestApplication]] relies on
  * [[org.eclipse.jetty.jaas.JAASLoginService]] for basic authentication that
  * could be configured only via
  * <code>-Djava.security.auth.login.config=`<config_file>`</code>. It also
  * doesn't provide any implementation for bearer token authentication. This
  * implementation relies on custom implementations of [[LoginService]], it
  * handles bearer authentication as well.
  */
private[schemaregistry] class CustomSchemaRegistryRestApplication(
    restAuth: SchemaRegistryRestAuth,
    config: SchemaRegistryConfig
) extends SchemaRegistryRestApplication(config) {
  override def createAuthenticator(): LoginAuthenticator = restAuth match {
    case SchemaRegistryRestAuth.None      => super.createAuthenticator()
    case SchemaRegistryRestAuth.Basic(_)  => new BasicAuthenticator()
    case SchemaRegistryRestAuth.Bearer(_) => new BearerAuthenticator()
  }

  override def createLoginService(): LoginService = restAuth match {
    case SchemaRegistryRestAuth.None => super.createLoginService()
    case SchemaRegistryRestAuth.Basic(creds) =>
      val javaCreds = creds
        .map(c =>
          new CustomBasicLoginService.UserCredentials(
            c.username,
            c.password,
            c.roles.asJava
          )
        )
        .asJava
      new CustomBasicLoginService(javaCreds)
    case SchemaRegistryRestAuth.Bearer(creds) =>
      val javaCreds = creds
        .map(c =>
          new CustomBearerLoginService.UserCredentials(c.token, c.roles.asJava)
        )
        .asJava
      new CustomBearerLoginService(javaCreds)
  }
}
