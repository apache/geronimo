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

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 * List the modules.
 *
 * @goal list-modules
 *
 * @version $Rev$ $Date$
 */
public class ListModulesMojo
    extends ModuleMojoSupport
{
    protected void doExecute() throws Exception {
        DeploymentManager manager = getDeploymentManager();
        Target[] targets = manager.getTargets();
        
        log.info("");
        log.info("Running Modules");
        log.info("===============");

        logModules(manager.getRunningModules(null, targets));

        log.info("");
        log.info("Non-Running Modules");
        log.info("===================");

        logModules(manager.getNonRunningModules(null, targets));
    }

    protected String getFullClassName() {
        return this.getClass().getName();
    }
}
