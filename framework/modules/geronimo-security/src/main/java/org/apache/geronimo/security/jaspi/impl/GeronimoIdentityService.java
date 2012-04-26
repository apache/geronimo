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

import java.security.AccessControlContext;
import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.geronimo.security.jaspi.IdentityService;
import org.apache.geronimo.security.jaspi.UserIdentity;
import org.apache.geronimo.security.realm.providers.GeronimoCallerPrincipal;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.Callers;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoIdentityService implements IdentityService {
    private final Subject defaultSubject;

    public GeronimoIdentityService(Subject defaultSubject) {
        this.defaultSubject = defaultSubject;
    }

    public Object associate(UserIdentity userIdentity) {
        Subject subject = userIdentity == null? defaultSubject: userIdentity.getSubject();
        Callers callers = ContextManager.getCallers();
        ContextManager.setCallers(subject, subject);
        return callers;
    }

    public void dissociate(Object previous) {
        ContextManager.popCallers((Callers) previous);
    }

    public UserIdentity newUserIdentity(Subject subject) {
        Principal callerPrincipal = null;
        for (GeronimoCallerPrincipal principal: subject.getPrincipals(GeronimoCallerPrincipal.class)) {
            if (principal instanceof WrappingCallerPrincipal) {
                callerPrincipal = ((WrappingCallerPrincipal)principal).getWrapped();
            } else {
                callerPrincipal = principal;
            }
        }
        AccessControlContext acc = ContextManager.registerSubjectShort(subject, callerPrincipal);
        return new JACCUserIdentity(subject, callerPrincipal, acc);
    }
}
