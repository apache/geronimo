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

package org.apache.geronimo.connector.outbound.security;

import java.io.IOException;
import java.util.Map;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:12 $
 *
 * */
public class PasswordCredentialLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private PasswordCredentialRealm passwordCredentialRealm;

    private String resourcePrincipalName;
    private String userName;
    private char[] password;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        passwordCredentialRealm = (PasswordCredentialRealm) options.get(PasswordCredentialRealm.REALM_INSTANCE);
        if (passwordCredentialRealm == null) {
            throw new IllegalArgumentException("No realm supplied in options");
        }
    }

    public boolean login() throws LoginException {
        if (passwordCredentialRealm == null || passwordCredentialRealm.managedConnectionFactory == null) {
            return false;
        }
        Callback[] callbacks = new Callback[3];

        callbacks[0] = new NameCallback("Resource Principal");
        callbacks[1] = new NameCallback("User name");
        callbacks[2] = new PasswordCallback("Password", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        resourcePrincipalName = ((NameCallback) callbacks[0]).getName();
        userName = ((NameCallback) callbacks[1]).getName();
        password = ((PasswordCallback) callbacks[2]).getPassword();
        return resourcePrincipalName != null && userName != null && password != null;
    }

    public boolean commit() throws LoginException {
        subject.getPrincipals().add(new ResourcePrincipal(resourcePrincipalName));
        PasswordCredential passwordCredential = new PasswordCredential(userName, password);
        passwordCredential.setManagedConnectionFactory(passwordCredentialRealm.getManagedConnectionFactory());
        subject.getPrivateCredentials().add(passwordCredential);
        return true;
    }

    public boolean abort() throws LoginException {
        subject = null;
        userName = null;
        password = null;
        return true;
    }

    public boolean logout() throws LoginException {
        subject = null;
        userName = null;
        password = null;
        return true;
    }
}
