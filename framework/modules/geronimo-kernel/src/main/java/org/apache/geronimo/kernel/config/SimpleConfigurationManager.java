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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.management.State;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */

@Component(componentAbstract = true)
public class SimpleConfigurationManager implements ConfigurationManager {
    protected static final Logger log = LoggerFactory.getLogger(SimpleConfigurationManager.class);

    protected final ConfigurationModel configurationModel;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ArtifactResolver artifactResolver;

    @Reference(referenceInterface = ConfigurationStore.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected final Collection<ConfigurationStore> stores = new LinkedHashSet<ConfigurationStore>();

    @Reference(referenceInterface = Repository.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected final Collection<Repository> repositories = new LinkedHashSet<Repository>();

    @Reference(referenceInterface = DeploymentWatcher.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected final Collection<DeploymentWatcher> watchers = new LinkedHashSet<DeploymentWatcher>();

    protected final Map<Artifact, Configuration> configurations = new LinkedHashMap<Artifact, Configuration>();
    protected final Map<Long, Configuration> configurationsByID = new LinkedHashMap<Long, Configuration>();
    protected final Map<Artifact, Bundle> bundles = new LinkedHashMap<Artifact, Bundle>();

    protected BundleContext bundleContext;

    //TODO need thread local of loaded configurations OSGI GROSS!!
    private final ThreadLocal<Map<Artifact, Configuration>> loadedConfigurations = new ThreadLocal<Map<Artifact, Configuration>>() {
    };

    /**
     * When this is not null, it points to the "new" configuration that is
     * part of an in-process reload operation.  This configuration will
     * definitely be loaded, but might not be started yet.  It shold never be
     * populated outside the scope of a reload operation.
     */
    private Configuration reloadingConfiguration;

    private Object reloadingConfigurationLock = new Object();

    public SimpleConfigurationManager() {
        this.configurationModel = new ConfigurationModel();
    }

    public SimpleConfigurationManager(Collection<ConfigurationStore> stores, ArtifactResolver artifactResolver, Collection<? extends Repository> repositories, BundleContext bundleContext) {
        this(stores, artifactResolver, repositories, Collections.<DeploymentWatcher>emptySet(), bundleContext);
    }

    public SimpleConfigurationManager(Collection<ConfigurationStore> stores,
                                      ArtifactResolver artifactResolver,
                                      Collection<? extends Repository> repositories,
                                      Collection<DeploymentWatcher> watchers,
                                      BundleContext bundleContext) {
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
        this.stores.addAll(stores);
        this.artifactResolver = artifactResolver;
        this.repositories.addAll(repositories);
        this.watchers.addAll(watchers);
        this.bundleContext = bundleContext;
    }


    public void setArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    public void unsetArtifactResolver(ArtifactResolver artifactResolver) {
        if (this.artifactResolver == artifactResolver) {
            this.artifactResolver = null;
        }
    }

    public void bindConfigurationStore(ConfigurationStore configurationStore) {
        stores.add(configurationStore);
    }

    public void unbindConfigurationStore(ConfigurationStore configurationStore) {
        stores.remove(configurationStore);
    }

    public void bindRepository(Repository repository) {
        repositories.add(repository);
    }

    public void unbindRepository(Repository repository) {
        repositories.remove(repository);
    }

    public void bindDeploymentWatcher(DeploymentWatcher deploymentWatcher) {
        watchers.add(deploymentWatcher);
    }

    public void unbindDeploymentWatcher(DeploymentWatcher deploymentWatcher) {
        watchers.remove(deploymentWatcher);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public ConfigurationModel getConfigurationModel() {
        return configurationModel;
    }

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

    public synchronized boolean isRunning(Artifact configId) {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        return configurationModel.isStarted(configId);
    }

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

    public Artifact[] getLoaded(Artifact query) {
        return configurationModel.getLoaded(query);
    }

    public Artifact[] getRunning(Artifact query) {
        return configurationModel.getStarted(query);
    }


    public List<AbstractName> listStores() {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        List<AbstractName> result = new ArrayList<AbstractName>(storeSnapshot.size());
        for (ConfigurationStore store : storeSnapshot) {
            result.add(store.getAbstractName());
        }
        return result;
    }

    public ConfigurationStore[] getStores() {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        return storeSnapshot.toArray(new ConfigurationStore[storeSnapshot.size()]);
    }

    public Collection<? extends Repository> getRepositories() {
        return repositories;
    }

    public List listConfigurations() {
        List<ConfigurationStore> storeSnapshot = getStoreList();
        List<ConfigurationInfo> list = new ArrayList<ConfigurationInfo>();
        for (ConfigurationStore store : storeSnapshot) {
            list.addAll(listConfigurations(store));
        }
        return list;
    }

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

    public boolean isConfiguration(Artifact artifact) {
        if (!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact " + artifact + " is not fully resolved");
        }
        synchronized (configurations) {
            // if it is loaded, it is definitely a configuration
            if (configurations.containsKey(artifact)) {
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
    public Configuration getConfiguration(long bundleId) {
        return configurationsByID.get(bundleId);
    }

    public Bundle getBundle(Artifact id) {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        return bundles.get(id);
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
        try {
            String location = locateBundle(configurationId, monitor);
            Bundle bundle = bundleContext.installBundle(location);
            if (BundleUtils.canStart(bundle)) {
                bundle.start(Bundle.START_TRANSIENT);
                if (bundle.getState() != 32) {
//                    throw new IllegalStateException("Cant start bundle " + configurationId);
                    bundle.start();   //should throw an exception if bundle won't start. start triggers loading in DependencyManager.
                }
            }
            bundles.put(configurationId, bundle);
        } catch (Exception e) {
            monitor.finished();
            throw new LifecycleException("load", configurationId, e);
        }

        // load the configuration
        LifecycleResults results = new LifecycleResults();
        synchronized (configurations) {
            if (!configurations.containsKey(configurationId)) {
                configurationModel.addConfiguration(configurationId, Collections.<Artifact> emptySet(), Collections.<Artifact> emptySet());
            }
        }
        load(configurationId);
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
//            LinkedHashMap<Artifact, UnloadedConfiguration> configurationsToLoad = new LinkedHashMap<Artifact, UnloadedConfiguration>();
            LinkedHashSet<Artifact> resolvedParents;
            try {
                resolvedParents = resolveParentIds(configurationData);
//                loadDepthFirst(configurationData, configurationsToLoad, monitor);
            } catch (Exception e) {
                monitor.finished();
                throw new LifecycleException("load", id, e);
            }

            // load and start the unloaded the gbean for each configuration (depth first)
            Map<Artifact, Configuration> actuallyLoaded = loadedConfigurations.get();
            boolean newLoad = actuallyLoaded == null;
            if (actuallyLoaded == null) {
                actuallyLoaded = new LinkedHashMap<Artifact, Configuration>(resolvedParents.size());
                loadedConfigurations.set(actuallyLoaded);
            }
            try {
                // update the status of the loaded configurations
                Configuration configuration = load(configurationData, resolvedParents, actuallyLoaded);
                actuallyLoaded.put(configurationData.getId(), configuration);
                addNewConfigurationToModel(configuration);
            } catch (Exception e) {
//                monitor.failed(configurationId, e);

                // there was a problem, so we need to unload all configurations that were actually loaded
//                for (Bundle bundle : actuallyLoaded.values()) {
//                    try {
//                        //TODO OSGI REALLY?
//                        bundle.stop();
//                    } catch (BundleException e1) {
//                        //?? TODO OSGI WHAT??
//                    }
//                }

                monitor.finished();
                throw new LifecycleException("load", id, e);
            } finally {
                if (newLoad) {
                    loadedConfigurations.remove();
                }
            }
        }
        load(id);
        monitor.finished();
        return results;
    }

    protected void load(Artifact configurationId) throws NoSuchConfigException {
        configurationModel.load(configurationId);
    }

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        try {
            ConfigurationResolver configurationResolver = newConfigurationResolver(configurationData);

            return doLoad(configurationData, resolvedParentIds, loadedConfigurations, configurationResolver);
        } catch (Exception e) {
            throw new InvalidConfigException("Error starting configuration gbean " + configurationData.getId(), e);
        }
    }

    public ConfigurationResolver newConfigurationResolver(ConfigurationData configurationData) {
        ConfigurationResolver configurationResolver = new ConfigurationResolver(configurationData, repositories, artifactResolver);
        return configurationResolver;
    }

    protected Configuration doLoad(ConfigurationData configurationData, LinkedHashSet<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations, ConfigurationResolver configurationResolver) throws Exception {
        DependencyNode dependencyNode = buildDependencyNode(configurationData);

//        ClassLoaderHolder classLoaderHolder = buildClassLoaders(configurationData, loadedConfigurations, dependencyNode, configurationResolver);

        List<Configuration> allServiceParents = buildAllServiceParents(loadedConfigurations, dependencyNode);

        Configuration configuration = new Configuration(configurationData, dependencyNode, allServiceParents, null, configurationResolver, this);
        configuration.doStart();
        //TODO why???
        resolvedParentIds.add(configuration.getId());

        Map<Artifact, Configuration> moreLoadedConfigurations = loadedConfigurations;//new LinkedHashMap<Artifact, Configuration>(loadedConfigurations);
        moreLoadedConfigurations.put(dependencyNode.getId(), configuration);
        for (Map.Entry<String, ConfigurationData> childEntry : configurationData.getChildConfigurations().entrySet()) {
            ConfigurationResolver childResolver = configurationResolver.createChildResolver(childEntry.getKey());
            Configuration child = doLoad(childEntry.getValue(), resolvedParentIds, moreLoadedConfigurations, childResolver);
            configuration.addChild(child);
        }
        return configuration;
    }

    protected List<Configuration> buildAllServiceParents(Map<Artifact, Configuration> loadedConfigurations, DependencyNode dependencyNode) throws InvalidConfigException, MissingDependencyException {
        List<Configuration> allServiceParents = new ArrayList<Configuration>();
        for (Artifact parentId : dependencyNode.getServiceParents()) {
            addDepthFirstServiceParents(parentId, allServiceParents, new HashSet<Artifact>(), loadedConfigurations);
        }
        return allServiceParents;
    }


    protected DependencyNode buildDependencyNode(ConfigurationData configurationData) throws MissingDependencyException {
        DependencyNode dependencyNode = DependencyNodeUtil.toDependencyNode(configurationData.getEnvironment(), artifactResolver, this);
        return dependencyNode;
    }

    private void addDepthFirstServiceParents(Artifact id, List<Configuration> ancestors, Set<Artifact> ids, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException, MissingDependencyException {
        if (!ids.contains(id)) {
        	Configuration configuration = null;
        	try {
        		configuration = getConfiguration(id, loadedConfigurations);
        	} catch (InvalidConfigException e) {
        		throw new MissingDependencyException(id);
        	}
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

        //TODO OSGI track loaded configurations in thread local ???
        if (loadedConfigurations.containsKey(resolvedArtifact)) {
            parent = loadedConfigurations.get(resolvedArtifact);
        } else
        if (isLoaded(resolvedArtifact)) {
            parent = getConfiguration(resolvedArtifact);
        } else {
            throw new InvalidConfigException("Cound not find parent configuration: " + resolvedArtifact);
        }
        return parent;
    }

//    private void addNewConfigurationsToModel(Map<Artifact, Bundle> loadedConfigurations) throws NoSuchConfigException {
//        for (Bundle configuration : loadedConfigurations.values()) {
//            addNewConfigurationToModel(configuration);
//        }
//    }

    protected void addNewConfigurationToModel(Configuration configuration) throws NoSuchConfigException {
        configurationModel.addConfiguration(configuration.getId(),
                getConfigurationIds(getLoadParents(configuration)),
                getConfigurationIds(getStartParents(configuration)));
        configurations.put(configuration.getId(), configuration);
        configurationsByID.put(configuration.getBundle().getBundleId(), configuration);
    }

    protected LinkedHashSet<Configuration> getLoadParents(Configuration configuration) throws NoSuchConfigException {
        LinkedHashSet<Configuration> loadParent = new LinkedHashSet<Configuration>();
        getLoadParentsInternal(configuration, loadParent, this);
        return loadParent;
    }

    private void getLoadParentsInternal(final Configuration configuration, LinkedHashSet<Configuration> parents, final ConfigurationSource configurationSource) throws NoSuchConfigException {
        DependencyNodeUtil.addClassParents(configuration.getDependencyNode(), parents, configurationSource);
        for (Configuration childConfiguration : configuration.getChildren()) {
            ConfigurationSource childSource = new ConfigurationSource() {

                @Override
                public Configuration getConfiguration(Artifact configurationId) {
                    if (configurationId.equals(configuration.getId())) {
                        return configuration;
                    }
                    return configurationSource.getConfiguration(configurationId);
                }

                @Override
                public Configuration getConfiguration(long bundleId) {
                    return configurationSource.getConfiguration(bundleId);
                }
            };
            getLoadParentsInternal(childConfiguration, parents, childSource);
            // remove this configuration from the parent Ids since it will cause an infinite loop
            parents.remove(configuration);
        }
    }

    protected LinkedHashSet<Configuration> getStartParents(Configuration configuration) throws NoSuchConfigException {
        LinkedHashSet<Configuration> startParent = new LinkedHashSet<Configuration>();
        getStartParentsInternal(configuration, startParent, this);
        return startParent;
    }

    private void getStartParentsInternal(final Configuration configuration, LinkedHashSet<Configuration> parents, final ConfigurationSource configurationSource) throws NoSuchConfigException {
        DependencyNodeUtil.addServiceParents(configuration.getDependencyNode(), parents, configurationSource);
        for (Configuration childConfiguration : configuration.getChildren()) {
            ConfigurationSource childSource = new ConfigurationSource() {

                public Configuration getConfiguration(Artifact configurationId) {
                    if (configurationId.equals(configuration.getId())) {
                        return configuration;
                    }
                    return configurationSource.getConfiguration(configurationId);
                }

                @Override
                public Configuration getConfiguration(long bundleId) {
                    return configurationSource.getConfiguration(bundleId);
                }
            };
            getStartParentsInternal(childConfiguration, parents, childSource);
            // remove this configuration from the parent Ids since it will cause an infinite loop
            parents.remove(configuration);
        }
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

    // Return ids that can be loaded in sorted order.  Remove loadable ids from source set.
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

    public LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        Environment environment = configurationData.getEnvironment();

        LinkedHashSet<Artifact> parentIds = new LinkedHashSet<Artifact>();
        List<Dependency> dependencies = new ArrayList<Dependency>(environment.getDependencies());
        for (ListIterator<Dependency> iterator = dependencies.listIterator(); iterator.hasNext();) {
            Dependency dependency = iterator.next();
            Artifact resolvedArtifact = null;
            try {
                resolvedArtifact = artifactResolver.resolveInClassLoader(dependency.getArtifact());
            } catch (MissingDependencyException e) {
                log.error("Cannot resolve dependency " + dependency.getArtifact() + " for configuration " + configurationData.getId());
                throw e;
            }
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

    public synchronized LifecycleResults startConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return startConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults startConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        LinkedHashSet<Artifact> unstartedConfigurations = configurationModel.start(id);

        addConfigurationsToMonitor(monitor, unstartedConfigurations);

        LifecycleResults results = new LifecycleResults();
        Artifact configurationId = null;
        try {
            for (Artifact unstartedConfiguration : unstartedConfigurations) {
                configurationId = unstartedConfiguration;
                Configuration configuration = getConfiguration(configurationId);
                if (configuration == null) {
                    throw new NoSuchConfigException(configurationId, "trying to start ancestor config for config " + id + ", but not found");
                }
                monitor.starting(configurationId);
                start(configuration);
                monitor.succeeded(configurationId);

                results.addStarted(configurationId);
            }
        } catch (Exception e) {
            monitor.failed(configurationId, e);
            configurationModel.stop(id);

            for (Artifact started : results.getStarted()) {
                Configuration configuration = getConfiguration(started);
                monitor.stopping(started);
                stop(configuration);
                monitor.succeeded(started);
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
        LinkedHashSet<Artifact> stopList = configurationModel.stop(id);

        addConfigurationsToMonitor(monitor, stopList);

        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : stopList) {
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
        LinkedHashSet<Artifact> restartList = configurationModel.restart(id);

        addConfigurationsToMonitor(monitor, restartList);

        // stop the configuations
        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : restartList) {
            Configuration configuration = getConfiguration(configurationId);
            monitor.stopping(configurationId);
            stop(configuration);
            monitor.succeeded(configurationId);
            results.addStopped(configurationId);
        }

        // reverse the list
        restartList = reverse(restartList);

        // restart the configurations
        Set<Artifact> skip = new HashSet<Artifact>();
        for (Artifact configurationId : restartList) {

            // skip the configurations that have alredy failed or are children of failed configurations
            if (skip.contains(configurationId)) {
                continue;
            }

            // try to start the configuation
            try {
                Configuration configuration = getConfiguration(configurationId);
                applyOverrides(configuration);
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

    public synchronized LifecycleResults unloadConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return unloadConfiguration(id, NullLifecycleMonitor.INSTANCE);
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        if (!id.isResolved()) {
            throw new IllegalArgumentException("Artifact " + id + " is not fully resolved");
        }
        Set started = configurationModel.getStarted();
        LinkedHashSet<Artifact> unloadList = configurationModel.unload(id);

        addConfigurationsToMonitor(monitor, unloadList);

        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : unloadList) {
            Configuration configuration = getConfiguration(configurationId);
            
            if(configuration == null) throw new NoSuchConfigException(configurationId);
            
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

            try {
                Bundle bundle = bundles.remove(configurationId);
                if(bundle == null) {
                	// Attempt to get the bundle from framework directly
					bundle = attemptGetBundleByLocation(configurationId, monitor);
                }
                
                if (bundle != null) {
                    if (BundleUtils.canStop(bundle)) {
                        bundle.stop(Bundle.STOP_TRANSIENT);
                    }
                    if (BundleUtils.canUninstall(bundle)) {
                        bundle.uninstall();
                    }
                }
            } catch (BundleException e) {
                monitor.finished();
                throw new LifecycleException("unload", configurationId, e);
            }
        }
        monitor.finished();
        return results;
    }
    /**
     * Attempt to get the bundle by searching the bundle's location in all bundles in framework 
     * 
     * @param artifact
     * @param monitor
     * @return 
     * @throws NoSuchConfigException
     * @throws IOException
     * @throws InvalidConfigException
     */
    protected Bundle attemptGetBundleByLocation(Artifact artifact, LifecycleMonitor monitor) {
    	String artifactLoc = "";
    	try {
    		artifactLoc = locateBundle(artifact, monitor);
    	} catch (Exception e) {
    		// Because just attempt to get, so ignore
    		return null;
    	}
    	 
    	Bundle[] bundles = this.bundleContext.getBundles();
    	
    	for(Bundle bundle : bundles) {
    		if(artifactLoc.equals(bundle.getLocation())) return bundle;
    	}
    	
    	return null;
    }
    
    protected void removeConfigurationFromModel(Artifact configurationId) throws NoSuchConfigException {
        if (configurationModel.containsConfiguration(configurationId)) {
            configurationModel.removeConfiguration(configurationId);
        }
        Configuration conf = configurations.remove(configurationId);
        if (conf != null) {
            configurationsByID.remove(conf.getBundle().getBundleId());

        }
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
            ConfigurationData existingConfigurationData = configuration.getConfigurationData();
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
        LinkedHashMap<Artifact, UnloadedConfiguration> newConfigurations = new LinkedHashMap<Artifact, UnloadedConfiguration>();
        try {
            loadDepthFirst(newConfigurationData, newConfigurations, monitor);
        } catch (Exception e) {
            monitor.finished();
            throw new LifecycleException("reload", newConfigurationId, e);
        }

        //
        // get a list of the started configuration, so we can restart them later
        //
        Set<Artifact> started = configurationModel.getStarted();

        //
        // get a list of the child configurations that will need to reload
        //
        //   note: we are iterating in reverse order
        LinkedHashMap<Artifact, LinkedHashSet<Artifact>> existingParents = new LinkedHashMap<Artifact, LinkedHashSet<Artifact>>();
        LinkedHashMap<Artifact, UnloadedConfiguration> reloadChildren = new LinkedHashMap<Artifact, UnloadedConfiguration>();
        for (Artifact configurationId : reverse(configurationModel.reload(existingConfigurationId))) {

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
            LinkedHashSet<Artifact> existingParentIds = getResolvedParentIds(configuration);
            existingParents.put(configurationId, existingParentIds);

            // check that the child doen't have a hard dependency on the old configuration
            LinkedHashSet<Artifact> resolvedParentIds;
            if (hasHardDependency(existingConfigurationId, configurationData)) {
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

            reloadChildren.put(configurationId, new UnloadedConfiguration(configurationData, resolvedParentIds));
            monitor.addConfiguration(configurationId);
        }

        //
        // unload the children
        //

        // note: we are iterating in reverse order
        LifecycleResults results = new LifecycleResults();
        for (Artifact configurationId : reverse(reloadChildren).keySet()) {
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
            Map<Artifact, Configuration> loadedParents = new LinkedHashMap<Artifact, Configuration>();
            Map<Artifact, Configuration> startedParents = new LinkedHashMap<Artifact, Configuration>();
            Configuration newConfiguration = null;
            Artifact configurationId = null;
            try {
                //
                // load all of the new configurations
                //
                for (Map.Entry<Artifact, UnloadedConfiguration> entry : newConfigurations.entrySet()) {
                    configurationId = entry.getKey();
                    UnloadedConfiguration unloadedConfiguration = entry.getValue();

                    monitor.loading(configurationId);
                    Configuration configuration = load(unloadedConfiguration.getConfigurationData(), unloadedConfiguration.getResolvedParentIds(), loadedParents);
                    monitor.succeeded(configurationId);

                    if (configurationId.equals(newConfigurationId)) {
                        newConfiguration = configuration;
                        synchronized (reloadingConfigurationLock) {
                            reloadingConfiguration = configuration;
                        }
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
                    LinkedHashSet<Configuration> startList = new LinkedHashSet<Configuration>();
                    for (Configuration serviceParent : getStartParents(newConfiguration)) {
                        if (loadedParents.containsKey(serviceParent.getId())) {
                            startList.add(serviceParent);
                        }
                    }

                    // start the new parents
                    for (Configuration startParent : startList) {
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
                //TODO OSGI TOTALLY BROKEN
//                addNewConfigurationsToModel(loadedParents);

                // now ugrade the existing node in the model
                if (configurationModel.containsConfiguration(existingConfigurationId)) {
                    configurationModel.upgradeConfiguration(existingConfigurationId,
                            newConfigurationId,
                            getConfigurationIds(getLoadParents(newConfiguration)),
                            getConfigurationIds(getStartParents(newConfiguration)));
                } else {
                    configurationModel.addConfiguration(newConfigurationId,
                            getConfigurationIds(getLoadParents(newConfiguration)),
                            getConfigurationIds(getStartParents(newConfiguration)));
                    load(newConfigurationId);
                }

                // replace the configuraiton in he configurations map
                configurations.remove(existingConfiguration.getId());
                configurations.put(newConfigurationId, newConfiguration);

                // migrate the configuration settings
                migrateConfiguration(existingConfigurationId, newConfigurationId, newConfiguration, started.contains(existingConfigurationId));
            } catch (Exception e) {
                monitor.failed(configurationId, e);
                results.addFailed(configurationId, e);

                //
                // stop and unload all configurations that were actually loaded
                //
                for (Configuration configuration : startedParents.values()) {
                    stop(configuration);
                }
                for (Configuration configuration : loadedParents.values()) {
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
                            Collections.<Artifact, Configuration>emptyMap()
                    );
                    synchronized (reloadingConfigurationLock) {
                        reloadingConfiguration = configuration;
                    }
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
                    for (Artifact childId : results.getUnloaded()) {
                        configurationModel.unload(childId);
                        removeConfigurationFromModel(childId);
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
        for (Map.Entry<Artifact, UnloadedConfiguration> entry : reloadChildren.entrySet()) {
            Artifact configurationId = entry.getKey();
            UnloadedConfiguration unloadedConfiguration = entry.getValue();

            // skip the configurations that have alredy failed or are children of failed configurations
            if (skip.contains(configurationId)) {
                continue;
            }

            // try to load the configuation
            Configuration configuration = null;
            try {
                // get the correct resolved parent ids based on if we are loading with the new config id or the existing one
                LinkedHashSet<Artifact> resolvedParentIds;
                if (!reinstatedExisting) {
                    resolvedParentIds = unloadedConfiguration.getResolvedParentIds();
                } else {
                    resolvedParentIds = existingParents.get(configurationId);
                }

                // if the resolved parent ids is null, then we are not supposed to reload this configuration
                if (resolvedParentIds != null) {
                    monitor.loading(configurationId);
                    configuration = load(unloadedConfiguration.getConfigurationData(),
                            resolvedParentIds,
                            Collections.<Artifact,
                                    Configuration>emptyMap()
                    );
                    synchronized (reloadingConfigurationLock) {
                        reloadingConfiguration = configuration;
                    }
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
                LinkedHashSet<Artifact> unloadList = configurationModel.unload(configurationId, false);
                configurationModel.removeConfiguration(configurationId);

                // all of the configurations to be unloaded must be in our unloaded list, or the model is corrupt
                if (!reloadChildren.keySet().containsAll(unloadList)) {
                    throw new AssertionError("Configuration data model is corrupt.   You must restart your server.");
                }

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

    private static LinkedHashSet<Artifact> getResolvedParentIds(Configuration configuration) {
        return configuration.getDependencyNode().getParents();
    }

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

            uninstall(configurationId);

            for (ConfigurationStore store : getStoreList()) {
                if (store.containsConfiguration(configurationId)) {
                    store.uninstall(configurationId);
                }
            }

            removeConfigurationFromModel(configurationId);
        }
        notifyWatchers(configurationId);
    }

    protected void uninstall(Artifact configurationId) {
        //child class can override this method
    }

    private void notifyWatchers(Artifact id) {
        for (DeploymentWatcher watcher : watchers) {
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

    protected List<ConfigurationStore> getStoreList() {
        return new ArrayList<ConfigurationStore>(stores);
    }

    private static void addConfigurationsToMonitor(LifecycleMonitor monitor, LinkedHashSet<Artifact> configurations) {
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

    /**
     * Used to apply overrides to a configuration's gbeans.
     * The overrides are applied before configuration restart.
     *
     * @param configuration configuration to customize
     * @throws InvalidConfigException on error
     */
    private void applyOverrides(Configuration configuration) throws InvalidConfigException {
        Bundle bundle = configuration.getBundle();
        Collection<GBeanData> gbeans = configuration.getConfigurationData().getGBeans(bundle);
        if (configuration.getManageableAttributeStore() != null) {
            configuration.getManageableAttributeStore().applyOverrides(configuration.getId(), gbeans,
                    bundle);
        }
    }

}
