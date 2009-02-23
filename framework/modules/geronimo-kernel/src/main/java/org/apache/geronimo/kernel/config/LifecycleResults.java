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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * This class contains the results of a lifecycle operation on the configuation manager.
 * @version $Rev$ $Date$
 */
public class LifecycleResults implements Serializable {
    private static final long serialVersionUID = 4660197333193740244L;
    private final Set<Artifact> loaded = new LinkedHashSet<Artifact>();
    private final Set<Artifact> unloaded = new LinkedHashSet<Artifact>();
    private final Set<Artifact> started = new LinkedHashSet<Artifact>();
    private final Set<Artifact> stopped = new LinkedHashSet<Artifact>();
    private final Map<Artifact, Throwable> failed = new LinkedHashMap<Artifact, Throwable>();

    /**
     * Checks whether the specified configuration was loaded.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return true if the specified configuration was loaded during the lifecycle operation
     */
    public boolean wasLoaded(Artifact configurationId) {
        return loaded.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations loaded.
     * @return the configuration identifiers (Artifact)
     */
    public Set<Artifact> getLoaded() {
        return Collections.unmodifiableSet(loaded);
    }

    /**
     * Adds a configuration the set of loaded configurations.
     * @param configurationId the configuration identifiers (Artifact)
     */
    public void addLoaded(Artifact configurationId) {
        loaded.add(configurationId);
    }

    /**
     * Clears the existing loaded set and add alls the specified configurations to the set
     * @param loaded the configuration identifiers (Artifact)
     */
    public void setLoaded(Set<Artifact> loaded) {
        this.loaded.clear();
        this.loaded.addAll(loaded);
    }

    /**
     * Checks whether the specified configuration was unloaded.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return true if the specified configuration was unloaded during the lifecycle operation
     */
    public boolean wasUnloaded(Artifact configurationId) {
        return unloaded.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations unloaded.
     * @return the configuration identifiers (Artifact)
     */
    public Set<Artifact> getUnloaded() {
        return Collections.unmodifiableSet(unloaded);
    }

    /**
     * Adds a configuration the set of unloaded configurations.
     * @param configurationId the configuration identifiers (Artifact)
     */
    public void addUnloaded(Artifact configurationId) {
        unloaded.add(configurationId);
    }

    /**
     * Clears the existing unloaded set and add alls the specified configurations to the set
     * @param unloaded the configuration identifiers (Artifact)
     */
    public void setUnloaded(Set<Artifact> unloaded) {
        this.unloaded.clear();
        this.unloaded.addAll(unloaded);
    }

    /**
     * Checks whether the specified configuration was started.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return true if the specified configuration was started during the lifecycle operation
     */
    public boolean wasStarted(Artifact configurationId) {
        return started.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations started.
     * @return the configuration identifiers (Artifact)
     */
    public Set<Artifact> getStarted() {
        return Collections.unmodifiableSet(started);
    }

    /**
     * Adds a configuration the set of started configurations.
     * @param configurationId the configuration identifiers (Artifact)
     */
    public void addStarted(Artifact configurationId) {
        started.add(configurationId);
    }

    /**
     * Clears the existing started set and add alls the specified configurations to the set
     * @param started the configuration identifiers (Artifact)
     */
    public void setStarted(Set<Artifact> started) {
        this.started.clear();
        this.started.addAll(started);
    }

    /**
     * Checks whether the specified configuration was stopped.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return true if the specified configuration was stopped during the lifecycle operation
     */
    public boolean wasStopped(Artifact configurationId) {
        return stopped.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations stopped.
     * @return the configuration identifiers (Artifact)
     */
    public Set<Artifact> getStopped() {
        return Collections.unmodifiableSet(stopped);
    }

    /**
     * Adds a configuration the set of stopped configurations.
     * @param configurationId the configuration identifiers (Artifact)
     */
    public void addStopped(Artifact configurationId) {
        stopped.add(configurationId);
    }

    /**
     * Clears the existing stopped set and add alls the specified configurations to the set
     * @param stopped the configuration identifiers (Artifact)
     */
    public void setStopped(Set<Artifact> stopped) {
        this.stopped.clear();
        this.stopped.addAll(stopped);
    }

    /**
     * Was the specified configuration failed the operation and threw an
     * exception.
     *
     * @param configurationId the configuration identifier.  May be a partial
     *                        ID, in which case will check whether any
     *                        matching conifguration failed.
     *
     * @return true if the specified (or any matching) configuration failed
     *              the operation and threw an exception during the lifecycle
     *              operation
     */
    public boolean wasFailed(Artifact configurationId) {
        for (Artifact failID : failed.keySet()) {
            if (configurationId.matches(failID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the exception that caused the operation on the specified configuration to fail.
     * @param configurationId id for artifact we are asking about
     * @return the configuration identifiers (Artifact)
     */
    public Throwable getFailedCause(Artifact configurationId) {
        return failed.get(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations that failed the operation and threw an exception.
     * @return the configuration identifiers (Artifact)
     */
    public Map<Artifact, Throwable> getFailed() {
        return Collections.unmodifiableMap(failed);
    }

    /**
     * Adds a configuration and associated causal exception to this result.
     * @param configurationId the configuration identifiers (Artifact)
     * @param cause the exception that caused the operation on the specified configuration to fail
     */
    public void addFailed(Artifact configurationId, Throwable cause) {
        failed.put(configurationId, cause);
    }

    /**
     * Clears the existing failed map and add alls the specified configurations to the map
     * @param failed a map from configuration identifier (Artifact) to causal exception
     */
    public void setFailed(Map<Artifact, Throwable> failed) {
        this.failed.clear();
        this.failed.putAll(failed);
    }
}
