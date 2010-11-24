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
package org.apache.geronimo.tomcat.authenticator;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.Base64;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;

/**
 * A custom authenticator which provides Spnego Login capabilities in Geronimo.
 * In web.xml use the <auth-method>SPNEGO</auth-method> to invoke this
 * authenticator.
 * 
 */
public class SpnegoAuthenticator extends AuthenticatorBase {

    private static Log log = LogFactory.getLog(SpnegoAuthenticator.class);

    private static final String SPNEGO_METHOD = "SPNEGO";

    /**
     * Authenticate bytes.
     */
    public static final byte[] AUTHENTICATE_BYTES = { (byte) 'W', (byte) 'W', (byte) 'W', (byte) '-', (byte) 'A',
            (byte) 'u', (byte) 't', (byte) 'h', (byte) 'e', (byte) 'n', (byte) 't', (byte) 'i', (byte) 'c', (byte) 'a',
            (byte) 't', (byte) 'e' };

    @Override
    protected boolean authenticate(Request request, Response response, LoginConfig config) throws IOException {
        HttpServletResponse httpResponse = response.getResponse();
        HttpServletRequest httpRequest = request.getRequest();
        String header = httpRequest.getHeader("Authorization");
        if (header == null) {
            httpResponse.setHeader("WWW-Authenticate", "Negotiate");
            httpResponse.setStatus(401);
            return (false);
        } else if (header != null && header.startsWith("Negotiate")) {
            Principal principal = request.getUserPrincipal();
            String username = header.substring(10);
            String password = null;
            principal = context.getRealm().authenticate(username, password);
            if (principal != null) {
                register(request, response, principal, SPNEGO_METHOD, username, password);
                return (true);
            } else
                request.getCoyoteRequest().getMimeHeaders().removeHeader("authorization");
        }

        // Validate any credentials already included with this request
        String username = null;
        String password = null;
        Principal principal = request.getUserPrincipal();
        MessageBytes authorization = request.getCoyoteRequest().getMimeHeaders().getValue("authorization");
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
                    password = new String(buf, colon + 1, authorizationCC.getEnd() - colon - 1);
                }

                authorizationBC.setOffset(authorizationBC.getOffset() - 6);
            }
            principal = context.getRealm().authenticate(username, password);
            if (principal != null) {
                register(request, response, principal, SPNEGO_METHOD, username, password);
                return (true);
            }
        }

        // Send an "unauthorized" response and an appropriate challenge
        MessageBytes authenticate = response.getCoyoteResponse().getMimeHeaders().addValue(AUTHENTICATE_BYTES, 0,
                AUTHENTICATE_BYTES.length);
        CharChunk authenticateCC = authenticate.getCharChunk();
        authenticateCC.append("Basic realm=\"");
        if (config.getRealmName() == null) {
            authenticateCC.append(request.getServerName());
            authenticateCC.append(':');
            authenticateCC.append(Integer.toString(request.getServerPort()));
        } else {
            authenticateCC.append(config.getRealmName());
        }
        authenticateCC.append('\"');
        authenticate.toChars();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        // response.flushBuffer();
        return (false);
    }
}