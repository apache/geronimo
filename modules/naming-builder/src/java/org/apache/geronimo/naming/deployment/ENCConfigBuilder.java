/**
 *
 * Copyright 2004-2005 The Apache Software Foundation
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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.ParamValueType;
import org.apache.geronimo.xbeans.j2ee.PortComponentRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefHandlerType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.apache.geronimo.xbeans.j2ee.XsdQNameType;
import org.apache.geronimo.xbeans.j2ee.XsdStringType;
import org.apache.geronimo.gbean.AbstractNameQuery;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev:385232 $ $Date$
 */
public class ENCConfigBuilder {

    private static final String JAXR_CONNECTION_FACTORY_CLASS = "javax.xml.registry.ConnectionFactory";

    public static void registerMessageDestinations(RefContext refContext, String moduleName, MessageDestinationType[] specDestinations, GerMessageDestinationType[] destinations) throws DeploymentException {
        Map nameMap = new HashMap();
        for (int i = 0; i < destinations.length; i++) {
            GerMessageDestinationType destination = destinations[i];
            String name = destination.getMessageDestinationName().trim();
            nameMap.put(name, destination);
            boolean found = false;
            for (int j = 0; j < specDestinations.length; j++) {
                MessageDestinationType specDestination = specDestinations[j];
                if (specDestination.getMessageDestinationName().getStringValue().trim().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new DeploymentException("No spec DD message-destination for " + name);
            }
        }
        refContext.registerMessageDestionations(moduleName, nameMap);
    }


    public static AbstractNameQuery getGBeanId(String j2eeType, GerGbeanLocatorType gerGbeanLocator, DeploymentContext context) throws DeploymentException {
        AbstractNameQuery abstractNameQuery;
        if (gerGbeanLocator.isSetGbeanLink()) {
            //exact match
            String linkName = gerGbeanLocator.getGbeanLink().trim();
            abstractNameQuery = buildAbstractNameQuery(null, j2eeType, linkName);

        } else {
            GerPatternType patternType = gerGbeanLocator.getPattern();
            //construct name from components
            abstractNameQuery = buildAbstractNameQuery(patternType, j2eeType);
        }
        //TODO check that the query is satisfied.
        return abstractNameQuery;
    }


    static void addEnvEntries(EnvEntryType[] envEntries, ComponentContextBuilder builder, ClassLoader classLoader) throws DeploymentException {
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

    static void addResourceRefs(EARContext earContext, URI moduleURI, ResourceRefType[] resourceRefs, Map refMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        if (refMap == null) {
            refMap = Collections.EMPTY_MAP;
        }
        RefContext refContext = earContext.getRefContext();

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
                    throw new DeploymentException("Could not convert " + gerResourceRef.getUrl() + " to URL", e);
                }
            } else {
                //determine jsr-77 type from interface
                String j2eeType;


                if ("javax.mail.Session".equals(type)) {
                    j2eeType = NameFactory.JAVA_MAIL_RESOURCE;
                } else if (JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {
                    j2eeType = NameFactory.JAXR_CONNECTION_FACTORY;
                } else {
                    j2eeType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY;
                }
                try {
                    AbstractNameQuery containerId = getResourceContainerId(name, j2eeType, moduleURI, gerResourceRef, earContext);

                    //TODO configid not sure what knownParent is supposed to be
                    ref = refContext.getConnectionFactoryRef(containerId, iface, earContext.getConfiguration());
                    builder.bind(name, ref);
                } catch (UnresolvedReferenceException e) {
                    throw new DeploymentException("Unable to resolve resource reference '" + name + "' (" + (e.isMultiple() ? "found multiple matching resources" : "no matching resources found") + ")");
                }
            }
        }

    }

    private static AbstractNameQuery getResourceContainerId(String name, String type, URI moduleURI, GerResourceRefType gerResourceRef, EARContext context) throws DeploymentException {
        AbstractNameQuery containerId;
        String module = moduleURI == null? null: moduleURI.toString();
        RefContext refContext = context.getRefContext();
        if (gerResourceRef == null) {
            containerId = buildAbstractNameQuery(module, type, name);
        } else if (gerResourceRef.isSetResourceLink()) {
            containerId = buildAbstractNameQuery(module, type, gerResourceRef.getResourceLink().trim());
        } else {
            //construct name from components
            GerPatternType patternType = gerResourceRef.getPattern();
            containerId = buildAbstractNameQuery(patternType, type);
        }
        return containerId;
    }

    static void addResourceEnvRefs(EARContext earContext, ResourceEnvRefType[] resourceEnvRefArray, Map refMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        if (refMap == null) {
            refMap = Collections.EMPTY_MAP;
        }

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
            try {
                AbstractNameQuery containerId = getAdminObjectContainerId(name, gerResourceEnvRef, earContext);
                //TODO not sure what knownParent is supposed to be
                Reference ref = earContext.getRefContext().getAdminObjectRef(containerId, iface, earContext.getConfiguration());

                builder.bind(name, ref);
            } catch (UnresolvedReferenceException e) {
                throw new DeploymentException("Unable to resolve resource env reference '" + name + "' (" + (e.isMultiple() ? "found multiple matching resources" : "no matching resources found") + ")");
            }
        }
    }

    private static AbstractNameQuery getAdminObjectContainerId(String name, GerResourceEnvRefType gerResourceEnvRef, EARContext context) throws DeploymentException {
        AbstractNameQuery containerId;
        RefContext refContext = context.getRefContext();
        if (gerResourceEnvRef == null) {
            containerId = buildAbstractNameQuery(null, NameFactory.JCA_ADMIN_OBJECT, name);
        } else if (gerResourceEnvRef.isSetMessageDestinationLink()) {
            containerId = buildAbstractNameQuery(null, NameFactory.JCA_ADMIN_OBJECT, gerResourceEnvRef.getMessageDestinationLink().trim());
        } else if (gerResourceEnvRef.isSetAdminObjectLink()) {
            String moduleURI = null;
            if (gerResourceEnvRef.isSetAdminObjectModule()) {
                    moduleURI = gerResourceEnvRef.getAdminObjectModule().trim();
            }
            containerId = buildAbstractNameQuery(moduleURI, NameFactory.JCA_ADMIN_OBJECT, gerResourceEnvRef.getAdminObjectLink().trim());
        } else {
            //construct name from components
            GerPatternType patternType = gerResourceEnvRef.getPattern();
            containerId = buildAbstractNameQuery(patternType, NameFactory.JCA_ADMIN_OBJECT);
        }
        return containerId;
    }

    static void addMessageDestinationRefs(RefContext refContext, EARContext earContext, MessageDestinationRefType[] messageDestinationRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
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
            String moduleURI = null;
            GerMessageDestinationType destination = (GerMessageDestinationType) refContext.getMessageDestination(linkName);
            if (destination != null) {
                if (destination.isSetAdminObjectLink()) {
                    if (destination.isSetAdminObjectModule()) {
                        moduleURI = destination.getAdminObjectModule().trim();
                    }
                    linkName = destination.getAdminObjectLink().trim();
                }
            } else {
                //well, we know for sure an admin object is not going to be defined in a modules that can have a message-destination
                int pos = linkName.indexOf('#');
                if (pos > -1) {
                    linkName = linkName.substring(pos + 1);
                }
            }

            //try to resolve ref based only matching resource-ref-name
            //throws exception if it can't locate ref.
            AbstractNameQuery containerId = buildAbstractNameQuery(moduleURI, NameFactory.JCA_ADMIN_OBJECT, linkName);
            //TODO configid not sure what knwonParent is suppsed to be
            Reference ref = refContext.getAdminObjectRef(containerId, iface, earContext.getConfiguration());
            builder.bind(name, ref);

        }

    }

    static void addEJBRefs(NamingContext earContext, NamingContext ejbContext, RefContext refContext, URI moduleURI, EjbRefType[] ejbRefs, Map ejbRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];

            String ejbRefName = getStringValue(ejbRef.getEjbRefName());

            String remote = getStringValue(ejbRef.getRemote());
            try {
                assureEJBObjectInterface(remote, cl);
            } catch (DeploymentException e) {
                throw new DeploymentException("Error processing 'remote' element for EJB Reference '" + ejbRefName + "' for module '" + moduleURI + "': " + e.getMessage());
            }

            String home = getStringValue(ejbRef.getHome());
            try {
                assureEJBHomeInterface(home, cl);
            } catch (DeploymentException e) {
                throw new DeploymentException("Error processing 'home' element for EJB Reference '" + ejbRefName + "' for module '" + moduleURI + "': " + e.getMessage());
            }

            Reference ejbReference;
            boolean isSession = "Session".equals(getStringValue(ejbRef.getEjbRefType()));

            if (isSession && remote.equals("javax.management.j2ee.Management") && home.equals("javax.management.j2ee.ManagementHome"))
            {
                String mejbName = refContext.getMEJBName();
                ejbReference = refContext.getEJBRemoteRef(mejbName, isSession, home, remote);
            } else {

                String ejbLink = null;
                GerEjbRefType remoteRef = (GerEjbRefType) ejbRefMap.get(ejbRefName);
                if (remoteRef != null && remoteRef.isSetNsCorbaloc()) {
                    try {
                        AbstractNameQuery cssBean;
                        if (remoteRef.isSetCssLink()) {
                            String cssLink = remoteRef.getCssLink().trim();
                            //TODO is this correct?
                            String moduleType = null;
                            cssBean = buildAbstractNameQuery(null, NameFactory.CORBA_CSS, cssLink);
                        } else {
                            GerPatternType css = remoteRef.getCss();
                            cssBean = buildAbstractNameQuery(css, NameFactory.CORBA_CSS);
                        }
                        //TODO configID this is probably completely messed up
                        ejbReference = refContext.getCORBARemoteRef(earContext.getConfigID(), cssBean, new URI(getStringValue(remoteRef.getNsCorbaloc())),
                                getStringValue(remoteRef.getName()),
                                home);
                    } catch (URISyntaxException e) {
                        throw new DeploymentException("Could not construct CORBA NameServer URI: " + remoteRef.getNsCorbaloc(), e);
                    }
                } else {
                    if (remoteRef != null && remoteRef.isSetEjbLink()) {
                        ejbLink = remoteRef.getEjbLink();
                    } else if (ejbRef.isSetEjbLink()) {
                        ejbLink = getStringValue(ejbRef.getEjbLink());
                    }
                    AbstractNameQuery containerId;
                    String module = moduleURI == null? null: moduleURI.toString();
                    if (ejbLink != null) {
                        //TODO 1. include artifact, we know it must be in this app.
                        //TODO 2. use the isSession and reason about stateful/stateless
                        containerId = buildAbstractNameQuery(module, null, ejbLink);
                        //TODO WRONG
                        ejbReference = refContext.getEJBRemoteRef(moduleURI, ejbLink, isSession, home, remote, ejbContext);
                    } else if (remoteRef != null) {
                        //TODO 2 as above
                        GerPatternType patternType = remoteRef.getPattern();
                        containerId = buildAbstractNameQuery(patternType, null);
                        //TODO WRONG
                        ejbReference = refContext.getEJBRemoteRef(moduleURI, ejbLink, isSession, home, remote, ejbContext);
                    } else {
                        ejbReference = refContext.getImplicitEJBRemoteRef(moduleURI, ejbRefName, isSession, home, remote, ejbContext);
                    }
                }
            }
            builder.bind(ejbRefName, ejbReference);
        }
    }

    static void addEJBLocalRefs(NamingContext ejbContext, RefContext refContext, URI moduleURI, EjbLocalRefType[] ejbLocalRefs, Map ejbLocalRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

            String ejbRefName = getStringValue(ejbLocalRef.getEjbRefName());

            String local = getStringValue(ejbLocalRef.getLocal());
            try {
                assureEJBLocalObjectInterface(local, cl);
            } catch (DeploymentException e) {
                throw new DeploymentException("Error processing 'local' element for EJB Local Reference '" + ejbRefName + "' for module '" + moduleURI + "': " + e.getMessage());
            }

            String localHome = getStringValue(ejbLocalRef.getLocalHome());
            try {
                assureEJBLocalHomeInterface(localHome, cl);
            } catch (DeploymentException e) {
                throw new DeploymentException("Error processing 'local-home' element for EJB Local Reference '" + ejbRefName + "' for module '" + moduleURI + "': " + e.getMessage());
            }

            boolean isSession = "Session".equals(getStringValue(ejbLocalRef.getEjbRefType()));

            String ejbLink = null;
            GerEjbLocalRefType localRef = (GerEjbLocalRefType) ejbLocalRefMap.get(ejbRefName);
            if (localRef != null && localRef.isSetEjbLink()) {
                ejbLink = localRef.getEjbLink();
            } else if (ejbLocalRef.isSetEjbLink()) {
                ejbLink = getStringValue(ejbLocalRef.getEjbLink());
            }

            Reference ejbReference;
            AbstractNameQuery containerId;
            String module = moduleURI == null? null: moduleURI.toString();
            if (ejbLink != null) {
                //TODO 1. include artifact, we know it must be in this app.
                //TODO 2. use the isSession and reason about stateful/stateless
                containerId = buildAbstractNameQuery(module, null, ejbLink);
                //TODO WRONG
                ejbReference = refContext.getEJBLocalRef(moduleURI, ejbLink, isSession, localHome, local, ejbContext);
            } else if (localRef != null) {
                //TODO 2 as above
                GerPatternType patternType = localRef.getPattern();
                containerId = buildAbstractNameQuery(patternType, null);
                //TODO WRONG
                ejbReference = refContext.getEJBLocalRef(moduleURI, ejbLink, isSession, localHome, local, ejbContext);

            } else {
                ejbReference = refContext.getImplicitEJBLocalRef(moduleURI, ejbLink, isSession, localHome, local, ejbContext);
            }
            builder.bind(ejbRefName, ejbReference);
        }
    }

//TODO current implementation does not deal with portComponentRef links.

    static void addServiceRefs(EARContext earContext, Module module, ServiceRefType[] serviceRefs, Map serviceRefMap, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {

        RefContext refContext = earContext.getRefContext();

        for (int i = 0; i < serviceRefs.length; i++) {
            ServiceRefType serviceRef = serviceRefs[i];
            String name = getStringValue(serviceRef.getServiceRefName());
            GerServiceRefType serviceRefType = (GerServiceRefType) serviceRefMap.get(name);
//            Map credentialsNameMap = (Map) serviceRefCredentialsNameMap.get(name);
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
            ServiceRefHandlerType[] handlers = serviceRef.getHandlerArray();
            List handlerInfos = buildHandlerInfoList(handlers, cl);

//we could get a Reference or the actual serializable Service back.
            Object ref = refContext.getServiceReference(serviceInterface, wsdlURI, jaxrpcMappingURI, serviceQName, portComponentRefMap, handlerInfos, serviceRefType, earContext, module, cl);
            builder.bind(name, ref);
        }

    }

    private static List buildHandlerInfoList(ServiceRefHandlerType[] handlers, ClassLoader classLoader) throws DeploymentException {
        List handlerInfos = new ArrayList();
        for (int i = 0; i < handlers.length; i++) {
            ServiceRefHandlerType handler = handlers[i];
            org.apache.geronimo.xbeans.j2ee.String[] portNameArray = handler.getPortNameArray();
            List portNames = new ArrayList();
            for (int j = 0; j < portNameArray.length; j++) {
                portNames.add(portNameArray[j].getStringValue().trim());

            }
//            Set portNames = new HashSet(Arrays.asList(portNameArray));
            String handlerClassName = handler.getHandlerClass().getStringValue().trim();
            Class handlerClass = null;
            try {
                handlerClass = ClassLoading.loadClass(handlerClassName, classLoader);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load handler class", e);
            }
            Map config = new HashMap();
            ParamValueType[] paramValues = handler.getInitParamArray();
            for (int j = 0; j < paramValues.length; j++) {
                ParamValueType paramValue = paramValues[j];
                String paramName = paramValue.getParamName().getStringValue().trim();
                String paramStringValue = paramValue.getParamValue().getStringValue().trim();
                config.put(paramName, paramStringValue);
            }
            XsdQNameType[] soapHeaderQNames = handler.getSoapHeaderArray();
            QName[] headerQNames = new QName[soapHeaderQNames.length];
            for (int j = 0; j < soapHeaderQNames.length; j++) {
                XsdQNameType soapHeaderQName = soapHeaderQNames[j];
                headerQNames[j] = soapHeaderQName.getQNameValue();
            }
            Set soapRoles = new HashSet();
            for (int j = 0; j < handler.getSoapRoleArray().length; j++) {
                String soapRole = handler.getSoapRoleArray(j).getStringValue().trim();
                soapRoles.add(soapRole);
            }
            ServiceReferenceBuilder.HandlerInfoInfo handlerInfoInfo = new ServiceReferenceBuilder.HandlerInfoInfo(new HashSet(portNames), handlerClass, config, headerQNames, soapRoles);
            handlerInfos.add(handlerInfoInfo);
        }
        return handlerInfos;
    }

    public static Class assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        return assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    public static Class assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        return assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    public static Class assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        return assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    public static Class assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        return assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }

    public static Class assureInterface(String interfaceName, String superInterfaceName, String interfaceType, ClassLoader cl) throws DeploymentException {
        if (interfaceName == null || interfaceName.equals("")) {
            throw new DeploymentException("interface name cannot be blank");
        }
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
        return clazz;
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
        Map refMap = mapResourceRefs(gerResourceRefs);
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceRefType resourceRefType = resourceRefs[i];

            String type = resourceRefType.getResType().getStringValue().trim();

            if (!URL.class.getName().equals(type)
                    && !"javax.mail.Session".equals(type)
                    && !JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {

                GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(resourceRefType.getResRefName().getStringValue());
                AbstractNameQuery containerId = getResourceContainerId(getStringValue(resourceRefType.getResRefName()), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, uri, gerResourceRef, earContext);

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

    public static Map buildComponentContext(EARContext earContext,
                                            NamingContext ejbContext,
                                            Module module,
                                            UserTransaction userTransaction,
                                            EnvEntryType[] envEntries,
                                            EjbRefType[] ejbRefs,
                                            GerEjbRefType[] gerEjbRefs,
                                            EjbLocalRefType[] ejbLocalRefs,
                                            GerEjbLocalRefType[] gerEjbLocalRef,
                                            ResourceRefType[] resourceRefs,
                                            GerResourceRefType[] gerResourceRef,
                                            ResourceEnvRefType[] resourceEnvRefs,
                                            GerResourceEnvRefType[] gerResourceEnvRef,
                                            MessageDestinationRefType[] messageDestinationRefs,
                                            ServiceRefType[] serviceRefs,
                                            GerServiceRefType[] gerServiceRefs,
                                            ClassLoader cl) throws DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder();
        RefContext refContext = earContext.getRefContext();

        if (userTransaction != null) {
            builder.addUserTransaction(userTransaction);
        }

        AbstractNameQuery corbaGBean = earContext.getCORBAGBeanObjectName();
        if (corbaGBean != null) {
            builder.addORB(earContext.getConfigID(), corbaGBean);
        }

        Object handleDelegateReference = earContext.getRefContext().getHandleDelegateReference();
        if (handleDelegateReference != null) {
            builder.addHandleDelegateReference(handleDelegateReference);
        }

        URI moduleURI = module.getModuleURI();

        addEnvEntries(envEntries, builder, cl);

        if (ejbContext == null) {
            ejbContext = earContext;
        }

// ejb-ref
        addEJBRefs(earContext, ejbContext, refContext, moduleURI, ejbRefs, mapEjbRefs(gerEjbRefs), cl, builder);

// ejb-local-ref
        addEJBLocalRefs(ejbContext, refContext, moduleURI, ejbLocalRefs, mapEjbLocalRefs(gerEjbLocalRef), cl, builder);

// resource-ref
        addResourceRefs(earContext, moduleURI, resourceRefs, mapResourceRefs(gerResourceRef), cl, builder);

// resource-env-ref
        addResourceEnvRefs(earContext, resourceEnvRefs, mapResourceEnvRefs(gerResourceEnvRef), cl, builder);

        addMessageDestinationRefs(earContext.getRefContext(), earContext, messageDestinationRefs, cl, builder);

//        Map serviceRefMap = new HashMap();
//        Map serviceRefCredentialsNameMap = new HashMap();
//        mapServiceRefs(gerServiceRefs, serviceRefMap, serviceRefCredentialsNameMap);
        Map serviceRefMap = mapServiceRefs(gerServiceRefs);
        addServiceRefs(earContext, module, serviceRefs, serviceRefMap, cl, builder);

        return builder.getContext();
    }

    private static Map mapEjbRefs(GerEjbRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbRefType ref = refs[i];
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    private static Map mapEjbLocalRefs(GerEjbLocalRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbLocalRefType ref = refs[i];
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    private static Map mapResourceRefs(GerResourceRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerResourceRefType ref = refs[i];
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    private static Map mapResourceEnvRefs(GerResourceEnvRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerResourceEnvRefType ref = refs[i];
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    private static Map mapServiceRefs(GerServiceRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerServiceRefType ref = refs[i];
                String serviceRefName = ref.getServiceRefName().trim();
                refMap.put(serviceRefName, ref);
            }
        }
        return refMap;
    }

    //TODO consider including target interface
    public static AbstractNameQuery buildAbstractNameQuery(GerPatternType pattern, String type) {
        String groupId = pattern.isSetGroupId() ? pattern.getGroupId().trim() : null;
        String artifactid = pattern.isSetArtifactId() ? pattern.getArtifactId().trim() : null;
        String version = pattern.isSetVersion() ? pattern.getVersion().trim() : null;
        String module = pattern.isSetModule() ? pattern.getModule().trim() : null;
        String name = pattern.getName().trim();

        Artifact artifact = artifactid != null ? new Artifact(groupId, artifactid, version, "car") : null;
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("type", type);
        }
        if (module != null) {
            nameMap.put("module", module);
        }
        return new AbstractNameQuery(artifact, nameMap, Collections.EMPTY_SET);
    }

    public static AbstractNameQuery buildAbstractNameQuery(String module, String type, String name) {
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("type", type);
        }
        if (module != null) {
            nameMap.put("module", module);
        }
        return new AbstractNameQuery(null, nameMap, Collections.EMPTY_SET);
    }


}
