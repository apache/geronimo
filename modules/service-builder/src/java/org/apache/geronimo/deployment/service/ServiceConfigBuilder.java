/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ReferencesType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xbeans.XmlAttributeType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferenceMap;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilder implements ConfigurationBuilder {
    private final List defaultParentId;
    private final Repository repository;
    private final Kernel kernel;

    //TODO this being static is a really good argument that all other builders should have a reference to this gbean, not use static methods on it.
    private static final Map xmlAttributeBuilderMap = new HashMap();
    private static final Map xmlReferenceBuilderMap = new HashMap();
    private Map attrRefMap;
    private Map refRefMap;
    private static final QName SERVICE_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.0", "configuration");


    public ServiceConfigBuilder(URI[] defaultParentId, Repository repository) {
        this(defaultParentId, repository, null, null, null);
    }

    public ServiceConfigBuilder(URI[] defaultParentId, Repository repository, Collection xmlAttributeBuilders, Collection xmlReferenceBuilders, Kernel kernel) {
        this.defaultParentId = defaultParentId == null? Collections.EMPTY_LIST: Arrays.asList(defaultParentId);

        this.repository = repository;
        this.kernel = kernel;
        if (xmlAttributeBuilders != null) {
            ReferenceMap.Key key = new ReferenceMap.Key() {

                public Object getKey(Object object) {
                    return ((XmlAttributeBuilder) object).getNamespace();
                }
            };
            attrRefMap = new ReferenceMap(xmlAttributeBuilders, xmlAttributeBuilderMap, key);
        }
        if (xmlReferenceBuilders != null) {
            ReferenceMap.Key key = new ReferenceMap.Key() {

                public Object getKey(Object object) {
                    return ((XmlReferenceBuilder) object).getNamespace();
                }
            };
            refRefMap = new ReferenceMap(xmlReferenceBuilders, xmlReferenceBuilderMap, key);
        }
    }

    public Object getDeploymentPlan(File planFile, JarFile module) throws DeploymentException {
        if (planFile == null) {
            return null;
        }

        try {
            XmlObject xmlObject = XmlBeansUtil.parse(planFile);
            XmlCursor cursor = xmlObject.newCursor();
            try {
                cursor.toFirstChild();
                if (!SERVICE_QNAME.equals(cursor.getName())) {
                    return null;
                }
            } finally {
                cursor.dispose();
            }
            ConfigurationDocument configurationDoc;
            if (xmlObject instanceof ConfigurationDocument) {
                configurationDoc = (ConfigurationDocument) xmlObject;
            } else {
                configurationDoc = (ConfigurationDocument) xmlObject.changeType(ConfigurationDocument.type);
            }
            Collection errors = new ArrayList();
            if (!configurationDoc.validate(XmlBeansUtil.createXmlOptions(errors))) {
                throw new DeploymentException("Invalid deployment descriptor: " + errors + "\nDescriptor: " + configurationDoc.toString());
            }
            return configurationDoc.getConfiguration();
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse xml in plan", e);
        } catch (IOException e) {
            throw new DeploymentException("no plan at " + planFile, e);
        }
    }

    public URI getConfigurationID(Object plan, JarFile module) throws IOException, DeploymentException {
        ConfigurationType configType = (ConfigurationType) plan;
        try {
            return new URI(configType.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + configType.getConfigId(), e);
        }
    }

    public ConfigurationData buildConfiguration(Object plan, JarFile unused, File outfile) throws IOException, DeploymentException {
        ConfigurationType configType = (ConfigurationType) plan;
        String domain = null;
        String server = null;

        return buildConfiguration(configType, domain, server, outfile);
    }

    public ConfigurationData buildConfiguration(ConfigurationType configType, String domain, String server, File outfile) throws DeploymentException, IOException {
        List parentID = getParentID(configType.getParentId(), configType.getImportArray());
        if (parentID == null || parentID.size() == 0) {
            if (configType.isSetDomain()) {
                if (!configType.isSetServer()) {
                    throw new DeploymentException("You must set both domain and server");
                }
                domain = configType.getDomain();
                server = configType.getServer();
            } else {
                parentID = defaultParentId;
            }
        }

        if (domain == null) {
            //get from parent id
            if (kernel == null) {
                throw new DeploymentException("You must supply a kernel or the domain and server names");
            }
        }

        URI configID;
        try {
            configID = new URI(configType.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + configType.getConfigId(), e);
        }

        DeploymentContext context = null;
        context = new DeploymentContext(outfile, configID, ConfigurationModuleType.SERVICE, parentID, domain, server, kernel);

        J2eeContext j2eeContext = new J2eeContextImpl(context.getDomain(), context.getServer(), NameFactory.NULL, NameFactory.J2EE_MODULE, configID.toString(), null, null);
        DependencyType[] includes = configType.getIncludeArray();
        addIncludes(context, includes, repository);
        addDependencies(context, configType.getDependencyArray(), repository);
        ClassLoader cl = context.getClassLoader(repository);
        GbeanType[] gbeans = configType.getGbeanArray();
        addGBeans(gbeans, cl, j2eeContext, context);
        if (configType.isSetInverseClassloading()) {
            context.setInverseClassloading(configType.getInverseClassloading());
        }
        ClassFilterType[] filters = configType.getHiddenClassesArray();
        addHiddenClasses(context, filters);
        filters = configType.getNonOverridableClassesArray();
        addNonOverridableClasses(context, filters);
        context.close();
        return context.getConfigurationData();
    }

    public static void addHiddenClasses(DeploymentContext context, ClassFilterType[] filters) throws DeploymentException {
        Set tmpFilters = new HashSet();
        for (int i = 0; i < filters.length; i++) {
            tmpFilters.add(filters[i].getFilter());
        }
        context.addHiddenClasses(tmpFilters);
    }

    public static void addNonOverridableClasses(DeploymentContext context, ClassFilterType[] filters) throws DeploymentException {
        Set tmpFilters = new HashSet();
        for (int i = 0; i < filters.length; i++) {
            tmpFilters.add(filters[i].getFilter());
        }
        context.addNonOverridableClasses(tmpFilters);
    }

    public static List getParentID(String parentIDString, DependencyType[] imports) throws DeploymentException {
        List uris = new ArrayList();
        if (parentIDString != null) {
            try {
                uris.add(new URI(parentIDString));
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + parentIDString, e);
            }
        } else if (imports.length == 0) {
            return new ArrayList();
        }
        for (int i = 0; i < imports.length; i++) {
            DependencyType anImport = imports[i];
            URI parentURI = getDependencyURI(anImport);
            uris.add(parentURI);
        }
        return uris;
    }

    public static void addIncludes(DeploymentContext context, DependencyType[] includes, Repository repository) throws DeploymentException {
        for (int i = 0; i < includes.length; i++) {
            DependencyType include = includes[i];
            URI uri = getDependencyURI(include, repository);
            String name = uri.toString();
            int idx = name.lastIndexOf('/');
            if (idx != -1) {
                name = name.substring(idx + 1);
            }
            URI path;
            try {
                path = new URI(name);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to generate path for include: " + uri, e);
            }
            try {
                URL url = repository.getURL(uri);
                context.addInclude(path, url);
            } catch (IOException e) {
                throw new DeploymentException("Unable to add include: " + uri, e);
            }
        }
    }

    public static void addDependencies(DeploymentContext context, DependencyType[] deps, Repository repository) throws DeploymentException {
        for (int i = 0; i < deps.length; i++) {
            URI dependencyURI = getDependencyURI(deps[i], repository);
            context.addDependency(dependencyURI);

            URL url;
            try {
                url = repository.getURL(dependencyURI);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Unable to get URL for dependency " + dependencyURI, e);
            }
            ClassLoader depCL = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
            InputStream is = depCL.getResourceAsStream("META-INF/geronimo-service.xml");
            if (is != null) {
                // it has a geronimo-service.xml file
                ServiceDocument serviceDoc = null;
                try {
                    Collection errors = new ArrayList();
                    serviceDoc = ServiceDocument.Factory.parse(is, XmlBeansUtil.createXmlOptions(errors));
                    if (errors.size() > 0) {
                        throw new DeploymentException("Invalid service doc: " + errors);
                    }
                } catch (XmlException e) {
                    throw new DeploymentException("Invalid geronimo-service.xml file in " + url, e);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to parse geronimo-service.xml file in " + url, e);
                }
                DependencyType[] dependencyDeps = serviceDoc.getService().getDependencyArray();
                if (dependencyDeps != null) {
                    addDependencies(context, dependencyDeps, repository);
                }
            }
        }
    }

    public static void addGBeans(GbeanType[] gbeans, ClassLoader cl, J2eeContext j2eeContext, DeploymentContext context) throws DeploymentException {
        for (int i = 0; i < gbeans.length; i++) {
            addGBeanData(gbeans[i], j2eeContext, cl, context);
        }
    }

    public static ObjectName addGBeanData(GbeanType gbean, J2eeContext j2eeContext, ClassLoader cl, DeploymentContext context) throws DeploymentException {
        GBeanInfo gBeanInfo = GBeanInfo.getGBeanInfo(gbean.getClass1(), cl);
        ObjectName objectName;
        if (gbean.isSetGbeanName()) {
            try {
                objectName = ObjectName.getInstance(gbean.getGbeanName());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName: " + gbean.getName(), e);
            }
        } else {
            String namePart = gbean.getName();
            try {
                String j2eeType = gBeanInfo.getJ2eeType();
                //todo investigate using the module type from the j2eecontext.
                objectName = NameFactory.getComponentName(null, null, null, null, namePart, j2eeType, j2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName: " + namePart, e);
            }
        }
        GBeanBuilder builder = new GBeanBuilder(objectName, gBeanInfo, cl, context, j2eeContext, xmlAttributeBuilderMap, xmlReferenceBuilderMap);

        // set up attributes
        AttributeType[] attributeArray = gbean.getAttributeArray();
        if (attributeArray != null) {
            for (int j = 0; j < attributeArray.length; j++) {
                builder.setAttribute(attributeArray[j].getName().trim(), attributeArray[j].getType(), attributeArray[j].getStringValue());
            }
        }

        XmlAttributeType[] xmlAttributeArray = gbean.getXmlAttributeArray();
        if (xmlAttributeArray != null) {
            for (int i = 0; i < xmlAttributeArray.length; i++) {
                XmlAttributeType xmlAttributeType = xmlAttributeArray[i];
                String name = xmlAttributeType.getName().trim();
                XmlObject[] anys = xmlAttributeType.selectChildren(XmlAttributeType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in xml-attribute " + anys.length + " qnameset: " + XmlAttributeType.type.qnameSetForWildcardElements());
                }
                builder.setXmlAttribute(name, anys[0]);
            }
        }

        // set up all single pattern references
        ReferenceType[] referenceArray = gbean.getReferenceArray();
        if (referenceArray != null) {
            for (int j = 0; j < referenceArray.length; j++) {
                builder.setReference(referenceArray[j].getName2(), referenceArray[j], j2eeContext);
            }
        }

        // set up app multi-patterned references
        ReferencesType[] referencesArray = gbean.getReferencesArray();
        if (referencesArray != null) {
            for (int j = 0; j < referencesArray.length; j++) {
                builder.setReference(referencesArray[j].getName(), referencesArray[j].getPatternArray(), j2eeContext);
            }
        }

        XmlAttributeType[] xmlReferenceArray = gbean.getXmlReferenceArray();
        if (xmlReferenceArray != null) {
            for (int i = 0; i < xmlReferenceArray.length; i++) {
                XmlAttributeType xmlAttributeType = xmlReferenceArray[i];
                String name = xmlAttributeType.getName().trim();
                XmlObject[] anys = xmlAttributeType.selectChildren(XmlAttributeType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in xml-attribute " + anys.length + " qnameset: " + XmlAttributeType.type.qnameSetForWildcardElements());
                }
                builder.setXmlReference(name, anys[0]);
            }
        }

        PatternType[] dependencyArray = gbean.getDependencyArray();
        if (dependencyArray != null) {
            for (int i = 0; i < dependencyArray.length; i++) {
                PatternType patternType = dependencyArray[i];
                builder.addDependency(patternType, j2eeContext);
            }
        }

        GBeanData gBeanData = builder.getGBeanData();
        context.addGBean(gBeanData);
        return objectName;
    }

    private static URI getDependencyURI(DependencyType dep, Repository repository) throws DeploymentException {
        URI uri = getDependencyURI(dep);
        if (!repository.hasURI(uri)) {
            throw new DeploymentException(new MissingDependencyException("uri " + uri + " not found in repository"));
        }
        return uri;
    }

    private static URI getDependencyURI(DependencyType dep) throws DeploymentException {
        URI uri;
        if (dep.isSetUri()) {
            try {
                uri = new URI(dep.getUri());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid dependency URI " + dep.getUri(), e);
            }
        } else {
            String groupId = dep.getGroupId();
            String type = dep.isSetType() ? dep.getType() : "jar";
            String artifactId = dep.getArtifactId();
            String version = dep.getVersion();
            String id = groupId + "/" + artifactId + "/" + version + "/" + type;
            try {
                uri = new URI(id);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to construct URI for groupId=" + dep.getGroupId() + ", artifactId=" + dep.getArtifactId() + ", version=" + dep.getVersion(), e);
            }
        }
        return uri;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServiceConfigBuilder.class, NameFactory.CONFIG_BUILDER);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.addAttribute("defaultParentId", URI[].class, true);
        infoFactory.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("XmlAttributeBuilders", XmlAttributeBuilder.class, "XmlAttributeBuilder");
        infoFactory.addReference("XmlReferenceBuilders", XmlReferenceBuilder.class, "XmlReferenceBuilder");
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{"defaultParentId", "Repository", "XmlAttributeBuilders", "XmlReferenceBuilders", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
