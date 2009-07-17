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


package org.apache.geronimo.tomcat;

import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.ServerAuthConfig;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.Authorizer;
import org.apache.geronimo.tomcat.security.IdentityService;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.SecurityValve;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.geronimo.tomcat.security.authentication.BasicAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.ClientCertAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.DigestAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.FormAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.NoneAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.jaspic.JaspicAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.jaspic.JaspicCallbackHandler;
import org.apache.geronimo.tomcat.security.impl.GeronimoIdentityService;
import org.apache.geronimo.tomcat.security.impl.GeronimoLoginService;
import org.apache.geronimo.tomcat.security.jacc.JACCAuthorizer;
import org.apache.geronimo.tomcat.security.jacc.JACCRealm;
import org.apache.geronimo.tomcat.security.jacc.JACCSecurityValve;

/**
 * @version $Rev$ $Date$
 */
public class BaseGeronimoContextConfig extends ContextConfig {
    private static final String MESSAGE_LAYER = "HttpServlet";


    protected void configureSecurity(StandardContext geronimoContext, String policyContextId, ConfigurationFactory configurationFactory, Subject defaultSubject, String authMethod, String realmName, String loginPage, String errorPage) {
        if (defaultSubject == null) {
            defaultSubject = ContextManager.EMPTY;
        }
        IdentityService identityService = new GeronimoIdentityService(defaultSubject);
        UserIdentity unauthenticatedIdentity = identityService.newUserIdentity(defaultSubject, null, null);
        LoginService loginService = new GeronimoLoginService(configurationFactory, identityService);
        Authenticator authenticator = null;
        AuthConfigFactory authConfigFactory = AuthConfigFactory.getFactory();
        RegistrationListener listener = new RegistrationListener() {

            public void notify(String layer, String appContext) {
            }
        };
        //?? TODO is context.getPath() the context root?
        String appContext = "server " + geronimoContext.getPath();
        AuthConfigProvider authConfigProvider = authConfigFactory.getConfigProvider(MESSAGE_LAYER, appContext, listener);
        ServerAuthConfig serverAuthConfig = null;
        JaspicCallbackHandler callbackHandler = null;
        if (authConfigProvider != null) {
            callbackHandler = new JaspicCallbackHandler(loginService);
            try {
                serverAuthConfig = authConfigProvider.getServerAuthConfig(MESSAGE_LAYER, appContext, callbackHandler);
            } catch (AuthException e) {
                //TODO log exception?  rethrow????
            }
        }
        if (serverAuthConfig != null) {
            Map authProperties = new HashMap();
            Subject serviceSubject = new Subject();
            authenticator = new JaspicAuthenticator(serverAuthConfig, authProperties, serviceSubject, callbackHandler, identityService);
        } else if ("BASIC".equalsIgnoreCase(authMethod)) {
            authenticator = new BasicAuthenticator(loginService, realmName, unauthenticatedIdentity);
        } else if ("CLIENT-CERT".equalsIgnoreCase(authMethod)) {
            authenticator = new ClientCertAuthenticator(loginService, unauthenticatedIdentity);
        } else if ("DIGEST".equalsIgnoreCase(authMethod)) {
            authenticator = new DigestAuthenticator(loginService, realmName, unauthenticatedIdentity);
        } else if ("FORM".equalsIgnoreCase(authMethod)) {
            authenticator = new FormAuthenticator(loginService, unauthenticatedIdentity, loginPage, errorPage);
        } else  if ("NONE".equalsIgnoreCase(authMethod)) {
            authenticator = new NoneAuthenticator(unauthenticatedIdentity);
        }
        if (authenticator == null) {
            throw new IllegalStateException("No authenticator configured");
        }

        AccessControlContext defaultAcc = ContextManager.registerSubjectShort(defaultSubject,  null, null);
        Authorizer authorizer = new JACCAuthorizer(defaultAcc);

        SecurityValve securityValve = new JACCSecurityValve(authenticator, authorizer, identityService, policyContextId);

        geronimoContext.addValve(securityValve);
        if (log.isDebugEnabled()) {
            log.debug(sm.getString(
                    "contextConfig.authenticatorConfigured",
                    authMethod));
        }

        geronimoContext.setRealm(new JACCRealm());
    }
}
