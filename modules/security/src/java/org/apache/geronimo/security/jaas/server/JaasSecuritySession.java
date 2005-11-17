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
package org.apache.geronimo.security.jaas.server;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * Tracks security information about a single user.  This is used before,
 * during, and after the login.
 *
 * @version $Rev$ $Date$
 */
public class JaasSecuritySession {
    private final String realmName;
    private final Subject subject;
    private final Map sharedContext;
    private final long created;
    private boolean done;
    private final JaasLoginModuleConfiguration[] modules;
    private final LoginModule[] loginModules;
    private DecouplingCallbackHandler handler = new DecouplingCallbackHandler();

    public JaasSecuritySession(String realmName, JaasLoginModuleConfiguration[] modules, Map sharedContext, ClassLoader classLoader) {
        this.realmName = realmName;
        this.created = System.currentTimeMillis();
        this.done = false;
        this.modules = modules;
        subject = new Subject();
        this.sharedContext = sharedContext;
        loginModules = new LoginModule[modules.length];
        for (int i = 0; i < modules.length; i++) {
            if (modules[i].isWrapPrincipals()) {
                loginModules[i] = new WrappingLoginModuleProxy(modules[i].getLoginModule(classLoader),
                                                               modules[i].getLoginDomainName(),
                                                               realmName);
            } else {
                loginModules[i] = modules[i].getLoginModule(classLoader);
            }
        }
    }

    public Subject getSubject() {
        return subject;
    }

    public Map getSharedContext() {
        return sharedContext;
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

    public LoginModule getLoginModule(int index) throws LoginException {
        checkRange(index);
        return loginModules[index];
    }

    private void checkRange(int index) throws LoginException {
        if (index < 0 || index >= loginModules.length) {
            throw new LoginException("Invalid index: " + index);
        }
    }

    public boolean isServerSide(int index) throws LoginException {
        checkRange(index);
        return modules[index].isServerSide();
    }

    public String getLoginDomainName(int index) throws LoginException {
        checkRange(index);
        return modules[index].getLoginDomainName();
    }

    public Map getOptions(int index) throws LoginException {
        checkRange(index);
        return modules[index].getOptions();
    }

    public DecouplingCallbackHandler getHandler() {
        return handler;
    }

    public String getRealmName() {
        return realmName;
    }
}
