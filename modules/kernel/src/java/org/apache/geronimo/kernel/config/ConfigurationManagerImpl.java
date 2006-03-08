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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.InvalidConfigurationException;
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
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev$ $Date$
 * @see EditableConfigurationManager
 */
public class ConfigurationManagerImpl implements ConfigurationManager, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);

    protected final Kernel kernel;
    private final Collection stores;
    protected final ManageableAttributeStore attributeStore;
    protected final PersistentConfigurationList configurationList;
    private final ShutdownHook shutdownHook;
    private final ArtifactManager artifactManager;
    private final ArtifactResolver artifactResolver;
    private final ClassLoader classLoader;
    private final Map configurations = new LinkedHashMap();

    public ConfigurationManagerImpl(Kernel kernel,
                                    Collection stores,
                                    ManageableAttributeStore attributeStore,
                                    PersistentConfigurationList configurationList,
                                    ArtifactManager artifactManager,
                                    ArtifactResolver artifactResolver,
                                    ClassLoader classLoader) {

        if (kernel == null) throw new NullPointerException("kernel is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        this.kernel = kernel;
        this.stores = stores;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        if (artifactResolver == null)
            artifactResolver = new DefaultArtifactResolver(artifactManager, Collections.EMPTY_SET);
        this.artifactManager = artifactManager;
        this.artifactResolver = artifactResolver;
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
            // hack
            Configuration configuration = (Configuration) kernel.getProxyManager().createProxy(Configuration.getConfigurationObjectName(configurationId), Configuration.class);
            configurationStatus = new ConfigurationStatus(configuration, getParentStatuses(configuration));
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
        try {
            GBeanData gbeanData = ConfigurationUtil.toConfigurationGBeanData(configurationData, configurationStore);

            return loadConfiguration(gbeanData);
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException(e);
        }
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
                        throw new InvalidConfigurationException("Configuration " + configurationId + " failed to start");
                    }

                    // create a proxy to the configuration
                    configuration = (Configuration) kernel.getProxyManager().createProxy(configurationName, Configuration.class);
                    loadedConfigurations.put(configurationId, configuration);
                    log.debug("Loaded Configuration " + configurationName);
                } catch (Exception e) {
                    safeConfigurationUnload(configurationId);
                    if (e instanceof InvalidConfigException) {
                        throw (InvalidConfigException) e;
                    }
                    throw new InvalidConfigException("Unable to start configuration gbean " + configurationId, e);
                }

                // todo move this to startConfiguration when deployment code has been update to not search kernel
                registerGBeans(configuration);
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

            List parentStatuses = getParentStatuses(configuration);
            ConfigurationStatus configurationStatus = new ConfigurationStatus(configuration, parentStatuses);
            configurations.put(getConfigurationId(configuration), configurationStatus);
        }

        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        configurationStatus.load();
        return configurationStatus.getConfiguration();
    }

    private List getParentStatuses(Configuration configuration) {
        List parents = configuration.getParents();
        List parentStatuses = new ArrayList(parents.size());
        for (Iterator iterator1 = parents.iterator(); iterator1.hasNext();) {
            Configuration parent = (Configuration) iterator1.next();
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
                preprocess(gbeanData);

                Environment environment = (Environment) gbeanData.getAttribute("environment");
                for (Iterator iterator = environment.getImports().iterator(); iterator.hasNext();) {
                    Artifact parentId = (Artifact) iterator.next();
                    if (!configurations.containsKey(parentId)) {
                        GBeanData parentGBeanData = loadConfigurationGBeanData(parentId);
                        loadDepthFirst(parentGBeanData, unloadedConfigurations);
                    }
                }
            }

            // depth first - all unloaded parents have been added, not add this configuration
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
                configurationGBean.setAttribute("configurationStore", store);

                return configurationGBean;
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configId);
    }

    private void preprocess(GBeanData gbeanData) throws MissingDependencyException, InvalidConfigException {
        if (artifactManager != null) {
            gbeanData.setAttribute("artifactManager", artifactManager);
        }
        gbeanData.setAttribute("artifactResolver", artifactResolver);

        Environment environment = (Environment) gbeanData.getAttribute("environment");

        // resolve the parents
        LinkedHashSet imports = environment.getImports();
        imports = artifactResolver.resolve(imports);
        environment.setImports(imports);

        // resolve the references
        LinkedHashSet references = environment.getReferences();
        references = artifactResolver.resolve(references);
        environment.setReferences(references);

        // convert the parents and reference artifactIds to objectNames
        LinkedHashSet importNames = new LinkedHashSet();
        for (Iterator iterator = imports.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            AbstractName importName = Configuration.getConfigurationAbstractName(artifact);
            gbeanData.getDependencies().add(importName);
            importNames.add(new AbstractNameQuery(importName));
        }
        for (Iterator iterator = references.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            AbstractName referenceName = Configuration.getConfigurationAbstractName(artifact);
            gbeanData.getDependencies().add(referenceName);
        }

        // imports become the parents
        gbeanData.setReferencePatterns("Parents", importNames);
    }

    private void safeConfigurationUnload(Artifact configurationId) {
        ObjectName configurationName;
        try {
            configurationName = Configuration.getConfigurationObjectName(configurationId);
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

    private void registerGBeans(Configuration configuration) throws InvalidConfigException, NoSuchConfigException, MalformedURLException {
        // load the attribute overrides from the attribute store
        Map gbeanMap = configuration.getGBeans();
        Collection gbeans = gbeanMap.values();
        if (attributeStore != null) {
            gbeans = attributeStore.applyOverrides(getConfigurationId(configuration), gbeans, configuration.getConfigurationClassLoader());
        }

        // register all the GBeans
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configuration.getId());
        ConfigurationStore configurationStore = configuration.getConfigurationStore();
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();

            // copy the gbeanData object as not to mutate the original
            gbeanData = new GBeanData(gbeanData);

            // If the GBean has a configurationBaseUrl attribute, set it
            // todo remove this when web app cl are config. cl.
            GAttributeInfo attribute = gbeanData.getGBeanInfo().getAttribute("configurationBaseUrl");
            if (attribute != null && attribute.getType().equals("java.net.URL")) {
                URL baseURL = configurationStore.resolve(getConfigurationId(configuration), URI.create(""));
                gbeanData.setAttribute("configurationBaseUrl", baseURL);
            }

            // add a dependency from the gbean to the configuration
            gbeanData.getDependencies().add(configurationName);

            log.trace("Registering GBean " + gbeanData.getName());

            try {
                kernel.loadGBean(gbeanData, configuration.getConfigurationClassLoader());
            } catch (GBeanAlreadyExistsException e) {
                throw new InvalidConfigException(e);
            }
        }
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
        start(configurationStatus.getConfiguration());
//        for (Iterator iterator = startList.iterator(); iterator.hasNext();) {
//            Configuration configuration = (Configuration) iterator.next();
//            start(configuration);
//        }
//        // todo clean up after failure
    }

    public void start(Configuration configuration) throws InvalidConfigException {
        // todo move this from loadConfiguration when deployment code has been update to not search kernel
        // registerGBeans(configuration);

        try {
            // start the gbeans
            Collection gbeans = configuration.getGBeans().values();
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                GBeanData gbeanData = (GBeanData) iterator.next();
                AbstractName gbeanName = gbeanData.getAbstractName();
                if (kernel.isGBeanEnabled(gbeanName)) {
                    kernel.startRecursiveGBean(gbeanName);
                }
            }

            // assure all of the gbeans are started
//            for (Iterator iterator = gbeans.values().iterator(); iterator.hasNext();) {
//                GBeanData gbeanData = (GBeanData) iterator.next();
//                AbstractName gbeanName = gbeanData.getAbstractName();
//                if (State.RUNNING_INDEX != kernel.getGBeanState(gbeanName)) {
//                    throw new InvalidConfigurationException("Configuration " + configuration.getId() + " failed to start because gbean " + gbeanName + " did not start");
//                }
//            }
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
                try {
                    Map gbeans = (Map) kernel.getAttribute(configName, "GBeans");

                    // unload the gbeans
                    // todo move this to stopConfiguration
                    for (Iterator iterator = gbeans.values().iterator(); iterator.hasNext();) {
                        GBeanData gbeanData = (GBeanData) iterator.next();
                        AbstractName gbeanName = gbeanData.getAbstractName();
                        kernel.unloadGBean(gbeanName);
                    }
                } catch (Exception e) {
                    throw new InvalidConfigException("Could not stop gbeans in configuration", e);
                }
                kernel.stopGBean(configName);
            }
            kernel.unloadGBean(configName);
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
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList", "ArtifactManager", "ArtifactResolver", "classLoader"});
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
        private final List parents;
        private int loadCount = 0;
        private int startCount = 0;

        public ConfigurationStatus(Configuration configuration, List parents) {
            this.configuration = configuration;
            this.parents = parents;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public int getLoadCount() {
            return loadCount;
        }

        public void load() {
            for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                parent.load();
            }
            loadCount++;
        }

        public List unload() {
            List unloadList = new LinkedList();
            for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                unloadList.addAll(parent.unload());
            }
            loadCount--;
            if (loadCount == 0) {
                assert(startCount == 0);
                unloadList.add(configuration);
            }
            return unloadList;
        }

        public int getStartCount() {
            return startCount;
        }

        public List start() {
            List startList = new LinkedList();
            for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
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
            List stopList = new LinkedList();
            for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
                ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                stopList.addAll(parent.stop());
            }
            startCount--;
            if (startCount == 0) {
                stopList.add(configuration);
            }
            return stopList;
        }
    }
}
