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

package org.apache.geronimo.kernel.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev:386276 $ $Date$
 * @see EditableConfigurationManager
 */
public class KernelConfigurationManager extends SimpleConfigurationManager implements GBeanLifecycle {

    protected final Kernel kernel;
    protected final ManageableAttributeStore attributeStore;
    protected final PersistentConfigurationList configurationList;
    private final ArtifactManager artifactManager;
    protected final ClassLoader classLoader;
    private final ShutdownHook shutdownHook;
    private boolean online = true;

    public KernelConfigurationManager(Kernel kernel,
            Collection stores,
            ManageableAttributeStore attributeStore,
            PersistentConfigurationList configurationList,
            ArtifactManager artifactManager,
            ArtifactResolver artifactResolver,
            Collection repositories,
            Collection watchers,
            ClassLoader classLoader) {

        super(stores,
                createArtifactResolver(artifactResolver, artifactManager, repositories),
                repositories, watchers);

        this.kernel = kernel;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        this.artifactManager = artifactManager;
        this.classLoader = classLoader;

        shutdownHook = new ShutdownHook(kernel);
    }

    private static ArtifactResolver createArtifactResolver(ArtifactResolver artifactResolver, ArtifactManager artifactManager, Collection repositories) {
        if (artifactResolver != null) {
            return artifactResolver;
        }
        return new DefaultArtifactResolver(artifactManager, repositories, null);
    }

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        // todo hack for bootstrap deploy
        AbstractName abstractName = null;
        try {
            abstractName = Configuration.getConfigurationAbstractName(configurationId);
        } catch (InvalidConfigException e) {
            throw new RuntimeException(e);
        }
        if (getConfiguration(configurationId) == null && kernel.isLoaded(abstractName)) {
            try {
                Configuration configuration = (Configuration) kernel.getGBean(abstractName);
                addNewConfigurationToModel(configuration);
                configurationModel.load(configurationId);
                configurationModel.start(configurationId);
                return new LifecycleResults();
            } catch (GBeanNotFoundException e) {
                // configuration was unloaded, just continue as normal
            }
        }

        return super.loadConfiguration(configurationId);
    }

    protected void load(Artifact configurationId) throws NoSuchConfigException {
        super.load(configurationId);
        if (configurationList != null) {
            configurationList.addConfiguration(configurationId);
        }
    }

    protected void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration, boolean running) throws NoSuchConfigException {
        super.migrateConfiguration(oldName, newName, configuration, running);
        if (configurationList != null) {
            configurationList.migrateConfiguration(oldName, newName, configuration);
            if(running) {
                configurationList.startConfiguration(newName);
            }
        }
    }

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet resolvedParentIds, Map loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = configurationData.getId();
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);
        GBeanData gbeanData = new GBeanData(configurationName, Configuration.GBEAN_INFO);
        gbeanData.setAttribute("configurationData", configurationData);
        gbeanData.setAttribute("configurationResolver", new ConfigurationResolver(configurationData, repositories, getArtifactResolver()));
        //TODO is this dangerous?
        gbeanData.setAttribute("managedAttributeStore", attributeStore);

        // add parents to the parents reference collection
        LinkedHashSet parentNames = new LinkedHashSet();
        for (Iterator iterator = resolvedParentIds.iterator(); iterator.hasNext();) {
            Artifact resolvedParentId = (Artifact) iterator.next();
            AbstractName parentName = Configuration.getConfigurationAbstractName(resolvedParentId);
            parentNames.add(parentName);
        }
        gbeanData.addDependencies(parentNames);
        gbeanData.setReferencePatterns("Parents", parentNames);

        // load the configuration
        try {
            kernel.loadGBean(gbeanData, classLoader);
        } catch (GBeanAlreadyExistsException e) {
            throw new InvalidConfigException("Unable to load configuration gbean " + configurationId, e);
        }

        // start the configuration and assure it started
        Configuration configuration;
        try {
            kernel.startGBean(configurationName);
            if (State.RUNNING_INDEX != kernel.getGBeanState(configurationName)) {
                String stateReason = kernel.getStateReason(configurationName);
                throw new InvalidConfigurationException("Configuration gbean failed to start " + configurationId + "\nreason: " + stateReason);
            }

            // get the configuration
            configuration = (Configuration) kernel.getGBean(configurationName);

            // declare the dependencies as loaded
            if (artifactManager != null) {
                artifactManager.loadArtifacts(configurationId, configuration.getDependencies());
            }

            log.debug("Loaded Configuration {}", configurationName);
        } catch (Exception e) {
            unload(configurationId);
            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Error starting configuration gbean " + configurationId, e);
        }
        return configuration;
    }

    public void start(Configuration configuration) throws InvalidConfigException {
        if (online) {
            ConfigurationUtil.startConfigurationGBeans(configuration.getAbstractName(), configuration, kernel);
        }

        if (configurationList != null && configuration.getConfigurationData().isAutoStart()) {
            configurationList.startConfiguration(configuration.getId());
        }
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    protected void stop(Configuration configuration) {
        stopRecursive(configuration);
        if (configurationList != null) {
            configurationList.stopConfiguration(configuration.getId());
        }
    }

    private void stopRecursive(Configuration configuration) {
        // stop all of the child configurations first
        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = (Configuration) iterator.next();
            stopRecursive(childConfiguration);
        }

        Collection gbeans = configuration.getGBeans().values();

        // stop the gbeans
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            AbstractName gbeanName = gbeanData.getAbstractName();
            try {
                kernel.stopGBean(gbeanName);
            } catch (GBeanNotFoundException ignored) {
            } catch (IllegalStateException ignored) {
            } catch (InternalKernelException kernelException) {
                log.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
            }
        }

        // unload the gbeans
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            AbstractName gbeanName = gbeanData.getAbstractName();
            try {
                kernel.unloadGBean(gbeanName);
            } catch (GBeanNotFoundException ignored) {
            } catch (IllegalStateException ignored) {
            } catch (InternalKernelException kernelException) {
                log.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
            }
        }
    }

    protected void unload(Configuration configuration) {
        Artifact configurationId = configuration.getId();
        unload(configurationId);
    }

    private void unload(Artifact configurationId) {
        AbstractName configurationName;
        try {
            configurationName = Configuration.getConfigurationAbstractName(configurationId);
        } catch (InvalidConfigException e) {
            throw new AssertionError(e);
        }

        if (artifactManager != null) {
            artifactManager.unloadAllArtifacts(configurationId);
        }

        // unload this configuration
        try {
            kernel.stopGBean(configurationName);
        } catch (GBeanNotFoundException ignored) {
            // Good
        } catch (Exception stopException) {
            log.warn("Unable to stop failed configuration: " + configurationId, stopException);
        }

        try {
            kernel.unloadGBean(configurationName);
        } catch (GBeanNotFoundException ignored) {
            // Good
        } catch (Exception unloadException) {
            log.warn("Unable to unload failed configuration: " + configurationId, unloadException);
        }
    }

    protected void uninstall(Artifact configurationId) {
        if (configurationList != null) {
            configurationList.removeConfiguration( configurationId );
        }
    }
    
    public void doStart() {
        kernel.registerShutdownHook(shutdownHook);
    }

    public void doStop() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    public void doFail() {
        log.error("Cofiguration manager failed");
    }

    private static class ShutdownHook implements Runnable {
        private final Kernel kernel;
        
        private final Logger log = LoggerFactory.getLogger(ShutdownHook.class);
        
        public ShutdownHook(Kernel kernel) {
            this.kernel = kernel;
        }

        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(new AbstractNameQuery(Configuration.class.getName()));
                if (configs.isEmpty()) {
                    return;
                }
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    AbstractName configName = (AbstractName) i.next();
                    if (kernel.isLoaded(configName)) {
                        try {
                            kernel.stopGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        } catch (InternalKernelException e) {
                            log.warn("Could not stop configuration: " + configName, e);
                        }
                        try {
                            kernel.unloadGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(KernelConfigurationManager.class, SimpleConfigurationManager.GBEAN_INFO, "ConfigurationManager");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("AttributeStore", ManageableAttributeStore.class, ManageableAttributeStore.ATTRIBUTE_STORE);
        infoFactory.addReference("PersistentConfigurationList", PersistentConfigurationList.class, PersistentConfigurationList.PERSISTENT_CONFIGURATION_LIST);
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList", "ArtifactManager", "ArtifactResolver", "Repositories", "Watchers", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
