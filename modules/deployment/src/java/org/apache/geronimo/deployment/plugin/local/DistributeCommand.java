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
import java.util.Iterator;
import java.util.Set;
import java.net.URI;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;

/**
 * @version $Revision: 1.11 $ $Date: 2004/06/23 22:44:49 $
 */
public class DistributeCommand extends CommandSupport {
    private static final String[] DEPLOY_SIG = {File.class.getName(), File.class.getName()};
    private final KernelMBean kernel;
    private final Target[] targetList;
    private final File moduleArchive;
    private final File deploymentPlan;

    public DistributeCommand(KernelMBean kernel, Target[] targetList, File moduleArchive, File deploymentPlan) {
        super(CommandType.DISTRIBUTE);
        this.kernel = kernel;
        this.targetList = targetList;
        this.moduleArchive = moduleArchive;
        this.deploymentPlan = deploymentPlan;
    }

    public void run() {
        try {
            Set deployers = kernel.listGBeans(new ObjectName("geronimo.deployment:role=Deployer,*"));
            if (deployers.isEmpty()) {
                fail("No deployer present in kernel");
                return;
            }
            Iterator i = deployers.iterator();
            ObjectName deployer = (ObjectName) i.next();
            if (i.hasNext()) {
                throw new UnsupportedOperationException("More than one deployer found");
            }

            Object[] args = {moduleArchive, deploymentPlan};
            URI configId = (URI) kernel.invoke(deployer, "deploy", args, DEPLOY_SIG);
            TargetModuleID moduleID = new TargetModuleIDImpl(targetList[0], configId.toString());
            addModule(moduleID);
            complete("Completed");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
