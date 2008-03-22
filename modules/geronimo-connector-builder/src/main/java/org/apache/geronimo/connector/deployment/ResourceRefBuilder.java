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
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.geronimo.naming.reference.ORBReference;
import org.apache.geronimo.naming.reference.ResourceReference;
import org.apache.geronimo.naming.reference.URLReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.ResAuthType;
import org.apache.geronimo.xbeans.javaee.ResSharingScopeType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.omg.CORBA.ORB;

/**
 * @version $Rev$ $Date$
 */
public class ResourceRefBuilder extends AbstractNamingBuilder implements ResourceEnvironmentSetter {

    private static final Log log = LogFactory.getLog(ResourceRefBuilder.class);

    private static final QName GER_RESOURCE_REF_QNAME = GerResourceRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_RESOURCE_REF_QNAME_SET = QNameSet.singleton(GER_RESOURCE_REF_QNAME);
    private static final String JAXR_CONNECTION_FACTORY_CLASS = "javax.xml.registry.ConnectionFactory";
    private static final String JAVAX_MAIL_SESSION_CLASS = "javax.mail.Session";


    private final QNameSet resourceRefQNameSet;
    private final SingleElementCollection corbaGBeanNameSourceCollection;

    public ResourceRefBuilder(Environment defaultEnvironment, String[] eeNamespaces, Collection corbaGBeanNameSourceCollection) {
        super(defaultEnvironment);

        resourceRefQNameSet = buildQNameSet(eeNamespaces, "resource-ref");
        this.corbaGBeanNameSourceCollection = new SingleElementCollection(corbaGBeanNameSourceCollection);
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(resourceRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {

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
        Map refMap = mapResourceRefs(gerResourceRefsUntyped);
        List unresolvedRefs = new ArrayList();
        ClassLoader cl = module.getEarContext().getClassLoader();                
        for (ResourceRefType resourceRef : resourceRefsUntyped) {
            String name = resourceRef.getResRefName().getStringValue().trim();
            addInjections(name, resourceRef.getInjectionTargetArray(), componentContext);
            String type = resourceRef.getResType().getStringValue().trim();
            GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(name);
            log.debug("trying to resolve " + name + ", type " + type + ", resourceRef " + gerResourceRef);
            if(!refMap.containsKey(name)){
                unresolvedRefs.add(name);
            } 
            Class iface;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
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
                getJndiContextMap(componentContext).put(ENV + name, new URLReference(url));

            } else if (ORB.class.isAssignableFrom(iface)) {
                CorbaGBeanNameSource corbaGBeanNameSource = (CorbaGBeanNameSource) corbaGBeanNameSourceCollection.getElement();
                if (corbaGBeanNameSource == null) {
                    throw new DeploymentException("No orb setup but there is a orb reference");
                }
                AbstractNameQuery corbaName = corbaGBeanNameSource.getCorbaGBeanName();
                if (corbaName != null) {
                    Artifact[] moduleId = module.getConfigId();
                    Map context = getJndiContextMap(componentContext);
                    context.put(ENV + name, new ORBReference(moduleId, corbaName));
                }
            } else {
                //determine jsr-77 type from interface
                String j2eeType;


                if (JAVAX_MAIL_SESSION_CLASS.equals(type)) {
                    j2eeType = NameFactory.JAVA_MAIL_RESOURCE;
                } else if (JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {
                    j2eeType = NameFactory.JAXR_CONNECTION_FACTORY;
                } else {
                    j2eeType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY;
                }
                try {
                    AbstractNameQuery containerId = getResourceContainerId(name, j2eeType, null, gerResourceRef);

                    module.getEarContext().findGBean(containerId);

                    Reference ref = new ResourceReference(module.getConfigId(), containerId, iface);
                    getJndiContextMap(componentContext).put(ENV + name, ref);

                    // we thought that this might be an unresolved
                    // name because it wasn't in the refMap, but now
                    // we've found it so we can take it out of the
                    // unresolvedRefs list
                    unresolvedRefs.remove(name);
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
        }

        if (unresolvedRefs.size() > 0) {
            log.warn("Failed to build reference to resource reference "+ unresolvedRefs +" defined in plan file, reason - corresponding entry in deployment descriptor missing.");
        }
    }

    public void setResourceEnvironment(ResourceEnvironmentBuilder builder, XmlObject[] resourceRefs, GerResourceRefType[] gerResourceRefs) throws DeploymentException {
        List<ResourceRefType> resourceRefList = convert(resourceRefs, J2EE_CONVERTER, ResourceRefType.class, ResourceRefType.type);
        Map refMap = mapResourceRefs(gerResourceRefs);
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
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
                            resourceRef.setResAuth(resAuth);
                        } else if (annotation.authenticationType() == Resource.AuthenticationType.APPLICATION) {
                            ResAuthType resAuth = resourceRef.addNewResAuth();
                            resAuth.setStringValue("Application");
                            resourceRef.setResAuth(resAuth);
                        }

                        // sharing scope
                        ResSharingScopeType resScope = resourceRef.addNewResSharingScope();
                        resScope.setStringValue(annotation.shareable() ? "Shareable" : "Unshareable");
                        resourceRef.setResSharingScope(resScope);

                        // mappedName
                        String mappdedNameAnnotation = annotation.mappedName();
                        if (!mappdedNameAnnotation.equals("")) {
                            XsdStringType mappedName = resourceRef.addNewMappedName();
                            mappedName.setStringValue(mappdedNameAnnotation);
                            resourceRef.setMappedName(mappedName);
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


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ResourceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addReference("CorbaGBeanNameSource", CorbaGBeanNameSource.class);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces", "CorbaGBeanNameSource"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
