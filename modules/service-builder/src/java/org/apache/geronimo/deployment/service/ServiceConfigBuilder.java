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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.beans.PropertyEditorManager;
import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ModuleDocument;
import org.apache.geronimo.deployment.xbeans.ModuleType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ReferencesType;
import org.apache.geronimo.deployment.xbeans.XmlAttributeType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferenceMap;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilder implements ConfigurationBuilder {
    private final Environment defaultEnvironment;
    private final Collection repositories;

    //TODO this being static is a really good argument that all other builders should have a reference to this gbean, not use static methods on it.
    private static final Map xmlAttributeBuilderMap = new HashMap();
    private static final Map xmlReferenceBuilderMap = new HashMap();
    private Map attrRefMap;
    private Map refRefMap;
    private static final QName SERVICE_QNAME = ModuleDocument.type.getDocumentElementName();
    private final Naming naming;
    private final ConfigurationManager configurationManager;

    public ServiceConfigBuilder(Environment defaultEnvironment, Collection repositories, Naming naming) {
        this(defaultEnvironment, repositories, null, null, naming, null);
    }

    public ServiceConfigBuilder(Environment defaultEnvironment, Collection repositories, Collection xmlAttributeBuilders, Collection xmlReferenceBuilders, Kernel kernel) {
        this(defaultEnvironment, repositories, xmlAttributeBuilders, xmlReferenceBuilders, kernel.getNaming(), ConfigurationUtil.getConfigurationManager(kernel));
    }

    public ServiceConfigBuilder(Environment defaultEnvironment, Collection repositories, Collection xmlAttributeBuilders, Collection xmlReferenceBuilders, Naming naming) {
        this(defaultEnvironment, repositories, xmlAttributeBuilders, xmlReferenceBuilders, naming, null);
    }
    private ServiceConfigBuilder(Environment defaultEnvironment, Collection repositories, Collection xmlAttributeBuilders, Collection xmlReferenceBuilders, Naming naming, ConfigurationManager configurationManager) {
        this.naming = naming;
        this.configurationManager = configurationManager;

        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        xmlAttributeBuilderMap.put(environmentBuilder.getNamespace(), environmentBuilder);
        //cf registering EnvironmentBuilder as a property editor in the static gbeaninfo block.
        this.defaultEnvironment = defaultEnvironment;

        this.repositories = repositories;
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

    public Object getDeploymentPlan(File planFile, JarFile jarFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (planFile == null && jarFile == null) {
            return null;
        }

        try {
            XmlObject xmlObject;
            if (planFile != null) {
                xmlObject = XmlBeansUtil.parse(planFile.toURL());
            } else {
                URL path = DeploymentUtil.createJarURL(jarFile, "META-INF/geronimo-service.xml");
                try {
                    xmlObject = XmlBeansUtil.parse(path);
                } catch (FileNotFoundException e) {
                    // It has a JAR but no plan, and nothing at META-INF/geronimo-service.xml,
                    // therefore it's not a service deployment
                    return null;
                }
            }
            if(xmlObject == null) {
                return null;
            }

            XmlCursor cursor = xmlObject.newCursor();
            try {
                cursor.toFirstChild();
                if (!SERVICE_QNAME.equals(cursor.getName())) {
                    return null;
                }
            } finally {
                cursor.dispose();
            }
            ModuleDocument moduleDoc;
            if (xmlObject instanceof ModuleDocument) {
                moduleDoc = (ModuleDocument) xmlObject;
            } else {
                moduleDoc = (ModuleDocument) xmlObject.changeType(ModuleDocument.type);
            }
            Collection errors = new ArrayList();
            if (!moduleDoc.validate(XmlBeansUtil.createXmlOptions(errors))) {
                throw new DeploymentException("Invalid deployment descriptor: " + errors + "\nDescriptor: " + moduleDoc.toString());
            }
            // If there's no artifact ID and we won't be able to figure one out later, use the plan file name.  Bit of a hack.
            if(jarFile == null && (moduleDoc.getModule().getEnvironment() == null ||
                        moduleDoc.getModule().getEnvironment().getModuleId() == null ||
                        moduleDoc.getModule().getEnvironment().getModuleId().getArtifactId() == null)) {
                if(moduleDoc.getModule().getEnvironment() == null) {
                    moduleDoc.getModule().addNewEnvironment();
                }
                if(moduleDoc.getModule().getEnvironment().getModuleId() == null) {
                    moduleDoc.getModule().getEnvironment().addNewModuleId();
                }
                String name = planFile.getName();
                int pos = name.lastIndexOf('.');
                if(pos > -1) {
                    name = name.substring(0, pos);
                }
                moduleDoc.getModule().getEnvironment().getModuleId().setArtifactId(name);
            }
            return moduleDoc.getModule();
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse xml in plan", e);
        } catch (IOException e) {
            throw new DeploymentException("no plan at " + planFile, e);
        }
    }

    public Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException {
        ModuleType configType = (ModuleType) plan;
        EnvironmentType environmentType = configType.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        idBuilder.resolve(environment, module == null ? "" : new File(module.getName()).getName(), "car");
        if(!environment.getConfigId().isResolved()) {
            throw new IllegalStateException("Service Module ID is not fully populated ("+environment.getConfigId()+")");
        }
        return environment.getConfigId();
    }

    public DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, Object plan, JarFile jar, Collection configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException {
        ModuleType configType = (ModuleType) plan;

        return buildConfiguration(inPlaceDeployment, configId, configType, jar, configurationStores, artifactResolver, targetConfigurationStore);
    }

    public DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, ModuleType moduleType, JarFile jar, Collection configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws DeploymentException, IOException {
        ArtifactType type = moduleType.getEnvironment().isSetModuleId() ? moduleType.getEnvironment().getModuleId() : moduleType.getEnvironment().addNewModuleId();
        type.setArtifactId(configId.getArtifactId());
        type.setGroupId(configId.getGroupId());
        type.setType(configId.getType());
        type.setVersion(configId.getVersion().toString());
        Environment environment = EnvironmentBuilder.buildEnvironment(moduleType.getEnvironment(), defaultEnvironment);
        if(!environment.getConfigId().isResolved()) {
            throw new IllegalStateException("Module ID should be fully resolved by now (not "+environment.getConfigId()+")");
        }
        File outfile;
        try {
            outfile = targetConfigurationStore.createNewConfigurationDir(configId);
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException(e);
        }

        DeploymentContext context = null;
        try {
            ConfigurationManager configurationManager = this.configurationManager;
            if (configurationManager == null) {
                configurationManager = new SimpleConfigurationManager(configurationStores, artifactResolver, repositories);
            }

            context = new DeploymentContext(outfile,
                    inPlaceDeployment && null != jar ? DeploymentUtil.toFile(jar) : null,
                    environment,
                    ConfigurationModuleType.SERVICE,
                    naming,
                    configurationManager,
                    repositories);
            if(jar != null) {
                File file = new File(jar.getName());
                context.addIncludeAsPackedJar(URI.create(file.getName()), jar);
            }

            ClassLoader cl = context.getClassLoader();


            AbstractName moduleName = naming.createRootName(configId, configId.toString(), NameFactory.SERVICE_MODULE);
            GbeanType[] gbeans = moduleType.getGbeanArray();

            addGBeans(gbeans, cl, moduleName, context);
            return context;
        } catch (DeploymentException de) {
            cleanupAfterFailedBuild(context, outfile);
            throw de;
        } catch (IOException ie) {
            cleanupAfterFailedBuild(context, outfile);
            throw ie;
        } catch (RuntimeException re) {
            cleanupAfterFailedBuild(context, outfile);
            throw re;
        } catch (Error e) {
            cleanupAfterFailedBuild(context, outfile);
            throw e;
        }
    }

    private void cleanupAfterFailedBuild(DeploymentContext context, File directory) {
        try {
            if (context !=null) {
                context.close();
            }
        } catch (DeploymentException de) {
            // ignore error on cleanup
        } catch (IOException ioe) {
            // ignore error on cleanu
        }
        if (directory != null) {
            DeploymentUtil.recursiveDelete(directory);
        }
    }

    public static void addGBeans(GbeanType[] gbeans, ClassLoader cl, AbstractName moduleName, DeploymentContext context) throws DeploymentException {
        for (int i = 0; i < gbeans.length; i++) {
            addGBeanData(gbeans[i], moduleName, cl, context);
        }
    }

    public static AbstractName addGBeanData(GbeanType gbean, AbstractName moduleName, ClassLoader cl, DeploymentContext context) throws DeploymentException {
        GBeanInfo gBeanInfo = GBeanInfo.getGBeanInfo(gbean.getClass1(), cl);
        String namePart = gbean.getName();
        String j2eeType = gBeanInfo.getJ2eeType();
        AbstractName abstractName = context.getNaming().createChildName(moduleName, namePart, j2eeType);
        GBeanBuilder builder = new GBeanBuilder(abstractName, gBeanInfo, cl, context, moduleName, xmlAttributeBuilderMap, xmlReferenceBuilderMap);

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
                builder.setReference(referenceArray[j].getName2(), referenceArray[j], moduleName);
            }
        }

        // set up app multi-patterned references
        ReferencesType[] referencesArray = gbean.getReferencesArray();
        if (referencesArray != null) {
            for (int j = 0; j < referencesArray.length; j++) {
                builder.setReference(referencesArray[j].getName(), referencesArray[j].getPatternArray(), moduleName);
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
                builder.addDependency(patternType);
            }
        }

        GBeanData gbeanData = builder.getGBeanData();
        try {
            context.addGBean(gbeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException(e);
        }
        return abstractName;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        PropertyEditorManager.registerEditor(Environment.class, EnvironmentBuilder.class);

        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServiceConfigBuilder.class, NameFactory.CONFIG_BUILDER);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.addAttribute("defaultEnvironment", Environment.class, true);
        infoFactory.addReference("Repository", Repository.class, "Repository");
        infoFactory.addReference("XmlAttributeBuilders", XmlAttributeBuilder.class, "XmlAttributeBuilder");
        infoFactory.addReference("XmlReferenceBuilders", XmlReferenceBuilder.class, "XmlReferenceBuilder");
        infoFactory.addAttribute("kernel", Kernel.class, false, false);

        infoFactory.setConstructor(new String[]{"defaultEnvironment", "Repository", "XmlAttributeBuilders", "XmlReferenceBuilders", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
