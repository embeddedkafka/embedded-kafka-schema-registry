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

/**
  * The [[SchemaRegistryRestApplication]] relies on
  * [[org.eclipse.jetty.jaas.JAASLoginService]] for basic authentication that
  * could be configured only via
  * <code>-Djava.security.auth.login.config=`<config_file>`</code>. It also
  * doesn't provide any implementation for bearer token authentication. This
  * implementation relies on [[CustomLoginService]], it handles bearer
  * authentication as well.
  */
private[schemaregistry] class CustomSchemaRegistryRestApplication(
    restAuth: SchemaRegistryRestAuth,
    config: SchemaRegistryConfig
) extends SchemaRegistryRestApplication(config) {
  override def createAuthenticator(): LoginAuthenticator = restAuth match {
    case SchemaRegistryRestAuth.None      => null
    case SchemaRegistryRestAuth.Basic(_)  => new BasicAuthenticator()
    case SchemaRegistryRestAuth.Bearer(_) => new BearerAuthenticator()
  }

  override def createLoginService(): LoginService = restAuth match {
    case SchemaRegistryRestAuth.None         => null
    case auth: SchemaRegistryRestAuth.Basic  => new CustomLoginService(auth)
    case auth: SchemaRegistryRestAuth.Bearer => new CustomLoginService(auth)
  }
}
