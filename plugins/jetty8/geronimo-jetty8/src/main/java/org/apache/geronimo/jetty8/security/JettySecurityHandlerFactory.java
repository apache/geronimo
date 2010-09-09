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


package org.apache.geronimo.jetty8.security;

import java.security.AccessControlContext;
import java.security.Permissions;

import javax.security.auth.Subject;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.jetty8.handler.JaccSecurityHandler;
import org.apache.geronimo.jetty8.handler.EJBWebServiceSecurityHandler;
import org.apache.geronimo.jetty8.security.auth.JAASLoginService;
import org.apache.geronimo.jetty8.security.auth.NoneAuthenticator;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.ClientCertAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.security.authentication.FormAuthenticator;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class JettySecurityHandlerFactory implements SecurityHandlerFactory {

    private final BuiltInAuthMethod authMethod;
    private final String loginPage;
    private final String errorPage;
    private final String realmName;
    private final ConfigurationFactory configurationFactory;

    public JettySecurityHandlerFactory(@ParamAttribute(name = "authMethod") BuiltInAuthMethod authMethod,
                                       @ParamAttribute(name = "loginPage") String loginPage,
                                       @ParamAttribute(name = "errorPage") String errorPage,
                                       @ParamAttribute(name = "realmName") String realmName,
                                       @ParamReference(name = "ConfigurationFactory") ConfigurationFactory configurationFactory) {
        if (authMethod == null) {
            throw new NullPointerException("authMethod required");
        }
        if (configurationFactory == null) {
            throw new NullPointerException("configurationFactory required");
        }
        this.authMethod = authMethod;
        this.loginPage = loginPage;
        this.errorPage = errorPage;
        this.realmName = realmName;
        this.configurationFactory = configurationFactory;
    }

    public SecurityHandler buildSecurityHandler(String policyContextID, Subject defaultSubject, RunAsSource runAsSource, boolean checkRolePermissions) {
        final LoginService loginService = new JAASLoginService(configurationFactory, realmName);
        Authenticator authenticator = buildAuthenticator();
        if (defaultSubject == null) {
            defaultSubject = ContextManager.EMPTY;
        }
        AccessControlContext defaultAcc = ContextManager.registerSubjectShort(defaultSubject, null, null);
        IdentityService identityService = new JettyIdentityService(defaultAcc, defaultSubject, runAsSource);
        if (checkRolePermissions) {
            return new JaccSecurityHandler(policyContextID, authenticator, loginService, identityService, defaultAcc);
        } else {
            return new EJBWebServiceSecurityHandler(policyContextID, authenticator, loginService, identityService, defaultAcc);
        }
    }

    private Authenticator buildAuthenticator() {
        Authenticator authenticator;
        if (authMethod == BuiltInAuthMethod.BASIC) {
            authenticator = new BasicAuthenticator();
        } else if (authMethod == BuiltInAuthMethod.DIGEST) {
            authenticator = new DigestAuthenticator();
        } else if (authMethod == BuiltInAuthMethod.CLIENTCERT) {
            authenticator = new ClientCertAuthenticator();
        } else if (authMethod == BuiltInAuthMethod.FORM) {
            authenticator = new FormAuthenticator(loginPage, errorPage, true);
        } else if (authMethod == BuiltInAuthMethod.NONE) {
            authenticator = new NoneAuthenticator();
        } else {
            throw new IllegalStateException("someone added a new BuiltInAuthMethod without telling us");
        }
        return authenticator;
    }
}
