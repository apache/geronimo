/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.io.IOException;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;


/**
 * ConfiguredIdentityRealmBridge supplies a constant mapping between realms:
 * it always returns the configured user and password, no matter what the
 * source realm or source subject.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:08 $
 */
public class ConfiguredIdentityUserPasswordRealmBridge extends AbstractRealmBridge {

    private static final GBeanInfo GBEAN_INFO;

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
            public void handle(Callback[] callbacks)
                    throws IOException, UnsupportedCallbackException {
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfiguredIdentityUserPasswordRealmBridge.class.getName(), AbstractRealmBridge.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("ConfiguredUser", true));
        infoFactory.addAttribute(new GAttributeInfo("ConfiguredPassword", true));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"TargetRealm", "ConfiguredUser", "ConfiguredPassword"},
                                                        new Class[]{String.class, String.class, String.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
