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
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class SingleFileHotDeployer {
    private static final Logger log = LoggerFactory.getLogger(SingleFileHotDeployer.class);
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final File dir;
    private final String[] watchPaths;
    private final Collection builders;
    private final ConfigurationStore store;
    private final ConfigurationManager configurationManager;
    private final boolean forceDeploy;
    private final Artifact configurationId;
    private boolean wasDeployed;

    public SingleFileHotDeployer(String path, ServerInfo serverInfo, String[] watchPaths, Collection builders, ConfigurationStore store, ConfigurationManager configurationManager, boolean forceDeploy) throws DeploymentException {
        this(serverInfo.resolve(path), watchPaths, builders, store, configurationManager, forceDeploy);
    }

    public SingleFileHotDeployer(File dir, String[] watchPaths, Collection builders, ConfigurationStore store, ConfigurationManager configurationManager, boolean forceDeploy) throws DeploymentException {
        this.dir = dir;
        this.watchPaths = watchPaths;
        this.builders = builders;
        this.store = store;
        this.configurationManager = configurationManager;
        this.forceDeploy = forceDeploy;

        configurationId = start(dir);
    }

    private Artifact start(File dir) throws DeploymentException {
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory does not exist " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Directory is not a directory " + dir.getAbsolutePath());
        }

        // take no action if there is nothing in the directory to deploy.   Perhaps we should
        // consider doing an undeploy in this case if the application is already deployed. Howevr
        // for now this is to handle the case where the application is not already laid down at the
        // time of the initial deploy of this gbean.
        if (dir.list().length == 0) {
            return null;
        }

        // get the existing inplace configuration if there is one
        ConfigurationInfo existingConfiguration = null;
        List list = configurationManager.listConfigurations();
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            ConfigurationInfo configurationInfo = (ConfigurationInfo) iterator.next();
            if (dir.equals(configurationInfo.getInPlaceLocation())) {
                existingConfiguration = configurationInfo;
            }
        }
        Artifact existingConfigurationId = (existingConfiguration == null) ? null : existingConfiguration.getConfigID();

        if (!forceDeploy && existingConfiguration != null && !isModifiedSince(existingConfiguration.getCreated())) {
            try {
                configurationManager.loadConfiguration(existingConfigurationId);
                configurationManager.startConfiguration(existingConfigurationId);
            } catch (Exception e) {
                throw new DeploymentException("Unable to load and start " + dir, e);
            }
            return existingConfigurationId;
        }

        // if the current id and the new id only differ by version, we can reload, otherwise we need to load and start
        if (existingConfigurationId != null && configurationManager.isLoaded(existingConfigurationId)) {
            try {
                configurationManager.unloadConfiguration(existingConfigurationId);
            } catch (NoSuchConfigException e) {
                throw new DeploymentException("Unable to unload existing configuration " + existingConfigurationId);
            } catch (LifecycleException e) {
                throw new DeploymentException("Unable to unload existing configuration " + existingConfigurationId);
            }
        }

        ModuleIDBuilder idBuilder = new ModuleIDBuilder();

        JarFile module = null;
        try {
            module = JarUtils.createJarFile(dir);
        } catch (IOException e) {
            throw new DeploymentException("Cound not open module file: " + dir.getAbsolutePath(), e);
        }

        try {
            // get the builder and plan
            Object plan = null;
            ConfigurationBuilder builder = null;
            for (Iterator i = builders.iterator(); i.hasNext();) {
                ConfigurationBuilder candidate = (ConfigurationBuilder) i.next();
                plan = candidate.getDeploymentPlan(null, module, idBuilder);
                if (plan != null) {
                    builder = candidate;
                    break;
                }
            }
            if (builder == null) {
                throw new DeploymentException("Cannot deploy the requested application module because no builder is able to handle it (dir=" + dir.getAbsolutePath() + ")");
            }

            // determine the new configuration id
            Artifact configurationId = builder.getConfigurationID(plan, module, idBuilder);

            // if the new configuration id isn't fully resolved, populate it with defaults
            if (!configurationId.isResolved()) {
                configurationId = resolve(configurationId);
            }

            // If we didn't find a previous configuration based upon the path then check one more time to
            // see if one exists by the same configurationID.   This will catch situations where the target
            // path was renamed or moved such that the path associated with the previous configuration no longer
            // matches the patch associated with the new configuration.
            if ((existingConfigurationId == null) && configurationManager.isInstalled(configurationId)) {
                log.info("Existing Module found by moduleId");
                existingConfigurationId = configurationId;
            }

            // if we are deploying over the exisitng version we need to uninstall first
            if(configurationId.equals(existingConfigurationId)) {
                log.info("Undeploying " + existingConfigurationId);
                configurationManager.uninstallConfiguration(existingConfigurationId);
            }

            // deploy it
            deployConfiguration(builder, store, configurationId, plan, module, Arrays.asList(configurationManager.getStores()), configurationManager.getArtifactResolver());
            wasDeployed = true;

            configurationManager.loadConfiguration(configurationId);
            configurationManager.startConfiguration(configurationId);

            log.info("Successfully deployed and started " + configurationId + " in location " + dir);

            return configurationId;
        } catch (Exception e) {
            throw new DeploymentException("Unable to deploy " + dir, e);
        } finally {
            JarUtils.close(module);
        }

    }

    private boolean isModifiedSince(long created) {
        for (int i = 0; i < watchPaths.length; i++) {
            String path = watchPaths[i];
            File file = new File(dir, path);
            if (!file.exists()) {
                log.warn("Watched file does not exist " + file);
            }
            if (file.isFile() && file.lastModified() > created) {
                log.info("Redeploying " + dir + " because file " + file + " was modified;");
                return true;
            }
        }
        return false;
    }

    private Artifact resolve(Artifact configID) throws DeploymentException {
        String group = configID.getGroupId();
        if (group == null) {
            group = Artifact.DEFAULT_GROUP_ID;
        }
        String artifactId = configID.getArtifactId();
        if (artifactId == null) {
            throw new DeploymentException("Every configuration to deploy must have a ConfigID with an ArtifactID (not " + configID + ")");
        }
        Version version = configID.getVersion();
        if (version == null) {
            version = new Version(Long.toString(System.currentTimeMillis()));
        }
        String type = configID.getType();
        if (type == null) {
            type = "car";
        }
        return new Artifact(group, artifactId, version, type);
    }

    private List deployConfiguration(ConfigurationBuilder builder, ConfigurationStore store, Artifact configurationId, Object plan, JarFile module, Collection stores, ArtifactResolver artifactResolver) throws DeploymentException {
        try {
            // It's our responsibility to close this context, once we're done with it...
            DeploymentContext context = builder.buildConfiguration(true, configurationId, plan, module, stores, artifactResolver, store);
            context.initializeConfiguration();
            List configurations = new ArrayList();
            try {
                configurations.add(context.getConfigurationData());
                configurations.addAll(context.getAdditionalDeployment());

                if (configurations.isEmpty()) {
                    throw new DeploymentException("Deployer did not create any configurations");
                }
                List deployedURIs = new ArrayList();
                for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
                    ConfigurationData configurationData = (ConfigurationData) iterator.next();
                    configurationData.setAutoStart(false);
                    store.install(configurationData);
                    deployedURIs.add(configurationData.getId().toString());
                }
                return deployedURIs;
            } catch (IOException e) {
                cleanupConfigurations(configurations);
                throw e;
            } catch (InvalidConfigException e) {
                cleanupConfigurations(configurations);
                // unlikely as we just built this
                throw new DeploymentException(e);
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        } catch (Throwable e) {
            if (e instanceof Error) {
                log.error("Deployment failed due to ", e);
                throw (Error) e;
            } else if (e instanceof DeploymentException) {
                throw (DeploymentException) e;
            } else if (e instanceof Exception) {
                log.error("Deployment failed due to ", e);
                throw new DeploymentException(e);
            }
            throw new Error(e);
        } finally {
            JarUtils.close(module);
        }
    }

    private void cleanupConfigurations(List configurations) {
        LinkedList cannotBeDeletedList = new LinkedList();
        for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
            ConfigurationData configurationData = (ConfigurationData) iterator.next();
            File dir = configurationData.getConfigurationDir();
            cannotBeDeletedList.clear();
            if (!FileUtils.recursiveDelete(dir,cannotBeDeletedList)) {
                // Output a message to help user track down file problem
                log.warn("Unable to delete " + cannotBeDeletedList.size() +
                        " files while recursively deleting directory "
                        + dir + LINE_SEP +
                        "The first file that could not be deleted was:" + LINE_SEP + "  "+
                        ( !cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : "") );
            }
        }
    }

    public File getDir() {
        return dir;
    }

    public Artifact getConfigurationId() {
        return configurationId;
    }

    public boolean isForceDeploy() {
        return forceDeploy;
    }

    public boolean wasDeployed() {
        return wasDeployed;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SingleFileHotDeployer.class);

        infoFactory.addAttribute("path", String.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addAttribute("watchPaths", String[].class, true);
        infoFactory.addReference("Builders", ConfigurationBuilder.class);
        infoFactory.addReference("Store", ConfigurationStore.class);
        infoFactory.addReference("ConfigurationManager", ConfigurationManager.class);
        infoFactory.addAttribute("forceDeploy", boolean.class, true);

        infoFactory.setConstructor(new String[]{"path", "ServerInfo", "watchPaths", "Builders", "Store", "ConfigurationManager", "forceDeploy"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
