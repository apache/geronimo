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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = "ConfigurationManager")
public class SimpleConfigurationManager implements ConfigurationManager {
    protected static final Logger log = LoggerFactory.getLogger(SimpleConfigurationManager.class);
    protected final Collection<ConfigurationStore> stores;
    private final ArtifactResolver artifactResolver;
    protected final Map<Artifact, ConfigurationData> loadedConfigurationData = new HashMap<Artifact, ConfigurationData>();
    protected final Map<Artifact, Configuration> configurations = new LinkedHashMap<Artifact, Configuration>();
    protected final Map<Artifact, Bundle> bundles = new LinkedHashMap<Artifact, Bundle>();
    protected final ConfigurationModel configurationModel;
    protected final Collection<? extends Repository> repositories;
    protected final Collection<DeploymentWatcher> watchers;
    protected final BundleContext bundleContext;

    //TODO need thread local of loaded configurations OSGI GROSS!!
    protected final ThreadLocal<Map<Artifact, Configuration>> loadedConfigurations = new ThreadLocal<Map<Artifact, Configuration>>() {
    };

    /**
     * When this is not null, it points to the "new" configuration that is
     * part of an in-process reload operation.  This configuration will
     * definitely be loaded, but might not be started yet.  It should never be
     * populated outside the scope of a reload operation.
     */
    private Configuration reloadingConfiguration;

    private Object reloadingConfigurationLock = new Object();

    public SimpleConfigurationManager(Collection<ConfigurationStore> stores, ArtifactResolver artifactResolver, Collection<? extends Repository> repositories, BundleContext bundleContext) {
        this(stores, artifactResolver, repositories, Collections.<DeploymentWatcher>emptySet(), bundleContext);
    }

    public SimpleConfigurationManager(@ParamReference(name = "Stores", namingType = "ConfigurationStore") Collection<ConfigurationStore> stores,
                                      @ParamReference(name = "ArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver artifactResolver,
                                      @ParamReference(name = "Repositories", namingType = "Repository") Collection<? extends Repository> repositories,
                                      @ParamReference(name = "Watchers") Collection<DeploymentWatcher> watchers,
                                      @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this(stores, artifactResolver, repositories, watchers, bundleContext, new ConfigurationModel());
    }

    public SimpleConfigurationManager(Collection<ConfigurationStore> stores, ArtifactResolver artifactResolver, Collection<? extends Repository> repositories, Collection<DeploymentWatcher> watchers, BundleContext bundleContext, ConfigurationModel configurationModel) {
        if (stores == null) stores = Collections.emptySet();
        if (repositories == null) repositories = Collections.emptySet();
        for (Repository repo : repositories) {
            if (repo == null) throw new NullPointerException("null repository");
        }
        if (watchers == null) watchers = Collections.emptySet();
        for (DeploymentWatcher watcher : watchers) {
            if (watcher == null) throw new NullPointerException("null DeploymentWatcher");
        }
        this.configurationModel = configurationModel;
        this.stores = stores;
        this.artifactResolver = artifactResolver;
        this.repositories = repositories;
        this.watchers = watchers;
        this.bundleContext = bundleContext;
    }

    public ConfigurationModel getConfigurationModel() {
        return configurationModel;
    }

    @Override
    public synchronized boolean isInstalled(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        for (ConfigurationStore store : getStoreList()) {
            if (store.containsConfiguration(configId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLoaded(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        synchronized (reloadingConfigurationLock) {
            if (reloadingConfiguration != null && reloadingConfiguration.getId().equals(configId)) {
                return true;
            }
        }
        synchronized (this) {
            return configurationModel.isLoaded(configId);
        }
    }

    @Override
    public synchronized boolean isRunning(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        return configurationModel.isStarted(configId);
    }

    @Override
    public Artifact[] getInstalled(Artifact query) {
        Artifact[] all = artifactResolver.queryArtifacts(query);
        List<Artifact> configs = new ArrayList<Artifact>();
        for (Artifact artifact : all) {
            if (isConfiguration(artifact)) {
                configs.add(artifact);
            }
        }
        if (configs.size() == all.length) {
            return all;
        }
        return configs.toArray(new Artifact[configs.size()]);
    }

    @Override
    public Artifact[] getLoaded(Artifact query) {
        return configurationModel.getLoaded(query);
    }

    @Override
    public Artifact[] getRunning(Artifact query) {
        return configurationModel.getStarted(query);
    }

    @Override
    public List<AbstractName> listStores() {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        List<AbstractName> result = new ArrayList<AbstractName>(storeSnapshot.size());
        for (ConfigurationStore store : storeSnapshot) {
            result.add(store.getAbstractName());
        }
        return result;
    }

    @Override
    public ConfigurationStore[] getStores() {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        return storeSnapshot.toArray(new ConfigurationStore[storeSnapshot.size()]);
    }

    @Override
    public Collection<? extends Repository> getRepositories() {
        return repositories;
    }

    @Override
    public List<ConfigurationInfo> listConfigurations() {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        List<ConfigurationInfo> list = new ArrayList<ConfigurationInfo>();
        for (ConfigurationStore store : storeSnapshot) {
            list.addAll(listConfigurations(store));
        }
        return list;
    }

    @Override
    public ConfigurationStore getStoreForConfiguration(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        List<ConfigurationStore> storeSnapshot = getStoreList();
        for (ConfigurationStore store : storeSnapshot) {
            if (store.containsConfiguration(configId)) {
                return store;
            }
        }
        return null;
    }

    @Override
    public List<ConfigurationInfo> listConfigurations(AbstractName storeName) throws NoSuchStoreException {
        for (ConfigurationStore store : getStoreList()) {
            if (storeName.equals(store.getAbstractName())) {
                return listConfigurations(store);
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    private List<ConfigurationInfo> listConfigurations(ConfigurationStore store) {
        List<ConfigurationInfo> list = store.listConfigurations();
        for (ListIterator<ConfigurationInfo> iterator = list.listIterator(); iterator.hasNext();) {
            ConfigurationInfo configurationInfo = iterator.next();
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

    @Override
    public boolean isConfiguration(Artifact artifact) {
        if (!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact " + artifact + " is not fully resolved");
        }
        synchronized (configurations) {
            // if it is loaded, it is definitely a configuration
            if (getLoadedConfigurationData(artifact) != null) {
                return true;
            }
        }

        // see if any stores think it is a configuration
        for (ConfigurationStore store : getStoreList()) {
            if (store.containsConfiguration(artifact)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Configuration getConfiguration(Artifact configurationId) {
        if (!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configurationId + " is not fully resolved");
        }
        synchronized (reloadingConfigurationLock) {
            if (reloadingConfiguration != null && reloadingConfiguration.getId().equals(configurationId)) {
                return reloadingConfiguration;
            }
        }
        synchronized (configurations) {
            return configurations.get(configurationId);
        }
    }

    @Override
    public Bundle getBundle(Artifact id) {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        return bundles.get(id);
    }

    @Override
    public synchronized LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return loadConfiguration(configurationId, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configurationId + " is not fully resolved");
        }
        if (isLoaded(configurationId)) {
            // already loaded, so just mark the configuration as user loaded
            loadConfigurationModel(configurationId);
            monitor.finished();
            return new LifecycleResults();
        }

        // load the ConfigurationData for the new configuration
        try {
            String location = locateBundle(configurationId, monitor);
            Bundle bundle = bundleContext.installBundle(location);
            if(!BundleUtils.isResolved(bundle)) {
                BundleUtils.resolve(bundle);
            }
            //loadConfiguration(ConfigurationData) should be successfully invoked in the same thread by the ConfigurationExtender
        } catch (Exception e) {
            throw new LifecycleException("load", configurationId, e);
        } finally {
            monitor.finished();
        }

        LifecycleResults results = new LifecycleResults();
        synchronized (configurations) {
        if (!loadedConfigurationData.containsKey(configurationId)) {
            addConfigurationModel(configurationId, Collections.<Artifact> emptySet(), Collections.<Artifact> emptySet());
        }

        }
        loadConfigurationModel(configurationId);
        return results;
    }

    @Override
    public synchronized LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return loadConfiguration(configurationData, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        Artifact id = configurationData.getId();
        LifecycleResults results = new LifecycleResults();
        if (!isLoaded(id)) {
            try {
                //TODO Just check whether we could resolve all the parents, and still need to invoke this while starting the configuration
                resolveParentIds(configurationData);
                loadedConfigurationData.put(id, configurationData);
                bundles.put(id, configurationData.getBundle());
                addConfigurationModel(id);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("load", id, e);
            }

        }
        loadConfigurationModel(id);
        monitor.finished();
        return results;
    }

    protected void loadConfigurationModel(Artifact configurationId) throws NoSuchConfigException {
        configurationModel.load(configurationId);
    }

    protected Configuration start(ConfigurationData configurationData, Set<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        try {
            ConfigurationResolver configurationResolver = newConfigurationResolver(configurationData);
            return doStart(configurationData, resolvedParentIds, loadedConfigurations, configurationResolver);
        } catch (Exception e) {
            throw new InvalidConfigException("Error starting configuration gbean " + configurationData.getId(), e);
        }
    }

    @Override
    public ConfigurationResolver newConfigurationResolver(ConfigurationData configurationData) {
        return new ConfigurationResolver(configurationData, repositories, artifactResolver);
    }

    protected Configuration doStart(ConfigurationData configurationData, Set<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations, ConfigurationResolver configurationResolver) throws Exception {
        DependencyNode dependencyNode = buildDependencyNode(configurationData);
        List<Configuration> allServiceParents = buildAllServiceParents(loadedConfigurations, dependencyNode);
        Configuration configuration = new Configuration(configurationData, dependencyNode, allServiceParents, null, configurationResolver, this);
        configuration.doStart();
        //TODO why???
        resolvedParentIds.add(configuration.getId());
        Map<Artifact, Configuration> moreLoadedConfigurations = loadedConfigurations;//new LinkedHashMap<Artifact, Configuration>(loadedConfigurations);
        moreLoadedConfigurations.put(dependencyNode.getId(), configuration);
        for (Map.Entry<String, ConfigurationData> childEntry : configurationData.getChildConfigurations().entrySet()) {
            ConfigurationResolver childResolver = configurationResolver.createChildResolver(childEntry.getKey());
            Configuration child = doStart(childEntry.getValue(), resolvedParentIds, moreLoadedConfigurations, childResolver);
            configuration.addChild(child);
        }
        return configuration;
    }

    protected List<Configuration> buildAllServiceParents(Map<Artifact, Configuration> loadedConfigurations, DependencyNode dependencyNode) throws InvalidConfigException {
        List<Configuration> allServiceParents = new ArrayList<Configuration>();
        for (Artifact parentId : dependencyNode.getServiceParents()) {
            addDepthFirstServiceParents(parentId, allServiceParents, new HashSet<Artifact>(), loadedConfigurations);
        }
        return allServiceParents;
    }

    /**
     * Return a DependencyNode instance which contains all its class and service parents, including those of its child configurations, but it does not contain those from parents
     * @param configurationData
     * @return
     * @throws MissingDependencyException
     */
    protected DependencyNode buildDependencyNode(ConfigurationData configurationData) throws MissingDependencyException {
        return DependencyNodeUtil.toDependencyNode(configurationData, artifactResolver, this);
    }

    private void addDepthFirstServiceParents(Artifact id, List<Configuration> ancestors, Set<Artifact> ids, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        if (!ids.contains(id)) {
            Configuration configuration = getConfiguration(id, loadedConfigurations);
            ancestors.add(configuration);
            ids.add(id);
            for (Artifact parentId : configuration.getDependencyNode().getServiceParents()) {
                addDepthFirstServiceParents(parentId, ancestors, ids, loadedConfigurations);
            }
        }
    }

//    private Collection<Configuration> findParentConfigurations(LinkedHashSet<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
//        LinkedHashMap<Artifact, Configuration> parents = new LinkedHashMap<Artifact, Configuration>();
//        for (Artifact resolvedArtifact : resolvedParentIds) {
//
//            Configuration parent = getConfiguration(resolvedArtifact, loadedConfigurations);
//
//            parents.put(resolvedArtifact, parent);
//        }
//        return parents.values();
//    }

    private Configuration getConfiguration(Artifact resolvedArtifact, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        Configuration parent;

        //TODO OSGi track loaded configurations in thread local ???
        if (loadedConfigurations.containsKey(resolvedArtifact)) {
            parent = loadedConfigurations.get(resolvedArtifact);
        } else if (isLoaded(resolvedArtifact)) {
            parent = getConfiguration(resolvedArtifact);
        } else {
            throw new InvalidConfigException("Cound not find parent configuration: " + resolvedArtifact);
        }
        return parent;
    }

    protected void addConfigurationModel(Artifact id) throws NoSuchConfigException, MissingDependencyException {
        ConfigurationData configurationData = getLoadedConfigurationData(id);
        if (configurationData == null) {
            throw new NoSuchConfigException(id, "Should be load the configurationData first");
        }
        DependencyNode node = buildDependencyNode(configurationData);
        addConfigurationModel(id, node.getClassParents(), node.getServiceParents());
    }

    protected void addConfigurationModel(Artifact id, Set<Artifact> loadParentsIds, Set<Artifact> startParentsIds) throws NoSuchConfigException {
        configurationModel.addConfiguration(id, loadParentsIds, startParentsIds);
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

    /*
     * Return ids that can be loaded in sorted order.  Remove loadable ids from source set.
     */
    @Override
    public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
        LinkedHashSet<Artifact> sorted = new LinkedHashSet<Artifact>();
        sort(ids, sorted, monitor);
        sorted.retainAll(ids);
        ids.removeAll(sorted);
        return sorted;
    }

    private void sort(Collection<Artifact> ids, LinkedHashSet<Artifact> sorted, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
        for (Artifact id : ids) {
            if (!sorted.contains(id)) {
                try {
                    //configuration may not be loadable yet, the config-store may not be available to load from
                    ConfigurationData data = loadConfigurationData(id, monitor);
                    LinkedHashSet<Artifact> parents = resolveParentIds(data);
                    sort(parents, sorted, monitor);
                    sorted.add(id);
                } catch (NoSuchConfigException e) {
                    //ignore
                } catch (IOException e) {
                    //ignore
                } catch (InvalidConfigException e) {
                    //ignore
                } catch (MissingDependencyException e) {
                    //ignore
                }
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

    private String locateBundle(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (System.getProperty("geronimo.build.car") == null) {
            return "mvn:" + configurationId.getGroupId() + "/" + configurationId.getArtifactId() + "/" + configurationId.getVersion() + "/" + configurationId.getType();
        }
        monitor.addConfiguration(configurationId);
        monitor.reading(configurationId);
        for (Repository repo : repositories) {
            if (repo.contains(configurationId)) {
                return "reference:file://" + repo.getLocation(configurationId).getAbsolutePath();
            }
        }
        NoSuchConfigException exception = new NoSuchConfigException(configurationId);
        monitor.failed(configurationId, exception);
        throw exception;
    }

    @Override
    public LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        Environment environment = configurationData.getEnvironment();

        LinkedHashSet<Artifact> parentIds = new LinkedHashSet<Artifact>();
        List<Dependency> dependencies = new ArrayList<Dependency>(environment.getDependencies());
        for (ListIterator<Dependency> iterator = dependencies.listIterator(); iterator.hasNext();) {
            Dependency dependency = iterator.next();
            Artifact resolvedArtifact = artifactResolver.resolveInClassLoader(dependency.getArtifact());
//            if (isConfiguration(resolvedArtifact)) {
            parentIds.add(resolvedArtifact);

            // update the dependency list to contain the resolved artifact
            dependency = new Dependency(resolvedArtifact, dependency.getImportType());
            iterator.set(dependency);
//            } else if (dependency.getImportType() == ImportType.SERVICES) {
//                 Service depdendencies require that the depdencency be a configuration
//                throw new InvalidConfigException("Dependency does not have services: " + resolvedArtifact);
//            }
        }

        for (ConfigurationData childConfigurationData : configurationData.getChildConfigurations().values()) {
            LinkedHashSet<Artifact> childParentIds = resolveParentIds(childConfigurationData);
            // remove this configuration's id from the parent Ids since it will cause an infinite loop
            childParentIds.remove(configurationData.getId());
            parentIds.addAll(childParentIds);
        }
        return parentIds;
    }

    private static class UnloadedConfiguration {
        private final ConfigurationData configurationData;
        private final LinkedHashSet<Artifact> resolvedParentIds;

        public UnloadedConfiguration(ConfigurationData configurationData, LinkedHashSet<Artifact> resolvedParentIds) {
            this.configurationData = configurationData;
            this.resolvedParentIds = resolvedParentIds;
        }

        public ConfigurationData getConfigurationData() {
            return configurationData;
        }

        public LinkedHashSet<Artifact> getResolvedParentIds() {
            return resolvedParentIds;
        }
    }

    @Override
    public synchronized LifecycleResults startConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return startConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults startConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            monitor.finished();
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }

        ConfigurationData configurationData = getLoadedConfigurationData(id);
        if (configurationData == null) {
            monitor.finished();
            throw new LifecycleException("start", id, new Throwable());
        }

        Map<Artifact, Configuration> actuallyLoaded = loadedConfigurations.get();
        boolean newLoad = actuallyLoaded == null;

        LifecycleResults results = new LifecycleResults();
        List<Bundle> unstartedBundles = new LinkedList<Bundle>();
        try {
            if (actuallyLoaded == null) {
                actuallyLoaded = new LinkedHashMap<Artifact, Configuration>();
                loadedConfigurations.set(actuallyLoaded);
            }

            LinkedHashSet<Artifact> unstartedConfigurationIds = configurationModel.start(id);
            addConfigurationsToMonitor(monitor, unstartedConfigurationIds);
            for (Artifact unstartedConfigurationId : unstartedConfigurationIds) {
                //Step 1. Start Bundle
                Bundle bundle = getBundle(unstartedConfigurationId);

                if (BundleUtils.canStart(bundle)) {
                    try {
                        bundle.start(Bundle.START_TRANSIENT);
                        unstartedBundles.add(bundle);
                    } catch (Exception e) {
                        monitor.finished();
                        throw new LifecycleException("start", id, e);
                    }
                }

                //Step 2. Start Configuration
                monitor.starting(unstartedConfigurationId);
                ConfigurationData unstartedConfigurationData = getLoadedConfigurationData(unstartedConfigurationId);
                Set<Artifact> parentArtifacts = resolveParentIds(unstartedConfigurationData);
                Configuration configuration = start(unstartedConfigurationData, parentArtifacts, actuallyLoaded);
                actuallyLoaded.put(unstartedConfigurationId, configuration);

                //configurationModel.start(configuration.getId());
                configurations.put(unstartedConfigurationId, configuration);
                startInternal(configuration);
                monitor.succeeded(unstartedConfigurationId);
                results.addStarted(unstartedConfigurationId);
            }
            return results;
        } catch (Exception e) {
            monitor.failed(id, e);
            configurationModel.stop(id);

            for (Artifact started : results.getStarted()) {
                Configuration stopConfiguration = getConfiguration(started);
                monitor.stopping(started);
                stopInternal(stopConfiguration);
                monitor.succeeded(started);
            }

            for (Bundle bundle : unstartedBundles) {
                if (BundleUtils.canStop(bundle)) {
                    try {
                        bundle.stop();
                    } catch (Exception e1) {
                    }
                }
            }

            throw new LifecycleException("start", id, e);
        } finally {
            if (newLoad) {
                loadedConfigurations.remove();
            }
            monitor.finished();
        }
    }

    protected Set<Artifact> getParentArtifacts(ConfigurationData configurationData) {
        LinkedHashSet<Artifact> parentIds = new LinkedHashSet<Artifact>();
        for (Dependency dependency : configurationData.getEnvironment().getDependencies()) {
            parentIds.add(dependency.getArtifact());
        }

        for (ConfigurationData childConfigurationData : configurationData.getChildConfigurations().values()) {
            Set<Artifact> childParentIds = getParentArtifacts(childConfigurationData);
            // remove this configuration's id from the parent Ids since it will cause an infinite loop
            childParentIds.remove(configurationData.getId());
            parentIds.addAll(childParentIds);
        }
        return parentIds;
    }

    protected void startInternal(Configuration configuration) throws Exception {
    }

    @Override
    public synchronized LifecycleResults stopConfiguration(Artifact id) throws NoSuchConfigException {
        return stopConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults stopConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }

        LinkedHashSet<Artifact> stopList = configurationModel.stop(id);
        addConfigurationsToMonitor(monitor, stopList);

        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : stopList) {
            //Step 1. Stop Configuration
            Configuration configuration = getConfiguration(configurationId);
            monitor.stopping(configurationId);
            stopInternal(configuration);
            monitor.succeeded(configurationId);
            configurations.remove(configuration.getId());
            results.addStopped(configurationId);

            //Step 2. Stop Bundle
            Bundle bundle = getBundle(configurationId);
            if (bundle != null && BundleUtils.canStop(bundle)) {
                try {
                    bundle.stop(Bundle.STOP_TRANSIENT);
                } catch (BundleException e) {
                    //Only log some error messages here, no need to throw a LifecycleException here.
                    log.error("fail to stop the bundle" + bundle.getLocation(), e);
                }
            }
        }

        monitor.finished();
        return results;
    }

    protected void stopInternal(Configuration configuration) {
        try {
            configuration.doStop();
        } catch (Exception e) {
            log.error("fail to stop configuration " + configuration.getId(), e);
        }
        configurations.remove(configuration.getId());
    }

    @Override
    public synchronized LifecycleResults restartConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return restartConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults restartConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        // get a sorted list of configurations to restart
        LinkedHashSet<Artifact> restartList = configurationModel.restart(id);

        addConfigurationsToMonitor(monitor, restartList);

        // stop the configuations
        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : restartList) {
            //Step 1. Stop Configuration
            Configuration configuration = getConfiguration(configurationId);
            monitor.stopping(configurationId);
            stopInternal(configuration);
            monitor.succeeded(configurationId);

            //Step 2. Stop Bundle
            Bundle bundle = getBundle(configurationId);
            if (bundle != null && BundleUtils.canStop(bundle)) {
                try {
                    bundle.stop(Bundle.STOP_TRANSIENT);
                } catch (BundleException e) {
                    //Only log some error messages here, no need to throw a LifecycleException here.
                    log.error("fail to stop the bundle" + bundle.getLocation(), e);
                }
            }

            results.addStopped(configurationId);
        }

        // reverse the list
        restartList = reverse(restartList);

        // restart the configurations
        Set<Artifact> skip = new HashSet<Artifact>();
        Map<Artifact, Configuration> actuallyLoaded = new LinkedHashMap<Artifact, Configuration>(restartList.size());
        for (Artifact configurationId : restartList) {

            // skip the configurations that have already failed or are children of failed configurations
            if (skip.contains(configurationId)) {
                continue;
            }

            // try to start the configuation
            try {
                //Step 1. Start Bundle
                Bundle bundle = getBundle(configurationId);

                if (BundleUtils.canStart(bundle)) {
                    try {
                        bundle.start(Bundle.START_TRANSIENT);
                    } catch (Exception e) {
                        monitor.finished();
                        throw new LifecycleException("start", id, e);
                    }
                }

                //Step 2. Start Configuration
                monitor.starting(configurationId);
                ConfigurationData configurationData = getLoadedConfigurationData(configurationId);
                Set<Artifact> parentArtifacts = resolveParentIds(configurationData);
                Configuration configuration = start(configurationData, parentArtifacts, actuallyLoaded);
                actuallyLoaded.put(configurationId, configuration);
                startInternal(configuration);
                configurations.put(configurationId, configuration);
                monitor.succeeded(configurationId);
                results.addStarted(configurationId);
            } catch (Exception e) {
                // the configuraiton failed to restart
                results.addFailed(configurationId, e);
                monitor.failed(configurationId, e);
                skip.add(configurationId);

                // officially stop the configuration in the model (without gc)
                LinkedHashSet<Artifact> stopList = configurationModel.stop(configurationId, false);

                // all of the configurations to be stopped must be in our restart list, or the model is corrupt
                if (!restartList.containsAll(stopList)) {
                    throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                }

                // add the children of the failed configuration to the results as stopped
                for (Artifact failedId : stopList) {

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

    @Override
    public synchronized LifecycleResults unloadConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return unloadConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults unloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        Set<Artifact> started = configurationModel.getStarted();
        LinkedHashSet<Artifact> unloadList = configurationModel.unload(id);

        addConfigurationsToMonitor(monitor, unloadList);

        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : unloadList) {
            Configuration configuration = getConfiguration(configurationId);

            // first make sure it is stopped
            if (configuration != null) {
                if (started.contains(configurationId)) {
                    monitor.stopping(configurationId);
                    stopInternal(configuration);
                    monitor.succeeded(configurationId);
                    results.addStopped(configurationId);
                } else {
                    // call stop just to be sure the beans aren't running
                    stopInternal(configuration);
                }
            }
            // now unload it
            monitor.unloading(configurationId);
            unloadInternal(configurationId);
            monitor.succeeded(configurationId);
            results.addUnloaded(configurationId);

            // clean up the model
            removeConfigurationModel(configurationId);
            // remove from the loadedConfigurationData map
            loadedConfigurationData.remove(id);
            Bundle bundle = bundles.remove(configurationId);
            if (bundle != null) {
                try {
                    if (BundleUtils.canStop(bundle)) {
                        bundle.stop(Bundle.STOP_TRANSIENT);
                    }
                    if (BundleUtils.canUninstall(bundle)) {
                        bundle.uninstall();
                   }
                } catch (BundleException e) {
                    monitor.finished();
                    throw new LifecycleException("unload", configurationId, e);
                }
            }
        }
        monitor.finished();
        return results;
    }

    protected void removeConfigurationModel(Artifact configurationId) throws NoSuchConfigException {
        if (configurationModel.containsConfiguration(configurationId)) {
            configurationModel.removeConfiguration(configurationId);
        }
    }

    protected void unloadInternal(Artifact configurationId) {
    }

    @Override
    public synchronized LifecycleResults reloadConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return reloadConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults reloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return reloadConfiguration(id, id.getVersion(), monitor);
    }

    @Override
    public synchronized LifecycleResults reloadConfiguration(Artifact id, Version version) throws NoSuchConfigException, LifecycleException {
        return reloadConfiguration(id, version, NullLifecycleMonitor.INSTANCE);
    }

    @Override
    public synchronized LifecycleResults reloadConfiguration(Artifact id, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        Configuration configuration = getConfiguration(id);
        if (configuration == null) { // The configuration to reload is not currently loaded
            ConfigurationData data = null;
            List<ConfigurationStore> storeSnapshot = getStoreList();
            for (ConfigurationStore store : storeSnapshot) {
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
            UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(data, new LinkedHashSet<Artifact>());
            Artifact newId = new Artifact(id.getGroupId(), id.getArtifactId(), version, id.getType());
            ConfigurationData newData;
            try {
                newData = loadConfigurationData(newId, monitor);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("reload", id, e);
            }

            return reloadConfiguration(existingUnloadedConfiguration, newData, monitor);
        } else { // The configuration to reload is loaded
            ConfigurationData existingConfigurationData = getLoadedConfigurationData(configuration.getId());
            UnloadedConfiguration existingUnloadedConfiguration = new UnloadedConfiguration(existingConfigurationData, getResolvedParentIds(configuration));

            Artifact newId = new Artifact(id.getGroupId(), id.getArtifactId(), version, id.getType());

            // reload the ConfigurationData from a store
            ConfigurationData configurationData;
            try {
                configurationData = loadConfigurationData(newId, monitor);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("reload", id, e);
            }

            return reloadConfiguration(existingUnloadedConfiguration, configurationData, monitor);
        }
    }

    @Override
    public synchronized LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws LifecycleException, NoSuchConfigException {
        return reloadConfiguration(configurationData, NullLifecycleMonitor.INSTANCE);
    }

    @Override
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
        for (Dependency dependency : configurationData.getEnvironment().getDependencies()) {
            Artifact artifact = dependency.getArtifact();
            if (artifact.getVersion() != null && artifact.matches(configurationId)) {
                return true;
            }
        }

        for (ConfigurationData childConfigurationData : configurationData.getChildConfigurations().values()) {
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
        /*LinkedHashMap<Artifact, UnloadedConfiguration> newConfigurations = new LinkedHashMap<Artifact, UnloadedConfiguration>();
        try {
            loadDepthFirst(newConfigurationData, newConfigurations, monitor);
        } catch (Exception e) {
            monitor.finished();
            throw new LifecycleException("reload", newConfigurationId, e);
        }*/

        //
        // get a list of the started configuration, so we can restart them later
        //
        Set<Artifact> started = configurationModel.getStarted();

        //
        // get a list of the child configurations that will need to reload
        //
        //   note: we are iterating in reverse order
        LinkedHashMap<Artifact, LinkedHashSet<Artifact>> existingParents = new LinkedHashMap<Artifact, LinkedHashSet<Artifact>>();
        LinkedHashSet<Artifact> reloadChildren = configurationModel.reload(existingConfigurationId);
        //Remove myself from the children configuration list
        reloadChildren.remove(existingConfigurationId);

        for (Artifact configurationId : reverse(reloadChildren)) {

            // if new configurations contains the child something we have a circular dependency
            /*if (newConfigurations.containsKey(configurationId)) {
                throw new LifecycleException("reload", newConfigurationId,
                        new IllegalStateException("Circular depenency between " + newConfigurationId + " and " + configurationId));
            }*/

            Configuration configuration = getConfiguration(configurationId);

            // save off the exising resolved parent ids in case we need to restore this configuration
            LinkedHashSet<Artifact> existingParentIds = getResolvedParentIds(configuration);
            existingParents.put(configurationId, existingParentIds);

            // check that the child doen't have a hard dependency on the old configuration
            LinkedHashSet<Artifact> resolvedParentIds;
            if (hasHardDependency(existingConfigurationId, configuration.getConfigurationData())) {
                if (force) {
                    throw new LifecycleException("reload", newConfigurationId,
                            new IllegalStateException("Existing configuration " + configurationId + " has a hard dependency on the current version of this configuration " + existingConfigurationId));
                }

                // we leave the resolved parent ids null to signal that we should not reload the configuration
                resolvedParentIds = null;
            } else {
                resolvedParentIds = new LinkedHashSet<Artifact>(existingParentIds);
                resolvedParentIds.remove(existingConfigurationId);
                resolvedParentIds.add(newConfigurationId);
            }
            monitor.addConfiguration(configurationId);
        }

        //
        // unload the children
        //

        // note: we are iterating in reverse order
        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : reloadChildren) {
            Configuration configuration = getConfiguration(configurationId);

            //Step 1. Stop Configuration
            if (configuration != null) {
                // first make sure it is stopped
                if (started.contains(configurationId)) {
                    monitor.stopping(configurationId);
                    stopInternal(configuration);
                    monitor.succeeded(configurationId);
                    results.addStopped(configurationId);
                } else {
                    // call stop just to be sure the beans aren't running
                    stopInternal(configuration);
                }
                configurationModel.stop(configurationId);
            }

            //Step 2. Stop Bundle
            Bundle bundle = getBundle(configurationId);
            if (bundle != null && BundleUtils.canStop(bundle)) {
                try {
                    bundle.stop(Bundle.STOP_TRANSIENT);
                } catch (BundleException e) {
                    //Only log some error messages here, no need to throw a LifecycleException here.
                    log.error("fail to stop the bundle" + bundle.getLocation(), e);
                }
            }

            // now unload it
            //Step 3. Unload the Configuraiton
            monitor.unloading(configurationId);
            unloadInternal(configurationId);
            monitor.succeeded(configurationId);
            results.addUnloaded(configurationId);
            //remove from the loadedConfigurationData map
            loadedConfigurationData.remove(configurationId);
            configurationModel.unload(configurationId);

            //Step 4. Uninstall the bundle
            if (bundle != null) {
                try {
                    if (BundleUtils.canUninstall(bundle)) {
                        bundle.uninstall();
                    }
                } catch (BundleException e) {
                    throw new LifecycleException("reload", configurationId, e);
                }
            }
        }

        {
            // unload the existing config
            //Step 1. Stop the existing Configuration
            Configuration existingConfiguration = getConfiguration(existingConfigurationId);
            if (started.contains(existingConfigurationId)) {
                monitor.stopping(existingConfigurationId);
                stopInternal(existingConfiguration);
                monitor.succeeded(existingConfigurationId);
                results.addStopped(existingConfigurationId);
            } else if (existingConfiguration != null) {
                // call stop just to be sure the beans aren't running
                stopInternal(existingConfiguration);
            }
            configurationModel.stop(existingConfigurationId);
            //Step 2. Stop Bundle
            Bundle bundle = getBundle(existingConfigurationId);
            if (bundle != null && BundleUtils.canStop(bundle)) {
                try {
                    bundle.stop(Bundle.STOP_TRANSIENT);
                } catch (BundleException e) {
                    //Only log some error messages here, no need to throw a LifecycleException here.
                    log.error("fail to stop the bundle" + bundle.getLocation(), e);
                }
            }
            //Step 3. Unload the existing configuration
            if (existingConfiguration != null) {
                monitor.unloading(existingConfigurationId);
                unloadInternal(existingConfigurationId);
                monitor.succeeded(existingConfigurationId);
                results.addUnloaded(existingConfigurationId);

                configurationModel.unload(existingConfigurationId);
            }
            //Step 4. Uninstall the bundle
            if (bundle != null) {
                try {
                    if (BundleUtils.canUninstall(bundle)) {
                        bundle.uninstall();
                    }
                } catch (BundleException e) {
                    throw new LifecycleException("reload", existingConfigurationId, e);
                }
            }
        }
        //
        // load the new configurations
        //
        boolean reinstatedExisting = false;
        /* reduce variable scope */
        {
            Set<Artifact> loadedParents = new LinkedHashSet<Artifact>();
            Set<Artifact> startedParents = new LinkedHashSet<Artifact>();
            Configuration newConfiguration = null;
            Artifact configurationId = null;
            try {
                // Step 1. Load the parents of new configuration ( Due to the DependencyManager, if the new ConfigurationData is ready, all its parents should also be loaded
                LifecycleResults loadLifecycleResults = loadConfiguration(newConfigurationData, monitor);
                mergeLifecycleResults(loadLifecycleResults, results);

                // Step 2. Start the new configurations if the old one was running
                if (started.contains(existingConfigurationId)) {
                    LifecycleResults startLifecycleResults = startConfiguration(newConfigurationId, monitor);
                    mergeLifecycleResults(startLifecycleResults, results);

                    newConfiguration = configurations.get(newConfigurationId);
                    //Update the results
                    loadedParents.addAll(startLifecycleResults.getLoaded());
                    results.setLoaded(startLifecycleResults.getLoaded());
                    results.addLoaded(newConfigurationId);
                    startedParents.addAll(startLifecycleResults.getStarted());
                    results.setStarted(startLifecycleResults.getStarted());
                    results.addStarted(newConfigurationId);
                }

                // add all of the new configurations the model
                //TODO OSGI TOTALLY BROKEN
//                addNewConfigurationsToModel(loadedParents);

                // now ugrade the existing node in the model
                DependencyNode dependencyNode = buildDependencyNode(newConfiguration.getConfigurationData());
                if (configurationModel.containsConfiguration(existingConfigurationId)) {
                    configurationModel.upgradeConfiguration(existingConfigurationId, newConfigurationId, dependencyNode.getClassParents(), dependencyNode.getServiceParents());
                } else {
                    configurationModel.addConfiguration(newConfigurationId, dependencyNode.getClassParents(), dependencyNode.getServiceParents());
                    loadConfigurationModel(newConfigurationId);
                }

                // migrate the configuration settings
                migrateConfiguration(existingConfigurationId, newConfigurationId, newConfiguration, started.contains(existingConfigurationId));
            } catch (Exception e) {
                monitor.failed(configurationId, e);
                results.addFailed(configurationId, e);

                //
                // stop and unload all configurations that were actually loaded
                //
                for (Artifact startParentId : startedParents) {
                    Configuration startParentConfiguration = configurations.get(startParentId);
                    if (startParentConfiguration != null) {
                        stopInternal(startParentConfiguration);
                    }
                }

                //TODO loadedParents should be always empty, as we always eagerly load all the parents
                for (Artifact loadedParentId : loadedParents) {
                    unloadInternal(loadedParentId);
                }

                // stop and unload the newConfiguration
                if (newConfiguration != null) {
                    stopInternal(newConfiguration);
                    unloadInternal(newConfiguration.getId());
                }

                //
                // atempt to reinstate the old configuation
                //
                Configuration configuration = null;
                try {
                    configuration = start(existingUnloadedConfiguration.getConfigurationData(),
                            existingUnloadedConfiguration.getResolvedParentIds(),
                            Collections.<Artifact, Configuration>emptyMap()
                    );
                    synchronized (reloadingConfigurationLock) {
                        reloadingConfiguration = configuration;
                    }
                    // if the configuration was started before restart it
                    if (started.contains(existingConfigurationId)) {
                        startInternal(configuration);
                        results.addStarted(existingConfigurationId);
                    }

                    // don't mark as loaded until start completes as it may throw an exception
                    results.addLoaded(existingConfigurationId);

                    configurations.put(existingConfigurationId, configuration);

                    reinstatedExisting = true;
                } catch (Exception ignored) {
                    monitor.failed(existingConfigurationId, e);

                    // we tried our best
                    if (configuration != null) {
                       unloadInternal(configuration.getId());
                    }

                    //
                    // cleanup the model
                    //
                    for (Artifact childId : results.getUnloaded()) {
                        configurationModel.unload(childId);
                        removeConfigurationModel(childId);
                    }

                    throw new LifecycleException("reload", newConfigurationId, results);
                }
            } finally {
                synchronized (reloadingConfigurationLock) {
                    reloadingConfiguration = null;
                }
            }
        }

        //
        // reload as many child configurations as possible
        //
        Set<Artifact> skip = new HashSet<Artifact>();
        for (Artifact configurationId : reverse(reloadChildren)) {

            // skip the configurations that have alredy failed or are children of failed configurations
            if (skip.contains(configurationId)) {
                continue;
            }

            // try to load the configuation
            Configuration configuration = null;
            try {
                // get the correct resolved parent ids based on if we are loading with the new config id or the existing one
                LinkedHashSet<Artifact> resolvedParentIds;
                /*if (reinstatedExisting) {
                    resolvedParentIds = existingParents.get(configurationId);
                } else {
                    resolvedParentIds = unloadedConfiguration.getResolvedParentIds();
                }*/
                LifecycleResults loadLifecycleResults = loadConfiguration(configurationId, monitor);
                mergeLifecycleResults(loadLifecycleResults, results);

                ConfigurationData childConfigurationData = getLoadedConfigurationData(configurationId);
                resolvedParentIds = resolveParentIds(childConfigurationData);
                // if the resolved parent ids is null, then we are not supposed to reload this configuration
                if (resolvedParentIds != null) {
                    monitor.loading(configurationId);
                    configuration = start(childConfigurationData, resolvedParentIds, Collections.<Artifact, Configuration> emptyMap());
                    synchronized (reloadingConfigurationLock) {
                        reloadingConfiguration = configuration;
                    }
                    monitor.succeeded(configurationId);

                    // if the configuration was started before restart it
                    if (started.contains(configurationId)) {
                        monitor.starting(configurationId);
                        startInternal(configuration);
                        monitor.succeeded(configurationId);
                        results.addStarted(configurationId);

                        configurationModel.start(configurationId);
                    }

                    // don't mark as loded until start completes as it may thow an exception
                    results.addLoaded(configurationId);

                    configurations.put(configurationId, configuration);
                } else {
                    removeConfigurationModel(configurationId);
                }
            } catch (Exception e) {
                // the configuraiton failed to restart
                results.addFailed(configurationId, e);
                monitor.failed(configurationId, e);
                skip.add(configurationId);

                // unload the configuration if it was loaded and failed in start
                if (configuration != null) {
                   unloadInternal(configuration.getId());
                }

                // officially unload the configuration in the model (without gc)
                LinkedHashSet<Artifact> unloadList = configurationModel.unload(configurationId, false);
                configurationModel.removeConfiguration(configurationId);

                // all of the configurations to be unloaded must be in our unloaded list, or the model is corrupt
               /* if (!reloadChildren.keySet().containsAll(unloadList)) {
                    throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                }*/

                // add the children of the failed configuration to the results as unloaded
                for (Artifact failedId : unloadList) {

                    // if any of the failed configuration are in the reloaded set, the model is
                    // corrupt because we loaded a child before a parent
                    if (results.wasLoaded(failedId)) {
                        throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                    }

                    skip.add(failedId);
                }
            } finally {
                synchronized (reloadingConfigurationLock) {
                    reloadingConfiguration = null;
                }
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

    protected void mergeLifecycleResults(LifecycleResults mergedLifecycleResults, LifecycleResults lifecycleResults) {
        for (Artifact loadedArtifact : mergedLifecycleResults.getLoaded()) {
            lifecycleResults.addLoaded(loadedArtifact);
        }
        for (Artifact startedArtifact : mergedLifecycleResults.getStarted()) {
            lifecycleResults.addStarted(startedArtifact);
        }
        for (Artifact stoppedArtifact : mergedLifecycleResults.getStopped()) {
            lifecycleResults.addStopped(stoppedArtifact);
        }
        for (Artifact unloadedArtifact : mergedLifecycleResults.getUnloaded()) {
            lifecycleResults.addUnloaded(unloadedArtifact);
        }
        for (Map.Entry<Artifact, Throwable> failedEntry : mergedLifecycleResults.getFailed().entrySet()) {
            lifecycleResults.addFailed(failedEntry.getKey(), failedEntry.getValue());
        }
    }

    private static LinkedHashSet<Artifact> getResolvedParentIds(Configuration configuration) {
        return configuration.getDependencyNode().getParents();
    }

    @Override
    public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException, LifecycleException {
        synchronized (this) {
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

            uninstallInternal(configurationId);

            for (ConfigurationStore store : getStoreList()) {
                if (store.containsConfiguration(configurationId)) {
                    store.uninstall(configurationId);
                }
            }
            removeConfigurationModel(configurationId);
        }
        notifyWatchers(configurationId);
    }

    protected void uninstallInternal(Artifact configurationId) {
        //child class can override this method
    }

    @Override
    public synchronized ConfigurationData getLoadedConfigurationData(Artifact configurationId) {
        if (!configurationId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configurationId + " is not fully resolved");
        }
        return loadedConfigurationData.get(configurationId);
    }

    private void notifyWatchers(Artifact id) {
        for (DeploymentWatcher watcher : watchers) {
            watcher.undeployed(id);
        }
    }

    @Override
    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    /**
     * this configuration manager never starts configurations.
     *
     * @return false
     */
    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void setOnline(boolean online) {
    }

    protected List<ConfigurationStore> getStoreList() {
        return new ArrayList<ConfigurationStore>(stores);
    }

    private void addConfigurationsToMonitor(LifecycleMonitor monitor, LinkedHashSet<Artifact> configurations) {
        for (Artifact configurationId : configurations) {
            monitor.addConfiguration(configurationId);
        }
    }

    private static LinkedHashSet<Artifact> reverse(LinkedHashSet<Artifact> set) {
        ArrayList<Artifact> reverseList = new ArrayList<Artifact>(set);
        Collections.reverse(reverseList);
        set = new LinkedHashSet<Artifact>(reverseList);
        return set;
    }

    private static LinkedHashMap<Artifact, UnloadedConfiguration> reverse(LinkedHashMap<Artifact, UnloadedConfiguration> map) {
        ArrayList<Map.Entry<Artifact, UnloadedConfiguration>> reverseEntrySet = new ArrayList<Map.Entry<Artifact, UnloadedConfiguration>>(map.entrySet());
        Collections.reverse(reverseEntrySet);

        map = new LinkedHashMap<Artifact, UnloadedConfiguration>(reverseEntrySet.size());
        for (Map.Entry<Artifact, UnloadedConfiguration> entry : reverseEntrySet) {
            Artifact key = entry.getKey();
            UnloadedConfiguration value = entry.getValue();
            map.put(key, value);
        }
        return map;
    }
}
