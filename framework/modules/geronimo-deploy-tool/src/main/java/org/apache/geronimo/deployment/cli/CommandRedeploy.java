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
import java.util.Arrays;
import java.util.List;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.DistributeCommandArgs;
import org.apache.geronimo.cli.deployer.DistributeCommandArgsImpl;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.JarUtils;

/**
 * The CLI deployer logic to redeploy.
 *
 * @version $Rev$ $Date$
 */
public class CommandRedeploy extends AbstractCommand {

    public void checkFirstArguement(File args) throws DeploymentException {
        if (!args.exists()) {
            throw new DeploymentSyntaxException("Module or plan file does not exist: " + args.getAbsolutePath());
        }
        if (!args.canRead()) {
            throw new DeploymentException("Cannot read file " + args.getAbsolutePath());
        }

    }

    protected String getAction() {
        return "Redeployed";
    }

    public String guessModuleId(List modules, ServerConnection connection, ConsoleReader consoleReader, File plan,
            File module, TargetModuleID[] allModules) throws DeploymentException {
        String moduleId = null;
        try {
            if (modules.size() == 0 && connection.isGeronimo()) {
                emit(consoleReader,
                        "No ModuleID or TargetModuleID provided.  Attempting to guess based on the content of the "
                                + (plan == null ? "archive" : "plan") + ".");
                try {
                    if (plan != null) {
                        moduleId = DeployUtils.extractModuleIdFromPlan(plan);
                        if (moduleId == null) { // plan just doesn't have a
                            // config ID
                            String fileName = module == null ? plan.getName() : module.getName();
                            int pos = fileName.lastIndexOf('.');
                            String artifactId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                            moduleId = Artifact.DEFAULT_GROUP_ID + "/" + artifactId + "_G_MASTER" + "//";
                            emit(consoleReader,
                                    "Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
                        } else {
                            Artifact configId = null;
                            configId = Artifact.create(moduleId);
                            moduleId = configId.getGroupId() + "/" + configId.getArtifactId() + "_G_MASTER" + "/"
                                    + configId.getVersion() + "/" + configId.getType();
                        }
                    } else if (module != null) {
                        moduleId = DeployUtils.extractModuleIdFromArchive(module);
                        if (moduleId == null) {
                            int pos = module.getName().lastIndexOf('.');
                            String artifactId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                            moduleId = Artifact.DEFAULT_GROUP_ID + "/" + artifactId + "_G_MASTER" + "//";
                            emit(consoleReader,
                                    "Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
                        } else {
                            Artifact configId = null;
                            configId = Artifact.create(moduleId);
                            moduleId = configId.getGroupId() + "/" + configId.getArtifactId() + "_G_MASTER" + "/"
                                    + configId.getVersion() + "/" + configId.getType();
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
                throw new DeploymentSyntaxException(
                        "No ModuleID or TargetModuleID available.  Nothing to do.  Maybe you should add a ModuleID or TargetModuleID to the command line?");
            }
        } catch (IOException e) {
            throw new DeploymentException("Unable to read input files: " + e.getMessage(), e);
        }

        return moduleId;
    }

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs)
            throws DeploymentException {

        ProgressObject po;
        DistributeCommandArgs distributeCommandArgs = null;
        try {
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                throw new DeploymentSyntaxException(
                        "Must specify a module or plan (or both) and optionally module IDs to replace");
            }

            DeploymentManager mgr = connection.getDeploymentManager();
            Target[] allTargets = mgr.getTargets();
            TargetModuleID[] allModules;
            try {
                allModules = mgr.getAvailableModules(null, allTargets);
            } catch (TargetException e) {
                throw new DeploymentException("Unable to load modules from server", e);
            }
            if (args.length >= 3 && args[0].equalsIgnoreCase("--targets")) // case of cluster redeployment
            {
                List<TargetModuleID> modules = new ArrayList<TargetModuleID>();
                boolean multipleTargets;
                File test = null;
                File test1 = null;
                File module = null;
                File plan = null;
                test = new File(args[2]); // check whether args[2] is a module or a plan
                checkFirstArguement(test);
                if (JarUtils.isJarFile(test) || test.isDirectory()) {
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
                if (args.length >= 4) {// than it can be plan,moduleId,TargetModuleId
                    test1 = new File(args[3]);
                    if (test1.exists() && test1.canRead()) // check if it is plan
                    {
                        if (JarUtils.isJarFile(test1) || test1.isDirectory()) {
                            if (module != null) {
                                throw new DeploymentSyntaxException(
                                        "Module and plan cannot both be JAR files or directories!");
                            }
                            module = test1;
                        } else {
                            if (plan != null) {
                                throw new DeploymentSyntaxException("Module or plan must be a JAR file or directory!");
                            }
                            plan = test1;
                        }
                    } else
                        modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, args[3], false));
                }
                if (module != null) {
                    module = module.getAbsoluteFile();
                }
                if (plan != null) {
                    plan = plan.getAbsoluteFile();
                }
                if (args.length >= 5) // Amy arguements beyond 4 should be ModuleId or Target ModuleId
                {
                    for (int i = 4; i < args.length; i++) {
                        modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, args[i], false));
                    }
                }
                try {
                    distributeCommandArgs = new DistributeCommandArgsImpl(args);
                } catch (CLParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                List<String> targets = Arrays.asList(distributeCommandArgs.getTargets());
                if (targets.size() > 0) {
                    //Target[] tlist = identifyTargets(targets, mgr);
                    // before starting undeployment and deployment verify the correctness of target argument
                }
                if (modules.size() == 0) {
                    String moduleId = guessModuleId(modules, connection, consoleReader, plan, module, allModules);
                    modules.addAll(DeployUtils.identifyTargetModuleIDs(allModules, moduleId, false));
                }

                TargetModuleID[] ids = modules.toArray(new TargetModuleID[modules.size()]);
                //boolean multiple = isMultipleTargets(ids);
                po = mgr.undeploy(ids);
                waitForProgress(consoleReader, po);
                //TargetModuleID[] done = po.getResultTargetModuleIDs();

                if (targets.size() > 0) {
                    Target[] tlist = identifyTargets(targets, mgr);
                    multipleTargets = tlist.length > 1;
                    po = mgr.distribute(tlist, module, plan);
                    waitForProgress(consoleReader, po);
                } else {
                    Target[] tlist = mgr.getTargets();
                    if (null == tlist) {
                        throw new IllegalStateException("No target to distribute to");
                    }
                    tlist = new Target[] { tlist[0] };
                    multipleTargets = tlist.length > 1;
                    po = mgr.distribute(tlist, module, plan);
                    waitForProgress(consoleReader, po);
                }
                if (po.getDeploymentStatus().isFailed()) {
                    throw new DeploymentException("Unable to redeploy "
                            + (module == null ? plan.getName() : module.getName()) + ": "
                            + po.getDeploymentStatus().getMessage());
                }
                po = mgr.start(po.getResultTargetModuleIDs());
                waitForProgress(consoleReader, po);
                TargetModuleID[] resultsDeployment = po.getResultTargetModuleIDs();
                for (int i = 0; i < resultsDeployment.length; i++) {
                    TargetModuleID result = resultsDeployment[i];
                    consoleReader.printString(DeployUtils.reformat("Deployed"
                            + " "
                            + result.getModuleID()
                            + (multipleTargets ? " to " + result.getTarget().getName() : "")
                            + (result.getWebURL() == null || !getAction().equals("Deployed") ? "" : " @ "
                                    + result.getWebURL()), 4, 72));
                    if (result.getChildTargetModuleID() != null) {
                        for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                            TargetModuleID child = result.getChildTargetModuleID()[j];
                            consoleReader.printString(DeployUtils.reformat("  `-> "
                                    + child.getModuleID()
                                    + (child.getWebURL() == null || !getAction().equals("Deployed") ? "" : " @ "
                                            + child.getWebURL()), 4, 72));
                        }
                    }
                }
                // print the results that succeeded
                TargetModuleID[] results = po.getResultTargetModuleIDs();
                for (int i = 0; i < results.length; i++) {
                    TargetModuleID result = results[i];
                    consoleReader.printString(DeployUtils.reformat(getAction()
                            + " "
                            + result.getModuleID()
                            + (multipleTargets ? " to " + result.getTarget().getName() : "")
                            + (result.getWebURL() == null || !getAction().equals("Deployed") ? "" : " @ "
                                    + result.getWebURL()), 4, 72));
                    if (result.getChildTargetModuleID() != null) {
                        for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                            TargetModuleID child = result.getChildTargetModuleID()[j];
                            consoleReader.printString(DeployUtils.reformat("  `-> "
                                    + child.getModuleID()
                                    + (child.getWebURL() == null || !getAction().equals("Deployed") ? "" : " @ "
                                            + child.getWebURL()), 4, 72));
                        }
                    }
                }
                // if any results failed then throw so that we'll return non-0 to the operating system
                if (po.getDeploymentStatus().isFailed()) {
                    throw new DeploymentException("Operation failed: " + po.getDeploymentStatus().getMessage());
                }
            } else { // case of local redeployment
                List<TargetModuleID> modules = new ArrayList<TargetModuleID>();
                File module = null;
                File plan = null;
                File test = new File(args[0]); // Guess whether the first argument is a module or a plan
                if (!test.exists()) {
                    throw new DeploymentSyntaxException("Module or plan file does not exist: " + test.getAbsolutePath());
                }
                if (!test.canRead()) {
                    throw new DeploymentException("Cannot read file " + test.getAbsolutePath());
                }
                if (JarUtils.isJarFile(test) || test.isDirectory()) {
                    module = test;
                } else {
                    plan = test;
                }
                if (args.length > 1) { // Guess whether the second argument is a module, plan, ModuleID or TargetModuleID
                    test = new File(args[1]);
                    if (test.exists() && test.canRead() && !args[1].equals(args[0])) {
                        if (JarUtils.isJarFile(test) || test.isDirectory()) {
                            if (module != null) {
                                throw new DeploymentSyntaxException(
                                        "Module and plan cannot both be JAR files or directories!");
                            }
                            module = test;
                        } else {
                            if (plan != null) {
                                throw new DeploymentSyntaxException("Module or plan must be a JAR file or directory!");
                            }
                            plan = test;
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
                    emit(consoleReader,
                            "No ModuleID or TargetModuleID provided.  Attempting to guess based on the content of the "
                                    + (plan == null ? "archive" : "plan") + ".");
                    String moduleId = null;
                    try {
                        if (plan != null) {
                            moduleId = DeployUtils.extractModuleIdFromPlan(plan);
                            if (moduleId == null) { // plan just doesn't have a config ID
                                String fileName = module == null ? plan.getName() : module.getName();
                                int pos = fileName.lastIndexOf('.');
                                String artifactId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                                moduleId = Artifact.DEFAULT_GROUP_ID + "/" + artifactId + "//";
                                emit(consoleReader,
                                        "Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
                            }
                        } else if (module != null) {
                            moduleId = DeployUtils.extractModuleIdFromArchive(module);
                            if (moduleId == null) {
                                int pos = module.getName().lastIndexOf('.');
                                String artifactId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
                                moduleId = Artifact.DEFAULT_GROUP_ID + "/" + artifactId + "//";
                                emit(consoleReader,
                                        "Unable to locate Geronimo deployment plan in archive.  Calculating default ModuleID from archive name.");
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
                    throw new DeploymentSyntaxException(
                            "No ModuleID or TargetModuleID available.  Nothing to do.  Maybe you should add a ModuleID or TargetModuleID to the command line?");
                }
                if (module != null) {
                    module = module.getAbsoluteFile();
                }
                if (plan != null) {
                    plan = plan.getAbsoluteFile();
                }
                // Now that we've sorted out all the arguments, do the work
                TargetModuleID[] ids = modules.toArray(new TargetModuleID[modules.size()]);
                boolean multiple = isMultipleTargets(ids);
                po = mgr.redeploy(ids, module, plan);
                waitForProgress(consoleReader, po);
                TargetModuleID[] done = po.getResultTargetModuleIDs();
                for (int i = 0; i < done.length; i++) {
                    TargetModuleID id = done[i];
                    emit(consoleReader, "Redeployed " + id.getModuleID()
                            + (multiple ? " on " + id.getTarget().getName() : "")
                            + (id.getWebURL() == null ? "" : " @ " + id.getWebURL()));
                    if (id.getChildTargetModuleID() != null) {
                        for (int j = 0; j < id.getChildTargetModuleID().length; j++) {
                            TargetModuleID child = id.getChildTargetModuleID()[j];
                            emit(consoleReader, "  `-> " + child.getModuleID()
                                    + (child.getWebURL() == null ? "" : " @ " + child.getWebURL()));
                        }
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
