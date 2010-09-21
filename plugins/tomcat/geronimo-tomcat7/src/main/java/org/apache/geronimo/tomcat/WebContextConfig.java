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

import org.apache.geronimo.web.info.LoginConfigInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev$ $Date$
 */
public class WebContextConfig extends BaseGeronimoContextConfig {
    
    public WebContextConfig(WebAppInfo webAppInfo) {
        super(webAppInfo);
    }
    

    @Override
    protected void authenticatorConfig(LoginConfigInfo loginConfig) {
        if (!(context instanceof GeronimoStandardContext)) {
            throw new IllegalStateException("Unexpected context type");
        }
        GeronimoStandardContext geronimoContext = (GeronimoStandardContext) context;
        if (geronimoContext.isAuthenticatorInstalled()) {
            return;
        }
        if (geronimoContext.getDefaultSubject() == null) {
            return;
        }
        if (loginConfig == null) {
            loginConfig = new LoginConfigInfo();
        }

        configureSecurity(geronimoContext,
                geronimoContext.getPolicyContextId(),
                geronimoContext.getConfigurationFactory(),
                geronimoContext.getDefaultSubject(),
                loginConfig.authMethod,
                loginConfig.realmName,
                loginConfig.formLoginPage,
                loginConfig.formErrorPage);
    }
    
}
