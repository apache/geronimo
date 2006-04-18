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

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.io.Serializable;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * This class contains the results of a lifecycle operation on the configuation manager.
 * @version $Rev$ $Date$
 */
public class LifecycleResults implements Serializable {
    private static final long serialVersionUID = 4660197333193740244L;
    private final Set loaded = new LinkedHashSet();
    private final Set unloaded = new LinkedHashSet();
    private final Set started = new LinkedHashSet();
    private final Set stopped = new LinkedHashSet();
    private final Set restarted = new LinkedHashSet();
    private final Set reloaded = new LinkedHashSet();
    private final Map failed = new LinkedHashMap();

    /**
     * Was the specified configuration loaded.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration was loaded during the lifecycle operation
     */
    public boolean wasLoaded(Artifact configurationId) {
        return loaded.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations loaded.
     * @return the configuration identifiers (Artifact)
     */
    public Set getLoaded() {
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
    public void setLoaded(Set loaded) {
        this.loaded.clear();
        this.loaded.addAll(loaded);
    }

    /**
     * Was the specified configuration unloaded.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration was unloaded during the lifecycle operation
     */
    public boolean wasUnloaded(Artifact configurationId) {
        return unloaded.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations unloaded.
     * @return the configuration identifiers (Artifact)
     */
    public Set getUnloaded() {
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
    public void setUnloaded(Set unloaded) {
        this.unloaded.clear();
        this.unloaded.addAll(unloaded);
    }

    /**
     * Was the specified configuration started.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration was started during the lifecycle operation
     */
    public boolean wasStarted(Artifact configurationId) {
        return started.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations started.
     * @return the configuration identifiers (Artifact)
     */
    public Set getStarted() {
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
    public void setStarted(Set started) {
        this.started.clear();
        this.started.addAll(started);
    }

    /**
     * Was the specified configuration stopped.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration was stopped during the lifecycle operation
     */
    public boolean wasStopped(Artifact configurationId) {
        return stopped.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations stopped.
     * @return the configuration identifiers (Artifact)
     */
    public Set getStopped() {
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
    public void setStopped(Set stopped) {
        this.stopped.clear();
        this.stopped.addAll(stopped);
    }

    /**
     * Was the specified configuration restarted.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration was restarted during the lifecycle operation
     */
    public boolean wasRestarted(Artifact configurationId) {
        return restarted.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations restarted.
     * @return the configuration identifiers (Artifact)
     */
    public Set getRestarted() {
        return Collections.unmodifiableSet(restarted);
    }

    /**
     * Adds a configuration the set of restarted configurations.
     * @param configurationId the configuration identifiers (Artifact)
     */
    public void addRestarted(Artifact configurationId) {
        restarted.add(configurationId);
    }

    /**
     * Clears the existing restarted set and add alls the specified configurations to the set
     * @param restarted the configuration identifiers (Artifact)
     */
    public void setRestarted(Set restarted) {
        this.restarted.clear();
        this.restarted.addAll(restarted);
    }

    /**
     * Was the specified configuration reloaded.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration was reloaded during the lifecycle operation
     */
    public boolean wasReloaded(Artifact configurationId) {
        return reloaded.contains(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations reloaded.
     * @return the configuration identifiers (Artifact)
     */
    public Set getReloaded() {
        return Collections.unmodifiableSet(reloaded);
    }

    /**
     * Adds a configuration the set of reloaded configurations.
     * @param configurationId the configuration identifiers (Artifact)
     */
    public void addReloaded(Artifact configurationId) {
        reloaded.add(configurationId);
    }

    /**
     * Clears the existing reloaded set and add alls the specified configurations to the set
     * @param reloaded the configuration identifiers (Artifact)
     */
    public void setReloaded(Set reloaded) {
        this.reloaded.clear();
        this.reloaded.addAll(reloaded);
    }

    /**
     * Was the specified configuration failed the operation and threw an exception.
     * @param configurationId the configuration identifier
     * @return true if the specified configuration failed the operation and threw an exception during the lifecycle operation
     */
    public boolean wasFailed(Artifact configurationId) {
        return failed.containsKey(configurationId);
    }

    /**
     * Gets the exception that caused the operation on the specified configuration to fail.
     * @return the configuration identifiers (Artifact)
     */
    public Throwable getFailedCause(Artifact configurationId) {
        return (Throwable) failed.get(configurationId);
    }

    /**
     * Gets the configuration identifiers (Artifact) of the configurations that failed the operation and threw an exception.
     * @return the configuration identifiers (Artifact)
     */
    public Map getFailed() {
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
    public void setFailed(Map failed) {
        this.failed.clear();
        this.failed.putAll(failed);
    }
}
