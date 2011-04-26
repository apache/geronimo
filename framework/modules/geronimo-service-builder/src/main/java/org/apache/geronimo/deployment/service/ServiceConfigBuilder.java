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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.plan.ArtifactType;
import org.apache.geronimo.deployment.service.plan.EnvironmentType;
import org.apache.geronimo.deployment.service.plan.JaxbUtil;
import org.apache.geronimo.deployment.service.plan.ModuleType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
@Component
@Service
public class ServiceConfigBuilder implements ConfigurationBuilder {

//    private static final QName MODULE_QNAME = ModuleDocument.type.getDocumentElementName();
    public static final String SERVICE_MODULE = "ServiceModule";
//    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
//    static {
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment", "http://geronimo.apache.org/xml/ns/deployment-1.2");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment-1.1", "http://geronimo.apache.org/xml/ns/deployment-1.2");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/deployment/javabean", "http://geronimo.apache.org/xml/ns/deployment/javabean-1.0");
//    }

    private final Naming naming = new Jsr77Naming();
//    private final NamespaceDrivenBuilder serviceBuilders = new GBeanBuilder(Collections.emptyList(), Collections.emptyList());
    private final GBeanBuilder serviceBuilders = new GBeanBuilder(Collections.emptyList(), Collections.emptyList());
    private BundleContext bundleContext;

//    public ServiceConfigBuilder(@ParamReference(name="Repository", namingType = "Repository")Collection<Repository> repositories,
//                                @ParamReference(name="ServiceBuilders", namingType = "ModuleBuilder")Collection<NamespaceDrivenBuilder> serviceBuilders,
//                                @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
//                                @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws GBeanNotFoundException {
//        this(repositories, serviceBuilders, kernel.getNaming(), bundleContext);
//    }

//    public void doStart() throws Exception {
//        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
//    }
//
//    public void doStop() {
//        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
//    }
//
//    public void doFail() {
//        doStop();
//    }

    public ServiceConfigBuilder() {
//        this.naming = new Jsr77Naming();

//        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
//        this.bundleContext = bundleContext;
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (planFile == null && jarFile == null) {
            return null;
        }

        try {
            ModuleType moduleType = null;
            if (planFile != null) {
                InputStream in = new FileInputStream(planFile);
                try {
//                    moduleType = JaxbUtil.unmarshal(ModuleType.class, in, false);
                    moduleType = JaxbUtil.unmarshalModule(in, false);
//                } catch (XMLStreamException e) {
//                     return null;
                } catch (JAXBException e) {
                    return null;
                } finally {
                    in.close();
                }
//                xmlObject = XmlBeansUtil.parse(planFile.toURI().toURL(), getClass().getClassLoader());
            } else {
                URL path = JarUtils.createJarURL(jarFile, "META-INF/geronimo-service.xml");
                try {
                    InputStream in = path.openStream();
                    try {
//                        moduleType = JaxbUtil.unmarshal(ModuleType.class, in, false);
                        moduleType = JaxbUtil.unmarshalModule(in, false);
                    } finally {
                        in.close();
                    }
                } catch (FileNotFoundException e) {
                    // It has a JAR but no plan, and nothing at META-INF/geronimo-service.xml,
                    // therefore it's not a service deployment
                    return null;
                }
            }
//            if(moduleType == null) {
//                return null;
//            }

//            XmlCursor cursor = xmlObject.newCursor();
//            try {
//                cursor.toFirstChild();
//                if (!MODULE_QNAME.equals(cursor.getName())) {
//                    return null;
//                }
//            } finally {
//                cursor.dispose();
//            }
//            ModuleDocument moduleDoc;
//            if (xmlObject instanceof ModuleDocument) {
//                moduleDoc = (ModuleDocument) xmlObject;
//            } else {
//                moduleDoc = (ModuleDocument) xmlObject.changeType(ModuleDocument.type);
//            }
//            XmlBeansUtil.validateDD(moduleDoc);
            // If there's no artifact ID and we won't be able to figure one out later, use the plan file name.  Bit of a hack.
            if(jarFile == null) {
                if (moduleType.getEnvironment() == null) {
                    moduleType.setEnvironment(new EnvironmentType());
                }
                if (moduleType.getEnvironment().getModuleId() == null) {
                    moduleType.getEnvironment().setModuleId(new ArtifactType());
                }
                if (moduleType.getEnvironment().getModuleId().getArtifactId() == null) {
                    String name = planFile.getName();
                    int pos = name.lastIndexOf('.');
                    if(pos > -1) {
                        name = name.substring(0, pos);
                    }
                    moduleType.getEnvironment().getModuleId().setArtifactId(name);
                }
            }
            return moduleType;
        } catch (Exception e) {
            throw new DeploymentException("Could not parse xml in plan", e);
//        } catch (IOException e) {
//            throw new DeploymentException("no plan at " + planFile, e);
        }
    }

    public Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException {
        ModuleType configType = (ModuleType) plan;
        EnvironmentType environmentType = configType.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType);
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
        ArtifactType type = moduleType.getEnvironment().getModuleId();
        type.setArtifactId(configId.getArtifactId());
        type.setGroupId(configId.getGroupId());
        type.setType(configId.getType());
        type.setVersion(configId.getVersion().toString());
        Environment environment = EnvironmentBuilder.buildEnvironment(moduleType.getEnvironment());
        if(!environment.getConfigId().isResolved()) {
            throw new IllegalStateException("Module ID should be fully resolved by now (not "+environment.getConfigId()+")");
        }
        try {
            targetConfigurationStore.createNewConfigurationDir(configId);
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException(e);
        }

        DeploymentContext context = null;
        try {
//            ConfigurationManager configurationManager = this.configurationManager;
//            if (configurationManager == null) {
//                configurationManager = new SimpleConfigurationManager(configurationStores, artifactResolver, repositories, bundleContext);
//            }

            AbstractName moduleName = naming.createRootName(configId, configId.toString(), SERVICE_MODULE);
            File tempDirectory = FileUtils.createTempDir();
            context = new DeploymentContext(tempDirectory,
                    inPlaceDeployment && null != jar ? JarUtils.toFile(jar) : null,
                    environment,
                    moduleName,
                    ConfigurationModuleType.SERVICE,
                    naming,
                    bundleContext);
            if(jar != null) {
                File file = new File(jar.getName());
                context.addIncludeAsPackedJar(URI.create(file.getName()), jar);
            }
            context.initializeConfiguration();
            serviceBuilders.build(moduleType.getGbean(), context, context);
            return context;
        } catch (DeploymentException de) {
            cleanupContext(context);
            throw de;
        } catch (IOException ie) {
            cleanupContext(context);
            throw ie;
        } catch (RuntimeException re) {
            cleanupContext(context);
            throw re;
        } catch (Error e) {
            cleanupContext(context);
            throw e;
        }
    }

    private void cleanupContext(DeploymentContext context) {
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
            }
        }
    }
}
