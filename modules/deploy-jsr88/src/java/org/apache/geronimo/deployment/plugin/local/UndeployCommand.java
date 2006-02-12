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

import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.ObjectName;
import java.net.URI;

/**
 * @version $Rev$ $Date$
 */
public class UndeployCommand extends CommandSupport {
    private static final String[] UNINSTALL_SIG = {URI.class.getName()};
    private final Kernel kernel;
    private final TargetModuleID[] modules;

    public UndeployCommand(Kernel kernel, TargetModuleID modules[]) {
        super(CommandType.UNDEPLOY);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleIDImpl module = (TargetModuleIDImpl) modules[i];

                    Artifact moduleID = Artifact.create(module.getModuleID());
                    try {
                        try {
                            configurationManager.stop(moduleID);
                        } catch (InvalidConfigException e) {
                            if(e.getCause() instanceof GBeanNotFoundException) {
                                GBeanNotFoundException gnf = (GBeanNotFoundException) e.getCause();
                                if(clean(gnf.getGBeanName().getKeyProperty("name")).equals(moduleID.toString())) {
                                    // the module is not running
                                } else {
                                    throw gnf;
                                }
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
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }
            //todo: this will probably never happen because the command line args are compared to actual modules
            if(getModuleCount() < modules.length) {
                updateStatus("Some of the modules to undeploy were not previously deployed.  This is not treated as an error.");
            }
            complete("Completed");
        } catch (Exception e) {
            doFail(e);
        }
    }
}
