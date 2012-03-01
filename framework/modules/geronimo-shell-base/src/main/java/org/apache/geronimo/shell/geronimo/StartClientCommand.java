/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.shell.geronimo;

import java.io.File;
import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.tools.ant.ExitStatusException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "geronimo", name = "start-client", description = "Start a Geronimo application client")
public class StartClientCommand extends BaseJavaCommand {

    @Option(name = "-v", aliases = { "--verbose" }, description = "Enable verbose output; specify multiple times to increase verbosity")
    boolean verbose = false;

    @Argument(index = 0, required = true, description = "Configuration name of application client")
    String moduleName;

    @Argument(index = 1, multiValued = true, description = "Application client arguments")
    List<String> moduleArguments;

    protected Object doExecute() throws Exception {
        ant = new AntBuilder(log);

        if (geronimoHome == null) {
            geronimoHome = this.bundleContext.getProperty("org.apache.geronimo.home.dir");
        }

        if (geronimoBase == null) {
            geronimoBase = this.bundleContext.getProperty("org.apache.geronimo.server.dir");
        }

        log.debug("Geronimo home: " + geronimoHome);

        // Set the properties which we pass to the JVM from the startup script
        properties.put("org.apache.geronimo.home.dir", geronimoHome);
        properties.put("org.apache.geronimo.server.dir", geronimoBase);
        properties.put("karaf.home", geronimoHome);
        properties.put("karaf.base", geronimoBase);
        // Use relative path
        properties.put("java.io.tmpdir", geronimoBase + "/var/temp");// Don't use relative path
        properties.put("java.endorsed.dirs", prefixSystemPath("java.endorsed.dirs", new File(geronimoHome, "lib/endorsed")));
        properties.put("java.ext.dirs", prefixSystemPath("java.ext.dirs", new File(geronimoHome, "lib/ext")));
        // set console properties
        properties.put("karaf.startLocalConsole", "false");
        properties.put("karaf.startRemoteShell", "false");
                
          // Setup default java flags
        if (getJavaAgentJar() != null && getJavaAgentJar().exists()) {
            javaFlags.add("-javaagent:" + getJavaAgentJar().getCanonicalPath());
        }
                 
        //init properties
        if (propertyFrom != null) {
            for(String nameValue : propertyFrom){
                addPropertyFrom(nameValue, null);
            }
        }
        if (gPropertyFrom != null) {
            for(String nameValue : gPropertyFrom){
                addPropertyFrom(nameValue, "org.apache.geronimo");
            }
        }

        ProcessLauncher launcher = new ProcessLauncher(log, "Geronimo Client", background,session.getConsole()) {
            @Override
            protected void process() throws Exception {
                try {
                    Java javaTask = (Java) ant.createTask("java");
                    javaTask.setClassname("org.apache.geronimo.cli.client.ClientCLI");
                    Path path = javaTask.createClasspath();
                    File libDir = new File(geronimoHome, "lib");
                    FileSet fileSet = new FileSet();
                    fileSet.setDir(libDir);
                    path.addFileset(fileSet);
                    javaTask.setDir(new File(geronimoHome));
                    javaTask.setFailonerror(true);
                    javaTask.setFork(true);
                    
                    if (timeout > 0) {
                        log.info("Timeout after: " + timeout + " seconds");
                        javaTask.setTimeout((long) timeout);
                    }

                    if (logFile != null) {
                        log.info("Redirecting output to: " + logFile);
                        File output = new File(logFile);
                        output.mkdirs();
                        javaTask.setOutput(output);
                    }

                    if (javaVirtualMachine != null) {
                        if (!(new File(javaVirtualMachine).exists())) {
                            throw new Exception("Java virtual machine is not valid: " + javaVirtualMachine);
                        }

                        log.info("Using Java virtual machine: " + javaVirtualMachine);
                        javaTask.setJvm(javaVirtualMachine);
                    }

                    if (javaFlags != null) {
                        for (String javaFlag : javaFlags) {
                            javaTask.createJvmarg().setValue(javaFlag);
                        }
                    }
                    
                    for (String i : properties.keySet()) {
                        Variable sysp = new Variable();
                        sysp.setKey(i);
                        sysp.setValue(properties.get(i));
                        javaTask.addSysproperty(sysp);
                    }

                    if (verbose) {
                        javaTask.createArg().setValue("--verbose");
                    }
                    
                    javaTask.createArg().setValue(moduleName);

                    if (moduleArguments != null) {
                        for (String m : moduleArguments) {
                            javaTask.createArg().setValue(m);
                        }
                    }
                    
                    javaTask.execute();
                } catch (ExitStatusException e) {
                    String tmp = "";
                    log.info(tmp);

                    if (e.getStatus() != 0) {
                        log.warn(tmp);
                    }

                    tmp = "Process exited with status: " + e.getStatus();

                    throw e;
                }
            }
        };

        launcher.launch();

        return null;
    }
}
