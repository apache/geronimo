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

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.DeploymentManager;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Base class for CLI deployer commands.  Tracks some simple properties and
 * has common utility methods.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public abstract class AbstractCommand implements DeployCommand {
    private String command;
    private String group;
    private String helpArgumentList;
    private String helpText;

    public AbstractCommand(String command, String group, String helpArgumentList, String helpText) {
        this.command = command;
        this.group = group;
        this.helpArgumentList = helpArgumentList;
        this.helpText = helpText;
    }

    public String getCommandName() {
        return command;
    }

    public String getHelpArgumentList() {
        return helpArgumentList;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getCommandGroup() {
        return group;
    }

    public boolean isLocalOnly() {
        return false;
    }

    protected static TargetModuleID[] waitForProgress(PrintWriter out, ProgressObject po) {
        while(po.getDeploymentStatus().isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(po.getDeploymentStatus().isFailed()) {
            out.println("Deployment failed");
            out.println("  Server reports: "+po.getDeploymentStatus().getMessage());
        }
        return po.getResultTargetModuleIDs();
    }

    protected static boolean isMultipleTargets(TargetModuleID[] ids) {
        Set set = new HashSet();
        for(int i = 0; i < ids.length; i++) {
            TargetModuleID id = ids[i];
            set.add(id.getTarget().getName());
        }
        return set.size() > 1;
    }

    protected static Target[] identifyTargets(List targetNames, final DeploymentManager mgr) throws DeploymentException {
        Target[] tlist = new Target[targetNames.size()];
        Target[] all = mgr.getTargets();
        Set found = new HashSet();
        for (int i = 0; i < tlist.length; i++) {
            if(found.contains(targetNames.get(i))) {
                throw new DeploymentException("Target list should not contain duplicates ("+targetNames.get(i)+")");
            }
            for (int j = 0; j < all.length; j++) {
                Target server = all[j];
                if(server.getName().equals(targetNames.get(i))) {
                    tlist[i] = server;
                    found.add(server.getName());
                    break;
                }
            }
            if(tlist[i] == null) {
                throw new DeploymentException("No target named '"+targetNames.get(i)+"' was found");
            }
        }
        return tlist;
    }

    protected static Collection identifyTargetModuleIDs(TargetModuleID[] allModules, String name) throws DeploymentException {
        List list = new LinkedList();
        int pos;
        if((pos = name.indexOf('|')) > -1) {
            String target = name.substring(0, pos);
            String module = name.substring(pos+1);
            // First pass: exact match
            for(int i=0; i<allModules.length; i++) {
                if(allModules[i].getTarget().getName().equals(target) && allModules[i].getModuleID().equals(module)) {
                    list.add(allModules[i]);
                }
            }
        }
        if(!list.isEmpty()) {
            return list;
        }
        // second pass: module matches
        for(int i = 0; i < allModules.length; i++) {
            if(allModules[i].getModuleID().equals(name)) {
                list.add(allModules[i]);
            }
        }
        if(list.isEmpty()) {
            throw new DeploymentException(name+" does not appear to be a module name or a TargetModuleID.  For a TargetModuleID, specify it as TargetName|ModuleName");
        }
        return list;
    }
}
