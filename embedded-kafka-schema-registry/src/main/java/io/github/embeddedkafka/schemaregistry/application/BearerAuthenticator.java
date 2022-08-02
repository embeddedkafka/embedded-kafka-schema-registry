package io.github.embeddedkafka.schemaregistry.application;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class BearerAuthenticator extends LoginAuthenticator {
    private static final String AUTH_METHOD = "BEARER";
    private static final String AUTH_METHOD_PREFIX = "Bearer ";

    protected BearerAuthenticator() {
    }

    @Override
    public String getAuthMethod() {
        return AUTH_METHOD;
    }

    @Override
    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse resp = (HttpServletResponse) response;
        final String credentials = req.getHeader(HttpHeader.AUTHORIZATION.asString());

        if (!mandatory) {
            return new DeferredAuthentication(this);
        }

        try {
            if (StringUtils.startsWith(credentials, AUTH_METHOD_PREFIX)) {
                final String token = credentials.substring(AUTH_METHOD_PREFIX.length());
                final UserIdentity user = login(null, token, request);
                if (user != null) {
                    return new UserAuthentication(getAuthMethod(), user);
                } else {
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return Authentication.SEND_CONTINUE;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return Authentication.SEND_CONTINUE;
            }
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
    }

    @Override
    public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication.User validatedUser) {
        return true;
    }
}
