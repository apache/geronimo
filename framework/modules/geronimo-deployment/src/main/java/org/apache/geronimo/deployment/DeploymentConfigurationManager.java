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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModel;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.DependencyNode;
import org.apache.geronimo.kernel.config.DeploymentWatcher;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
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
        super(Arrays.asList(configurationManager.getStores()), configurationManager.getArtifactResolver(), repositories, Collections.<DeploymentWatcher>emptySet(), bundleContext, getConfigurationModel(configurationManager));
        this.configurationManager = configurationManager;
    }

    private static ConfigurationModel getConfigurationModel(ConfigurationManager configurationManager) {
        if (configurationManager instanceof SimpleConfigurationManager) {
            return ((SimpleConfigurationManager)configurationManager).getConfigurationModel();
        }
        return new ConfigurationModel();
    }

    //
    // GENERAL DATA
    //

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

    @Override
    public Bundle getBundle(Artifact id) {
        Bundle bundle = configurationManager.getBundle(id);
        if (bundle == null) {
            bundle = super.getBundle(id);
        }
        return bundle;
    }

    @Override
    public synchronized ConfigurationData getLoadedConfigurationData(Artifact configurationId) {
        ConfigurationData configurationData = configurationManager.getLoadedConfigurationData(configurationId);
        return configurationData == null ? super.getLoadedConfigurationData(configurationId) : configurationData;
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

    @Override
    protected void addConfigurationModel(Artifact id) throws NoSuchConfigException, MissingDependencyException {
        ConfigurationData configurationData = getLoadedConfigurationData(id);
        if (configurationData == null) {
            throw new NoSuchConfigException(id, "Should be load the configurationData first");
        }
        DependencyNode node = buildDependencyNode(configurationData);
        for (Artifact classParentId : node.getClassParents()) {
            if (!configurationModel.containsConfiguration(classParentId)) {
                configurationModel.addConfiguration(classParentId, Collections.<Artifact> emptySet(), Collections.<Artifact> emptySet());
                configurationModel.load(classParentId);
            }
        }
        for (Artifact serviceParentId : node.getServiceParents()) {
            if (!configurationModel.containsConfiguration(serviceParentId)) {
                configurationModel.addConfiguration(serviceParentId, Collections.<Artifact> emptySet(), Collections.<Artifact> emptySet());
                configurationModel.load(serviceParentId);
            }
        }
        configurationModel.addConfiguration(id, node.getClassParents(), node.getServiceParents());
    }

    @Override
    protected void loadConfigurationModel(Artifact configurationId) throws NoSuchConfigException {
        if (configurationModel.containsConfiguration(configurationId)) {
            super.loadConfigurationModel(configurationId);
        }
    }

    @Override
    protected void startInternal(Configuration configuration) throws Exception {
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

    public List<ConfigurationInfo> listConfigurations() {
        throw new UnsupportedOperationException();
    }

    public ConfigurationStore getStoreForConfiguration(Artifact configId) {
        throw new UnsupportedOperationException();
    }

    public List<ConfigurationInfo> listConfigurations(AbstractName storeName) throws NoSuchStoreException {
        throw new UnsupportedOperationException();
    }

    /*public synchronized LifecycleResults startConfiguration(Artifact id) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }

    public synchronized LifecycleResults startConfiguration(Artifact id, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        throw new UnsupportedOperationException();
    }*/

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
