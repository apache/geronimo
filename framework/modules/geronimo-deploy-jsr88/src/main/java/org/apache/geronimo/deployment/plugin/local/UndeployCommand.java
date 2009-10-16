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
package org.apache.geronimo.deployment.plugin.local;

import java.net.URI;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

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
                    	if(!configurationManager.isOnline()) {
                    		//If an offline undeploy, need to load the configuration first, so that stopConfiguration()
                    		//can resolve the configuration and successfully set load=false in attribute manager, otherwise
                    		//starting the server will fail attempting to start an config that does not exist.
                    		configurationManager.loadConfiguration(moduleID);
                    	}
                    	
                        configurationManager.stopConfiguration(moduleID);


                        configurationManager.unloadConfiguration(moduleID);
                        updateStatus("Module " + moduleID + " unloaded.");
                    } catch (InternalKernelException e) {
                        // this is cause by the kernel being already shutdown
                    } catch (NoSuchConfigException e) {
                        // module was already unloaded - just continue
                    }

                    try {
                        configurationManager.uninstallConfiguration(moduleID);
                        updateStatus("Module " + moduleID + " uninstalled.");
                        addModule(module);
                    } catch (NoSuchConfigException e) {
                        // module was already undeployed - just continue
                    }
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }

            //todo: this will probably never happen because the command line args are compared to actual modules
            if (getModuleCount() < modules.length) {
                updateStatus("Some of the modules to undeploy were not previously deployed.  This is not treated as an error.");
            }
            complete("Completed");
        } catch (Throwable e) {
            doFail(e);
        }
    }
}
