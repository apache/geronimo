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


package org.apache.geronimo.tomcat.security.jacc;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.security.jacc.WebRoleRefPermission;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.geronimo.security.ContextManager;
import org.ietf.jgss.GSSContext;

/**
 * @version $Rev$ $Date$
 */
public class JACCRealm implements Realm {

    public static final JACCRealm INSTANCE = new JACCRealm();

    private static final ThreadLocal<String> currentRequestWrapperName = new ThreadLocal<String>();

    public static String setRequestWrapperName(String requestWrapperName) {
        String old = currentRequestWrapperName.get();
        currentRequestWrapperName.set(requestWrapperName);
        return old;
    }

    /*@Override
    @Deprecated
    public boolean hasRole(Principal principal, String role) {
        AccessControlContext acc = ContextManager.getCurrentContext();
        String name = currentRequestWrapperName.get();

        *//**
         * JACC v1.0 secion B.19
         *//*
        if (name == null || name.equals("jsp")) {
            name = "";
        }
        try {
            acc.checkPermission(new WebRoleRefPermission(name, role));
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }*/

    @Override
    public boolean hasRole(Wrapper wrapper, Principal principal, String role) {
        AccessControlContext acc = ContextManager.getCurrentContext();
        String name = currentRequestWrapperName.get();

        /**
         * JACC v1.0 secion B.19
         */
        if (name == null || name.equals("jsp")) {
            name = "";
        }
        try {
            acc.checkPermission(new WebRoleRefPermission(name, role));
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }

    @Override
    public Container getContainer() {
        return null;
    }

    @Override
    public void setContainer(Container container) {
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public Principal authenticate(String username, String credentials) {
        return null;
    }

    @Override
    public Principal authenticate(String username, String digest, String nonce, String nc, String cnonce, String qop, String realm, String md5a2) {
        return null;
    }

    @Override
    public Principal authenticate(X509Certificate[] certs) {
        return null;
    }

    @Override
    public void backgroundProcess() {
    }

    @Override
    public SecurityConstraint[] findSecurityConstraints(Request request, Context context) {
        return new SecurityConstraint[0];
    }

    @Override
    public boolean hasResourcePermission(Request request, Response response, SecurityConstraint[] constraint, Context context) throws IOException {
        return false;
    }

    @Override
    public boolean hasUserDataPermission(Request request, Response response, SecurityConstraint[] constraint) throws IOException {
        return false;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public Principal authenticate(GSSContext gssContext, boolean storeCreds) {
        return null;
    }
}
