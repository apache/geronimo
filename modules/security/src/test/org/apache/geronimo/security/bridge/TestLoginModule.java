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

import java.io.IOException;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredential;


/**
 * @version $Rev$ $Date$
 */
public class TestLoginModule implements LoginModule {
    public final static String REALM_NAME = "bridge-realm";
    public final static String JAAS_NAME = "bridge";

    private Subject subject;
    private CallbackHandler callbackHandler;

    private String resourcePrincipalName;
    private String userName;
    private char[] password;


    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[3];

        callbacks[0] = new NameCallback("Resource Principal");
        callbacks[1] = new NameCallback("User Name");
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
        subject.getPrincipals().add(new TestPrincipal(resourcePrincipalName));
        GeronimoPasswordCredential passwordCredential = new GeronimoPasswordCredential(userName, password);
        subject.getPrivateCredentials().add(passwordCredential);
        return true;
    }

    public boolean abort() throws LoginException {
        return false;
    }

    public boolean logout() throws LoginException {
        return false;
    }
}
