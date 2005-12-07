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

import javax.enterprise.deploy.spi.Target;
import java.io.PrintWriter;

/**
 * The CLI deployer logic to list targets.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandListTargets extends AbstractCommand {
    public CommandListTargets() {
        super("list-targets", "2. Other Commands", "",
                "Lists the targets known to the server you've connected to.\n" +
                "In the case of Geronimo, each configuration store is a separate " +
                "target.  Geronimo does not yet support clusters as targets.");
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        Target[] list = connection.getDeploymentManager().getTargets();
        out.println("Available Targets:");
        for(int i = 0; i < list.length; i++) {
            Target target = list[i];
            out.println("  "+target.getName());
        }
    }
}
