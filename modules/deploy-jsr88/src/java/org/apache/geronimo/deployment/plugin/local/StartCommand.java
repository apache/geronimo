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
import java.util.List;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;

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
            for (int i = 0; i < modules.length; i++) {
                TargetModuleID module = modules[i];

                URI moduleID = URI.create(module.getModuleID());
                List list = configurationManager.loadRecursive(moduleID);
                for (int j = 0; j < list.size(); j++) {
                    ObjectName name = (ObjectName) list.get(j);
                    kernel.startRecursiveGBean(name);
                    String configName = ObjectName.unquote(name.getKeyProperty("name"));
                    addModule(new TargetModuleIDImpl(modules[i].getTarget(), configName));
                }
            }
            complete("Completed");
        } catch (Exception e) {
            doFail(e);
        }
    }
}
