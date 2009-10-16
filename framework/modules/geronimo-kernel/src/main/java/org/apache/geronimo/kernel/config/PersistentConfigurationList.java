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

import org.apache.geronimo.kernel.repository.Artifact;

import java.io.IOException;
import java.util.List;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public interface PersistentConfigurationList {

    static final String PERSISTENT_CONFIGURATION_LIST = "PersistentConfigurationList";
    
    boolean isKernelFullyStarted();

    void setKernelFullyStarted(boolean kernelFullyStarted);

    void save() throws IOException;

    List<Artifact> restore() throws IOException;

    /**
     * Adds a configuration to the list, but does not mark it as started.
     */
    void addConfiguration(Artifact configName);

    /**
     * Indicates that the configuration should be started when the server is
     * started.  The configuration should have been previously added with
     * addConfiguration.
     */
    void startConfiguration(Artifact configName);

    /**
     * Indicates that the configuration should not be started when the
     * server is started.  The configuration should have been previously added
     * with addConfiguration (and presumably started with startConfiguration).
     */
    void stopConfiguration(Artifact configName);

    /**
     * Removes all record of the specified configuration from the configuration
     * list.  This is somewhat unusual -- normally you want to remember the
     * settings in case the configuration is deployed again later.
     */
    void removeConfiguration(Artifact configName);

    /**
     * Gets all configurations in the list matching the specified query,
     * whether they are marked at starting or not.
     * 
     * @param query The artifact to search for, normally not fully resolved
     *              so there may be multiple matches or matches that are not
     *              exactly equal to the argument.
     *
     * @return The matching artifacts that have data in the config list.
     */
    Artifact[] getListedConfigurations(Artifact query);

    /**
     * Migrates settings from an old version of a configuration to a newer
     * version of the configuration.  Used when an updated version is deployed
     * with a newer version number in the name, but the settings used for the
     * previous version should be carried forward.
     *
     * @param oldName        The name that the existing settings are under
     * @param newName        The name to move the settings to
     * @param configuration  The configuration itself, which can be used to
     *                       verify that all the settings are still valid as
     *                       they are migrated.
     */
    void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration);

    /**
     * This method checks if there are any custom gbean attributes in the configuration.
     *
     * @param configName Name of the configuration
     * @return true if the configuration contains any custom gbean attributes
     */
    boolean hasGBeanAttributes(Artifact configName);
}
