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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.TextController;

/**
 * List deployed modules for the selected targets
 *
 * @version $Revision: 1.6 $ $Date: 2004/07/23 07:27:50 $
 */
public class ListDeployments extends TextController {
    private static final Log log = LogFactory.getLog(ListDeployments.class);

    private static final ModuleType[] ALL = new ModuleType[] {ModuleType.CAR,
        ModuleType.EAR,
        ModuleType.EJB,
        ModuleType.RAR,
        ModuleType.WAR};
    
    ModuleType type = null;
    boolean selected = false;

    public ListDeployments(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            newScreen("Display Deployments");
            if(!context.connected) {
                println("ERROR: cannot display deployments in disconnected mode.");
                return;
            }
            if(selected) {
                showModules();
                if(!context.connected) {
                    return;
                }
            }
            String choice;
            while(true) {
                context.out.print("Action ([E]JB, [C]lient, [W]eb app, [R]A, [A]pp, A[L]L modules or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("b")) {
                    return;
                } else if(choice.equals("e")) {
                    selected = true;
                    type = ModuleType.EJB;
                    break;
                } else if(choice.equals("c")) {
                    selected = true;
                    type = ModuleType.CAR;
                    break;
                } else if(choice.equals("w")) {
                    selected = true;
                    type = ModuleType.WAR;
                    break;
                } else if(choice.equals("r")) {
                    selected = true;
                    type = ModuleType.RAR;
                    break;
                } else if(choice.equals("a")) {
                    selected = true;
                    type = ModuleType.EAR;
                    break;
                } else if(choice.equals("l")) {
                    selected = true;
                    type = null;
                    break;
                }
            }
        }
    }

    private void showModules() {
        TargetModuleID[] ids;
        try {
            if(type != null) {
                ids = context.deployer.getAvailableModules(type, context.targets);
                if ( null == ids ) {
                    ids = new TargetModuleID[0];
                }
            } else {
                List list = new ArrayList();
                for (int i = 0; i < ALL.length; i++) {
                    TargetModuleID[] modules = context.deployer.getAvailableModules(ALL[i], context.targets);
                    if ( null != modules ) {
                        list.addAll(Arrays.asList(modules));
                    }
                }
                ids = (TargetModuleID[])list.toArray(new TargetModuleID[list.size()]);
            }
            println(ids.length == 0 ? "No matching modules found." : "Found "+ids.length+" matching module"+(ids.length == 1 ? "" : "s"));
            for(int i=0; i<ids.length; i++) {
                println("  "+ids[i].toString());
            }
            println("");
        } catch(TargetException e) {
            println("ERROR: "+e.getMessage());
        } catch(IllegalStateException e) {
            println("ERROR: No longer connected to server.");
            context.connected = false;
        }
    }
}
