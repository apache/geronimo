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


package org.apache.geronimo.security.credentialstore;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import java.security.Principal;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.osgi.framework.Bundle;

/**
 * Simple login module that may help create subjects for run-as roles.  List the desired class as the principalClass and the
 * desired allowed names for principals as allowedNames in a comma-separated list.
 *
 * @version $Rev$ $Date$
 */
public class RunAsLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Class<Principal> principalClass;
    private List<String> allowedNames;
    private String name;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        Bundle bundle = (Bundle) options.get(JaasLoginModuleUse.CLASSLOADER_LM_OPTION);
        String principalClassName = (String) options.get("principalClass");
        try {
            principalClass = (Class<Principal>) bundle.loadClass(principalClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(principalClassName + " not found", e);
        }
        String allNames = (String) options.get("principalNames");
        allowedNames = Arrays.asList(allNames.split(","));
    }

    public boolean login() throws LoginException {
        NameCallback callback = new NameCallback("foo");
        try {
            callbackHandler.handle(new Callback[] {callback});
        } catch (IOException e) {
            throw (LoginException) new LoginException().initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException) new LoginException().initCause(e);
        }
        name = callback.getName();
        if (allowedNames.contains(name)) {
            return true;
        }
        throw new FailedLoginException("name not recognized " + name);
    }

    public boolean commit() throws LoginException {
        try {
            Constructor<Principal> c = principalClass.getConstructor(String.class);
            Principal principal = c.newInstance(name);
            subject.getPrincipals().add(principal);
            return true;
        } catch (InstantiationException e) {
            throw (LoginException) new LoginException().initCause(e);
        } catch (IllegalAccessException e) {
            throw (LoginException) new LoginException().initCause(e);
        } catch (NoSuchMethodException e) {
            throw (LoginException) new LoginException().initCause(e);
        } catch (InvocationTargetException e) {
            throw (LoginException) new LoginException().initCause(e);
        }
    }

    public boolean abort() throws LoginException {
        return false;
    }

    public boolean logout() throws LoginException {
        return false;
    }
}
