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


package org.apache.geronimo.jetty8.security;

import java.security.AccessControlContext;
import java.security.Principal;
import java.util.Arrays;

import javax.security.auth.Subject;

import org.apache.geronimo.jetty8.handler.GeronimoRunAsToken;
import org.apache.geronimo.jetty8.handler.GeronimoUserIdentity;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class JettyIdentityService implements IdentityService {

    private final AccessControlContext defaultAcc;
    private final Subject defaultSubject;
    private final RunAsSource runAsSource;

    public JettyIdentityService(AccessControlContext defaultAcc, Subject defaultSubject, RunAsSource runAsSource) {
        this.defaultAcc = defaultAcc;
        this.defaultSubject = defaultSubject;
        this.runAsSource = runAsSource;
    }

    public Object associate(UserIdentity user) {
        Callers oldCallers = ContextManager.getCallers();
        if (user == null) {
            //exit
            ContextManager.setCallers(defaultSubject, defaultSubject);
        } else {
            //enter
            ContextManager.setCallers(user.getSubject(), user.getSubject());
        }
        return oldCallers;
    }

    public void disassociate(Object previousIdentity) {
        ContextManager.popCallers((Callers) previousIdentity);

    }

    public Object setRunAs(UserIdentity userIdentity, RunAsToken token) {
        GeronimoRunAsToken geronimoRunAsToken = (GeronimoRunAsToken) token;
        Subject runAsSubject = geronimoRunAsToken == null? null: geronimoRunAsToken.getRunAsSubject();
        return ContextManager.pushNextCaller(runAsSubject);
    }

    public void unsetRunAs(Object previousToken) {
        ContextManager.popCallers((Callers) previousToken);
    }

    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles) {
        if (subject != null) {
            AccessControlContext acc = ContextManager.registerSubjectShort(subject, userPrincipal, roles == null? null: Arrays.asList(roles));
            return new GeronimoUserIdentity(subject, userPrincipal, acc);
        }
        return new GeronimoUserIdentity(null, null, defaultAcc);
    }

    public RunAsToken newRunAsToken(String runAsName) {
        Subject runAsSubject = runAsSource.getSubjectForRole(runAsName);
        return new GeronimoRunAsToken(runAsSubject);
    }

    public UserIdentity getSystemUserIdentity() {
        return new GeronimoUserIdentity(null, null, defaultAcc);
    }
}
