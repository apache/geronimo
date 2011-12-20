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
package org.apache.geronimo.deployment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModel;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.DeploymentWatcher;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentConfigurationManager extends SimpleConfigurationManager {
    private final ConfigurationManager configurationManager;

    public DeploymentConfigurationManager(ConfigurationManager configurationManager, Collection<? extends Repository> repositories, BundleContext bundleContext) {
        super(Arrays.asList(configurationManager.getStores()), configurationManager.getArtifactResolver(), mergeRepositories(repositories, configurationManager), Collections.<DeploymentWatcher>emptySet(), bundleContext, getConfigurationModel(configurationManager));
        this.configurationManager = configurationManager;
    }

    private static ConfigurationModel getConfigurationModel(ConfigurationManager configurationManager) {
        if (configurationManager instanceof SimpleConfigurationManager) {
            return ((SimpleConfigurationManager)configurationManager).getConfigurationModel();
        }
        return new ConfigurationModel();
    }

    private static Collection<? extends Repository> mergeRepositories(Collection<? extends Repository> repositories, ConfigurationManager configurationManager) {
        List<Repository> mergedRepositories = new ArrayList<Repository>();        
        if (repositories != null) {
            mergedRepositories.addAll(repositories);
        }
        if (configurationManager.getRepositories() != null) {
            mergedRepositories.addAll(configurationManager.getRepositories());
        }
        return mergedRepositories;
    }

    //
    // GENERAL DATA
    //

    public synchronized boolean isInstalled(Artifact configId) {
        return super.isInstalled(configId);
    }

    public synchronized boolean isLoaded(Artifact configId) {
        return configurationManager.isLoaded(configId) || super.isLoaded(configId);
    }

    public synchronized boolean isRunning(Artifact configId) {
        return configurationManager.isRunning(configId) || super.isRunning(configId);
    }

    public boolean isConfiguration(Artifact artifact) {
        return configurationManager.isConfiguration(artifact) || super.isConfiguration(artifact);
    }

    @Override
    public synchronized Configuration getConfiguration(Artifact configurationId) {
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        if (configuration == null) {
            configuration = super.getConfiguration(configurationId);
        }
        return configuration;
    }

    @Override
    public synchronized Configuration getConfiguration(long bundleId) {
        Configuration configuration = configurationManager.getConfiguration(bundleId);
        if (configuration == null) {
            configuration = super.getConfiguration(bundleId);
        }
        return configuration;
    }

    @Override
    public Bundle getBundle(Artifact id) {
        Bundle bundle = configurationManager.getBundle(id);
        if (bundle == null) {
            bundle = super.getBundle(id);
        }
        return bundle;
    }

    public ArtifactResolver getArtifactResolver() {
        return super.getArtifactResolver();
    }

    /**
     * This configuration manager never starts any configurations
     * @return false
     */
    public boolean isOnline() {
        return false;
    }

    public void setOnline(boolean online) {
    }

    //
    // LOAD
    //

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return super.loadConfiguration(configurationId);
    }

    public synchronized LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return super.loadConfiguration(configurationId, monitor);
    }

    public synchronized LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return super.loadConfiguration(configurationData);
    }

    public synchronized LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return super.loadConfiguration(configurationData, monitor);
    }

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        return super.load(configurationData, resolvedParentIds, loadedConfigurations);
    }

    protected void load(Artifact configurationId) throws NoSuchConfigException {
        if (configurationModel.containsConfiguration(configurationId)) {
            super.load(configurationId);
        }
    }

    protected void addNewConfigurationToModel(Configuration configuration) throws NoSuchConfigException {
        LinkedHashSet<Configuration> loadParents = getLoadParents(configuration);
        for (Configuration loadParent : loadParents) {
            if (!configurationModel.containsConfiguration(loadParent.getId())) {
                configurationModel.addConfiguration(loadParent.getId(), Collections.<Artifact>emptySet(), Collections.<Artifact>emptySet());
                configurationModel.load(loadParent.getId());
            }
        }
        LinkedHashSet<Configuration> startParents = getStartParents(configuration);
        for (Configuration startParent : startParents) {
            if (!configurationModel.containsConfiguration(startParent.getId())) {
                configurationModel.addConfiguration(startParent.getId(), Collections.<Artifact>emptySet(), Collections.<Artifact>emptySet());
                configurationModel.load(startParent.getId());
            }
        }
        super.addNewConfigurationToModel(configuration);
    }

    //
    // UNLOAD
    //

    public synchronized LifecycleResults unloadConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        return super.unloadConfiguration(id);
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return super.unloadConfiguration(id, monitor);
    }

    protected void unload(Configuration configuration) {
        super.unload(configuration);
    }

    //
    // STOP.. used by unload
    //
    protected void stop(Configuration configuration) {
        super.stop(configuration);
    }


    //
    // UNSUPPORTED
    //

    public Artifact[] getInstalled(Artifact query) {
        throw new UnsupportedOperationException();
    }

    public Artifact[] getLoaded(Artifact query) {
        throw new UnsupportedOperationException();
    }

    public Artifact[] getRunning(Artifact query) {
        throw new UnsupportedOperationException();
    }

    public List<AbstractName> listStores() {
        throw new UnsupportedOperationException();
    }

    public ConfigurationStore[] getStores() {
        throw new UnsupportedOperationException();
    }

    public List listConfigurations() {
        throw new UnsupportedOperationException();
    }

    public ConfigurationStore getStoreForConfiguration(Artifact configId) {
        throw new UnsupportedOperationException();
    }

    public List<ConfigurationInfo> listConfigurations(AbstractName storeName) throws NoSuchStoreException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults startConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults startConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    protected void start(Configuration configuration) throws Exception {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults stopConfiguration(Artifact id) throws NoSuchConfigException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults stopConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults restartConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults restartConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id, Version version) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults reloadConfiguration(Artifact id, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws LifecycleException, NoSuchConfigException {
        throw new UnsupportedOperationException();
    }

    public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws LifecycleException, NoSuchConfigException {
        throw new UnsupportedOperationException();
    }

    public synchronized void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {
        throw new UnsupportedOperationException();
    }
}
