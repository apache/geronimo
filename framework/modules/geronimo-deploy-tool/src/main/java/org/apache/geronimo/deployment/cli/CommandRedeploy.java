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

import java.io.File;
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
import org.apache.geronimo.common.FileUtils;
import org.apache.geronimo.kernel.repository.Artifact;
import jline.ConsoleReader;

/**
 * The CLI deployer logic to redeploy.
 *
 * @version $Rev$ $Date$
 */
public class CommandRedeploy extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        ProgressObject po;
        try {
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                throw new DeploymentSyntaxException("Must specify a module or plan (or both) and optionally module IDs to replace");
            }

            DeploymentManager mgr = connection.getDeploymentManager();
            Target[] allTargets = mgr.getTargets();
            TargetModuleID[] allModules;
            try {
                allModules = mgr.getAvailableModules(null, allTargets);
            } catch (TargetException e) {
                throw new DeploymentException("Unable to load modules from server", e);
            }

            List modules = new ArrayList();
            File module = null;
            File plan = null;
            File test = new File(args[0]); // Guess whether the first argument is a module or a plan
            if (!test.exists()) {
                throw new DeploymentSyntaxException("Module or plan file does not exist: " + test.getAbsolutePath());
            }
            if (!test.canRead()) {
                throw new DeploymentException("Cannot read file " + test.getAbsolutePath());
            }
            try {
                if (FileUtils.isJarFile(test) || test.isDirectory()) {
                    module = test;
                } else {
                    plan = test;
                }
            } catch (IOException e) {
                throw new DeploymentException("Invalid JAR File " + args[0]);
            }
            if (args.length > 1) { // Guess whether the second argument is a module, plan, ModuleID, or TargetModuleID
                test = new File(args[1]);
                if (test.exists() && test.canRead() && !args[1].equals(args[0])) {
                    try {
                        if (FileUtils.isJarFile(test) || test.isDirectory()) {
                            if (module != null) {
                                throw new DeploymentSyntaxException("Module and plan cannot both be JAR files or directories!");
                            }
                            module = test;
                        } else {
                            if (plan != null) {
                                throw new DeploymentSyntaxException("Module or plan must be a JAR file or directory!");
                            }
                            plan = test;
                        }
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid JAR File " + args[1]);
                    }
                } else {
                    modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, args[1], false));
                }
            }
            for (int i = 2; i < args.length; i++) { // Any arguments beyond 2 must be a ModuleID or TargetModuleID
                modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, args[i], false));
            }
            // If we don't have any moduleIDs, try to guess one.
            if (modules.size() == 0 && connection.isGeronimo()) {
                emit(consoleReader, "No ModuleID or TargetModuleID provided.  Attempting to guess based on the content of the " + (plan == null ? "archive" : "plan") + ".");
                String moduleId = null;
                try {
                    if (plan != null) {
                        moduleId = DeployUtils.extractModuleIdFromPlan(plan);
                        if (moduleId == null) { // plan just doesn't have a config ID
                            String fileName = module == null ? plan.getName() : module.getName();
                            int pos = fileName.lastIndexOf('.');
                            String artifactId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                            moduleId = Artifact.DEFAULT_GROUP_ID + "/" + artifactId + "//";
                            emit(consoleReader, "Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
                        }
                    } else if (module != null) {
                        moduleId = DeployUtils.extractModuleIdFromArchive(module);
                        if (moduleId == null) {
                            int pos = module.getName().lastIndexOf('.');
                            String artifactId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                            moduleId = Artifact.DEFAULT_GROUP_ID + "/" + artifactId + "//";
                            emit(consoleReader, "Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
                        }
                    }
                } catch (IOException e) {
                    throw new DeploymentException("Unable to read input files: " + e.getMessage(), e);
                }
                if (moduleId != null) {
                    emit(consoleReader, "Attempting to use ModuleID '" + moduleId + "'");
                    modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, moduleId, true));
                } else {
                    emit(consoleReader, "Unable to calculate a ModuleID from supplied module and/or plan.");
                }
            }
            if (modules.size() == 0) { // Either not deploying to Geronimo or unable to identify modules
                throw new DeploymentSyntaxException("No ModuleID or TargetModuleID available.  Nothing to do.  Maybe you should add a ModuleID or TargetModuleID to the command line?");
            }
            if (module != null) {
                module = module.getAbsoluteFile();
            }
            if (plan != null) {
                plan = plan.getAbsoluteFile();
            }
            // Now that we've sorted out all the arguments, do the work
            TargetModuleID[] ids = (TargetModuleID[]) modules.toArray(new TargetModuleID[modules.size()]);
            boolean multiple = isMultipleTargets(ids);
            po = mgr.redeploy(ids, module, plan);
            waitForProgress(consoleReader, po);
            TargetModuleID[] done = po.getResultTargetModuleIDs();
            for (TargetModuleID id : done) {
                emit(consoleReader, "Redeployed " + id.getModuleID() + (multiple ? " on " + id.getTarget().getName() : "") + (id.getWebURL() == null ? "" : " @ " + id.getWebURL()));
                if (id.getChildTargetModuleID() != null) {
                    for (int j = 0; j < id.getChildTargetModuleID().length; j++) {
                        TargetModuleID child = id.getChildTargetModuleID()[j];
                        emit(consoleReader, "  `-> " + child.getModuleID() + (child.getWebURL() == null ? "" : " @ " + child.getWebURL()));
                    }
                }
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not write to console", e);
        }
        if (po.getDeploymentStatus().isFailed()) {
            throw new DeploymentException("Operation failed: " + po.getDeploymentStatus().getMessage());
        }
    }

}
