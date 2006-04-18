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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationModel {
    private final Map configurations = new TreeMap();

    public void addConfiguation(Artifact configurationId, Set loadParentIds, Set startParentIds) throws NoSuchConfigException {
        Set startParents = getStatuses(startParentIds);

        // load parents are both the class parents and the service parents
        Set loadParents = new LinkedHashSet(startParents);
        loadParents.addAll(getStatuses(loadParentIds));

        ConfigurationStatus configurationStatus = new ConfigurationStatus(configurationId, loadParents, startParents);
        configurations.put(configurationId, configurationStatus);
    }

    private Set getStatuses(Set configurationIds) throws NoSuchConfigException {
        LinkedHashSet statuses = new LinkedHashSet(configurationIds.size());
        for (Iterator iterator = configurationIds.iterator(); iterator.hasNext();) {
            Artifact configurationId = (Artifact) iterator.next();
            ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
            if (configurationStatus == null) {
                throw new NoSuchConfigException(configurationId);
            }

            statuses.add(configurationStatus);
        }
        return statuses;
    }

    public void removeConfiguration(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        configurationStatus.destroy();
        configurations.remove(configurationId);
    }

    public boolean containsConfiguration(Artifact configurationId) {
        return configurations.containsKey(configurationId);
    }

    public boolean isLoaded(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.isLoaded();
        }
        return false;
    }

    public boolean isStarted(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.isStarted();
        }
        return false;
    }

    public LinkedHashSet load(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.load();
    }

    public LinkedHashSet start(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.start();
    }

    public LinkedHashSet stop(Artifact configurationId) throws NoSuchConfigException {
        return stop(configurationId, true);
    }

    public LinkedHashSet stop(Artifact configurationId, boolean gc) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.stop(gc);
    }

    public LinkedHashSet restart(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.restart();
    }

    public LinkedHashSet unload(Artifact configurationId) throws NoSuchConfigException {
        return unload(configurationId, true);
    }

    public LinkedHashSet unload(Artifact configurationId, boolean gc) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.unload(gc);
    }

    public LinkedHashSet reload(Artifact configurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(configurationId);
        }
        return configurationStatus.reload();
    }

    public Set getLoaded() {
        Set result = new LinkedHashSet();
        for (Iterator iterator = configurations.values().iterator(); iterator.hasNext();) {
            ConfigurationStatus status = (ConfigurationStatus) iterator.next();
            if (status.isLoaded()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }

    public Set getStarted() {
        Set result = new LinkedHashSet();
        for (Iterator iterator = configurations.values().iterator(); iterator.hasNext();) {
            ConfigurationStatus status = (ConfigurationStatus) iterator.next();
            if (status.isStarted()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }

    public Set getUserLoaded() {
        Set result = new LinkedHashSet();
        for (Iterator iterator = configurations.values().iterator(); iterator.hasNext();) {
            ConfigurationStatus status = (ConfigurationStatus) iterator.next();
            if (status.isUserLoaded()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }

    public Set getUserStarted() {
        Set result = new LinkedHashSet();
        for (Iterator iterator = configurations.values().iterator(); iterator.hasNext();) {
            ConfigurationStatus status = (ConfigurationStatus) iterator.next();
            if (status.isUserStarted()) {
                result.add(status.getConfigurationId());
            }
        }
        return result;
    }
}
