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
<<<<<<< .working
 * @version $Rev$ $Date$
=======
 * SubjectRegistrationLoginModule registers the Subject with geronimo and adds an identification principal.
 *
 * This login module does not check credentials so it should never be able to cause a login to succeed.
 * Therefore the lifecycle methods must return false to indicate success or throw a LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
>>>>>>> .merge-right.r565912
 */
public class SubjectRegistrationLoginModule implements LoginModule {

    private Subject subject;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
    }

    public boolean login() throws LoginException {
        return false;
    }

    public boolean commit() throws LoginException {
        SubjectId id = ContextManager.registerSubject(subject);
        IdentificationPrincipal principal = new IdentificationPrincipal(id);
        subject.getPrincipals().add(principal);
        return false;
    }

    public boolean abort() throws LoginException {
        return false;
    }

    public boolean logout() throws LoginException {
        ContextManager.unregisterSubject(subject);
        return false;
    }
}
