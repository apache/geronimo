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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Chooses a set of distributed but running or not running modules, so the
 * caller can start, stop, or undeploy, or redeploy them.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:26 $
 */
public class SelectDistributedModules extends TextController {
    private static final Log log = LogFactory.getLog(SelectDistributedModules.class);
    private Runner runner;

    /**
     * Pass in either a new SelectDistributedModules.RunningModules or a new
     * SelectDistributedModules.NonRunningModules to indicate whether the user
     * is selecting running or non-running modules.
     */
    public SelectDistributedModules(DeploymentContext context, Runner runner) {
        super(context);
        this.runner = runner;
    }

    public void execute() {
        while(true) {
            newScreen("Select Modules to "+runner.getAction());
            if(!context.connected) {
                println("ERROR: cannot "+runner.getAction()+" modules in disconnected mode.");
                context.targets = new Target[0];
                context.modules = new TargetModuleID[0];
                return;
            }
            println((context.targets.length == 0 ? "No" : String.valueOf(context.targets.length))+" target"+(context.targets.length != 1 ? "s" : "")+" currently selected.");
            println((context.modules.length == 0 ? "No" : String.valueOf(context.modules.length))+" module"+(context.modules.length != 1 ? "s" : "")+" currently selected"+(context.modules.length > 0 ? ":" : "."));
            for(int i=0; i<context.modules.length; i++) {
                println("  "+(i+1)+") "+context.modules[i]);
            }
            String choice;
            while(true) {
                context.out.print("Action ("+(context.modules.length > 0 ? "Remove Module [1"+(context.modules.length > 1 ? "-"+context.modules.length : "")+"], " : "")+"[A]dd Module, manage [T]argets, or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("a")) {
                    executeAdd();
                    break;
                } else if(choice.equals("b")) {
                    return;
                } else if(choice.equals("t")) {
                    new SelectServer(context).execute();
                    break;
                } else {
                    int i = 0;
                    try {
                        i = Integer.parseInt(choice);
                    } catch(NumberFormatException e) {
                        continue;
                    }
                    if(i < 1 || i > context.modules.length) {
                        println("  ERROR: There are only "+context.modules.length+" module(s) selected");
                    } else {
                        TargetModuleID[] list = new TargetModuleID[context.modules.length-1];
                        System.arraycopy(context.modules, 0, list, 0, i-1);
                        System.arraycopy(context.modules, i, list, i-1, context.modules.length-i);
                        context.modules = list;
                        break;
                    }
                }
            }
        }
    }

    private boolean selected;
    private ModuleType type;

    public void executeAdd() {
        selected = false;
        while(true) {
            TargetModuleID[] available = new TargetModuleID[0];
            newScreen("Add Module");
            if(!context.connected) {
                println("ERROR: cannot select modules in disconnected mode.");
                return;
            }

            if(context.modules.length > 0) {
                println("Selected Modules");
                for(int i=0; i<context.modules.length; i++) {
                    println("  "+context.modules[i]);
                }
            }

            if(selected) {
                available = showModules();
                if(!context.connected) {
                    return;
                }
            }


            String choice;
            while(true) {
                print("List [E]JB, [C]lient, [W]eb app, [R]A, [A]pp, A[L]L modules");
                if(available.length > 0) {
                    println("");
                    print("  or add Module [1"+(available.length > 1 ? "-"+available.length : "")+"]");
                }
                print(" or [B]ack): ");
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
                } else {
                    int i = 0;
                    try {
                        i = Integer.parseInt(choice);
                    } catch(NumberFormatException e) {
                        continue;
                    }
                    if(i < 1 || i > available.length) {
                        println("  ERROR: There are only "+available.length+" module(s) available");
                    } else {
                        TargetModuleID[] list = new TargetModuleID[context.modules.length+1];
                        System.arraycopy(context.modules, 0, list, 0, context.modules.length);
                        list[context.modules.length] = available[i-1];
                        context.modules = list;
                        break;
                    }
                }
            }
        }
    }

    private TargetModuleID[] showModules() {
        TargetModuleID[] ids;
        try {
            if(type != null) {
                ids = runner.getModules(type, context);
            } else {
                List list = new ArrayList();
                list.addAll(Arrays.asList(runner.getModules(ModuleType.CAR, context)));
                list.addAll(Arrays.asList(runner.getModules(ModuleType.EAR, context)));
                list.addAll(Arrays.asList(runner.getModules(ModuleType.EJB, context)));
                list.addAll(Arrays.asList(runner.getModules(ModuleType.RAR, context)));
                list.addAll(Arrays.asList(runner.getModules(ModuleType.WAR, context)));
                ids = (TargetModuleID[])list.toArray(new TargetModuleID[list.size()]);
            }
            ids = available(ids, context.modules);
            println(ids.length == 0 ? "No matching modules found." : "Found "+ids.length+" matching module"+(ids.length == 1 ? "" : "s"));
            for(int i=0; i<ids.length; i++) {
                println("  "+(i+1)+") "+ids[i]);
            }
            println("");
            return ids;
        } catch(TargetException e) {
            println("ERROR: "+e.getMessage());
        } catch(IllegalStateException e) {
            println("ERROR: No longer connected to server.");
            context.connected = false;
        }
        return new TargetModuleID[0];
    }

    private static abstract class Runner {
        private final String action;

        /**
         * @param action The action that the user is trying to perform on the
         *               selected modules (i.e. "start" or "undeploy").
         */
        public Runner(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        abstract TargetModuleID[] getModules(ModuleType type, DeploymentContext context) throws TargetException;
    }

    public static class RunningModules extends Runner {
        /**
         * @param action The action that the user is trying to perform on the
         *               selected modules (i.e. "stop" or "redeploy").
         */
        public RunningModules(String action) {
            super(action);
        }

        public TargetModuleID[] getModules(ModuleType type, DeploymentContext context) throws TargetException {
            return context.deployer.getRunningModules(type, context.targets);
        }
    }

    public static class NonRunningModules extends Runner {
        /**
         * @param action The action that the user is trying to perform on the
         *               selected modules (i.e. "start" or "undeploy").
         */
        public NonRunningModules(String action) {
            super(action);
        }

        public TargetModuleID[] getModules(ModuleType type, DeploymentContext context) throws TargetException {
            return context.deployer.getNonRunningModules(type, context.targets);
        }
    }
}
