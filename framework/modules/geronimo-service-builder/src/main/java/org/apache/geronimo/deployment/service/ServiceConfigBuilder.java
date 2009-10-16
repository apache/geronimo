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

package org.apache.geronimo.deployment.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ModuleDocument;
import org.apache.geronimo.deployment.xbeans.ModuleType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = "ConfigBuilder")
public class ServiceConfigBuilder implements ConfigurationBuilder, GBeanLifecycle {
    private final Environment defaultEnvironment;
    private final Collection<Repository> repositories;

    private static final QName MODULE_QNAME = ModuleDocument.type.getDocumentElementName();
    public static final String SERVICE_MODULE = "ServiceModule";
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment-1.1", "http://geronimo.apache.org/xml/ns/deployment-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment/javabean", "http://geronimo.apache.org/xml/ns/deployment/javabean-1.0");
    }

    private final Naming naming;
    private final ConfigurationManager configurationManager;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final BundleContext bundleContext;

    public ServiceConfigBuilder(Environment defaultEnvironment, Collection<Repository> repositories, Naming naming, BundleContext bundleContext) {
        this(defaultEnvironment, repositories, Collections.<NamespaceDrivenBuilder>emptyList(), naming, null, bundleContext);
    }

    public ServiceConfigBuilder(@ParamAttribute(name="defaultEnvironment")Environment defaultEnvironment,
                                @ParamReference(name="Repository", namingType = "Repository")Collection<Repository> repositories,
                                @ParamReference(name="ServiceBuilders", namingType = "ModuleBuilder")Collection<NamespaceDrivenBuilder> serviceBuilders,
                                @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
                                @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws GBeanNotFoundException {
        this(defaultEnvironment, repositories, serviceBuilders, kernel.getNaming(), ConfigurationUtil.getConfigurationManager(kernel), bundleContext);
    }

    public ServiceConfigBuilder(Environment defaultEnvironment, Collection<Repository> repositories, Collection<NamespaceDrivenBuilder> serviceBuilders, Naming naming, BundleContext bundleContext) {
        this(defaultEnvironment, repositories, serviceBuilders, naming, null, bundleContext);
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
    }

    private ServiceConfigBuilder(Environment defaultEnvironment, Collection<Repository> repositories, Collection<NamespaceDrivenBuilder> serviceBuilders, Naming naming, ConfigurationManager configurationManager, BundleContext bundleContext) {
        this.naming = naming;
        this.configurationManager = configurationManager;

//        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        this.defaultEnvironment = defaultEnvironment;

        this.repositories = repositories;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.bundleContext = bundleContext;
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (planFile == null && jarFile == null) {
            return null;
        }

        try {
            XmlObject xmlObject;
            if (planFile != null) {
                xmlObject = XmlBeansUtil.parse(planFile.toURI().toURL(), getClass().getClassLoader());
            } else {
                URL path = DeploymentUtil.createJarURL(jarFile, "META-INF/geronimo-service.xml");
                try {
                    xmlObject = XmlBeansUtil.parse(path, getClass().getClassLoader());
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
                if (!MODULE_QNAME.equals(cursor.getName())) {
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
            XmlBeansUtil.validateDD(moduleDoc);
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

    public DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, Object plan, JarFile jar, Collection<ConfigurationStore> configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException {
        ModuleType configType = (ModuleType) plan;

        return buildConfiguration(inPlaceDeployment, configId, configType, jar, configurationStores, artifactResolver, targetConfigurationStore);
    }

    private DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, ModuleType moduleType, JarFile jar, Collection<ConfigurationStore> configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws DeploymentException, IOException {
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
                configurationManager = new SimpleConfigurationManager(configurationStores, artifactResolver, repositories, bundleContext);
            }

            AbstractName moduleName = naming.createRootName(configId, configId.toString(), SERVICE_MODULE);
            context = new DeploymentContext(outfile,
                    inPlaceDeployment && null != jar ? DeploymentUtil.toFile(jar) : null,
                    environment,
                    moduleName,
                    ConfigurationModuleType.SERVICE,
                    naming,
                    configurationManager,
                    repositories, bundleContext);
            if(jar != null) {
                File file = new File(jar.getName());
                context.addIncludeAsPackedJar(URI.create(file.getName()), jar);
            }
            context.initializeConfiguration();
            serviceBuilders.build(moduleType, context, context);
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

}
