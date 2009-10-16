/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Principal;

import javax.security.auth.x500.X500Principal;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.PrimaryDomainPrincipal;
import org.apache.geronimo.security.PrimaryPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * A collection of utility functions that assist with the configuration of
 * <code>PolicyConfiguration</code>s.
 *
 * @version $Rev$ $Date$
 * @see javax.security.jacc.PolicyConfiguration
 * @see "JSR 115" Java Authorization Contract for Containers
 */
public class ConfigurationUtil {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationUtil.class);

    /**
     * Create an X500Principal from a deployment description.
     *
     * @param name the distinguished name of the principal
     * @return an X500Principal from a deployment description
     */
    public static X500Principal generateX500Principal(String name) {
        return new X500Principal(name);
    }

    /**
     * Create a Principal from a deployment description.
     *
     * @param principalInfo the deployment description of the principal to be created.
     * @param classLoader
     * @return a RealmPrincipal from a deployment description
     */
    public static Principal generatePrincipal(final PrincipalInfo principalInfo, ClassLoader classLoader) {
        return generatePrincipal(principalInfo.getClassName(), principalInfo.getPrincipalName(), classLoader);
    }

    public static Principal generatePrincipal(final String className, final String principalName, final ClassLoader classLoader) {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Principal>() {
                public Principal run() throws Exception {
                    Class<Principal> clazz = (Class<Principal>) classLoader.loadClass(className);
                    try {
                        Constructor<Principal> constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                        return constructor.newInstance(new Object[]{principalName});
                    } catch (NoSuchMethodException e) {
                        Constructor<Principal>[] constructors = (Constructor<Principal>[])clazz.getDeclaredConstructors();
                        for (Constructor<Principal> constructor: constructors) {
                            Class<?>[] paramTypes = constructor.getParameterTypes();
                            if (paramTypes.length == 0) {
                                Principal p = constructor.newInstance();
                                Method m = clazz.getMethod("setName", String.class);
                                m.invoke(p, principalName);
                                return p;
                            }
                            if (paramTypes[0] == String.class) {
                                Object[] params = new Object[paramTypes.length];
                                params[0] = principalName;
                                return constructor.newInstance(params);
                            }
                        }
                        throw new RuntimeException("Could not construct principal of class: " + className);
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace();
            if (e.getException() != null) {
                log.info("PrivilegedActionException containing", e.getException());
            } else {
                log.info("PrivilegedActionException", e);
            }
            return null;
        }
    }

    /**
     * Create a RealmPrincipal from a deployment description.
     *
     * @param principalInfo the deployment description of the principal to be created.
     * @param classLoader
     * @return a RealmPrincipal from a deployment description
     */
    public static RealmPrincipal generateRealmPrincipal(final String realm, final String loginDomain, final PrincipalInfo principalInfo, ClassLoader classLoader) {
        return generateRealmPrincipal(realm, loginDomain, principalInfo.getClassName(), principalInfo.getPrincipalName(), classLoader);
    }

    public static RealmPrincipal generateRealmPrincipal(final String realm, final String loginDomain, final String className, final String principalName,
                                                        ClassLoader classLoader)
    {
        return new RealmPrincipal(realm, loginDomain, generatePrincipal(className, principalName, classLoader));
    }

    /**
     * Create a DomainPrincipal from a deployment description.
     *
     * @param principalInfo the deployment description of the principal to be created.
     * @param classLoader
     * @return a RealmPrincipal from a deployment description
     */
    public static DomainPrincipal generateDomainPrincipal(final String loginDomain, final PrincipalInfo principalInfo, ClassLoader classLoader) {
        return generateDomainPrincipal(loginDomain, principalInfo.getClassName(), principalInfo.getPrincipalName(), classLoader);
    }

    public static DomainPrincipal generateDomainPrincipal(final String loginDomain, final String className, final String principalName, ClassLoader classLoader) {
        return new DomainPrincipal(loginDomain, generatePrincipal(className, principalName, classLoader));
    }

    /**
     * Create a RealmPrincipal from a deployment description.
     *
     * @param principalInfo the deployment description of the principal to be created.
     * @param classLoader
     * @return a PrimaryRealmPrincipal from a deployment description
     */
    public static PrimaryRealmPrincipal generatePrimaryRealmPrincipal(final String realm, final String domain, final PrincipalInfo principalInfo, ClassLoader classLoader) throws DeploymentException {
        return generatePrimaryRealmPrincipal(realm, domain, principalInfo.getClassName(), principalInfo.getPrincipalName(), classLoader);
    }

    public static PrimaryRealmPrincipal generatePrimaryRealmPrincipal(final String realm, final String domain, final String className, final String principalName,
                                                                      final ClassLoader classLoader) throws DeploymentException
    {
        try {
            return (PrimaryRealmPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    Principal p = null;
                    Class clazz = classLoader.loadClass(className);
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (Principal) constructor.newInstance(new Object[]{principalName});

                    return new PrimaryRealmPrincipal(realm, domain, p);
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new DeploymentException("Unable to create realm principal", pae.getException());
        }
    }

    /**
     * Create a DomainPrincipal from a deployment description.
     *
     * @param principalInfo the deployment description of the principal to be created.
     * @param classLoader
     * @return a PrimaryDomainPrincipal from a deployment description
     */
    public static PrimaryDomainPrincipal generatePrimaryDomainPrincipal(final String domain, final PrincipalInfo principalInfo, ClassLoader classLoader) throws DeploymentException {
        return generatePrimaryDomainPrincipal(domain, principalInfo.getClassName(), principalInfo.getPrincipalName(), classLoader);
    }

    public static PrimaryDomainPrincipal generatePrimaryDomainPrincipal(final String domain, final String className, final String principalName,
                                                                        final ClassLoader classLoader) throws DeploymentException
    {
        try {
            return (PrimaryDomainPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    Principal p = null;
                    Class clazz = classLoader.loadClass(className);
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (Principal) constructor.newInstance(new Object[]{principalName});

                    return new PrimaryDomainPrincipal(domain, p);
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new DeploymentException("Unable to create domain principal", pae.getException());
        }
    }

    /**
     * Create a Principal from a deployment description.
     *
     * @param principalInfo the deployment description of the principal to be created.
     * @param classLoader
     * @return a Principal from a deployment description
     */
    public static PrimaryPrincipal generatePrimaryPrincipal(final PrincipalInfo principalInfo, ClassLoader classLoader) throws DeploymentException {
        return generatePrimaryPrincipal(principalInfo.getClassName(), principalInfo.getPrincipalName(), classLoader);
    }

    public static PrimaryPrincipal generatePrimaryPrincipal(final String className, final String principalName, final ClassLoader classLoader) throws DeploymentException {
        try {
            return (PrimaryPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    Principal p = null;
                    Class clazz = classLoader.loadClass(className);
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (Principal) constructor.newInstance(new Object[]{principalName});

                    return new PrimaryPrincipal(p);
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new DeploymentException("Unable to create principal", pae.getException());
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
