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
import java.net.URI;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.management.State;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/07/22 03:22:53 $
 */
public class ConfigurationInfo implements Serializable {
    private final ObjectName storeName;
    private final URI configID;
    private final State state;
    private final ConfigurationModuleType type;

    public ConfigurationInfo(ObjectName storeName, URI configID, State state, ConfigurationModuleType type) {
        this.storeName = storeName;
        this.configID = configID;
        this.state = state;
        this.type = type;
    }

    public ObjectName getStoreName() {
        return storeName;
    }

    public URI getConfigID() {
        return configID;
    }

    public State getState() {
        return state;
    }
    
    public ConfigurationModuleType getType() {
        return type;
    }
    
}
