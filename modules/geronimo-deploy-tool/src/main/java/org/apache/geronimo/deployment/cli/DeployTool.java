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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.Main;


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

    private static final Collection<DeployCommand> commands = new ArrayList<DeployCommand>();

    static {
        commands.add(new CommandLogin());
        commands.add(new CommandDeploy());
        commands.add(new CommandDistribute());
        commands.add(new CommandListModules());
        commands.add(new CommandListTargets());
        commands.add(new CommandRedeploy());
        commands.add(new CommandStart());
        commands.add(new CommandStop());
        commands.add(new CommandRestart());
        commands.add(new CommandUndeploy());
        commands.add(new CommandListConfigurations());
        commands.add(new CommandInstallCAR());
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
    
    public int execute(String args[]) {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
        InputStream in = System.in;

        String command;
        String[] commandArgs = new String[0];
        if(args.length == 0) {
            command = "help";
        } else {
            String[] temp = getCommonArgsAndCommand(args);
            if(temp == null || temp.length == 0) {
                command = "help";
            } else {
                command = temp[temp.length-1];
                if(generalArgs.length == 0 && temp.length > 1) {
                    generalArgs = new String[temp.length-1];
                    System.arraycopy(temp, 0, generalArgs, 0, temp.length-1);
                }
                commandArgs = new String[args.length - temp.length];
                System.arraycopy(args, temp.length, commandArgs, 0, commandArgs.length);
            }
        }
        if(command.equals("help")) {
            showHelp(out, commandArgs);
        } else if(command.equals("command-file")) {
            multipleCommands = true;
            if(commandArgs.length != 1) {
                processException(out, new DeploymentSyntaxException("Must provide a command file to read from and no other arguments"));
            } else {
                String arg = commandArgs[0];
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
            }
        } else {
            DeployCommand dc = getCommand(command);
            if(dc == null) {
                out.println();
                processException(out, new DeploymentSyntaxException("No such command: '"+command+"'"));
                showHelp(out, new String[0]);
            } else {
                try {
                    if(con == null) {
                        con = new ServerConnection(generalArgs, out, in, kernel);
                    }
                    try {
                        dc.execute(out, con, commandArgs);
                    } catch (DeploymentSyntaxException e) {
                        processException(out, e);
                    } catch (DeploymentException e) {
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
        out.print(DeployUtils.reformat("Error: "+e.getMessage(),4,72));
        if(e.getCause() != null) {
            e.getCause().printStackTrace(out);
        }
    }

    private void showHelp(PrintWriter out, String[] args) {
        out.println();
        out.println("Command-line deployer syntax:");
        out.println("    deployer [general options] command [command options]");
        out.println();
        if(args.length > 0) {
            DeployCommand command = getCommand(args[0]);
            if(command != null) {
                out.println("Help for command: "+command.getCommandName());
                out.println();
                out.println("    deployer "+hangingIndent(command.getCommandName()+" "+command.getHelpArgumentList(), 13));
                out.println();
                out.print(DeployUtils.reformat(command.getHelpText(), 8, 72));
                out.println();
                return;
            } else if(args[0].equals("options")) {
                out.println("Help on general options:");
                out.println();
                Map map = ServerConnection.getOptionHelp();
                for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                    String s = (String) it.next();
                    out.println("   "+s);
                    out.println();
                    out.print(DeployUtils.reformat((String)map.get(s), 8, 72));
                }
                return;
            } else if(args[0].equals("all")) {
                DeployCommand[] all = getAllCommands();
                out.println();
                out.println("All commands");
                out.println();
                for (int i = 0; i < all.length; i++) {
                    DeployCommand cmd = all[i];
                    out.println("    deployer "+hangingIndent(cmd.getCommandName()+" "+cmd.getHelpArgumentList(), 13));
                    out.print(DeployUtils.reformat(cmd.getHelpText(), 8, 72));
                }
                out.println();
                return;
            }
        }
        out.println("The general options are:");
        Map map = ServerConnection.getOptionHelp();
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String s = (String) it.next();
            out.println("    "+s);
        }
        out.println();
        out.println("The available commands are:");
        renderCommandList(out, getAllCommands());
        out.println();
        out.println("For more information about a specific command, run");
        out.println("    deployer help [command name]");
        out.println();
        out.println("For more information about general options, run");
        out.println("    deployer help options");
        out.println();
    }

    private String hangingIndent(String source, int cols) {
        String s = DeployUtils.reformat(source, cols, 72);
        return s.substring(cols);
    }

    private void renderCommandList(PrintWriter out, DeployCommand[] all) {
        Map temp = new HashMap();
        for (int i = 0; i < all.length; i++) {
            DeployCommand command = all[i];
            List list = (List) temp.get(command.getCommandGroup());
            if(list == null) {
                list = new ArrayList();
                temp.put(command.getCommandGroup(), list);
            }
            list.add(command.getCommandName());
        }
        List groups = new ArrayList(temp.keySet());
        Collections.sort(groups);
        for (int i = 0; i < groups.size(); i++) {
            String name = (String) groups.get(i);
            out.println("    "+name);
            List list = (List) temp.get(name);
            Collections.sort(list);
            for (int j = 0; j < list.size(); j++) {
                String cmd = (String) list.get(j);
                out.println("        "+cmd);
            }
        }
    }

    private String[] getCommonArgsAndCommand(String[] all) {
        List list = new ArrayList();
        for (int i = 0; i < all.length; i++) {
            String s = all[i];
            boolean option = ServerConnection.isGeneralOption(list, s);
            list.add(s);
            if(!option) {
                break;
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private DeployCommand getCommand(String commandName) {
        for (DeployCommand command : commands) {
            if (command.getCommandName().equals(commandName)) {
                return command;
            }
        }
        return null;
    }

    private DeployCommand[] getAllCommands() {
        DeployCommand[] list = (DeployCommand[]) commands.toArray(new DeployCommand[0]);
        Arrays.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((DeployCommand)o1).getCommandName().compareTo(((DeployCommand)o2).getCommandName());
            }
        });
        return list;
    }
    
    
    public static final GBeanInfo GBEAN_INFO;
    
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
