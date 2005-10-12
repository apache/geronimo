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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Collection;
import javax.management.ObjectName;

/**
 * @version $Rev$ $Date$
 */
public interface ConfigurationManager {
    /**
     * Is the specified configuration loaded into the kernel?
     * @param configID the name of the configuration
     * @return true if the configuration has been loaded; false otherwise
     */
    boolean isLoaded(URI configID);

    /**
     * Return a list of the stores this manager knows about.
     * @return a List<ObjectName> of the stores this manager controls
     */
    List listStores();

    /**
     * Return a list of the configurations in a specific store.
     * @param store the store to list
     * @return a List<ConfigurationInfo> of all the configurations in the store
     * @throws NoSuchStoreException if the store could not be located
     */
    List listConfigurations(ObjectName store) throws NoSuchStoreException;

    /**
     * Load the specified configuration into the kernel.
     * @param configID the id of the configuration
     * @return the name of the new configuration object mounted into the kernel
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws IOException if there is a problem loading te configuration from the store
     * @throws InvalidConfigException if the configuration is corrupt
     */
    ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Load the specified configuration and all parent configurations into the kernel.
     * @param configID the id of the configuration
     * @return a list of names of configurations loaded into the kernel
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws IOException if there is a problem loading te configuration from the store
     * @throws InvalidConfigException if the configuration is corrupt
     */
    List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Unloads the specified configuration from the kernel
     * @param configID the name fo the configuration to remove
     * @throws NoSuchConfigException if the configuration is now loaded into the kernel
     */
    void unload(URI configID) throws NoSuchConfigException;

    void start(ObjectName configName) throws InvalidConfigException;

    void loadGBeans(ObjectName configName) throws InvalidConfigException;
}
