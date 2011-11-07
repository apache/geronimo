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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.BundleContextReference;
import org.apache.geronimo.naming.reference.BundleReference;
import org.apache.geronimo.naming.reference.GBeanReference;
import org.apache.geronimo.naming.reference.JndiReference;
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
    private static final Set<String> DEFAULT_COMP_JNDI_NAMES = new HashSet<String>();
    static {
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/EJBContext");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/WebServiceContext");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/TimerService");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/Validator");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/ValidatorFactory");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/BeanManager");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/TransactionSynchronizationRegistry");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/TransactionManager");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/Bundle");
        DEFAULT_COMP_JNDI_NAMES.add("java:comp/BundleContext");
    }

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
        Collection<MessageDestination> specDestinations;
        try {
            Method m = specDD.getClass().getMethod("getMessageDestination", new Class[0]);
            m.setAccessible(true);
            specDestinations = (Collection<MessageDestination>) m.invoke(specDD, new Object[0]);
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
        module.getRootEarContext().registerMessageDestionations(module.getModuleURI().toString(), nameMap);
    }


    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map sharedContext) throws DeploymentException {

        AbstractNameQuery transactionManager = module.getEarContext().getTransactionManagerName();
        if (transactionManager != null) {
            Set<AbstractNameQuery> query = new HashSet<AbstractNameQuery>();
            query.add(transactionManager);
            GBeanReference transactionManagerRef = new GBeanReference(module.getConfigId(), query, TransactionManager.class);
            put("java:comp/TransactionManager", transactionManagerRef, ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget>emptySet(), sharedContext);
            GBeanReference transactionSynchronizationRef = new GBeanReference(module.getConfigId(), query, TransactionSynchronizationRegistry.class);
            put("java:comp/TransactionSynchronizationRegistry", transactionSynchronizationRef, ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget>emptySet(), sharedContext);
        }

        put("java:comp/Bundle", new BundleReference(), ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget> emptySet(), sharedContext);
        put("java:comp/BundleContext", new BundleContextReference(), ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget> emptySet(), sharedContext);

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

        List<String> unresolvedRefs = new ArrayList<String>();
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        for (Map.Entry<String, ResourceEnvRef> entry : specDD.getResourceEnvRefMap().entrySet()) {
            String name = entry.getKey();
            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already
                continue;
            }
            ResourceEnvRef resourceEnvRef = entry.getValue();
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
                put(name, value, ReferenceType.RESOURCE_ENV, module.getJndiContext(), resourceEnvRef.getInjectionTarget(), sharedContext);
            }
        }

        if (unresolvedRefs.size() > 0) {
            log.warn("Failed to build reference to resource env reference " + unresolvedRefs + " defined in plan file. The corresponding entry in Geronimo deployment descriptor is missing.");
        }

        //message-destination-refs

        for (Map.Entry<String, MessageDestinationRef> entry : specDD.getMessageDestinationRefMap().entrySet()) {
            String name = entry.getKey();
            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already
                continue;
            }
            MessageDestinationRef messageDestinationRef = entry.getValue();
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
                put(name, value, ReferenceType.RESOURCE_ENV, module.getJndiContext(), messageDestinationRef.getInjectionTarget(), sharedContext);
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
            } else {
                addr = new StringRefAddr("","");
            }

            String objectFactory = getStringValue(gerResourceEnvRef.getObjectFactory());
            String objectFactoryLocation = getStringValue(gerResourceEnvRef.getObjectFactoryLocation());
            return new Reference(clazz, addr, objectFactory, objectFactoryLocation);
        }

        if (type.equals("javax.transaction.UserTransaction")) {
            return new UserTransactionReference();
        }

        if(DEFAULT_COMP_JNDI_NAMES.contains(name)) {
            return null;
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
        if ("javax.enterprise.inject.spi.BeanManager".equals(type)) {
            return new JndiReference("java:comp/BeanManager");
        }
        if ("javax.transaction.TransactionSynchronizationRegistry".equals(type)) {
            return new JndiReference("java:comp/TransactionSynchronizationRegistry");
        }
        if ("javax.transaction.TransactionManager".equals(type)) {
            return new JndiReference("java:comp/TransactionManager");
        }
        if ("org.osgi.framework.Bundle".equals(type)) {
            return new JndiReference("java:comp/Bundle");
        }
        if ("org.osgi.framework.BundleContext".equals(type)) {
            return new JndiReference("java:comp/BundleContext");
        }
        try {
            AbstractNameQuery containerId = getAdminObjectContainerId(name, gerResourceEnvRef);
            Reference ref = buildAdminObjectReference(module, containerId);
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
        Reference ref = buildAdminObjectReference(module, containerId);
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
                StringBuilder sb = new StringBuilder();
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


    private Reference buildAdminObjectReference(Module module, AbstractNameQuery containerId) throws DeploymentException {
        Configuration localConfiguration = module.getEarContext().getConfiguration();
        try {
            AbstractName abstractName = localConfiguration.findGBean(containerId);
            //String osgiJndiName = "aries:services/" + module.getEarContext().getNaming().toOsgiJndiName(abstractName);
            return new JndiReference("aries:services/", abstractName);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Can not resolve admin object ref " + containerId + " in configuration " + localConfiguration.getId(), e);
        }
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
                refMap.put(getJndiName(ref.getRefName().trim()), ref);
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

        private static final Set<String> knownResourceEnvEntries = new HashSet<String>(Arrays.asList(
                "javax.ejb.SessionContext",
                "javax.ejb.MessageDrivenContext",
                "javax.ejb.EntityContext",
                "javax.ejb.TimerService",
                "javax.validation.Validator",
                "javax.validation.ValidatorFactory",
                "javax.enterprise.inject.spi.BeanManager",
                "javax.transaction.UserTransaction",
                "javax.transaction.TransactionManager",
                "javax.transaction.TransactionSynchronizationRegistry",
                "org.osgi.framework.Bundle",
                "org.osgi.framework.BundleContext"
        ));

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

            String jndiName = getJndiName(resourceName);

            //If it already exists in xml as a message-destination-ref or resource-env-ref, we are done.
            MessageDestinationRef messageDestinationRef = annotatedApp.getMessageDestinationRefMap().get(jndiName);
            if (messageDestinationRef != null) {
                if (method != null || field != null) {
                    Set<InjectionTarget> targets = messageDestinationRef.getInjectionTarget();
                    if (!hasTarget(method, field, targets)) {
                        messageDestinationRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                    }
                }
                return true;
            }

            ResourceEnvRef resourceEnvRef = annotatedApp.getResourceEnvRefMap().get(jndiName);
            if (resourceEnvRef != null) {
                if (method != null || field != null) {
                    Set<InjectionTarget> targets = resourceEnvRef.getInjectionTarget();
                    if (!hasTarget(method, field, targets)) {
                        resourceEnvRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                    }
                }
                return true;
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
                    resourceEnvRefType = refMap.get(jndiName);
                }
                if (resourceEnvRefType != null || knownResourceEnvEntries.contains(resourceType)) {
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
    
    @Override
    public int getPriority() {
        return 45;
    }

}
