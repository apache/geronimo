/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.security.jaas;

import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.ContextManager;

import javax.security.auth.Subject;
import java.util.*;
import java.security.Principal;

/**
 * Tracks security information about a single user.  This is used before,
 * during, and after the login.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JaasSecurityContext {
    private String realmName;
    private Subject subject;
    private long created;
    private boolean done;
    private JaasLoginModuleConfiguration[] modules;
    private DecouplingCallbackHandler handler;
    private Set processedPrincipals = new HashSet();

    public JaasSecurityContext(String realmName, JaasLoginModuleConfiguration[] modules) {
        this.realmName = realmName;
        this.created = System.currentTimeMillis();
        this.done = false;
        this.modules = modules;
        subject = new Subject();
    }

    public Subject getSubject() {
        return subject;
    }

    public long getCreated() {
        return created;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public JaasLoginModuleConfiguration[] getModules() {
        return modules;
    }

    public DecouplingCallbackHandler getHandler() {
        if(handler == null) { //lazy create
            handler = new DecouplingCallbackHandler();
        }
        return handler;
    }

    public void processPrincipals(String loginDomainName) {
        List list = new LinkedList();
        for (Iterator it = subject.getPrincipals().iterator(); it.hasNext();) {
            Principal p = (Principal) it.next();
            if(!(p instanceof RealmPrincipal) && !processedPrincipals.contains(p)) {
                list.add(ContextManager.registerPrincipal(new RealmPrincipal(loginDomainName, p, realmName)));
                processedPrincipals.add(p);
            }
        }
        subject.getPrincipals().addAll(list);
    }

    public void processPrincipals(Principal[] principals, String loginDomainName) {
        List list = new LinkedList();
        for (int i = 0; i < principals.length; i++) {
            Principal p = principals[i];
            list.add(p);
            list.add(ContextManager.registerPrincipal(new RealmPrincipal(loginDomainName, p, realmName)));
            processedPrincipals.add(p);
        }
        subject.getPrincipals().addAll(list);
    }

    public Set getProcessedPrincipals() {
        return processedPrincipals;
    }

    public String getRealmName() {
        return realmName;
    }
}
