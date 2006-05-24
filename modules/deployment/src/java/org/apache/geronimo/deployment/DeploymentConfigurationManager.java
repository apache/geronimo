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
package org.apache.geronimo.deployment;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import java.util.Arrays;
import java.io.IOException;

import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentConfigurationManager extends SimpleConfigurationManager {
    private final ConfigurationManager configurationManager;

    public DeploymentConfigurationManager(ConfigurationManager configurationManager, Collection repositories) {
        super(Arrays.asList(configurationManager.getStores()), configurationManager.getArtifactResolver(), repositories);
        this.configurationManager = configurationManager;
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

    public synchronized Configuration getConfiguration(Artifact configurationId) {
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        if (configuration == null) {
            configuration = super.getConfiguration(configurationId);
        }
        return configuration;
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

    protected Configuration load(ConfigurationData configurationData, LinkedHashSet resolvedParentIds, Map loadedConfigurations) throws InvalidConfigException {
        return super.load(configurationData, resolvedParentIds, loadedConfigurations);
    }

    protected void load(Artifact configurationId) throws NoSuchConfigException {
        if (configurationModel.containsConfiguration(configurationId)) {
            super.load(configurationId);
        }
    }

    protected void addNewConfigurationToModel(Configuration configuration) throws NoSuchConfigException {
        LinkedHashSet loadParents = getLoadParents(configuration);
        for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
            Configuration loadParent= (Configuration) iterator.next();
            if (!configurationModel.containsConfiguration(loadParent.getId())) {
                configurationModel.addConfiguation(loadParent.getId(), Collections.EMPTY_SET, Collections.EMPTY_SET);
                configurationModel.load(loadParent.getId());
            }
        }
        LinkedHashSet startParents = getStartParents(configuration);
        for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
            Configuration startParent = (Configuration) iterator.next();
            if (!configurationModel.containsConfiguration(startParent.getId())) {
                configurationModel.addConfiguation(startParent.getId(), Collections.EMPTY_SET, Collections.EMPTY_SET);
                configurationModel.load(startParent.getId());
            }
        }
        super.addNewConfigurationToModel(configuration);
    }

    //
    // UNLOAD
    //

    public synchronized LifecycleResults unloadConfiguration(Artifact id) throws NoSuchConfigException {
        return super.unloadConfiguration(id);
    }

    public synchronized LifecycleResults unloadConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException {
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

    public List listStores() {
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

    public List listConfigurations(AbstractName storeName) throws NoSuchStoreException {
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
