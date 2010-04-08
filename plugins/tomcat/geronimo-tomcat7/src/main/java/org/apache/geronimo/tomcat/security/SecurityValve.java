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

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * @version $Rev$ $Date$
 */
public class SecurityValve extends ValveBase {

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
            AuthResult authResult = authenticator.validateRequest(request, response, isAuthMandatory);

            TomcatAuthStatus authStatus = authResult.getAuthStatus();

            if (authStatus == TomcatAuthStatus.FAILURE) {
                return;
            } else if (authStatus == TomcatAuthStatus.SEND_CONTINUE) {
                return;
            } else if (authStatus == TomcatAuthStatus.SEND_FAILURE) {
                return;
            } else if (authStatus == TomcatAuthStatus.SEND_SUCCESS) {
                return;
            } else if (authStatus == TomcatAuthStatus.SUCCESS) {
                request.setAuthType(authenticator.getAuthType());
                UserIdentity userIdentity = authResult.getUserIdentity();
                Principal principal = userIdentity == null? null: userIdentity.getUserPrincipal();
                request.setUserPrincipal(principal);
                if (isAuthMandatory) {
                    if (!authorizer.hasResourcePermissions(request, authResult, constraints, userIdentity)) {
                        if (!response.isError()) {
                            response.sendError(Response.SC_FORBIDDEN);
                        }
                        return;
                    }
                }
                Object previous = identityService.associate(userIdentity);
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
}
