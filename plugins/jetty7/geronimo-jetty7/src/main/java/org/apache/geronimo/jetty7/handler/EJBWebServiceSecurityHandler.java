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


package org.apache.geronimo.jetty7.handler;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.Permission;

import javax.security.jacc.WebUserDataPermission;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class EJBWebServiceSecurityHandler extends SecurityHandler {

    private final Permission permission;

    public EJBWebServiceSecurityHandler(
            Authenticator authenticator,
            final LoginService loginService,
            IdentityService identityService,
            Permission permission) {
        setAuthenticator(authenticator);

        loginService.setIdentityService(identityService);
        setLoginService(loginService);
        setIdentityService(identityService);
        this.permission = permission;
    }

    protected Object prepareConstraintInfo(String pathInContext, Request request) {
        return null;
    }

    protected boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException {
        return permission.implies(new WebUserDataPermission(request));
    }

    protected boolean isAuthMandatory(Request baseRequest, Response base_response, Object constraintInfo) {
        //TODO we were given a list of protected methods, but how to we figure out what the method is?
        return true;
    }

    protected boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo, UserIdentity userIdentity) throws IOException {
        return true;
    }
}
