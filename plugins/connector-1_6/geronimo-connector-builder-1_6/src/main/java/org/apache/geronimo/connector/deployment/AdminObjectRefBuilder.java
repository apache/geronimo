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

package org.apache.geronimo.connector.deployment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.ResourceReferenceFactory;
import org.apache.geronimo.naming.reference.UserTransactionReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationTypeType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class AdminObjectRefBuilder extends AbstractNamingBuilder {
    private static final Logger log = LoggerFactory.getLogger(AdminObjectRefBuilder.class);
    private final QNameSet adminOjbectRefQNameSet;
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

    public void initContext(XmlObject specDD, XmlObject plan, Module module) throws DeploymentException {
        List<MessageDestinationType> specDestinations = convert(specDD.selectChildren(messageDestinationQNameSet), JEE_CONVERTER, MessageDestinationType.class, MessageDestinationType.type);
        XmlObject[] gerDestinations = plan.selectChildren(GER_MESSAGE_DESTINATION_QNAME_SET);
        Map<String, GerMessageDestinationType> nameMap = new HashMap<String, GerMessageDestinationType>();
        for (XmlObject gerDestination : gerDestinations) {
            GerMessageDestinationType destination = (GerMessageDestinationType) gerDestination.copy().changeType(GerMessageDestinationType.type);
            String name = destination.getMessageDestinationName().trim();
            nameMap.put(name, destination);
            boolean found = false;
            for (MessageDestinationType specDestination : specDestinations) {
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


    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] gerResourceEnvRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_ADMIN_OBJECT_REF_QNAME_SET);
        Map<String, GerResourceEnvRefType> refMap = mapResourceEnvRefs(gerResourceEnvRefsUntyped);
        int initialGerRefSize = refMap.size();
        Map<String, Map<String, GerMessageDestinationType>> messageDestinations = module.getRootEarContext().getMessageDestinations();

        // Discover and process any @Resource annotations (if !metadata-complete)
        if (module.getClassFinder() != null) {

            // Process all the annotations for this naming builder type
            try {
                ResourceAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder(), new AdminObjectRefProcessor(refMap, messageDestinations, module.getEarContext()));
            }
            catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
            }
        }

        List<ResourceEnvRefType> resourceEnvRefsUntyped = convert(specDD.selectChildren(adminOjbectRefQNameSet), JEE_CONVERTER, ResourceEnvRefType.class, ResourceEnvRefType.type);
        int unresolvedRefSize = resourceEnvRefsUntyped.size();
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        for (ResourceEnvRefType resourceEnvRef : resourceEnvRefsUntyped) {
            String name = resourceEnvRef.getResourceEnvRefName().getStringValue().trim();
            if (lookupJndiContextMap(componentContext, ENV + name) != null) {
                // some other builder handled this entry already
                continue;
            }
            addInjections(name, resourceEnvRef.getInjectionTargetArray(), componentContext);
            String type = resourceEnvRef.getResourceEnvRefType().getStringValue().trim();
            Class iface;
            try {
                iface = bundle.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            GerResourceEnvRefType gerResourceEnvRef = refMap.get(name);
            refMap.remove(name);
            try {
                String refType = getStringValue(resourceEnvRef.getResourceEnvRefType());
                if (refType.equals("javax.transaction.UserTransaction")) {
                    Reference ref = new UserTransactionReference();
                    put(name, ref, getJndiContextMap(componentContext));
                } else {
                    AbstractNameQuery containerId = getAdminObjectContainerId(name, gerResourceEnvRef);
                    ResourceReferenceFactory<RuntimeException> ref = buildAdminObjectReference(module, containerId, iface);
                    put(name, ref, getJndiContextMap(componentContext));
                }
            } catch (UnresolvedReferenceException e) {
                throw new DeploymentException("Unable to resolve resource env reference '" + name + "' (" + (e.isMultiple() ? "found multiple matching resources" : "no matching resources found") + ")", e);
            }
        }
        
        if (refMap.size() > 0 && ((initialGerRefSize - unresolvedRefSize) != refMap.size())) {
            log.warn("Failed to build reference to Admin object reference "+refMap.keySet()+" defined in plan file, reason - corresponding entry in deployment descriptor missing.");
        }
        
        //message-destination-refs
        List<MessageDestinationRefType> messageDestinationRefsUntyped = convert(specDD.selectChildren(messageDestinationRefQNameSet), JEE_CONVERTER, MessageDestinationRefType.class, MessageDestinationRefType.type);

        for (MessageDestinationRefType messageDestinationRef : messageDestinationRefsUntyped) {
            String name = getStringValue(messageDestinationRef.getMessageDestinationRefName());
            if (lookupJndiContextMap(componentContext, name) != null) {
                // some other builder handled this entry already
                continue;
            }
            addInjections(name, messageDestinationRef.getInjectionTargetArray(), componentContext);
            String linkName = getStringValue(messageDestinationRef.getMessageDestinationLink());
            //TODO figure out something better to do here!
            if (linkName == null) {
                linkName = name;
            }
            String type = getStringValue(messageDestinationRef.getMessageDestinationType());
            if (type == null) {
                //must have an injection target to determine type EE5.8.1.3
                InjectionTargetType[] targets = messageDestinationRef.getInjectionTargetArray();
                if (targets.length == 0) {
                    throw new DeploymentException("No type for message-destination-ref can be determined from explicit specification or injection target: " + messageDestinationRef);
                }
                type = getStringValue(targets[0].getInjectionTargetClass());
                if (type == null) {
                    throw new DeploymentException("no type for message destination ref in injection target: " + targets[0]);
                }
            }
            Class iface;
            try {
                iface = bundle.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            String moduleURI = null;
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
            ResourceReferenceFactory<RuntimeException> ref = buildAdminObjectReference(module, containerId, iface);
            put(name, ref, getJndiContextMap(componentContext));

        }

    }

    public static GerMessageDestinationType getMessageDestination(String messageDestinationLink, Map<String, Map<String, GerMessageDestinationType>> messageDestinations) throws DeploymentException {
        GerMessageDestinationType destination = null;
        int pos = messageDestinationLink.indexOf('#');
        if (pos > -1) {
            String targetModule = messageDestinationLink.substring(0, pos);
            Map<String, GerMessageDestinationType> destinations = messageDestinations.get(targetModule);
            // Hmmm...if we don't find the module then something is wrong in the deployment.
            if (destinations == null) {
                StringBuffer sb = new StringBuffer();
                for (Object o : messageDestinations.keySet()) {
                    sb.append(o).append("\n");
                }
                throw new DeploymentException("Unknown module " + targetModule + " when processing message destination " + messageDestinationLink +
                        "\nKnown modules in deployable unit are:\n" + sb.toString());
            }
            messageDestinationLink = messageDestinationLink.substring(pos + 1);
            destination = destinations.get(messageDestinationLink);
        } else {
            for (Map<String, GerMessageDestinationType> destinations : messageDestinations.values()) {
                GerMessageDestinationType destinationTest = destinations.get(messageDestinationLink);
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


    private ResourceReferenceFactory<RuntimeException> buildAdminObjectReference(Module module, AbstractNameQuery containerId, Class iface) throws DeploymentException {
        Configuration localConfiguration = module.getEarContext().getConfiguration();
        try {
            localConfiguration.findGBean(containerId);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Can not resolve admin object ref " + containerId + " in configuration " + localConfiguration.getId(), e);
        }
        return new ResourceReferenceFactory<RuntimeException>(module.getConfigId(), containerId, iface);
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

    private static Map<String, GerResourceEnvRefType> mapResourceEnvRefs(XmlObject[] refs) {
        Map<String, GerResourceEnvRefType> refMap = new HashMap<String, GerResourceEnvRefType>();
        if (refs != null) {
            for (XmlObject ref1 : refs) {
                GerResourceEnvRefType ref = (GerResourceEnvRefType) ref1.copy().changeType(GerResourceEnvRefType.type);
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

    public static class AdminObjectRefProcessor extends ResourceAnnotationHelper.ResourceProcessor {
        public static final AdminObjectRefProcessor INSTANCE = new AdminObjectRefProcessor(null, null, null);

        private final EARContext earContext;
        private final Map<String, GerResourceEnvRefType> refMap;
        private final Map<String, Map<String, GerMessageDestinationType>> messageDestinations;

        public AdminObjectRefProcessor(Map<String, GerResourceEnvRefType> refMap, Map<String, Map<String, GerMessageDestinationType>> messageDestinations, EARContext earContext) {
            this.refMap = refMap;
            this.messageDestinations = messageDestinations;
            this.earContext = earContext;
        }

        public boolean processResource(AnnotatedApp annotatedApp, Resource annotation, Class cls, Method method, Field field) throws DeploymentException {
            String resourceName = getResourceName(annotation, method, field);
            String resourceType = getResourceType(annotation, method, field);

            if (resourceType.equals("javax.ejb.SessionContext")) return true;
            if (resourceType.equals("javax.ejb.MessageDrivenContext")) return true;
            if (resourceType.equals("javax.ejb.EntityContext")) return true;
            if (resourceType.equals("javax.ejb.TimerService")) return true;

            //If it already exists in xml as a message-destination-ref or resource-env-ref, we are done.
            MessageDestinationRefType[] messageDestinationRefs = annotatedApp.getMessageDestinationRefArray();
            for (MessageDestinationRefType messageDestinationRef : messageDestinationRefs) {
                if (messageDestinationRef.getMessageDestinationRefName().getStringValue().trim().equals(resourceName)) {
                    if (method != null || field != null) {
                        InjectionTargetType[] targets = messageDestinationRef.getInjectionTargetArray();
                        if (!hasTarget(method, field, targets)) {
                            configureInjectionTarget(messageDestinationRef.addNewInjectionTarget(), method, field);
                        }
                    }
                    return true;
                }
            }
            ResourceEnvRefType[] ResourceEnvRefs = annotatedApp.getResourceEnvRefArray();
            for (ResourceEnvRefType resourceEnvRefType : ResourceEnvRefs) {
                if (resourceEnvRefType.getResourceEnvRefName().getStringValue().trim().equals(resourceName)) {
                    if (method != null || field != null) {
                        InjectionTargetType[] targets = resourceEnvRefType.getInjectionTargetArray();
                        if (!hasTarget(method, field, targets)) {
                            configureInjectionTarget(resourceEnvRefType.addNewInjectionTarget(), method, field);
                        }
                    }
                    return true;
                }
            }

            //if it maps to a message-destination in the geronimo plan, it's a message-destination.
            GerMessageDestinationType gerMessageDestinationType = null;
            if (messageDestinations != null) {
                gerMessageDestinationType = getMessageDestination(resourceName, messageDestinations);
            }
            if (gerMessageDestinationType != null) {
                addMethodDestinationRef(annotatedApp, resourceName, resourceType, method, field, annotation);
                return true;
            } else {
                //if it maps to a resource-env-ref in the geronimo plan, it's a resource-ref
                GerResourceEnvRefType resourceEnvRefType = null;
                if (refMap != null) {
                    resourceEnvRefType = refMap.get(resourceName);
                }
                if (resourceEnvRefType != null || resourceType.equals("javax.transaction.UserTransaction")) {
                    //mapped resource-env-ref
                    addResourceEnvRef(annotatedApp, resourceName, resourceType, method, field, annotation);
                    return true;
                } else {
                    if (earContext != null) {
                        // look for an JCAAdminObject gbean with the right name
                        AbstractNameQuery containerId = buildAbstractNameQuery(null, null, resourceName,
                                NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE);
                        try {
                            earContext.findGBean(containerId);
                        } catch (GBeanNotFoundException e) {
                            // not identifiable as an admin object ref
                            return false;
                        }
                    } else {
                        if (!("javax.jms.Queue".equals(resourceType) || "javax.jms.Topic".equals(resourceType) 
                                || "javax.jms.Destination".equals(resourceType))) {
                            // not identifiable as an admin object ref
                            return false;
                        }
                    }
                    addResourceEnvRef(annotatedApp, resourceName, resourceType, method, field, annotation);
                    return true;
                }
            }
        }

        private void addResourceEnvRef(AnnotatedApp annotatedApp, String resourceName, String resourceType, Method method, Field field, Resource annotation) {
            ResourceEnvRefType resourceEnvRef = annotatedApp.addNewResourceEnvRef();

            //------------------------------------------------------------------------------
            // <resource-env-ref> required elements:
            //------------------------------------------------------------------------------

            // resource-env-ref-name
            JndiNameType resourceEnvRefName = resourceEnvRef.addNewResourceEnvRefName();
            resourceEnvRefName.setStringValue(resourceName);
            resourceEnvRef.setResourceEnvRefName(resourceEnvRefName);

            if (!resourceType.equals("")) {
                // resource-env-ref-type
                FullyQualifiedClassType qualifiedClass = resourceEnvRef.addNewResourceEnvRefType();
                qualifiedClass.setStringValue(resourceType);
                resourceEnvRef.setResourceEnvRefType(qualifiedClass);
            }
            if (method != null || field != null) {
                // injectionTarget
                InjectionTargetType injectionTarget = resourceEnvRef.addNewInjectionTarget();
                configureInjectionTarget(injectionTarget, method, field);
            }

            //------------------------------------------------------------------------------
            // <resource-env-ref> optional elements:
            //------------------------------------------------------------------------------

            // description
            String descriptionAnnotation = annotation.description();
            if (!descriptionAnnotation.equals("")) {
                DescriptionType description = resourceEnvRef.addNewDescription();
                description.setStringValue(descriptionAnnotation);
            }

            // mappedName
            String mappdedNameAnnotation = annotation.mappedName();
            if (!mappdedNameAnnotation.equals("")) {
                XsdStringType mappedName = resourceEnvRef.addNewMappedName();
                mappedName.setStringValue(mappdedNameAnnotation);
                resourceEnvRef.setMappedName(mappedName);
            }
        }

        private void addMethodDestinationRef(AnnotatedApp annotatedApp, String resourceName, String resourceType, Method method, Field field, Resource annotation) {
            MessageDestinationRefType messageDestinationRef = annotatedApp.addNewMessageDestinationRef();

            //------------------------------------------------------------------------------
            // <message-destination-ref> required elements:
            //------------------------------------------------------------------------------

            // message-destination-ref-name
            JndiNameType messageDestinationRefName = messageDestinationRef.addNewMessageDestinationRefName();
            messageDestinationRefName.setStringValue(resourceName);
            messageDestinationRef.setMessageDestinationRefName(messageDestinationRefName);

            if (!resourceType.equals("")) {
                // message-destination-ref-type
                MessageDestinationTypeType msgDestType = messageDestinationRef.addNewMessageDestinationType();
                msgDestType.setStringValue(resourceType);
                messageDestinationRef.setMessageDestinationType(msgDestType);
            }
            if (method != null || field != null) {
                // injectionTarget
                InjectionTargetType injectionTarget = messageDestinationRef.addNewInjectionTarget();
                configureInjectionTarget(injectionTarget, method, field);
            }

            //------------------------------------------------------------------------------
            // <message-destination-ref> optional elements:
            //------------------------------------------------------------------------------

            // description
            String descriptionAnnotation = annotation.description();
            if (!descriptionAnnotation.equals("")) {
                DescriptionType description = messageDestinationRef.addNewDescription();
                description.setStringValue(descriptionAnnotation);
            }

            // mappedName
            String mappdedNameAnnotation = annotation.mappedName();
            if (!mappdedNameAnnotation.equals("")) {
                XsdStringType mappedName = messageDestinationRef.addNewMappedName();
                mappedName.setStringValue(mappdedNameAnnotation);
                messageDestinationRef.setMappedName(mappedName);
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AdminObjectRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo
            () {
        return GBEAN_INFO;
    }

}
