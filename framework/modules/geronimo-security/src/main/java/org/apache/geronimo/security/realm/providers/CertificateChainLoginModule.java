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

import java.io.IOException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;

/**
 * An example LoginModule that authenticates based on a client certificate.
 * Authentication is provided by the SSL layer supplying the client certificate.
 * All we check is that it is present. Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 *
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class CertificateChainLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(CertificateChainLoginModule.class);

    // Note: If this LoginModule supports any options, the Collections.EMPTY_LIST in the following should be
    // replaced with the list of supported options for e.g. Arrays.asList(option1, option2, ...) etc.
    public final static List<String> supportedOptions = Collections.unmodifiableList(Collections.EMPTY_LIST);
    private Subject subject;
    private CallbackHandler handler;
    private X500Principal principal;
    private boolean loginSucceeded;
    private final Set<Principal> allPrincipals = new HashSet<Principal>();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
        for(Object option: options.keySet()) {
            if(!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: "+option+". Not supported.");
            }
        }
    }

    /**
     * This LoginModule is not to be ignored.  So, this method should never return false.
     * @return true if authentication succeeds, or throw a LoginException such as FailedLoginException
     *         if authentication fails
     */
    public boolean login() throws LoginException {
        loginSucceeded = false;
        Callback[] callbacks = new Callback[1];

        callbacks[0] = new CertificateChainCallback();
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        assert callbacks.length == 1;
        Certificate[] certificateChain = ((CertificateChainCallback)callbacks[0]).getCertificateChain();
        if (certificateChain == null || certificateChain.length == 0) {
            throw new FailedLoginException();
        }
        if (!(certificateChain[0] instanceof X509Certificate)) {
            throw new FailedLoginException();
        }
        //TODO actually validate chain
        principal = ((X509Certificate)certificateChain[0]).getSubjectX500Principal();

        loginSucceeded = true;
        return true;
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     *
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if(loginSucceeded) {
            allPrincipals.add(principal);
            allPrincipals.add(new GeronimoUserPrincipal(principal.getName()));
            subject.getPrincipals().addAll(allPrincipals);
        }
        // Clear out the private state
        principal = null;
        
        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if(loginSucceeded) {
            // Clear out the private state
            principal = null;
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        principal = null;
        if(!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

}
