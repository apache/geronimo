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
package org.apache.geronimo.console.util;

import java.io.Serializable;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

/**
 * Standard metadata about a configuration
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationData implements Serializable, Comparable {
    private final Artifact configID;
    private final State state;
    private final AbstractName parentName;
    private final String childName;
    private final ConfigurationModuleType type;
    private final AbstractName moduleBeanName;

    public ConfigurationData(Artifact configID, AbstractName parentName, String childName, State state, ConfigurationModuleType type, AbstractName moduleBeanName) {
        this.configID = configID;
        this.childName = childName;
        this.parentName = parentName;
        this.state = state;
        this.type = type;
        this.moduleBeanName = moduleBeanName;
    }

    public boolean isChild() {
        return childName != null;
    }

    public String getChildName() {
        return childName;
    }

    public AbstractName getModuleBeanName() {
        return moduleBeanName;
    }

    public AbstractName getParentName() {
        return parentName;
    }

    public State getState() {
        return state;
    }

    public ConfigurationModuleType getType() {
        return type;
    }

    public Artifact getConfigID() {
        return configID;
    }

    public boolean isRunning() {
        return state.toInt() == State.RUNNING_INDEX;
    }

    public int compareTo(Object o) {
        ConfigurationData other = (ConfigurationData) o;
        int test = getParentName().toString().compareTo(other.getParentName().toString());
        if(test == 0) {
            if(getChildName() != null && other.getChildName() != null) {
                return getChildName().compareTo(other.getChildName());
            } else if(getChildName() == null && other.getChildName() == null) {
                return 0;
            } else return getChildName() == null ? 1 : -1;
        } else return test;
    }
}
