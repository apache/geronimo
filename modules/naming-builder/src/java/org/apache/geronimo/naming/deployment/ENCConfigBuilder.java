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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.PortComponentRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefHandlerType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.apache.geronimo.xbeans.j2ee.XsdStringType;

/**
 * @version $Rev$ $Date$
 */
public class ENCConfigBuilder {

    public static ObjectName getGBeanId(String j2eeType, GerGbeanLocatorType gerGbeanLocator, J2eeContext j2eeContext, DeploymentContext context, Kernel kernel) throws DeploymentException {
        ObjectName containerId = null;
        if (gerGbeanLocator.isSetGbeanLink()) {
            //exact match
            String linkName = gerGbeanLocator.getGbeanLink().trim();
            ObjectName exact = null;
            try {
                exact = NameFactory.getComponentName(null, null, null, null, linkName, j2eeType, j2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not construct gbean name", e);
            }
            if (context.listGBeans(exact).size() == 1) {
                containerId = exact;
            } else {
                ObjectName query = null;
                try {
                    query = NameFactory.getComponentNameQuery(null, null, null, linkName, j2eeType, j2eeContext);
                } catch(MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct query for gbean name", e);
                }
                Set localMatches = context.listGBeans(query);
                if (localMatches.size() > 1) {
                    throw new DeploymentException("More than one local match for gbean link, " + localMatches);
                }
                if (localMatches.size() == 1) {
                    containerId = (ObjectName) localMatches.iterator().next();
                }
                if (containerId == null) {
                    try {
                        query = NameFactory.getComponentRestrictedQueryName(null, null, linkName, j2eeType, j2eeContext);
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("Could not construct query for gbean name", e);
                    }
                    Set matches = kernel.listGBeans(query);
                    if (matches.size() != 1) {
                        throw new DeploymentException("No or ambiguous match for gbean link: " + linkName + " using query " + query + ", matches: " + matches);
                    }
                    containerId = (ObjectName) matches.iterator().next();
                }
            }
        } else if (gerGbeanLocator.isSetTargetName()) {
            try {
                containerId = ObjectName.getInstance(getStringValue(gerGbeanLocator.getTargetName()));
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not construct object name from specified string", e);
            }
        } else {
            //construct name from components
            try {
                containerId = NameFactory.getComponentName(getStringValue(gerGbeanLocator.getDomain()),
                        getStringValue(gerGbeanLocator.getServer()),
                        getStringValue(gerGbeanLocator.getApplication()),
                        getStringValue(gerGbeanLocator.getModule()),
                        getStringValue(gerGbeanLocator.getName()),
                        j2eeType,
                        j2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("could not construct object name for jms resource", e);
            }
        }
        return containerId;
    }


    public static void addEnvEntries(EnvEntryType[] envEntries, ComponentContextBuilder builder, ClassLoader classLoader) throws DeploymentException {
        for (int i = 0; i < envEntries.length; i++) {
            EnvEntryType envEntry = envEntries[i];
            String name = getStringValue(envEntry.getEnvEntryName());
            String type = getStringValue(envEntry.getEnvEntryType());
            String text = getStringValue(envEntry.getEnvEntryValue());
            try {
                builder.addEnvEntry(name, type, text, classLoader);
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid env-entry value for name: " + name, e);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid env-entry definition for name: " + name, e);
            }
        }

    }

    public static void addResourceRefs(EARContext earContext, URI uri, ResourceRefType[] resourceRefs, Map refMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        if (refMap == null) {
            refMap = Collections.EMPTY_MAP;
        }
        RefContext refContext = earContext.getRefContext();
        J2eeContext j2eeContext = earContext.getJ2eeContext();

        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceRefType resourceRef = resourceRefs[i];
            String name = getStringValue(resourceRef.getResRefName());
            String type = getStringValue(resourceRef.getResType());
            GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(name);
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            Reference ref = null;
            if (iface == URL.class) {
                if (gerResourceRef == null || !gerResourceRef.isSetUrl()) {
                    throw new DeploymentException("No url supplied to resolve: " + name);
                }
                try {
                    //TODO expose jsr-77 objects for these guys
                    builder.bind(name, new URL(gerResourceRef.getUrl()));
                } catch (MalformedURLException e) {
                    throw  new DeploymentException("Could not convert " + gerResourceRef.getUrl() + " to URL", e);
                } catch (NamingException e) {
                    throw  new DeploymentException("Could not bind " + name, e);
                }
            } else {
                //determine jsr-77 type from interface
                String j2eeType;
                if ("javax.mail.Session".equals(type)) {
                    j2eeType = NameFactory.JAVA_MAIL_RESOURCE;
                } else {
                    j2eeType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY;
                }
                String containerId = getResourceContainerId(name, j2eeType, uri, gerResourceRef, refContext, j2eeContext, earContext);

                ref = refContext.getConnectionFactoryRef(containerId, iface);
                try {
                    builder.bind(name, ref);
                } catch (NamingException e) {
                    throw new DeploymentException("Invalid resource-ref definition for name: " + name, e);
                }
            }
        }

    }

    private static String getResourceContainerId(String name, String type, URI uri, GerResourceRefType gerResourceRef, RefContext refContext, J2eeContext j2eeContext, DeploymentContext context) throws DeploymentException {
        String containerId = null;
        if (gerResourceRef == null) {
            //try to resolve ref based only matching resource-ref-name
            //throws exception if it can't locate ref.
            containerId = refContext.getConnectionFactoryContainerId(uri, name, type, j2eeContext, context);
        } else if (gerResourceRef.isSetResourceLink()) {
            containerId = refContext.getConnectionFactoryContainerId(uri, gerResourceRef.getResourceLink().trim(), type, j2eeContext, context);
        } else if (gerResourceRef.isSetTargetName()) {
            containerId =  gerResourceRef.getTargetName().trim();
        } else {
            //construct name from components
            try {
                containerId = NameFactory.getResourceComponentNameString(getStringValue(gerResourceRef.getDomain()),
                        getStringValue(gerResourceRef.getServer()),
                        getStringValue(gerResourceRef.getApplication()),
                        getStringValue(gerResourceRef.getModule()),
                        getStringValue(gerResourceRef.getName()),
                        gerResourceRef.getType() == null ? type : gerResourceRef.getType().trim(),
                        j2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("could not construct object name for resource", e);
            }
        }
        return containerId;
    }

    public static void addResourceEnvRefs(EARContext earContext, URI uri, ResourceEnvRefType[] resourceEnvRefArray, Map refMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        if (refMap == null) {
            refMap = Collections.EMPTY_MAP;
        }
        RefContext refContext = earContext.getRefContext();
        J2eeContext j2eeContext = earContext.getJ2eeContext();

        for (int i = 0; i < resourceEnvRefArray.length; i++) {
            ResourceEnvRefType resourceEnvRef = resourceEnvRefArray[i];
            String name = getStringValue(resourceEnvRef.getResourceEnvRefName());
            String type = getStringValue(resourceEnvRef.getResourceEnvRefType());
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            GerResourceEnvRefType gerResourceEnvRef = (GerResourceEnvRefType) refMap.get(name);
            String containerId = getAdminObjectContainerId(name, uri, gerResourceEnvRef, refContext, j2eeContext);
            Reference ref = refContext.getAdminObjectRef(containerId, iface);

            try {
                builder.bind(name, ref);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid resource-ref definition for name: " + name, e);
            }
        }
    }

    private static String getAdminObjectContainerId(String name, URI uri, GerResourceEnvRefType gerResourceEnvRef, RefContext refContext, J2eeContext j2eeContext) throws DeploymentException {
        String containerId = null;
        if (gerResourceEnvRef == null) {
            //try to resolve ref based only matching resource-ref-name
            //throws exception if it can't locate ref.
            containerId = refContext.getAdminObjectContainerId(uri, name, j2eeContext);
        } else if (gerResourceEnvRef.isSetMessageDestinationLink()) {
            containerId = refContext.getAdminObjectContainerId(uri, getStringValue(gerResourceEnvRef.getMessageDestinationLink()), j2eeContext);
        } else if (gerResourceEnvRef.isSetTargetName()) {
            containerId = getStringValue(gerResourceEnvRef.getTargetName());
        } else {
            //construct name from components
            try {
                containerId = NameFactory.getResourceComponentNameString(getStringValue(gerResourceEnvRef.getDomain()),
                        getStringValue(gerResourceEnvRef.getServer()),
                        getStringValue(gerResourceEnvRef.getApplication()),
                        getStringValue(gerResourceEnvRef.getModule()),
                        getStringValue(gerResourceEnvRef.getName()),
                        NameFactory.JMS_RESOURCE,
                        //gerResourceEnvRef.getType(),
                        j2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("could not construct object name for jms resource", e);
            }
        }
        return containerId;
    }

    public static void addMessageDestinationRefs(EARContext earContext, URI uri, MessageDestinationRefType[] messageDestinationRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        for (int i = 0; i < messageDestinationRefs.length; i++) {
            MessageDestinationRefType messageDestinationRef = messageDestinationRefs[i];
            String name = getStringValue(messageDestinationRef.getMessageDestinationRefName());
            String linkName = getStringValue(messageDestinationRef.getMessageDestinationLink());
            String type = getStringValue(messageDestinationRef.getMessageDestinationType());
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            //try to resolve ref based only matching resource-ref-name
            //throws exception if it can't locate ref.
            String containerId = refContext.getAdminObjectContainerId(uri, linkName, earContext.getJ2eeContext());
            Reference ref = refContext.getAdminObjectRef(containerId, iface);
            try {
                builder.bind(name, ref);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid message-destination-ref definition for name: " + name, e);
            }

        }

    }

    public static void addEJBRefs(EARContext earContext, URI uri, EjbRefType[] ejbRefs, Map ejbRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        J2eeContext j2eeContext = earContext.getJ2eeContext();
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];

            String ejbRefName = getStringValue(ejbRef.getEjbRefName());

            String remote = getStringValue(ejbRef.getRemote());
            assureEJBObjectInterface(remote, cl);

            String home = getStringValue(ejbRef.getHome());
            assureEJBHomeInterface(home, cl);

            boolean isSession = "Session".equals(getStringValue(ejbRef.getEjbRefType()));

            String ejbLink = null;
            GerEjbRefType remoteRef = (GerEjbRefType) ejbRefMap.get(ejbRefName);
            if (remoteRef != null && remoteRef.isSetEjbLink()) {
                ejbLink = remoteRef.getEjbLink();
            } else if (ejbRef.isSetEjbLink()) {
                ejbLink = getStringValue(ejbRef.getEjbLink());
            }

            Reference ejbReference;
            if (ejbLink != null) {
                ejbReference = refContext.getEJBRemoteRef(uri, ejbLink, isSession, home, remote);
            } else if (remoteRef != null) {
                if (remoteRef.isSetTargetName()) {
                    ejbReference = refContext.getEJBRemoteRef(getStringValue(remoteRef.getTargetName()), isSession, home, remote);
                } else {
                    String containerId = null;
                    try {
                        containerId = NameFactory.getEjbComponentNameString(getStringValue(remoteRef.getDomain()),
                                getStringValue(remoteRef.getServer()),
                                getStringValue(remoteRef.getApplication()),
                                getStringValue(remoteRef.getModule()),
                                getStringValue(remoteRef.getName()),
                                getStringValue(remoteRef.getType()),
                                j2eeContext);
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("Could not construct ejb object name: " + remoteRef.getName(), e);
                    }
                    ejbReference = refContext.getEJBRemoteRef(containerId, isSession, home, remote);

                }
            } else {
                ejbReference = refContext.getImplicitEJBRemoteRef(uri, ejbRefName, isSession, home, remote);
            }
            try {
                builder.bind(ejbRefName, ejbReference);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-ref: ejb-ref-name=" + ejbRefName);
            }
        }
    }

    public static void addEJBLocalRefs(EARContext earContext, URI uri, EjbLocalRefType[] ejbLocalRefs, Map ejbLocalRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        J2eeContext j2eeContext = earContext.getJ2eeContext();
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

            String ejbRefName = getStringValue(ejbLocalRef.getEjbRefName());

            String local = getStringValue(ejbLocalRef.getLocal());
            assureEJBLocalObjectInterface(local, cl);

            String localHome = getStringValue(ejbLocalRef.getLocalHome());
            assureEJBLocalHomeInterface(localHome, cl);

            boolean isSession = "Session".equals(getStringValue(ejbLocalRef.getEjbRefType()));

            String ejbLink = null;
            GerEjbLocalRefType localRef = (GerEjbLocalRefType) ejbLocalRefMap.get(ejbRefName);
            if (localRef != null && localRef.isSetEjbLink()) {
                ejbLink = localRef.getEjbLink();
            } else if (ejbLocalRef.isSetEjbLink()) {
                ejbLink = getStringValue(ejbLocalRef.getEjbLink());
            }

            Reference ejbReference;
            if (ejbLink != null) {
                ejbReference = refContext.getEJBLocalRef(uri, ejbLink, isSession, localHome, local);
            } else if (localRef != null) {
                if (localRef.isSetTargetName()) {
                    ejbReference = refContext.getEJBLocalRef(getStringValue(localRef.getTargetName()), isSession, localHome, local);
                } else {
                    String containerId = null;
                    try {
                        containerId = NameFactory.getEjbComponentNameString(getStringValue(localRef.getDomain()),
                                getStringValue(localRef.getServer()),
                                getStringValue(localRef.getApplication()),
                                getStringValue(localRef.getModule()),
                                getStringValue(localRef.getName()),
                                getStringValue(localRef.getType()),
                                j2eeContext);
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("Could not construct ejb object name: " + localRef.getName(), e);
                    }
                    ejbReference = refContext.getEJBLocalRef(containerId, isSession, localHome, local);

                }
            } else {
                ejbReference = refContext.getImplicitEJBLocalRef(uri, ejbLink, isSession, localHome, local);
            }
            try {
                builder.bind(ejbRefName, ejbReference);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-local-ref: ejb-ref-name=" + ejbRefName);
            }
        }
    }

    //TODO current implementation does not deal with portComponentRefs or handlers.
    public static void addServiceRefs(EARContext earContext, Module module, ServiceRefType[] serviceRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();

        for (int i = 0; i < serviceRefs.length; i++) {
            ServiceRefType serviceRef = serviceRefs[i];
            String name = getStringValue(serviceRef.getServiceRefName());
            String serviceInterfaceName = getStringValue(serviceRef.getServiceInterface());
            assureInterface(serviceInterfaceName, "javax.xml.rpc.Service", "[Web]Service", cl);
            Class serviceInterface = null;
            try {
                serviceInterface = cl.loadClass(serviceInterfaceName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load service interface class: " + serviceInterfaceName, e);
            }
            URI wsdlURI = null;
            if (serviceRef.isSetWsdlFile()) {
                try {
                    wsdlURI = new URI(getStringValue(serviceRef.getWsdlFile().getStringValue()));
                } catch (URISyntaxException e) {
                    throw new DeploymentException("could not construct wsdl uri from " + serviceRef.getWsdlFile().getStringValue(), e);
                }
            }
            URI jaxrpcMappingURI = null;
            if (serviceRef.isSetJaxrpcMappingFile()) {
                try {
                    jaxrpcMappingURI = new URI(getStringValue(serviceRef.getJaxrpcMappingFile()));
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Could not construct jaxrpc mapping uri from " + serviceRef.getJaxrpcMappingFile(), e);
                }
            }
            QName serviceQName = null;
            if (serviceRef.isSetServiceQname()) {
                serviceQName = serviceRef.getServiceQname().getQNameValue();
            }
            Map portComponentRefMap = new HashMap();
            PortComponentRefType[] portComponentRefs = serviceRef.getPortComponentRefArray();
            if (portComponentRefs != null) {
                for (int j = 0; j < portComponentRefs.length; j++) {
                    PortComponentRefType portComponentRef = portComponentRefs[j];
                    String portComponentLink = getStringValue(portComponentRef.getPortComponentLink());
                    String serviceEndpointInterfaceType = getStringValue(portComponentRef.getServiceEndpointInterface());
                    assureInterface(serviceEndpointInterfaceType, "java.rmi.Remote", "ServiceEndpoint", cl);
                    Class serviceEndpointClass;
                    try {
                        serviceEndpointClass = cl.loadClass(serviceEndpointInterfaceType);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("could not load service endpoint class " + serviceEndpointInterfaceType, e);
                    }
                    portComponentRefMap.put(serviceEndpointClass, portComponentLink);
                }
            }
            //TODO this sucks, but the handlers aren't implemented yet anyway.
            ServiceRefHandlerType[] handlerTypes = serviceRef.getHandlerArray();
            List handlers = Arrays.asList(handlerTypes);

            //we could get a Reference or the actual serializable Service back.
            Object ref = refContext.getServiceReference(serviceInterface, wsdlURI, jaxrpcMappingURI, serviceQName, portComponentRefMap, handlers, earContext, module, cl);
            try {
                builder.bind(name, ref);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid resource-ref definition for name: " + name, e);
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

    public static void assureInterface(String interfaceName, String superInterfaceName, String interfaceType, ClassLoader cl) throws DeploymentException {
        Class clazz = null;
        try {
            clazz = cl.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(interfaceType + " interface class not found: " + interfaceName);
        }
        if (!clazz.isInterface()) {
            throw new DeploymentException(interfaceType + " interface is not an interface: " + interfaceName);
        }
        Class superInterface = null;
        try {
            superInterface = cl.loadClass(superInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Class " + superInterfaceName + " could not be loaded");
        }
        if (!superInterface.isAssignableFrom(clazz)) {
            throw new DeploymentException(interfaceType + " interface does not extend " + superInterfaceName + ": " + interfaceName);
        }
    }

    private static String getStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        String s = string.getStringValue();
        return s == null ? null : s.trim();
    }

    private static String getStringValue(XsdStringType string) {
        if (string == null) {
            return null;
        }
        String s = string.getStringValue();
        return s == null ? null : s.trim();
    }

    private static String getStringValue(String string) {
        return string == null ? null : string.trim();
    }


    public static void setResourceEnvironment(EARContext earContext, URI uri, ResourceEnvironmentBuilder builder, ResourceRefType[] resourceRefs, GerResourceRefType[] gerResourceRefs) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        J2eeContext j2eeContext = earContext.getJ2eeContext();
        Map refMap = mapResourceRefs(gerResourceRefs);
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceRefType resourceRefType = resourceRefs[i];

            String type = resourceRefType.getResType().getStringValue().trim();
            if (!URL.class.getName().equals(type)
                    && !"javax.mail.Session".equals(type)) {
                GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(resourceRefType.getResRefName().getStringValue());
                String containerId = getResourceContainerId(getStringValue(resourceRefType.getResRefName()), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, uri, gerResourceRef, refContext, j2eeContext, earContext);
                if ("Unshareable".equals(getStringValue(resourceRefType.getResSharingScope()))) {
                    unshareableResources.add(containerId);
                }
                if ("Application".equals(getStringValue(resourceRefType.getResAuth()))) {
                    applicationManagedSecurityResources.add(containerId);
                }
            }
        }
        builder.setUnshareableResources(unshareableResources);
        builder.setApplicationManagedSecurityResources(applicationManagedSecurityResources);
    }

    public static Map buildComponentContext(EARContext earContext, Module module, UserTransaction userTransaction, EnvEntryType[] envEntries, EjbRefType[] ejbRefs, GerEjbRefType[] gerEjbRefs, EjbLocalRefType[] ejbLocalRefs, GerEjbLocalRefType[] gerEjbLocalRef, ResourceRefType[] resourceRefs, GerResourceRefType[] gerResourceRef, ResourceEnvRefType[] resourceEnvRefs, GerResourceEnvRefType[] gerResourceEnvRef, MessageDestinationRefType[] messageDestinationRefs, ServiceRefType[] serviceRefs, ClassLoader cl) throws DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder();

        if (userTransaction != null) {
            try {
                builder.addUserTransaction(userTransaction);
            } catch (NamingException e) {
                throw new DeploymentException("Could not bind UserTransaction", e);
            }
        }

        URI uri = module.getConfigId();

        addEnvEntries(envEntries, builder, cl);

        // ejb-ref
        addEJBRefs(earContext, uri, ejbRefs, mapEjbRefs(gerEjbRefs), cl, builder);

        // ejb-local-ref
        addEJBLocalRefs(earContext, uri, ejbLocalRefs, mapEjbLocalRefs(gerEjbLocalRef), cl, builder);

        // resource-ref
        addResourceRefs(earContext, uri, resourceRefs, mapResourceRefs(gerResourceRef), cl, builder);

        // resource-env-ref
        addResourceEnvRefs(earContext, uri, resourceEnvRefs, mapResourceEnvRefs(gerResourceEnvRef), cl, builder);

        addMessageDestinationRefs(earContext, uri, messageDestinationRefs, cl, builder);

        addServiceRefs(earContext, module, serviceRefs, cl, builder);

        return builder.getContext();
    }

    private static Map mapEjbRefs(GerEjbRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
    }

    private static Map mapEjbLocalRefs(GerEjbLocalRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbLocalRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
    }

    private static Map mapResourceRefs(GerResourceRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerResourceRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
    }

    private static Map mapResourceEnvRefs(GerResourceEnvRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerResourceEnvRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
    }


}
