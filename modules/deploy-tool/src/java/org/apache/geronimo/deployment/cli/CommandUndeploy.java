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

package org.apache.geronimo.deployment.cli;

import java.io.PrintWriter;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;

/**
 * the CLI deployer logic to undeploy.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandUndeploy extends CommandStart {
    public CommandUndeploy() {
        super("undeploy", "1. Common Commands", "[ModuleID|TargetModuleID]+",
                "Accepts the configId of a module, or the fully-qualified " +
                "TargetModuleID identifying both the module and the server or cluster it's " +
                "on, stops that module, and removes the deployment files for that module " +
                "from the server environment.  If multiple modules are specified, they will " +
                "all be undeployed.");
    }

    protected TargetModuleID[] runCommand(PrintWriter out, DeploymentManager mgr, TargetModuleID[] ids) {
        return waitForProgress(out, mgr.undeploy(ids));
    }

    protected String getAction() {
        return "Undeployed";
    }
}
