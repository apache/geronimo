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

package org.apache.geronimo.naming.deployment;

import java.util.Map;
import java.lang.String;
import java.net.URI;

import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.xbeans.j2ee.*;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.j2ee.deployment.EARContext;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ENCConfigBuilder {

    public static void addEnvEntries(EnvEntryType[] envEntries, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < envEntries.length; i++) {
            EnvEntryType envEntry = envEntries[i];
            String name = envEntry.getEnvEntryName().getStringValue();
            String type = envEntry.getEnvEntryType().getStringValue();
            String text = envEntry.getEnvEntryValue().getStringValue();
            try {
                builder.addEnvEntry(name, type, text);
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid env-entry value for name: " + name, e);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid env-entry definition for name: " + name, e);
            }
        }

    }

    public static void addResourceRefs(ResourceRefType[] resourceRefs, ClassLoader cl, Map refAdapterMap, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceRefType resourceRef = resourceRefs[i];
            String name = resourceRef.getResRefName().getStringValue();
            String type = resourceRef.getResType().getStringValue();
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            RefAdapter refAdapter = (RefAdapter) refAdapterMap.get(name);
            if (refAdapter == null) {
                throw  new DeploymentException("No geronimo configuration for resource ref named: " + name);
            }
            try {
                builder.addResourceRef(name, iface, refAdapter);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid resource-ref definition for name: " + name, e);
            }
        }

    }

    public static void addResourceEnvRefs(ResourceEnvRefType[] resourceEnvRefArray, ClassLoader cl, Map refAdapterMap, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < resourceEnvRefArray.length; i++) {
            ResourceEnvRefType resourceEnvRef = resourceEnvRefArray[i];
            String name = resourceEnvRef.getResourceEnvRefName().getStringValue();
            String type = resourceEnvRef.getResourceEnvRefType().getStringValue();
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            RefAdapter refAdapter = (RefAdapter) refAdapterMap.get(name);
            if (refAdapter == null) {
                throw  new DeploymentException("No geronimo configuration for resource env ref named: " + name);
            }
            try {
                builder.addResourceEnvRef(name, iface, refAdapter);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid resource-env-ref definition for name: " + name, e);
            }
        }
    }

    public static void addMessageDestinationRefs(MessageDestinationRefType[] messageDestinationRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < messageDestinationRefs.length; i++) {
            MessageDestinationRefType messageDestinationRef = messageDestinationRefs[i];
            String name = messageDestinationRef.getMessageDestinationRefName().getStringValue();
            String linkName = messageDestinationRef.getMessageDestinationLink().getStringValue();
            String type = messageDestinationRef.getMessageDestinationType().getStringValue();
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            try {
                builder.addMessageDestinationRef(name, linkName, iface);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid message-destination-ref definition for name: " + name, e);
            }

        }

    }

    public static void addEJBRefs(EARContext earContext, URI uri, EjbRefType[] ejbRefs, Map ejbRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];

            String ejbRefName = ejbRef.getEjbRefName().getStringValue();

            String remote = ejbRef.getRemote().getStringValue();
            assureEJBObjectInterface(remote, cl);

            String home = ejbRef.getHome().getStringValue();
            assureEJBHomeInterface(home, cl);

            boolean isSession = "Session".equals(ejbRef.getEjbRefType().getStringValue());

            String ejbLink = getJ2eeStringValue(ejbRef.getEjbLink());
            if (ejbLink != null) {
                try {
                    builder.bind(ejbRefName, earContext.getEJBRef(uri, ejbLink));
                } catch (NamingException e) {
                    throw new DeploymentException("Unable to to bind ejb-ref: ejb-ref-name=" + ejbRefName);
                }
            } else {
                RefAdapter refAdapter = (RefAdapter) ejbRefMap.get(ejbRefName);
                if (refAdapter == null) {
                    throw  new DeploymentException("No geronimo configuration for resource ref named: " + ejbRefName);
                }
                try {
                    builder.bind(ejbRefName, earContext.createEJBRemoteReference(refAdapter.getTargetName(), isSession, home, remote));
                } catch (NamingException e) {
                    throw new DeploymentException("Invalid env-entry definition for name: " + ejbRefName, e);
                }
            }

        }
    }

    public static void addEJBLocalRefs(EARContext earContext, URI uri, EjbLocalRefType[] ejbLocalRefs, Map ejbLocalRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

            String ejbRefName = ejbLocalRef.getEjbRefName().getStringValue();

            String local = ejbLocalRef.getLocal().getStringValue();
            assureEJBLocalObjectInterface(local, cl);

            String localHome = ejbLocalRef.getLocalHome().getStringValue();
            assureEJBLocalHomeInterface(localHome, cl);

            boolean isSession = "Session".equals(ejbLocalRef.getEjbRefType().getStringValue());

            String ejbLink = getJ2eeStringValue(ejbLocalRef.getEjbLink());
            if (ejbLink != null) {
                try {
                    builder.bind(ejbRefName, earContext.getEJBLocalRef(uri, ejbLink));
                } catch (NamingException e) {
                    throw new DeploymentException("Unable to to bind ejb-local-ref: ejb-ref-name=" + ejbRefName);
                }
            } else {
                RefAdapter refAdapter = (RefAdapter) ejbLocalRefMap.get(ejbRefName);
                if (refAdapter == null) {
                    throw  new DeploymentException("No geronimo configuration for resource ref named: " + ejbRefName);
                }
                try {
                    builder.bind(ejbRefName, earContext.createEJBLocalReference(refAdapter.getTargetName(), isSession, localHome, local));
                } catch (NamingException e) {
                    throw new DeploymentException("Invalid env-entry definition for name: " + ejbRefName, e);
                }
            }


        }
    }

    public static void assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    public static void assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    public static void assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    public static void assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }

    public static void assureInterface(String interfaceName, String superInterfaceName, String interfactType, ClassLoader cl) throws DeploymentException {
        Class clazz = null;
        try {
            clazz = cl.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(interfactType + " interface class not found: " + interfaceName);
        }
        if (!clazz.isInterface()) {
            throw new DeploymentException(interfactType + " interface is not an interface: " + interfaceName);
        }
        Class superInterface = null;
        try {
            superInterface = cl.loadClass(superInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Class " + superInterfaceName + " could not be loaded");
        }
        if (clazz.isAssignableFrom(superInterface)) {
            throw new DeploymentException(interfactType + " interface does not extend " + superInterfaceName + ": " + interfaceName);
        }
    }

    private static String getJ2eeStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
    }

    public static ReadOnlyContext buildComponentContext(EARContext earContext, URI uri, UserTransaction userTransaction, EnvEntryType[] envEntries, EjbRefType[] ejbRefs, Map ejbRefMap, EjbLocalRefType[] ejbLocalRefs, Map ejbLocalRefMap, ResourceRefType[] resourceRefs, Map resourceRefMap, ResourceEnvRefType[] resourceEnvRefs, Map resourceEnvRefMap, MessageDestinationRefType[] messageDestinationRefs, ClassLoader cl) throws DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder(new JMXReferenceFactory());

        if (userTransaction != null) {
            try {
                builder.addUserTransaction(userTransaction);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind UserTransaction", e);
            }
        }

        addEnvEntries(envEntries, builder);

        // ejb-ref
        addEJBRefs(earContext, uri, ejbRefs, ejbRefMap, cl, builder);

        // ejb-local-ref
        addEJBLocalRefs(earContext, uri, ejbLocalRefs, ejbLocalRefMap, cl, builder);

        // resource-ref
        if (!resourceRefMap.isEmpty()) {
            addResourceRefs(resourceRefs, cl, resourceRefMap, builder);
        }

        // resource-env-ref
        if (!resourceEnvRefMap.isEmpty()) {
            addResourceEnvRefs(resourceEnvRefs, cl, resourceEnvRefMap, builder);
        }

        addMessageDestinationRefs(messageDestinationRefs, cl, builder);

        return builder.getContext();
    }
}
