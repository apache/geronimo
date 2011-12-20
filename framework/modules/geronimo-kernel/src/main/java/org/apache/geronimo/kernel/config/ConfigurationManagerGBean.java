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


package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */

@GBean(j2eeType="ConfigurationManager")
public class ConfigurationManagerGBean extends AbstractServiceWrapper<ConfigurationManager> implements ConfigurationManager {
    public ConfigurationManagerGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, ConfigurationManager.class);
    }

    @Override
    public boolean isInstalled(Artifact configurationId) {
        return get().isInstalled(configurationId);
    }

    @Override
    public boolean isLoaded(Artifact configurationId) {
        return get().isLoaded(configurationId);
    }

    @Override
    public boolean isRunning(Artifact configurationId) {
        return get().isRunning(configurationId);
    }

    @Override
    public Artifact[] getInstalled(Artifact query) {
        return get().getInstalled(query);
    }

    @Override
    public Artifact[] getLoaded(Artifact query) {
        return get().getLoaded(query);
    }

    @Override
    public Artifact[] getRunning(Artifact query) {
        return get().getRunning(query);
    }

    @Override
    public List listConfigurations() {
        return get().listConfigurations();
    }

    @Override
    public List<AbstractName> listStores() {
        return get().listStores();
    }

    @Override
    public ConfigurationStore[] getStores() {
        return get().getStores();
    }

    @Override
    public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
        return get().getStoreForConfiguration(configuration);
    }

    @Override
    public List listConfigurations(AbstractName store) throws NoSuchStoreException {
        return get().listConfigurations(store);
    }

    @Override
    public boolean isConfiguration(Artifact artifact) {
        return get().isConfiguration(artifact);
    }

    @Override
    public LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return get().loadConfiguration(configurationId);
    }

    @Override
    public LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return get().loadConfiguration(configurationData);
    }

    @Override
    public LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().loadConfiguration(configurationId, monitor);
    }

    @Override
    public LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().loadConfiguration(configurationData, monitor);
    }

    @Override
    public LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return get().unloadConfiguration(configurationId);
    }

    @Override
    public LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().unloadConfiguration(configurationId, monitor);
    }

    @Override
    public LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return get().startConfiguration(configurationId);
    }

    @Override
    public LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().startConfiguration(configurationId, monitor);
    }

    @Override
    public LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException {
        return get().stopConfiguration(configurationId);
    }

    @Override
    public LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
        return get().stopConfiguration(configurationId, monitor);
    }

    @Override
    public LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return get().restartConfiguration(configurationId);
    }

    @Override
    public LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().restartConfiguration(configurationId, monitor);
    }

    @Override
    public LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        return get().reloadConfiguration(configurationId);
    }

    @Override
    public LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().reloadConfiguration(configurationId, monitor);
    }

    @Override
    public LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException {
        return get().reloadConfiguration(configurationId, version);
    }

    @Override
    public LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().reloadConfiguration(configurationId, version, monitor);
    }

    @Override
    public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
        return get().reloadConfiguration(configurationData);
    }

    @Override
    public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
        return get().reloadConfiguration(configurationData, monitor);
    }

    @Override
    public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException, LifecycleException {
        get().uninstallConfiguration(configurationId);
    }

    @Override
    public ArtifactResolver getArtifactResolver() {
        return get().getArtifactResolver();
    }

    @Override
    public boolean isOnline() {
        return get().isOnline();
    }

    @Override
    public void setOnline(boolean online) {
        get().setOnline(online);
    }

    @Override
    public Collection<? extends Repository> getRepositories() {
        return get().getRepositories();
    }

    @Override
    public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
        return get().sort(ids, monitor);
    }

    @Override
    public ConfigurationResolver newConfigurationResolver(ConfigurationData configurationData) {
        return get().newConfigurationResolver(configurationData);
    }

    @Override
    public LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
        return get().resolveParentIds(configurationData);
    }

    @Override
    public Bundle getBundle(Artifact id) {
        return get().getBundle(id);
    }

    @Override
    public Configuration getConfiguration(Artifact configurationId) {
        return get().getConfiguration(configurationId);
    }

    @Override
    public Configuration getConfiguration(long bundleId) {
        return get().getConfiguration(bundleId);
    }
}
