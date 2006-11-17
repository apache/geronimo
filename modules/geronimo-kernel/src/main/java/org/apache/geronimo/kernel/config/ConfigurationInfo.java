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
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class ConfigurationInfo implements Serializable {
    private static final long serialVersionUID = -16555213664245560L;
    private final AbstractName storeName;
    private final Artifact configID;
    private final ConfigurationModuleType type;
    private final long created;
    private final File inPlaceLocation;
    private final Set ownedConfigurations = new LinkedHashSet();
    private final Set childConfigurations = new LinkedHashSet();
    private final State state;
    private final Artifact parentID;

    public ConfigurationInfo(AbstractName storeName, Artifact configID, ConfigurationModuleType type, long created, Set ownedConfigurations, Set childConfigurations, File inPlaceLocation) {
        this(storeName, configID, type, created, ownedConfigurations, childConfigurations, inPlaceLocation, null, null);
    }

    public ConfigurationInfo(AbstractName storeName, Artifact configID, ConfigurationModuleType type, long created, Set ownedConfigurations, Set childConfigurations, File inPlaceLocation, State state) {
        this(storeName, configID, type, created, ownedConfigurations, childConfigurations, inPlaceLocation, state, null);
    }

    public ConfigurationInfo(AbstractName storeName, Artifact configID, ConfigurationModuleType type, long created, Set ownedConfigurations, Set childConfigurations, File inPlaceLocation, State state, Artifact parentID) {
        this.storeName = storeName;
        this.configID = configID;
        this.type = type;
        this.created = created;
        this.inPlaceLocation = inPlaceLocation;
        if (ownedConfigurations != null) {
            this.ownedConfigurations.addAll(ownedConfigurations);
        }
        if (childConfigurations != null) {
            this.childConfigurations.addAll(childConfigurations);
        }
        this.state = state;
        this.parentID = parentID;
    }

    public AbstractName getStoreName() {
        return storeName;
    }

    public Artifact getConfigID() {
        return configID;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

    public long getCreated() {
        return created;
    }

    public File getInPlaceLocation() {
        return inPlaceLocation;
    }

    public Set getOwnedConfigurations() {
        return ownedConfigurations;
    }

    public Set getChildConfigurations() {
        return childConfigurations;
    }

    public State getState() {
        return state;
    }

    public Artifact getParentID() {
        return parentID;
    }
}
