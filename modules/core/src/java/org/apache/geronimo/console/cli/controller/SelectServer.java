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

package org.apache.geronimo.console.cli.controller;

import java.io.IOException;
import javax.enterprise.deploy.spi.Target;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Select targets to operate on.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:26 $
 */
public class SelectServer extends TextController {
    private static final Log log = LogFactory.getLog(SelectServer.class);
    private Target[] all;
    private Target[] available;
    public SelectServer(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        all = context.deployer.getTargets();
        if(all.length == 0) {
            println("  ERROR: No targets available.");
            println("         Perhaps the deployment manager is not connected?");
            return;
        }
        while(true) {
            available = available(all, context.targets);
            newScreen("Select Targets");
            println((context.targets.length == 0 ? "No" : String.valueOf(context.targets.length))+" target"+(context.targets.length != 1 ? "s" : "")+" currently selected"+((context.targets.length > 0 ? ":" : ".")));
            for(int i=0; i<context.targets.length; i++) {
                println("  "+(i+1)+") "+context.targets[i].getName()+" ("+truncate(context.targets[i].getDescription(), 66-context.targets[i].getName().length())+")");
            }
            String choice;
            while(true) {
                context.out.print("Action ("+(context.targets.length > 0 ? "Remove Target [1"+(context.targets.length > 1 ? "-"+context.targets.length : "")+"] or " : "")+(available.length > 0 ? "[A]dd Target or " : "")+"[B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("a") && available.length > 0) {
                    new AddServer(context, all).execute();
                    break;
                } else if(choice.equals("b")) {
                    return;
                } else {
                    int i = 0;
                    try {
                        i = Integer.parseInt(choice);
                    } catch(NumberFormatException e) {
                        continue;
                    }
                    if(i < 1 || i > context.targets.length) {
                        println("  ERROR: There are only "+context.targets.length+" target(s) selected");
                    } else {
                        Target[] list = new Target[context.targets.length-1];
                        System.arraycopy(context.targets, 0, list, 0, i-1);
                        System.arraycopy(context.targets, i, list, i-1, context.targets.length-i);
                        context.targets = list;
                        break;
                    }
                }
            }
        }
    }
}
