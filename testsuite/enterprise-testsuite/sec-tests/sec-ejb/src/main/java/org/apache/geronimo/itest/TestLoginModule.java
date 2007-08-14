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


package org.apache.geronimo.itest;

import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.io.IOException;
import java.security.Principal;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal;

/**
 * @version $Rev$ $Date$
 */
public class TestLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Set<String> users;
    private String user;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        String userList = (String) options.get("users");
        String[] userArray = userList.split(",");
        users = new HashSet<String>(Arrays.asList(userArray));
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[] {
                new NameCallback("user"),
                new PasswordCallback("password", false)
        };
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }
        user = ((NameCallback)callbacks[0]).getName();
        String password = new String(((PasswordCallback)callbacks[1]).getPassword());
        if (user.equals(password) && users.contains(user)) {
            return true;
        }
        throw new LoginException();
    }

    public boolean commit() throws LoginException {
        Principal principal = new GeronimoUserPrincipal(user);
        subject.getPrincipals().add(principal);
        return true;
    }

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean logout() throws LoginException {
        return true;
    }
}
