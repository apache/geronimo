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
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 * The CLI deployer logic to redeploy.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandRedeploy extends AbstractCommand {
    public CommandRedeploy() {
        super("redeploy", "1. Common Commands", "[module] [plan] [ModuleID|TargetModuleID+]",
                "A shortcut to undeploy a module from one or more servers, then " +
                "deploy a new version.  This is not a smooth cutover -- some client " +
                "requests may be rejected while the redeploy takes place.\n" +
                "Normally both a module and plan are passed to the deployer. " +
                "Sometimes the module contains a plan, or requires no plan, in which case " +
                "the plan may be omitted.  Sometimes the plan references a module already " +
                "deployed in the Geronimo server environment, in which case a module does " +
                "not need to be provided.\n" +
                "If more than one TargetModuleID is provided, all TargetModuleIDs " +
                "must refer to the same module (just running on different targets).\n" +
                "Regardless of whether the old module was running or not, the new " +
                "module will be started.\n" +
                "If no ModuleID or TargetModuleID is specified, and you're deploying to "+
                "Geronimo, the deployer will attempt to guess the correct ModuleID for "+
                "you based on the module and/or plan you provided.\n"+
                "Note: To specify a TargetModuleID, use the form TargetName|ModuleName");
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        setOut(out);
        if(!connection.isOnline()) {
            throw new DeploymentException("This command cannot be run unless connecting to a running server.  Specify --url if server is not running on the default port on localhost.");
        }
        if(args.length == 0) {
            throw new DeploymentSyntaxException("Must specify a module or plan (or both) and optionally module IDs to replace");
        }
        DeploymentManager mgr = connection.getDeploymentManager();
        Target[] allTargets = mgr.getTargets();
        TargetModuleID[] allModules = new TargetModuleID[0];
        try {
            allModules = mgr.getAvailableModules(null, allTargets);
        } catch(TargetException e) {
            throw new DeploymentException("Unable to load modules from server", e);
        }
        List modules = new ArrayList();
        File module = null;
        File plan = null;
        File test = new File(args[0]); // Guess whether the first argument is a module or a plan
        if(!test.exists()) {
            throw new DeploymentSyntaxException("Must specify a module or plan (or both) and optionally module IDs to replace");
        }
        if(!test.canRead()) {
            throw new DeploymentException("Cannot read file "+test.getAbsolutePath());
        }
        if(DeployUtils.isJarFile(test) || test.isDirectory()) {
            if(module != null) {
                throw new DeploymentSyntaxException("Module and plan cannot both be JAR files or directories!");
            }
            module = test;
        } else {
            if(plan != null) {
                throw new DeploymentSyntaxException("Module or plan must be a JAR file or directory!");
            }
            plan = test;
        }
        if(args.length > 1) { // Guess whether the second argument is a module, plan, ModuleID, or TargetModuleID
            test = new File(args[1]);
            if(test.exists() && test.canRead() && !args[1].equals(args[0])) {
                if(DeployUtils.isJarFile(test) || test.isDirectory()) {
                    if(module != null) {
                        throw new DeploymentSyntaxException("Module and plan cannot both be JAR files or directories!");
                    }
                    module = test;
                } else {
                    if(plan != null) {
                        throw new DeploymentSyntaxException("Module or plan must be a JAR file or directory!");
                    }
                    plan = test;
                }
            } else {
                modules.addAll(identifyTargetModuleIDs(allModules, args[1]));
            }
        }
        for(int i=2; i<args.length; i++) { // Any arguments beyond 2 must be a ModuleID or TargetModuleID
            modules.addAll(identifyTargetModuleIDs(allModules, args[i]));
        }
        // If we don't have any moduleIDs, try to guess one.
        if(modules.size() == 0 && connection.isGeronimo()) {
            emit("No ModuleID or TargetModuleID provided.  Attempting to guess based on the content of the "+(plan == null ? "archive" : "plan")+".");
            String moduleId = null;
            try {
                if(plan != null) {
                    moduleId = DeployUtils.extractModuleIdFromPlan(plan);
                } else if(module != null) {
                    moduleId = DeployUtils.extractModuleIdFromArchive(module);
                    if(moduleId == null) {
                        int pos = module.getName().lastIndexOf('.');
                        moduleId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                        emit("Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
                    }
                }
            } catch (IOException e) {
                throw new DeploymentException("Unable to read input files: "+e.getMessage());
            }
            if(moduleId != null) {
                emit("Attempting to use ModuleID '"+moduleId+"'");
                modules.addAll(identifyTargetModuleIDs(allModules, moduleId));
            } else {
                emit("Unable to calculate a ModuleID from supplied module and/or plan.");
            }
        }
        if(modules.size() == 0) { // Either not deploying to Geronimo or unable to identify modules
            throw new DeploymentSyntaxException("No ModuleID or TargetModuleID available.  Nothing to do.  Maybe you should add a ModuleID or TargetModuleID to the command line?");
        }
        if(module != null) {
            module = module.getAbsoluteFile();
        }
        if(plan != null) {
            plan = plan.getAbsoluteFile();
        }
        // Now that we've sorted out all the arguments, do the work
        TargetModuleID[] ids = (TargetModuleID[]) modules.toArray(new TargetModuleID[modules.size()]);
        boolean multiple = isMultipleTargets(ids);
        ProgressObject po = mgr.redeploy(ids, module, plan);
        waitForProgress(out, po);
        TargetModuleID[] done = po.getResultTargetModuleIDs();
        for(int i = 0; i < done.length; i++) {
            TargetModuleID id = done[i];
            emit("Redeployed "+id.getModuleID()+(multiple ? " on "+id.getTarget().getName() : "")+(id.getWebURL() == null ? "" : " @ "+id.getWebURL()));
            if(id.getChildTargetModuleID() != null) {
                for (int j = 0; j < id.getChildTargetModuleID().length; j++) {
                    TargetModuleID child = id.getChildTargetModuleID()[j];
                    emit("  `-> "+child.getModuleID()+(child.getWebURL() == null ? "" : " @ "+child.getWebURL()));
                }
            }
        }
        if(po.getDeploymentStatus().isFailed()) {
            throw new DeploymentException("Operation failed: "+po.getDeploymentStatus().getMessage());
        }
    }

}
