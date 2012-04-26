/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.security.jaspi.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.callback.CertStoreCallback;
import javax.security.auth.message.callback.PrivateKeyCallback;
import javax.security.auth.message.callback.SecretKeyCallback;
import javax.security.auth.message.callback.TrustStoreCallback;
import javax.security.auth.Subject;

import org.apache.geronimo.security.jaspi.LoginService;
import org.apache.geronimo.security.jaspi.UserIdentity;
import org.apache.geronimo.security.realm.providers.GeronimoCallerPrincipal;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;
import org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal;

/**
 * @version $Rev$ $Date$
 */
public class JaspicCallbackHandler implements CallbackHandler {
    private final LoginService loginService;

    public JaspicCallbackHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            // jaspi to server communication
            if (callback instanceof CallerPrincipalCallback) {
                CallerPrincipalCallback callerPrincipalCallback = (CallerPrincipalCallback) callback;
                if (callerPrincipalCallback.getPrincipal() != null) {
                    Principal callerPrincipal = callerPrincipalCallback.getPrincipal();
                    callerPrincipalCallback.getSubject().getPrincipals().add(callerPrincipal);
                    callerPrincipalCallback.getSubject().getPrincipals().add(new WrappingCallerPrincipal(callerPrincipal));
                } else if (callerPrincipalCallback.getName() != null) {
                    Principal callerPrincipal = new GeronimoUserPrincipal(callerPrincipalCallback.getName());
                    callerPrincipalCallback.getSubject().getPrincipals().add(callerPrincipal);
                    callerPrincipalCallback.getSubject().getPrincipals().add(new WrappingCallerPrincipal(callerPrincipal));
                }
            } else if (callback instanceof GroupPrincipalCallback) {
                GroupPrincipalCallback groupPrincipalCallback = ( GroupPrincipalCallback ) callback;
                if (groupPrincipalCallback.getGroups() != null) {
                    Set<Principal> principalSet = groupPrincipalCallback.getSubject().getPrincipals();
                    for (String groupName : groupPrincipalCallback.getGroups()) {
                        principalSet.add(new GeronimoGroupPrincipal(groupName));
                    }
                }
            } else if (callback instanceof PasswordValidationCallback) {
                PasswordValidationCallback passwordValidationCallback = (PasswordValidationCallback) callback;
                Subject subject = passwordValidationCallback.getSubject();

                UserIdentity user = loginService.login(passwordValidationCallback.getUsername(), new String(passwordValidationCallback.getPassword()));

                if (user != null) {
                    passwordValidationCallback.setResult(true);
                    subject.getPrincipals().addAll(user.getSubject().getPrincipals());
                    subject.getPrivateCredentials().add(user);
                }
            }
            // server to jaspi communication
            // TODO implement these
            else if (callback instanceof CertStoreCallback) {
            } else if (callback instanceof PrivateKeyCallback) {
            } else if (callback instanceof SecretKeyCallback) {
            } else if (callback instanceof TrustStoreCallback) {
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

}
