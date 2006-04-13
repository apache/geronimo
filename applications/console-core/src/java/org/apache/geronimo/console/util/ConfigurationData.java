/**
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.console.util;

import java.io.Serializable;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.gbean.AbstractName;

/**
 * Standard metadata about a configuration
 *
 * @version $Rev: 355877 $ $Date: 2005-12-10 21:48:27 -0500 (Sat, 10 Dec 2005) $
 */
public class ConfigurationData implements Serializable, Comparable {
    private final State state;
    private final AbstractName parentName;
    private final String childName;
    private final ConfigurationModuleType type;
    private final AbstractName moduleBeanName;

    public ConfigurationData(AbstractName parentName, String childName, State state, ConfigurationModuleType type, AbstractName moduleBeanName) {
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
