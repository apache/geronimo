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

import org.apache.geronimo.deployment.DeploymentException;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import java.io.PrintWriter;
import java.io.File;

/**
 * The CLI deployer logic to deploy (distribute plus start).
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandDeploy extends CommandDistribute {
    public CommandDeploy() {
        super("deploy", "1. Common Commands", "[--targets target,target,...] [module] [plan]",
                "Normally both a module and plan are passed to the deployer.  " +
                "Sometimes the module contains a plan, or requires no plan, in which case " +
                "the plan may be omitted.  Sometimes the plan references a module already " +
                "deployed in the Geronimo server environment, in which case a module does " +
                "not need to be provided.\n" +
                "If the server is not currently running, the module will be marked " +
                "to start next time the server is started.\n" +
                "If no targets are provided, the module is deployed to all available" +
                "targets.  Geronimo only provides one target (ever), so this is primarily" +
                "useful when using a different driver.");
    }

    protected String getAction() {
        return "Deployed";
    }

    protected ProgressObject runCommand(DeploymentManager mgr, PrintWriter out, Target[] tlist, File module, File plan) {
        TargetModuleID[] modules = waitForProgress(out, mgr.distribute(tlist, module, plan));
        return mgr.start(modules);
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        if(!connection.isOnline()) {
            throw new DeploymentException("This command cannot be run unless connecting to a running server.  Specify --url if server is not running on the default port on localhost.");
        }
        super.execute(out, connection, args);
    }
}
