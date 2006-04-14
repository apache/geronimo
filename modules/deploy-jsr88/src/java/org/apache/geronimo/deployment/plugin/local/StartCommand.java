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

import java.util.List;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev:392614 $ $Date$
 */
public class StartCommand extends CommandSupport {
    private final Kernel kernel;
    private final TargetModuleID[] modules;

    public StartCommand(Kernel kernel, TargetModuleID modules[]) {
        super(CommandType.START);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleID module = modules[i];

                    // Check to see whether the module is already started
                    Artifact moduleID = Artifact.create(module.getModuleID());
                    AbstractName abstractName = Configuration.getConfigurationAbstractName(moduleID);
                    String objectName = abstractName.getObjectName().getCanonicalName();
                    if (kernel.isRunning(abstractName)) {
                        updateStatus("Module " + moduleID + " is already running");
                        Thread.sleep(100);
                        continue;
                    }

                    // Load
                    configurationManager.loadConfiguration(moduleID);

                    // Start
                    configurationManager.startConfiguration(moduleID);

                    // Determine the child modules of the configuration
                    //TODO might be a hack
                    String configName = abstractName.getArtifact().toString();
                    List kids = loadChildren(kernel, configName);

                    // Build a response obect containg the started configuration and a list of it's contained modules
                    TargetModuleIDImpl id = new TargetModuleIDImpl(modules[i].getTarget(), module.getModuleID(),
                            (String[]) kids.toArray(new String[kids.size()]));
                    if (isWebApp(kernel, configName)) {
                        id.setType(ModuleType.WAR);
                    }
                    if (id.getChildTargetModuleID() != null) {
                        for (int k = 0; k < id.getChildTargetModuleID().length; k++) {
                            TargetModuleIDImpl child = (TargetModuleIDImpl) id.getChildTargetModuleID()[k];
                            if (isWebApp(kernel, child.getModuleID())) {
                                child.setType(ModuleType.WAR);
                            }
                        }
                    }
                    addModule(id);
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }
            addWebURLs(kernel);
            complete("Completed");
        } catch (Exception e) {
            e.printStackTrace();
            doFail(e);
        }
    }
}
