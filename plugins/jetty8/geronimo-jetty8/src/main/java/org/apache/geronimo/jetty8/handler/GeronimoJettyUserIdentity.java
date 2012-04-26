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


package org.apache.geronimo.jetty8.handler;

import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.jacc.WebRoleRefPermission;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaspi.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoJettyUserIdentity implements org.eclipse.jetty.server.UserIdentity {

    private final UserIdentity userIdentity;

    public GeronimoJettyUserIdentity(UserIdentity userIdentity) {
        this.userIdentity = userIdentity;
    }

    @Override
    public Subject getSubject() {
        return userIdentity.getSubject();
    }

    @Override
    public Principal getUserPrincipal() {
        return userIdentity.getUserPrincipal();
    }

    public boolean isUserInRole(String role, Scope scope) {

        String servletName = scope.getName();
        if (servletName == null || servletName.equals("jsp")) {
            servletName = "";
        }
        try {
            //correct run-as identity available from context manager.
            AccessControlContext acc = ContextManager.getCurrentContext();
            acc.checkPermission(new WebRoleRefPermission(servletName, role));
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "GeronimoJettyUserIdentity[Subject: " + getSubject() + ", Principal: " + getUserPrincipal() + "]";
    }

    public AccessControlContext getAccessControlContext() {
        return userIdentity.getAccessControlContext();
    }
}
