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
package org.apache.geronimo.security.jaas;

import org.apache.geronimo.common.GeronimoSecurityException;

import javax.security.auth.spi.LoginModule;
import java.io.Serializable;
import java.util.Map;

/**
 * Describes the configuration of a LoginModule -- its name, class, control
 * flag, options, and the Geronimo extension for whether it should run on
 * the client side or server side.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JaasLoginModuleConfiguration implements Serializable {
    private boolean serverSide;
    private LoginModuleControlFlag flag;
    private String loginModuleName;
    private Map options;
    private transient LoginModule loginModule;

    public JaasLoginModuleConfiguration(String loginModuleName, LoginModuleControlFlag flag, Map options, boolean serverSide) {
        this.serverSide = serverSide;
        this.flag = flag;
        this.loginModuleName = loginModuleName;
        this.options = options;
    }

    public LoginModule getLoginModule(ClassLoader loader) throws GeronimoSecurityException {
        if(loginModule == null) {
            try {
                loginModule = (LoginModule) loader.loadClass(loginModuleName).newInstance();
            } catch (Exception e) {
                throw new GeronimoSecurityException("Unable to instantiate login module", e);
            }
        }
        return loginModule;
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
}
