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
import java.util.Collection;

import org.apache.aries.application.management.AriesApplication;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean
public class ApplicationInstaller implements GBeanLifecycle {
    
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInstaller.class);
    
    private Kernel kernel;
    private BundleContext bundleContext;   
    private AbstractName abstractName;
    private ServiceRegistration registration;
    private ConfigurationManager configurationManager;
    private Collection<ConfigurationStore> configurationStores;

    public ApplicationInstaller(@ParamReference(name="Store", namingType = "ConfigurationStore") Collection<ConfigurationStore> configurationStores,
                                @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                                @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName)  
        throws GBeanNotFoundException {
        this.kernel = kernel;
        this.bundleContext = bundleContext;
        this.abstractName = abstractName;
        this.configurationStores = configurationStores;
        this.configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
    }
    
    public void doStart() throws Exception {
        registration = bundleContext.registerService(ApplicationInstaller.class.getName(), this, null);
    }

    public void doStop() {
        if (registration != null) {
            registration.unregister();
        }
    }

    public void doFail() {
        doStop();
    }
                
    protected ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
    
    public DeploymentContext startInstall(AriesApplication app, ConfigurationStore targetConfigurationStore) 
        throws ConfigurationAlreadyExistsException, DeploymentException {
                                
        Artifact configId = getConfigId(app);
        
        File configDir = targetConfigurationStore.createNewConfigurationDir(configId);
        
        Environment environment = new Environment();
        environment.setConfigId(configId);
        
        Naming naming = kernel.getNaming();
        AbstractName moduleName = naming.createRootName(configId, configId.toString(), "AriesApplication");
        try {
            DeploymentContext context = new DeploymentContext(configDir,                
                            null,
                            environment,
                            moduleName,
                            ConfigurationModuleType.SERVICE,
                            naming,
                            configurationManager,
                            null, 
                            bundleContext);
                                    
            context.flush();
            context.initializeConfiguration();
                        
            app.store(configDir);
                        
            AbstractName name = naming.createChildName(moduleName, "AriesApplication", "GBean");
            GBeanData data = new GBeanData(name, ApplicationGBean.class);
            data.setAttribute("configId", configId);
            data.setReferencePattern("Installer", abstractName);
            
            context.addGBean(data);
            
            return context;
        } catch (DeploymentException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("", e);
        }
    }

    public ConfigurationData finishInstall(DeploymentContext context, ConfigurationStore targetConfigurationStore)  
        throws ConfigurationAlreadyExistsException, DeploymentException {
        try {
            ConfigurationData configurationData = context.getConfigurationData();
            targetConfigurationStore.install(configurationData);
            return configurationData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("", e);
        } finally {
            try { context.close(); } catch (IOException e) {}
        }
    }
    
    public void install(AriesApplication app) throws ConfigurationAlreadyExistsException, DeploymentException {
        ConfigurationStore store = configurationStores.iterator().next();
        if (store == null) {
            throw new DeploymentException("No ConfigurationStore");
        }
        DeploymentContext context = startInstall(app, store);
        ConfigurationData configurationData = finishInstall(context, store);
        
        try {
            configurationManager.loadConfiguration(configurationData.getId());
            configurationManager.startConfiguration(configurationData.getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException("", e);
        }
    }
    
    public void uninstall(AriesApplication app) {
        Artifact configId = getConfigId(app);
        try {            
            Repository repository = findRepository(configId);
            File location = repository.getLocation(configId);
            
            configurationManager.unloadConfiguration(configId);
            configurationManager.uninstallConfiguration(configId);
            
            FileUtils.recursiveDelete(location.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Repository findRepository(Artifact configId) {
        for (Repository repository : configurationManager.getRepositories()) {
            if (repository.contains(configId)) {
                return repository;
            }
        }
        return null;
    }
    
    private static Artifact getConfigId(AriesApplication app) {
        return createArtifact("aries-app", 
                              app.getApplicationMetadata().getApplicationSymbolicName(), 
                              app.getApplicationMetadata().getApplicationVersion());
    }
    
    private static Artifact createArtifact(String group, String symbolicName, Version version) {
        return new Artifact(group, symbolicName, getVersion(version), "jar");
    }
    
    private static String getVersion(Version version) {
        String str = version.getMajor() + "." + version.getMinor() + "." + version.getMinor();
        if (version.getQualifier() != null) {
            str += "-" + version.getQualifier();
        }
        return str;
    }
    
}
