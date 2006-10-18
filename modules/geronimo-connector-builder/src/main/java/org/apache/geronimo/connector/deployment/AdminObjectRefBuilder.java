/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.connector.deployment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.ResourceReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class AdminObjectRefBuilder extends AbstractNamingBuilder {
    private  final QNameSet adminOjbectRefQNameSet;
    private final QNameSet messageDestinationQNameSet;
    private final QNameSet messageDestinationRefQNameSet;

    private static final QName GER_ADMIN_OBJECT_REF_QNAME = GerResourceEnvRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_ADMIN_OBJECT_REF_QNAME_SET = QNameSet.singleton(GER_ADMIN_OBJECT_REF_QNAME);
    private static final QName GER_MESSAGE_DESTINATION_QNAME = GerMessageDestinationDocument.type.getDocumentElementName();
    private static final QNameSet GER_MESSAGE_DESTINATION_QNAME_SET = QNameSet.singleton(GER_MESSAGE_DESTINATION_QNAME);

    public AdminObjectRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        adminOjbectRefQNameSet = buildQNameSet(eeNamespaces, "resource-env-ref");
        messageDestinationQNameSet = buildQNameSet(eeNamespaces, "message-destination");
        messageDestinationRefQNameSet = buildQNameSet(eeNamespaces, "message-destination-ref");
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(adminOjbectRefQNameSet).length > 0 || specDD.selectChildren(messageDestinationRefQNameSet).length > 0;
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
        XmlObject[] specDestinations = convert(specDD.selectChildren(messageDestinationQNameSet), J2EE_CONVERTER, MessageDestinationType.type);
        XmlObject[] gerDestinations = plan.selectChildren(GER_MESSAGE_DESTINATION_QNAME_SET);
            Map nameMap = new HashMap();
            for (int i = 0; i < gerDestinations.length; i++) {
                GerMessageDestinationType destination = (GerMessageDestinationType) gerDestinations[i].copy().changeType(GerMessageDestinationType.type);
                String name = destination.getMessageDestinationName().trim();
                nameMap.put(name, destination);
                boolean found = false;
                for (int j = 0; j < specDestinations.length; j++) {
                    MessageDestinationType specDestination = (MessageDestinationType) specDestinations[j];
                    if (specDestination.getMessageDestinationName().getStringValue().trim().equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new DeploymentException("No spec DD message-destination for " + name);
                }
            }
            module.getRootEarContext().registerMessageDestionations(module.getName(), nameMap);
    }


    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] resourceEnvRefsUntyped = convert(specDD.selectChildren(adminOjbectRefQNameSet), J2EE_CONVERTER, ResourceEnvRefType.type);
        ClassLoader cl = localConfiguration.getConfigurationClassLoader();
        XmlObject[] gerResourceEnvRefsUntyped = plan == null? NO_REFS: plan.selectChildren(GER_ADMIN_OBJECT_REF_QNAME_SET);
        Map refMap = mapResourceEnvRefs(gerResourceEnvRefsUntyped);
        for (int i = 0; i < resourceEnvRefsUntyped.length; i++) {
            ResourceEnvRefType resourceEnvRef = (ResourceEnvRefType) resourceEnvRefsUntyped[i];
            String name = resourceEnvRef.getResourceEnvRefName().getStringValue().trim();
            String type = resourceEnvRef.getResourceEnvRefType().getStringValue().trim();
            Class iface;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            GerResourceEnvRefType gerResourceEnvRef = (GerResourceEnvRefType) refMap.get(name);
            try {
                AbstractNameQuery containerId = getAdminObjectContainerId(name, gerResourceEnvRef);
                Reference ref = buildAdminObjectReference(localConfiguration, containerId, iface);
                getJndiContextMap(componentContext).put(ENV + name, ref);
            } catch (UnresolvedReferenceException e) {
                throw new DeploymentException("Unable to resolve resource env reference '" + name + "' (" + (e.isMultiple() ? "found multiple matching resources" : "no matching resources found") + ")");
            }
        }

        //message-destination-refs
        XmlObject[] messageDestinationRefsUntyped = convert(specDD.selectChildren(messageDestinationRefQNameSet), J2EE_CONVERTER, MessageDestinationRefType.type);

        for (int i = 0; i < messageDestinationRefsUntyped.length; i++) {
            MessageDestinationRefType messageDestinationRef = (MessageDestinationRefType) messageDestinationRefsUntyped[i];
            String name = getStringValue(messageDestinationRef.getMessageDestinationRefName());
            String linkName = getStringValue(messageDestinationRef.getMessageDestinationLink());
            String type = getStringValue(messageDestinationRef.getMessageDestinationType());
            Class iface;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            String moduleURI = null;
            Map messageDestinations = module.getRootEarContext().getMessageDestinations();
            GerMessageDestinationType destination = getMessageDestination(linkName, messageDestinations);
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
                    //AMM -- the following line causes blowups; e.g. to look in DayTrader EJB module for a RA -- why is that?!?
                    //moduleURI = linkName.substring(0, pos);
                    linkName = linkName.substring(pos + 1);
                }
            }

            //try to resolve ref based only matching resource-ref-name
            //throws exception if it can't locate ref.
            AbstractNameQuery containerId = buildAbstractNameQuery(null, moduleURI, linkName, NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE);
            Reference ref = buildAdminObjectReference(localConfiguration, containerId, iface);
            getJndiContextMap(componentContext).put(ENV + name, ref);

        }

    }

    public static GerMessageDestinationType getMessageDestination(String messageDestinationLink, Map messageDestinations) throws DeploymentException {
        GerMessageDestinationType destination = null;
        int pos = messageDestinationLink.indexOf('#');
        if (pos > -1) {
            String targetModule = messageDestinationLink.substring(0, pos);
            Map destinations = (Map) messageDestinations.get(targetModule);
            // Hmmm...if we don't find the module then something is wrong in the deployment.
            if (destinations == null) {
                StringBuffer sb = new StringBuffer();
                for (Iterator mapIterator = messageDestinations.keySet().iterator(); mapIterator.hasNext();) {
                    sb.append(mapIterator.next()).append("\n");
                }
                throw new DeploymentException("Unknown module " + targetModule + " when processing message destination " + messageDestinationLink +
                        "\nKnown modules in deployable unit are:\n" + sb.toString());
            }
            messageDestinationLink = messageDestinationLink.substring(pos + 1);
            destination = (GerMessageDestinationType) destinations.get(messageDestinationLink);
        } else {
            for (Iterator iterator = messageDestinations.values().iterator(); iterator.hasNext();) {
                Map destinations = (Map) iterator.next();
                GerMessageDestinationType destinationTest = (GerMessageDestinationType) destinations.get(messageDestinationLink);
                if (destinationTest != null) {
                    if (destination != null) {
                        throw new DeploymentException("Duplicate message destination " + messageDestinationLink + " accessed from a message-destination-link without a module");
                    }
                    destination = destinationTest;
                }
            }
        }
        return destination;
    }


    private Reference buildAdminObjectReference(Configuration localConfiguration, AbstractNameQuery containerId, Class iface) throws DeploymentException {
        try {
            localConfiguration.findGBean(containerId);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Can not resolve admin object ref " + containerId + " in configuration " + localConfiguration.getId());
        }
        return new ResourceReference(localConfiguration.getId(), containerId, iface);
    }

    private static AbstractNameQuery getAdminObjectContainerId(String name, GerResourceEnvRefType gerResourceEnvRef) {
        AbstractNameQuery containerId;
        if (gerResourceEnvRef == null) {
            containerId = buildAbstractNameQuery(null, null, name, NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else if (gerResourceEnvRef.isSetMessageDestinationLink()) {
            containerId = buildAbstractNameQuery(null, null, gerResourceEnvRef.getMessageDestinationLink().trim(), NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else if (gerResourceEnvRef.isSetAdminObjectLink()) {
            String moduleURI = null;
            if (gerResourceEnvRef.isSetAdminObjectModule()) {
                moduleURI = gerResourceEnvRef.getAdminObjectModule().trim();
            }
            containerId = buildAbstractNameQuery(null, moduleURI, gerResourceEnvRef.getAdminObjectLink().trim(), NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else {
            //construct name from components
            GerPatternType patternType = gerResourceEnvRef.getPattern();
            containerId = buildAbstractNameQuery(patternType, NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE, null);
        }
        return containerId;
    }

    private static Map mapResourceEnvRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerResourceEnvRefType ref = (GerResourceEnvRefType) refs[i].copy().changeType(GerResourceEnvRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    public QNameSet getSpecQNameSet() {
        return adminOjbectRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_ADMIN_OBJECT_REF_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AdminObjectRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
