/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.tomcat.security.authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.Base64;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;

/**
 * A custom authenticator which provides Spnego Login capabilities in Geronimo. In web.xml use the
 * <auth-method>SPNEGO</auth-method> to invoke this authenticator.
 * 
 */
public class SpnegoAuthenticator implements Authenticator {

    private static final String SPNEGO_AUTH = "SPNEGO";

    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    private final LoginService loginService;

    private final String realmName;

    private final UserIdentity unauthenticatedIdentity;

    public SpnegoAuthenticator(LoginService loginService, String realmName, UserIdentity unauthenticatedIdentity) {
        this.loginService = loginService;
        this.realmName = realmName;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
    }

    @Override
    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory,
            UserIdentity cachedIdentity) throws ServerAuthException {
        // FIXME: Is the logics of basic authorization necessary?

        MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue("authorization");

        // Send an "unauthorized" response and an appropriate challenge (SPNEGO)
        if (authorization == null) {
            if (isAuthMandatory) {
                response.addHeader(WWW_AUTHENTICATE, "Negotiate");
                try {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } catch (IOException e) {
                    throw new ServerAuthException(e);
                }
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, null, false);
            }
            return new AuthResult(TomcatAuthStatus.SUCCESS, unauthenticatedIdentity, false);
        }

        // Validate any credentials already included with this request
        String username = null;
        String password = null;

        authorization.toBytes();
        ByteChunk authorizationBC = authorization.getByteChunk();
        if (authorizationBC.startsWithIgnoreCase("basic ", 0)) { // Basic authorization
            authorizationBC.setOffset(authorizationBC.getOffset() + 6);
            // FIXME: Add trimming
            // authorizationBC.trim();

            CharChunk authorizationCC = authorization.getCharChunk();
            Base64.decode(authorizationBC, authorizationCC);

            // Get username and password
            int colon = authorizationCC.indexOf(':');
            if (colon < 0) {
                username = authorizationCC.toString();
            } else {
                char[] buf = authorizationCC.getBuffer();
                username = new String(buf, 0, colon);
                password = new String(buf, colon + 1, authorizationCC.getEnd() - colon - 1);
            }

            authorizationBC.setOffset(authorizationBC.getOffset() - 6);
        } else if (authorizationBC.startsWithIgnoreCase("negotiate ", 0)) { // Spnego authorization
            authorizationBC.setOffset(authorizationBC.getOffset() + 10);
            username = authorizationBC.toString();
            authorizationBC.setOffset(authorizationBC.getOffset() - 10);
        }

        UserIdentity userIdentity = loginService.login(username, password);
        if (userIdentity != null) {
            return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, false);
        }

        // Send an "unauthorized" response and an appropriate challenge (BASIC)
        if (isAuthMandatory) {
            try {
                StringBuilder authenticateCC = new StringBuilder();
                authenticateCC.append("Basic realm=\"");
                if (realmName == null) {
                    authenticateCC.append(request.getServerName());
                    authenticateCC.append(':');
                    authenticateCC.append(Integer.toString(request.getServerPort()));
                } else {
                    authenticateCC.append(realmName);
                }
                authenticateCC.append('\"');
                response.addHeader(WWW_AUTHENTICATE, authenticateCC.toString());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, null, false);
            } catch (IOException e) {
                throw new ServerAuthException(e);
            }
        }

        return new AuthResult(TomcatAuthStatus.SUCCESS, unauthenticatedIdentity, false);
    }

    @Override
    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return true;
    }

    @Override
    public String getAuthType() {
        return SPNEGO_AUTH;
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        UserIdentity userIdentity = loginService.login(username, password);
        if (userIdentity != null) {
            return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, false);
        }
        return new AuthResult(TomcatAuthStatus.FAILURE, null, false);
    }

    @Override
    public void logout(Request request) throws ServletException {
    }

}
