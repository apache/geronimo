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

import java.net.URI;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.jmx.KernelMBean;

/**
 * @version $Rev$ $Date$
 */
public class UndeployCommand extends CommandSupport {
    private static final String[] UNINSTALL_SIG = {URI.class.getName()};
    private final KernelMBean kernel;
    private final TargetModuleID[] modules;

    public UndeployCommand(KernelMBean kernel, TargetModuleID modules[]) {
        super(CommandType.START);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            for (int i = 0; i < modules.length; i++) {
                TargetModuleIDImpl module = (TargetModuleIDImpl) modules[i];

                URI moduleID = URI.create(module.getModuleID());
                try {
                    kernel.stopConfiguration(moduleID);
                } catch (NoSuchConfigException e) {
                    // module was already undeployed - just continue
                }

                try {
                    TargetImpl target = (TargetImpl) module.getTarget();
                    ObjectName storeName = target.getObjectName();
                    URI configID = URI.create(module.getModuleID());
                    kernel.invoke(storeName, "uninstall", new Object[]{configID}, UNINSTALL_SIG);
                } catch (NoSuchConfigException e) {
                    // module was already undeployed - just continue
                }

                addModule(module);
            }
            complete("Completed");
        } catch (Exception e) {
            doFail(e);
        }
    }
}
