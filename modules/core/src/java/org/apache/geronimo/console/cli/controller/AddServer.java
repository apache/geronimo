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
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.TextController;

/**
 * Add a target to the list of selected targets.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:26 $
 */
public class AddServer extends TextController {
    private static final Log log = LogFactory.getLog(AddServer.class);
    private Target[] all;

    public AddServer(DeploymentContext context, Target[] all) {
        super(context);
        this.all = all;
    }

    public void execute() {
        while(true) {
            Target[] available = available(all, context.targets);
            newScreen("Add Target");
            println("Selected Targets");
            for(int i=0; i<context.targets.length; i++) {
                println("  "+context.targets[i].getName()+" ("+truncate(context.targets[i].getDescription(), 69-context.targets[i].getName().length())+")");
            }
            println("Available Targets");
            for(int i = 0; i < available.length; i++) {
                println("  "+(i+1)+") "+available[i].getName()+" ("+truncate(available[i].getDescription(), 66-available[i].getName().length())+")");
            }
            String choice;
            while(true) {
                context.out.print("Action (Add Target [1"+(available.length > 1 ? "-"+available.length : "")+"] or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("b")) {
                    return;
                } else {
                    int i = 0;
                    try {
                        i = Integer.parseInt(choice);
                    } catch(NumberFormatException e) {
                        continue;
                    }
                    if(i < 1 || i > available.length) {
                        println("  ERROR: There are only "+available.length+" target(s) available");
                    } else {
                        Target[] list = new Target[context.targets.length+1];
                        System.arraycopy(context.targets, 0, list, 0, context.targets.length);
                        list[context.targets.length] = available[i-1];
                        context.targets = list;
                        if(available.length <= 1) {
                            return;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
}
