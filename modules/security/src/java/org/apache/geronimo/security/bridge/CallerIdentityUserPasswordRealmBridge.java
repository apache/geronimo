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

import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredential;


/**
 * @version $Rev$ $Date$
 */
public class CallerIdentityUserPasswordRealmBridge extends AbstractRealmBridge {

    private static final GBeanInfo GBEAN_INFO;

    public CallerIdentityUserPasswordRealmBridge() {
    }

    public CallerIdentityUserPasswordRealmBridge(String targetRealm) {
        super(targetRealm);
    }


    protected CallbackHandler getCallbackHandler(final Subject sourceSubject) {
        return new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                Set credentials = sourceSubject.getPrivateCredentials(GeronimoPasswordCredential.class);
                if (credentials == null || credentials.size() != 1) {
                    throw new UnsupportedCallbackException(null, "No GeronimoPasswordCredential to read");
                }
                GeronimoPasswordCredential geronimoPasswordCredential = (GeronimoPasswordCredential) credentials.iterator().next();
                for (int i = 0; i < callbacks.length; i++) {
                    Callback callback = callbacks[i];
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(geronimoPasswordCredential.getUserName());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(geronimoPasswordCredential.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callback, "Only name and password callbacks supported");
                    }

                }
            }

        };
    }

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(CallerIdentityUserPasswordRealmBridge.class, AbstractRealmBridge.GBEAN_INFO);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
