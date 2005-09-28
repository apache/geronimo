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

/**
 * Configuration types.
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationModuleType implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4121586344416418391L;

    public static final ConfigurationModuleType EAR = new ConfigurationModuleType("EAR", 0);

    public static final ConfigurationModuleType EJB = new ConfigurationModuleType("EJB", 1);

    public static final ConfigurationModuleType CAR = new ConfigurationModuleType("CAR", 2); // app client

    public static final ConfigurationModuleType RAR = new ConfigurationModuleType("RAR", 3);

    public static final ConfigurationModuleType WAR = new ConfigurationModuleType("WAR", 4);

    public static final ConfigurationModuleType SERVICE = new ConfigurationModuleType("SERVICE", 5);

    public static final ConfigurationModuleType SPR = new ConfigurationModuleType("SPR", 6);

    private static final ConfigurationModuleType[] fromInt = {EAR, EJB, CAR, RAR, WAR, SERVICE, SPR};

    private final String name;
    
    private final int value;

    public static ConfigurationModuleType getFromValue(int index) {
        if (index < 0 || index >= fromInt.length) {
            return null;
        }
        return fromInt[index];
    }

    public static ConfigurationModuleType getFromValue(Integer index) {
        return getFromValue(index.intValue());
    }

    /**
     * This constructor is intentionally public: this class is not a type-safe
     * enumeration.
     */
    public ConfigurationModuleType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }
    
    /**
     * Gets the identifier of this type. For a configuration associated to
     * a J2EE ModuleType, this value MUST be equal to ModuleType.getValue().
     * 
     * @return
     */
    public int getValue() {
        return value;
    }
    
    public String toString() {
        return name;
    }

    private Object readResolve() {
        return fromInt[value];
    }

}
