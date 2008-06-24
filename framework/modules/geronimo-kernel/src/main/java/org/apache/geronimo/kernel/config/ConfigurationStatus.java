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
import java.util.Iterator;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationStatus {
    private Artifact configurationId;
    private final Set loadParents = new LinkedHashSet();
    private final Set startParents = new LinkedHashSet();
    private final LinkedHashSet loadChildren = new LinkedHashSet();
    private final LinkedHashSet startChildren = new LinkedHashSet();
    private boolean loaded = false;
    private boolean started = false;
    private boolean userLoaded = false;
    private boolean userStarted = false;

    public ConfigurationStatus(Artifact configId, Set loadParents, Set startParents) {
        if (configId == null) throw new NullPointerException("configId is null");
        if (loadParents == null) throw new NullPointerException("loadParents is null");
        if (startParents == null) throw new NullPointerException("startParents is null");
        if (!loadParents.containsAll(startParents)) {
            throw new IllegalArgumentException("loadParents must contain all startParents");
        }
        this.configurationId = configId;
        this.loadParents.addAll(loadParents);
        this.startParents.addAll(startParents);

        for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus loadParent = (ConfigurationStatus) iterator.next();
            loadParent.loadChildren.add(this);
        }

        for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus startParent = (ConfigurationStatus) iterator.next();
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

        for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus loadParent = (ConfigurationStatus) iterator.next();
            loadParent.loadChildren.remove(this);
        }
        loadParents.clear();

        for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus startParent = (ConfigurationStatus) iterator.next();
            startParent.startChildren.remove(this);
        }
        startChildren.clear();
    }

    public Artifact getConfigurationId() {
        return configurationId;
    }
    
    public LinkedHashSet getStartedChildren() {
        LinkedHashSet childrenStatuses = new LinkedHashSet();
        getStartedChildrenInternal(childrenStatuses);

        LinkedHashSet childrenIds = new LinkedHashSet(childrenStatuses.size());
        for (Iterator iterator = childrenStatuses.iterator(); iterator.hasNext();) {
            ConfigurationStatus configurationStatus = (ConfigurationStatus) iterator.next();
            childrenIds.add(configurationStatus.configurationId);
        }

        return childrenIds;
    }

    private void getStartedChildrenInternal(LinkedHashSet childrenStatuses) {
        // if we aren't started, there is nothing to do
        if (!started) {
            return;
        }

        // visit all children
        for (Iterator iterator = startChildren.iterator(); iterator.hasNext();) {
            ConfigurationStatus child = (ConfigurationStatus) iterator.next();
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


    public void upgrade(Artifact newId, Set newLoadParents, Set newStartParents) {
        this.configurationId = newId;

        //
        // remove links from the current parents to me
        //
        for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus loadParent = (ConfigurationStatus) iterator.next();
            loadParent.loadChildren.remove(this);
        }
        loadParents.clear();

        for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus startParent = (ConfigurationStatus) iterator.next();
            startParent.startChildren.remove(this);
        }
        startChildren.clear();

        //
        // connect to to the new parents
        //
        this.loadParents.addAll(newLoadParents);
        this.startParents.addAll(newStartParents);

        for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus loadParent = (ConfigurationStatus) iterator.next();
            loadParent.loadChildren.add(this);
        }

        for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus startParent = (ConfigurationStatus) iterator.next();
            startParent.startChildren.add(this);
        }
    }

    public LinkedHashSet load() {
        LinkedHashSet loadList = new LinkedHashSet();
        loadInternal(loadList);
        userLoaded = true;
        return loadList;
    }

    private void loadInternal(LinkedHashSet loadList) {
        // visit all unloaded parents
        for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
            if (!parent.isLoaded()) {
                parent.loadInternal(loadList);
            }
        }

        if (!loaded) {
            loadList.add(configurationId);
            loaded = true;
        }
    }


    public LinkedHashSet start() {
        if (!loaded) {
            throw new IllegalStateException(configurationId + " is not loaded");
        }
        LinkedHashSet startList = new LinkedHashSet();
        startInternal(startList);
        userLoaded = true;
        userStarted = true;
        return startList;
    }

    private void startInternal(LinkedHashSet startList) {
        // visit all stopped parents
        for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
            ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
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
     */
    public LinkedHashSet stop(boolean gc) {
        LinkedHashSet stopStatuses = new LinkedHashSet();
        stopInternal(stopStatuses, gc);

        LinkedHashSet stopIds = new LinkedHashSet(stopStatuses.size());
        for (Iterator iterator = stopStatuses.iterator(); iterator.hasNext();) {
            ConfigurationStatus configurationStatus = (ConfigurationStatus) iterator.next();
            stopIds.add(configurationStatus.configurationId);
        }

        return stopIds;
    }

    private void stopInternal(LinkedHashSet stopList, boolean gc) {
        // if we aren't started, there is nothing to do
        if (!started) {
            return;
        }

        // visit all children
        for (Iterator iterator = startChildren.iterator(); iterator.hasNext();) {
            ConfigurationStatus child = (ConfigurationStatus) iterator.next();
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
                for (Iterator iterator = startParents.iterator(); iterator.hasNext();) {
                    ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                    if (!parent.isUserStarted() && stopList.containsAll(parent.startChildren)) {
                        parent.stopInternal(stopList, gc);
                    }
                }
            }
        }
    }

    public LinkedHashSet restart() {
        if (!started) {
            throw new IllegalStateException(configurationId + " is not started");
        }

        LinkedHashSet restartStatuses = new LinkedHashSet();
        restartInternal(restartStatuses);

        LinkedHashSet restartIds = new LinkedHashSet(restartStatuses.size());
        for (Iterator iterator = restartStatuses.iterator(); iterator.hasNext();) {
            ConfigurationStatus configurationStatus = (ConfigurationStatus) iterator.next();
            restartIds.add(configurationStatus.configurationId);
        }

        userLoaded = true;
        userStarted = true;
        return restartIds;
    }

    private void restartInternal(LinkedHashSet restartList) {
        // if we aren't started, there is nothing to do
        if (!started) {
            return;
        }

        // visit all children
        for (Iterator iterator = startChildren.iterator(); iterator.hasNext();) {
            ConfigurationStatus child = (ConfigurationStatus) iterator.next();
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
     */
    public LinkedHashSet unload(boolean gc) {

        LinkedHashSet unloadStatuses = new LinkedHashSet();
        unloadInternal(unloadStatuses, gc);

        LinkedHashSet unloadIds = new LinkedHashSet(unloadStatuses.size());
        for (Iterator iterator = unloadStatuses.iterator(); iterator.hasNext();) {
            ConfigurationStatus configurationStatus = (ConfigurationStatus) iterator.next();
            unloadIds.add(configurationStatus.configurationId);
        }

        return unloadIds;
    }

    private void unloadInternal(LinkedHashSet unloadList, boolean gc) {
        // if we aren't loaded, there is nothing to do
        if (!loaded) {
            return;
        }

        // visit all loaded children
        for (Iterator iterator = loadChildren.iterator(); iterator.hasNext();) {
            ConfigurationStatus child = (ConfigurationStatus) iterator.next();
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
                for (Iterator iterator = loadParents.iterator(); iterator.hasNext();) {
                    ConfigurationStatus parent = (ConfigurationStatus) iterator.next();
                    if (!parent.isUserLoaded() && unloadList.containsAll(parent.loadChildren)) {
                        parent.unloadInternal(unloadList, gc);
                    }
                }
            }
        }
    }

    public LinkedHashSet reload() {
        if (!loaded) {
            throw new IllegalStateException(configurationId + " is not loaded");
        }

        LinkedHashSet reloadStatuses = new LinkedHashSet();
        reloadInternal(reloadStatuses);

        LinkedHashSet reloadIds = new LinkedHashSet(reloadStatuses.size());
        for (Iterator iterator = reloadStatuses.iterator(); iterator.hasNext();) {
            ConfigurationStatus configurationStatus = (ConfigurationStatus) iterator.next();
            reloadIds.add(configurationStatus.configurationId);
        }

        userLoaded = true;
        return reloadIds;
    }

    private void reloadInternal(LinkedHashSet reloadList) {
        // if we aren't loaded, there is nothing to do
        if (!loaded) {
            return;
        }

        // visit all children
        for (Iterator iterator = loadChildren.iterator(); iterator.hasNext();) {
            ConfigurationStatus child = (ConfigurationStatus) iterator.next();
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
        if (userLoaded) {
            start = "user-started";
        } else if (loaded) {
            start = "started";
        } else {
            start = "not-started";
        }
        return "[" + configurationId + " " + load + " " + start + "]";
    }
}
