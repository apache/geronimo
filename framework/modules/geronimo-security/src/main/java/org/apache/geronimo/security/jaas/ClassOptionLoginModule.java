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


package org.apache.geronimo.security.jaas;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * This provides a workaround to the problem of the LoginContext not knowing what classloader to use for creating LoginModules.
 *
 * @version $Revision$ $Date$
 */
public class ClassOptionLoginModule implements LoginModule {
    public static final String CLASS_OPTION = WrappingLoginModule.class.getName() + ".LoginModuleClass";
    public static final List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(CLASS_OPTION));
    private LoginModule delegate;


    public ClassOptionLoginModule() {
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        Class lmClass = (Class) options.get(CLASS_OPTION);
        try {
            delegate = (LoginModule) lmClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create login module instance", e);
        }
        delegate.initialize(subject, callbackHandler, sharedState, options);
    }

    public boolean login() throws LoginException {
        return delegate.login();
    }

    public boolean abort() throws LoginException {
        return delegate.abort();
    }

    public boolean commit() throws LoginException {
        return delegate.commit();
    }

    public boolean logout() throws LoginException {
        return delegate.logout();
    }
}