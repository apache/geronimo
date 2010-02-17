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
package org.apache.geronimo.aries.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.ApplicationMetadataFactory;
import org.apache.aries.application.Content;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.util.JarUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean(j2eeType = "ConfigBuilder")
public class AriesAppConfigBuilder implements ConfigurationBuilder, GBeanLifecycle {
    
    private static final Logger LOG = LoggerFactory.getLogger(AriesAppConfigBuilder.class);
    
    private Kernel kernel;
    private BundleContext bundleContext;
    private WritableListableRepository repository;
    private ConfigurationManager configurationManager;

    public AriesAppConfigBuilder(@ParamReference(name="Repository", namingType = "Repository")WritableListableRepository repository,
                                 @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                 @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) 
        throws GBeanNotFoundException {
        this.repository = repository;
        this.kernel = kernel;
        this.bundleContext = bundleContext;
        this.configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
    }
    
    public void doStart() throws Exception {
    }

    public void doStop() {
    }

    public void doFail() {
        doStop();
    }

    private WritableListableRepository getRepository() {
        return repository;   
    }
    
    private ApplicationMetadataFactory getApplicationMetadataManager() {
        ServiceReference ref = 
            bundleContext.getServiceReference(ApplicationMetadataFactory.class.getName());
        if (ref != null) {
            return (ApplicationMetadataFactory) bundleContext.getService(ref);
        } else {
            return null;
        }
    }
    
    private String getSymbolicName(Manifest mf) {
        String name = null;
        if (mf != null) {
            name = (String) mf.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
        }
        return name;
    }
    
    private String getBundleVersion(Manifest mf) {
        String version = null;
        if (mf != null) {
            version = (String) mf.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
        }
        return version;
    }
    
    private static class AppEntry {
        String entryName;
        Manifest manifest;
        
        public AppEntry(String entryName, Manifest manifest) {
            this.entryName = entryName;
            this.manifest = manifest;
        }
    }
    
    public void install(JarFile jarFile, ApplicationMetadata appMetadata, Environment environment) throws DeploymentException {       
        /*
         * XXX: This is totally not right but for now allows us to install 
         * simple Aries applications into Geronimo. 
         */
        HashMap<String, AppEntry> mapping = new HashMap<String, AppEntry>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".jar")) {
                try {
                    InputStream in = jarFile.getInputStream(entry);
                    JarInputStream jarInput = new JarInputStream(in);
                    String name = getSymbolicName(jarInput.getManifest());
                    if (name != null) {
                        mapping.put(name, new AppEntry(entry.getName(), jarInput.getManifest()));
                    }
                } catch (IOException e) {
                    LOG.warn("Error getting jar entry {}", entry.getName(), e);
                }
            }
        }

        try {
            for (Content content : appMetadata.getApplicationContents()) {
                AppEntry appEntry = mapping.get(content.getContentName());
                if (appEntry == null) {
                    LOG.warn("Unknown bundle name in application context {}", content.getContentName());
                    continue;
                }
                ZipEntry entry = jarFile.getEntry(appEntry.entryName);
                if (entry == null) {
                    // this should not happen
                    throw new DeploymentException("Jar entry not found " + appEntry.entryName);
                }

                Artifact artifact = new Artifact("aries-app", 
                                                 content.getContentName(), 
                                                 getBundleVersion(appEntry.manifest),
                                                 "jar");
                InputStream in = jarFile.getInputStream(entry);
                getRepository().copyToRepository(in, (int) entry.getSize(), artifact, null);
                
                environment.addDependency(new Dependency(artifact, ImportType.ALL));
            }
            
        } catch (Exception e) {
            throw new DeploymentException("Failed to install application", e);
        }        
    }


    public Object getDeploymentPlan(File planFile, 
                                    JarFile jarFile, 
                                    ModuleIDBuilder idBuilder) 
        throws DeploymentException {
        if (planFile == null && jarFile == null) {
            return null;
        }
        
        JarEntry appManifest = jarFile.getJarEntry("META-INF/APPLICATION.MF");
        
        if (appManifest == null) {
            return null;
        }
        
        ApplicationMetadataFactory service = getApplicationMetadataManager();
        if (service == null) {
            return null;
        }
        
        ApplicationMetadata appMetadata = null;
        try {
            InputStream in = jarFile.getInputStream(appManifest);
            appMetadata = service.parseApplicationMetadata(in);
        } catch (IOException e) {
            throw new DeploymentException("Failed to parse application metadata", e);
        }
        
        return appMetadata;
    }
    
    public Artifact getConfigurationID(Object plan, 
                                       JarFile jarFile, 
                                       ModuleIDBuilder idBuilder)
        throws IOException, DeploymentException {
        ApplicationMetadata appMetadata = (ApplicationMetadata) plan;
        
        Artifact name = new Artifact("aries-app", appMetadata.getApplicationSymbolicName(), appMetadata.getApplicationVersion().toString(), "jar");
        
        return name;
    }
    
    public DeploymentContext buildConfiguration(boolean inPlaceDeployment, 
                                                Artifact configId, 
                                                Object plan,
                                                JarFile jarFile, 
                                                Collection<ConfigurationStore> configurationStores, 
                                                ArtifactResolver artifactResolver, 
                                                ConfigurationStore targetConfigurationStore) 
        throws IOException, DeploymentException {
        ApplicationMetadata appMetadata = (ApplicationMetadata) plan;
        
        Environment environment = new Environment();
        environment.setConfigId(configId);
        
        install(jarFile, appMetadata, environment);
        
        File outfile;
        try {
            outfile = targetConfigurationStore.createNewConfigurationDir(configId);
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException(e);
        }
        
        Naming naming = kernel.getNaming();
        AbstractName moduleName = naming.createRootName(configId, configId.toString(), "AriesApplication");
        try {
            DeploymentContext context = new DeploymentContext(outfile,                
                            inPlaceDeployment && null != jarFile ? JarUtils.toFile(jarFile) : null,
                            environment,
                            moduleName,
                            ConfigurationModuleType.SERVICE,
                            naming,
                            configurationManager,
                            null, 
                            bundleContext);
            
            
            context.flush();
            context.initializeConfiguration();
            
            return context;
        } catch (DeploymentException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try { jarFile.close(); } catch (IOException ignore) {}
        }
    }

}
