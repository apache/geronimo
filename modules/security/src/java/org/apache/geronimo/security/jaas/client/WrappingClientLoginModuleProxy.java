/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas.client;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;


/**
 * @version $Revision: $ $Date: $
 */
public class WrappingClientLoginModuleProxy extends ClientLoginModuleProxy {
    private final String loginDomainName;
    private final String realmName;
    private final Subject localSubject = new Subject();

    public WrappingClientLoginModuleProxy(LoginModuleControlFlag controlFlag, Subject subject, LoginModule source,
                                          String loginDomainName, String realmName)
    {
        super(controlFlag, subject, source);
        this.loginDomainName = loginDomainName;
        this.realmName = realmName;
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        super.initialize(localSubject, callbackHandler, sharedState, options);
    }

    public boolean commit() throws LoginException {
        boolean result = super.commit();

        Set wrapped = new HashSet();
        for (Iterator iter = subject.getPrincipals().iterator(); iter.hasNext();) {
            Principal principal = (Principal) iter.next();

            wrapped.add(new DomainPrincipal(loginDomainName, principal));
            wrapped.add(new RealmPrincipal(realmName, loginDomainName, principal));
        }
        localSubject.getPrincipals().addAll(wrapped);
        subject.getPrincipals().addAll(localSubject.getPrincipals());

        return result;
    }

    public boolean logout() throws LoginException {
        boolean result = super.logout();

        subject.getPrincipals().removeAll(localSubject.getPrincipals());
        localSubject.getPrincipals().clear();

        return result;
    }
}