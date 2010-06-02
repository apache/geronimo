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

import javax.annotation.Resource;
import javax.resource.ResourceException;
import javax.xml.namespace.QName;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.deployment.CorbaGBeanNameSource;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
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
import org.apache.geronimo.naming.reference.ResourceReferenceFactory;
import org.apache.geronimo.naming.reference.URLReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.javaee6.DescriptionType;
import org.apache.geronimo.xbeans.javaee6.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.JndiNameType;
import org.apache.geronimo.xbeans.javaee6.ResAuthType;
import org.apache.geronimo.xbeans.javaee6.ResSharingScopeType;
import org.apache.geronimo.xbeans.javaee6.ResourceRefType;
import org.apache.geronimo.xbeans.javaee6.XsdStringType;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.omg.CORBA.ORB;
import org.osgi.framework.Bundle;

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

    public ResourceRefBuilder(
            @ParamAttribute(name = "defaultEnvironment")Environment defaultEnvironment,
            @ParamAttribute(name = "corbaEnvironment")Environment corbaEnvironment,
            @ParamAttribute(name = "eeNamespaces")String[] eeNamespaces,
            @ParamReference(name = "CorbaGBeanNameSource")Collection<CorbaGBeanNameSource> corbaGBeanNameSourceCollection) {
        super(defaultEnvironment);

        resourceRefQNameSet = buildQNameSet(eeNamespaces, "resource-ref");
        this.corbaEnvironment = corbaEnvironment;
        this.corbaGBeanNameSourceCollection = new SingleElementCollection<CorbaGBeanNameSource>(corbaGBeanNameSourceCollection);
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(resourceRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> componentContext) throws DeploymentException {

        // Discover and process any @Resource annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {

            // Process all the annotations for this naming builder type
            try {
                ResourceAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder(), ResourceRefProcessor.INSTANCE);
            }
            catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
            }
        }

        List<ResourceRefType> resourceRefsUntyped = convert(specDD.selectChildren(resourceRefQNameSet), J2EE_CONVERTER, ResourceRefType.class, ResourceRefType.type);
        XmlObject[] gerResourceRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_RESOURCE_REF_QNAME_SET);
        Map<String, GerResourceRefType> refMap = mapResourceRefs(gerResourceRefsUntyped);
        List<String> unresolvedRefs = new ArrayList<String>();
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        for (ResourceRefType resourceRef : resourceRefsUntyped) {
            String name = getStringValue(resourceRef.getResRefName());
            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already
                continue;
            }
            addInjections(name, resourceRef.getInjectionTargetArray(), componentContext);
            String type = getStringValue(resourceRef.getResType());
            type = inferAndCheckType(module, bundle, resourceRef.getInjectionTargetArray(), name, type);
            GerResourceRefType gerResourceRef = refMap.get(name);
            log.debug("trying to resolve " + name + ", type " + type + ", resourceRef " + gerResourceRef);

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
                put(name, value, module.getJndiContext());
            }

        }

        if (unresolvedRefs.size() > 0) {
            log.warn("Failed to build reference to resource reference " + unresolvedRefs + " defined in plan file. The corresponding entry in Geronimo deployment descriptor is missing.");
        }
    }

    private Object buildReference(Module module, String name, String type, GerResourceRefType gerResourceRef) throws DeploymentException {
        Bundle bundle = module.getEarContext().getDeploymentBundle();

        Class iface;
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
        } else if (ORB.class.isAssignableFrom(iface)) {
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
        } else {
            //determine jsr-77 type from interface
            String j2eeType;

            if (JAVAX_MAIL_SESSION_CLASS.equals(type)) {
                j2eeType = NameFactory.JAVA_MAIL_RESOURCE;
            } else if (JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {
                j2eeType = NameFactory.JAXR_CONNECTION_FACTORY;
            } else {
                j2eeType = NameFactory.JCA_CONNECTION_FACTORY;
            }
            try {
                AbstractNameQuery containerId = getResourceContainerId(name, j2eeType, null, gerResourceRef);

                module.getEarContext().findGBean(containerId);

                return new ResourceReferenceFactory<ResourceException>(module.getConfigId(), containerId, iface);
            } catch (GBeanNotFoundException e) {
                StringBuffer errorMessage = new StringBuffer("Unable to resolve resource reference '");
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

    public void setResourceEnvironment(ResourceEnvironmentBuilder builder, XmlObject[] resourceRefs, GerResourceRefType[] gerResourceRefs) throws DeploymentException {
        List<ResourceRefType> resourceRefList = convert(resourceRefs, J2EE_CONVERTER, ResourceRefType.class, ResourceRefType.type);
        Map refMap = mapResourceRefs(gerResourceRefs);
        Set<AbstractNameQuery> unshareableResources = new HashSet<AbstractNameQuery>();
        Set<AbstractNameQuery> applicationManagedSecurityResources = new HashSet<AbstractNameQuery>();
        for (ResourceRefType resourceRefType : resourceRefList) {

            String type = resourceRefType.getResType().getStringValue().trim();

            if (!URL.class.getName().equals(type)
                    && !"javax.mail.Session".equals(type)
                    && !JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {

                GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(resourceRefType.getResRefName().getStringValue());
                AbstractNameQuery containerId = getResourceContainerId(getStringValue(resourceRefType.getResRefName()), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, null, gerResourceRef);

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

    private Map<String, GerResourceRefType> mapResourceRefs(XmlObject[] refs) {
        Map<String, GerResourceRefType> refMap = new HashMap<String, GerResourceRefType>();
        if (refs != null) {
            for (XmlObject ref1 : refs) {
                GerResourceRefType ref = (GerResourceRefType) ref1.copy().changeType(GerResourceRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
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

        public boolean processResource(AnnotatedApp annotatedApp, Resource annotation, Class cls, Method method, Field field) {
            log.debug("processResource( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                    "[annotation] " + annotation.toString() + "," + '\n' +
                    "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                    "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                    "[field] " + (field != null ? field.getName() : null) + " ): Entry");

            String resourceName = getResourceName(annotation, method, field);
            String resourceType = getResourceType(annotation, method, field);

            if (resourceType.equals("javax.sql.DataSource") ||
                    resourceType.equals("javax.mail.Session") ||
                    resourceType.equals("java.net.URL") ||
                    resourceType.equals("org.omg.CORBA.ORB") ||
                    resourceType.equals("org.omg.CORBA_2_3.ORB") ||
                    resourceType.equals("org.omg.CORBA_2_4.ORB") ||
                    resourceType.endsWith("ConnectionFactory")) {

                log.debug("processResource(): <resource-ref> found");

                boolean exists = false;
                ResourceRefType[] resourceRefs = annotatedApp.getResourceRefArray();
                for (ResourceRefType resourceRef : resourceRefs) {
                    if (resourceRef.getResRefName().getStringValue().trim().equals(resourceName)) {
                        if (method != null || field != null) {
                            InjectionTargetType[] targets = resourceRef.getInjectionTargetArray();
                            if (!hasTarget(method, field, targets)) {
                                configureInjectionTarget(resourceRef.addNewInjectionTarget(), method, field);
                            }
                        }
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    try {

                        log.debug("processResource(): Does not exist in DD: " + resourceName);

                        // Doesn't exist in deployment descriptor -- add new
                        ResourceRefType resourceRef = annotatedApp.addNewResourceRef();

                        //------------------------------------------------------------------------------
                        // <resource-ref> required elements:
                        //------------------------------------------------------------------------------

                        // resource-ref-name
                        JndiNameType resourceRefName = resourceRef.addNewResRefName();
                        resourceRefName.setStringValue(resourceName);

                        if (!resourceType.equals("")) {
                            // resource-ref-type
                            FullyQualifiedClassType qualifiedClass = resourceRef.addNewResType();
                            qualifiedClass.setStringValue(resourceType);
                        }
                        if (method != null || field != null) {
                            // injectionTarget
                            InjectionTargetType injectionTarget = resourceRef.addNewInjectionTarget();
                            configureInjectionTarget(injectionTarget, method, field);
                        }

                        //------------------------------------------------------------------------------
                        // <resource-ref> optional elements:
                        //------------------------------------------------------------------------------

                        // description
                        String descriptionAnnotation = annotation.description();
                        if (!descriptionAnnotation.equals("")) {
                            DescriptionType description = resourceRef.addNewDescription();
                            description.setStringValue(descriptionAnnotation);
                        }

                        // authentication
                        if (annotation.authenticationType() == Resource.AuthenticationType.CONTAINER) {
                            ResAuthType resAuth = resourceRef.addNewResAuth();
                            resAuth.setStringValue("Container");
                        } else if (annotation.authenticationType() == Resource.AuthenticationType.APPLICATION) {
                            ResAuthType resAuth = resourceRef.addNewResAuth();
                            resAuth.setStringValue("Application");
                        }

                        // sharing scope
                        ResSharingScopeType resScope = resourceRef.addNewResSharingScope();
                        resScope.setStringValue(annotation.shareable() ? "Shareable" : "Unshareable");

                        // mappedName
                        String mappdedNameAnnotation = annotation.mappedName();
                        if (!mappdedNameAnnotation.equals("")) {
                            XsdStringType mappedName = resourceRef.addNewMappedName();
                            mappedName.setStringValue(mappdedNameAnnotation);
                        }

                        // lookup
                        String lookup = annotation.lookup();
                        if (!lookup.equals("")) {
                            XsdStringType lookupName = resourceRef.addNewLookupName();
                            lookupName.setStringValue(lookup);
                        }
                    }
                    catch (Exception anyException) {
                        log.debug("ResourceRefBuilder: Exception caught while processing <resource-ref>");
                    }
                }
                return true;
            }
            return false;
        }
    }

}
