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

import java.security.Principal;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;


/**
 * @version $Rev$ $Date$
 */
public class JAASJettyRealm implements UserRealm {

    private final String webRealmName;
    private final InternalJAASJettyRealm internalJAASJettyRealm;

    public JAASJettyRealm(String realmName, InternalJAASJettyRealm internalJAASJettyRealm) {
        this.webRealmName = realmName;
        this.internalJAASJettyRealm = internalJAASJettyRealm;
    }

    public String getName() {
        return webRealmName;
    }

    public Principal getPrincipal(String username) {
        return internalJAASJettyRealm.getPrincipal(username);
    }

    public Principal authenticate(String username, Object credentials, Request request) {
        return internalJAASJettyRealm.authenticate(username, credentials, request);
    }

    public boolean reauthenticate(Principal user) {
        return internalJAASJettyRealm.reauthenticate(user);
    }

    public boolean isUserInRole(Principal user, String role) {
        return internalJAASJettyRealm.isUserInRole(user, role);
    }

    public void disassociate(Principal user) {
        internalJAASJettyRealm.disassociate(user);
    }

    public Principal pushRole(Principal user, String role) {
        return internalJAASJettyRealm.pushRole(user, role);
    }

    public Principal popRole(Principal user) {
        return internalJAASJettyRealm.popRole(user);
    }

    public void logout(Principal user) {
        internalJAASJettyRealm.logout(user);
    }

}
