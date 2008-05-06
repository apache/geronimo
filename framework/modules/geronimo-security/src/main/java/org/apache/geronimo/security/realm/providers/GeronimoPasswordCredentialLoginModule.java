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

package org.apache.geronimo.security.realm.providers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;

/**
 * GeronimoPasswordCredentialLoginModule stores the user name and password in a GeronimoPasswordCredential.
 * This allows an application to  retrieve the subject through jacc or the geronimo specific ContextManager and
 * find out what the password was.  I can't think of any other reason to use it right now.
 *
 * This login module does not check credentials so it should never be able to cause a login to succeed.
 * Therefore the lifecycle methods must return false to indicate success or throw a LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoPasswordCredentialLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(GeronimoPasswordCredentialLoginModule.class);

    // Note: If this LoginModule supports any options, the Collections.EMPTY_LIST in the following should be
    // replaced with the list of supported options for e.g. Arrays.asList(option1, option2, ...) etc.
    public final static List<String> supportedOptions = Collections.unmodifiableList(Collections.EMPTY_LIST);

    private Subject subject;
    private CallbackHandler callbackHandler;

    private GeronimoPasswordCredential geronimoPasswordCredential;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        for(Object option: options.keySet()) {
            if(!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: "+option+". Not supported.");
            }
        }
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name");
        callbacks[1] = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (java.io.IOException e) {
            throw (LoginException) new LoginException("Could not determine username and password").initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException) new LoginException("Unlikely UnsupportedCallbackException").initCause(e);
        }
        String username = ((NameCallback) callbacks[0]).getName();
        char[] password = ((PasswordCallback) callbacks[1]).getPassword();

        if (username == null || password == null) return false;
        geronimoPasswordCredential = new GeronimoPasswordCredential(username, password);
        return false;
    }

    public boolean commit() throws LoginException {
        if(geronimoPasswordCredential != null) {
            subject.getPrivateCredentials().add(geronimoPasswordCredential);
        }
        return false;
    }

    public boolean abort() throws LoginException {
        if(geronimoPasswordCredential != null) {
            try {
                geronimoPasswordCredential.destroy();
            } catch (DestroyFailedException e) {
                // do nothing
            }
            geronimoPasswordCredential = null;
        }
        return false;
    }

    public boolean logout() throws LoginException {
        if(geronimoPasswordCredential == null) {
            return false;
        }
        if(!subject.isReadOnly()) {
            subject.getPrivateCredentials().remove(geronimoPasswordCredential);
        }
        try {
            geronimoPasswordCredential.destroy();
        } catch (DestroyFailedException e) {
            // do nothing
        }
        geronimoPasswordCredential = null;
        return false;
    }
}
