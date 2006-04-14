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
import java.util.List;
import javax.management.ObjectName;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

/**
 * Encapsulates logic for dealing with configurations.
 *
 * @version $Rev$ $Date$
 */
public interface ConfigurationManager {
    /**
     * Is the specified configuration loaded into the kernel?
     *
     * @param configID the name of the configuration
     *
     * @return true if the configuration has been loaded; false otherwise
     */
    boolean isLoaded(Artifact configID);

    /**
     * Return a list of the stores this manager knows about.
     * @return a List (with elements of type AbstractName) of the stores this manager controls
     */
    List listStores();

    /**
     * Get all the ConfigurationStores known to this manager at present
     */
    ConfigurationStore[] getStores();

    /**
     * Gets the configuration store responsible for the specified
     * configuration, or null if there is none.
     */
    ConfigurationStore getStoreForConfiguration(Artifact configuration);

    /**
     * Return a list of the configurations in a specific store.
     *
     * @param store the store to list
     *
     * @return a List (with elements of type ConfigurationInfo) of all the configurations in the store
     *
     * @throws NoSuchStoreException if the store could not be located
     */
    List listConfigurations(AbstractName store) throws NoSuchStoreException;

    /**
     * Is the specified artifact a configuration that has been loaded in the kernel?
     *
     * @param artifact the artifact to check
     *
     * @return true if the artifact is a configuration and has been loaded
     */
    boolean isConfiguration(Artifact artifact);

    /**
     * Gets a loaded Configuration (does not see unloaded configurations).
     *
     * @param configId the configuration to get
     *
     * @return the specified configuration or null if the configuration has not been loaded
     */
    Configuration getConfiguration(Artifact configId);

    void loadConfiguration(Artifact configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    Configuration loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, IOException, InvalidConfigException;

    void unloadConfiguration(Artifact configID) throws NoSuchConfigException;

    void unloadConfiguration(Configuration configuration) throws NoSuchConfigException;

    void startConfiguration(Artifact configID) throws InvalidConfigException;

    void startConfiguration(Configuration configuration) throws InvalidConfigException;

    void stopConfiguration(Artifact configID) throws NoSuchConfigException;

    void stopConfiguration(Configuration configuration) throws NoSuchConfigException;

    boolean isRunning(Artifact configurationId);

    List listConfigurations();

    void uninstallConfiguration(Artifact configId) throws IOException, NoSuchConfigException;
}
