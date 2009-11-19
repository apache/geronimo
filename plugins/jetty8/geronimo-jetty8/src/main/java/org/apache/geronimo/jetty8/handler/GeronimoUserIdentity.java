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

import javax.security.jacc.WebRoleRefPermission;
import javax.security.auth.Subject;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.security.RunAsToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoUserIdentity implements UserIdentity {
    private final Logger log = LoggerFactory.getLogger(GeronimoUserIdentity.class);

    private final Subject subject;
    private final Principal userPrincipal;
    private final AccessControlContext acc;
    private RunAsToken runAsToken;
    private ServletHolder servletHolder;

    public GeronimoUserIdentity(Subject subject, Principal userPrincipal, AccessControlContext acc) {
//        if ((subject == null) != (userPrincipal == null)) throw new IllegalArgumentException("both or neither of subject (" + subject + ") and userPrincipal (" + userPrincipal + ") must be null");
        if (acc == null) throw new NullPointerException("AccessControlContext acc required");
        this.subject = subject;
        this.userPrincipal = userPrincipal;
        this.acc = acc;
    }

    public Subject getSubject() {
        return subject;
    }

    public Principal getUserPrincipal() {
        //not clear whether this should reflect any run-as identity.  Currently it does not.
        return userPrincipal;
    }

    public String[] getRoles() {
        RuntimeException e = new RuntimeException("Not implemented");
        log.info("getRoles called on identity " + this, e);
        throw e;
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

    //jaspi called from FormAuthenticator.valueUnbound (when session is unbound)
    //TODO usable???
    public void logout(Principal user) {
    }

    public AccessControlContext getAccessControlContext() {
        return acc;
    }

    @Override
    public String toString() {
        return "GeronimoUserIdentity[Subject: " + subject + ", Principal: " + userPrincipal + ", acc: " + acc + "]";
    }
}
