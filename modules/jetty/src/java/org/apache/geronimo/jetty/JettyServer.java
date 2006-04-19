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

import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.Server;


/**
 * @version $Rev$ $Date$
 */
public class JettyServer extends Server {
    private final Map realmDelegates = new HashMap();

    public UserRealm addRealm(UserRealm realm) {
        RealmDelegate delegate = (RealmDelegate) getRealm(realm.getName());
        delegate.addDelegate(realm);
        return delegate.delegate;
    }

    public UserRealm getRealm(String realmName) {
        RealmDelegate delegate = (RealmDelegate) realmDelegates.get(realmName);

        if (delegate == null) {
            delegate = new RealmDelegate(realmName);
            realmDelegates.put(realmName, delegate);
        }
        return delegate;
    }

    public synchronized void removeRealm(UserRealm realm) {
        RealmDelegate delegate = (RealmDelegate) realmDelegates.get(realm.getName());
        if (delegate != null) {
            if (delegate.removeDelegate() == 0) {
                realmDelegates.remove(realm.getName());
            }
        }
    }

    private static class RealmDelegate implements UserRealm {

        private UserRealm delegate;
        private final String name;
        private int  count;

        private RealmDelegate(String name) {
            this.name = name;
        }

        private synchronized void addDelegate(UserRealm newDelegate) {
            if (delegate != null && !delegate.equals(newDelegate)) {
                throw new IllegalArgumentException("Inconsistent assigment of user realm: old: " + delegate + ", new: " + newDelegate);
            }
            if (delegate == null) {
                delegate = newDelegate;
            }
            count++;
        }

        private int removeDelegate() {
            return count--;
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
