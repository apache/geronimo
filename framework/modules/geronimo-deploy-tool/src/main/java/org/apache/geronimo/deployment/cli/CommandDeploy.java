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

package org.apache.geronimo.deployment.cli;

import java.io.File;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.common.DeploymentException;

/**
 * The CLI deployer logic to deploy (distribute plus start).
 *
 * @version $Rev$ $Date$
 */
public class CommandDeploy extends CommandDistribute {

    protected String getAction() {
        return "Deployed";
    }

    protected ProgressObject runCommand(DeploymentManager mgr, ConsoleReader out, boolean inPlace, Target[] tlist, File module, File plan) throws DeploymentException {
        ProgressObject po = super.runCommand(mgr, out, inPlace, tlist, module, plan);
        waitForProgress(out, po);
        if(po.getDeploymentStatus().isFailed()) {
            throw new DeploymentException("Unable to deploy "+(module == null ? plan.getName() : module.getName())+": "+po.getDeploymentStatus().getMessage());
        }
        return mgr.start(po.getResultTargetModuleIDs());
    }

}
