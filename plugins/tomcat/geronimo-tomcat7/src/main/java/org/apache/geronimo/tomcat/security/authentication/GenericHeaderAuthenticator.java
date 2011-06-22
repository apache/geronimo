package org.apache.geronimo.tomcat.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.security.realm.providers.RequestCallbackHandler;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;

public class GenericHeaderAuthenticator implements Authenticator {

    private static final String GENERIC_AUTH = "GENERIC";

    private final LoginService loginService;
    private final UserIdentity unauthenticatedIdentity;

    public GenericHeaderAuthenticator(LoginService loginService, UserIdentity unauthenticatedIdentity) {
        this.loginService = loginService;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
    }

    @Override
    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory,
            UserIdentity cachedIdentity) throws ServerAuthException {
        try {
            HttpServletRequest httpRequest = request.getRequest();
            UserIdentity userIdentity = loginService.login(new RequestCallbackHandler(httpRequest));
            if (userIdentity != null) {
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, false);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return new AuthResult(TomcatAuthStatus.FAILURE, unauthenticatedIdentity, false);
            }
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
    }

    @Override
    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return false;
    }

    @Override
    public String getAuthType() {
        return GENERIC_AUTH;
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        UserIdentity userIdentity = loginService.login(username, password);
        if (userIdentity != null) {
            return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, true);
        }
        return new AuthResult(TomcatAuthStatus.FAILURE, null, false);
    }

    @Override
    public void logout(Request request) throws ServletException {
    }

}
package org.apache.geronimo.tomcat.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.security.realm.providers.RequestCallbackHandler;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;

public class GenericHeaderAuthenticator implements Authenticator {

    private static final String GENERIC_AUTH = "GENERIC";

    private final LoginService loginService;
    private final UserIdentity unauthenticatedIdentity;

    public GenericHeaderAuthenticator(LoginService loginService, UserIdentity unauthenticatedIdentity) {
        this.loginService = loginService;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
    }

    @Override
    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory,
            UserIdentity cachedIdentity) throws ServerAuthException {
        try {
            HttpServletRequest httpRequest = request.getRequest();
            UserIdentity userIdentity = loginService.login(new RequestCallbackHandler(httpRequest));
            if (userIdentity != null) {
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, false);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return new AuthResult(TomcatAuthStatus.FAILURE, unauthenticatedIdentity, false);
            }
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
    }

    @Override
    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return false;
    }

    @Override
    public String getAuthType() {
        return GENERIC_AUTH;
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        UserIdentity userIdentity = loginService.login(username, password);
        if (userIdentity != null) {
            return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, true);
        }
        return new AuthResult(TomcatAuthStatus.FAILURE, null, false);
    }

    @Override
    public void logout(Request request) throws ServletException {
    }

}
package org.apache.geronimo.tomcat.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.security.realm.providers.RequestCallbackHandler;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;

public class GenericHeaderAuthenticator implements Authenticator {

    private static final String GENERIC_AUTH = "GENERIC";

    private final LoginService loginService;
    private final UserIdentity unauthenticatedIdentity;

    public GenericHeaderAuthenticator(LoginService loginService, UserIdentity unauthenticatedIdentity) {
        this.loginService = loginService;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
    }

    @Override
    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory,
            UserIdentity cachedIdentity) throws ServerAuthException {
        try {
            HttpServletRequest httpRequest = request.getRequest();
            UserIdentity userIdentity = loginService.login(new RequestCallbackHandler(httpRequest));
            if (userIdentity != null) {
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, false);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return new AuthResult(TomcatAuthStatus.FAILURE, unauthenticatedIdentity, false);
            }
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
    }

    @Override
    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return false;
    }

    @Override
    public String getAuthType() {
        return GENERIC_AUTH;
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        UserIdentity userIdentity = loginService.login(username, password);
        if (userIdentity != null) {
            return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, true);
        }
        return new AuthResult(TomcatAuthStatus.FAILURE, null, false);
    }

    @Override
    public void logout(Request request) throws ServletException {
    }

}
