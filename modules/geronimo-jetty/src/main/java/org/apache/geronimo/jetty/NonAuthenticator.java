package org.apache.geronimo.jetty;

import java.io.IOException;
import java.security.Principal;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.UserRealm;

/**
 * Authenticator that always denies, returning null.  Useful when you need to install a default principal/subject
 * in an unsecured web app.
 */
public class NonAuthenticator implements Authenticator {
    public Principal authenticate(UserRealm realm, String pathInContext, HttpRequest request, HttpResponse response) throws IOException {
        return null;
    }

    public String getAuthMethod() {
        return "None"; 
    }
}
