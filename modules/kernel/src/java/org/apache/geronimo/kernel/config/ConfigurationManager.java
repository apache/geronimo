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
 *
 *
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
     * Load the specified configuration into the kernel. This does not start the configuration gbean
     * and thus does not load or start any gbeans in the configuration.
     *
     * @param configID the id of the configuration
     * @return the name of the new configuration object mounted into the kernel
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws IOException if there is a problem loading te configuration from the store
     * @throws InvalidConfigException if the configuration is corrupt
     */
    ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Load the specified configuration and all parent configurations into the kernel. This does not
     * start any configuration gbeans or load any gbeans from the configurations loaded.
     *
     * @param configID the id of the configuration
     * @return a list of names of configurations loaded into the kernel
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws IOException if there is a problem loading te configuration from the store
     * @throws InvalidConfigException if the configuration is corrupt
     */
    List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Unloads the gbeans of the specified configuration, stops the configuration gbean, and unloads the
     * configuration gbean from the kernel.  Stop should always be called first.
     *
     * @param configID the name fo the configuration to remove
     * @throws NoSuchConfigException if the configuration is now loaded into the kernel
     */
    void unload(URI configID) throws NoSuchConfigException;

    /**
     * Load the gbeans of the named configuration into the kernel, but do not start them.
     * This starts the configuration gbean.  You must have loaded the configuration using load(uri) or loadRecursive(uri)
     * before calling this method.
     *
     * @param configID
     * @throws InvalidConfigException
     */
    void loadGBeans(URI configID) throws InvalidConfigException;

    /**
     * Start the gbeans in this configuration.  You must have called loadGBeans before calling this method.
     *
     * @param configID
     * @throws InvalidConfigException
     */
    void start(URI configID) throws InvalidConfigException;

    /**
     * Stop the gbeans in this configuration, but do not stop the configuration gbean.
     *
     * @param configID
     * @throws InvalidConfigException
     */
    void stop(URI configID) throws InvalidConfigException;
}
