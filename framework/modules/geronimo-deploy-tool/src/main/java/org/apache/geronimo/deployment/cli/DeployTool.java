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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.CommandFileCommandMetaData;
import org.apache.geronimo.cli.deployer.CommandMetaData;
import org.apache.geronimo.cli.deployer.DeployCommandMetaData;
import org.apache.geronimo.cli.deployer.DeployerCLParser;
import org.apache.geronimo.cli.deployer.DistributeCommandMetaData;
import org.apache.geronimo.cli.deployer.InstallLibraryCommandMetaData;
import org.apache.geronimo.cli.deployer.InstallPluginCommandMetaData;
import org.apache.geronimo.cli.deployer.ListModulesCommandMetaData;
import org.apache.geronimo.cli.deployer.ListTargetsCommandMetaData;
import org.apache.geronimo.cli.deployer.LoginCommandMetaData;
import org.apache.geronimo.cli.deployer.RedeployCommandMetaData;
import org.apache.geronimo.cli.deployer.RestartCommandMetaData;
import org.apache.geronimo.cli.deployer.SearchPluginsCommandMetaData;
import org.apache.geronimo.cli.deployer.StartCommandMetaData;
import org.apache.geronimo.cli.deployer.StopCommandMetaData;
import org.apache.geronimo.cli.deployer.UndeployCommandMetaData;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.Main;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import jline.ConsoleReader;


/**
 * The main class for the CLI deployer.  Handles chunking the input arguments
 * and formatting help text and maintaining the list of individual commands.
 * Uses a ServerConnection to handle the server connection and arguments, and
 * a list of DeployCommands to manage the details of the various available
 * commands.
 *
 * Returns 0 normally, or 1 of any exceptions or error messages were generated
 * (whether for syntax or other failure).
 *
 * @version $Rev$ $Date$
 */
public class DeployTool implements Main {

    private static final IdentityHashMap<CommandMetaData, DeployCommand> commands = new IdentityHashMap<CommandMetaData, DeployCommand>();
    private static final Log log = LogFactory.getLog(DeployTool.class);

    static {
        commands.put(LoginCommandMetaData.META_DATA, new CommandLogin());
        commands.put(DeployCommandMetaData.META_DATA, new CommandDeploy());
        commands.put(DistributeCommandMetaData.META_DATA, new CommandDistribute());
        commands.put(ListModulesCommandMetaData.META_DATA, new CommandListModules());
        commands.put(ListTargetsCommandMetaData.META_DATA, new CommandListTargets());
        commands.put(RedeployCommandMetaData.META_DATA, new CommandRedeploy());
        commands.put(StartCommandMetaData.META_DATA, new CommandStart());
        commands.put(StopCommandMetaData.META_DATA, new CommandStop());
        commands.put(RestartCommandMetaData.META_DATA, new CommandRestart());
        commands.put(UndeployCommandMetaData.META_DATA, new CommandUndeploy());
        commands.put(SearchPluginsCommandMetaData.META_DATA, new CommandListConfigurations());
        commands.put(InstallPluginCommandMetaData.META_DATA, new CommandInstallCAR());
        commands.put(InstallLibraryCommandMetaData.META_DATA, new CommandInstallLibrary());
    }

    private boolean failed = false;
    String[] generalArgs = new String[0];
    ServerConnection con = null;
    private boolean multipleCommands = false;
    private final Kernel kernel;
    private final DeploymentFactory deploymentFactory;

    public DeployTool(Kernel kernel, DeploymentFactory deploymentFactory) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        } else if (null == deploymentFactory) {
            throw new IllegalArgumentException("deploymentFactory is required");
        }
        this.kernel = kernel;
        this.deploymentFactory = deploymentFactory;
    }
    
    public int execute(Object opaque) {
        if (! (opaque instanceof DeployerCLParser)) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected [" + DeployerCLParser.class + "]");
        }
        DeployerCLParser parser = (DeployerCLParser) opaque;
        
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
        InputStream in = System.in;

        CommandMetaData commandMetaData = parser.getCommandMetaData();
        CommandArgs commandArgs = parser.getCommandArgs();
        if(commandMetaData == CommandFileCommandMetaData.META_DATA) {
            multipleCommands = true;
            String arg = commandArgs.getArgs()[0];
            File source = new File(arg);
            if(!source.exists() || !source.canRead() || source.isDirectory()) {
                processException(out, new DeploymentSyntaxException("Cannot read command file "+source.getAbsolutePath()));
            } else {
                try {
                    BufferedReader commands = new BufferedReader(new FileReader(source));
                    String line;
                    boolean oneFailed = false;
                    while((line = commands.readLine()) != null) {
                        line = line.trim();
                        if(!line.equals("")) {
                            String[] lineArgs = splitCommand(line);
                            if(failed) {
                                oneFailed = true;
                            }
                            failed = false;
                            execute(lineArgs);
                        }
                    }
                    failed = oneFailed;
                } catch (IOException e) {
                    processException(out, new DeploymentException("Unable to read command file", e));
                } finally {
                    try {
                        con.close();
                    } catch (DeploymentException e) {
                        processException(out, e);
                    }
                }
            }
        } else {
            DeployCommand dc = commands.get(commandMetaData);
            if(dc == null) {
                out.println();
                processException(out, new DeploymentSyntaxException("No such command: '"+commandMetaData+"'"));
            } else {
                try {
                    if(con == null) {
                        con = new ServerConnection(parser, out, in, kernel, deploymentFactory);
                    }
                    try {
                        dc.execute(new ConsoleReader(in, out), con, commandArgs);
                    } catch (DeploymentSyntaxException e) {
                        processException(out, e);
                    } catch (DeploymentException e) {
                        processException(out, e);
                    } catch (IOException e) {
                        processException(out, e);
                    } finally {
                        if(!multipleCommands) {
                            try {
                                con.close();
                            } catch(DeploymentException e) {
                                processException(out, e);
                            }
                        }
                    }
                } catch(DeploymentException e) {
                    processException(out, e);
                }
            }
        }
        out.flush();
        System.out.flush();
        return failed ? 1 : 0;
    }

    public static String[] splitCommand(String line) {
        String[] chunks = line.split("\"");
        List list = new LinkedList();
        for (int i = 0; i < chunks.length; i++) {
            String chunk = chunks[i];
            if(i % 2 == 1) { // it's in quotes
                list.add(chunk);
            } else { // it's not in quotes
                list.addAll(Arrays.asList(chunk.split("\\s")));
            }
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            String test = (String) it.next();
            if(test.trim().equals("")) {
                it.remove();
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private void processException(PrintWriter out, Exception e) {
        failed = true;
        log.error("Error: ", e);
    }

    public static final GBeanInfo GBEAN_INFO;
    public static final String GBEAN_REF_DEPLOYMENT_FACTORY = "DeploymentFactory";
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("DeployTool", DeployTool.class, "DeployTool");

        infoBuilder.addReference(GBEAN_REF_DEPLOYMENT_FACTORY, DeploymentFactory.class);
        infoBuilder.addInterface(Main.class);
        
        infoBuilder.setConstructor(new String[] {"kernel", GBEAN_REF_DEPLOYMENT_FACTORY});
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
