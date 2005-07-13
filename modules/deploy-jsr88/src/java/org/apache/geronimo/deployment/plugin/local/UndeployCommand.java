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
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.GBeanNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class UndeployCommand extends CommandSupport {
    private static final String[] UNINSTALL_SIG = {URI.class.getName()};
    private final Kernel kernel;
    private final TargetModuleID[] modules;

    public UndeployCommand(Kernel kernel, TargetModuleID modules[]) {
        super(CommandType.START);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            for (int i = 0; i < modules.length; i++) {
                TargetModuleIDImpl module = (TargetModuleIDImpl) modules[i];

                URI moduleID = URI.create(module.getModuleID());
                try {
                    ObjectName configName = Configuration.getConfigurationObjectName(moduleID);
                    try {
                        kernel.stopGBean(configName);
                        updateStatus("Module "+moduleID+" stopped.");
                    } catch (GBeanNotFoundException e) {
                        if(clean(e.getGBeanName().getKeyProperty("name")).equals(moduleID.toString())) {
                            // the module is not running
                        } else {
                            throw e;
                        }
                    }
                    configurationManager.unload(moduleID);
                    updateStatus("Module "+moduleID+" unloaded.");
                } catch (InternalKernelException e) {
                    // this is cause by the kernel being already shutdown
                } catch (NoSuchConfigException e) {
                    // module was already undeployed - just continue
                }

                try {
                    TargetImpl target = (TargetImpl) module.getTarget();
                    ObjectName storeName = target.getObjectName();
                    URI configID = URI.create(module.getModuleID());
                    kernel.invoke(storeName, "uninstall", new Object[]{configID}, UNINSTALL_SIG);
                    updateStatus("Module "+moduleID+" uninstalled.");
                    addModule(module);
                } catch (NoSuchConfigException e) {
                    // module was already undeployed - just continue
                }
            }
            if(getModuleCount() < modules.length) {
                updateStatus("Some of the modules to undeploy were not previously deployed.  This is not treated as an error.");
            }
            complete("Completed");
        } catch (Exception e) {
            doFail(e);
        }
    }
}
