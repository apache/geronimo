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

import java.io.Externalizable;
import java.io.Serializable;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.common.GeronimoSecurityException;

/**
 * Describes the configuration of a LoginModule -- its name, class, control
 * flag, options, and the Geronimo extension for whether it should run on
 * the client side or server side.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JaasLoginModuleConfiguration implements Serializable {
    private boolean serverSide;
    private String loginDomainName;
    private LoginModuleControlFlag flag;
    private String loginModuleName;
    private Map options;
    private transient LoginModule loginModule;

    public JaasLoginModuleConfiguration(String loginModuleName, LoginModuleControlFlag flag, Map options, boolean serverSide, String loginDomainName) {
        this.serverSide = serverSide;
        this.flag = flag;
        this.loginModuleName = loginModuleName;
        this.options = options;
        this.loginDomainName = loginDomainName;
    }
    public JaasLoginModuleConfiguration(String loginModuleName, LoginModuleControlFlag flag, Map options, boolean serverSide) {
        this(loginModuleName, flag, options, serverSide, null);
    }

    public String getLoginModuleClassName() {
        return loginModuleName;
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

    public String getLoginDomainName() {
        return loginDomainName;
    }

    /**
     * Strips out stuff that isn't serializable so this can be safely passed to
     * a remote server.
     */
    public JaasLoginModuleConfiguration getSerializableCopy() {
        Map other = new HashMap();
        for (Iterator it = options.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            Object value = options.get(key);
            if(value instanceof Serializable || value instanceof Externalizable || value instanceof Remote) {
                other.put(key, value);
            }
        }

        return new JaasLoginModuleConfiguration(loginModuleName, flag, other, serverSide, loginDomainName);
    }
}
