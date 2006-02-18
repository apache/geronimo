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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ReferencesType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xbeans.XmlAttributeType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferenceMap;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilder implements ConfigurationBuilder {
    private final Environment defaultEnvironment;
    private final Repository repository;
    private final Kernel kernel;

    //TODO this being static is a really good argument that all other builders should have a reference to this gbean, not use static methods on it.
    private static final Map xmlAttributeBuilderMap = new HashMap();
    private static final Map xmlReferenceBuilderMap = new HashMap();
    private Map attrRefMap;
    private Map refRefMap;
    private static final QName SERVICE_QNAME = ConfigurationDocument.type.getDocumentElementName();


    public ServiceConfigBuilder(Environment defaultEnvironment, Repository repository) {
        this(defaultEnvironment, repository, null, null, null);
    }

    public ServiceConfigBuilder(Environment defaultEnvironment, Repository repository, Collection xmlAttributeBuilders, Collection xmlReferenceBuilders, Kernel kernel) {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        xmlAttributeBuilderMap.put(environmentBuilder.getNamespace(), environmentBuilder);
        this.defaultEnvironment = defaultEnvironment;

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

    public Artifact getConfigurationID(Object plan, JarFile module) throws IOException, DeploymentException {
        ConfigurationType configType = (ConfigurationType) plan;
        EnvironmentType environmentType = configType.getEnvironment();
        //TODO default id based on name?
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        return environment.getConfigId();
    }

    public ConfigurationData buildConfiguration(Object plan, JarFile unused, ConfigurationStore configurationStore) throws IOException, DeploymentException {
        ConfigurationType configType = (ConfigurationType) plan;

        return buildConfiguration(configType, configurationStore);
    }

    public ConfigurationData buildConfiguration(ConfigurationType configurationType, ConfigurationStore configurationStore) throws DeploymentException, IOException {

        Environment environment = EnvironmentBuilder.buildEnvironment(configurationType.getEnvironment(), defaultEnvironment);
        Artifact configId = environment.getConfigId();
        File outfile = configurationStore.createNewConfigurationDir(configId);
        DeploymentContext context = new DeploymentContext(outfile, environment, ConfigurationModuleType.SERVICE, kernel);
        ClassLoader cl = context.getClassLoader(repository);


        J2eeContext j2eeContext = null;
        try {
            j2eeContext = NameFactory.buildJ2eeContext(environment.getProperties(), NameFactory.NULL, NameFactory.J2EE_MODULE, environment.getConfigId().toString(), null, null);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }
        GbeanType[] gbeans = configurationType.getGbeanArray();
        addGBeans(gbeans, cl, j2eeContext, context);
        context.close();
        return context.getConfigurationData();
    }
    public static void addIncludes(DeploymentContext context, ArtifactType[] includes, Repository repository) throws DeploymentException {
        for (int i = 0; i < includes.length; i++) {
            ArtifactType include = includes[i];
            Artifact artifact = getDependencyURI(include, repository);
            String name = getDependencyFileName(include);
            URI path;
            try {
                path = new URI(name);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to generate path for include: " + artifact, e);
            }
            try {
                File file = repository.getLocation(artifact);
                context.addInclude(path, file.toURL());
            } catch (IOException e) {
                throw new DeploymentException("Unable to add include: " + artifact, e);
            }
        }
    }

    //TODO this is part of Environment resolution
    public static void addDependencies(DeploymentContext context, ArtifactType[] deps, Repository repository) throws DeploymentException {
        for (int i = 0; i < deps.length; i++) {
            Artifact artifact = getDependencyURI(deps[i], repository);
//            context.addDependency(dependencyURI);

            URL url;
            try {
                File location = repository.getLocation(artifact);
                url = location.toURL();
            } catch (MalformedURLException e) {
                throw new DeploymentException("Unable to get URL for dependency " + artifact, e);
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
                ArtifactType[] dependencyDeps = serviceDoc.getService().getDependencyArray();
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

    private static Artifact getDependencyURI(ArtifactType dep, Repository repository) throws DeploymentException {
        Artifact artifact = EnvironmentBuilder.toArtifact(dep);
        if (!repository.contains(artifact)) {
            throw new DeploymentException(new MissingDependencyException("Artifact " + artifact + " not found in repository"));
        }
        return artifact;
    }

    private static String getDependencyFileName(ArtifactType dep) {
        String type = dep.isSetType() ? dep.getType().trim() : "jar";
        String artifactId = dep.getArtifactId().trim();
        String version = dep.getVersion().trim();
        String name = artifactId + "-" + version + "." + type;
        return name;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServiceConfigBuilder.class, NameFactory.CONFIG_BUILDER);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.addAttribute("defaultEnvironment", Environment.class, true);
        infoFactory.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("XmlAttributeBuilders", XmlAttributeBuilder.class, "XmlAttributeBuilder");
        infoFactory.addReference("XmlReferenceBuilders", XmlReferenceBuilder.class, "XmlReferenceBuilder");
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{"defaultEnvironment", "Repository", "XmlAttributeBuilders", "XmlReferenceBuilders", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
