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

import javax.security.auth.Subject;

import org.apache.catalina.core.StandardContext;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.tomcat.security.Authorizer;
import org.apache.geronimo.tomcat.security.jacc.JACCEJBWebServiceAuthorizer;
import org.apache.geronimo.web.info.LoginConfigInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev$ $Date$
 */
public class EjbWsContextConfig extends BaseGeronimoContextConfig {
    private final String policyContextId;
    private final ConfigurationFactory configurationFactory;
    private final Subject defaultSubject;
    private final String authMethod;
    private final String realmName;

    public EjbWsContextConfig(WebAppInfo webAppInfo, String policyContextId, ConfigurationFactory configurationFactory, Subject defaultSubject, String authMethod, String realmName) {
        super(webAppInfo);
        this.policyContextId = policyContextId;
        this.configurationFactory = configurationFactory;
        this.defaultSubject = defaultSubject;
        this.authMethod = authMethod;
        this.realmName = realmName;
    }

    @Override
    protected void authenticatorConfig(LoginConfigInfo loginConfigInfo) {
        if (policyContextId == null || configurationFactory == null) {
            return;
        }

        configureSecurity((StandardContext)context,
                policyContextId,
                configurationFactory,
                defaultSubject,
                authMethod, realmName, null, null);
    }

    @Override
    protected Authorizer createAuthorizer(AccessControlContext defaultAcc) {
        return new JACCEJBWebServiceAuthorizer(defaultAcc);
    }

}
