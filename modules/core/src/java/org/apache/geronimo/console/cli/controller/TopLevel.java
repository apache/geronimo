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

package org.apache.geronimo.console.cli.controller;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.console.cli.module.WARInfo;

/**
 * Top-level menu for working with the DeploymentManager.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:41 $
 */
public class TopLevel extends TextController {
    private static final Log log = LogFactory.getLog(TopLevel.class);

    public TopLevel(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            if(context.moduleInfo instanceof EJBJARInfo) {
                new WorkWithEJBJAR(context).execute();
                continue;
            } else if(context.moduleInfo instanceof WARInfo) {
                new WorkWithWAR(context).execute();
                continue;
            }
            println("\n\nNo J2EE module is currently selected.");
            println("  "+(context.connected ? "1)" : "--")+" Control existing deployments (review, start, stop, undeploy)");
            println("  2) Select an EJB JAR or WAR to configure, deploy, or redeploy"); //todo: change text when other modules are supported
            println("  "+(context.connected ? "3)" : "--")+" Disconnect from the server.");
            String choice;
            while(true) {
                print("Action ([1-3] or [Q]uit): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("1")) {
                    if(!context.connected) {
                        continue;
                    }
                    new ControlDeployments(context).execute();
                    break;
                } else if(choice.equals("2")) {
                    new SelectModule(context).execute();
                    break;
                } else if(choice.equals("3")) {
                    if(!context.connected) {
                        continue;
                    }
                    context.deployer.release();
                    context.connected = false;
                    println("Released any server resources and disconnected.");
                    break;
                } else if(choice.equals("q")) {
                    return;
                }
            }
        }
    }
}
