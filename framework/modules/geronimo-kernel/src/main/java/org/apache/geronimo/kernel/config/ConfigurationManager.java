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

import java.io.IOException;
import java.util.List;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.gbean.AbstractName;
import org.osgi.framework.Bundle;

/**
 * Encapsulates logic for dealing with configurations.
 *
 * Configurations have a lifecycle with three states: installed, loaded, and
 * running.  Installed means that the configuration is present in the server's
 * repository.  Loaded means that the Configuration GBean (including the
 * configuration's ClassLoader) is running.  Running means that all the GBeans
 * in the Configuration are running.
 *
 * From a user perspective, there's not much difference between installed and
 * loaded if the configuration has not been started (it still shows up as not
 * running).  However, certain operations will cause a configuration to be
 * loaded but not started.  For example, if ModuleA depends on ModuleB, then
 * when ModuleA is distributed ModuleB will normally be loaded (to fire up the
 * ClassLoader and validate ModuleA).  But ModuleB will not be started at that
 * point.  It can be started manually or it will be started automatically when
 * ModuleA is started.
 *
 * When a Configuration is not loaded, only its ConfigurationData is available
 * for inspection.  It's normally not possible to inspect the GBeans in the
 * configuration because there's no ClassLoader that could be used to load the
 * classes needed by the GBeanDatas in the configuration.  Once the
 * configuration has been loaded, it's ClassLoader is available so the
 * GBeanDatas can be loaded and inspected.  But the GBean instances are not
 * instantiated and started until the configuration is started.
 *
 * @version $Rev$ $Date$
 */
public interface ConfigurationManager extends ConfigurationSource {
    /**
     * Is the specified configuration installed into the server
     * environment?  That is, does it exist in the configuration store,
     * regardless of whether it's loaded or running?  Note that this
     * always returns false if the argument does not represent a
     * configuration (e.g. if it's for a plain JAR).
     *
     * @param configurationId the configuration identifier, which must be
     *                        fully resolved (isResolved() == true)
     *
     * @return true if the configuration has been loaded; false otherwise
     */
    boolean isInstalled(Artifact configurationId);

    /**
     * Is the specified configuration loaded into the kernel?  Note that this
     * always returns false if the argument does not represent a
     * configuration (e.g. if it's for a plain JAR).
     *
     * @param configurationId the configuration identifier, which must be
     *                        fully resolved (isResolved() == true)
     *
     * @return true if the configuration has been loaded; false otherwise
     */
    boolean isLoaded(Artifact configurationId);

    /**
     * Is the specified configuation running?  Note that this
     * always returns false if the argument does not represent a
     * configuration (e.g. if it's for a plain JAR).
     *
     * @param configurationId the configuration identifier, which must be
     *                        fully resolved (isResolved() == true)
     *
     * @return true if the configuration is running, false otherwise
     */
    boolean isRunning(Artifact configurationId);

    /**
     * Given an artifact that's not fully resolved (e.g. some parts are
     * missing), check whether there are any instances installed into
     * the server environment.  That is, are there any matches in the
     * configuration store, regardless of whether they're loaded or running?
     * Note that this always returns an empty array if the argument does not
     * represent a configuration (e.g. if it's for a plain JAR).
     *
     * @param query The partially-complete artifact name to check for
     *
     * @return All matching artifacts that are loaded in the server
     */
    Artifact[] getInstalled(Artifact query);

    /**
     * Given an artifact that's not fully resolved (e.g. some parts are
     * missing), check whether there are any instances loaded.
     * Note that this always returns an empty array if the argument does not
     * represent a configuration (e.g. if it's for a plain JAR).
     *
     * @param query The partially-complete artifact name to check for
     *
     * @return All matching artifacts that are loaded in the server
     */
    Artifact[] getLoaded(Artifact query);

    /**
     * Given an artifact that's not fully resolved (e.g. some parts are
     * missing), check whether there are any instances running.
     * Note that this always returns an empty array if the argument does not
     * represent a configuration (e.g. if it's for a plain JAR).
     *
     * @param query The partially-complete artifact name to check for
     *
     * @return All matching artifacts that are loaded in the server
     */
    Artifact[] getRunning(Artifact query);

    /**
     * Gets a List&gt;ConfigurationInfo&lt; of every of every available configuation.
     * This includes all configurations installed, regardless of whether they are
     * currently loaded or running.
     */
    List listConfigurations();

    /**
     * Return a list of the stores this manager knows about.
     *
     * @return a List&gt;AbstractName&lt; of the stores this manager controls
     */
    List<AbstractName> listStores();

    /**
     * Get all the ConfigurationStores known to this manager at present
     */
    ConfigurationStore[] getStores();

    /**
     * Gets the configuration store responsible for the specified
     * configuration, or null if there is none.  The configuration need not be
     * loaded or running; this just checks which store holds the data for it.
     *
     * @param configuration The unique ID for the configuration to check for,
     *                      which must be fully resolved (isResolved() == true)
     *
     * @return The ConfigurationStore for this configuration, or null if the
     *         configuration was not found in any configuration store.
     */
    ConfigurationStore getStoreForConfiguration(Artifact configuration);

    /**
     * Return a list of the configurations in a specific store.
     *
     * @param store the store to list
     *
     * @return a List&gt;ConfigurationInfo&lt; of all the configurations in the store
     *
     * @throws NoSuchStoreException if the store could not be located
     */
    List listConfigurations(AbstractName store) throws NoSuchStoreException;

    /**
     * Is the specified artifact a configuration?
     *
     * @param artifact the ID of the artifact to check, which must be fully
     *                 resolved (isResolved() == true)
     *
     * @return true if the artifact is a configuration available in the
     *         server (regardless of whether it has been loaded/started)
     */
    boolean isConfiguration(Artifact artifact);

    /**
     * Gets a loaded Configuration (does not see unloaded configurations).
     *
     * @param configurationId the unique ID of the configuration to get, which
     *                        must be fully resolved (isResolved() == true)
     *
     * @return the specified configuration or null if the configuration has not been loaded
     */
//    Configuration getConfiguration(Artifact configurationId);

    /**
     * Load the specified configuration (from a config store) and all
     * configurations it depends on into the kernel.  This causes the
     * configuration gbean to be loaded and started, but does not load any of
     * the gbeans contained within the configuration.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException;

    /**
     * Load the specified configurationData and all configurations it depends
     * on (from a config store) into the kernel. This causes the configuration
     * gbean to be loaded and started, but does not load any of the gbeans
     * contained within the configuration.
     *
     * @param configurationData the configuration to load
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException;

    /**
     * Load the specified configuration (from a config store) and all
     * configurations it depends on into the kernel.  This causes the
     * configuration gbean to be loaded and started, but does not load any of
     * the gbeans contained within the configuration.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Load the specified configurationData and all configurations it depends
     * on (from a config store) into the kernel. This causes the configuration
     * gbean to be loaded and started, but does not load any of the gbeans
     * contained within the configuration.
     *
     * @param configurationData the configuration to load
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if no configuration with the given id exists in the configuration stores
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Stops and unloads the configuration.  This causes all contained gbeans
     * to be stopped and unloaded, and the configuration gbean is stopped and
     * unloaded.  This operation causes all configurations that have a class
     * or service dependency on the specified configuration to be stopped and
     * unloaded.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException
     */
    LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException;

    /**
     * Stops and unloads the configuration.  This causes all contained gbeans
     * to be stopped and unloaded, and the configuration gbean is stopped and
     * unloaded.  This operation causes all configurations that have a class
     * or service dependency on the specified configuration to be stopped and
     * unloaded.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor         the monitor that should receive events as the
     *                        operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException
     */
    LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Loads and starts all of the gbeans contained within the configuration.
     * If any of the gbeans fails to fully start, all gbeans will be unloaded
     * and an exception will be thrown.  This operation causes all
     * configurations that the specified configuration has a service dependency
     * on to be started.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     */
    LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException;

    /**
     * Loads and starts all of the gbeans contained within the configuration.
     * If any of the gbeans fails to fully start, all gbeans will be unloaded
     * and an exception will be thrown.  This operation causes all
     * configurations that the specified configuration has a service dependency
     * on to be started.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     */
    LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Stop the gbeans contained within the configuration.  This operation
     * causes all configurations that have a service dependency on the
     * specified configuration to be stopped.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     */
    LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException;

    /**
     * Stop the gbeans contained within the configuration.  This operation
     * causes all configurations that have a service dependency on the
     * specified configuration to be stopped.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     */
    LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException;

    /**
     * Restarts the specified configuration and all configurations that have a
     * service dependency on the specified configuration
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException;

    /**
     * Restarts the specified configuration and all configurations that have a
     * service dependency on the specified configuration
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Reloads the specified configuration and all configurations that have a
     * dependency on the specified configuration
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException;

    /**
     * Reloads the specified configuration and all configurations that have a
     * dependency on the specified configuration
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Reloads the specified configuration and all configurations that have a
     * dependency on the specified configuration
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param version new version to load from the config store
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException;

    /**
     * Reloads the specified configuration and all configurations that have a
     * dependency on the specified configuration
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     * @param monitor the monitor that should receive events as the operation is carried out
     * @param version new version to load from the config store
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Reloads the specified configuration and all configurations that have a
     * dependency on the specified configuration
     *
     * @param configurationData the configuration to load
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException;

    /**
     * Reloads the specified configuration and all configurations that have a
     * dependency on the specified configuration
     *
     * @param configurationData the configuration to load
     * @param monitor the monitor that should receive events as the operation is carried out
     *
     * @return the results of the operation
     *
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException if there is a problem loading the configuration
     */
    LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException;

    /**
     * Unstalls the specified configuration from the server.   This operation
     * can not be reversed.
     *
     * @param configurationId the configuration identifier, which must be fully
     *                        resolved (isResolved() == true)
     *
     * @throws IOException if there was a problem removing the configuration
     * @throws NoSuchConfigException if the configuration is not loaded
     * @throws LifecycleException
     */
    void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException, LifecycleException;

    /**
     * Gets the common ArtifactResolver in case the caller wants to use this
     * directly.  It is configured for all the repositories known to this
     * configuration manager, etc.
     */
    ArtifactResolver getArtifactResolver();

    /**
     * Online means full functionality.  Offline typically means that configurations will never be started,
     * although they may be marked in the persistent configuration list.
     *
     * @return online status of ConfigurationManager
     */
    boolean isOnline();
    void setOnline(boolean online);

    Collection<? extends Repository> getRepositories();

    LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException;

    ConfigurationResolver newConfigurationResolver(ConfigurationData configurationData);

    LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException;

    Bundle getBundle(Artifact id);
}
