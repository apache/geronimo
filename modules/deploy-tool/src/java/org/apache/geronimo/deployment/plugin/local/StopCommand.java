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

import org.apache.geronimo.kernel.jmx.KernelMBean;

/**
 * @version $Rev$ $Date$
 */
public class StopCommand extends CommandSupport {
    private final KernelMBean kernel;
    private final TargetModuleID[] modules;

    public StopCommand(KernelMBean kernel, TargetModuleID modules[]) {
        super(CommandType.START);
        this.kernel = kernel;
        this.modules = modules;
    }

    public void run() {
        try {
            for (int i = 0; i < modules.length; i++) {
                TargetModuleID module = modules[i];

                URI moduleID = URI.create(module.getModuleID());
                kernel.stopConfiguration(moduleID);
                addModule(module);
            }
            complete("Completed");
        } catch (Exception e) {
            doFail(e);
        }
    }
}
