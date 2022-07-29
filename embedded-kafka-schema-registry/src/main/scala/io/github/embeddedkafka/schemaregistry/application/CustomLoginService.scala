package io.github.embeddedkafka.schemaregistry.application

import io.github.embeddedkafka.schemaregistry.EmbeddedKafkaConfig.SchemaRegistryRestAuth
import org.eclipse.jetty.security.AbstractLoginService.UserPrincipal
import org.eclipse.jetty.security.{IdentityService, LoginService}
import org.eclipse.jetty.server.UserIdentity
import org.eclipse.jetty.util.security.Password

import javax.security.auth.Subject
import javax.servlet.ServletRequest

/**
  * A simple LoginService that lookups user credentials from
  * [[SchemaRegistryRestAuth]] credentials.
  */
private[application] class CustomLoginService(auth: SchemaRegistryRestAuth.Some)
    extends LoginService {

  protected var identityService: IdentityService = _

  override def getName: String = "Custom"

  override def login(
      username: String,
      credentials: Any,
      request: ServletRequest
  ): UserIdentity =
    if (credentials != null && credentials.isInstanceOf[String]) {
      val creds = credentials.asInstanceOf[String]

      @inline def toUserIdentity(rolesOpt: Option[Set[String]]) =
        rolesOpt.map { roles =>
          identityService.newUserIdentity(
            new Subject(),
            new UserPrincipal(username, new Password(creds)),
            roles.toArray
          )
        }.orNull

      auth match {
        case SchemaRegistryRestAuth.Basic(credentials) =>
          toUserIdentity(
            credentials
              .collectFirst {
                case SchemaRegistryRestAuth.Basic.UserCredential(u, p, r)
                    if u == username && p == creds =>
                  r
              }
          )

        case SchemaRegistryRestAuth.Bearer(credentials) =>
          toUserIdentity(
            credentials
              .collectFirst {
                case SchemaRegistryRestAuth.Bearer.TokenCredential(t, r)
                    if t == creds =>
                  r
              }
          )
      }
    } else {
      null
    }

  override def validate(user: UserIdentity): Boolean = true

  override def getIdentityService: IdentityService = identityService

  override def setIdentityService(service: IdentityService): Unit =
    identityService = service

  override def logout(user: UserIdentity): Unit = ()
}
