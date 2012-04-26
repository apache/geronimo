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


package org.apache.geronimo.jetty8.security.auth;

import javax.security.auth.callback.CallbackHandler;

import org.apache.geronimo.jetty8.handler.GeronimoJettyUserIdentity;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jaspi.IdentityService;
import org.apache.geronimo.security.jaspi.impl.GeronimoLoginService;
import org.apache.geronimo.security.realm.providers.PasswordCallbackHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class JAASLoginService implements LoginService {
    private final String realmName;

    private final GeronimoLoginService geronimoLoginService;

    /**
     * Construct a JAASLoginService
     * @param realmName may be null e.g. for jaspi.
     * @param geronimoLoginService
     */
    public JAASLoginService(String realmName, GeronimoLoginService geronimoLoginService) {
        this.realmName = realmName;
        this.geronimoLoginService = geronimoLoginService;
    }

    public void logout(UserIdentity userIdentity) {
        //not sure how to do this
    }

    public String getName() {
        return realmName;
    }

    public UserIdentity login(String username, Object credentials) {
        char[] password = credentials instanceof  String? ((String)credentials).toCharArray(): (char[]) credentials;
        CallbackHandler callbackHandler = new PasswordCallbackHandler(username, password);
        org.apache.geronimo.security.jaspi.UserIdentity userIdentity = geronimoLoginService.login(callbackHandler);
        return new GeronimoJettyUserIdentity(userIdentity);
    }

    public boolean validate(UserIdentity user) {
        return true;
    }

    public org.eclipse.jetty.security.IdentityService getIdentityService() {
        return null;
    }

    public void setIdentityService(org.eclipse.jetty.security.IdentityService identityService) {
    }
}
