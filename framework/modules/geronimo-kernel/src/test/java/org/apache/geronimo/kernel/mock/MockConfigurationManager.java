/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.kernel.mock;

import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.config.DependencyNode;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.gbean.AbstractName;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class MockConfigurationManager implements ConfigurationManager {

    private final Map<Artifact, Configuration> configurations = new HashMap<Artifact, Configuration>();

    public boolean isInstalled(Artifact configurationId) {
        return false;
    }

    public Artifact[] getInstalled(Artifact query) {
        return new Artifact[0];
    }

    public Artifact[] getLoaded(Artifact query) {
        return new Artifact[0];
    }

    public Artifact[] getRunning(Artifact query) {
        return new Artifact[0];
    }

    public boolean isLoaded(Artifact configID) {
        return false;
    }

    public List listStores() {
        return Collections.EMPTY_LIST;
    }

    public ConfigurationStore[] getStores() {
        return new ConfigurationStore[0];
    }

    public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
        return null;
    }

    public List listConfigurations(AbstractName store) throws NoSuchStoreException {
        return Collections.EMPTY_LIST;
    }

    public boolean isRunning(Artifact configurationId) {
        return false;
    }

    public List listConfigurations() {
        return null;
    }

    public boolean isConfiguration(Artifact artifact) {
        return configurations.containsKey(artifact);
    }

    public Configuration getConfiguration(Artifact configurationId) {
        return configurations.get(configurationId);
    }

    @Override
    public Configuration getConfiguration(long bundleId) {
        return null;
    }

    public LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return loadConfiguration(configurationData, null);
    }

    public LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        try {
            Artifact configId = configurationData.getEnvironment().getConfigId();
            Configuration configuration = new Configuration(configurationData, new DependencyNode(configId, null, null), null, null, null, this);
            configurations.put(configId, configuration);
        } catch (InvalidConfigException e) {

        }
        return null;
    }

    public LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException {
        return null;
    }

    public LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
        return null;
    }

    public LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException {
        return null;
    }

    public LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
        return null;
    }

    public LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return null;
    }

    public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {

    }

    public ArtifactResolver getArtifactResolver() {
        return null;
    }

    public boolean isOnline() {
        return true;
    }

    public void setOnline(boolean online) {
    }

    public Collection<? extends Repository> getRepositories() {
        return null;
    }

    public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
        return null;
    }

    public ConfigurationResolver newConfigurationResolver(ConfigurationData configurationData) {
        return null;
    }

    public LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        return null;
    }

    public Bundle getBundle(Artifact id) {
        return null;
    }
}
