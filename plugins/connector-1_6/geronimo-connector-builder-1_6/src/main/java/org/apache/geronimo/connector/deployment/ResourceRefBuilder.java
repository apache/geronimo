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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.CorbaGBeanNameSource;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.naming.reference.JndiReference;
import org.apache.geronimo.naming.reference.ORBReference;
import org.apache.geronimo.naming.reference.ResourceReference;
import org.apache.geronimo.naming.reference.URLReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.ResSharingScope;
import org.apache.openejb.jee.ResourceRef;
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
public class ResourceRefBuilder extends AbstractNamingBuilder implements ResourceEnvironmentSetter {

    private static final Logger log = LoggerFactory.getLogger(ResourceRefBuilder.class);

    private static final QName GER_RESOURCE_REF_QNAME = GerResourceRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_RESOURCE_REF_QNAME_SET = QNameSet.singleton(GER_RESOURCE_REF_QNAME);
    private static final String JAXR_CONNECTION_FACTORY_CLASS = "javax.xml.registry.ConnectionFactory";
    private static final String JAVAX_MAIL_SESSION_CLASS = "javax.mail.Session";

    private final QNameSet resourceRefQNameSet;
    private final Environment corbaEnvironment;
    private final SingleElementCollection<CorbaGBeanNameSource> corbaGBeanNameSourceCollection;
    private final Bundle bundle;

    public ResourceRefBuilder(
            @ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
            @ParamAttribute(name = "corbaEnvironment") Environment corbaEnvironment,
            @ParamAttribute(name = "eeNamespaces") String[] eeNamespaces,
            @ParamReference(name = "CorbaGBeanNameSource", namingType = "") Collection<CorbaGBeanNameSource> corbaGBeanNameSourceCollection,
            @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) {
        super(defaultEnvironment);

        resourceRefQNameSet = buildQNameSet(eeNamespaces, "resource-ref");
        this.corbaEnvironment = corbaEnvironment;
        this.corbaGBeanNameSourceCollection = new SingleElementCollection<CorbaGBeanNameSource>(corbaGBeanNameSourceCollection);
        this.bundle = bundle;
    }

    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) {
        return !specDD.getResourceRef().isEmpty();
    }

    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {

        // Discover and process any @Resource annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {

            // Process all the annotations for this naming builder type
            try {
                ResourceAnnotationHelper.processAnnotations(specDD, module.getClassFinder(), ResourceRefProcessor.INSTANCE);
            }
            catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
            }
        }

        XmlObject[] gerResourceRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_RESOURCE_REF_QNAME_SET);
        Map<String, GerResourceRefType> refMap = mapResourceRefs(gerResourceRefsUntyped);
        List<String> unresolvedRefs = new ArrayList<String>();
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        for (Map.Entry<String, ResourceRef> entry : specDD.getResourceRefMap().entrySet()) {
            String name = entry.getKey();
            ResourceRef resourceRef = entry.getValue();
            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already

                // Always merge injections. This is for example where data source is defined as
                // @DataSource(name='foo') and it is injected via @Resource(name='foo')
                addInjections(normalize(name), ReferenceType.RESOURCE, resourceRef.getInjectionTarget(), NamingBuilder.INJECTION_KEY.get(sharedContext));

                continue;
            }
            String type = getStringValue(resourceRef.getResType());
            type = inferAndCheckType(module, bundle, resourceRef.getInjectionTarget(), name, type);
            GerResourceRefType gerResourceRef = refMap.get(name);
            if (log.isDebugEnabled()) {
                log.debug("trying to resolve " + name + ", type " + type + ", resourceRef " + gerResourceRef);
            }
            Object value = null;
            if (gerResourceRef == null) {
                String lookupName = getStringValue(resourceRef.getLookupName());
                if (lookupName != null) {
                    if (lookupName.equals(getJndiName(name))) {
                        throw new DeploymentException("resource-ref lookup name refers to itself");
                    }
                    value = new JndiReference(lookupName);
                }
            }

            if (value == null) {
                value = buildReference(module, name, type, gerResourceRef);
            }

            if (value == null) {
                unresolvedRefs.add(name);
            } else {
                put(name, value, ReferenceType.RESOURCE, module.getJndiContext(), resourceRef.getInjectionTarget(), sharedContext);
            }

        }

        if (unresolvedRefs.size() > 0) {
            log.warn("Failed to build reference to resource reference " + unresolvedRefs + " defined in plan file. The corresponding entry in Geronimo deployment descriptor is missing.");
        }
    }

    private Object buildReference(Module module, String name, String type, GerResourceRefType gerResourceRef) throws DeploymentException {
        Bundle bundle = module.getEarContext().getDeploymentBundle();

        Class<?> iface;
        try {
            iface = bundle.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not resource-ref entry class " + type, e);
        }

        if (iface == URL.class) {
            if (gerResourceRef == null || !gerResourceRef.isSetUrl()) {
                throw new DeploymentException("No url supplied to resolve: " + name);
            }
            String url = gerResourceRef.getUrl().trim();
            //TODO expose jsr-77 objects for these guys
            try {
                //check for malformed URL
                new URL(url);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Could not convert " + url + " to URL", e);
            }
            return new URLReference(url);
        } else if (isOrbReference(iface)) {
            CorbaGBeanNameSource corbaGBeanNameSource = corbaGBeanNameSourceCollection.getElement();
            if (corbaGBeanNameSource == null) {
                throw new DeploymentException("No orb setup but there is a orb reference");
            }
            AbstractNameQuery corbaName = corbaGBeanNameSource.getCorbaGBeanName();
            if (corbaName != null) {
                Artifact[] moduleId = module.getConfigId();
                EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), corbaEnvironment);
                return new ORBReference(moduleId, corbaName);
            }
        } else if (iface.isAnnotationPresent(ManagedBean.class)) {
            ManagedBean managed = iface.getAnnotation(ManagedBean.class);
            String beanName = managed.value().length() == 0 ? iface.getSimpleName() : managed.value();
            if (iface != null) {
                beanName = beanName + "!" + iface.getName();
            }
            return new JndiReference("java:module/" + beanName);
        } else {
            //determine jsr-77 type from interface
            String j2eeType;

            if (JAVAX_MAIL_SESSION_CLASS.equals(type)) {
                j2eeType = NameFactory.JAVA_MAIL_RESOURCE;
            } else if (JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {
                j2eeType = NameFactory.JAXR_CONNECTION_FACTORY;
            } else {
//                j2eeType = NameFactory.JCA_CONNECTION_FACTORY;
                j2eeType = NameFactory.JCA_CONNECTION_MANAGER;
            }
            try {
                AbstractNameQuery containerId = getResourceContainerId(name, j2eeType, null, gerResourceRef);

                AbstractName abstractName = module.getEarContext().findGBean(containerId);
                String osgiJndiName = module.getEarContext().getNaming().toOsgiJndiName(abstractName);
                String filter = "(osgi.jndi.service.name=" + osgiJndiName + ')';

                return new ResourceReference(abstractName, type);
                        //ResourceReferenceFactory<ResourceException>(module.getConfigId(), containerId, iface);
            } catch (GBeanNotFoundException e) {
                StringBuilder errorMessage = new StringBuilder("Unable to resolve resource reference '");
                errorMessage.append(name);
                errorMessage.append("' (");
                if (e.hasMatches()) {
                    errorMessage.append("Found multiple matching resources.  Try being more specific in a resource-ref mapping in your Geronimo deployment plan.\n");
                    for (AbstractName match : e.getMatches()) {
                        errorMessage.append(match).append("\n");
                    }
                } else if (gerResourceRef == null) {
                    errorMessage.append("Could not auto-map to resource.  Try adding a resource-ref mapping to your Geronimo deployment plan.");
                } else if (gerResourceRef.isSetResourceLink()) {
                    errorMessage.append("Could not find resource '");
                    errorMessage.append(gerResourceRef.getResourceLink());
                    errorMessage.append("'.  Perhaps it has not yet been configured, or your application does not have a dependency declared for that resource module?");
                } else {
                    errorMessage.append("Could not find the resource specified in your Geronimo deployment plan:");
                    errorMessage.append(gerResourceRef.getPattern());
                }
                errorMessage.append("\nSearch conducted in current module and dependencies:\n");
                for (Dependency dependency : module.getEnvironment().getDependencies()) {
                    errorMessage.append(dependency).append("\n");
                }
                errorMessage.append(")");

                throw new DeploymentException(errorMessage.toString());
            }
        }

        return null;
    }

    private boolean isOrbReference(Class iface) {
        try {
            Class orbClass = bundle.loadClass("org.omg.CORBA.ORB");
            return orbClass.isAssignableFrom(iface);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void setResourceEnvironment(ResourceEnvironmentBuilder builder, Collection<ResourceRef> resourceRefList, GerResourceRefType[] gerResourceRefs) throws DeploymentException {
        Map refMap = mapResourceRefs(gerResourceRefs);
        Set<AbstractNameQuery> unshareableResources = new HashSet<AbstractNameQuery>();
        Set<AbstractNameQuery> applicationManagedSecurityResources = new HashSet<AbstractNameQuery>();
        for (ResourceRef resourceRef : resourceRefList) {

            String type = resourceRef.getResType();
            if(type == null){
                continue;
            }else {
                type = type.trim();
            }

            if (!URL.class.getName().equals(type)
                    && !"javax.mail.Session".equals(type)
                    && !JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {

                GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(resourceRef.getResRefName());
                AbstractNameQuery containerId = getResourceContainerId(getStringValue(resourceRef.getResRefName()), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, null, gerResourceRef);

                if (ResSharingScope.UNSHAREABLE.equals(resourceRef.getResSharingScope())) {
                    unshareableResources.add(containerId);
                }
                if (ResAuth.APPLICATION.equals(resourceRef.getResAuth())) {
                    applicationManagedSecurityResources.add(containerId);
                }
            }
        }
        builder.setUnshareableResources(unshareableResources);
        builder.setApplicationManagedSecurityResources(applicationManagedSecurityResources);
    }

    private Map<String, GerResourceRefType> mapResourceRefs(XmlObject[] refs) {
        Map<String, GerResourceRefType> refMap = new HashMap<String, GerResourceRefType>();
        if (refs != null) {
            for (XmlObject ref1 : refs) {
                GerResourceRefType ref = (GerResourceRefType) ref1.copy().changeType(GerResourceRefType.type);
                refMap.put(getJndiName(ref.getRefName().trim()), ref);
            }
        }
        return refMap;
    }

    private AbstractNameQuery getResourceContainerId(String name, String type, URI moduleURI, GerResourceRefType gerResourceRef) {
        if (name.startsWith("java:")) {
            name = name.substring(name.indexOf("/env/") + 5);
        }
        AbstractNameQuery containerId;
        String module = moduleURI == null ? null : moduleURI.toString();
        if (gerResourceRef == null) {
            containerId = buildAbstractNameQuery(null, module, name, type, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else if (gerResourceRef.isSetResourceLink()) {
            containerId = buildAbstractNameQuery(null, module, gerResourceRef.getResourceLink().trim(), type, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else {
            //construct name from components
            GerPatternType patternType = gerResourceRef.getPattern();
            containerId = buildAbstractNameQuery(patternType, type, NameFactory.RESOURCE_ADAPTER_MODULE, null);
        }
        return containerId;
    }


    public QNameSet getSpecQNameSet() {
        return resourceRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_RESOURCE_REF_QNAME_SET;
    }

    public static class ResourceRefProcessor extends ResourceAnnotationHelper.ResourceProcessor {

        public static final ResourceRefProcessor INSTANCE = new ResourceRefProcessor();

        private ResourceRefProcessor() {
        }

        public boolean processResource(JndiConsumer annotatedApp, Resource annotation, Class cls, Method method, Field field) {
            if(log.isDebugEnabled()) {
                log.debug("processResource( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                        "[annotation] " + annotation.toString() + "," + '\n' +
                        "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                        "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                        "[field] " + (field != null ? field.getName() : null) + " ): Entry");
            }
            String resourceName = getResourceName(annotation, method, field);
            Class resourceTypeClass = getResourceTypeClass(annotation, method, field);
            String resourceType = resourceTypeClass.getCanonicalName();

            if (resourceType.equals("javax.sql.DataSource") ||
                    resourceType.equals("javax.mail.Session") ||
                    resourceType.equals("java.net.URL") ||
                    resourceType.equals("org.omg.CORBA.ORB") ||
                    resourceType.equals("org.omg.CORBA_2_3.ORB") ||
                    resourceType.equals("org.omg.CORBA_2_4.ORB") ||
                    resourceType.endsWith("ConnectionFactory") ||
                    isManagedBeanReference(resourceTypeClass, annotation)) {

                log.debug("processResource(): <resource-ref> found");

                ResourceRef resourceRef = annotatedApp.getResourceRefMap().get(getJndiName(resourceName));

                if (resourceRef == null) {
                    try {

                        log.debug("processResource(): Does not exist in DD: " + resourceName);

                        // Doesn't exist in deployment descriptor -- add new
                        resourceRef = new ResourceRef();

                        //------------------------------------------------------------------------------
                        // <resource-ref> required elements:
                        //------------------------------------------------------------------------------

                        // resource-ref-name
                        resourceRef.setResRefName(resourceName);

                        if (!resourceType.isEmpty()) {
                            // resource-ref-type
                            resourceRef.setResType(resourceType);
                        }

                        //------------------------------------------------------------------------------
                        // <resource-ref> optional elements:
                        //------------------------------------------------------------------------------

                        // description
                        String descriptionAnnotation = annotation.description();
                        if (!descriptionAnnotation.equals("")) {
                            resourceRef.setDescriptions(new Text[]{new Text(null, descriptionAnnotation)});
                        }

                        // authentication
                        if (annotation.authenticationType() == Resource.AuthenticationType.CONTAINER) {
                            resourceRef.setResAuth(ResAuth.CONTAINER);
                        } else if (annotation.authenticationType() == Resource.AuthenticationType.APPLICATION) {
                            resourceRef.setResAuth(ResAuth.APPLICATION);
                        }

                        // sharing scope
                        resourceRef.setResSharingScope(annotation.shareable() ? ResSharingScope.SHAREABLE : ResSharingScope.UNSHAREABLE);

                        // mappedName
                        String mappdedNameAnnotation = annotation.mappedName();
                        if (!mappdedNameAnnotation.equals("")) {
                            resourceRef.setMappedName(mappdedNameAnnotation);
                        }

                        // lookup
                        String lookup = annotation.lookup();
                        if (!lookup.equals("")) {
                            resourceRef.setLookupName(lookup);
                        }
                        annotatedApp.getResourceRef().add(resourceRef);
                    }
                    catch (Exception anyException) {
                        log.debug("ResourceRefBuilder: Exception caught while processing <resource-ref>");
                    }
                }

                if (method != null || field != null) {
                    Set<InjectionTarget> targets = resourceRef.getInjectionTarget();
                    if (!hasTarget(method, field, targets)) {
                        resourceRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                    }
                }

                return true;
            }

            return false;
        }

        private boolean isManagedBeanReference(Class<?> resourceTypeClass, Resource annotation) {
            // Check if this is @Resource managedBean injection. Handle two cases:
            // 1) @Resource managedBeanClass; or
            // 2) @Resource(lookup='...') managedBeanInterfaceClass;
            if (resourceTypeClass.isAnnotationPresent(ManagedBean.class) ||
                (resourceTypeClass.isInterface() && annotation.lookup().length() != 0)) {
                return true;
            }
            return false;
        }
    }

}
