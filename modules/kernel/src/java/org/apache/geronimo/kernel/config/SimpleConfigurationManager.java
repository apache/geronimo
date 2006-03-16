/**
 *
 * Copyright 2005 The Apache Software Foundation
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
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.Naming;

/**
 * @version $Rev$ $Date$
 */
public class SimpleConfigurationManager implements ConfigurationManager {
    protected static final Log log = LogFactory.getLog(SimpleConfigurationManager.class);
    protected final Collection stores;
    protected final ArtifactResolver artifactResolver;
    protected final Map configurations = new LinkedHashMap();
    protected final Collection repositories;
    private final Naming naming;

    public SimpleConfigurationManager(Collection stores, ArtifactResolver artifactResolver, Naming naming, Collection repositories) {
        if (naming == null) throw new NullPointerException("naming is null");
        if (stores == null) stores = Collections.EMPTY_SET;
        if (repositories == null) repositories = Collections.EMPTY_SET;

        this.stores = stores;
        this.artifactResolver = artifactResolver;
        this.naming = naming;
        this.repositories = repositories;
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

    public synchronized Configuration getConfiguration(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        return configurationStatus.getConfiguration();
    }

    protected Artifact getConfigurationId(Configuration configuration) {
        return configuration.getEnvironment().getConfigId();
    }

    public synchronized boolean isLoaded(Artifact configId) {
        return configurations.containsKey(configId);
    }

    public synchronized Configuration loadConfiguration(Artifact configurationId) throws NoSuchConfigException, IOException, InvalidConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            // already loaded, so just update the load count
            configurationStatus.load();
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

    private synchronized Configuration loadConfiguration(GBeanData gbeanData) throws NoSuchConfigException, IOException, InvalidConfigException {
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
                Configuration configuration = load(configurationData, loadedConfigurations);
                loadedConfigurations.put(configurationId, configuration);
            }
        } catch (Exception e) {
            for (Iterator iterator = loadedConfigurations.values().iterator(); iterator.hasNext();) {
                Configuration configuration = (Configuration) iterator.next();
                unload(configuration);
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

    protected Configuration load(GBeanData configurationData, Map loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = getConfigurationId(configurationData);
        try {
            Environment environment = (Environment) configurationData.getAttribute("environment");

            LinkedHashMap parents = new LinkedHashMap();
            List dependencies = new ArrayList(environment.getDependencies());
            for (ListIterator iterator = dependencies.listIterator(); iterator.hasNext();) {
                Dependency dependency = (Dependency) iterator.next();
                Artifact resolvedArtifact = artifactResolver.resolve(dependency.getArtifact());

                Configuration parent = null;
                if (loadedConfigurations.containsKey(resolvedArtifact)) {
                    parent = (Configuration) loadedConfigurations.get(resolvedArtifact);
                } else if (isConfiguration(resolvedArtifact)) {
                    parent = getConfiguration(resolvedArtifact);
                } else if (dependency.getImportType() == ImportType.SERVICES) {
                    // Service depdendencies require that the depdencency be a configuration
                    throw new InvalidConfigException("Dependency does not have services: " + resolvedArtifact);
                }

                if (parent != null) {
                    parents.put(resolvedArtifact, parent);

                    // update the dependency list to contain the resolved artifact
                    dependency = new Dependency(resolvedArtifact, dependency.getImportType());
                    iterator.set(dependency);
                }
            }
            environment.setDependencies(dependencies);

            ConfigurationModuleType moduleType = (ConfigurationModuleType) configurationData.getAttribute("moduleType");
            List classPath = (List) configurationData.getAttribute("classPath");
            byte[] gbeanState = (byte[]) configurationData.getAttribute("gBeanState");
            ConfigurationResolver configurationResolver = (ConfigurationResolver) configurationData.getAttribute("configurationResolver");

            Configuration configuration = new Configuration(parents.values(), moduleType, environment, classPath, gbeanState, configurationResolver, naming);
            configuration.doStart();
            return configuration;
        } catch (Exception e) {
            throw new InvalidConfigException("Error starting configuration gbean " + configurationId, e);
        }
    }


    protected synchronized ConfigurationStatus createConfigurationStatus(Configuration configuration) {
        // start parents are just the service parents of the configuration... we want the services to be running so we can use them
        List startParents = getParentStatuses(configuration.getServiceParents());

        // load parents are both the class parents and the service parents
        LinkedHashSet loadParents = new LinkedHashSet(startParents);
        loadParents.addAll(getParentStatuses(configuration.getClassParents()));

        ConfigurationStatus configurationStatus = new ConfigurationStatus(configuration, new ArrayList(loadParents), startParents);
        return configurationStatus;
    }

    private synchronized List getParentStatuses(List parents) {
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

    protected static Artifact getConfigurationId(GBeanData gbeanData) {
        Environment environment = (Environment) gbeanData.getAttribute("environment");
        return environment.getConfigId();
    }

    private synchronized void loadDepthFirst(GBeanData gbeanData, LinkedHashMap unloadedConfigurations) throws NoSuchConfigException, IOException, InvalidConfigException {
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

        gbeanData.setAttribute("naming", naming);
    }

    public void startConfiguration(Configuration configuration) throws InvalidConfigException {
        startConfiguration(getConfigurationId(configuration));
    }

    public synchronized void startConfiguration(Artifact id) throws InvalidConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        if (configurationStatus == null) {
            throw new InvalidConfigurationException("Configuration is not loaded " + id);
        }

        List unstartedConfigurations = configurationStatus.start();
        List startedConfigurations = new ArrayList(unstartedConfigurations.size());
        try {
            for (Iterator iterator = unstartedConfigurations.iterator(); iterator.hasNext();) {
                Configuration configuration = (Configuration) iterator.next();
                start(configuration);
                startedConfigurations.add(configuration);
            }
        } catch (Exception e) {
            for (Iterator iterator = startedConfigurations.iterator(); iterator.hasNext();) {
                Configuration configuration = (Configuration) iterator.next();
                stop(configuration);
            }
            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Unable to start configuration gbean " + id, e);
        }
    }

    protected void start(Configuration configuration) throws InvalidConfigException {
        throw new UnsupportedOperationException();
    }

    public void stopConfiguration(Configuration configuration) throws InvalidConfigException {
        stopConfiguration(getConfigurationId(configuration));
    }

    public synchronized void stopConfiguration(Artifact id) throws InvalidConfigException {
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

    protected void stop(Configuration configuration) throws InvalidConfigException {
        throw new UnsupportedOperationException();
    }

    public void unloadConfiguration(Configuration configuration) throws NoSuchConfigException {
        unloadConfiguration(getConfigurationId(configuration));
    }

    public synchronized void unloadConfiguration(Artifact id) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        List unloadList = configurationStatus.unload();
        for (Iterator iterator = unloadList.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            Artifact configurationId = getConfigurationId(configuration);
            unload(configuration);
            configurations.remove(configurationId);
        }
    }

    protected void unload(Configuration configuration) {
        try {
            configuration.doStop();
        } catch (Exception e) {
            log.debug("Problem unloading config: " + getConfigurationId(configuration), e);
        }
    }

    private List getStores() {
        return new ArrayList(stores);
    }

    protected static class ConfigurationStatus {
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
                KernelConfigurationManager.ConfigurationStatus parent = (KernelConfigurationManager.ConfigurationStatus) iterator.next();
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
                KernelConfigurationManager.ConfigurationStatus parent = (KernelConfigurationManager.ConfigurationStatus) iterator.next();
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
                KernelConfigurationManager.ConfigurationStatus parent = (KernelConfigurationManager.ConfigurationStatus) iterator.next();
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
                KernelConfigurationManager.ConfigurationStatus parent = (KernelConfigurationManager.ConfigurationStatus) iterator.next();
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
