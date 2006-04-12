/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.io.Serializable;

import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class ConfigurationInfo implements Serializable {
    private static final long serialVersionUID = 576134736036202445L;
    private final Artifact configID;
    private final ConfigurationModuleType type;
    private final AbstractName storeName;
    private final Artifact parentID;
    private final State state;

    public ConfigurationInfo(AbstractName storeName, Artifact configID, ConfigurationModuleType type) {
        this.configID = configID;
        this.type = type;
        state = null;
        this.storeName = storeName;
        this.parentID = null;
    }

    public ConfigurationInfo(AbstractName storeName, Artifact configID, State state, ConfigurationModuleType type) {
        this.configID = configID;
        this.state = state;
        this.type = type;
        this.storeName = storeName;
        this.parentID = null;
    }

    public ConfigurationInfo(AbstractName storeName, Artifact configID, State state, ConfigurationModuleType type, Artifact parentID) {
        this.configID = configID;
        this.state = state;
        this.type = type;
        this.storeName = storeName;
        this.parentID = parentID;
    }

    public Artifact getConfigID() {
        return configID;
    }

    public State getState() {
        return state;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

    public AbstractName getStoreName() {
        return storeName;
    }

    public Artifact getParentID() {
        return parentID;
    }
}
