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

import org.apache.geronimo.common.DeploymentException;

import java.io.PrintWriter;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 * The CLI deployer logic to stop.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandStop extends CommandStart {
    public CommandStop() {
        super("stop", "1. Common Commands", "[ModuleID|TargetModuleID]+",
                "Accepts the configId of a module, or the fully-qualified " +
                "TargetModuleID identifying both the module and the server or cluster it's " +
                "on, and stops that module.  The module should be available to the server " +
                "and running.  After stop is completed, the server still has the module and " +
                "deployment information available, it's just not running.  If multiple " +
                "modules are specified, they will all be stopped.\n" +
                "If the server is not running, the module will be marked to not " +
                "start next time the server is started.");
    }

    protected ProgressObject runCommand(PrintWriter out, DeploymentManager mgr, TargetModuleID[] ids) {
        ProgressObject po = mgr.stop(ids);
        waitForProgress(out, po);
        return po;
    }

    protected String getAction() {
        return "Stopped";
    }
}
