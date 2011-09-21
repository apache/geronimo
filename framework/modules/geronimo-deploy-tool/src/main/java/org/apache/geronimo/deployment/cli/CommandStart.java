/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev$ $Date$
 */
public class CommandStart extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        ProgressObject po = null;
        try {
            String[] args = commandArgs.getArgs();

            DeploymentManager mgr = connection.getDeploymentManager();
            Target[] allTargets = mgr.getTargets();
            TargetModuleID[] allModules;
            try {
                allModules = mgr.getAvailableModules(null, allTargets);
            } catch(TargetException e) {
                throw new DeploymentException("Unable to load module list from server", e);
            }
            List modules = new ArrayList();
            for(int i=0; i<args.length; i++) {
                modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, args[i], false));
            }
            TargetModuleID[] ids = (TargetModuleID[]) modules.toArray(new TargetModuleID[modules.size()]);
            boolean multiple = isMultipleTargets(ids);
            po = runCommand(consoleReader, mgr, ids);
            TargetModuleID[] done = po.getResultTargetModuleIDs();
            consoleReader.printNewline();
            for(int i = 0; i < done.length; i++) {
                TargetModuleID id = done[i];
                consoleReader.printString(DeployUtils.reformat((getAction()+" "+id.getModuleID()+((multiple && id.getTarget() != null) ? " on "+ id.getTarget().getName() : "")+(id.getWebURL() == null || !getAction().equals("Started") ? "" : " @ "+id.getWebURL())), 4, 72));
                if(id.getChildTargetModuleID() != null) {
                    for (int j = 0; j < id.getChildTargetModuleID().length; j++) {
                        TargetModuleID child = id.getChildTargetModuleID()[j];
                        consoleReader.printString(DeployUtils.reformat("  `-> "+child.getModuleID()+(child.getWebURL() == null || getAction().toLowerCase().indexOf("started") == -1 ? "" : " @ "+child.getWebURL()),4, 72));
                    }
                } // Also print childs if existing in earlier configuration
                else{
                    java.util.Iterator iterator = DeployUtils.identifyTargetModuleIDs(allModules, id.getModuleID(), false).iterator();
                    if(iterator.hasNext()){
                        TargetModuleID childs = (TargetModuleID)iterator.next();
                        if(childs.getChildTargetModuleID() != null) {
                            for (int j = 0; j < childs.getChildTargetModuleID().length; j++) {
                                TargetModuleID child = childs.getChildTargetModuleID()[j];
                                consoleReader.printString(DeployUtils.reformat("  `-> "+child.getModuleID()+(child.getWebURL() == null || getAction().toLowerCase().indexOf("started") == -1 ? "" : " @ "+child.getWebURL()),4, 72));
                            }
                        }
                    }
                }
//                consoleReader.printNewline();
            }
        } catch (IOException e) {
            throw new DeploymentException("could not write to console", e);
        }
        if(po.getDeploymentStatus().isFailed()) {
            throw new DeploymentException("Operation failed: "+po.getDeploymentStatus().getMessage());
        }
    }

    protected ProgressObject runCommand(ConsoleReader out, DeploymentManager mgr, TargetModuleID[] ids) {
        ProgressObject po = mgr.start(ids);
        waitForProgress(out, po);
        return po;
    }

    protected String getAction() {
        return "Started";
    }

}
