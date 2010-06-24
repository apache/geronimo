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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.JndiReference;
import org.apache.geronimo.naming.reference.ResourceReferenceFactory;
import org.apache.geronimo.naming.reference.UserTransactionReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.MessageDestination;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.Text;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class AdminObjectRefBuilder extends AbstractNamingBuilder {
    private static final Logger log = LoggerFactory.getLogger(AdminObjectRefBuilder.class);
    private final QNameSet adminOjbectRefQNameSet;
    private final QNameSet messageDestinationQNameSet;
    private final QNameSet messageDestinationRefQNameSet;

    private static final QName GER_ADMIN_OBJECT_REF_QNAME = GerResourceEnvRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_ADMIN_OBJECT_REF_QNAME_SET = QNameSet.singleton(GER_ADMIN_OBJECT_REF_QNAME);
    private static final QName GER_MESSAGE_DESTINATION_QNAME = GerMessageDestinationDocument.type.getDocumentElementName();
    private static final QNameSet GER_MESSAGE_DESTINATION_QNAME_SET = QNameSet.singleton(GER_MESSAGE_DESTINATION_QNAME);

    public AdminObjectRefBuilder(
            @ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
            @ParamAttribute(name = "eeNamespaces") String[] eeNamespaces) {
        super(defaultEnvironment);
        adminOjbectRefQNameSet = buildQNameSet(eeNamespaces, "resource-env-ref");
        messageDestinationQNameSet = buildQNameSet(eeNamespaces, "message-destination");
        messageDestinationRefQNameSet = buildQNameSet(eeNamespaces, "message-destination-ref");
    }

    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) {
        return !specDD.getResourceEnvRef().isEmpty() || !specDD.getMessageDestinationRef().isEmpty();
    }

    public void initContext(JndiConsumer specDD, XmlObject plan, Module module) throws DeploymentException {
        List<MessageDestination> specDestinations;
        try {
            Method m = specDD.getClass().getMethod("getMessageDestination", new Class[0]);
            specDestinations = (List<MessageDestination>) m.invoke(specDD, new Object[0]);
        } catch (Exception e) {
            specDestinations = new ArrayList<MessageDestination>();
        }
        XmlObject[] gerDestinations = plan.selectChildren(GER_MESSAGE_DESTINATION_QNAME_SET);
        Map<String, GerMessageDestinationType> nameMap = new HashMap<String, GerMessageDestinationType>();
        for (XmlObject gerDestination : gerDestinations) {
            GerMessageDestinationType destination = (GerMessageDestinationType) gerDestination.copy().changeType(GerMessageDestinationType.type);
            String name = destination.getMessageDestinationName().trim();
            nameMap.put(name, destination);
            boolean found = false;
            for (MessageDestination specDestination : specDestinations) {
                if (specDestination.getMessageDestinationName().trim().equals(name)) {
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


    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] gerResourceEnvRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_ADMIN_OBJECT_REF_QNAME_SET);
        Map<String, GerResourceEnvRefType> refMap = mapResourceEnvRefs(gerResourceEnvRefsUntyped);
        Map<String, Map<String, GerMessageDestinationType>> messageDestinations = module.getRootEarContext().getMessageDestinations();

        // Discover and process any @Resource annotations (if !metadata-complete)
        if (module.getClassFinder() != null) {

            // Process all the annotations for this naming builder type
            try {
                ResourceAnnotationHelper.processAnnotations(specDD, module.getClassFinder(), new AdminObjectRefProcessor(refMap, messageDestinations, module.getEarContext()));
            }
            catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
            }
        }

        Collection<ResourceEnvRef> resourceEnvRefsUntyped = specDD.getResourceEnvRef();
        List<String> unresolvedRefs = new ArrayList<String>();
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        for (ResourceEnvRef resourceEnvRef : resourceEnvRefsUntyped) {
            String name = getStringValue(resourceEnvRef.getResourceEnvRefName());
            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already
                continue;
            }
            addInjections(name, resourceEnvRef.getInjectionTarget(), componentContext);
            String type = getStringValue(resourceEnvRef.getResourceEnvRefType());
            type = inferAndCheckType(module, bundle, resourceEnvRef.getInjectionTarget(), name, type);
            GerResourceEnvRefType gerResourceEnvRef = refMap.remove(name);

            Object value = null;
            if (gerResourceEnvRef == null) {
                String lookupName = getStringValue(resourceEnvRef.getLookupName());
                if (lookupName != null) {
                    if (lookupName.equals(getJndiName(name))) {
                        throw new DeploymentException("resource-env-ref lookup name refers to itself");
                    }
                    value = new JndiReference(lookupName);
                }
            }

            if (value == null) {
                value = buildResourceReference(module, name, type, gerResourceEnvRef);
            }

            if (value == null) {
                unresolvedRefs.add(name);
            } else {
                put(name, value, module.getJndiContext());
            }
        }

        if (unresolvedRefs.size() > 0) {
            log.warn("Failed to build reference to resource env reference " + unresolvedRefs + " defined in plan file. The corresponding entry in Geronimo deployment descriptor is missing.");
        }

        //message-destination-refs
        Collection<MessageDestinationRef> messageDestinationRefsUntyped = specDD.getMessageDestinationRef();

        for (MessageDestinationRef messageDestinationRef : messageDestinationRefsUntyped) {
            String name = getStringValue(messageDestinationRef.getMessageDestinationRefName());
            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already
                continue;
            }
            addInjections(name, messageDestinationRef.getInjectionTarget(), componentContext);
            String linkName = getStringValue(messageDestinationRef.getMessageDestinationLink());
            //TODO figure out something better to do here!
            if (linkName == null) {
                linkName = name;
            }
            String type = getStringValue(messageDestinationRef.getMessageDestinationType());
            type = inferAndCheckType(module, bundle, messageDestinationRef.getInjectionTarget(), name, type);

            GerMessageDestinationType destination = getMessageDestination(linkName, messageDestinations);

            Object value = null;
            if (destination == null) {
                String lookupName = getStringValue(messageDestinationRef.getLookupName());
                if (lookupName != null) {
                    if (lookupName.equals(getJndiName(name))) {
                        throw new DeploymentException("message-destination-ref lookup name refers to itself");
                    }
                    value = new JndiReference(lookupName);
                }
            }

            if (value == null) {
                value = buildMessageReference(module, linkName, type, destination);
            }

            if (value != null) {
                put(name, value, module.getJndiContext());
            }
        }

    }

    private Object buildResourceReference(Module module, String name, String type, GerResourceEnvRefType gerResourceEnvRef)
            throws DeploymentException {
        Bundle bundle = module.getEarContext().getDeploymentBundle();

        Class iface;
        try {
            iface = bundle.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load resource-env-ref entry class " + type, e);
        }
        if (gerResourceEnvRef != null && gerResourceEnvRef.isSetReferenceClass()) {
            String clazz = gerResourceEnvRef.getReferenceClass();
            RefAddr addr = null;
            if (gerResourceEnvRef.isSetStringAddrType()) {
                String refAddrType = getStringValue(gerResourceEnvRef.getStringAddrType());
                String refAddr = getStringValue(gerResourceEnvRef.getStringAddr());
                addr = new StringRefAddr(refAddrType, refAddr);
            }
            String objectFactory = getStringValue(gerResourceEnvRef.getObjectFactory());
            String objectFactoryLocation = getStringValue(gerResourceEnvRef.getObjectFactoryLocation());
            return new Reference(clazz, addr, objectFactory, objectFactoryLocation);
        }

        if (type.equals("javax.transaction.UserTransaction")) {
            return new UserTransactionReference();
        }
        if ("javax.ejb.EJBContext".equals(type) ||
                "javax.ejb.EntityContext".equals(type) ||
                "javax.ejb.MessageDrivenContext".equals(type) ||
                "javax.ejb.SessionContext".equals(type)) {
            return new JndiReference("java:comp/EJBContext");
        }
        if ("javax.xml.ws.WebServiceContext".equals(type)) {
            return new JndiReference("java:comp/WebServiceContext");
        }
        if ("javax.ejb.TimerService".equals(type)) {
            return new JndiReference("java:comp/TimerService");
        }
        if ("javax.validation.Validator".equals(type)) {
            return new JndiReference("java:comp/Validator");
        }
        if ("javax.validation.ValidatorFactory".equals(type)) {
            return new JndiReference("java:comp/ValidatorFactory");
        }
        if ("javax.transaction.TransactionSynchronizationRegistry".equals(type)) {
            return new JndiReference("java:comp/TransactionSynchronizationRegistry");
        }
        try {
            AbstractNameQuery containerId = getAdminObjectContainerId(name, gerResourceEnvRef);
            ResourceReferenceFactory<RuntimeException> ref = buildAdminObjectReference(module, containerId, iface);
            return ref;
        } catch (UnresolvedReferenceException e) {
            throw new DeploymentException("Unable to resolve resource env reference '" + name + "' (" + (e.isMultiple() ? "found multiple matching resources" : "no matching resources found") + ")", e);
        }
    }

    private Object buildMessageReference(Module module, String linkName, String type, GerMessageDestinationType destination)
            throws DeploymentException {
        Bundle bundle = module.getEarContext().getDeploymentBundle();

        Class iface;
        try {
            iface = bundle.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load message-destination-ref entry type class " + type, e);
        }

        String moduleURI = null;

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
        return ref;
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

        public boolean processResource(JndiConsumer annotatedApp, Resource annotation, Class cls, Method method, Field field) throws DeploymentException {
            String resourceName = getResourceName(annotation, method, field);
            String resourceType = getResourceType(annotation, method, field);

            //If it already exists in xml as a message-destination-ref or resource-env-ref, we are done.
            Collection<MessageDestinationRef> messageDestinationRefs = annotatedApp.getMessageDestinationRef();
            for (MessageDestinationRef messageDestinationRef : messageDestinationRefs) {
                if (messageDestinationRef.getMessageDestinationRefName().trim().equals(resourceName)) {
                    if (method != null || field != null) {
                        List<InjectionTarget> targets = messageDestinationRef.getInjectionTarget();
                        if (!hasTarget(method, field, targets)) {
                            messageDestinationRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                        }
                    }
                    return true;
                }
            }
            Collection<ResourceEnvRef> ResourceEnvRefs = annotatedApp.getResourceEnvRef();
            for (ResourceEnvRef resourceEnvRefType : ResourceEnvRefs) {
                if (resourceEnvRefType.getResourceEnvRefName().trim().equals(resourceName)) {
                    if (method != null || field != null) {
                        List<InjectionTarget> targets = resourceEnvRefType.getInjectionTarget();
                        if (!hasTarget(method, field, targets)) {
                            resourceEnvRefType.getInjectionTarget().add(configureInjectionTarget(method, field));
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
                if (resourceEnvRefType != null ||
                        resourceType.equals("javax.ejb.EJBContext") ||
                        resourceType.equals("javax.ejb.SessionContext") ||
                        resourceType.equals("javax.ejb.MessageDrivenContext") ||
                        resourceType.equals("javax.ejb.EntityContext") ||
                        resourceType.equals("javax.ejb.TimerService") ||
                        resourceType.equals("javax.validation.Validator") ||
                        resourceType.equals("javax.validation.ValidatorFactory") ||
                        resourceType.equals("javax.transaction.UserTransaction") ||
                        resourceType.equals("javax.transaction.TransactionSynchronizationRegistry")) {
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

        private void addResourceEnvRef(JndiConsumer annotatedApp, String resourceName, String resourceType, Method method, Field field, Resource annotation) {
            ResourceEnvRef resourceEnvRef = new ResourceEnvRef();

            //------------------------------------------------------------------------------
            // <resource-env-ref> required elements:
            //------------------------------------------------------------------------------

            // resource-env-ref-name
            resourceEnvRef.setResourceEnvRefName(resourceName);

            if (!resourceType.isEmpty()) {
                // resource-env-ref-type
                resourceEnvRef.setResourceEnvRefType(resourceType);
            }
            if (method != null || field != null) {
                // injectionTarget
                resourceEnvRef.getInjectionTarget().add(configureInjectionTarget(method, field));
            }

            //------------------------------------------------------------------------------
            // <resource-env-ref> optional elements:
            //------------------------------------------------------------------------------

            // description
            String descriptionAnnotation = annotation.description();
            if (!descriptionAnnotation.isEmpty()) {
                resourceEnvRef.setDescriptions(new Text[] {new Text(null, descriptionAnnotation) }  );
            }

            // mappedName
            String mappdedNameAnnotation = annotation.mappedName();
            if (!mappdedNameAnnotation.isEmpty()) {
                resourceEnvRef.setMappedName(mappdedNameAnnotation);
            }

            // lookup
            String lookup = annotation.lookup();
            if (!lookup.equals("")) {
                resourceEnvRef.setLookupName(lookup);
            }
            annotatedApp.getResourceEnvRef().add(resourceEnvRef);
        }

        private void addMethodDestinationRef(JndiConsumer annotatedApp, String resourceName, String resourceType, Method method, Field field, Resource annotation) {
            MessageDestinationRef messageDestinationRef = new MessageDestinationRef();

            //------------------------------------------------------------------------------
            // <message-destination-ref> required elements:
            //------------------------------------------------------------------------------

            // message-destination-ref-name
            messageDestinationRef.setMessageDestinationRefName(resourceName);

            if (!resourceType.isEmpty()) {
                // message-destination-ref-type
                messageDestinationRef.setMessageDestinationType(resourceType);
            }
            if (method != null || field != null) {
                // injectionTarget
                messageDestinationRef.getInjectionTarget().add(configureInjectionTarget(method, field));
            }

            //------------------------------------------------------------------------------
            // <message-destination-ref> optional elements:
            //------------------------------------------------------------------------------

            // description
            String descriptionAnnotation = annotation.description();
            if (!descriptionAnnotation.isEmpty()) {
                messageDestinationRef.setDescriptions(new Text[]{new Text(null, descriptionAnnotation)});
            }

            // mappedName
            String mappdedNameAnnotation = annotation.mappedName();
            if (!mappdedNameAnnotation.isEmpty()) {
                messageDestinationRef.setMappedName(mappdedNameAnnotation);
            }

            // lookup
            String lookup = annotation.lookup();
            if (!lookup.isEmpty()) {
                messageDestinationRef.setLookupName(lookup);
            }
            annotatedApp.getMessageDestinationRef().add(messageDestinationRef);
        }
    }

}
