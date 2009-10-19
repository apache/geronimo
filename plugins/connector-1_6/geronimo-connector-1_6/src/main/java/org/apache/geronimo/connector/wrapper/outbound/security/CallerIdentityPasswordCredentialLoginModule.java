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

package org.apache.geronimo.connector.wrapper.outbound.security;

import java.io.IOException;
import java.util.Map;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.geronimo.connector.outbound.security.ResourcePrincipal;

/**
 * CallerIdentityPasswordCredentialLoginModule uses the username and password from the CallbackHandler
 * and a ManagedConnectionFactory from the Options to construct a j2ca PasswordCredential that can be
 * used for j2ca container managed security.
 *
 * This login module does not check credentials so it should never be able to cause a login to succeed.
 * Therefore the lifecycle methods must return false to indicate success or throw a LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 *
 * */
public class CallerIdentityPasswordCredentialLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private ManagedConnectionFactory managedConnectionFactory;

    private String resourcePrincipalName;
    private String userName;
    private char[] password;
    private ResourcePrincipal resourcePrincipal;
    private PasswordCredential passwordCredential;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        managedConnectionFactory = (ManagedConnectionFactory) options.get(PasswordCredentialLoginModuleWrapper.MANAGED_CONNECTION_FACTORY_OPTION);
        if (managedConnectionFactory == null) {
            throw new IllegalArgumentException("No ManagedConnectionFactory supplied in options");
        }
    }

    public boolean login() throws LoginException {
        if (managedConnectionFactory == null) {
            return false;
        }
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
        resourcePrincipalName = ((NameCallback) callbacks[0]).getName();
        userName = ((NameCallback) callbacks[0]).getName();
        password = ((PasswordCallback) callbacks[1]).getPassword();
        return false;
    }

    public boolean commit() throws LoginException {
        if (resourcePrincipalName == null || userName == null || password == null) {
            return false;
        }
        resourcePrincipal = new ResourcePrincipal(resourcePrincipalName);
        subject.getPrincipals().add(resourcePrincipal);
        passwordCredential = new PasswordCredential(userName, password);
        passwordCredential.setManagedConnectionFactory(managedConnectionFactory);
        subject.getPrivateCredentials().add(passwordCredential);
        
        // Clear private state
        resourcePrincipalName = null;
        userName = null;
        password = null;
        return false;
    }

    public boolean abort() throws LoginException {
        resourcePrincipalName = null;
        userName = null;
        password = null;
        return false;
    }

    public boolean logout() throws LoginException {
        if(!subject.isReadOnly()) {
            subject.getPrincipals().remove(resourcePrincipal);
            subject.getPrivateCredentials().remove(passwordCredential);
        }
        
        // TODO: Destroy the credential when subject is read-only.
        resourcePrincipal = null;
        passwordCredential = null;

        // Clear private state
        resourcePrincipalName = null;
        userName = null;
        password = null;
        return false;
    }
}
