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

import java.util.Map;
import java.util.Set;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ConfiguredIdentityNamedUsernamePasswordLoginModule implements LoginModule {
    public static final String CREDENTIAL_NAME = "org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Name";
    public static final String USER_NAME = "org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Username";
    public static final String PASSWORD = "org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Password";

    private Subject subject;
    private NamedUsernamePasswordCredential namedUsernamePasswordCredential;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        String name = (String) options.get(CREDENTIAL_NAME);
        String username = (String) options.get(USER_NAME);
        String password = (String) options.get(PASSWORD);
        namedUsernamePasswordCredential = new NamedUsernamePasswordCredential(username, password.toCharArray(), name);
    }

    public boolean login() throws LoginException {
        return true;
    }

    public boolean commit() throws LoginException {
        if (subject.isReadOnly()) {
            throw new LoginException("Subject is ReadOnly");
        }

        Set pvtCreds = subject.getPrivateCredentials();
        if (namedUsernamePasswordCredential != null && !pvtCreds.contains(namedUsernamePasswordCredential)) {
            pvtCreds.add(namedUsernamePasswordCredential);
        }
        return true;
    }

    public boolean abort() throws LoginException {
        return logout();
    }

    public boolean logout() throws LoginException {
        if (namedUsernamePasswordCredential == null) {
            return true;
        }

        Set pvtCreds = subject.getPrivateCredentials(UsernamePasswordCredential.class);
        if (pvtCreds.contains(namedUsernamePasswordCredential)) {
            pvtCreds.remove(namedUsernamePasswordCredential);
        }

        namedUsernamePasswordCredential = null;

        return true;
    }
}
