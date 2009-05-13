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

import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;

import javax.security.jacc.WebRoleRefPermission;
import javax.security.auth.Subject;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.security.RunAsToken;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoUserIdentity implements UserIdentity {

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
        return userPrincipal;
    }

    public String[] getRoles() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isUserInRole(String role) {
        throw new RuntimeException("Not implemented");
    }

    //jaspi called from FormAuthenticator.valueUnbound (when session is unbound)
    //TODO usable???
    public void logout(Principal user) {
    }

    public AccessControlContext getAccessControlContext() {
        return acc;
    }

//    public RunAsToken getOldRunAsToken() {
//        throw new RuntimeException("Not implemented");
//    }
//
//    public void setOldRunAsToken(RunAsToken oldRunAsToken) {
//        throw new RuntimeException("Not implemented");
//    }
//
//    private static class NamedUserIdentity extends GeronimoUserIdentity {
//        private final String servletName;
//        private RunAsToken oldRunAsToken;
//
//        private NamedUserIdentity(GeronimoUserIdentity gui, String servletName) {
//            super(gui.subject, gui.userPrincipal, gui.acc);
//            // JACC v1.0 section B.19
//            if (servletName == null || servletName.equals("jsp")) {
//                servletName = "";
//            }
//            this.servletName = servletName;
//        }
//
//        public boolean isUserInRole(String role) {
//            if (servletName == null || servletName.equals("jsp")) {
//                servletName = "";
//            }
//            try {
//                getAccessControlContext().checkPermission(new WebRoleRefPermission(servletName, role));
//                return true;
//            } catch (AccessControlException e) {
//                return false;
//            }
//        }
//
//        public RunAsToken getOldRunAsToken() {
//            return oldRunAsToken;
//        }
//
//        public void setOldRunAsToken(RunAsToken oldRunAsToken) {
//            this.oldRunAsToken = oldRunAsToken;
//        }
//    }


}
