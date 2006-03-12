/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.net.URL;
import java.net.URI;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev: 384999 $ $Date$
 * @see EditableConfigurationManager
 */
public class ConfigurationManagerImpl implements ConfigurationManager, GBeanLifecycle {
    protected static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);

    protected final Kernel kernel;
    private final Collection stores;
    protected final ManageableAttributeStore attributeStore;
    protected final PersistentConfigurationList configurationList;
    private final ShutdownHook shutdownHook;
    private final ArtifactManager artifactManager;
    private final ArtifactResolver artifactResolver;
    private final ClassLoader classLoader;
    private final Map configurations = new LinkedHashMap();
    private final Collection repositories;

    public ConfigurationManagerImpl(Kernel kernel,
            Collection stores,
            ManageableAttributeStore attributeStore,
            PersistentConfigurationList configurationList,
            ArtifactManager artifactManager,
            ArtifactResolver artifactResolver,
            Collection repositories,
            ClassLoader classLoader) {

        if (kernel == null) throw new NullPointerException("kernel is null");
        if (stores == null) stores = Collections.EMPTY_SET;
        if (classLoader == null) throw new NullPointerException("classLoader is null");
        if (artifactResolver == null) artifactResolver = new DefaultArtifactResolver(artifactManager, repositories);
        if (repositories == null) repositories = Collections.EMPTY_SET;

        this.kernel = kernel;
        this.stores = stores;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        this.artifactManager = artifactManager;
        this.artifactResolver = artifactResolver;
        this.repositories = repositories;
        this.classLoader = classLoader;
        shutdownHook = new ShutdownHook(kernel);
    }

    public List listStores() {
        List storeSnapshot = getStores();
        List result = new ArrayList(storeSnapshot.size());
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            result.add(JMXUtil.getObjectName(store.getObjectName()));
        }
        return result;
    }

    public List listConfigurations(ObjectName storeName) throws NoSuchStoreException {
        List storeSnapshot = getStores();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (storeName.equals(JMXUtil.getObjectName(store.getObjectName()))) {
                return store.listConfigurations();
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    public boolean isConfiguration(Artifact artifact) {
        List storeSnapshot = getStores();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(artifact)) {
                return true;
            }
        }
        return false;
    }

    public Configuration getConfiguration(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        return configurationStatus.getConfiguration();
    }

    public boolean isLoaded(Artifact configId) {
        return configurations.containsKey(configId);
    }

    public Configuration loadConfiguration(Artifact configurationId) throws NoSuchConfigException, IOException, InvalidConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            // already loaded, so just update the load count
            configurationStatus.load();
            return configurationStatus.getConfiguration();
        } else if (kernel.isLoaded(Configuration.getConfigurationObjectName(configurationId))) {
            // todo hack for bootstrap deploy
            Configuration configuration = (Configuration) kernel.getProxyManager().createProxy(Configuration.getConfigurationObjectName(configurationId), Configuration.class);
            configurationStatus = createConfigurationStatus(configuration);
            configurationStatus.load();
            configurations.put(configurationId, configurationStatus);
            return configurationStatus.getConfiguration();
        }

        // load the GBeanData for the new configuration
        GBeanData gbeanData = loadConfigurationGBeanData(configurationId);

        // load the configuration
        return loadConfiguration(gbeanData);
    }

    public Configuration loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, IOException, InvalidConfigException {
        return loadConfiguration(configurationData, null);
    }

    public Configuration loadConfiguration(ConfigurationData configurationData, ConfigurationStore configurationStore) throws NoSuchConfigException, IOException, InvalidConfigException {
        GBeanData gbeanData = ConfigurationUtil.toConfigurationGBeanData(configurationData, configurationStore, repositories, artifactResolver);
        return loadConfiguration(gbeanData);
    }

    private Configuration loadConfiguration(GBeanData gbeanData) throws NoSuchConfigException, IOException, InvalidConfigException {
        Artifact id = getConfigurationId(gbeanData);
        if (configurations.containsKey(id)) {
            // already loaded, so just update the load count
            ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
            configurationStatus.load();
            return configurationStatus.getConfiguration();
        }

        // load configurations from the new child to the parents
        LinkedHashMap unloadedConfigurations = new LinkedHashMap();
        loadDepthFirst(gbeanData, unloadedConfigurations);

        // load and start the unloaded configurations depth first
        Map loadedConfigurations = new LinkedHashMap(unloadedConfigurations.size());
        try {
            for (Iterator iterator = unloadedConfigurations.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Artifact configurationId = (Artifact) entry.getKey();
                GBeanData configurationData = (GBeanData) entry.getValue();
                AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);

                // load the configuation
                try {
                    kernel.loadGBean(configurationData, classLoader);
                } catch (GBeanAlreadyExistsException e) {
                    throw new InvalidConfigException("Unable to load configuration gbean " + configurationId, e);
                }

                // start the configuration and assure it started
                Configuration configuration;
                try {
                    kernel.startGBean(configurationName);
                    if (State.RUNNING_INDEX != kernel.getGBeanState(configurationName)) {
                        throw new InvalidConfigurationException("Configuration gbean failed to start " + configurationId);
                    }

                    // create a proxy to the configuration
                    configuration = (Configuration) kernel.getProxyManager().createProxy(configurationName, Configuration.class);
                    loadedConfigurations.put(configurationId, configuration);

                    // declare the dependencies as loaded
                    if (artifactManager != null) {
                        artifactManager.loadArtifacts(configurationId, configuration.getDependencies());
                    }

                    log.debug("Loaded Configuration " + configurationName);
                } catch (InvalidConfigurationException e) {
                    throw e;
                } catch (Exception e) {
                    safeConfigurationUnload(configurationId);
                    if (e instanceof InvalidConfigException) {
                        throw (InvalidConfigException) e;
                    }
                    throw new InvalidConfigException("Error starting configuration gbean " + configurationId, e);
                }

//                // todo move this to startConfiguration when deployment code has been update to not search kernel
//                registerGBeans(configuration);
            }
        } catch (Exception e) {
            for (Iterator iterator = loadedConfigurations.keySet().iterator(); iterator.hasNext();) {
                Artifact configurationId = (Artifact) iterator.next();
                safeConfigurationUnload(configurationId);
            }
            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Unable to start configuration gbean " + id, e);
        }

        // update the status of the loaded configurations
        for (Iterator iterator = loadedConfigurations.values().iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            ConfigurationStatus configurationStatus = createConfigurationStatus(configuration);
            configurations.put(getConfigurationId(configuration), configurationStatus);
        }

        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        configurationStatus.load();
        return configurationStatus.getConfiguration();
    }

    private ConfigurationStatus createConfigurationStatus(Configuration configuration) {
        // start parents are just the service parents of the configuration... we want the services to be running so we can use them
        List startParents = getParentStatuses(configuration.getServiceParents());

        // load parents are both the class parents and the service parents
        LinkedHashSet loadParents = new LinkedHashSet(startParents);
        loadParents.addAll(getParentStatuses(configuration.getClassParents()));

        ConfigurationStatus configurationStatus = new ConfigurationStatus(configuration, new ArrayList(loadParents), startParents);
        return configurationStatus;
    }

    private List getParentStatuses(List parents) {
        List parentStatuses = new ArrayList(parents.size());
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration parent = (Configuration) iterator.next();
            Artifact parentId = getConfigurationId(parent);
            ConfigurationStatus parentStatus = (ConfigurationStatus) configurations.get(parentId);
            if (parentStatus == null) {
                throw new IllegalStateException("Parent status not found " + parentId);
            }

            parentStatuses.add(parentStatus);
        }
        return parentStatuses;
    }

    private Artifact getConfigurationId(GBeanData gbeanData) {
        Environment environment = (Environment) gbeanData.getAttribute("environment");
        return environment.getConfigId();
    }

    private void loadDepthFirst(GBeanData gbeanData, LinkedHashMap unloadedConfigurations) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            // if this parent hasn't already been processed, iterate into the parent
            Artifact configurationId = getConfigurationId(gbeanData);
            if (!unloadedConfigurations.containsKey(configurationId)) {
                preprocessConfiguration(gbeanData);

                Environment environment = (Environment) gbeanData.getAttribute("environment");
                for (Iterator iterator = environment.getDependencies().iterator(); iterator.hasNext();) {
                    Dependency dependency = (Dependency) iterator.next();
                    Artifact parentId = dependency.getArtifact();
                    if (!configurations.containsKey(parentId) && isConfiguration(parentId)) {
                        GBeanData parentGBeanData = loadConfigurationGBeanData(parentId);
                        loadDepthFirst(parentGBeanData, unloadedConfigurations);
                    }
                }
            }

            // depth first - all unloaded parents have been added, now add this configuration
            unloadedConfigurations.put(configurationId, gbeanData);
        } catch (NoSuchConfigException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (InvalidConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException(e);
        }
    }

    private GBeanData loadConfigurationGBeanData(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStores();

        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configId)) {
                GBeanData configurationGBean = store.loadConfiguration(configId);
                AbstractName configurationName = Configuration.getConfigurationAbstractName(configId);
                configurationGBean.setAbstractName(configurationName);

                Environment environment = (Environment) configurationGBean.getAttribute("environment");
                ConfigurationResolver configurationResolver = new ConfigurationResolver(environment.getConfigId(), store, repositories, artifactResolver);
                configurationGBean.setAttribute("configurationResolver", configurationResolver);

                return configurationGBean;
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configId);
    }

    private void preprocessConfiguration(GBeanData gbeanData) throws MissingDependencyException, InvalidConfigException {
        Environment environment = (Environment) gbeanData.getAttribute("environment");

        LinkedHashSet parentNames = new LinkedHashSet();
        List dependencies = new ArrayList(environment.getDependencies());
        for (ListIterator iterator = dependencies.listIterator(); iterator.hasNext();) {
            Dependency dependency = (Dependency) iterator.next();
            Artifact resolvedArtifact = artifactResolver.resolve(dependency.getArtifact());
            if (isConfiguration(resolvedArtifact)) {
                AbstractName parentName = Configuration.getConfigurationAbstractName(resolvedArtifact);
                parentNames.add(parentName);

                // update the dependency list to contain the resolved artifact
                dependency = new Dependency(resolvedArtifact, dependency.getImportType());
                iterator.set(dependency);
            } else if (dependency.getImportType() == ImportType.SERVICES) {
                // Service depdendencies require that the depdencency be a configuration
                throw new InvalidConfigException("Dependency does not have services: " + resolvedArtifact);
            }
        }
        environment.setDependencies(dependencies);


        // add parents to the parents reference collection
        gbeanData.addDependencies(parentNames);
        gbeanData.setReferencePatterns("Parents", parentNames);
    }

    private void safeConfigurationUnload(Artifact configurationId) {
        AbstractName configurationName;
        try {
            configurationName = Configuration.getConfigurationAbstractName(configurationId);
        } catch (InvalidConfigException e) {
            throw new AssertionError(e);
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

    private void registerGBeans(Configuration configuration) throws InvalidConfigException {
        // load the attribute overrides from the attribute store
        Map gbeanMap = configuration.getGBeans();
        Collection gbeans = gbeanMap.values();
        if (attributeStore != null) {
            gbeans = attributeStore.applyOverrides(getConfigurationId(configuration), gbeans, configuration.getConfigurationClassLoader());
        }

        // register all the GBeans
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();

            // copy the gbeanData object as not to mutate the original
            gbeanData = new GBeanData(gbeanData);

            // preprocess the gbeanData (resolve references, set base url, declare dependency, etc.)
            preprocessGBeanData(configuration, gbeanData);
            log.trace("Registering GBean " + gbeanData.getName());

            try {
                kernel.loadGBean(gbeanData, configuration.getConfigurationClassLoader());
            } catch (GBeanAlreadyExistsException e) {
                throw new InvalidConfigException(e);
            }
        }
    }

    protected static void preprocessGBeanData(Configuration configuration, GBeanData gbeanData) throws InvalidConfigException {
        for (Iterator references = gbeanData.getReferencesNames().iterator(); references.hasNext();) {
            String referenceName = (String) references.next();
            GReferenceInfo referenceInfo = gbeanData.getGBeanInfo().getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference named " + referenceName + " in gbean " + gbeanData.getAbstractName());
            }
            boolean isSingleValued = !referenceInfo.getProxyType().equals(Collection.class.getName());
            if (isSingleValued) {
                ReferencePatterns referencePatterns = gbeanData.getReferencePatterns(referenceName);
                AbstractName abstractName = null;
                try {
                    abstractName = configuration.findGBean(referencePatterns);
                } catch (GBeanNotFoundException e) {
                    throw new InvalidConfigException("Unable to resolve reference named " + referenceName + " in gbean " + gbeanData.getAbstractName(), e);
                }
                gbeanData.setReferencePatterns(referenceName, new ReferencePatterns(abstractName));
            }
        }

        Set newDependencies = new HashSet();
        for (Iterator dependencyIterator = gbeanData.getDependencies().iterator(); dependencyIterator.hasNext();) {
            ReferencePatterns referencePatterns = (ReferencePatterns) dependencyIterator.next();
            AbstractName abstractName = null;
            try {
                abstractName = configuration.findGBean(referencePatterns);
            } catch (GBeanNotFoundException e) {
                throw new InvalidConfigException("Unable to resolve dependency in gbean " + gbeanData.getAbstractName(), e);
            }
            newDependencies.add(new ReferencePatterns(abstractName));
        }
        gbeanData.setDependencies(newDependencies);

        // If the GBean has a configurationBaseUrl attribute, set it
        // todo remove this when web app cl are config. cl.
        GAttributeInfo attribute = gbeanData.getGBeanInfo().getAttribute("configurationBaseUrl");
        if (attribute != null && attribute.getType().equals("java.net.URL")) {
            try {
                URL baseURL = configuration.getConfigurationResolver().resolve(URI.create(""));
                gbeanData.setAttribute("configurationBaseUrl", baseURL);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to set attribute named " + "configurationBaseUrl" + " in gbean " + gbeanData.getAbstractName(), e);
            }
        }

        // add a dependency from the gbean to the configuration
        gbeanData.addDependency(configuration.getAbstractName());
    }

    public void startConfiguration(Configuration configuration) throws InvalidConfigException {
        startConfiguration(getConfigurationId(configuration));
    }

    public void startConfiguration(Artifact id) throws InvalidConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        if (configurationStatus == null) {
            throw new InvalidConfigurationException("Configuration is not loaded " + id);
        }

        // todo recursion disabled
        List startList = configurationStatus.start();
        for (Iterator iterator = startList.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            start(configuration);
        }
        // todo clean up after failure
    }

    public void start(Configuration configuration) throws InvalidConfigException {
        // todo move this from loadConfiguration when deployment code has been update to not search kernel
         registerGBeans(configuration);

        try {
            // start the gbeans
            Map gbeans = configuration.getGBeans();
            for (Iterator iterator = gbeans.values().iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                if (kernel.isGBeanEnabled(gbeanName)) {
                    kernel.startRecursiveGBean(gbeanName);
                }
            }

            // assure all of the gbeans are started
            for (Iterator iterator = gbeans.keySet().iterator(); iterator.hasNext();) {
                AbstractName gbeanName = (AbstractName) iterator.next();
                if (State.RUNNING_INDEX != kernel.getGBeanState(gbeanName)) {
                    throw new InvalidConfigurationException("Configuration " + getConfigurationId(configuration) + " failed to start because gbean " + gbeanName + " did not start");
                }
            }
        } catch (GBeanNotFoundException e) {
            throw new InvalidConfigException(e);
        }
        // todo clean up after failure

        if (configurationList != null) {
            configurationList.addConfiguration(getConfigurationId(configuration).toString());
        }
    }

    public void stopConfiguration(Configuration configuration) throws InvalidConfigException {
        stopConfiguration(getConfigurationId(configuration));
    }

    public void stopConfiguration(Artifact id) throws InvalidConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        if (configurationStatus == null) {
            throw new InvalidConfigurationException("Configuration is not loaded " + id);
        }

        List stopList = configurationStatus.stop();
        for (Iterator iterator = stopList.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            stop(configuration);
        }
    }

    private void stop(Configuration configuration) throws InvalidConfigException {
        try {
            Collection gbeans = configuration.getGBeans().values();

            // stop the gbeans
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                kernel.stopGBean(gbeanName);
            }

            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                kernel.unloadGBean(gbeanName);
            }
        } catch (Exception e) {
            throw new InvalidConfigException("Could not stop gbeans in configuration", e);
        }
        if (configurationList != null) {
            configurationList.removeConfiguration(getConfigurationId(configuration).toString());
        }
    }

    public void unloadConfiguration(Configuration configuration) throws NoSuchConfigException {
        unloadConfiguration(getConfigurationId(configuration));
    }

    private Artifact getConfigurationId(Configuration configuration) {
        return configuration.getEnvironment().getConfigId();
    }

    public void unloadConfiguration(Artifact id) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        List unloadList = configurationStatus.unload();
        for (Iterator iterator = unloadList.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            Artifact configurationId = getConfigurationId(configuration);
            unload(configurationId);
            configurations.remove(configurationId);
        }
    }

    private void unload(Artifact configurationId) throws NoSuchConfigException {
        AbstractName configName;
        try {
            configName = Configuration.getConfigurationAbstractName(configurationId);
        } catch (InvalidConfigException e) {
            throw new NoSuchConfigException("Could not construct configuration object name", e);
        }
        try {
            if (State.RUNNING_INDEX == kernel.getGBeanState(configName)) {
//                try {
//                    Map gbeans = (Map) kernel.getAttribute(configName, "GBeans");
//
//                    // unload the gbeans
//                    // todo move this to stopConfiguration
//                    for (Iterator iterator = gbeans.values().iterator(); iterator.hasNext();) {
//                        GBeanData gbeanData = (GBeanData) iterator.next();
//                        AbstractName gbeanName = gbeanData.getAbstractName();
//                        kernel.unloadGBean(gbeanName);
//                    }
//                } catch (Exception e) {
//                    throw new InvalidConfigException("Could not stop gbeans in configuration", e);
//                }
                kernel.stopGBean(configName);
            }
            kernel.unloadGBean(configName);

            // declare all artifacts as unloaded
            if (artifactManager != null) {
                artifactManager.unloadAllArtifacts(configurationId);
            }
        } catch (GBeanNotFoundException e) {
            throw new NoSuchConfigException("No config registered: " + configName, e);
        } catch (Exception e) {
            throw new NoSuchConfigException("Problem unloading config: " + configName, e);
        }
    }

    private List getStores() {
        return new ArrayList(stores);
    }

    public void doStart() {
        kernel.registerShutdownHook(shutdownHook);
    }

    private static final ObjectName CONFIG_QUERY = JMXUtil.getObjectName("geronimo.config:*");

    public void doStop() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    public void doFail() {
        log.error("Cofiguration manager failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigurationManagerImpl.class, "ConfigurationManager");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("Stores", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("AttributeStore", ManageableAttributeStore.class, ManageableAttributeStore.ATTRIBUTE_STORE);
        infoFactory.addReference("PersistentConfigurationList", PersistentConfigurationList.class, PersistentConfigurationList.PERSISTENT_CONFIGURATION_LIST);
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addReference("ArtifactResolver", ArtifactResolver.class, "ArtifactResolver");
        infoFactory.addReference("Repositories", Repository.class, "Repository");
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList", "ArtifactManager", "ArtifactResolver", "Repositories", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private static class ShutdownHook implements Runnable {
        private final Kernel kernel;

        public ShutdownHook(Kernel kernel) {
            this.kernel = kernel;
        }

        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(CONFIG_QUERY);
                if (configs.isEmpty()) {
                    return;
                }
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    ObjectName configName = (ObjectName) i.next();
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

    private static class ConfigurationStatus {
        private final Configuration configuration;
        private final List loadParents;
        private final List startParents;
        private int loadCount = 0;
        private int startCount = 0;

        public ConfigurationStatus(Configuration configuration, List loadParents, List startParents) {
            if (!loadParents.containsAll(startParents)) throw new IllegalArgumentException("loadParents must contain all startParents");
            this.configuration = configuration;
            this.loadParents = loadParents;
            this.startParents = startParents;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public int getLoadCount() {
            return loadCount;
        }

        public void load() {
            for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                parent.load();
            }
            loadCount++;
        }

        public List unload() {
            if (loadCount == 1 && startCount > 0) {
                // todo this will most likely need to be removed
                throw new IllegalStateException(configuration.getId() + " is RUNNING: startCount=" + startCount);
            }

            LinkedList unloadList = new LinkedList();
            for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                unloadList.addAll(parent.unload());
            }
            loadCount--;
            if (loadCount == 0) {
                assert(startCount == 0);
                unloadList.addFirst(configuration);
            }
            return unloadList;
        }

        public int getStartCount() {
            return startCount;
        }

        public List start() {
            List startList = new LinkedList();
            for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                startList.addAll(parent.start());
            }
            startCount++;
            if (startCount == 1) {
                startList.add(configuration);
            }
            return startList;
        }

        public List stop() {
            LinkedList stopList = new LinkedList();
            for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                stopList.addAll(parent.stop());
            }
            startCount--;
            if (startCount == 0) {
                stopList.addFirst(configuration);
            }
            return stopList;
        }
    }
}
