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

import java.io.PrintWriter;
import java.io.File;
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
                "module will be started (if the server is running) or marked to start (if " +
                "the server is not running).\n" +
                "Note: To specify a TargetModuleID, use the form TargetName|ModuleName");
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        if(!connection.isOnline()) {
            throw new DeploymentException("This command cannot be run unless connecting to a running server.  Specify --url if server is not running on the default port on localhost.");
        }
        if(args.length < 2) {
            throw new DeploymentSyntaxException("Must specify a module or plan (or both) and one or more module IDs to replace");
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
        File test = new File(args[0]);
        if(!test.exists()) {
            throw new DeploymentSyntaxException("Must specify a module or plan (or both) and one or more module IDs to replace");
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
        test = new File(args[1]);
        if(test.exists() && test.canRead()) {
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
        for(int i=2; i<args.length; i++) {
            modules.addAll(identifyTargetModuleIDs(allModules, args[i]));
        }
        TargetModuleID[] ids = (TargetModuleID[]) modules.toArray(new TargetModuleID[modules.size()]);
        boolean multiple = isMultipleTargets(ids);
        ProgressObject po = mgr.redeploy(ids, module, plan);
        waitForProgress(out, po);
        TargetModuleID[] done = po.getResultTargetModuleIDs();
        for(int i = 0; i < done.length; i++) {
            TargetModuleID id = done[i];
            out.println("Redeployed "+id.getModuleID()+(multiple ? " on "+id.getTarget().getName() : ""));
        }
        if(po.getDeploymentStatus().isFailed()) {
            throw new DeploymentException("Deployment failed, Server reports: "+po.getDeploymentStatus().getMessage());
        }
    }
}
