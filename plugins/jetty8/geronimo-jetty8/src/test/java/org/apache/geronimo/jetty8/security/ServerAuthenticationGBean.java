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
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.geronimo.jetty8.handler.JaccSecurityHandler;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;

/**
 * Wraps a supplied ServerAuthentication in a AuthenticationManager instance.  Mostly for testing...
 *
 * @version $Rev$ $Date$
 */
public class ServerAuthenticationGBean implements SecurityHandlerFactory {

    private Map authConfigProperties = new HashMap<Object, Object>();
    private Subject serviceSubject = null;
    private final Authenticator authenticator;
    private final LoginService loginService;


    public ServerAuthenticationGBean(Authenticator authenticator, LoginService loginService) {
        this.authenticator = authenticator;
        this.loginService = loginService;
    }

    public SecurityHandler buildSecurityHandler(String policyContextID, Subject defaultSubject, RunAsSource runAsSource, boolean checkRolePermissions) {
        if (defaultSubject == null) {
            defaultSubject = ContextManager.EMPTY;
        }
        AccessControlContext defaultAcc = ContextManager.registerSubjectShort(defaultSubject, null, null);
        IdentityService identityService = new JettyIdentityService(defaultAcc, defaultSubject, runAsSource);
        return new JaccSecurityHandler(policyContextID, authenticator, loginService, identityService, defaultAcc);
    }

}