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

package org.apache.geronimo.deployment.plugin.local;

import java.io.File;
import java.io.InputStream;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class DistributeCommand extends AbstractDeployCommand {
    protected final Target[] targetList;

    public DistributeCommand(Kernel kernel, Target[] targetList, File moduleArchive, File deploymentPlan) {
        super(CommandType.DISTRIBUTE, kernel, moduleArchive, deploymentPlan, null, null, false);
        this.targetList = targetList;
    }

    public DistributeCommand(Kernel kernel, Target[] targetList, InputStream moduleStream, InputStream deploymentStream) {
        super(CommandType.DISTRIBUTE, kernel, null, null, moduleStream, deploymentStream, true);
        this.targetList = targetList;
    }

    public void run() {
        try {
            if (spool) {
                if (moduleStream != null) {
                    moduleArchive = DeploymentUtil.createTempFile();
                    copyTo(moduleArchive, moduleStream);
                }
                if (deploymentStream != null) {
                    deploymentPlan = DeploymentUtil.createTempFile();
                    copyTo(deploymentPlan, deploymentStream);
                }
            }
            ObjectName deployer = getDeployerName();
            if (deployer == null) {
                return;
            }
            doDeploy(deployer, targetList[0], true);
        } catch (Exception e) {
            doFail(e);
        } finally {
            if (spool) {
                if (moduleArchive != null) {
                    moduleArchive.delete();
                }
                if (deploymentPlan != null) {
                    deploymentPlan.delete();
                }
            }
        }
    }

}
