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


package org.apache.geronimo.tomcat.security.impl;

import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;

import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.geronimo.tomcat.security.IdentityService;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.realm.providers.CertificateChainCallbackHandler;
import org.apache.geronimo.security.realm.providers.PasswordCallbackHandler;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoLoginService implements LoginService {

    private final ConfigurationFactory configurationFactory;
    private final IdentityService identityService;

    public GeronimoLoginService(ConfigurationFactory configurationFactory, IdentityService identityService) {
        this.configurationFactory = configurationFactory;
        this.identityService = identityService;
    }

    public UserIdentity login(String userName, String password) {
        return login(new PasswordCallbackHandler(userName, (password == null) ? null : password.toCharArray()));
    }

    public UserIdentity login(X509Certificate[] certs) {
        return login(new CertificateChainCallbackHandler(certs));
    }
    
    public UserIdentity login(CallbackHandler callbackHandler) {
        try {
            LoginContext loginContext = ContextManager.login(configurationFactory.getConfigurationName(), callbackHandler, configurationFactory.getConfiguration());
            Subject establishedSubject = loginContext.getSubject();
            Principal userPrincipal = ContextManager.getCurrentPrincipal(establishedSubject);
            return identityService.newUserIdentity(establishedSubject, userPrincipal, null);
        } catch (LoginException e) {
            return null;
        }
    }
    
    public void logout(UserIdentity userIdentity) {
    }
}
