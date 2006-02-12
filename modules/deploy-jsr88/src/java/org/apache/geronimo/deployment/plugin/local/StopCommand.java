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

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 * @version $Rev$ $Date$
 */
public class StopCommand extends CommandSupport {
    private final Kernel kernel;
    private final TargetModuleID[] modules;

    public StopCommand(Kernel kernel, TargetModuleID modules[]) {
        super(CommandType.STOP);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleID module = modules[i];
                    Artifact moduleID = Artifact.create(module.getModuleID());
                    try {
                        configurationManager.stop(moduleID);
                    } catch (InvalidConfigException e) {
                        updateStatus("Module "+moduleID+" is not running.");
                        continue;
                    }
                    configurationManager.unload(moduleID);
                    addModule(module);
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }
            if(getModuleCount() < modules.length) {
                fail("Some modules could not be stopped");
            } else {
                complete("Completed");
            }
        } catch (Exception e) {
            doFail(e);
        }
    }
}
