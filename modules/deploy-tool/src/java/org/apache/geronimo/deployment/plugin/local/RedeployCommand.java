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
import java.net.URI;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.jmx.KernelMBean;

/**
 * @version $Rev$ $Date$
 */
public class RedeployCommand extends AbstractDeployCommand {
    private static final String[] DEPLOY_SIG = {File.class.getName(), File.class.getName()};
    private static final String[] UNINSTALL_SIG = {URI.class.getName()};
    private final TargetModuleID[] modules;
    private final File moduleArchive;
    private final File deploymentPlan;

    public RedeployCommand(KernelMBean kernel, TargetModuleID modules[], File moduleArchive, File deploymentPlan) {
        super(CommandType.START, kernel);
        this.modules = modules;
        this.moduleArchive = moduleArchive;
        this.deploymentPlan = deploymentPlan;
    }

    public void run() {
        ObjectName deployer = getDeployerName();
        if (deployer == null) {
            return;
        }

        try {
            for (int i = 0; i < modules.length; i++) {
                TargetModuleIDImpl module = (TargetModuleIDImpl) modules[i];

                URI configID = URI.create(module.getModuleID());
                kernel.stopConfiguration(configID);

                TargetImpl target = (TargetImpl) module.getTarget();
                ObjectName storeName = target.getObjectName();
                kernel.invoke(storeName, "uninstall", new Object[]{configID}, UNINSTALL_SIG);

                Object[] args = {moduleArchive, deploymentPlan};
                URI configId = (URI) kernel.invoke(deployer, "deploy", args, DEPLOY_SIG);
                module = new TargetModuleIDImpl(module.getTarget(), configId.toString());
                addModule(module);
            }
            complete("Completed");
        } catch (Exception e) {
            doFail(e);
        }
    }
}
