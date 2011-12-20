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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.util.CircularReferencesException;
import org.apache.geronimo.kernel.util.IllegalNodeConfigException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev:386276 $ $Date$
 */

@Component(inherit = true, immediate = true, metatype = true)
@Service
public class KernelConfigurationManager extends SimpleConfigurationManager implements GBeanLifecycle {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected Kernel kernel;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ManageableAttributeStore attributeStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PersistentConfigurationList configurationList;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ArtifactManager artifactManager;

    private ShutdownHook shutdownHook;

    private boolean online = true;

    public KernelConfigurationManager() {
    }

    public KernelConfigurationManager(Kernel kernel,
                                      @ParamReference(name = "Stores", namingType = "ConfigurationStore") Collection<ConfigurationStore> stores,
                                      @ParamReference(name = "AttributeStore", namingType = "AttributeStore") ManageableAttributeStore attributeStore,
                                      @ParamReference(name = "PersistentConfigurationList") PersistentConfigurationList configurationList,
                                      @ParamReference(name = "ArtifactManager", namingType = "ArtifactManager") ArtifactManager artifactManager,
                                      @ParamReference(name = "ArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver artifactResolver,
                                      @ParamReference(name = "Repositories", namingType = "Repository") Collection<ListableRepository> repositories,
                                      @ParamReference(name = "Watchers") Collection<DeploymentWatcher> watchers,
                                      @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {

        super(stores,
                createArtifactResolver(artifactResolver, artifactManager, repositories),
                repositories, watchers, bundleContext);

        this.kernel = kernel;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        this.artifactManager = artifactManager;

        shutdownHook = new ShutdownHook(kernel, configurationModel);
    }

    public void setArtifactManager(ArtifactManager artifactManager) {
        this.artifactManager = artifactManager;
    }

    public void unsetArtifactManager(ArtifactManager artifactManager) {
        this.artifactManager = null;
    }

    public void setAttributeStore(ManageableAttributeStore attributeStore) {
        this.attributeStore = attributeStore;
    }

    public void unsetAttributeStore(ManageableAttributeStore attributeStore) {
        this.attributeStore = null;
    }

    public void setConfigurationList(PersistentConfigurationList configurationList) {
        this.configurationList = configurationList;
    }

    public void unsetConfigurationList(PersistentConfigurationList configurationList) {
        this.configurationList = null;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }
    public void unsetKernel(Kernel kernel) {
        this.kernel = null;
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        super.activate(bundleContext);
        shutdownHook = new ShutdownHook(kernel, getConfigurationModel());
        kernel.registerShutdownHook(shutdownHook);
    }

    @Deactivate
    public void deactivate() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    private static ArtifactResolver createArtifactResolver(ArtifactResolver artifactResolver, ArtifactManager artifactManager, Collection<ListableRepository> repositories) {
        if (artifactResolver != null) {
            return artifactResolver;
        }
        //TODO no reference to this may cause problems
        return new DefaultArtifactResolver(artifactManager, repositories, null, Collections.<ConfigurationManager>emptyList());
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
            if (running) {
                configurationList.startConfiguration(newName);
            }
        }
    }

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = configurationData.getId();
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);
        GBeanData gbeanData = new GBeanData(configurationName, Configuration.class);
        gbeanData.setAttribute("configurationData", configurationData);
        DependencyNode dependencyNode = null;
        ConfigurationResolver configurationResolver = new ConfigurationResolver(configurationData, repositories, getArtifactResolver());
        gbeanData.setAttribute("configurationResolver", configurationResolver);
        try {
            dependencyNode = buildDependencyNode(configurationData);

            gbeanData.setAttribute("dependencyNode", dependencyNode);
//            gbeanData.setAttribute("classLoaderHolder", classLoaderHolder);
            gbeanData.setAttribute("allServiceParents", buildAllServiceParents(loadedConfigurations, dependencyNode));
        } catch (MissingDependencyException e) {
            throw new InvalidConfigException(e);
//        } catch (MalformedURLException e) {
//            throw new InvalidConfigException(e);
//        } catch (NoSuchConfigException e) {
//            throw new InvalidConfigException(e);
        }
        gbeanData.setAttribute("configurationManager", this);
        //TODO is this dangerous?  should really add dependency on attribute store name
        gbeanData.setAttribute("attributeStore", attributeStore);

        // add parents to the parents reference collection
        LinkedHashSet<AbstractName> parentNames = new LinkedHashSet<AbstractName>();
        for (Artifact resolvedParentId : resolvedParentIds) {
            if (isConfiguration(resolvedParentId)) {
                AbstractName parentName = Configuration.getConfigurationAbstractName(resolvedParentId);
                parentNames.add(parentName);
            }
        }
        gbeanData.addDependencies(parentNames);

        // load the configuration
        try {
            //TODO OSGI more likely use the configuration bundle??
            kernel.loadGBean(gbeanData, bundleContext);
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
                artifactManager.loadArtifacts(configurationId, configuration.getDependencyNode().getParents());
            }
            Map<Artifact, Configuration> moreLoadedConfigurations = new LinkedHashMap<Artifact, Configuration>(loadedConfigurations);
            moreLoadedConfigurations.put(dependencyNode.getId(), configuration);
            for (Map.Entry<String, ConfigurationData> childEntry : configurationData.getChildConfigurations().entrySet()) {
                ConfigurationResolver childResolver = configurationResolver.createChildResolver(childEntry.getKey());
                Configuration child = doLoad(childEntry.getValue(), resolvedParentIds, moreLoadedConfigurations, childResolver);
                configuration.addChild(child);
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
        Collection<GBeanData> gbeans;
        try {
            List<GBeanData> sortedGBeans = ConfigurationUtil.sortGBeanDataByDependency(configuration.getGBeans().values());
            Collections.reverse(sortedGBeans);
            gbeans = sortedGBeans;
        } catch (IllegalNodeConfigException e) {
            gbeans = configuration.getGBeans().values();
        } catch (CircularReferencesException e) {
            gbeans = configuration.getGBeans().values();
        }
        // stop the gbeans
        for (Iterator<GBeanData> iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = iterator.next();
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
        for (Iterator<GBeanData> iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = iterator.next();
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
            configurationList.removeConfiguration(configurationId);
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
        private final ConfigurationModel configurationModel;
        private final Logger log = LoggerFactory.getLogger(ShutdownHook.class);

        public ShutdownHook(Kernel kernel, ConfigurationModel configurationModel) {
            this.kernel = kernel;
            this.configurationModel = configurationModel;
        }

        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(new AbstractNameQuery(Configuration.class.getName()));
                if (configs.isEmpty()) {
                    return;
                }
                LinkedHashSet orderedConfigs = new LinkedHashSet();
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    AbstractName configName = (AbstractName) i.next();
                    if (kernel.isLoaded(configName) && !orderedConfigs.contains(configName)) {
                        LinkedHashSet startedChildren = configurationModel.getStartedChildren(configName.getArtifact());
                        for (Iterator iterator = startedChildren.iterator(); iterator.hasNext();) {
                            Artifact configurationId = (Artifact) iterator.next();
                            Set childConfig = kernel.listGBeans(new AbstractNameQuery(configurationId, Collections.emptyMap(), Configuration.class.getName()));
                            if (!childConfig.isEmpty()) {
                                AbstractName childConfigName = (AbstractName) childConfig.iterator().next();
                                if (!orderedConfigs.contains(childConfigName))
                                    orderedConfigs.add(childConfigName);
                            }
                        }
                        orderedConfigs.add(configName);
                    }
                }

                for (Iterator i = orderedConfigs.iterator(); i.hasNext();) {
                    AbstractName configName = (AbstractName) i.next();
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
