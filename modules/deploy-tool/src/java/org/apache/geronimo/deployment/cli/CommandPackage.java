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
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;

/**
 * The CLI deployer logic to create a configuration package.  Can only be run
 * in ofline mode (not via JSR-88).
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class CommandPackage extends AbstractCommand {
    public CommandPackage() {
        super("package", "3. Use if you know what you're doing", "[--classPath path] [--mainClass class] [--install] [module] [plan] fileName",
                "Creates a configuration JAR rather than installing into the server " +
                "environment.  The fileName argument specifies the JAR to create.  The " +
                "optional classPath argument specifies a Class-Path to include in the JAR " +
                "manifest.  The optional mainClass argument specifies the Main-Class to include in " +
                "the JAR manifest.  The install option specifies that the " +
                "configuration should be build into a JAR and also installed into " +
                "the server configuration (otherwise it is packaged but not installed).\n" +
                "The standard arguments may not be used with this command -- it " +
                "never connects to a remote server.");
    }

    public boolean isLocalOnly() {
        return true;
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] argArray) throws DeploymentException {
        if(connection.isOnline()) {
            throw new DeploymentException("This command cannot be run when the server is running.  Make sure the server is shut down first.");
        }

        String classPath = null;
        String mainClass = null;
        String endorsedDirs = null;
        boolean install = false;

        // Read off the optional arguments (clasPath, mainClass, endorsedDirs, and install)
        LinkedList args = new LinkedList(Arrays.asList(argArray));
        for (Iterator iterator = args.iterator(); iterator.hasNext();) {
            String arg = (String) iterator.next();
            if(arg.equals("--classPath")) {
                iterator.remove();
                classPath = (String) iterator.next();
                iterator.remove();
            } else if(arg.equals("--mainClass")) {
                iterator.remove();
                mainClass = (String) iterator.next();
                iterator.remove();
            } else if(arg.equals("--endorsedDirs")) {
                iterator.remove();
                endorsedDirs = (String) iterator.next();
                iterator.remove();
            } else if(arg.equals("--install")) {
                iterator.remove();
                install = true;
            } else if(arg.startsWith("--")) {
                throw new DeploymentSyntaxException("Invalid option '" + arg + "'");
            } else {
                break;
            }
        }

        // if we have any other options on the comman line they are invalid
        for (Iterator iterator = args.iterator(); iterator.hasNext();) {
            String arg = (String) iterator.next();
            if(arg.startsWith("--")) {
                throw new DeploymentSyntaxException("All command line options must appear before module, plan or packageFile: " + arg);
            }
        }

        if(args.isEmpty()) {
            throw new DeploymentSyntaxException("No fileName specified for package command");
        }

        // Read off packageFile which is always the last argument
        File packageFile;
        packageFile = new File((String) args.removeLast());
        File parent = packageFile.getAbsoluteFile().getParentFile();
        if(!parent.exists() || !parent.canWrite()) {
            throw new DeploymentSyntaxException("Cannot write to output file "+packageFile.getAbsolutePath());
        }

        // Read off the plan and module
        File module = null;
        File plan = null;
        if(!args.isEmpty()) {
            // if the arg is a directory or jar file, it must be the module; otherwise it is the plan
            File test = new File((String) args.removeLast()).getAbsoluteFile();
            if(DeployUtils.isJarFile(test) || test.isDirectory()) {
                module = test;
            } else {
                plan = test;
            }
        }
        if(!args.isEmpty()) {
            File test = new File((String) args.removeLast()).getAbsoluteFile();
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
        }

        // are there extra left over args on the command prompt
        if(!args.isEmpty()) {
            throw new DeploymentSyntaxException("Too many arguments for package command");
        }

        // invoke the deployer
        List list = (List) connection.invokeOfflineDeployer(
                new Object[]{
                    plan,
                    module,
                    packageFile,
                    install ? Boolean.TRUE : Boolean.FALSE,
                    mainClass,
                    classPath,
                    endorsedDirs},
                new String[]{
                    File.class.getName(),
                    File.class.getName(),
                    File.class.getName(),
                    boolean.class.getName(),
                    String.class.getName(),
                    String.class.getName(),
                    String.class.getName()});

        // print the configurations created
        for (int j = 0; j < list.size(); j++) {
            out.println("Packaged configuration "+list.get(j)+" to "+packageFile);
        }
    }
}
