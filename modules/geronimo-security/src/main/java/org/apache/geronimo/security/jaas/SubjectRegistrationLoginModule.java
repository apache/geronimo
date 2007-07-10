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

import java.util.Map;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.IdentificationPrincipal;

/**
 * @version $Rev:$ $Date:$
 */
public class SubjectRegistrationLoginModule implements LoginModule {

    private Subject subject;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
    }

    public boolean login() throws LoginException {
        return true;
    }

    public boolean commit() throws LoginException {
        SubjectId id = ContextManager.registerSubject(subject);
        IdentificationPrincipal principal = new IdentificationPrincipal(id);
        subject.getPrincipals().add(principal);
        return true;
    }

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean logout() throws LoginException {
        ContextManager.unregisterSubject(subject);
        return true;
    }
}
