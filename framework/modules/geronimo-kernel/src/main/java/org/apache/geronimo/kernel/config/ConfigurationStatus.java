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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationStatus {
    private Artifact configurationId;
    private final Set<ConfigurationStatus> loadParents = new LinkedHashSet<ConfigurationStatus>();
    private final Set<ConfigurationStatus> startParents = new LinkedHashSet<ConfigurationStatus>();
    private final LinkedHashSet<ConfigurationStatus> loadChildren = new LinkedHashSet<ConfigurationStatus>();
    private final LinkedHashSet<ConfigurationStatus> startChildren = new LinkedHashSet<ConfigurationStatus>();
    private boolean loaded = false;
    private boolean started = false;
    private boolean userLoaded = false;
    private boolean userStarted = false;

    public ConfigurationStatus(Artifact configId, Set<ConfigurationStatus> loadParents, Set<ConfigurationStatus> startParents) {
        if (configId == null) throw new NullPointerException("configId is null");
        if (loadParents == null) throw new NullPointerException("loadParents is null");
        if (startParents == null) throw new NullPointerException("startParents is null");
        if (!loadParents.containsAll(startParents)) {
            throw new IllegalArgumentException("loadParents must contain all startParents");
        }
        this.configurationId = configId;
        this.loadParents.addAll(loadParents);
        this.startParents.addAll(startParents);

        for (ConfigurationStatus loadParent : loadParents) {
            loadParent.loadChildren.add(this);
        }

        for (ConfigurationStatus startParent : startParents) {
            startParent.startChildren.add(this);
        }
    }

    public void destroy() {
        if (started) {
            throw new IllegalStateException("Configuration " + configurationId + " is still running");
        }
        if (loaded) {
            throw new IllegalStateException("Configuration " + configurationId + " is still loaded");
        }
        if (loadChildren.size() > 0 || startChildren.size() > 0) {
            throw new IllegalStateException("Configuration " + configurationId + " still has children");
        }

        for (ConfigurationStatus loadParent : loadParents) {
            loadParent.loadChildren.remove(this);
        }
        loadParents.clear();

        for (ConfigurationStatus startParent : startParents) {
            startParent.startChildren.remove(this);
        }
        startChildren.clear();
    }

    public Artifact getConfigurationId() {
        return configurationId;
    }

    public LinkedHashSet<Artifact> getStartedChildren() {
        LinkedHashSet<ConfigurationStatus> childrenStatuses = new LinkedHashSet<ConfigurationStatus>();
        getStartedChildrenInternal(childrenStatuses);

        LinkedHashSet<Artifact> childrenIds = new LinkedHashSet<Artifact>(childrenStatuses.size());
        for (ConfigurationStatus configurationStatus : childrenStatuses) {
            childrenIds.add(configurationStatus.configurationId);
        }

        return childrenIds;
    }

    private void getStartedChildrenInternal(LinkedHashSet<ConfigurationStatus> childrenStatuses) {
        // if we aren't started, there is nothing to do
        if (!started) {
            return;
        }

        // visit all children
        for (ConfigurationStatus child : startChildren) {
            if (child.isStarted() && !child.configurationId.equals(configurationId)) {
                child.getStartedChildrenInternal(childrenStatuses);
            }
        }
        childrenStatuses.add(this);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isUserLoaded() {
        return userLoaded;
    }

    public boolean isUserStarted() {
        return userStarted;
    }


    public void upgrade(Artifact newId, Set<ConfigurationStatus> newLoadParents, Set<ConfigurationStatus> newStartParents) {
        this.configurationId = newId;

        //
        // remove links from the current parents to me
        //
        for (ConfigurationStatus loadParent : loadParents) {
            loadParent.loadChildren.remove(this);
        }
        loadParents.clear();

        for (ConfigurationStatus startParent : startParents) {
            startParent.startChildren.remove(this);
        }
        startChildren.clear();

        //
        // connect to to the new parents
        //
        this.loadParents.addAll(newLoadParents);
        this.startParents.addAll(newStartParents);

        for (ConfigurationStatus loadParent : loadParents) {
            loadParent.loadChildren.add(this);
        }

        for (ConfigurationStatus startParent : startParents) {
            startParent.startChildren.add(this);
        }
    }

    public LinkedHashSet<Artifact> load() {
        LinkedHashSet<Artifact> loadList = new LinkedHashSet<Artifact>();
        loadInternal(loadList);
        userLoaded = true;
        return loadList;
    }

    private void loadInternal(LinkedHashSet<Artifact> loadList) {
        // visit all unloaded parents
        for (ConfigurationStatus parent : loadParents) {
            if (!parent.isLoaded()) {
                parent.loadInternal(loadList);
            }
        }

        if (!loaded) {
            loadList.add(configurationId);
            loaded = true;
        }
    }


    public LinkedHashSet<Artifact> start() {
        if (!loaded) {
            throw new IllegalStateException(configurationId + " is not loaded");
        }
        LinkedHashSet<Artifact> startList = new LinkedHashSet<Artifact>();
        startInternal(startList);
        userLoaded = true;
        userStarted = true;
        return startList;
    }

    private void startInternal(LinkedHashSet<Artifact> startList) {
        // visit all stopped parents
        for (ConfigurationStatus parent : startParents) {
            if (!parent.isStarted()) {
                parent.startInternal(startList);
            }
        }

        if (!started) {
            startList.add(configurationId);
            started = true;
        }
    }

    /**
     * Stop this configuration and its children (if it's running) or do nothing
     * (if it's not running).
     * @param gc whether to gc (??)
     * @return list of Artifacts for stopped configurations
     */
    public LinkedHashSet<Artifact> stop(boolean gc) {
        LinkedHashSet<ConfigurationStatus> stopStatuses = new LinkedHashSet<ConfigurationStatus>();
        stopInternal(stopStatuses, gc);

        LinkedHashSet<Artifact> stopIds = new LinkedHashSet<Artifact>(stopStatuses.size());
        for (ConfigurationStatus configurationStatus : stopStatuses) {
            stopIds.add(configurationStatus.configurationId);
        }

        return stopIds;
    }

    private void stopInternal(LinkedHashSet<ConfigurationStatus> stopList, boolean gc) {
        // if we aren't started, there is nothing to do
        if (!started) {
            return;
        }

        // visit all children
        for (ConfigurationStatus child : startChildren) {
            if (child.isStarted()) {
                child.stopInternal(stopList, gc);
            }
        }

        // mark this node as stoped, and add this node to the stop list
        if (started) {
            started = false;
            userStarted = false;
            stopList.add(this);

            // if we are garbage collecting, visit parents
            if (gc) {
                // visit all non-user started parents that haven't already been visited
                for (ConfigurationStatus parent : startParents) {
                    if (!parent.isUserStarted() && stopList.containsAll(parent.startChildren)) {
                        parent.stopInternal(stopList, gc);
                    }
                }
            }
        }
    }

    public LinkedHashSet<Artifact> restart() {
        if (!started) {
            throw new IllegalStateException(configurationId + " is not started");
        }

        LinkedHashSet<ConfigurationStatus> restartStatuses = new LinkedHashSet<ConfigurationStatus>();
        restartInternal(restartStatuses);

        LinkedHashSet<Artifact> restartIds = new LinkedHashSet<Artifact>(restartStatuses.size());
        for (ConfigurationStatus configurationStatus : restartStatuses) {
            restartIds.add(configurationStatus.configurationId);
        }

        userLoaded = true;
        userStarted = true;
        return restartIds;
    }

    private void restartInternal(LinkedHashSet<ConfigurationStatus> restartList) {
        // if we aren't started, there is nothing to do
        if (!started) {
            return;
        }

        // visit all children
        for (ConfigurationStatus child : startChildren) {
            if (child.isStarted()) {
                child.restartInternal(restartList);
            }
        }

        // add this node to the restart list
        restartList.add(this);
    }

    /**
     * Unload the configuration and all its children (if it's loaded), or do
     * nothing (if it's not loaded).
     * @param gc whether to gc (??)
     * @return artifacts for unloaded configurations
     */
    public LinkedHashSet<Artifact> unload(boolean gc) {

        LinkedHashSet<ConfigurationStatus> unloadStatuses = new LinkedHashSet<ConfigurationStatus>();
        unloadInternal(unloadStatuses, gc);

        LinkedHashSet<Artifact> unloadIds = new LinkedHashSet<Artifact>(unloadStatuses.size());
        for (ConfigurationStatus configurationStatus : unloadStatuses) {
            unloadIds.add(configurationStatus.configurationId);
        }

        return unloadIds;
    }

    private void unloadInternal(LinkedHashSet<ConfigurationStatus> unloadList, boolean gc) {
        // if we aren't loaded, there is nothing to do
        if (!loaded) {
            return;
        }

        // visit all loaded children
        for (ConfigurationStatus child : loadChildren) {
            if (child.isLoaded()) {
                child.unloadInternal(unloadList, gc);
            }
        }

        // mark this node as unloaded, and add this node to the unload list
        if (loaded) {
            started = false;
            userStarted = false;
            loaded = false;
            userLoaded = false;
            unloadList.add(this);

            // if we are garbage collecting, visit parents
            if (gc) {
                // visit all non-user loaded parents
                for (ConfigurationStatus parent : loadParents) {
                    if (!parent.isUserLoaded() && unloadList.containsAll(parent.loadChildren)) {
                        parent.unloadInternal(unloadList, gc);
                    }
                }
            }
        }
    }

    public LinkedHashSet<Artifact> reload() {
        if (!loaded) {
            throw new IllegalStateException(configurationId + " is not loaded");
        }

        LinkedHashSet<ConfigurationStatus> reloadStatuses = new LinkedHashSet<ConfigurationStatus>();
        reloadInternal(reloadStatuses);

        LinkedHashSet<Artifact> reloadIds = new LinkedHashSet<Artifact>(reloadStatuses.size());
        for (ConfigurationStatus configurationStatus : reloadStatuses) {
            reloadIds.add(configurationStatus.configurationId);
        }

        userLoaded = true;
        return reloadIds;
    }

    private void reloadInternal(LinkedHashSet<ConfigurationStatus> reloadList) {
        // if we aren't loaded, there is nothing to do
        if (!loaded) {
            return;
        }

        // visit all children
        for (ConfigurationStatus child : loadChildren) {
            if (child.isLoaded()) {
                child.reloadInternal(reloadList);
            }
        }

        // add this node to the reload list
        reloadList.add(this);
    }

    public String toString() {
        String load;
        if (userLoaded) {
            load = "user-loaded";
        } else if (loaded) {
            load = "loaded";
        } else {
            load = "not-loaded";
        }
        String start;
        if (userStarted) {
            start = "user-started";
        } else if (started) {
            start = "started";
        } else {
            start = "not-started";
        }
        return "[" + configurationId + " " + load + " " + start + "]";
    }
}
