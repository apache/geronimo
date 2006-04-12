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
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;

/**
 * @version $Rev$ $Date$
 */
public class SimpleConfigurationManager implements ConfigurationManager {
    protected static final Log log = LogFactory.getLog(SimpleConfigurationManager.class);
    protected final Collection stores;
    protected final ArtifactResolver artifactResolver;
    protected final Map configurations = new LinkedHashMap();
    protected final Collection repositories;

    public SimpleConfigurationManager(Collection stores, ArtifactResolver artifactResolver, Collection repositories) {
        if (stores == null) stores = Collections.EMPTY_SET;
        if (repositories == null) repositories = Collections.EMPTY_SET;

        this.stores = stores;
        this.artifactResolver = artifactResolver;
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

    public List listConfigurations() {
        List storeSnapshot = getStores();
        List list = new ArrayList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            list.addAll(listConfigurations(store));
        }
        return list;
    }

    public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
        List storeSnapshot = getStores();
        List result = new ArrayList(storeSnapshot.size());
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if(store.containsConfiguration(configuration)) {
                return store;
            }
        }
        return null;
    }

    public List listConfigurations(ObjectName storeName) throws NoSuchStoreException {
        List storeSnapshot = getStores();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (storeName.equals(JMXUtil.getObjectName(store.getObjectName()))) {
                return listConfigurations(store);
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    private List listConfigurations(ConfigurationStore store) {
        List list = store.listConfigurations();
        for (ListIterator iterator = list.listIterator(); iterator.hasNext();) {
            ConfigurationInfo configurationInfo = (ConfigurationInfo) iterator.next();
            if (isRunning(configurationInfo.getConfigID())) {
                configurationInfo = new ConfigurationInfo(store.getAbstractName(), configurationInfo.getConfigID(), State.RUNNING, configurationInfo.getType());
            } else {
                configurationInfo = new ConfigurationInfo(store.getAbstractName(), configurationInfo.getConfigID(), State.STOPPED, configurationInfo.getType());
            }
            iterator.set(configurationInfo);
        }
        return list;
    }

    public boolean isConfiguration(Artifact artifact) {
        if (configurations.containsKey(artifact)) {
            return true;
        }

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

    public synchronized boolean isLoaded(Artifact configurationId) {
        return configurations.containsKey(configurationId);
    }

    public synchronized boolean isRunning(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.getStartCount() > 0;
        }
        return false;
    }

    public synchronized void loadConfiguration(Artifact configurationId) throws NoSuchConfigException, IOException, InvalidConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            // already loaded, so just update the load count
            configurationStatus.load();
            configurationStatus.getConfiguration();
            return;
        }

        // load the ConfigurationData for the new configuration
        ConfigurationData configurationData = loadConfigurationGBeanData(configurationId);

        // load the configuration
        loadConfiguration(configurationData);
    }

    public synchronized Configuration loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, IOException, InvalidConfigException {
        Artifact id = configurationData.getId();
        if (configurations.containsKey(id)) {
            // already loaded, so just update the load count
            ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
            configurationStatus.load();
            return configurationStatus.getConfiguration();
        }

        // load configurations from the new child to the parents
        LinkedHashMap unloadedConfigurations = new LinkedHashMap();
        loadDepthFirst(configurationData, unloadedConfigurations);

        // load and start the unloaded configurations depth first
        Map loadedConfigurations = new LinkedHashMap(unloadedConfigurations.size());
        try {
            for (Iterator iterator = unloadedConfigurations.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Artifact configurationId = (Artifact) entry.getKey();
                UnloadedContiguration unloadedConfiguration = (UnloadedContiguration) entry.getValue();
                Configuration configuration = load(unloadedConfiguration.getConfigurationData(), unloadedConfiguration.getResolvedParentIds(), loadedConfigurations);
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
            configurations.put(configuration.getId(), configurationStatus);
        }

        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        configurationStatus.load();
        return configurationStatus.getConfiguration();
    }

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet resolvedParentIds, Map loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = configurationData.getId();
        try {
            Collection parents = findParentConfigurations(resolvedParentIds, loadedConfigurations);

            Configuration configuration = new Configuration(parents, configurationData, new ConfigurationResolver(configurationData, repositories, artifactResolver));
            configuration.doStart();
            return configuration;
        } catch (Exception e) {
            throw new InvalidConfigException("Error starting configuration gbean " + configurationId, e);
        }
    }

    private Collection findParentConfigurations(LinkedHashSet resolvedParentIds, Map loadedConfigurations) throws InvalidConfigException {
        LinkedHashMap parents = new LinkedHashMap();
        for (Iterator iterator = resolvedParentIds.iterator(); iterator.hasNext();) {
            Artifact resolvedArtifact = (Artifact) iterator.next();

            Configuration parent = null;
            if (loadedConfigurations.containsKey(resolvedArtifact)) {
                parent = (Configuration) loadedConfigurations.get(resolvedArtifact);
            } else if (isConfiguration(resolvedArtifact)) {
                parent = getConfiguration(resolvedArtifact);
            } else {
                throw new InvalidConfigException("Cound not find parent configuration: " + resolvedArtifact);
            }

            parents.put(resolvedArtifact, parent);
        }
        return parents.values();
    }


    protected synchronized ConfigurationStatus createConfigurationStatus(Configuration configuration) {
        // start parents are just the service parents of the configuration... we want the services to be running so we can use them
        List startParents = getParentStatuses(configuration.getServiceParents());

        // load parents are both the class parents and the service parents
        LinkedHashSet loadParents = new LinkedHashSet(startParents);
        loadParents.addAll(getParentStatuses(configuration.getClassParents()));

        return new ConfigurationStatus(configuration, new ArrayList(loadParents), startParents);
    }

    private synchronized List getParentStatuses(List parents) {
        List parentStatuses = new ArrayList(parents.size());
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration parent = (Configuration) iterator.next();
            ConfigurationStatus parentStatus = (ConfigurationStatus) configurations.get(parent.getId());
            if (parentStatus == null) {
                throw new IllegalStateException("Parent status not found " + parent.getId());
            }

            parentStatuses.add(parentStatus);
        }
        return parentStatuses;
    }

    private synchronized void loadDepthFirst(ConfigurationData configurationData, LinkedHashMap unloadedConfigurations) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            // if this parent hasn't already been processed, iterate into the parent
            Artifact configurationId = configurationData.getId();
            if (!unloadedConfigurations.containsKey(configurationId)) {
                LinkedHashSet resolvedParentIds = resolveParentIds(configurationData);

                for (Iterator iterator = resolvedParentIds.iterator(); iterator.hasNext();) {
                    Artifact parentId = (Artifact) iterator.next();
                    if (!configurations.containsKey(parentId) && isConfiguration(parentId)) {
                        ConfigurationData parentConfigurationData = loadConfigurationGBeanData(parentId);
                        loadDepthFirst(parentConfigurationData, unloadedConfigurations);
                    }
                }

                // depth first - all unloaded parents have been added, now add this configuration
                unloadedConfigurations.put(configurationId, new UnloadedContiguration(configurationData, resolvedParentIds));
            }
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

    private ConfigurationData loadConfigurationGBeanData(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStores();

        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configId)) {
                ConfigurationData configurationData = store.loadConfiguration(configId);
                return configurationData;
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configId);
    }

    private LinkedHashSet resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        Environment environment = configurationData.getEnvironment();

        LinkedHashSet parentIds = new LinkedHashSet();
        List dependencies = new ArrayList(environment.getDependencies());
        for (ListIterator iterator = dependencies.listIterator(); iterator.hasNext();) {
            Dependency dependency = (Dependency) iterator.next();
            Artifact resolvedArtifact = artifactResolver.resolve(dependency.getArtifact());
            if (isConfiguration(resolvedArtifact)) {
                parentIds.add(resolvedArtifact);

                // update the dependency list to contain the resolved artifact
                dependency = new Dependency(resolvedArtifact, dependency.getImportType());
                iterator.set(dependency);
            } else if (dependency.getImportType() == ImportType.SERVICES) {
                // Service depdendencies require that the depdencency be a configuration
                throw new InvalidConfigException("Dependency does not have services: " + resolvedArtifact);
            }
        }

        for (Iterator iterator = configurationData.getChildConfigurations().values().iterator(); iterator.hasNext();) {
            ConfigurationData childConfigurationData = (ConfigurationData) iterator.next();
            LinkedHashSet childParentIds = resolveParentIds(childConfigurationData);
            // remove this configuration's id from the parent Ids since it will cause an infinite loop
            childParentIds.remove(configurationData.getId());
            parentIds.addAll(childParentIds);
        }
        return parentIds;
    }

    private static class UnloadedContiguration {
        private final ConfigurationData configurationData;
        private final LinkedHashSet resolvedParentIds;

        public UnloadedContiguration(ConfigurationData configurationData, LinkedHashSet resolvedParentIds) {
            this.configurationData = configurationData;
            this.resolvedParentIds = resolvedParentIds;
        }

        public ConfigurationData getConfigurationData() {
            return configurationData;
        }

        public LinkedHashSet getResolvedParentIds() {
            return resolvedParentIds;
        }
    }

    public void startConfiguration(Configuration configuration) throws InvalidConfigException {
        startConfiguration(configuration.getId());
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
            configurationStatus.stop();
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
        stopConfiguration(configuration.getId());
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

    protected void stop(Configuration configuration) {
        throw new UnsupportedOperationException();
    }

    public void unloadConfiguration(Configuration configuration) throws NoSuchConfigException {
        unloadConfiguration(configuration.getId());
    }

    public synchronized void unloadConfiguration(Artifact id) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(id);
        List unloadList = configurationStatus.unload();
        for (Iterator iterator = unloadList.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            unload(configuration);
            configurations.remove(configuration.getId());
        }
    }

    protected void unload(Configuration configuration) {
        try {
            configuration.doStop();
        } catch (Exception e) {
            log.debug("Problem unloading config: " + configuration.getId(), e);
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
            if (!loadParents.containsAll(startParents)) {
                throw new IllegalArgumentException("loadParents must contain all startParents");
            }
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
