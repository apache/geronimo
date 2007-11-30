/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.DestroyFailedException;
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
 * Inserts Username/Password credential into private credentials of Subject.
 * <p/>
 * If either the username or password is not passed in the callback handler,
 * then the credential is not placed into the Subject.
 *
 * This login module does not check credentials so it should never be able to cause a login to succeed.
 * Therefore the lifecycle methods must return false to indicate success or throw a LoginException to indicate failure.
 *
 * @deprecated As of release 2.0.3
 * @see org.apache.geronimo.security.realm.providers.GeronimoPasswordCredentialLoginModule
 * 
 * @version $Revision$ $Date$
 */
@Deprecated
public class UPCredentialLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private UsernamePasswordCredential upCredential;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("User name");
        callbacks[1] = new PasswordCallback("Password", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }

        String username = ((NameCallback) callbacks[0]).getName();
        char[] password = ((PasswordCallback) callbacks[1]).getPassword();

        if (username == null || password == null) return false;

        upCredential = new UsernamePasswordCredential(username, password);

        return false;
    }

    public boolean commit() throws LoginException {

        if (subject.isReadOnly()) {
            throw new LoginException("Subject is ReadOnly");
        }

        Set pvtCreds = subject.getPrivateCredentials();
        if (upCredential != null && !pvtCreds.contains(upCredential)) {
            pvtCreds.add(upCredential);
        }

        return false;
    }

    public boolean abort() throws LoginException {

        return logout();
    }

    public boolean logout() throws LoginException {

        if (upCredential == null) return true;

        Set pvtCreds = subject.getPrivateCredentials(UsernamePasswordCredential.class);
        if (pvtCreds.contains(upCredential)) {
            pvtCreds.remove(upCredential);
        }

        try {
            upCredential.destroy();
        } catch (DestroyFailedException e) {
            // do nothing
        }
        upCredential = null;

        return false;
    }

}
