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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationModel {
    private final Map<Artifact, ConfigurationStatus> configurations = new TreeMap<Artifact, ConfigurationStatus>();

    public void addConfiguration(Artifact configurationId, Set<Artifact> loadParentIds, Set<Artifact> startParentIds) throws NoSuchConfigException {
        Set<ConfigurationStatus> startParents = getStatuses(startParentIds);

        // load parents are a superset of start parents
        Set<ConfigurationStatus> loadParents = new LinkedHashSet<ConfigurationStatus>(startParents);
        loadParents.addAll(getStatuses(loadParentIds));

        ConfigurationStatus configurationStatus = new ConfigurationStatus(configurationId, loadParents, startParents);
        configurations.put(configurationId, configurationStatus);
    }

    private Set<ConfigurationStatus> getStatuses(Set<Artifact> configurationIds) throws NoSuchConfigException {
        LinkedHashSet<ConfigurationStatus> statuses = new LinkedHashSet<ConfigurationStatus>(configurationIds.size());
        for (Artifact configurationId : configurationIds) {
            ConfigurationStatus configurationStatus = configurations.get(configurationId);
            if (configurationStatus == null) {
                throw new NoSuchConfigException(configurationId);
            }

            statuses.add(configurationStatus);
        }
        return statuses;
    }

    public void removeConfiguration(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        configurationStatus.destroy();
        configurations.remove(configurationId);
    }

    public boolean containsConfiguration(Artifact configurationId) {
        return configurations.containsKey(configurationId);
    }

    public void upgradeConfiguration(Artifact existingId, Artifact newId, Set<Artifact> newLoadParentIds, Set<Artifact> newStartParentIds) throws NoSuchConfigException {
        Set<ConfigurationStatus> newStartParents = getStatuses(newStartParentIds);

        // load parents are a superset of start parents
        Set<ConfigurationStatus> newLoadParents = new LinkedHashSet<ConfigurationStatus>(newStartParents);
        newLoadParents.addAll(getStatuses(newLoadParentIds));

        ConfigurationStatus configurationStatus = configurations.remove(existingId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(existingId);
        }
        configurations.put(newId, configurationStatus);
        configurationStatus.upgrade(newId, newLoadParents, newStartParents);
    }

    public boolean isLoaded(Artifact configurationId) {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.isLoaded();
        }
        return false;
    }

    public Artifact[] getLoaded(Artifact query) {
        List<Artifact> results = new ArrayList<Artifact>();
        for (Map.Entry<Artifact, ConfigurationStatus> entry : configurations.entrySet()) {
            Artifact test = entry.getKey();
            ConfigurationStatus status = entry.getValue();
            if (query.matches(test) && status.isLoaded()) {
                results.add(test);
            }
        }
        return results.toArray(new Artifact[results.size()]);
    }

    public boolean isStarted(Artifact configurationId) {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.isStarted();
        }
        return false;
    }

    public Artifact[] getStarted(Artifact query) {
        List<Artifact> results = new ArrayList<Artifact>();
        for (Map.Entry<Artifact, ConfigurationStatus> entry : configurations.entrySet()) {
            Artifact test = entry.getKey();
            ConfigurationStatus status = entry.getValue();
            if (query.matches(test) && status.isStarted()) {
                results.add(test);
            }
        }
        return results.toArray(new Artifact[results.size()]);
    }

    public LinkedHashSet<Artifact> load(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.load();
    }

    public LinkedHashSet<Artifact> start(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.start();
    }

    public LinkedHashSet<Artifact> stop(Artifact configurationId) throws NoSuchConfigException {
        return stop(configurationId, true);
    }

    public LinkedHashSet<Artifact> stop(Artifact configurationId, boolean gc) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.stop(gc);
    }

    public LinkedHashSet<Artifact> restart(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.restart();
    }

    public LinkedHashSet<Artifact> unload(Artifact configurationId) throws NoSuchConfigException {
        return unload(configurationId, true);
    }

    public LinkedHashSet<Artifact> unload(Artifact configurationId, boolean gc) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.unload(gc);
    }

    public LinkedHashSet<Artifact> reload(Artifact existingConfigurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = configurations.get(existingConfigurationId);
        if (configurationStatus == null) {
            return new LinkedHashSet<Artifact>();
        }
        return configurationStatus.reload();
    }

    public Set<Artifact> getLoaded() {
        Set<Artifact> result = new LinkedHashSet<Artifact>();
        for (ConfigurationStatus status : configurations.values()) {
            if (status.isLoaded()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }

    public Set<Artifact> getStarted() {
        Set<Artifact> result = new LinkedHashSet<Artifact>();
        for (ConfigurationStatus status : configurations.values()) {
            if (status.isStarted()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }

    public Set<Artifact> getUserLoaded() {
        Set<Artifact> result = new LinkedHashSet<Artifact>();
        for (ConfigurationStatus status : configurations.values()) {
            if (status.isUserLoaded()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }

    public Set<Artifact> getUserStarted() {
        Set<Artifact> result = new LinkedHashSet<Artifact>();
        for (ConfigurationStatus status : configurations.values()) {
            if (status.isUserStarted()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }
    
    public LinkedHashSet<Artifact> getStartedChildren(Artifact configurationId) {
        ConfigurationStatus configurationStatus = configurations.get(configurationId);
        if (configurationStatus == null) {
            return new LinkedHashSet<Artifact>();
        }
        return configurationStatus.getStartedChildren();
    }
}
