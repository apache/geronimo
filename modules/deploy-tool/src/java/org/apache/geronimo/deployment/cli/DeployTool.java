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

import java.util.*;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;

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
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DeployTool {
    private final static Map commands = new HashMap();

    public static void registerCommand(DeployCommand command) {
        String key = command.getCommandName();
        if(commands.containsKey(key)) {
            throw new IllegalArgumentException("Command "+key+" is already registered!");
        } else {
            commands.put(key, command);
        }
    }

    private static DeployCommand getCommand(String name) {
        return (DeployCommand) commands.get(name);
    }

    private static DeployCommand[] getAllCommands() {
        DeployCommand[] list = (DeployCommand[]) commands.values().toArray(new DeployCommand[0]);
        Arrays.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((DeployCommand)o1).getCommandName().compareTo(((DeployCommand)o2).getCommandName());
            }
        });
        return list;
    }

    static {
        registerCommand(new CommandDeploy());
        registerCommand(new CommandDistribute());
        registerCommand(new CommandListModules());
        registerCommand(new CommandListTargets());
        registerCommand(new CommandPackage());
        registerCommand(new CommandRedeploy());
        registerCommand(new CommandStart());
        registerCommand(new CommandStop());
        registerCommand(new CommandUndeploy());
    }

    private boolean failed = false;

    public boolean execute(String args[]) {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String command;
        String[] generalArgs = new String[0];
        String[] commandArgs = new String[0];
        if(args.length == 0) {
            command = "help";
        } else {
            String[] temp = getCommonArgsAndCommand(args);
            if(temp == null || temp.length == 0) {
                command = "help";
            } else {
                command = temp[temp.length-1];
                generalArgs = new String[temp.length-1];
                System.arraycopy(temp, 0, generalArgs, 0, temp.length-1);
                commandArgs = new String[args.length - temp.length];
                System.arraycopy(args, temp.length, commandArgs, 0, commandArgs.length);
            }
        }
        if(command.equals("help")) {
            showHelp(out, commandArgs);
        } else {
            DeployCommand dc = getCommand(command);
            if(dc == null) {
                out.println();
                processException(out, new DeploymentSyntaxException("No such command: '"+command+"'"));
                showHelp(out, new String[0]);
            } else {
                ServerConnection con = null;
                try {
                    con = new ServerConnection(generalArgs, dc.isLocalOnly(), out, in);
                    try {
                        dc.execute(out, con, commandArgs);
                    } catch (DeploymentSyntaxException e) {
                        processException(out, e);
                    } catch (DeploymentException e) {
                        processException(out, e);
                    } finally {
                        try {
                            con.close();
                        } catch(DeploymentException e) {
                            processException(out, e);
                        }
                    }
                } catch(DeploymentException e) {
                    processException(out, e);
                }
            }
        }
        out.flush();
        System.out.flush();
        return !failed;
    }

    private void processException(PrintWriter out, Exception e) {
        failed = true;
        out.println(DeployUtils.reformat("Error: "+e.getMessage(),4,72));
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
                out.println(DeployUtils.reformat(command.getHelpText(), 8, 72));
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
                    out.println(DeployUtils.reformat((String)map.get(s), 8, 72));
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
                    out.println(DeployUtils.reformat(cmd.getHelpText(), 8, 72));
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

    public static void main(String[] args) {
        if(!new DeployTool().execute(args)) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
