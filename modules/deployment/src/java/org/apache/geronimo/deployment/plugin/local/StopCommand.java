/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:38 $
 */
public class StopCommand extends CommandSupport {
    private final Kernel kernel;
    private final TargetModuleID[] modules;

    public StopCommand(Kernel kernel, TargetModuleID modules[]) {
        super(CommandType.START);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            for (int i = 0; i < modules.length; i++) {
                TargetModuleID module = modules[i];

                URI moduleID = URI.create(module.getModuleID());
                ObjectName name = ConfigurationManager.getConfigObjectName(moduleID);
                kernel.getMBeanServer().invoke(name, "stop", null, null);
                addModule(module);
            }
            complete("Completed");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
