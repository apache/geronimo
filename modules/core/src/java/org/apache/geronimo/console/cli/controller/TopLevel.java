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

/**
 * Top-level menu for working with the DeploymentManager.
 *
 * @version $Revision: 1.4 $ $Date: 2004/07/16 04:39:41 $
 */
public class TopLevel extends TextController {
    private static final Log log = LogFactory.getLog(TopLevel.class);

    public TopLevel(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            println("  "+(context.connected ? "--" : "1)")+" Connect to the deployment server");
            println("  "+(context.connected ? "2)" : "--")+" Configure a module");
            println("  "+(context.connected ? "3)" : "--")+" Control working targets");
            println("  "+(context.connected ? "4)" : "--")+" Distribute plan or module");
            println("  "+(context.connected ? "5)" : "--")+" Control existing plans or modules");
            println("  "+(context.connected ? "6)" : "--")+" Disconnect from the deployment server");
            String choice;
            while(true) {
                print("Action ([1-6] or [Q]uit): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("1")) {
                    if( context.connected ) {
                        println("To re-connect, please disconnect first.");
                        continue;
                    }
                    new ConnectDeploymentManager(context).execute();
                    break;
                } else if(choice.equals("2")) {
                    println("Not yet available");
//                    new SelectModule(context).execute();
                } else if(choice.equals("3")) {
                    if( !ensureConnected() ) {
                        continue;
                    }
                    new SelectServer(context).execute();
                    break;
                } else if(choice.equals("4")) {
                    if( !ensureConnected() ) {
                        continue;
                    }
                    new DistributeModule(context).execute();
                    break;
                } else if(choice.equals("5")) {
                    if( !ensureConnected() ) {
                        continue;
                    }
                    new ControlDeployments(context).execute();
                    break;
                } else if(choice.equals("6")) {
                    if( !ensureConnected() ) {
                        continue;
                    }
                    println("Disconnecting from deployment server...");
                    context.deployer.release();
                    println("Disconnected.");
                    break;
                } else if(choice.equals("q")) {
                    return;
                }
            }
        }
    }

}
