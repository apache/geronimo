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
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;

/**
 * @version $Rev$ $Date$
 */
public class RemoteDeployer {
    public static void main(String[] args) throws Exception {
        File module = new File(args[0]);
        ProgressObject po;
        new DeploymentFactoryImpl();

        String uri = "deployer:geronimo:jmx:rmi://localhost/jndi/rmi:/JMXConnector";
        DeploymentManager manager = DeploymentFactoryManager.getInstance().getDeploymentManager(uri, "system", "manager");
        Target[] targets = manager.getTargets();
        TargetModuleID[] modules = manager.getAvailableModules(null, targets);
        List redeploy = new ArrayList();
        if (args.length > 1) {
            for (int i = 0; i < modules.length; i++) {
                TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl)modules[i];
                if (targetModuleID.getModuleID().equals(args[1])) {
                    redeploy.add(targetModuleID);
                }
            }
        }
        if (redeploy.isEmpty()) {
            po = manager.distribute(targets, module, null);
        } else {
            TargetModuleID[] todo = (TargetModuleID[]) redeploy.toArray(new TargetModuleID[redeploy.size()]);
            po = manager.redeploy(todo, module, null);
        }
        while (po.getDeploymentStatus().isRunning()) {
            Thread.sleep(100);
        }
        System.out.println(po.getDeploymentStatus().getMessage());
        if (po.getDeploymentStatus().isCompleted()) {
            manager.start(po.getResultTargetModuleIDs());
        }

//        po = manager.stop(po.getResultTargetModuleIDs());
//        while (po.getDeploymentStatus().isRunning()) {
//            Thread.sleep(100);
//        }
//        System.out.println(po.getDeploymentStatus().getMessage());
//        po = manager.undeploy(po.getResultTargetModuleIDs());
//        while (po.getDeploymentStatus().isRunning()) {
//            Thread.sleep(100);
//        }
//        System.out.println(po.getDeploymentStatus().getMessage());
    }
}
