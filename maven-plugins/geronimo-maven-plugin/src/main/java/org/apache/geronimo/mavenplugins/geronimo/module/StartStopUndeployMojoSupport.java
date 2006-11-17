/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.geronimo.module;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.apache.geronimo.mavenplugins.geronimo.ModuleConfig;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Support for start/stop/undeploy mojos.
 *
 * @version $Rev$ $Date$
 */
public abstract class StartStopUndeployMojoSupport
    extends ModuleMojoSupport
{
    //
    // TODO: Add support to take a set of ModuleConfig's and operate on them
    //
    
    /**
     * The id of the module to be started in the format of <tt>groupId/artifactId/version/type</tt>.
     *
     * @parameter expression="${moduleId}
     */
    protected String moduleId = null;

    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();

        if (moduleId != null) {
            log.info("Using non-artifact based module id: " + moduleId);

            // Add the single module to the list
            //
            // FIXME Should be able to handle multiple moduleIds
            //
            ModuleConfig moduleConfig = createModuleConfigFromId(moduleId);
            if (modules == null) {
                modules = new ModuleConfig[] {
                    moduleConfig
                };
            }
            else {
                List list = Arrays.asList(modules);
                ArrayList aList = new ArrayList(list);
                aList.add(moduleConfig);
                modules = (ModuleConfig[]) aList.toArray(new ModuleConfig[list.size()]);
            }
        }
        else if (modules == null || modules.length == 0) {
            throw new MojoExecutionException("At least one module configuration (or moduleId) must be specified");
        }
    }

    private ModuleConfig createModuleConfigFromId(String moduleId) throws MojoExecutionException {
        assert moduleId != null;

        ModuleConfig moduleConfig = new ModuleConfig();
        moduleId = moduleId.replace('\\', '/');
        String[] splitStr = moduleId.split("/");
        if (splitStr.length != 4) {
            throw new MojoExecutionException("Invalid moduleId: " + moduleId);
        }
        moduleConfig.setGroupId(splitStr[0]);
        moduleConfig.setArtifactId(splitStr[1]);
        moduleConfig.setVersion(splitStr[2]);
        moduleConfig.setType(splitStr[3]);
         
        return moduleConfig;
    }
}
