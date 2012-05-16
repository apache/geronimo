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


package org.apache.geronimo.tomcat.security;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.valves.ValveBase;

/**
 * @version $Rev$ $Date$
 */
public class SecurityValve extends ValveBase implements org.apache.catalina.Authenticator {

    public final static String CACHED_IDENTITY_KEY = "org.apache.geronimo.jaspic.servlet.cachedIdentity";

    private final Authenticator authenticator;
    private final Authorizer authorizer;
    private final IdentityService identityService;

    public SecurityValve(Authenticator authenticator, Authorizer authorizer, IdentityService identityService) {
        super(true);
        this.authenticator = authenticator;
        this.authorizer = authorizer;
        this.identityService = identityService;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {

        Object constraints = authorizer.getConstraints(request);

        if (!authorizer.hasUserDataPermissions(request, constraints)) {
            //TODO redirect to secure port?
            if (!response.isError()) {
                response.sendError(Response.SC_FORBIDDEN);
            }
            return;
        }
        boolean isAuthMandatory = authorizer.isAuthMandatory(request, constraints);

        try {
            AuthResult authResult = authenticator.validateRequest(request, response, isAuthMandatory, getCachedIdentity(request));

            TomcatAuthStatus authStatus = authResult.getAuthStatus();

            if (authStatus == TomcatAuthStatus.FAILURE) {
            } else if (authStatus == TomcatAuthStatus.SEND_CONTINUE) {
                cacheIdentity(request, authResult);
            } else if (authStatus == TomcatAuthStatus.SEND_FAILURE) {
            } else if (authStatus == TomcatAuthStatus.SEND_SUCCESS) {
            } else if (authStatus == TomcatAuthStatus.SUCCESS) {
                Object previous = doSuccess(request, authResult);
                if (isAuthMandatory) {
                    if (!authorizer.hasResourcePermissions(request, authResult, constraints, authResult.getUserIdentity())) {
                        if (!response.isError()) {
                            response.sendError(Response.SC_FORBIDDEN);
                        }
                        return;
                    }
                }
                try {
                    getNext().invoke(request, response);
                } finally {
                    identityService.dissociate(previous);
                }
                //This returns a success code but I'm not sure what to do with it.
                authenticator.secureResponse(request, response, authResult);
            } else {
                //illegal state?
                throw new ServletException("unexpected auth status: " + authStatus);
            }
        } catch (ServerAuthException e) {
            throw new ServletException(e);
        }


    }

    private Object doSuccess(Request request, AuthResult authResult) {
        cacheIdentity(request, authResult);
        UserIdentity userIdentity = authResult.getUserIdentity();
        Principal principal = userIdentity == null? null: userIdentity.getUserPrincipal();
        if (principal != null) {
            request.setAuthType(authenticator.getAuthType());
            request.setUserPrincipal(principal);
        }
        return identityService.associate(userIdentity);
    }

    private void cacheIdentity(Request request, AuthResult authResult) {
        UserIdentity userIdentity = authResult.getUserIdentity();
        if (userIdentity != null && authResult.isContainerCaching()) {
            Session session = request.getSessionInternal(true);
            session.setNote(CACHED_IDENTITY_KEY, userIdentity);
        }
    }

    private UserIdentity getCachedIdentity(Request request) {
        Session session = request.getSessionInternal(false);
        return session == null? null: (UserIdentity)session.getNote(CACHED_IDENTITY_KEY);

    }
    
    @Override
    public boolean authenticate(Request request, HttpServletResponse response)
            throws IOException {        
        return authenticate(request, response, null);
    }

    @Override
    public boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws IOException {
        try {
            //this call is the user program requesting authentication,
            // so auth was not declaratively mandatory for this request, but is mandatory now.
            AuthResult authResult = authenticator.validateRequest(request, response, true, getCachedIdentity(request));
            TomcatAuthStatus authStatus = authResult.getAuthStatus();
            if (TomcatAuthStatus.SUCCESS.equals(authStatus)) {
                doSuccess(request, authResult);
                return true;
            }
            return false;
        } catch (ServerAuthException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }    

    @Override
    public void login(String username, String password, Request request) throws ServletException {
        AuthResult authResult = authenticator.login(username, password, request);
        TomcatAuthStatus authStatus = authResult.getAuthStatus();

        if (authStatus == TomcatAuthStatus.SUCCESS) {
            doSuccess(request, authResult);
        } else {
            throw new ServletException("Could not log in");
        }
    }

    @Override
    public void logout(Request request) throws ServletException {
        authenticator.logout(request);
        request.setUserPrincipal(null);
        Session session = request.getSessionInternal(false);
        if (session != null) {
            session.removeNote(CACHED_IDENTITY_KEY);
        }
        identityService.associate(null);
    }

}
