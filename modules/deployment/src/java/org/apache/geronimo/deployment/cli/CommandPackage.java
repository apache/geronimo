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

import java.io.PrintWriter;
import java.io.File;
import java.util.List;

/**
 * The CLI deployer logic to create a configuration package.  Can only be run
 * in ofline mode (not via JSR-88).
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandPackage extends AbstractCommand {
    public CommandPackage() {
        super("package", "3. Use if you know what you're doing", "[--classPath path] [--mainClass class] [module] [plan] fileName",
                "Creates a configuration JAR rather than installing into the server " +
                "environment.  The fileName argument specifies the JAR to create.  The " +
                "optional classPath argument specifies a Class-Path to include in the JAR " +
                "manifest.  The mainClass argument specifies the Main-Class to include in " +
                "the JAR manifest.\n" +
                "The standard arguments may not be used with this command -- it " +
                "never connects to a remote server.");
    }

    public boolean isLocalOnly() {
        return true;
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        if(connection.isOnline()) {
            throw new DeploymentException("This command cannot be run when the server is running.  Make sure the server is shut down first.");
        }
        String classPath = null;
        String mainClass = null;
        File module = null;
        File plan = null;
        File packageFile;
        int i;
        for(i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("--classPath")) {
                classPath = args[++i];
            } else if(arg.equals("--mainClass")) {
                mainClass = args[++i];
            } else if(arg.startsWith("--")) {
                throw new DeploymentSyntaxException("Invalid argument '"+arg+"'");
            } else {
                break;
            }
        }
        if(i >= args.length) {
            throw new DeploymentSyntaxException("No fileName specified for package command");
        }
        packageFile = new File(args[args.length-1]);
        File parent = packageFile.getAbsoluteFile().getParentFile();
        if(!parent.exists() || !parent.canWrite()) {
            throw new DeploymentSyntaxException("Cannot write to output file "+packageFile.getAbsolutePath());
        }
        if(i < args.length-1) {
            File test = new File(args[args.length-2]);
            if(DeployUtils.isJarFile(test)) {
                if(module != null) {
                    throw new DeploymentSyntaxException("Module and plan cannot both be JAR files!");
                }
                module = test;
            } else {
                if(plan != null) {
                    throw new DeploymentSyntaxException("Module or plan must be a text file!");
                }
                plan = test;
            }
        }
        if(i < args.length-2) {
            File test = new File(args[args.length-2]);
            if(DeployUtils.isJarFile(test)) {
                if(module != null) {
                    throw new DeploymentSyntaxException("Module and plan cannot both be JAR files!");
                }
                module = test;
            } else {
                if(plan != null) {
                    throw new DeploymentSyntaxException("Module or plan must be a text file!");
                }
                plan = test;
            }
        }
        if(i < args.length - 3) {
            throw new DeploymentSyntaxException("Too many arguments for deploy command");
        }
        if(module != null) {
            module = module.getAbsoluteFile();
        }
        if(plan != null) {
            plan = plan.getAbsoluteFile();
        }
        List list = (List)connection.invokeOfflineDeployer("deploy", new Object[]{plan, module, packageFile, Boolean.TRUE, mainClass, classPath},
                        new String[]{File.class.getName(), File.class.getName(), File.class.getName(), boolean.class.getName(), String.class.getName(), String.class.getName()});
        for (int j = 0; j < list.size(); j++) {
            out.println("Packaged configuration "+list.get(j)+" to "+packageFile);
        }
    }
}
