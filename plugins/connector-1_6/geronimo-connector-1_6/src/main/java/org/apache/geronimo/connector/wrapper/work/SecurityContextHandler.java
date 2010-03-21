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


package org.apache.geronimo.connector.wrapper.work;

import java.util.Stack;

import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.WorkContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.connector.work.WorkContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class SecurityContextHandler implements WorkContextHandler<SecurityContext> {
    private static final Logger log = LoggerFactory.getLogger(SecurityContextHandler.class);

    private final String realm;
    private final Subject defaultSubject;
    private final Subject serviceSubject;

    private final ThreadLocal<Stack<Callers>> callers = new ThreadLocal<Stack<Callers>>() {
        @Override
        protected Stack<Callers> initialValue() {
            return new Stack<Callers>();
        }
    };

    public SecurityContextHandler(@ParamAttribute(name="realm") String realm,
                                        @ParamAttribute(name="defaultSubjectRealm")String defaultSubjectRealm, 
                                        @ParamAttribute(name="defaultSubjectId")String defaultSubjectId,
                                        @ParamReference(name="DefaultCredentialStore") CredentialStore defaultCredentialStore,
                                        @ParamAttribute(name="serviceSubjectRealm")String serviceSubjectRealm,
                                        @ParamAttribute(name="serviceSubjectId")String serviceSubjectId,
                                        @ParamReference(name="ServiceCredentialStore")CredentialStore serviceCredentialStore) throws LoginException {
        log.info("SecurityContextHandler set up with\n realm: {}\n defaultSubjectRealm: {}\n defaultSubjectId {}\n DefaultCredentialStore {}\n serviceSubjectRealm {}\n serviceSubjectId {}\n ServiceCredentialStore {}",
               new Object[] {realm, defaultSubjectRealm, defaultSubjectId, defaultCredentialStore, serviceSubjectRealm, serviceSubjectId, serviceCredentialStore});
        if (defaultCredentialStore != null && defaultSubjectRealm != null && defaultSubjectId != null) {
            defaultSubject = defaultCredentialStore.getSubject(defaultSubjectRealm, defaultSubjectId);
        } else {
            defaultSubject = ContextManager.EMPTY;
        }
        if (serviceCredentialStore != null && serviceSubjectRealm != null && serviceSubjectId != null) {
            serviceSubject = serviceCredentialStore.getSubject(serviceSubjectRealm, serviceSubjectId);
        } else {
            serviceSubject = null;
        }
        this.realm = realm;
    }

    public void before(SecurityContext securityContext) throws WorkCompletedException {
        Subject clientSubject;
        if (securityContext == null) {
            clientSubject = defaultSubject;
        } else {
            clientSubject = new Subject();
            ConnectorCallbackHandler callbackHandler = new ConnectorCallbackHandler(realm);
            securityContext.setupSecurityContext(callbackHandler, clientSubject, serviceSubject);
            ContextManager.registerSubjectShort(clientSubject, callbackHandler.getCallerPrincipal(), callbackHandler.getGroups());
        }
        callers.get().push(ContextManager.getCallers());
        ContextManager.setCallers(clientSubject, clientSubject);
    }

    public void after(SecurityContext securityContext) throws WorkCompletedException {
        Subject clientSubject = ContextManager.getCurrentCaller();
        ContextManager.popCallers(callers.get().pop());
        ContextManager.unregisterSubject(clientSubject);
    }

    public boolean supports(Class<? extends WorkContext> clazz) {
        return SecurityContext.class.isAssignableFrom(clazz);
    }

    public boolean required() {
        return true;
    }
}
