/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.mavenplugin;

import java.util.List;
import java.util.ArrayList;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/25 08:22:07 $
 *
 * */
public class UndeployModule extends AbstractModuleCommand {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void execute() throws Exception {
        DeploymentManager manager = getDeploymentManager();

        Target[] targets = manager.getTargets();
        TargetModuleID moduleIds[] = manager.getNonRunningModules(null, targets);
        List toUndeploy = new ArrayList(moduleIds.length);
        for (int i = 0; i < moduleIds.length; i++) {
            TargetModuleID moduleId = moduleIds[i];
            if (getId().equals(moduleId.getModuleID())) {
                toUndeploy.add(moduleId);
            }
        }
        if (toUndeploy.size() == 0) {
            System.out.println("Module is running or not deployed: " + getId());
            return;
        }
        moduleIds = (TargetModuleID[]) toUndeploy.toArray(new TargetModuleID[toUndeploy.size()]);
        ProgressObject progress = manager.undeploy(moduleIds);
        DeploymentClient.waitFor(progress);
    }

}
