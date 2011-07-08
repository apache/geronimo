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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
 * @version $Rev$ $Date$
 */
public class BasicAuthenticator implements Authenticator {
    private static final byte[] AUTHENTICATE_BYTES = {
        (byte) 'W',
        (byte) 'W',
        (byte) 'W',
        (byte) '-',
        (byte) 'A',
        (byte) 'u',
        (byte) 't',
        (byte) 'h',
        (byte) 'e',
        (byte) 'n',
        (byte) 't',
        (byte) 'i',
        (byte) 'c',
        (byte) 'a',
        (byte) 't',
        (byte) 'e'
    };


    private final LoginService loginService;
    private final String realmName;
    private final UserIdentity unauthenticatedIdentity;

    public BasicAuthenticator(LoginService loginService, String realmName, UserIdentity unauthenticatedIdentity) {
        this.loginService = loginService;
        this.realmName = realmName;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
    }

    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory, UserIdentity cachedIdentity) throws ServerAuthException {
        // Validate any credentials already included with this request
        String username = null;
        String password = null;

        MessageBytes authorization =
            request.getCoyoteRequest().getMimeHeaders()
            .getValue("authorization");

        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authorizationBC = authorization.getByteChunk();
            if (authorizationBC.startsWithIgnoreCase("basic ", 0)) {
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
                    password = new String(buf, colon + 1,
                            authorizationCC.getEnd() - colon - 1);
                }

                authorizationBC.setOffset(authorizationBC.getOffset() - 6);
            }

            UserIdentity userIdentity = loginService.login(username, password);
            if (userIdentity != null) {
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, false);
            }
        }


        // Send an "unauthorized" response and an appropriate challenge
        if (isAuthMandatory) {
            try {
                StringBuilder authenticateCC = new StringBuilder();
                authenticateCC.append("Basic realm=\"");
                authenticateCC.append((realmName == null) ? "<unspecified>" : realmName);
                authenticateCC.append('\"');
                response.addHeader(new String(AUTHENTICATE_BYTES), authenticateCC.toString());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, null, false);
            } catch (IOException e) {
                throw new ServerAuthException(e);
            }
        }
        return new AuthResult(TomcatAuthStatus.SUCCESS, unauthenticatedIdentity, false);
    }

    public boolean secureResponse(Request request, Response response, AuthResult authResult) {
        return true;
    }

    public String getAuthType() {
        return HttpServletRequest.BASIC_AUTH;
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
    public void logout(Request request) {
    }
}