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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class NoneAuthenticator implements Authenticator {

    private final AuthResult unauthenticated;

    public NoneAuthenticator(UserIdentity unauthenticatedIdentity) {
        unauthenticated = new AuthResult(TomcatAuthStatus.SUCCESS, unauthenticatedIdentity, false);
    }

    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory, UserIdentity cachedIdentity) throws ServerAuthException {
        return unauthenticated;
    }

    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return true;
    }

    public String getAuthType() {
        return "NONE";
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        return unauthenticated;
    }

    @Override
    public void logout(Request request) {
    }

}
