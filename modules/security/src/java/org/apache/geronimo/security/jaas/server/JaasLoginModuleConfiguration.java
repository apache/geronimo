/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas.server;

import java.io.Serializable;
import java.util.Map;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;


/**
 * Describes the configuration of a LoginModule -- its name, class, control
 * flag, options, and the Geronimo extension for whether it should run on
 * the client side or server side.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JaasLoginModuleConfiguration implements Serializable {
    private final boolean serverSide;
    private final String loginDomainName;
    private final LoginModuleControlFlag flag;
    private final String loginModuleName;
    private final Map options;
    private final boolean wrapPrincipals;

    public JaasLoginModuleConfiguration(String loginModuleName, LoginModuleControlFlag flag, Map options,
                                        boolean serverSide, String loginDomainName, boolean wrapPrincipals)
    {
        this.serverSide = serverSide;
        this.flag = flag;
        this.loginModuleName = loginModuleName;
        this.options = options;
        this.loginDomainName = loginDomainName;
        this.wrapPrincipals = wrapPrincipals;
    }

    public JaasLoginModuleConfiguration(String loginModuleName, LoginModuleControlFlag flag, Map options, boolean serverSide) {
        this(loginModuleName, flag, options, serverSide, null, false);
    }

    public String getLoginModuleClassName() {
        return loginModuleName;
    }

    public LoginModule getLoginModule(ClassLoader loader) throws GeronimoSecurityException {
        try {
            return (LoginModule) loader.loadClass(loginModuleName).newInstance();
        } catch (Exception e) {
            throw new GeronimoSecurityException("Unable to instantiate login module", e);
        }
    }

    public boolean isServerSide() {
        return serverSide;
    }

    public LoginModuleControlFlag getFlag() {
        return flag;
    }

    public Map getOptions() {
        return options;
    }

    public String getLoginDomainName() {
        return loginDomainName;
    }

    public boolean isWrapPrincipals() {
        return wrapPrincipals;
    }
}
