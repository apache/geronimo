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

package org.apache.geronimo.security.util;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.Subject;

import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.common.GeronimoSecurityException;


/**
 * A collection of utility functions that assist with the configuration of
 * <code>PolicyConfiguration</code>s.
 *
 * @version $Rev$ $Date$
 * @see javax.security.jacc.PolicyConfiguration
 * @see "JSR 115" Java Authorization Contract for Containers
 */
public class ConfigurationUtil {

    /**
     * Create an X500Principal from a deployment description.
     * @param name the distinguished name of the principal
     * @return an X500Principal from a deployment description
     */
    public static X500Principal generateX500Principal(String name) {
        return new X500Principal(name);
    }

    /**
     * Create a RealmPrincipal from a deployment description.
     * @param principal the deployment description of the principal to be created.
     * @param loginDomain
     * @return a RealmPrincipal from a deployment description
     */
    public static java.security.Principal generateRealmPrincipal(final Principal principal, final String loginDomain, final String realmName) {
        return generateRealmPrincipal(principal.getClassName(), principal.getPrincipalName(), loginDomain, realmName);
    }

    public static java.security.Principal generateRealmPrincipal(final String className, final String principalName, final String loginDomain, final String realmName) {
        try {
            return (java.security.Principal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    java.security.Principal p = null;
                    Class clazz = Class.forName(className);
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (java.security.Principal) constructor.newInstance(new Object[]{principalName});
                    if (loginDomain != null) {
                        p = new DomainPrincipal(loginDomain, p);
                        if (realmName != null) {
                            p = new RealmPrincipal(realmName, p);
                        }
                    }
                    return p;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace();
            if(e.getException() != null) {
                e.getException().printStackTrace();
            }
            return null;
        }
    }

    /**
     * Create a RealmPrincipal from a deployment description.
     * @param principal the deployment description of the principal to be created.
     * @return a RealmPrincipal from a deployment description
     */
    public static PrimaryRealmPrincipal generatePrimaryRealmPrincipal(final Principal principal, final String loginDomain) {
        return generatePrimaryRealmPrincipal(principal.getClassName(), principal.getPrincipalName(), loginDomain);
    }

    public static PrimaryRealmPrincipal generatePrimaryRealmPrincipal(final String className, final String principalName, final String loginDomain) {
        try {
            return (PrimaryRealmPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    java.security.Principal p = null;
                    Class clazz = Class.forName(className);
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (java.security.Principal) constructor.newInstance(new Object[]{principalName});

                    return new PrimaryRealmPrincipal(loginDomain, p);
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace();
            if(e.getException() != null) {
                e.getException().printStackTrace();
            }
            return null;
        }
    }

    /**
     * Generate the default principal from the security config.
     *
     * @param defaultPrincipal
     * @return the default principal
     */
    public static Subject generateDefaultSubject(DefaultPrincipal defaultPrincipal) throws GeronimoSecurityException {
        if (defaultPrincipal == null) {
            throw new GeronimoSecurityException("No DefaultPrincipal configuration supplied");
        }
        Subject defaultSubject = new Subject();

        java.security.Principal realmPrincipal = generateRealmPrincipal(defaultPrincipal.getPrincipal(), defaultPrincipal.getLoginDomain(), defaultPrincipal.getRealmName());
        if (realmPrincipal == null) {
            throw new GeronimoSecurityException("Unable to create realm principal");
        }
        PrimaryRealmPrincipal primaryRealmPrincipal = generatePrimaryRealmPrincipal(defaultPrincipal.getPrincipal(), defaultPrincipal.getRealmName());
        if (primaryRealmPrincipal == null) {
            throw new GeronimoSecurityException("Unable to create primary realm principal");
        }

        defaultSubject.getPrincipals().add(realmPrincipal);
        defaultSubject.getPrincipals().add(primaryRealmPrincipal);

        Set namedUserPasswordCredentials = defaultPrincipal.getNamedUserPasswordCredentials();
        if (namedUserPasswordCredentials != null) {
            defaultSubject.getPrivateCredentials().addAll(namedUserPasswordCredentials);
        }

        return defaultSubject;
    }



    /**
     * A simple helper method to register PolicyContextHandlers
     *
     * @param handler an object that implements the <code>PolicyContextHandler</code>
     *                interface. The value of this parameter must not be null.
     * @param replace this boolean value defines the behavior of this method
     *                if, when it is called, a <code>PolicyContextHandler</code> has already
     *                been registered to handle the same key. In that case, and if the value
     *                of this argument is true, the existing handler is replaced with the
     *                argument handler. If the value of this parameter is false the existing
     *                registration is preserved and an exception is thrown.
     */
    public static void registerPolicyContextHandler(PolicyContextHandler handler, boolean replace) throws PolicyContextException {
        String[] keys = handler.getKeys();

        for (int i = 0; i < keys.length; i++) {
            PolicyContext.registerHandler(keys[i], handler, replace);
        }
    }


}
