package io.github.embeddedkafka.schemaregistry.application

import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.security.authentication.{
  DeferredAuthentication,
  LoginAuthenticator
}
import org.eclipse.jetty.security.{ServerAuthException, UserAuthentication}
import org.eclipse.jetty.server.Authentication

import java.io.IOException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.{ServletRequest, ServletResponse}

/**
  * A simple login authenticator for bearer token authentication method.
  */
private[application] class BearerAuthenticator extends LoginAuthenticator {
  override def getAuthMethod: String = "BEARER"

  override def validateRequest(
      request: ServletRequest,
      response: ServletResponse,
      mandatory: Boolean
  ): Authentication = {
    val req         = request.asInstanceOf[HttpServletRequest]
    val resp        = response.asInstanceOf[HttpServletResponse]
    val credentials = req.getHeader(HttpHeader.AUTHORIZATION.asString())

    if (!mandatory) {
      new DeferredAuthentication(this)
    } else {
      try {
        if (credentials != null && credentials.startsWith("Bearer ")) {
          val token = credentials.stripPrefix("Bearer ")
          val user  = login(null, token, request)
          if (user != null) {
            new UserAuthentication(getAuthMethod, user)
          } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            Authentication.SEND_CONTINUE
          }
        } else {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
          Authentication.SEND_CONTINUE
        }
      } catch {
        case e: IOException => throw new ServerAuthException(e)
      }
    }

  }

  override def secureResponse(
      request: ServletRequest,
      response: ServletResponse,
      mandatory: Boolean,
      validatedUser: Authentication.User
  ): Boolean = true
}
