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
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Select targets or view, start, stop, or undeploy modules from the targets.
 *
 * @version $Rev$ $Date$
 */
public class ControlDeployments extends TextController {
    private static final Log log = LogFactory.getLog(ControlDeployments.class);
    Boolean running;

    public ControlDeployments(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            newScreen("Control Deployments");
            if(!context.connected) {
                println("ERROR: cannot control deployments in disconnected mode.");
                context.targets = new Target[0];
                return;
            }
            context.out.println((context.targets.length == 0 ? "No" : String.valueOf(context.targets.length))+" target"+(context.targets.length != 1 ? "s" : "")+" currently selected.");
            context.out.println("  1) Select targets (usually servers or clusters) to work with");
            context.out.println("  "+(context.targets.length > 0 ? "2)" : "--")+" Start non-running modules on the selected servers/clusters");
            context.out.println("  "+(context.targets.length > 0 ? "3)" : "--")+" Stop running modules on the selected servers/clusters");
            context.out.println("  "+(context.targets.length > 0 ? "4)" : "--")+" Undeploy modules from the selected servers/clusters");
            context.out.println("  "+(context.targets.length > 0 ? "5)" : "--")+" View modules on the selected servers/clusters");
            String choice;
            while(true) {
                context.out.print("Action ([1"+(context.targets.length > 0 ? "-5" : "")+"] or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                    if(choice.equals("1")) {
                        new SelectServer(context).execute();
                        break;
                    } else if(choice.equals("2")) {
                        if(running == null || running.booleanValue()) {
                            context.modules = new TargetModuleID[0];
                        }
                        new SelectDistributedModules(context, new SelectDistributedModules.NonRunningModules("start")).execute();
                        running = Boolean.FALSE;
                        if(confirmModuleAction("Start")) {
                            ProgressObject po = context.deployer.start(context.modules);
                            if(po != null) {
                                new ProgressMonitor(context, po).execute();
                                if(po.getDeploymentStatus().isCompleted()) {
                                    running = Boolean.TRUE;
                                } else {
                                    running = null;
                                }
                            } else { // assume success
                                running = Boolean.TRUE;
                            }
                        }
                        break;
                    } else if(choice.equals("3")) {
                        if(running == null || !running.booleanValue()) {
                            context.modules = new TargetModuleID[0];
                        }
                        new SelectDistributedModules(context, new SelectDistributedModules.RunningModules("stop")).execute();
                        running = Boolean.TRUE;
                        if(confirmModuleAction("Stop")) {
                            ProgressObject po = context.deployer.stop(context.modules);
                            if(po != null) {
                                new ProgressMonitor(context, po).execute();
                                if(po.getDeploymentStatus().isCompleted()) {
                                    running = Boolean.FALSE;
                                } else {
                                    running = null;
                                }
                            } else { // assume success
                                running = Boolean.FALSE;
                            }
                        }
                        break;
                    } else if(choice.equals("4")) {
                        if(running == null || running.booleanValue()) {
                            context.modules = new TargetModuleID[0];
                        }
                        new SelectDistributedModules(context, new SelectDistributedModules.NonRunningModules("undeploy")).execute();
                        running = Boolean.FALSE;
                        if(confirmModuleAction("Undeploy")) {
                            ProgressObject po = context.deployer.undeploy(context.modules);
                            if(po != null) {
                                new ProgressMonitor(context, po).execute();
                            }
                            running = null;
                        }
                        break;
                    } else if(choice.equals("5")) {
                        new ListDeployments(context).execute();
                        break;
                    } else if(choice.equals("b")) {
                        return;
                    }
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
            }
        }
    }
}
