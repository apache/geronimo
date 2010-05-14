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
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.DistributeCommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.kernel.util.JarUtils;

/**
 * The CLI deployer logic to distribute.
 *
 * @version $Rev$ $Date$
 */
public class CommandDistribute extends AbstractCommand {

    protected ProgressObject runCommand(DeploymentManager mgr, ConsoleReader out, boolean inPlace, Target[] tlist, File module, File plan) throws DeploymentException {
        if (inPlace) {
            if (!(mgr instanceof JMXDeploymentManager)) {
                throw new DeploymentSyntaxException(
                        "Target DeploymentManager is not a Geronimo one. \n" +
                        "Cannot perform in-place deployment.");
            }
            JMXDeploymentManager jmxMgr = (JMXDeploymentManager) mgr;
            try {
                jmxMgr.setInPlace(true);
                return mgr.distribute(tlist, module, plan);
            } finally {
                jmxMgr.setInPlace(false);
            }
        } else {
            return mgr.distribute(tlist, module, plan);
        }
    }

    protected String getAction() {
        return "Distributed";
    }

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        if (!(commandArgs instanceof DistributeCommandArgs)) {
            throw new DeploymentSyntaxException("CommandArgs has the type [" + commandArgs.getClass() + "]; expected [" + DistributeCommandArgs.class + "]");
        }
        DistributeCommandArgs distributeCommandArgs = (DistributeCommandArgs) commandArgs;

        BooleanHolder inPlaceHolder = new BooleanHolder();
        inPlaceHolder.inPlace = distributeCommandArgs.isInPlace();

        List<String> targets = Arrays.asList(distributeCommandArgs.getTargets());

        String[] args = distributeCommandArgs.getArgs();
        File module = null;
        File plan = null;
        if (args.length > 0) {
            File test = new File(args[0]);
            try {
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
            } catch (IOException e) {
                throw new DeploymentException("File not found: " + args[0]);
            }
        }
        if (args.length > 1) {
            File test = new File(args[1]);
            try {
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
            } catch (IOException e) {
                throw new DeploymentException("File not found:" + args[1]);
            }
        }
        if (module != null) {
            module = module.getAbsoluteFile();
        }
        if (plan != null) {
            plan = plan.getAbsoluteFile();
        }
        try {
            executeOnline(connection, inPlaceHolder.inPlace, targets, consoleReader, module, plan);
        } catch (IOException e) {
            throw new DeploymentException("Could not write to output", e);
        }
    }

    private void executeOnline(ServerConnection connection, boolean inPlace, List targets, ConsoleReader out, File module, File plan) throws DeploymentException, IOException {
        final DeploymentManager mgr = connection.getDeploymentManager();
        TargetModuleID[] results;
        boolean multipleTargets;
        ProgressObject po;
        if(targets.size() > 0) {
            Target[] tlist = identifyTargets(targets, mgr);
            multipleTargets = tlist.length > 1;
            po = runCommand(mgr, out, inPlace, tlist, module, plan);
            waitForProgress(out, po);
        } else {
            Target[] tlist = mgr.getTargets();
            if (null == tlist) {
                throw new IllegalStateException("No target to distribute to");
            }
            tlist = new Target[] {tlist[0]};

            multipleTargets = tlist.length > 1;
            po = runCommand(mgr, out, inPlace, tlist, module, plan);
            waitForProgress(out, po);
        }

        // print the results that succeeded
        results = po.getResultTargetModuleIDs();
        for (TargetModuleID result : results) {
            out.printString(DeployUtils.reformat(
                    getAction() + " " + result.getModuleID() + (multipleTargets ? " to " + result.getTarget().getName() : "") + (result.getWebURL() == null || !getAction().equals("Deployed") ? "" : " @ " + result.getWebURL()), 4, 72));
            if (result.getChildTargetModuleID() != null) {
                for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                    TargetModuleID child = result.getChildTargetModuleID()[j];
                    out.printString(DeployUtils.reformat("  `-> " + child.getModuleID() + (child.getWebURL() == null || !getAction().equals("Deployed") ? "" : " @ " + child.getWebURL()), 4, 72));
                }
            }
        }

        // if any results failed then throw so that we'll return non-0
        // to the operating system
        if(po.getDeploymentStatus().isFailed()) {
            throw new DeploymentException("Operation failed: "+po.getDeploymentStatus().getMessage());
        }
    }

    private String[] processTargets(String[] args, List targets) {
        if(args.length >= 2 && args[0].equals("--targets")) {
            String value = args[1];
            StringTokenizer tok = new StringTokenizer(value, ";", false);
            while(tok.hasMoreTokens()) {
                targets.add(tok.nextToken());
            }
            String[] temp = new String[args.length-2];
            System.arraycopy(args, 2, temp, 0, temp.length);
            args = temp;
        }
        return args;
    }

    private String[] processInPlace(String[] args, BooleanHolder inPlaceHolder) {
        if(args.length >= 2 && args[0].equals("--inPlace")) {
        	inPlaceHolder.inPlace = true;
            String[] temp = new String[args.length - 1];
            System.arraycopy(args, 1, temp, 0, temp.length);
            args = temp;
        }
        return args;
    }

    private final class BooleanHolder {
    	public boolean inPlace;
    }
}
