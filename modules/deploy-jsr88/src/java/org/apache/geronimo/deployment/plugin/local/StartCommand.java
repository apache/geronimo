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

import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import java.util.List;

/**
 *
 *
 * @version $Rev$ $Date$
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
                    try {
                        if(kernel.getGBeanState(Configuration.getConfigurationObjectName(moduleID)) == State.RUNNING_INDEX) {
                            updateStatus("Module "+moduleID+" is already running");
                            Thread.sleep(100);
                            continue;
                        }
                    } catch (GBeanNotFoundException e) {
                        // That means that the configuration may have been distributed but has not yet been loaded.
                        // That's fine, we'll load it next.
                    }

                    // Load and start the module
                    List list = configurationManager.loadRecursive(moduleID);
                    for (int j = 0; j < list.size(); j++) {
                        Artifact name = (Artifact) list.get(j);
                        configurationManager.loadGBeans(name);
                        configurationManager.start(name);
                        String configName = name.toString();
                        List kids = loadChildren(kernel, configName);
                        TargetModuleIDImpl id = new TargetModuleIDImpl(modules[i].getTarget(), configName,
                                (String[]) kids.toArray(new String[kids.size()]));
                        if(isWebApp(kernel, configName)) {
                            id.setType(ModuleType.WAR);
                        }
                        if(id.getChildTargetModuleID() != null) {
                            for (int k = 0; k < id.getChildTargetModuleID().length; k++) {
                                TargetModuleIDImpl child = (TargetModuleIDImpl) id.getChildTargetModuleID()[k];
                                if(isWebApp(kernel, child.getModuleID())) {
                                    child.setType(ModuleType.WAR);
                                }
                            }
                        }
                        addModule(id);
                    }
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
