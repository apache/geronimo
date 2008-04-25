/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty6;

import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.WebRoleRefPermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.realm.providers.CertificateCallbackHandler;
import org.apache.geronimo.security.realm.providers.ClearableCallbackHandler;
import org.apache.geronimo.security.realm.providers.PasswordCallbackHandler;
import org.mortbay.jetty.Request;


/**
 * @version $Rev$ $Date$
 */
public class InternalJAASJettyRealm {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String securityRealmName;
    private final HashMap<String, Principal> userMap = new HashMap<String, Principal>();
    private int count = 1;

    public InternalJAASJettyRealm(String geronimoRealmName) {
        this.securityRealmName = geronimoRealmName;
    }

    public String getSecurityRealmName() {
        return securityRealmName;
    }

    public Principal getPrincipal(String username) {
        return userMap.get(username);
    }

    public Principal authenticate(String username, Object credentials, Request request) {
        try {
            if ((username != null) && (!username.equals(""))) {

                JAASJettyPrincipal userPrincipal = (JAASJettyPrincipal) userMap.get(username);

                //user has been previously authenticated, but
                //re-authentication has been requested, so remove them
                if (userPrincipal != null) {
                    userMap.remove(username);
                }

                ClearableCallbackHandler callbackHandler;
                if (credentials instanceof char[]) {
                    char[] password = (char[]) credentials;
                    callbackHandler = new PasswordCallbackHandler(username, password);
                } else if (credentials instanceof String) {
                    char[] password = ((String) credentials).toCharArray();
                    callbackHandler = new PasswordCallbackHandler(username, password);
                } else if (credentials instanceof X509Certificate[]) {
                    X509Certificate[] certs = (X509Certificate[]) credentials;
                    if (certs.length < 1) {
                        throw new LoginException("no certificates supplied");
                    }
                    callbackHandler = new CertificateCallbackHandler(certs[0]);
                } else {
                    throw new LoginException("Cannot extract credentials from class: " + credentials.getClass().getName());
                }

                //set up the login context
                LoginContext loginContext = ContextManager.login(securityRealmName, callbackHandler);
                callbackHandler.clear();

                Subject subject = loginContext.getSubject();
                ContextManager.setCallers(subject, subject);

                //login success
                userPrincipal = new JAASJettyPrincipal(username);
                userPrincipal.setSubject(subject);

                userMap.put(username, userPrincipal);

                return userPrincipal;
            } else {
                log.debug("Login Failed - null userID");
                return null;
            }

        } catch (LoginException e) {
            log.debug("Login Failed", e);
            return null;
        }
    }

    public void logout(Principal user) {
        JAASJettyPrincipal principal = (JAASJettyPrincipal) user;

        userMap.remove(principal.getName());
        ContextManager.unregisterSubject(principal.getSubject());
    }

    public boolean reauthenticate(Principal user) {
        // TODO This is not correct if auth can expire! We need to

        Subject subject = ((JAASJettyPrincipal) user).getSubject();
        ContextManager.setCallers(subject, subject);

        // get the user out of the cache
        return (userMap.get(user.getName()) != null);
    }

    public void disassociate(Principal user) {
        // do nothing
    }

    public boolean isUserInRole(Principal user, String role) {
        if (user == null || role == null) {
            return false;
        }

        AccessControlContext acc = ContextManager.getCurrentContext();
        try {
            // JACC v1.0 secion B.19
            String servletName = InternalJettyServletHolder.getCurrentServletName();
            if (servletName == null || servletName.equals("jsp")) {
                servletName = "";
            }
            acc.checkPermission(new WebRoleRefPermission(servletName, role));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    public Principal pushRole(Principal user, String role) {
        //handled by JettyServletHolder and its runAsSubject
        return user;
    }

    public Principal popRole(Principal user) {
        return user;
    }

    public void addUse() {
        count++;
    }

    public int removeUse() {
        return count--;
    }

}
