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

package org.apache.geronimo.concurrent.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.util.concurrent.ContextService;
import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedScheduledExecutorService;
import javax.util.concurrent.ManagedThreadFactory;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.concurrent.naming.ResourceReferenceFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.management.ManagedConstants;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev: 587764 $ $Date: 2008/03/06 22:05:03 $
 */
public class ResourceRefBuilder extends AbstractNamingBuilder {
    private final static Log log = LogFactory.getLog(ResourceRefBuilder.class);

    private static final QName GER_MANAGED_OBJECT_REF_QNAME = GerResourceEnvRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_MANAGED_OBJECT_REF_QNAME_SET = QNameSet.singleton(GER_MANAGED_OBJECT_REF_QNAME);

    private final QNameSet resourceRefQNameSet;
    private final Kernel kernel;

    public ResourceRefBuilder(Kernel kernel, Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        this.kernel = kernel;
        this.resourceRefQNameSet = buildQNameSet(eeNamespaces, "resource-env-ref");
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(resourceRefQNameSet).length > 0;
    }

    static Key<Map<String, GerResourceEnvRefType>> DEFAULT_MAPPINGS_KEY = new Key<Map<String, GerResourceEnvRefType>>() {
        public Map<String, GerResourceEnvRefType> get(Map context) {
            Map<String, GerResourceEnvRefType> result =
                (Map<String, GerResourceEnvRefType>) context.get(this);
            if (result == null) {
                result = new HashMap<String, GerResourceEnvRefType>();
                context.put(this, result);
            }
            return result;
        }
    };

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] gerResourceEnvRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_MANAGED_OBJECT_REF_QNAME_SET);
        Map<String, GerResourceEnvRefType> refMap = mapResourceEnvRefs(gerResourceEnvRefsUntyped);

        // Discover and process any @Resource annotations (if !metadata-complete)
        if (module.getClassFinder() != null) {
            // Process all the annotations for this naming builder type
            try {
                ManagedResourceRefProcessor processor = new ManagedResourceRefProcessor(refMap, module.getSharedContext());
                ResourceAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder(), processor);
            } catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module " + module.getName(), e);
            }
        }

        Map<String, GerResourceEnvRefType> defaultMappings = DEFAULT_MAPPINGS_KEY.get(module.getSharedContext());
        refMap.putAll(defaultMappings);

        List<ResourceEnvRefType> resourceEnvRefsUntyped = convert(specDD.selectChildren(resourceRefQNameSet), JEE_CONVERTER, ResourceEnvRefType.class, ResourceEnvRefType.type);
        Bundle bundle = module.getEarContext().getBundle();
        for (ResourceEnvRefType resourceEnvRef : resourceEnvRefsUntyped) {
            String name = getStringValue(resourceEnvRef.getResourceEnvRefName());
            String type = getStringValue(resourceEnvRef.getResourceEnvRefType());

            addInjections(name, resourceEnvRef.getInjectionTargetArray(), componentContext);

            Class iface;
            try {
                iface = bundle.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }

            GerResourceEnvRefType gerResourceEnvRef = refMap.remove(name);

            String j2eeType = null;
            if (iface == ContextService.class) {

            } else if (iface == ManagedThreadFactory.class) {
                j2eeType = ManagedConstants.MANAGED_THREAD_FACTORY;
            } else if (iface == ManagedExecutorService.class) {
                j2eeType = ManagedConstants.MANAGED_EXECUTOR_SERVICE;
            } else if (iface == ManagedScheduledExecutorService.class) {
                j2eeType = ManagedConstants.MANAGED_EXECUTOR_SERVICE;
            } else {
                log.debug("Ignoring non-managed resource reference type: " + iface);
                continue;
            }

            try {
                // TODO: should we also pass interfaces to discover the right gbeans?
                AbstractNameQuery containerId = getAdminObjectContainerId(name, j2eeType, gerResourceEnvRef);
                ResourceReferenceFactory ref = buildManagedObjectReference(module, containerId, iface);
                getJndiContextMap(componentContext).put(ENV + name, ref);
            } catch (UnresolvedReferenceException e) {
                throw new DeploymentException(
                        "Unable to resolve resource env reference '"
                                + name
                                + "' ("
                                + (e.isMultiple() ? "found multiple matching resources"
                                        : "no matching resources found") + ")", e);
            }
        }
    }

    private ResourceReferenceFactory buildManagedObjectReference(Module module, AbstractNameQuery containerId, Class iface) throws DeploymentException {
        Configuration localConfiguration = module.getEarContext().getConfiguration();
        try {
            // first, lookup in configuration
            localConfiguration.findGBean(containerId);
        } catch (GBeanNotFoundException e) {
            // second, lookup in kernel
            Set results = this.kernel.listGBeans(containerId);
            if (results == null || results.isEmpty()) {
                throw new DeploymentException("Cannot resolve managed object ref " + containerId);
            } else if (results.size() > 1) {
                throw new DeploymentException("Managed object ref resolved to multiple results " + containerId);
            }
        }
        return new ResourceReferenceFactory(module.getConfigId(), containerId, iface, module.getModuleName());
    }

    private static AbstractNameQuery getAdminObjectContainerId(String name, String j2eeType, GerResourceEnvRefType gerResourceEnvRef) {
        AbstractNameQuery containerId;
        if (gerResourceEnvRef == null) {
            containerId = buildAbstractNameQuery(null, null, name, j2eeType, null);
        } else {
            //construct name from components
            GerPatternType patternType = gerResourceEnvRef.getPattern();
            containerId = buildAbstractNameQuery(patternType, j2eeType, null, null);
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

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY - 5;
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    private static class ManagedResourceRefProcessor extends ResourceAnnotationHelper.ResourceProcessor {

        private Map<String, GerResourceEnvRefType> refMap;
        private Map sharedContext;

        public ManagedResourceRefProcessor(Map<String, GerResourceEnvRefType> refMap,
                                           Map sharedContext) {
            this.refMap = refMap;
            this.sharedContext = sharedContext;
        }

        private static String getDefaultServiceMapping(String resourceType) {
            if (resourceType.equals(ContextService.class.getName())) {
                return "DefaultContextService";
            } else if (resourceType.equals(ManagedThreadFactory.class.getName())) {
                return "DefaultManagedThreadFactory";
            } else if (resourceType.equals(ManagedExecutorService.class.getName())) {
                return "DefaultManagedExecutorService";
            } else if (resourceType.equals(ManagedScheduledExecutorService.class.getName())) {
                return "DefaultManagedScheduledExecutorService";
            } else {
                throw new IllegalArgumentException("Invalid resource type: " + resourceType);
            }
        }

        public boolean processResource(AnnotatedApp annotatedApp, Resource annotation, Class cls, Method method, Field field) throws DeploymentException {
            String resourceName = getResourceName(annotation, method, field);
            String resourceType = getResourceType(annotation, method, field);

            if (resourceType.equals(ContextService.class.getName()) ||
                resourceType.equals(ManagedThreadFactory.class.getName()) ||
                resourceType.equals(ManagedExecutorService.class.getName()) ||
                resourceType.equals(ManagedScheduledExecutorService.class.getName())) {

                ResourceEnvRefType resourceEnvRef = null;

                ResourceEnvRefType[] ResourceEnvRefs = annotatedApp.getResourceEnvRefArray();
                for (ResourceEnvRefType resourceEnvRefType : ResourceEnvRefs) {
                    if (resourceEnvRefType.getResourceEnvRefName().getStringValue().trim().equals(resourceName)) {
                        resourceEnvRef = resourceEnvRefType;
                        break;
                    }
                }

                if (resourceEnvRef == null) {
                    resourceEnvRef = annotatedApp.addNewResourceEnvRef();

                    // resource-env-ref-name
                    JndiNameType resourceEnvRefName = resourceEnvRef.addNewResourceEnvRefName();
                    resourceEnvRefName.setStringValue(resourceName);
                    resourceEnvRef.setResourceEnvRefName(resourceEnvRefName);
                }

                // resource-env-ref-type
                if (!resourceEnvRef.isSetResourceEnvRefType() && !resourceType.equals("")) {
                    FullyQualifiedClassType qualifiedClass = resourceEnvRef.addNewResourceEnvRefType();
                    qualifiedClass.setStringValue(resourceType);
                    resourceEnvRef.setResourceEnvRefType(qualifiedClass);
                }

                // description
                if ((resourceEnvRef.getDescriptionArray() == null ||
                     resourceEnvRef.getDescriptionArray().length == 0) &&
                     annotation.description().trim().length() > 0) {
                    DescriptionType description = resourceEnvRef.addNewDescription();
                    String descriptionAnnotation = annotation.description();
                    description.setStringValue(descriptionAnnotation);

                }

                // mapped-name
                if (!resourceEnvRef.isSetMappedName() && annotation.mappedName().trim().length() > 0) {
                    XsdStringType mappedName = resourceEnvRef.addNewMappedName();
                    mappedName.setStringValue(annotation.mappedName().trim());
                    resourceEnvRef.setMappedName(mappedName);
                }

                // injection target
                if (method != null || field != null) {
                    InjectionTargetType[] targets = resourceEnvRef.getInjectionTargetArray();
                    if (!hasTarget(method, field, targets)) {
                        configureInjectionTarget(resourceEnvRef.addNewInjectionTarget(), method, field);
                    }
                }

                // automatically map to default services
                if (annotation.name().trim().length() == 0 && !refMap.containsKey(resourceName)) {
                    GerResourceEnvRefType gerResourceEnvRefType = GerResourceEnvRefType.Factory.newInstance();
                    gerResourceEnvRefType.setRefName(resourceName);

                    GerPatternType patternType = gerResourceEnvRefType.addNewPattern();

                    patternType.setName(getDefaultServiceMapping(resourceType));

                    DEFAULT_MAPPINGS_KEY.get(sharedContext).put(resourceName, gerResourceEnvRefType);
                }
            }

            return true;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ResourceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("kernel", Kernel.class, false, false);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{ "kernel",
                                                 "defaultEnvironment",
                                                 "eeNamespaces" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
