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

package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.repository.Repository;

/**
 * Interface to a store for Configurations.
 *
 * @version $Rev$ $Date$
 */
public interface ConfigurationStore {
    
    /**
     * Determines if the identified configuration is an in-place one. This 
     * means that the configuration store only stores some meta-data and the 
     * actual content of the configuration is rooted somewhere else.
     * 
     * @param configId the unique ID of the configuration, which must be fully
     *                 resolved (isResolved() == true)
     *
     * @return true if the identified configuration is an in-place one.
     *
     * @throws NoSuchConfigException if the configuration is not contained in
     *                               the store
     * @throws IOException If the store cannot be read.
     */
    boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException;
    
    /**
     * Move the unpacked configuration directory into this store
     *
     * @param configurationData the configuration data
     * @throws IOException if the direcotyr could not be moved into the store
     * @throws InvalidConfigException if there is a configuration problem within the source direcotry
     */
    void install(ConfigurationData configurationData) throws IOException, InvalidConfigException;

    /**
     * Removes a configuration from the store
     *
     * @param configId the id of the configuration to remove, which must be
     *                 fully resolved (isResolved() == true)
     *
     * @throws NoSuchConfigException if the configuration is not contained in the store
     * @throws IOException if a problem occurs during the removal
     */
    void uninstall(Artifact configId) throws NoSuchConfigException, IOException;

    /**
     * Loads the specified configuration into the kernel
     *
     * @param configId the id of the configuration to load, which must be fully
     *                 resolved (isResolved() == true)
     *
     * @return the the configuration object
     *
     * @throws NoSuchConfigException if the configuration is not contained in the kernel
     * @throws IOException if a problem occurs loading the configuration from the store
     * @throws InvalidConfigException if the configuration is corrupt
     */
    ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Determines if the store contains a configuration with the specified ID.
     * The configuration need not be loaded or running, this just checks
     * whether the configuration store has the data for it.
     *
     * @param configId the unique ID of the configuration, which must be fully
     *                 resolved (isResolved() == true)
     *
     * @return true if the store contains the configuration
     */
    boolean containsConfiguration(Artifact configId);

    /**
     * Return the object name for the store.
     *
     * @return the object name for the store
     */
    String getObjectName();

    /**
     * Return the object name for the store.
     *
     * @return the object name for the store
     */
    AbstractName getAbstractName();

    /**
     * Return the configurations in the store
     *
     * @return a List (with entries of type ConfigurationInfo) of all the
     *         configurations contained in this configuration store
     */
    List<ConfigurationInfo> listConfigurations();

    /**
     * Creates an empty directory for a new configuration with the specified configId
     *
     * @param configId the unique ID of the configuration, which must be fully
     *                 resolved (isResolved() == true)
     *
     * @return the location of the new directory
     * 
     * @throws ConfigurationAlreadyExistsException if the configuration already exists in this store
     */
    File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException;

    /**
     * Locate the physical locations which match the supplied path in the given
     * artifact/module.  The path may be an Ant-style pattern.
     *
     * @param configId    the artifact to search, which must be fully resolved
     *                    (isResolved() == true)
     * @param moduleName  the module name or null to search in the top-level
     *                    artifact location
     * @param path        the pattern to search for within the artifact/module,
     *                    which may also be null to identify the artifact or
     *                    module base path
     *
     * @return a Set (with entries of type URL) of the matching locations
     */
    Set<URL> resolve(Artifact configId, String moduleName, String path) throws NoSuchConfigException, MalformedURLException;

    /**
     * Exports a configuration as a ZIP file.
     *
     * @param configId  The unique ID of the configuration to export, which
     *                  must be fully resolved (isResolved() == true)
     * @param output    The stream to write the ZIP content to
     */
    void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException;
}
