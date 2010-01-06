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
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;

/**
 * @version $Rev$ $Date$
 */
public interface ConfigurationBuilder {
    static final String CONFIG_BUILDER = "ConfigBuilder";

    /**
     * Builds a deployment plan specific to this builder from a planFile and/or
     * module if this builder can process it.
     * @param planFile the deployment plan to examine; can be null
     * @param module the URL of the module to examine; can be null
     * @return the deployment plan, or null if this builder can not handle the module
     * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
     */
    Object getDeploymentPlan(File planFile, JarFile module, ModuleIDBuilder idBuilder) throws DeploymentException;

    /**
     * Checks what configuration URL will be used for the provided module.
     * @param plan the deployment plan
     * @param module the module to build
     * @return the ID that will be used for the Configuration
     * @throws IOException if there was a problem reading or writing the files
     * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
     */
    Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException;

    /**
     * Build a configuration from a local file
     *
     * @param inPlaceDeployment true if the deployment is in-place.
     * @param plan the deployment plan
     * @param module the module to build
     * @param configurationStores
     * @param artifactResolver
     * @param targetConfigurationStore
     * @return the deployment context created from the deployment (the contexts must be closed by the caller)
     * @throws IOException if there was a problem reading or writing the files
     * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
     */
    DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, Object plan, JarFile module, Collection<ConfigurationStore> configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException;
}
