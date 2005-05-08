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

package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.config.ConfigurationData;

/**
 * @version $Rev$ $Date$
 */
public interface ConfigurationBuilder {
    /**
     * Builds a deployment plan specific to this builder from a planFile and/or
     * module if this builder can process it.
     * @param planFile the deployment plan to examine; can be null
     * @param module the URL of the module to examine; can be null
     * @return the deployment plan, or null if this builder can not handle the module
     * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
     */
    Object getDeploymentPlan(File planFile, JarFile module) throws DeploymentException;

    /**
     * Build a configuration from a local file
     *
     * @param plan the deployment plan
     * @param module the module to build
     * @param outfile the file in which the configiguration files should be written
     * @return the Configuration information
     * @throws IOException if there was a problem reading or writing the files
     * @throws org.apache.geronimo.common.DeploymentException if there was a problem with the configuration
     */
    ConfigurationData buildConfiguration(Object plan, JarFile module, File outfile) throws IOException, DeploymentException;
}
