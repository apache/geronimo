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
