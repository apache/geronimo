/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty;

import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.HashMap;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.WebRoleRefPermission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.jetty.interceptor.SecurityContextBeforeAfter;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;


/**
 * @version $Rev$ $Date$
 */
public class JAASJettyRealm implements UserRealm {
    private static Log log = LogFactory.getLog(JAASJettyRealm.class);

    private final String realmName;
    private final String loginDomainName;
    private final HashMap userMap = new HashMap();

    public JAASJettyRealm(String realmName, String loginDomainName) {
        this.realmName = realmName;
        this.loginDomainName = loginDomainName;
    }

    public String getName() {
        return realmName;
    }

    public Principal getPrincipal(String username) {
        return (Principal) userMap.get(username);
    }

    public Principal authenticate(String username, Object credentials, HttpRequest request) {
        try {
            JAASJettyPrincipal userPrincipal = (JAASJettyPrincipal) userMap.get(username);

            //user has been previously authenticated, but
            //re-authentication has been requested, so remove them
            if (userPrincipal != null) {
                userMap.remove(username);
            }


            char[] password;
            if (credentials instanceof char[]) {
                password = (char[]) credentials;
            } else if (credentials instanceof String) {
                password = ((String) credentials).toCharArray();
            } else {
                throw new LoginException("Cannot extract credentials from class: " + credentials.getClass().getName());
            }
            PasswordCallbackHandler callbackHandler = new PasswordCallbackHandler(username, password);

            //set up the login context
            LoginContext loginContext = new LoginContext(loginDomainName, callbackHandler);
            loginContext.login();
            callbackHandler.clear();

            Subject subject = ContextManager.getServerSideSubject(loginContext.getSubject());
            ContextManager.setCurrentCaller(subject);

            //login success
            userPrincipal = new JAASJettyPrincipal(username);
            userPrincipal.setSubject(subject);

            userMap.put(username, userPrincipal);

            return userPrincipal;
        } catch (LoginException e) {
            log.warn(e);
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

        ContextManager.setCurrentCaller(((JAASJettyPrincipal) user).getSubject());

        // get the user out of the cache
        return (userMap.get(user.getName()) != null);
    }

    public void disassociate(Principal user) {
        // do nothing
    }

    public boolean isUserInRole(Principal user, String role) {
        AccessControlContext acc = ContextManager.getCurrentContext();
        try {
            acc.checkPermission(new WebRoleRefPermission(JettyServletHolder.getJettyServletHolder().getName(), role));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    public Principal pushRole(Principal user, String role) {
        ((JAASJettyPrincipal) user).push(ContextManager.getCurrentCaller());
        ContextManager.setCurrentCaller(SecurityContextBeforeAfter.getCurrentRoleDesignate(role));
        return user;
    }

    public Principal popRole(Principal user) {
        ContextManager.setCurrentCaller(((JAASJettyPrincipal) user).pop());
        return user;
    }

}
