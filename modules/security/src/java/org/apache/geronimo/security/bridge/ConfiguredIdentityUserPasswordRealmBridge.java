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

package org.apache.geronimo.security.bridge;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;


/**
 * ConfiguredIdentityRealmBridge supplies a constant mapping between realms:
 * it always returns the configured user and password, no matter what the
 * source realm or source subject.
 *
 * @version $Rev$ $Date$
 */
public class ConfiguredIdentityUserPasswordRealmBridge extends AbstractRealmBridge {
    private String configuredUser;
    private char[] configuredPassword;

    public ConfiguredIdentityUserPasswordRealmBridge() {
    }

    public ConfiguredIdentityUserPasswordRealmBridge(String targetRealm, String configuredUser, String configuredPassword) {
        super(targetRealm);
        this.configuredUser = configuredUser;
        setConfiguredPassword(configuredPassword);
    }

    public String getConfiguredUser() {
        return configuredUser;
    }

    public void setConfiguredUser(String configuredUser) {
        this.configuredUser = configuredUser;
    }

    public String getConfiguredPassword() {
        return configuredPassword == null ? null : new String(configuredPassword);
    }

    public void setConfiguredPassword(String configuredPassword) {
        this.configuredPassword = configuredPassword == null ? null : configuredPassword.toCharArray();
    }

    protected CallbackHandler getCallbackHandler(Subject sourceSubject) {
        return new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    Callback callback = callbacks[i];
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(configuredUser);
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(configuredPassword);
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }

        };
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfiguredIdentityUserPasswordRealmBridge.class, AbstractRealmBridge.GBEAN_INFO);
        infoFactory.addAttribute("configuredUser", String.class, true);
        infoFactory.addAttribute("configuredPassword", String.class, true);
        infoFactory.setConstructor(new String[]{"targetRealm", "configuredUser", "configuredPassword"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
