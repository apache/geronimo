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

import java.security.AccessControlException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.jacc.WebRoleRefPermission;

import org.eclipse.jetty.server.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoUserIdentityWrapper implements UserIdentity {

    private final UserIdentity delegate;
    private final UserIdentity.Scope scope;

    public GeronimoUserIdentityWrapper(UserIdentity delegate, Scope scope) {
        this.delegate = delegate;
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public Subject getSubject() {
        return delegate.getSubject();
    }

    public Principal getUserPrincipal() {
        return delegate.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        if (delegate instanceof GeronimoUserIdentity) {
            String servletName = scope.getName();
            if (servletName == null || servletName.equals("jsp")) {
                servletName = "";
            }
            try {
                ((GeronimoUserIdentity) delegate).getAccessControlContext().checkPermission(new WebRoleRefPermission(servletName, role));
                return true;
            } catch (AccessControlException e) {
                return false;
            }
        }
        //not right... should use default_acc
        return false;
    }

    public GeronimoUserIdentityWrapper newWrapper(Scope oldContext) {
        return new GeronimoUserIdentityWrapper(delegate, oldContext);
    }

}
