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
import java.net.URL;
import java.util.jar.Manifest;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public interface ConfigurationBuilder {
    SchemaTypeLoader[] getTypeLoaders();

    /**
     * Determine if this builder can handle the supplied plan.
     * @param plan the plan to examine
     * @return true if this builder will handle it
     */
    boolean canConfigure(XmlObject plan);

    /**
     * Extract the deployment plan from a module if this builder can
     * process it.
     * @param module the URL of the module to examine
     * @return the deployment plan, or null if this module can not handle it
     */
    XmlObject getDeploymentPlan(URL module) throws XmlException;

    /**
     * Build a configuration from a local file
     * @param outfile the file to write the configuration to
     * @param module the module to build
     * @param plan the deployment plan
     * @throws IOException if there was a problem reading or writing the files
     * @throws DeploymentException if there was a problem with the configuration
     */
    void buildConfiguration(File outfile, Manifest manifest, File module, XmlObject plan) throws IOException, DeploymentException;
}
