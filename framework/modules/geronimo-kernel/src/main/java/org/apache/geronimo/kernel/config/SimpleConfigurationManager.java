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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class SimpleConfigurationManager implements ConfigurationManager {
    protected static final Log log = LogFactory.getLog(SimpleConfigurationManager.class);
    protected final Collection stores;
    private final ArtifactResolver artifactResolver;
    protected final Map configurations = new LinkedHashMap();
    protected final ConfigurationModel configurationModel = new ConfigurationModel();
    protected final Collection<? extends Repository> repositories;
    protected final Collection watchers;

    /**
     * When this is not null, it points to the "new" configuration that is
     * part of an in-process reload operation.  This configuration will
     * definitely be loaded, but might not be started yet.  It shold never be
     * populated outside the scope of a reload operation.
     */
    private Configuration reloadingConfiguration;


    public SimpleConfigurationManager(Collection stores, ArtifactResolver artifactResolver, Collection<? extends Repository> repositories) {
        this(stores, artifactResolver, repositories, Collections.EMPTY_SET);
    }

    public SimpleConfigurationManager(Collection stores,
                                      ArtifactResolver artifactResolver,
                                      Collection<? extends Repository> repositories,
                                      Collection watchers) {
        if (stores == null) stores = Collections.EMPTY_SET;
        if (repositories == null) repositories = Collections.emptySet();
        if (watchers == null) watchers = Collections.EMPTY_SET;

        this.stores = stores;
        this.artifactResolver = artifactResolver;
        this.repositories = repositories;
        this.watchers = watchers;
    }

    public synchronized boolean isInstalled(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isLoaded(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        if (reloadingConfiguration != null && reloadingConfiguration.getId().equals(configId)) {
            return true;
        }
        return configurationModel.isLoaded(configId);
    }

    public synchronized boolean isRunning(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        return configurationModel.isStarted(configId);
    }

    public Artifact[] getInstalled(Artifact query) {
        Artifact[] all = artifactResolver.queryArtifacts(query);
        List configs = new ArrayList();
        for (int i = 0; i < all.length; i++) {
            Artifact artifact = all[i];
            if (isConfiguration(artifact)) {
                configs.add(artifact);
            }
        }
        if (configs.size() == all.length) {
            return all;
        }
        return (Artifact[]) configs.toArray(new Artifact[configs.size()]);
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

    public Collection<? extends Repository> getRepositories() {
        return repositories;
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
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configId)) {
                return store;
            }
        }
        return null;
    }

    public List<ConfigurationInfo> listConfigurations(AbstractName storeName) throws NoSuchStoreException {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        for (ConfigurationStore store : storeSnapshot) {
            if (storeName.equals(store.getAbstractName())) {
                return listConfigurations(store);
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    private List<ConfigurationInfo> listConfigurations(ConfigurationStore store) {
        List<ConfigurationInfo> list = store.listConfigurations();
        for (ListIterator<ConfigurationInfo> iterator = list.listIterator(); iterator.hasNext();) {
            ConfigurationInfo configurationInfo = (ConfigurationInfo) iterator.next();
            if (isRunning(configurationInfo.getConfigID())) {
                configurationInfo = new ConfigurationInfo(store.getAbstractName(),
                        configurationInfo.getConfigID(),
                        configurationInfo.getType(),
                        configurationInfo.getCreated(),
                        configurationInfo.getOwnedConfigurations(),
                        configurationInfo.getChildConfigurations(),
                        configurationInfo.getInPlaceLocation(),
                        State.RUNNING);
            } else {
                configurationInfo = new ConfigurationInfo(store.getAbstractName(),
                        configurationInfo.getConfigID(),
                        configurationInfo.getType(),
                        configurationInfo.getCreated(),
                        configurationInfo.getOwnedConfigurations(),
                        configurationInfo.getChildConfigurations(),
                        configurationInfo.getInPlaceLocation(),
                        State.STOPPED);
            }
            iterator.set(configurationInfo);
        }
        return list;
    }

    public boolean isConfiguration(Artifact artifact) {
        if (!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact " + artifact + " is not fully resolved");
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
        if (!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configurationId + " is not fully resolved");
        }
        if (reloadingConfiguration != null && reloadingConfiguration.getId().equals(configurationId)) {
            return reloadingConfiguration;
        }
        return (Configuration) configurations.get(configurationId);
    }

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return loadConfiguration(configurationId, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configurationId + " is not fully resolved");
        }
        if (isLoaded(configurationId)) {
            // already loaded, so just mark the configuration as user loaded
            load(configurationId);

            monitor.finished();
            return new LifecycleResults();
        }

        // load the ConfigurationData for the new configuration
        ConfigurationData configurationData = null;
        try {
            configurationData = loadConfigurationData(configurationId, monitor);
        } catch (Exception e) {
            monitor.finished();
            throw new LifecycleException("load", configurationId, e);
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
        if (!isLoaded(id)) {
            // recursively load configurations from the new child to the parents
            LinkedHashMap configurationsToLoad = new LinkedHashMap();
            try {
                loadDepthFirst(configurationData, configurationsToLoad, monitor);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("load", id, e);
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

                monitor.finished();
                throw new LifecycleException("load", id, e);
            }

            // update the status of the loaded configurations
            addNewConfigurationsToModel(actuallyLoaded);
            results.setLoaded(actuallyLoaded.keySet());
        }
        load(id);
        monitor.finished();
        return results;
    }

    protected void load(Artifact configurationId) throws NoSuchConfigException {
        configurationModel.load(configurationId);
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
            } else if (isLoaded(resolvedArtifact)) {
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
                getConfigurationIds(getLoadParents(configuration)),
                getConfigurationIds(getStartParents(configuration)));
        configurations.put(configuration.getId(), configuration);
    }

    protected LinkedHashSet getLoadParents(Configuration configuration) {
        LinkedHashSet loadParent = new LinkedHashSet(configuration.getClassParents());
        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = (Configuration) iterator.next();
            LinkedHashSet childLoadParent = getLoadParents(childConfiguration);

            // remove this configuration from the parent Ids since it will cause an infinite loop
            childLoadParent.remove(configuration);

            loadParent.addAll(childLoadParent);
        }
        return loadParent;
    }

    protected LinkedHashSet getStartParents(Configuration configuration) {
        LinkedHashSet startParent = new LinkedHashSet(configuration.getServiceParents());
        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = (Configuration) iterator.next();
            LinkedHashSet childStartParent = getStartParents(childConfiguration);

            // remove this configuration from the parent Ids since it will cause an infinite loop
            childStartParent.remove(configuration);

            startParent.addAll(childStartParent);
        }
        return startParent;
    }

    private static LinkedHashSet<Artifact> getConfigurationIds(Collection<Configuration> configurations) {
        LinkedHashSet<Artifact> configurationIds = new LinkedHashSet<Artifact>(configurations.size());
        for (Configuration configuration : configurations) {
            configurationIds.add(configuration.getId());
        }
        return configurationIds;
    }

    private synchronized void loadDepthFirst(ConfigurationData configurationData, LinkedHashMap<Artifact, UnloadedConfiguration> configurationsToLoad, LifecycleMonitor monitor) throws NoSuchConfigException, IOException, InvalidConfigException, MissingDependencyException {
        // if this parent hasn't already been processed, iterate into the parent
        Artifact configurationId = configurationData.getId();
        if (!configurationsToLoad.containsKey(configurationId)) {
            monitor.resolving(configurationId);
            LinkedHashSet<Artifact> resolvedParentIds = resolveParentIds(configurationData);
            monitor.succeeded(configurationId);

            for (Artifact parentId : resolvedParentIds) {
                // if this parent id hasn't already been loaded and is actually a configuration
                if (!isLoaded(parentId) && isConfiguration(parentId)) {
                    ConfigurationData parentConfigurationData = loadConfigurationData(parentId, monitor);
                    loadDepthFirst(parentConfigurationData, configurationsToLoad, monitor);
                }
            }

            // depth first - all unloaded parents have been added, now add this configuration
            configurationsToLoad.put(configurationId, new UnloadedConfiguration(configurationData, resolvedParentIds));
        }
    }

    public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
        LinkedHashSet<Artifact> sorted = new LinkedHashSet<Artifact>();
        sort(ids, sorted, monitor);
        sorted.retainAll(ids);
        return sorted;
    }

    private void sort(Collection<Artifact> ids, LinkedHashSet<Artifact> sorted, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
        for (Artifact id : ids) {
            if (!sorted.contains(id)) {
                ConfigurationData data = loadConfigurationData(id, monitor);
                LinkedHashSet<Artifact> parents = resolveParentIds(data);
                sort(parents, sorted, monitor);
                sorted.add(id);
            }
        }
    }

    private ConfigurationData loadConfigurationData(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, IOException, InvalidConfigException {
        List<ConfigurationStore> storeSnapshot = getStoreList();

        monitor.addConfiguration(configurationId);
        monitor.reading(configurationId);
        for (ConfigurationStore store : storeSnapshot) {
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

    private LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        Environment environment = configurationData.getEnvironment();

        LinkedHashSet<Artifact> parentIds = new LinkedHashSet<Artifact>();
        List<Dependency> dependencies = new ArrayList<Dependency>(environment.getDependencies());
        for (ListIterator<Dependency> iterator = dependencies.listIterator(); iterator.hasNext();) {
            Dependency dependency = iterator.next();
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
            LinkedHashSet<Artifact> childParentIds = resolveParentIds(childConfigurationData);
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

    public synchronized LifecycleResults startConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
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
            configurationModel.stop(id);

            for (Iterator iterator = results.getStarted().iterator(); iterator.hasNext();) {
                configurationId = (Artifact) iterator.next();
                Configuration configuration = getConfiguration(configurationId);
                monitor.stopping(configurationId);
                stop(configuration);
                monitor.succeeded(configurationId);
            }
            monitor.finished();
            throw new LifecycleException("start", id, e);
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
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
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
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        // get a sorted list of configurations to restart
        LinkedHashSet restartList = configurationModel.restart(id);

        addConfigurationsToMonitor(monitor, restartList);

        // stop the configuations
        LifecycleResults results = new LifecycleResults();
        for (Iterator iterator = restartList.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            Configuration configuration = getConfiguration(configurationId);
            monitor.stopping(configurationId);
            stop(configuration);
            monitor.succeeded(configurationId);
            results.addStopped(configurationId);
        }

        // reverse the list
        restartList = reverse(restartList);

        // restart the configurations
        Set skip = new HashSet();
        for (Iterator iterator = restartList.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();

            // skip the configurations that have alredy failed or are children of failed configurations
            if (skip.contains(configurationId)) {
                continue;
            }

            // try to start the configuation
            try {
                Configuration configuration = getConfiguration(configurationId);
                monitor.starting(configurationId);
                start(configuration);
                monitor.succeeded(configurationId);
                results.addStarted(configurationId);
            } catch (Exception e) {
                // the configuraiton failed to restart
                results.addFailed(configurationId, e);
                monitor.failed(configurationId, e);
                skip.add(configurationId);

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
                    if (results.wasStarted(failedId)) {
                        throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                    }

                    skip.add(failedId);
                }
            }
        }

        monitor.finished();
        if (!results.wasStarted(id)) {
            throw new LifecycleException("restart", id, results);
        }
        return results;
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id) throws NoSuchConfigException {
        return unloadConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
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
            removeConfigurationFromModel(configurationId);
        }
        monitor.finished();
        return results;
    }

    protected void removeConfigurationFromModel(Artifact configurationId) throws NoSuchConfigException {
        if (configurationModel.containsConfiguration(configurationId)) {
            configurationModel.removeConfiguration(configurationId);
        }
        configurations.remove(configurationId);
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
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        Configuration configuration = getConfiguration(id);
        if (configuration == null) { // The configuration to reload is not currently loaded
            ConfigurationData data = null;
            List storeSnapshot = getStoreList();
            for (int i = 0; i < storeSnapshot.size(); i++) {
                ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
                if (store.containsConfiguration(id)) {
                    try {
                        data = store.loadConfiguration(id);
                    } catch (Exception e) {
                        log.warn("Unable to load existing configuration " + id + " from config store", e);
                    }
                }
            }
            if (data == null) {
                throw new NoSuchConfigException(id);
            }
            UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(data, new LinkedHashSet());
            Artifact newId = new Artifact(id.getGroupId(), id.getArtifactId(), version, id.getType());
            ConfigurationData newData = null;
            try {
                newData = loadConfigurationData(newId, monitor);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("reload", id, e);
            }

            return reloadConfiguration(existingUnloadedConfiguration, newData, monitor);
        } else { // The configuration to reload is loaded
            ConfigurationData existingConfigurationData = configuration.getConfigurationData();
            UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(existingConfigurationData, getResolvedParentIds(configuration));

            Artifact newId = new Artifact(id.getGroupId(), id.getArtifactId(), version, id.getType());

            // reload the ConfigurationData from a store
            ConfigurationData configurationData = null;
            try {
                configurationData = loadConfigurationData(newId, monitor);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("reload", id, e);
            }

            return reloadConfiguration(existingUnloadedConfiguration, configurationData, monitor);
        }
    }

    public synchronized LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws LifecycleException, NoSuchConfigException {
        return reloadConfiguration(configurationData, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws LifecycleException, NoSuchConfigException {
        Configuration configuration = getConfiguration(configurationData.getId());
        if (configuration == null) {
            throw new NoSuchConfigException(configurationData.getId());
        }
        ConfigurationData existingConfigurationData = configuration.getConfigurationData();
        UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(existingConfigurationData, getResolvedParentIds(configuration));
        return reloadConfiguration(existingUnloadedConfiguration, configurationData, monitor);
    }

    private boolean hasHardDependency(Artifact configurationId, ConfigurationData configurationData) {
        for (Iterator iterator = configurationData.getEnvironment().getDependencies().iterator(); iterator.hasNext();) {
            Dependency dependency = (Dependency) iterator.next();
            Artifact artifact = dependency.getArtifact();
            if (artifact.getVersion() != null && artifact.matches(configurationId)) {
                return true;
            }
        }

        for (Iterator iterator = configurationData.getChildConfigurations().values().iterator(); iterator.hasNext();) {
            ConfigurationData childConfigurationData = (ConfigurationData) iterator.next();
            if (hasHardDependency(configurationId, childConfigurationData)) {
                return true;
            }
        }
        return false;
    }

    // todo this method ignores garbage collection of configurations
    private LifecycleResults reloadConfiguration(UnloadedConfiguration existingUnloadedConfiguration, ConfigurationData newConfigurationData, LifecycleMonitor monitor) throws LifecycleException, NoSuchConfigException {
        boolean force = false;

        Artifact existingConfigurationId = existingUnloadedConfiguration.getConfigurationData().getId();
        Artifact newConfigurationId = newConfigurationData.getId();

        //
        // recursively load the new configuration; this will catch any new parents
        //
        LinkedHashMap newConfigurations = new LinkedHashMap();
        try {
            loadDepthFirst(newConfigurationData, newConfigurations, monitor);
        } catch (Exception e) {
            monitor.finished();
            throw new LifecycleException("reload", newConfigurationId, e);
        }

        //
        // get a list of the started configuration, so we can restart them later
        //
        Set started = configurationModel.getStarted();

        //
        // get a list of the child configurations that will need to reload
        //
        //   note: we are iterating in reverse order
        LinkedHashMap existingParents = new LinkedHashMap();
        LinkedHashMap reloadChildren = new LinkedHashMap();
        for (Iterator iterator = reverse(configurationModel.reload(existingConfigurationId)).iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();

            if (configurationId.equals(existingConfigurationId)) {
                continue;
            }

            // if new configurations contains the child something we have a circular dependency
            if (newConfigurations.containsKey(configurationId)) {
                throw new LifecycleException("reload", newConfigurationId,
                        new IllegalStateException("Circular depenency between " + newConfigurationId + " and " + configurationId));
            }

            Configuration configuration = getConfiguration(configurationId);
            ConfigurationData configurationData = configuration.getConfigurationData();

            // save off the exising resolved parent ids in case we need to restore this configuration
            LinkedHashSet existingParentIds = getResolvedParentIds(configuration);
            existingParents.put(configurationId, existingParentIds);

            // check that the child doen't have a hard dependency on the old configuration
            LinkedHashSet resolvedParentIds = null;
            if (hasHardDependency(existingConfigurationId, configurationData)) {
                if (force) {
                    throw new LifecycleException("reload", newConfigurationId,
                            new IllegalStateException("Existing configuration " + configurationId + " has a hard dependency on the current version of this configuration " + existingConfigurationId));
                }

                // we leave the resolved parent ids null to signal that we should not reload the configuration
                resolvedParentIds = null;
            } else {
                resolvedParentIds = new LinkedHashSet(existingParentIds);
                resolvedParentIds.remove(existingConfigurationId);
                resolvedParentIds.add(newConfigurationId);
            }

            reloadChildren.put(configurationId, new UnloadedConfiguration(configurationData, resolvedParentIds));
            monitor.addConfiguration(configurationId);
        }

        //
        // unload the children
        //

        // note: we are iterating in reverse order
        LifecycleResults results = new LifecycleResults();
        for (Iterator iterator = reverse(reloadChildren).keySet().iterator(); iterator.hasNext();) {
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
        }

        //
        // unload the existing config
        //
        Configuration existingConfiguration = getConfiguration(existingConfigurationId);
        if (started.contains(existingConfigurationId)) {
            monitor.stopping(existingConfigurationId);
            stop(existingConfiguration);
            monitor.succeeded(existingConfigurationId);
            results.addStopped(existingConfigurationId);
        } else if (existingConfiguration != null) {
            // call stop just to be sure the beans aren't running
            stop(existingConfiguration);
        }
        if (existingConfiguration != null) {
            monitor.unloading(existingConfigurationId);
            unload(existingConfiguration);
            monitor.succeeded(existingConfigurationId);
            results.addUnloaded(existingConfigurationId);
        }

        //
        // load the new configurations
        //
        boolean reinstatedExisting = false;
        /* reduce variable scope */
        {
            Map loadedParents = new LinkedHashMap();
            Map startedParents = new LinkedHashMap();
            Configuration newConfiguration = null;
            Artifact configurationId = null;
            try {
                //
                // load all of the new configurations
                //
                for (Iterator iterator = newConfigurations.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    configurationId = (Artifact) entry.getKey();
                    UnloadedConfiguration unloadedConfiguration = (UnloadedConfiguration) entry.getValue();

                    monitor.loading(configurationId);
                    Configuration configuration = load(unloadedConfiguration.getConfigurationData(), unloadedConfiguration.getResolvedParentIds(), loadedParents);
                    monitor.succeeded(configurationId);

                    if (configurationId.equals(newConfigurationId)) {
                        newConfiguration = configuration;
                        reloadingConfiguration = configuration;
                    } else {
                        loadedParents.put(configurationId, configuration);
                    }
                }

                if (newConfiguration == null) {
                    AssertionError cause = new AssertionError("Internal error: configuration was not load");
                    results.addFailed(newConfigurationId, cause);
                    throw new LifecycleException("reload", newConfigurationId, results);
                }

                //
                // start the new configurations if the old one was running
                //
                if (started.contains(existingConfigurationId)) {

                    // determine which of the parents we need to start
                    LinkedHashSet startList = new LinkedHashSet();
                    for (Iterator iterator = getStartParents(newConfiguration).iterator(); iterator.hasNext();) {
                        Configuration serviceParent = (Configuration) iterator.next();
                        if (loadedParents.containsKey(serviceParent.getId())) {
                            startList.add(serviceParent);
                        }
                    }

                    // start the new parents
                    for (Iterator iterator = startList.iterator(); iterator.hasNext();) {
                        Configuration startParent = (Configuration) iterator.next();
                        monitor.starting(configurationId);
                        start(startParent);
                        monitor.succeeded(configurationId);

                        startedParents.put(configurationId, startParent);
                    }

                    //  start the new configuration
                    monitor.starting(newConfigurationId);
                    start(newConfiguration);
                    monitor.succeeded(newConfigurationId);
                }

                //
                // update the results
                //
                results.setLoaded(loadedParents.keySet());
                results.addLoaded(newConfigurationId);
                if (started.contains(existingConfigurationId)) {
                    results.setStarted(startedParents.keySet());
                    results.addStarted(newConfigurationId);
                }

                //
                // update the model
                //

                // add all of the new configurations the model
                addNewConfigurationsToModel(loadedParents);

                // now ugrade the existing node in the model
                if (configurationModel.containsConfiguration(existingConfigurationId)) {
                    configurationModel.upgradeConfiguration(existingConfigurationId,
                            newConfigurationId,
                            getConfigurationIds(getLoadParents(newConfiguration)),
                            getConfigurationIds(getStartParents(newConfiguration)));
                } else {
                    configurationModel.addConfiguation(newConfigurationId,
                            getConfigurationIds(getLoadParents(newConfiguration)),
                            getConfigurationIds(getStartParents(newConfiguration)));
                    load(newConfigurationId);
                }

                // replace the configuraiton in he configurations map
                configurations.remove(existingConfiguration);
                configurations.put(newConfigurationId, newConfiguration);

                // migrate the configuration settings
                migrateConfiguration(existingConfigurationId, newConfigurationId, newConfiguration, started.contains(existingConfigurationId));
            } catch (Exception e) {
                monitor.failed(configurationId, e);
                results.addFailed(configurationId, e);

                //
                // stop and unload all configurations that were actually loaded
                //
                for (Iterator iterator = startedParents.values().iterator(); iterator.hasNext();) {
                    Configuration configuration = (Configuration) iterator.next();
                    stop(configuration);
                }
                for (Iterator iterator = loadedParents.values().iterator(); iterator.hasNext();) {
                    Configuration configuration = (Configuration) iterator.next();
                    unload(configuration);
                }

                // stop and unload the newConfiguration
                if (newConfiguration != null) {
                    stop(newConfiguration);
                    unload(newConfiguration);
                }

                //
                // atempt to reinstate the old configuation
                //
                Configuration configuration = null;
                try {
                    configuration = load(existingUnloadedConfiguration.getConfigurationData(),
                            existingUnloadedConfiguration.getResolvedParentIds(),
                            Collections.EMPTY_MAP);
                    reloadingConfiguration = configuration;
                    // if the configuration was started before restart it
                    if (started.contains(existingConfigurationId)) {
                        start(configuration);
                        results.addStarted(existingConfigurationId);
                    }

                    // don't mark as loded until start completes as it may thorw an exception
                    results.addLoaded(existingConfigurationId);

                    configurations.put(existingConfigurationId, configuration);

                    reinstatedExisting = true;
                } catch (Exception ignored) {
                    monitor.failed(existingConfigurationId, e);

                    // we tried our best
                    if (configuration != null) {
                        unload(configuration);
                    }

                    //
                    // cleanup the model
                    //
                    for (Iterator iterator = results.getUnloaded().iterator(); iterator.hasNext();) {
                        Artifact childId = (Artifact) iterator.next();
                        configurationModel.unload(childId);
                        removeConfigurationFromModel(childId);
                    }

                    throw new LifecycleException("reload", newConfigurationId, results);
                }
            } finally {
                reloadingConfiguration = null;
            }
        }

        //
        // reload as many child configurations as possible
        //
        Set skip = new HashSet();
        for (Iterator iterator = reloadChildren.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Artifact configurationId = (Artifact) entry.getKey();
            UnloadedConfiguration unloadedConfiguration = (UnloadedConfiguration) entry.getValue();

            // skip the configurations that have alredy failed or are children of failed configurations
            if (skip.contains(configurationId)) {
                continue;
            }

            // try to load the configuation
            Configuration configuration = null;
            try {
                // get the correct resolved parent ids based on if we are loading with the new config id or the existing one
                LinkedHashSet resolvedParentIds;
                if (!reinstatedExisting) {
                    resolvedParentIds = unloadedConfiguration.getResolvedParentIds();
                } else {
                    resolvedParentIds = (LinkedHashSet) existingParents.get(configurationId);
                }

                // if the resolved parent ids is null, then we are not supposed to reload this configuration
                if (resolvedParentIds != null) {
                    monitor.loading(configurationId);
                    configuration = load(unloadedConfiguration.getConfigurationData(), resolvedParentIds, Collections.EMPTY_MAP);
                    reloadingConfiguration = configuration;
                    monitor.succeeded(configurationId);

                    // if the configuration was started before restart it
                    if (started.contains(configurationId)) {
                        monitor.starting(configurationId);
                        start(configuration);
                        monitor.succeeded(configurationId);
                        results.addStarted(configurationId);
                    }

                    // don't mark as loded until start completes as it may thow an exception
                    results.addLoaded(configurationId);

                    configurations.put(configurationId, configuration);
                } else {
                    removeConfigurationFromModel(configurationId);
                }
            } catch (Exception e) {
                // the configuraiton failed to restart
                results.addFailed(configurationId, e);
                monitor.failed(configurationId, e);
                skip.add(configurationId);

                // unload the configuration if it was loaded and failed in start
                if (configuration != null) {
                    unload(configuration);
                }

                // officially unload the configuration in the model (without gc)
                LinkedHashSet unloadList = configurationModel.unload(configurationId, false);
                configurationModel.removeConfiguration(configurationId);

                // all of the configurations to be unloaded must be in our unloaded list, or the model is corrupt
                if (!reloadChildren.keySet().containsAll(unloadList)) {
                    throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                }

                // add the children of the failed configuration to the results as unloaded
                for (Iterator iterator1 = unloadList.iterator(); iterator1.hasNext();) {
                    Artifact failedId = (Artifact) iterator1.next();

                    // if any of the failed configuration are in the reloaded set, the model is
                    // corrupt because we loaded a child before a parent
                    if (results.wasLoaded(failedId)) {
                        throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                    }

                    skip.add(failedId);
                }
            } finally {
                reloadingConfiguration = null;
            }
        }

        //
        // If nothing failed, delete all the unloaded modules that weren't reloaded
        //
        if (!results.wasLoaded(existingConfigurationId) && !results.wasFailed(existingConfigurationId)) {
            try {
                uninstallConfiguration(existingConfigurationId);
            } catch (IOException e) {
                log.error("Unable to uninstall configuration " + existingConfigurationId, e);
            }
        }

        monitor.finished();
        if (results.wasFailed(newConfigurationId) || !results.wasLoaded(newConfigurationId)) {
            throw new LifecycleException("restart", newConfigurationId, results);
        }
        return results;
    }

    protected void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration, boolean running) throws NoSuchConfigException {
    }

    private static LinkedHashSet getResolvedParentIds(Configuration configuration) {
        LinkedHashSet resolvedParentIds = new LinkedHashSet();
        for (Iterator iterator = configuration.getClassParents().iterator(); iterator.hasNext();) {
            Configuration classParent = (Configuration) iterator.next();
            resolvedParentIds.add(classParent.getId());
        }
        for (Iterator iterator = configuration.getServiceParents().iterator(); iterator.hasNext();) {
            Configuration serviceParent = (Configuration) iterator.next();
            resolvedParentIds.add(serviceParent.getId());
        }
        for (Iterator iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration child = (Configuration) iterator.next();
            resolvedParentIds.addAll(getResolvedParentIds(child));
        }

        return resolvedParentIds;
    }

    public synchronized void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {
        if (!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configurationId + " is not fully resolved");
        }
        if (configurations.containsKey(configurationId)) {
            if (isRunning(configurationId)) {
                stopConfiguration(configurationId);
            }
            if (isLoaded((configurationId))) {
                unloadConfiguration(configurationId);
            }
        }

        uninstall(configurationId);
        List storeSnapshot = getStoreList();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configurationId)) {
                store.uninstall(configurationId);
            }
        }

        removeConfigurationFromModel(configurationId);
        notifyWatchers(configurationId);
    }

    protected void uninstall(Artifact configurationId) {
        //child class can override this method
    }

    private void notifyWatchers(Artifact id) {
        for (Iterator it = watchers.iterator(); it.hasNext();) {
            DeploymentWatcher watcher = (DeploymentWatcher) it.next();
            watcher.undeployed(id);
        }
    }

    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    /**
     * this configuration manager never starts configurations.
     *
     * @return false
     */
    public boolean isOnline() {
        return false;
    }

    public void setOnline(boolean online) {
    }

    protected List getStoreList() {
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SimpleConfigurationManager.class, "ConfigurationManager");
        infoFactory.addReference("Stores", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("ArtifactResolver", ArtifactResolver.class, "ArtifactResolver");
        infoFactory.addReference("Repositories", Repository.class, "Repository");
        infoFactory.addReference("Watchers", DeploymentWatcher.class);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"Stores", "ArtifactResolver", "Repositories", "Watchers"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
