package org.apache.geronimo.tomcat.authenticator;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm;

/*
 * An Authenticator which utilizes HttpRequest to perform authentication. This authentication
 * is non-interactive and does not require any user intervention.
 */
public class GenericHeaderAuthenticator extends AuthenticatorBase {
   
    private static final String GENERIC_METHOD="GENERIC";
    protected boolean authenticate(Request request, Response response, LoginConfig config) throws IOException {
        HttpServletRequest httpRequest=request.getRequest(); 
        Principal principal = request.getUserPrincipal();
        if(context.getRealm() instanceof TomcatGeronimoRealm)
        principal =((TomcatGeronimoRealm)context.getRealm()).authenticate(httpRequest);
        if (principal != null) {
            register(request, response, principal, GENERIC_METHOD,
                     null, null);
            return (true);
        }
        else
            response.setStatus(401);
        return false;
    }
}
