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
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.deploy.Principal;


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
     * Create a RealmPrincipal from a deployment description.
     * @param principal the deployment description of the principal to be created.
     * @param realmName the security realm that the principal belongs go
     * @return a RealmPrincipal from a deployment description
     */
    public static RealmPrincipal generateRealmPrincipal(final Principal principal, final String realmName) {
        try {
            return (RealmPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    java.security.Principal p = null;
                    Class clazz = Class.forName(principal.getClassName());
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (java.security.Principal) constructor.newInstance(new Object[]{principal.getPrincipalName()});

                    return new RealmPrincipal(realmName, p);
                }
            });
        } catch (PrivilegedActionException e) {
            return null;
        }
    }

    /**
     * Create a RealmPrincipal from a deployment description.
     * @param principal the deployment description of the principal to be created.
     * @param realmName the security realm that the principal belongs go
     * @return a RealmPrincipal from a deployment description
     */
    public static PrimaryRealmPrincipal generatePrimaryRealmPrincipal(final Principal principal, final String realmName) {
        try {
            return (PrimaryRealmPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    java.security.Principal p = null;
                    Class clazz = Class.forName(principal.getClassName());
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (java.security.Principal) constructor.newInstance(new Object[]{principal.getPrincipalName()});

                    return new PrimaryRealmPrincipal(realmName, p);
                }
            });
        } catch (PrivilegedActionException e) {
            return null;
        }
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
