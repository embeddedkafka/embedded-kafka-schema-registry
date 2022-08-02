package io.github.embeddedkafka.schemaregistry.application;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Password;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import java.util.Optional;
import java.util.Set;

public class CustomBearerLoginService implements LoginService {
    public static final String SERVICE_NAME = "CUSTOM BEARER";

    private IdentityService identityService;

    private final Set<UserCredentials> userCredentials;

    public CustomBearerLoginService(Set<UserCredentials> userCredentials) {
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public UserIdentity login(String username, Object credentials, ServletRequest request) {
        if (credentials instanceof String) {
            final Optional<UserCredentials> foundCred =
                    userCredentials
                            .stream()
                            .filter(c -> c.getToken().equals(credentials))
                            .findFirst();

            if (foundCred.isPresent()) {
                final UserCredentials cred = foundCred.get();
                return identityService.newUserIdentity(
                        new Subject(),
                        new AbstractLoginService.UserPrincipal(cred.getToken(), new Password(cred.getToken())),
                        cred.getRoles().toArray(new String[]{})
                );
            }
        }
        return null;
    }

    @Override
    public boolean validate(UserIdentity user) {
        return true;
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    @Override
    public void setIdentityService(IdentityService service) {
        identityService = service;
    }

    @Override
    public void logout(UserIdentity user) {
        // empty as nothing to do here
    }

    public static class UserCredentials {
        private final String token;
        private final Set<String> roles;

        public UserCredentials( String token, Set<String> roles) {
            this.token = token;
            this.roles = roles;
        }

        public String getToken() {
            return token;
        }

        public Set<String> getRoles() {
            return roles;
        }
    }
}
