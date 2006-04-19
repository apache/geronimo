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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class SimpleConfigurationManager implements ConfigurationManager {
    protected static final Log log = LogFactory.getLog(SimpleConfigurationManager.class);
    protected final Collection stores;
    protected final ArtifactResolver artifactResolver;
    protected final Map configurations = new LinkedHashMap();
    protected final ConfigurationModel configurationModel = new ConfigurationModel();
    protected final Collection repositories;

    public SimpleConfigurationManager(Collection stores, ArtifactResolver artifactResolver, Collection repositories) {
        if (stores == null) stores = Collections.EMPTY_SET;
        if (repositories == null) repositories = Collections.EMPTY_SET;

        this.stores = stores;
        this.artifactResolver = artifactResolver;
        this.repositories = repositories;
    }


    public synchronized boolean isInstalled(Artifact configId) {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if(store.containsConfiguration(configId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isLoaded(Artifact configId) {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        return configurationModel.isLoaded(configId);
    }

    public synchronized boolean isRunning(Artifact configId) {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        return configurationModel.isStarted(configId);
    }

    public Artifact[] getInstalled(Artifact query) {
        return artifactResolver.queryArtifacts(query);
    }

    public Artifact[] getLoaded(Artifact query) {
        return configurationModel.getLoaded(query);
    }

    public Artifact[] getRunning(Artifact query) {
        return configurationModel.getStarted(query);
    }


    public List listStores() {
        List storeSnapshot = getStoreList();
        List result = new ArrayList(storeSnapshot.size());
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            result.add(store.getAbstractName());
        }
        return result;
    }

    public ConfigurationStore[] getStores() {
        List storeSnapshot = getStoreList();
        return (ConfigurationStore[]) storeSnapshot.toArray(new ConfigurationStore[storeSnapshot.size()]);
    }


    public List listConfigurations() {
        List storeSnapshot = getStoreList();
        List list = new ArrayList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            list.addAll(listConfigurations(store));
        }
        return list;
    }

    public ConfigurationStore getStoreForConfiguration(Artifact configId) {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if(store.containsConfiguration(configId)) {
                return store;
            }
        }
        return null;
    }

    public List listConfigurations(AbstractName storeName) throws NoSuchStoreException {
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (storeName.equals(store.getAbstractName())) {
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
                configurationInfo = new ConfigurationInfo(store.getAbstractName(),
                        configurationInfo.getConfigID(),
                        configurationInfo.getType(),
                        configurationInfo.getCreated(),
                        configurationInfo.getOwnedConfigurations(),
                        State.RUNNING);
            } else {
                configurationInfo = new ConfigurationInfo(store.getAbstractName(),
                        configurationInfo.getConfigID(),
                        configurationInfo.getType(),
                        configurationInfo.getCreated(),
                        configurationInfo.getOwnedConfigurations(), State.STOPPED);
            }
            iterator.set(configurationInfo);
        }
        return list;
    }

    public boolean isConfiguration(Artifact artifact) {
        if(!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact "+artifact+" is not fully resolved");
        }
        synchronized (this) {
            // if it is loaded, it is definitely a configuration
            if (configurations.containsKey(artifact)) {
                return true;
            }
        }

        // see if any stores think it is a configuration
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(artifact)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Configuration getConfiguration(Artifact configurationId) {
        if(!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configurationId+" is not fully resolved");
        }
        return (Configuration) configurations.get(configurationId);
    }

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return loadConfiguration(configurationId, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if(!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configurationId+" is not fully resolved");
        }
        if (configurationModel.isLoaded(configurationId)) {
            // already loaded, so just mark the configuration as user loaded
            configurationModel.load(configurationId);

            monitor.finished();
            return new LifecycleResults();
        }

        // load the ConfigurationData for the new configuration
        ConfigurationData configurationData = null;
        try {
            configurationData = loadConfigurationData(configurationId, monitor);
        } catch (Exception e) {
            LifecycleResults results = new LifecycleResults();
            results.addFailed(configurationId, e);
            monitor.finished();
            throw new LifecycleException("load", configurationId, results);
        }

        // load the configuration
        LifecycleResults results = loadConfiguration(configurationData, monitor);

        return results;
    }

    public synchronized LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return loadConfiguration(configurationData, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        Artifact id = configurationData.getId();
        LifecycleResults results = new LifecycleResults();
        if (!configurationModel.isLoaded(id)) {
            // recursively load configurations from the new child to the parents
            LinkedHashMap configurationsToLoad = new LinkedHashMap();
            try {
                loadDepthFirst(configurationData, configurationsToLoad, monitor);
            } catch (Exception e) {
                results.addFailed(id, e);
                monitor.finished();
                throw new LifecycleException("load", id, results);
            }

            // load and start the unloaded the gbean for each configuration (depth first)
            Map actuallyLoaded = new LinkedHashMap(configurationsToLoad.size());
            Artifact configurationId = null;
            try {
                for (Iterator iterator = configurationsToLoad.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    configurationId = (Artifact) entry.getKey();
                    UnloadedConfiguration unloadedConfiguration = (UnloadedConfiguration) entry.getValue();

                    monitor.loading(configurationId);
                    Configuration configuration = load(unloadedConfiguration.getConfigurationData(), unloadedConfiguration.getResolvedParentIds(), actuallyLoaded);
                    monitor.succeeded(configurationId);

                    actuallyLoaded.put(configurationId, configuration);
                }
            } catch (Exception e) {
                monitor.failed(configurationId, e);

                // there was a problem, so we need to unload all configurations that were actually loaded
                for (Iterator iterator = actuallyLoaded.values().iterator(); iterator.hasNext();) {
                    Configuration configuration = (Configuration) iterator.next();
                    unload(configuration);
                }

                results.addFailed(id, e);
                monitor.finished();
                throw new LifecycleException("load", id, results);
            }

            // update the status of the loaded configurations
            addNewConfigurationsToModel(actuallyLoaded);
            results.setLoaded(actuallyLoaded.keySet());
        }
        configurationModel.load(id);
        monitor.finished();
        return results;
    }

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet resolvedParentIds, Map loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = configurationData.getId();
        try {
            Collection parents = findParentConfigurations(resolvedParentIds, loadedConfigurations);

            Configuration configuration = new Configuration(parents, configurationData, new ConfigurationResolver(configurationData, repositories, artifactResolver), null);
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

    private void addNewConfigurationsToModel(Map loadedConfigurations) throws NoSuchConfigException {
        for (Iterator iterator = loadedConfigurations.values().iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            addNewConfigurationToModel(configuration);
        }
    }

    protected void addNewConfigurationToModel(Configuration configuration) throws NoSuchConfigException {
        configurationModel.addConfiguation(configuration.getId(),
                getLoadParentIds(configuration),
                getStartParentIds(configuration));
        configurations.put(configuration.getId(), configuration);
    }

    private Set getLoadParentIds(Configuration configuration) {
        Set loadParentIds = getConfigurationIds(configuration.getClassParents());
        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = (Configuration) iterator.next();
            Set childLoadParentIds = getLoadParentIds(childConfiguration);

            // remove this configuration's id from the parent Ids since it will cause an infinite loop
            childLoadParentIds.remove(configuration.getId());

            loadParentIds.addAll(childLoadParentIds);
        }
        return loadParentIds;
    }

    private Set getStartParentIds(Configuration configuration) {
        Set startParentIds = getConfigurationIds(configuration.getServiceParents());
        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = (Configuration) iterator.next();
            Set childStartParentIds = getStartParentIds(childConfiguration);

            // remove this configuration's id from the parent Ids since it will cause an infinite loop
            childStartParentIds.remove(configuration.getId());

            startParentIds.addAll(childStartParentIds);
        }
        return startParentIds;
    }

    private static Set getConfigurationIds(List configurations) {
        LinkedHashSet configurationIds = new LinkedHashSet(configurations.size());
        for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            configurationIds.add(configuration.getId());
        }
        return configurationIds;
    }

    private synchronized void loadDepthFirst(ConfigurationData configurationData, LinkedHashMap configurationsToLoad, LifecycleMonitor monitor) throws NoSuchConfigException, IOException, InvalidConfigException, MissingDependencyException {
        // if this parent hasn't already been processed, iterate into the parent
        Artifact configurationId = configurationData.getId();
        if (!configurationsToLoad.containsKey(configurationId)) {
            LinkedHashSet resolvedParentIds = resolveParentIds(configurationData);

            for (Iterator iterator = resolvedParentIds.iterator(); iterator.hasNext();) {
                Artifact parentId = (Artifact) iterator.next();
                // if this parent id hasn't already been loaded and is actually a configuration
                if (!configurations.containsKey(parentId) && isConfiguration(parentId)) {
                    ConfigurationData parentConfigurationData = loadConfigurationData(parentId, monitor);
                    loadDepthFirst(parentConfigurationData, configurationsToLoad, monitor);
                }
            }

            // depth first - all unloaded parents have been added, now add this configuration
            configurationsToLoad.put(configurationId, new UnloadedConfiguration(configurationData, resolvedParentIds));
        }
    }

    private ConfigurationData loadConfigurationData(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStoreList();

        monitor.addConfiguration(configurationId);
        monitor.reading(configurationId);
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configurationId)) {
                ConfigurationData configurationData = store.loadConfiguration(configurationId);
                monitor.succeeded(configurationId);
                return configurationData;
            }
        }
        NoSuchConfigException exception = new NoSuchConfigException(configurationId);
        monitor.failed(configurationId, exception);
        throw exception;
    }

    private LinkedHashSet resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        Environment environment = configurationData.getEnvironment();

        LinkedHashSet parentIds = new LinkedHashSet();
        List dependencies = new ArrayList(environment.getDependencies());
        for (ListIterator iterator = dependencies.listIterator(); iterator.hasNext();) {
            Dependency dependency = (Dependency) iterator.next();
            Artifact resolvedArtifact = artifactResolver.resolveInClassLoader(dependency.getArtifact());
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

    private static class UnloadedConfiguration {
        private final ConfigurationData configurationData;
        private final LinkedHashSet resolvedParentIds;

        public UnloadedConfiguration(ConfigurationData configurationData, LinkedHashSet resolvedParentIds) {
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

    public synchronized LifecycleResults startConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return startConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults startConfiguration(Artifact id, LifecycleMonitor monitor) throws  NoSuchConfigException, LifecycleException {
        if(!id.isResolved()) {
            throw new IllegalArgumentException("Artifact "+id+" is not fully resolved");
        }
        LinkedHashSet unstartedConfigurations = configurationModel.start(id);

        addConfigurationsToMonitor(monitor, unstartedConfigurations);

        LifecycleResults results = new LifecycleResults();
        Artifact configurationId = null;
        try {
            for (Iterator iterator = unstartedConfigurations.iterator(); iterator.hasNext();) {
                configurationId = (Artifact) iterator.next();
                Configuration configuration = getConfiguration(configurationId);

                monitor.starting(configurationId);
                start(configuration);
                monitor.succeeded(configurationId);

                results.addStarted(configurationId);
            }
        } catch (Exception e) {
            monitor.failed(configurationId, e);
            results.addFailed(configurationId, e);
            results.setStarted(Collections.EMPTY_SET);
            configurationModel.stop(id);

            for (Iterator iterator = results.getStarted().iterator(); iterator.hasNext();) {
                configurationId = (Artifact) iterator.next();
                Configuration configuration = getConfiguration(configurationId);
                monitor.stopping(configurationId);
                stop(configuration);
                monitor.succeeded(configurationId);
            }
            monitor.finished();
            throw new LifecycleException("start", id, results);
        }
        monitor.finished();
        return results;
    }

    protected void start(Configuration configuration) throws Exception {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults stopConfiguration(Artifact id) throws NoSuchConfigException {
        return stopConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults stopConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException {
        if(!id.isResolved()) {
            throw new IllegalArgumentException("Artifact "+id+" is not fully resolved");
        }
        LinkedHashSet stopList = configurationModel.stop(id);

        addConfigurationsToMonitor(monitor, stopList);

        LifecycleResults results = new LifecycleResults();
        for (Iterator iterator = stopList.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            Configuration configuration = getConfiguration(configurationId);

            monitor.stopping(configurationId);
            stop(configuration);
            monitor.succeeded(configurationId);

            results.addStopped(configurationId);
        }

        monitor.finished();
        return results;
    }

    protected void stop(Configuration configuration) {
        // Don't throw an exception because we call this from unload to be sure that all
        // unloaded configurations are stopped first
    }

    public synchronized LifecycleResults restartConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return restartConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults restartConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if(!id.isResolved()) {
            throw new IllegalArgumentException("Artifact "+id+" is not fully resolved");
        }
        // get a sorted list of configurations to restart
        LinkedHashSet restartList = configurationModel.restart(id);

        addConfigurationsToMonitor(monitor, restartList);

        // stop the configuations
        for (Iterator iterator = restartList.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            Configuration configuration = getConfiguration(configurationId);
            monitor.stopping(configurationId);
            stop(configuration);
            monitor.succeeded(configurationId);
        }

        // reverse the list
        restartList = reverse(restartList);

        // restart the configurations
        LifecycleResults results = new LifecycleResults();
        for (Iterator iterator = restartList.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();

            // skip the configurations that have alredy failed or were stopped
            if (results.wasFailed(configurationId) || results.wasStopped(configurationId)) {
                continue;
            }

            // try to start the configuation
            try {
                Configuration configuration = getConfiguration(configurationId);
                monitor.starting(configurationId);
                start(configuration);
                monitor.succeeded(configurationId);
                results.addRestarted(configurationId);
            } catch (Exception e) {
                // the configuraiton failed to restart
                results.addFailed(configurationId, e);
                monitor.failed(configurationId, e);

                // officially stop the configuration in the model (without gc)
                LinkedHashSet stopList = configurationModel.stop(configurationId, false);

                // all of the configurations to be stopped must be in our restart list, or the model is corrupt
                if (!restartList.containsAll(stopList)) {
                    throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                }

                // add the children of the failed configuration to the results as stopped
                for (Iterator iterator1 = stopList.iterator(); iterator1.hasNext();) {
                    Artifact failedId = (Artifact) iterator1.next();

                    // if any of the failed configuration is in the restarted set, the model is
                    // corrupt because we started a child before a parent
                    if (results.wasRestarted(failedId)) {
                        throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                    }

                    if (!results.wasFailed(failedId)) {
                        results.addStopped(failedId);
                    }
                }
            }
        }

        monitor.finished();
        if (!results.wasRestarted(id)) {
            throw new LifecycleException("restart", id, results);
        }
        return results;
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id) throws NoSuchConfigException {
        return unloadConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException {
        if(!id.isResolved()) {
            throw new IllegalArgumentException("Artifact "+id+" is not fully resolved");
        }
        Set started = configurationModel.getStarted();
        LinkedHashSet unloadList = configurationModel.unload(id);

        addConfigurationsToMonitor(monitor, unloadList);

        LifecycleResults results = new LifecycleResults();
        for (Iterator iterator = unloadList.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            Configuration configuration = getConfiguration(configurationId);

            // first make sure it is stopped
            if (started.contains(configurationId)) {
                monitor.stopping(configurationId);
                stop(configuration);
                monitor.succeeded(configurationId);
                results.addStopped(configurationId);
            } else {
                // call stop just to be sure the beans aren't running
                stop(configuration);
            }

            // now unload it
            monitor.unloading(configurationId);
            unload(configuration);
            monitor.succeeded(configurationId);
            results.addUnloaded(configurationId);

            // clean up the model
            configurationModel.removeConfiguration(configurationId);
            configurations.remove(configurationId);
        }
        monitor.finished();
        return results;
    }

    protected void unload(Configuration configuration) {
        try {
            configuration.doStop();
        } catch (Exception e) {
            log.debug("Problem unloading config: " + configuration.getId(), e);
        }
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return reloadConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return reloadConfiguration(id, id.getVersion(), monitor);
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id, Version version) throws NoSuchConfigException, LifecycleException {
        return reloadConfiguration(id, version, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if(!id.isResolved()) {
            throw new IllegalArgumentException("Artifact "+id+" is not fully resolved");
        }
        Configuration configuration = getConfiguration(id);
        if (configuration == null) {
            throw new NoSuchConfigException(id);
        }
        ConfigurationData existingConfigurationData = configuration.getConfigurationData();
        UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(existingConfigurationData, getResolvedParentIds(configuration));

        Artifact newId = new Artifact(id.getGroupId(), id.getArtifactId(), version, id.getType());

        // reload the ConfigurationData from a store
        ConfigurationData configurationData = null;
        try {
            configurationData = loadConfigurationData(newId, monitor);
        } catch (Exception e) {
            LifecycleResults results = new LifecycleResults();
            results.addFailed(id, e);
            monitor.finished();
            throw new LifecycleException("reload", id, results);
        }

        return reloadConfiguration(existingUnloadedConfiguration, configurationData, monitor);
    }

    public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws LifecycleException, NoSuchConfigException {
        return reloadConfiguration(configurationData, NullLifecycleMonitor.INSTANCE);
    }

    public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws LifecycleException, NoSuchConfigException {
        Configuration configuration = getConfiguration(configurationData.getId());
        if (configuration == null) {
            throw new NoSuchConfigException(configurationData.getId());
        }
        ConfigurationData existingConfigurationData = configuration.getConfigurationData();
        UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(existingConfigurationData, getResolvedParentIds(configuration));
        return reloadConfiguration(existingUnloadedConfiguration, configurationData, monitor);
    }

    private LifecycleResults reloadConfiguration(UnloadedConfiguration existingUnloadedConfiguration, ConfigurationData newConfigurationData, LifecycleMonitor monitor) throws LifecycleException, NoSuchConfigException {
        LifecycleResults results = new LifecycleResults();

        // recursively load configurations from the reloaded child to the parents
        // this will catch any new parents
        LinkedHashMap unloadedConfigurations = new LinkedHashMap();
        try {
            loadDepthFirst(newConfigurationData, unloadedConfigurations, monitor);
        } catch (Exception e) {
            results.addFailed(newConfigurationData.getId(), e);
            monitor.finished();
            throw new LifecycleException("load", newConfigurationData.getId(), results);
        }

        // get a list of the started configuration
        Set started = configurationModel.getStarted();

        // add all of the child configurations that we will need to reload to the unloaded map
        //   note: we are iterating in reverse order
        for (Iterator iterator = reverse(configurationModel.reload(newConfigurationData.getId())).iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            if (unloadedConfigurations.containsKey(configurationId)) {
                continue;
            }

            Configuration configuration = getConfiguration(configurationId);
            ConfigurationData data = configuration.getConfigurationData();
            LinkedHashSet resolvedParentIds = getResolvedParentIds(configuration);
            unloadedConfigurations.put(configurationId, new UnloadedConfiguration(data, resolvedParentIds));
            monitor.addConfiguration(configurationId);
        }

        // unload the configuations
        //   note: we are iterating in reverse order
        for (Iterator iterator = reverse(unloadedConfigurations).keySet().iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            Configuration configuration = getConfiguration(configurationId);

            // first make sure it is stopped
            if (started.contains(configurationId)) {
                monitor.stopping(configurationId);
                stop(configuration);
                monitor.succeeded(configurationId);
                results.addRestarted(configurationId);
            } else {
                // call stop just to be sure the beans aren't running
                stop(configuration);
            }

            // now unload it
            monitor.unloading(configurationId);
            unload(configuration);
            monitor.succeeded(configurationId);
        }

        // reload the configurations
        Map actuallyLoaded = new LinkedHashMap(unloadedConfigurations.size());
        for (Iterator iterator = unloadedConfigurations.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Artifact configurationId = (Artifact) entry.getKey();
            UnloadedConfiguration unloadedConfiguration = (UnloadedConfiguration) entry.getValue();

            // skip the configurations that have alredy failed or were stopped
            if (results.wasFailed(configurationId) || results.wasStopped(configurationId)) {
                continue;
            }

            // try to load the configuation
            Configuration configuration = null;
            try {
                monitor.loading(configurationId);
                configuration = load(unloadedConfiguration.getConfigurationData(), unloadedConfiguration.getResolvedParentIds(), actuallyLoaded);
                monitor.succeeded(configurationId);

                results.addReloaded(configurationId);

                // if the configuration was started before restart it
                if (started.contains(configurationId)) {
                    monitor.starting(configurationId);
                    start(configuration);
                    monitor.succeeded(configurationId);
                    results.addRestarted(configurationId);
                }

                actuallyLoaded.put(configurationId, configuration);
                configurations.put(configurationId, configuration);
            } catch (Exception e) {
                // the configuraiton failed to restart
                results.addFailed(configurationId, e);
                monitor.failed(configurationId, e);

                // unload the configuration if it was loaded and failed in start
                if (configuration != null) {
                    unload(configuration);
                }

                // if this is root configuration, attempt to reinstate the original configuration
                boolean reinstatedExisting = false;
                if (configurationId.equals(newConfigurationData.getId())) {
                    configuration = null;
                    try {
                        configuration = load(existingUnloadedConfiguration.getConfigurationData(),
                                existingUnloadedConfiguration.getResolvedParentIds(),
                                actuallyLoaded);

                        results.addReloaded(configurationId);

                        // if the configuration was started before restart it
                        if (started.contains(configurationId)) {
                            start(configuration);
                            results.addRestarted(configurationId);
                        }

                        actuallyLoaded.put(configurationId, configuration);
                        configurations.put(configurationId, configuration);

                        reinstatedExisting = true;
                    } catch (Exception ignored) {
                        // we tried our best
                        if (configuration != null) {
                            unload(configuration);
                        }
                    }
                }

                if (!reinstatedExisting) {
                    // officially unload the configuration in the model (without gc)
                    LinkedHashSet unloadList = configurationModel.unload(configurationId, false);
                    configurationModel.removeConfiguration(configurationId);

                    // all of the configurations to be unloaded must be in our unloaded list, or the model is corrupt
                    if (!unloadedConfigurations.keySet().containsAll(unloadList)) {
                        throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                    }

                    // add the children of the failed configuration to the results as unloaded
                    for (Iterator iterator1 = unloadList.iterator(); iterator1.hasNext();) {
                        Artifact failedId = (Artifact) iterator1.next();

                        // if any of the failed configuration is in the reloaded set, the model is
                        // corrupt because we loaded a child before a parent
                        if (results.wasLoaded(failedId)) {
                            throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                        }

                        if (!results.wasFailed(failedId)) {
                            results.addUnloaded(failedId);
                            if (started.contains(configurationId)) {
                                results.addStopped(failedId);
                            }
                        }
                    }
                }
            }
        }

        monitor.finished();
        if (results.wasFailed(newConfigurationData.getId()) || !results.wasReloaded(newConfigurationData.getId())) {
            throw new LifecycleException("reload", newConfigurationData.getId(), results);
        }

        return results;
    }

    private static LinkedHashSet getResolvedParentIds(Configuration configuration) {
        LinkedHashSet resolvedParentIds = new LinkedHashSet();
        for (Iterator iterator1 = configuration.getClassParents().iterator(); iterator1.hasNext();) {
            Configuration classParent = (Configuration) iterator1.next();
            resolvedParentIds.add(classParent.getId());
        }
        for (Iterator iterator1 = configuration.getServiceParents().iterator(); iterator1.hasNext();) {
            Configuration serviceParent = (Configuration) iterator1.next();
            resolvedParentIds.add(serviceParent.getId());
        }
        return resolvedParentIds;
    }

    public synchronized void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {
        if(!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configurationId+" is not fully resolved");
        }
        if (configurations.containsKey(configurationId)) {
            stopConfiguration(configurationId);
            unloadConfiguration(configurationId);
        }

        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if(store.containsConfiguration(configurationId)) {
                store.uninstall(configurationId);
            }
        }

    }

    private List getStoreList() {
        return new ArrayList(stores);
    }

    private static void addConfigurationsToMonitor(LifecycleMonitor monitor, LinkedHashSet configurations) {
        for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            monitor.addConfiguration(configurationId);
        }
    }

    private static LinkedHashSet reverse(LinkedHashSet set) {
        ArrayList reverseList = new ArrayList(set);
        Collections.reverse(reverseList);
        set = new LinkedHashSet(reverseList);
        return set;
    }

    private static LinkedHashMap reverse(LinkedHashMap map) {
        ArrayList reverseEntrySet = new ArrayList(map.entrySet());
        Collections.reverse(reverseEntrySet);

        map = new LinkedHashMap(reverseEntrySet.size());
        for (Iterator iterator = reverseEntrySet.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            map.put(key, value);
        }
        return map;
    }
}
