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

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 * Interface to a store for Configurations.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:01 $
 */
public interface ConfigurationStore {
    /**
     * Add the CAR at the supplied URL into this store
     * @param source the URL of a CAR format archive
     * @throws java.io.IOException if the CAR could not be read
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if there is a configuration problem with the CAR
     */
    void install(URL source) throws IOException, InvalidConfigException;

    /**
     * Determines if the store contains a configuration with the spedified ID.
     * @param configID the unique ID of the configuration
     * @return true if the store contains the configuration
     */
    boolean containsConfiguration(URI configID);

    /**
     * Return the Configuration GBean for the specified ID
     * @param id the unique ID of a Configuration
     * @return the GBeanMBean for that configuration
     * @throws org.apache.geronimo.kernel.config.NoSuchConfigException if the store does not contain a Configuration with that id
     * @throws java.io.IOException if there was a problem loading the Configuration from the store
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if the Configuration is invalid
     */
    GBeanMBean getConfiguration(URI id) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Return the base URL for the specified ID
     * @param id the unique ID for a Configuration
     * @return the URL of the base location for the Configuration that should be used for resolution
     * @throws org.apache.geronimo.kernel.config.NoSuchConfigException if the store does not contain a Configuration with that id
     */
    URL getBaseURL(URI id) throws NoSuchConfigException;
}
