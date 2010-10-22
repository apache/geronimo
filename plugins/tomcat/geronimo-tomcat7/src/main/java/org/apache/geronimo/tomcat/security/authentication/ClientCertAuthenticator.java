/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.tomcat.security.authentication;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.coyote.ActionCode;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.tomcat.util.res.StringManager;

/**
 * @version $Rev$ $Date$
 */
public class ClientCertAuthenticator implements Authenticator {

    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    private final LoginService loginService;
    private final UserIdentity unauthenticatedIdentity;

    public ClientCertAuthenticator(LoginService loginService, UserIdentity unauthenticatedIdentity) {
        this.loginService = loginService;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
    }

    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory, UserIdentity cachedIdentity) throws ServerAuthException {
        X509Certificate certs[] = (X509Certificate[])
            request.getAttribute(Globals.CERTIFICATES_ATTR);
        if ((certs == null) || (certs.length < 1)) {
            request.getCoyoteRequest().action
                              (ActionCode.REQ_SSL_CERTIFICATE, null);
            certs = (X509Certificate[])
                request.getAttribute(Globals.CERTIFICATES_ATTR);
        }
        try {
            if ((certs == null) || (certs.length < 1)) {
                if (isAuthMandatory) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                   sm.getString("authenticator.certificates"));
                    return new AuthResult(TomcatAuthStatus.SEND_FAILURE, null, false);
                } else {
                    return new AuthResult(TomcatAuthStatus.SUCCESS, unauthenticatedIdentity, false);
                }
            }

            // Authenticate the specified certificate chain
            UserIdentity userIdentity = loginService.login(certs);
            if (userIdentity != null) {
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, true);
            }
            if (isAuthMandatory) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                   sm.getString("authenticator.unauthorized"));
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, null, false);
            }
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }
        return new AuthResult(TomcatAuthStatus.SUCCESS, unauthenticatedIdentity, false);
    }

    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return true;
    }

    public String getAuthType() {
        return HttpServletRequest.CLIENT_CERT_AUTH;
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
    public void logout(Request request) {
    }
    
}
