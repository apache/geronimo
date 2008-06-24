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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationModel {
    private final Map configurations = new TreeMap();

    public void addConfiguation(Artifact configurationId, Set loadParentIds, Set startParentIds) throws NoSuchConfigException {
        Set startParents = getStatuses(startParentIds);

        // load parents are a superset of start parents
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

    public void upgradeConfiguration(Artifact existingId, Artifact newId, Set newLoadParentIds, Set newStartParentIds) throws NoSuchConfigException {
        Set newStartParents = getStatuses(newStartParentIds);

        // load parents are a superset of start parents
        Set newLoadParents = new LinkedHashSet(newStartParents);
        newLoadParents.addAll(getStatuses(newLoadParentIds));

        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.remove(existingId);
        if (configurationStatus == null) {
            throw new NoSuchConfigException(existingId);
        }
        configurations.put(newId, configurationStatus);
        configurationStatus.upgrade(newId, newLoadParents, newStartParents);
    }

    public boolean isLoaded(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.isLoaded();
        }
        return false;
    }

    public Artifact[] getLoaded(Artifact query) {
        List results = new ArrayList();
        for (Iterator it = configurations.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Artifact test = (Artifact) entry.getKey();
            ConfigurationStatus status = (ConfigurationStatus) entry.getValue();
            if(query.matches(test) && status.isLoaded()) {
                results.add(test);
            }
        }
        return (Artifact[]) results.toArray(new Artifact[results.size()]);
    }

    public boolean isStarted(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus != null) {
            return configurationStatus.isStarted();
        }
        return false;
    }

    public Artifact[] getStarted(Artifact query) {
        List results = new ArrayList();
        for (Iterator it = configurations.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Artifact test = (Artifact) entry.getKey();
            ConfigurationStatus status = (ConfigurationStatus) entry.getValue();
            if(query.matches(test) && status.isStarted()) {
                results.add(test);
            }
        }
        return (Artifact[]) results.toArray(new Artifact[results.size()]);
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

    public LinkedHashSet reload(Artifact existingConfigurationId) throws NoSuchConfigException {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(existingConfigurationId);
        if (configurationStatus == null) {
            return new LinkedHashSet();
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
    
    public LinkedHashSet getStartedChildren(Artifact configurationId) {
        ConfigurationStatus configurationStatus = (ConfigurationStatus) configurations.get(configurationId);
        if (configurationStatus == null) {
            return new LinkedHashSet();
        }
        return configurationStatus.getStartedChildren();
    }
}
