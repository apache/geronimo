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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.security.ContextManager;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.Server;


/**
 * @version $Rev$ $Date$
 */
public class JettyServer extends Server {
    private final static ThreadLocal currentWebAppContext = new ThreadLocal();
    private final Map realmDelegates = new HashMap();

    public UserRealm addRealm(UserRealm realm) {
        RealmDelegate delegate = (RealmDelegate) realmDelegates.get(realm.getName());
        if (delegate == null) {
            delegate = new RealmDelegate(realm.getName());
            realmDelegates.put(realm.getName(), delegate);
        }
        delegate.delegate = realm;

        return delegate;
    }

    public UserRealm getRealm(String realmName) {
        RealmDelegate delegate = (RealmDelegate) realmDelegates.get(realmName);

        if (delegate == null) {
            delegate = new RealmDelegate(realmName);
            realmDelegates.put(realmName, delegate);
        }
        return delegate;
    }

    public void removeRealm(UserRealm realm) {
        realmDelegates.remove(realm.getName());
    }

    public static void setCurrentWebAppContext(JettyWebAppJACCContext context) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ContextManager.SET_CONTEXT);

        currentWebAppContext.set(context);
    }

    public static JettyWebAppJACCContext getCurrentWebAppContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ContextManager.GET_CONTEXT);

        return (JettyWebAppJACCContext) currentWebAppContext.get();
    }

    private class RealmDelegate implements UserRealm {

        private UserRealm delegate;
        private final String name;

        private RealmDelegate(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Principal getPrincipal(String username) {
            return delegate.getPrincipal(username);
        }

        public Principal authenticate(String username, Object credentials, HttpRequest request) {
            return delegate.authenticate(username, credentials, request);
        }

        public boolean reauthenticate(Principal user) {
            return delegate.reauthenticate(user);
        }

        public boolean isUserInRole(Principal user, String role) {
            return delegate.isUserInRole(user, role);
        }

        public void disassociate(Principal user) {
            delegate.disassociate(user);
        }

        public Principal pushRole(Principal user, String role) {
            return delegate.pushRole(user, role);
        }

        public Principal popRole(Principal user) {
            return delegate.popRole(user);
        }

        public void logout(Principal user) {
            delegate.logout(user);
        }
    }
}
