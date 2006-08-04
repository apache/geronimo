/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.plugins.util.DeploymentClient;
import org.apache.maven.plugin.MojoExecutionException;

//
// TODO: Rename to StopMojo
//

/**
 * ???
 *
 * @goal stop
 * 
 * @version $Rev$ $Date$
 */
public class StopModuleMojo extends AbstractModuleMojo {

    protected void doExecute() throws Exception {
        RemoteDeploymentManager manager = (RemoteDeploymentManager) getDeploymentManager();
        manager.setLogConfiguration(true, true);

        Target[] targets = manager.getTargets();
        TargetModuleID moduleIds[] = manager.getRunningModules(null, targets);
        List toStop = new ArrayList(moduleIds.length);

        for (int i = 0; i < moduleIds.length; i++) {
            TargetModuleID moduleId = moduleIds[i];

            if (this.id.equals(moduleId.getModuleID())) {
                toStop.add(moduleId);
            }
        }

        if (toStop.size() == 0) {
            throw new MojoExecutionException("Module is not deployed: " + this.id);
        }

        moduleIds = (TargetModuleID[]) toStop.toArray(new TargetModuleID[toStop.size()]);
        ProgressObject progress = manager.stop(moduleIds);
        DeploymentClient.waitFor(progress);
    }
}
