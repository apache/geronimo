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

package org.apache.geronimo.security.realm.providers;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.util.Map;


/**
 * @version $Rev$ $Date$
 */
public class GeronimoPasswordCredentialLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private GeronimoPasswordCredential geronimoPasswordCredential;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("");
        callbacks[1] = new PasswordCallback("", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (java.io.IOException e) {
        } catch (UnsupportedCallbackException e) {
            throw (LoginException) new LoginException("Unlikely UnsupportedCallbackException").initCause(e);
        }
        geronimoPasswordCredential = new GeronimoPasswordCredential(((NameCallback) callbacks[0]).getName(),
                                                                    ((PasswordCallback) callbacks[1]).getPassword());
        return true;
    }

    public boolean commit() throws LoginException {
        subject.getPrivateCredentials().add(geronimoPasswordCredential);
        return true;
    }

    public boolean abort() throws LoginException {
        geronimoPasswordCredential = null;
        return true;
    }

    public boolean logout() throws LoginException {
        geronimoPasswordCredential = null;
        return true;
    }
}
