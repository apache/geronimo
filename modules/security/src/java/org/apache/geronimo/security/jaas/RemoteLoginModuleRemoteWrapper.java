/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * A wrapper used by the Geronimo security system to make sure that the
 * principals that are put into the subject get copied into RealmPrincipals
 * which, in turn, also get placed into the subject.  It is these RealmPrincipals
 * that are used in the principal to role mapping.
 *
 * @version $Rev$ $Date$
 */
public class RemoteLoginModuleRemoteWrapper implements LoginModule {
    private String realm;
    private LoginModule module;
    private Subject internalSubject = new Subject();
    private Subject externalSubject;
    private static ClassLoader classLoader;

    static {
        classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

        externalSubject = subject;
        realm = (String) options.get(LoginModuleConstants.REALM_NAME);
        try {
            final String finalClass = (String) options.get(LoginModuleConstants.MODULE);
            module = (LoginModule) java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                    return Class.forName(finalClass, true, classLoader).newInstance();
                }
            });
            module.initialize(internalSubject, callbackHandler, sharedState, options);
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof InstantiationException) {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error:" + e.getCause().getMessage() + "\n").initCause(e.getCause());
            } else {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString() + "\n").initCause(e);
            }
        }
    }

    public boolean login() throws LoginException {
        return module.login();
    }

    public boolean commit() throws LoginException {

        if (!module.commit()) return false;

        RealmPrincipal principal;
        Set set = internalSubject.getPrincipals();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            principal = new RealmPrincipal(realm, (Principal) iter.next());
            externalSubject.getPrincipals().add(principal);
        }
        externalSubject.getPrincipals().addAll(internalSubject.getPrincipals());
        externalSubject.getPrivateCredentials().addAll(internalSubject.getPrivateCredentials());
        externalSubject.getPublicCredentials().addAll(internalSubject.getPublicCredentials());

        return true;
    }

    public boolean abort() throws LoginException {
        return module.abort();
    }

    public boolean logout() throws LoginException {
        Iterator pricipals = externalSubject.getPrincipals().iterator();
        while (pricipals.hasNext()) {
            Object o = pricipals.next();
            if (o instanceof RealmPrincipal) pricipals.remove();
            if (internalSubject.getPrincipals().contains(o)) pricipals.remove();
        }

        Iterator privateCredentials = externalSubject.getPrivateCredentials().iterator();
        while (privateCredentials.hasNext()) {
            Object o = privateCredentials.next();
            if (internalSubject.getPrivateCredentials().contains(o)) privateCredentials.remove();
        }

        Iterator publicCredentials = externalSubject.getPublicCredentials().iterator();
        while (publicCredentials.hasNext()) {
            Object o = publicCredentials.next();
            if (internalSubject.getPublicCredentials().contains(o)) publicCredentials.remove();
        }
        return module.logout();
    }
}
