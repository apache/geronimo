/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.net.URI;
import javax.management.ObjectName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * A specialized ConfigurationManager that can change the set of GBeans
 * included in the configuration at runtime.
 *
 * @version $Rev$ $Date$
 */
public interface EditableConfigurationManager extends ConfigurationManager {
    /**
     * Adds a new GBean to an existing Configuration.
     * @param configID  The configuration to add the GBean to.
     * @param gbean     The data representing the GBean to add.
     * @param start     If true, the GBean should be started as part of this call.
     */
    void addGBeanToConfiguration(Artifact configID, GBeanData gbean, boolean start) throws InvalidConfigException;

    /**
     * Removes a GBean from a configuration.  Note: this may simply mark it to
     * not be loaded in the future, as opposed to actually removing it from
     * the data in the config store.
     * @param configID  The configuration to remove the GBean from.
     * @param gbean     The ObjectName of the GBean to remove.
     */
    void removeGBeanFromConfiguration(Artifact configID, ObjectName gbean) throws InvalidConfigException, GBeanNotFoundException;
}
