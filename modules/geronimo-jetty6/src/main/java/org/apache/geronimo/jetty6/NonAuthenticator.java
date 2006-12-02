package org.apache.geronimo.jetty6;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import java.security.Principal;
import java.io.IOException;

/**
 * Authenticator that always denies, returning null.  Useful when you need to install a default principal/subject
 * in an unsecured web app.
 */
public class NonAuthenticator implements Authenticator {
    public Principal authenticate(UserRealm realm, String pathInContext, Request request, Response response) throws IOException {
        return null;
    }

    public String getAuthMethod() {
        return "None"; 
    }
}
