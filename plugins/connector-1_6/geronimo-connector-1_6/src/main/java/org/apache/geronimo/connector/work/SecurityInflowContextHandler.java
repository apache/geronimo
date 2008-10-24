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


package org.apache.geronimo.connector.work;

import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.SecurityInflowContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.Callers;

/**
 * @version $Rev:$ $Date:$
 */
public class SecurityInflowContextHandler implements InflowContextHandler<SecurityInflowContext> {

    private final String realm;
    private final Subject serviceSubject;

    private final ThreadLocal<Callers> callers = new ThreadLocal<Callers>();

    public SecurityInflowContextHandler(String realm, String serviceSubjectRealm, String serviceSubjectId, CredentialStore credentialStore) throws LoginException {
        serviceSubject = credentialStore.getSubject(serviceSubjectRealm, serviceSubjectId);
        this.realm = realm;
    }

    public void before(SecurityInflowContext securityInflowContext) throws WorkCompletedException {
        Subject clientSubject = new Subject();
        ConnectorCallbackHandler callbackHandler = new ConnectorCallbackHandler(realm);
        securityInflowContext.setupSecurityContext(callbackHandler, clientSubject, serviceSubject);
        ContextManager.registerSubjectShort(clientSubject, callbackHandler.getCallerPrincipal(), callbackHandler.getGroups());
        callers.set(ContextManager.getCallers());
        ContextManager.setCallers(clientSubject, clientSubject);
    }

    public void after(SecurityInflowContext securityInflowContext) throws WorkCompletedException {
        Subject clientSubject = ContextManager.getCurrentCaller();
        ContextManager.popCallers(callers.get());
        callers.remove();
        ContextManager.unregisterSubject(clientSubject);
    }

    public Class<SecurityInflowContext> getHandledClass() {
        return SecurityInflowContext.class;
    }
}
