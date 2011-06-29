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
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.CommandFileCommandMetaData;
import org.apache.geronimo.cli.deployer.CommandMetaData;
import org.apache.geronimo.cli.deployer.DeployCommandMetaData;
import org.apache.geronimo.cli.deployer.DeployerCLParser;
import org.apache.geronimo.cli.deployer.DistributeCommandMetaData;
import org.apache.geronimo.cli.deployer.EncryptCommandMetaData;
import org.apache.geronimo.cli.deployer.InstallBundleCommandMetaData;
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
import org.apache.geronimo.cli.deployer.UninstallBundleCommandMetaData;
import org.apache.geronimo.cli.deployer.UnlockKeystoreCommandMetaData;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static final Logger log = LoggerFactory.getLogger(DeployTool.class);

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
        commands.put(EncryptCommandMetaData.META_DATA, new CommandEncrypt());
        commands.put(UnlockKeystoreCommandMetaData.META_DATA, new CommandUnlockKeystore());
        commands.put(InstallBundleCommandMetaData.META_DATA, new CommandInstallBundle());
        commands.put(UninstallBundleCommandMetaData.META_DATA, new CommandUninstallBundle());
    }

    private boolean failed = false;
    String[] generalArgs = new String[0];
    ServerConnection con = null;
    private boolean multipleCommands = false;
    private final Kernel kernel;

    public DeployTool(Kernel kernel) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        this.kernel = kernel;
    }

    public int execute(Object opaque) {
        if (! (opaque instanceof DeployerCLParser)) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected [" + DeployerCLParser.class + "]");
        }
        DeployerCLParser parser = (DeployerCLParser) opaque;

        ConsoleReader consoleReader = new StreamConsoleReader(System.in, System.out);

        CommandMetaData commandMetaData = parser.getCommandMetaData();
        CommandArgs commandArgs = parser.getCommandArgs();
        if(commandMetaData == CommandFileCommandMetaData.META_DATA) {
            multipleCommands = true;
            String arg = commandArgs.getArgs()[0];
            File source = new File(arg);
            if(!source.exists() || !source.canRead() || source.isDirectory()) {
                processException(new DeploymentSyntaxException("Cannot read command file "+source.getAbsolutePath()));
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
                    processException(new DeploymentException("Unable to read command file", e));
                } finally {
                    try {
                        con.close();
                    } catch (DeploymentException e) {
                        processException(e);
                    }
                }
            }
        } else {
            DeployCommand dc = commands.get(commandMetaData);
            if(dc == null) {
                try {
                    consoleReader.printNewline();
                } catch (IOException e) {
                }
                processException(new DeploymentSyntaxException("No such command: '"+commandMetaData+"'"));
            } else {
                try {
                    if (con == null) {
                        if (parser.isOffline()) {
                            con = new OfflineServerConnection(kernel, true);
                        } else {
                            con = new OnlineServerConnection(parser, consoleReader);
                        }
                    }
                    try {
                        dc.execute(consoleReader, con, commandArgs);
                    } catch (DeploymentSyntaxException e) {
                        processException( e);
                    } catch (DeploymentException e) {
                        processException(e);
                    } finally {
                        if(!multipleCommands) {
                            try {
                                con.close();
                            } catch(DeploymentException e) {
                                processException(e);
                            }
                        }
                    }
                } catch(DeploymentException e) {
                    processException(e);
                }
            }
        }
        try {
            consoleReader.flushConsole();
        } catch (IOException e) {
        }
        return failed ? 1 : 0;
    }

    public static String[] splitCommand(String line) {
        String[] chunks = line.split("\"");
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < chunks.length; i++) {
            String chunk = chunks[i];
            if(i % 2 == 1) { // it's in quotes
                list.add(chunk);
            } else { // it's not in quotes
                list.addAll(Arrays.asList(chunk.split("\\s")));
            }
        }
        for (Iterator<String> it = list.iterator(); it.hasNext();) {
            String test = it.next();
            if(test.trim().equals("")) {
                it.remove();
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private void processException(Exception e) {
        failed = true;
        log.error("Error: ", e);
    }

    public static final GBeanInfo GBEAN_INFO;
    public static final String GBEAN_REF_DEPLOYMENT_FACTORY = "DeploymentFactory";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("DeployTool", DeployTool.class, "DeployTool");

        infoBuilder.addInterface(Main.class);

        infoBuilder.setConstructor(new String[] {"kernel"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
