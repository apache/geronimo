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

import org.apache.geronimo.deployment.DeploymentException;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.PrintWriter;
import java.io.File;
import java.util.*;

/**
 * The CLI deployer logic to distribute.
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandDistribute extends AbstractCommand {
    public CommandDistribute() {
        super("distribute", "2. Other Commands", "[--targets target,target,...] [module] [plan]",
                "Processes a module and adds it to the server environment, but does "+
                "not start it or mark it to be started in the future." +
                "Normally both a module and plan are passed to the deployer.  " +
                "Sometimes the module contains a plan, or requires no plan, in which case " +
                "the plan may be omitted.  Sometimes the plan references a module already " +
                "deployed in the Geronimo server environment, in which case a module does " +
                "not need to be provided.\n" +
                "If no targets are provided, the module is distributed to all available" +
                "targets.  Geronimo only provides one target (ever), so this is primarily" +
                "useful when using a different driver.\n" +
                "If the server is not running when this command is invoked, no targets may be" +
                "specified.");
    }

    protected CommandDistribute(String command, String group, String helpArgumentList, String helpText) {
        super(command, group, helpArgumentList, helpText);
    }

    protected ProgressObject runCommand(DeploymentManager mgr, PrintWriter out, Target[] tlist, File module, File plan) {
        return mgr.distribute(tlist, module, plan);
    }

    protected String getAction() {
        return "Distributed";
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        if(args.length == 0) {
            throw new DeploymentSyntaxException("Must specify a module or plan (or both)");
        }
        List targets = new ArrayList();
        args = processTargets(args, targets);
        if(targets.size() > 0 && !connection.isOnline()) {
            throw new DeploymentSyntaxException("Cannot specify targets unless connecting to a running server.  Specify --url if server is not running on the default port on localhost.");
        }
        if(args.length > 2) {
            throw new DeploymentSyntaxException("Too many arguments");
        }
        File module = null;
        File plan = null;
        if(args.length > 0) {
            File test = new File(args[0]);
            if(DeployUtils.isJarFile(test)) {
                if(module != null) {
                    throw new DeploymentSyntaxException("Module and plan cannot both be JAR files!");
                }
                module = test;
            } else {
                if(plan != null) {
                    throw new DeploymentSyntaxException("Module or plan must be a JAR file!");
                }
                plan = test;
            }
        }
        if(args.length > 1) {
            File test = new File(args[1]);
            if(DeployUtils.isJarFile(test)) {
                if(module != null) {
                    throw new DeploymentSyntaxException("Module and plan cannot both be JAR files!");
                }
                module = test;
            } else {
                if(plan != null) {
                    throw new DeploymentSyntaxException("Module or plan must be a JAR file!");
                }
                plan = test;
            }
        }
        if(module != null) {
            module = module.getAbsoluteFile();
        }
        if(plan != null) {
            plan = plan.getAbsoluteFile();
        }
        if(connection.isOnline()) {
            executeOnline(connection, targets, out, module, plan);
        } else {
            executeOffline(connection, out, module, plan);
        }
    }

    private void executeOffline(ServerConnection connection, PrintWriter out, File module, File plan) throws DeploymentException {
        List list = (List)connection.invokeOfflineDeployer("deploy", new Object[]{module, plan},
                        new String[]{File.class.getName(), File.class.getName()});
        for(Iterator it = list.iterator(); it.hasNext();) {
            out.println(getAction()+" "+it.next());
        }
    }

    private void executeOnline(ServerConnection connection, List targets, PrintWriter out, File module, File plan) throws DeploymentException {
        final DeploymentManager mgr = connection.getDeploymentManager();
        TargetModuleID[] results;
        boolean multipleTargets;
        if(targets.size() > 0) {
            Target[] tlist = identifyTargets(targets, mgr);
            multipleTargets = tlist.length > 1;
            results = waitForProgress(out, runCommand(mgr, out, tlist, module, plan));
        } else {
            final Target[] tlist = mgr.getTargets();
            multipleTargets = tlist.length > 1;
            results = waitForProgress(out, runCommand(mgr, out, tlist, module, plan));
        }
        for (int i = 0; i < results.length; i++) {
            TargetModuleID result = results[i];
            out.println(getAction()+" "+result.getModuleID()+(multipleTargets ? " to "+result.getTarget().getName() : ""));
        }
    }

    private String[] processTargets(String[] args, List targets) {
        if(args.length >= 2 && args[0].equals("--targets")) {
            String value = args[1];
            StringTokenizer tok = new StringTokenizer(value, ",", false);
            while(tok.hasMoreTokens()) {
                targets.add(tok.nextToken());
            }
            String[] temp = new String[args.length-2];
            System.arraycopy(args, 2, temp, 0, temp.length);
            args = temp;
        }
        return args;
    }
}
