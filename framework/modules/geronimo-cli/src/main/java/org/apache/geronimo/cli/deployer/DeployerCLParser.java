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
package org.apache.geronimo.cli.deployer;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.geronimo.cli.BaseCLParser;
import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.cli.PrintHelper;


/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class DeployerCLParser extends BaseCLParser implements ConnectionParams {
    private final static String ARGUMENT_URI_SHORTFORM = "U";
    private final static String ARGUMENT_URI = "uri";
    
    private final static String ARGUMENT_HOST_SHORTFORM = "host";
    
    private final static String ARGUMENT_PORT_SHORTFORM = "port";
    private final static String ARGUMENT_PORT = "port";

    private final static String ARGUMENT_DRIVER_SHORTFORM = "d";
    private final static String ARGUMENT_DRIVER = "driver";

    private final static String ARGUMENT_USER_SHORTFORM = "u";
    private final static String ARGUMENT_USER = "user";

    private final static String ARGUMENT_PASSWORD_SHORTFORM = "p";
    private final static String ARGUMENT_PASSWORD = "password";

    private final static String ARGUMENT_SYSERR_SHORTFORM = "s";
    private final static String ARGUMENT_SYSERR = "syserr";

    private final static String ARGUMENT_VERBOSE_SHORTFORM = "v";
    private final static String ARGUMENT_VERBOSE = "verbose";

    private final static String ARGUMENT_OFFLINE_SHORTFORM = "o";
    private final static String ARGUMENT_OFFLINE = "offline";
    
    private final static String ARGUMENT_SECURE_SHORTFORM = "secure";
    private final static String ARGUMENT_SECURE = "secure";

    private final Collection<CommandMetaData> commandMetaData;

    private CommandArgs commandArgs;
    private CommandMetaData metaData;
    
    public DeployerCLParser(OutputStream out) {
        super(out);
        
        commandMetaData = new ArrayList<CommandMetaData>();
        commandMetaData.add(LoginCommandMetaData.META_DATA);
        commandMetaData.add(DeployCommandMetaData.META_DATA);
        commandMetaData.add(DistributeCommandMetaData.META_DATA);
        commandMetaData.add(ListModulesCommandMetaData.META_DATA);
        commandMetaData.add(ListTargetsCommandMetaData.META_DATA);
        commandMetaData.add(RedeployCommandMetaData.META_DATA);
        commandMetaData.add(StartCommandMetaData.META_DATA);
        commandMetaData.add(StopCommandMetaData.META_DATA);
        commandMetaData.add(RestartCommandMetaData.META_DATA);
        commandMetaData.add(UndeployCommandMetaData.META_DATA);
        commandMetaData.add(SearchPluginsCommandMetaData.META_DATA);
        commandMetaData.add(InstallPluginCommandMetaData.META_DATA);
        commandMetaData.add(HelpCommandMetaData.META_DATA);
        commandMetaData.add(InstallLibraryCommandMetaData.META_DATA);
        commandMetaData.add(EncryptCommandMetaData.META_DATA);

        addURI();
        addHost();
        addPort();
        addDriver();
        addUser();
        addPassword();
        addSyserr();
        addVerbose();
        addOffline();
        addSecure();
    }
    
    public CommandMetaData getCommandMetaData() {
        return metaData;
    }

    public CommandArgs getCommandArgs() {
        return commandArgs;
    }
    
    public String getURI() {
        return commandLine.getOptionValue(ARGUMENT_URI_SHORTFORM);
    }
    
    public String getHost() {
        return commandLine.getOptionValue(ARGUMENT_HOST_SHORTFORM);
    }
    
    public Integer getPort() {
        String port = commandLine.getOptionValue(ARGUMENT_PORT_SHORTFORM);
        if (null == port) {
            return null;
        }
        return new Integer(port);
    }

    public String getDriver() {
        return commandLine.getOptionValue(ARGUMENT_DRIVER_SHORTFORM);
    }
    
    public String getUser() {
        return commandLine.getOptionValue(ARGUMENT_USER_SHORTFORM);
    }
    
    public String getPassword() {
        return commandLine.getOptionValue(ARGUMENT_PASSWORD_SHORTFORM);
    }
    
    public boolean isSyserr() {
        return commandLine.hasOption(ARGUMENT_SYSERR_SHORTFORM);
    }
    
    public boolean isVerbose() {
        return commandLine.hasOption(ARGUMENT_VERBOSE_SHORTFORM);
    }
    
    public boolean isOffline() {
        return commandLine.hasOption(ARGUMENT_OFFLINE_SHORTFORM);
    }
    
    public boolean isSecure() {
        return commandLine.hasOption(ARGUMENT_SECURE_SHORTFORM);
    }
    
    @Override
    public void displayHelp() {
        String[] args = new String[0];
        if (null != commandArgs) {
            args = commandArgs.getArgs();
        } else if (null != metaData) {
            args = new String[] {metaData.getCommandName()};
        }
        displayHelp(args);
    }

    @Override
    protected void displayHelp(String[] args) {
        PrintHelper printHelper = new PrintHelper(System.out);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);

        out.println();
        if(args.length > 0) {
            CommandMetaData commandLine = getCommandMetaData(args[0]);
            if(commandLine != null) {
                out.println("Help for command: "+commandLine.getCommandName());
                out.println("    "+hangingIndent(commandLine.getCommandName()+" "+commandLine.getHelpArgumentList(), 4));
                out.println();
                out.print(PrintHelper.reformat(commandLine.getHelpText(), 8, 72));
                out.println();
                return;
            } else if(args[0].equals("options")) {
                out.println("Help on general options:");
                printHelper.printOptions(out, options);
                out.println();
                return;
            } else if(args[0].equals("all")) {
                out.println();
                out.println("All commands");
                out.println();
                for (CommandMetaData commandLine2 : commandMetaData) {
                    out.println("    "+hangingIndent(commandLine2.getCommandName()+" "+commandLine2.getHelpArgumentList(), 4));
                    out.print(PrintHelper.reformat(commandLine2.getHelpText(), 8, 72));
                    out.println();
                }
                out.println();
                return;
            }
        }

        out.println("usage: java -jar bin/deployer.jar [general options] command [command options]");
        out.println();
        out.println("The general options are:");
        printHelper.printOptionsNoDesc(out, options);
        out.println();
        out.println("The available commands are:");
        renderCommandList(out);
        out.println();
        out.println("For more information about a specific command, run");
        out.println("    java -jar bin/deployer.jar help [command name]");
        out.println();
        out.println("For more information about all commands, run");
        out.println("    java -jar bin/deployer.jar help all");
        out.println();
        out.println("For more information about general options, run");
        out.println("    java -jar bin/deployer.jar help options");
        out.println();
    }

    private void renderCommandList(PrintWriter out) {
        Map temp = new HashMap();
        for (CommandMetaData commandLine : commandMetaData) {
            List list = (List) temp.get(commandLine.getCommandGroup());
            if(list == null) {
                list = new ArrayList();
                temp.put(commandLine.getCommandGroup(), list);
            }
            list.add(commandLine.getCommandName());
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
    
    protected CommandMetaData getCommandMetaData(String commandName) {
        for (CommandMetaData commandLine : commandMetaData) {
            if (commandLine.getCommandName().equals(commandName)) {
                return commandLine;
            }
        }
        return null;
    }

    protected String hangingIndent(String source, int cols) {
        String s = PrintHelper.reformat(source, cols, 72);
        return s.substring(cols);
    }
    
    @Override
    protected void validateOptions() throws CLParserException {
        try {
            getPort();
        } catch (NumberFormatException e) {
            throw new CLParserException("Port [" + commandLine.getOptionValue(ARGUMENT_PORT_SHORTFORM) + "] is not an integer.", e);
        }
    }

    @Override
    protected void validateRemainingArgs() throws CLParserException {
        String[] args = commandLine.getArgs();
        if (0 == args.length) {
            throw new CLParserException("No command has been provided.");
        }
        
        String command = args[0];
        metaData = getCommandMetaData(command);
        if (null == metaData) {
            throw new CLParserException("Command [" + command + "] is undefined.");
        }
        
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        commandArgs = metaData.parse(newArgs);
    }
    
    protected void addOffline() {
        options.addOption(ARGUMENT_OFFLINE_SHORTFORM,
                ARGUMENT_OFFLINE,
                false,
                "Deploy offline to a local server, using whatever deployers are available in the local server");
    }
    
    protected void addSecure() {
        options.addOption(ARGUMENT_SECURE_SHORTFORM,
                ARGUMENT_SECURE,
                false,
                "Use secure channel to communicate with the server.  Unsecured channel is used by default.");
    }

    protected void addVerbose() {
        options.addOption(ARGUMENT_VERBOSE_SHORTFORM,
                ARGUMENT_VERBOSE,
                false,
                "Enables verbose execution mode.  Disabled by default.");
    }

    protected void addSyserr() {
        options.addOption(ARGUMENT_SYSERR_SHORTFORM,
                ARGUMENT_SYSERR,
                false,
                "Enables error logging to syserr.  Disabled by default.");
    }

    protected void addPassword() {
        addOptionWithParam(ARGUMENT_PASSWORD,
                ARGUMENT_PASSWORD_SHORTFORM,
                "password",
                "Specifies a password to use to authenticate to the server.");
    }

    protected void addUser() {
        addOptionWithParam(ARGUMENT_USER,
                ARGUMENT_USER_SHORTFORM,
                "username",
                "If the deployment operation requires authentication, then you can "
                        + "specify the username to use to connect.  If no password is specified, the "
                        + "deployer will attempt to connect to the server with no password, and if "
                        + "that fails, will prompt you for a password.");
    }

    protected void addDriver() {
        addOptionWithParam(ARGUMENT_DRIVER,
                ARGUMENT_DRIVER_SHORTFORM,
                "driver.jar",
                "If you want to use this tool with a server other than Geronimo, "
                        + "then you must provide the path to its driver JAR.  Currently, manifest "
                        + "Class-Path entries in that JAR are ignored.");
    }

    protected void addPort() {
        addOptionWithParam(ARGUMENT_PORT,
                ARGUMENT_PORT_SHORTFORM,
                "port",
                "The RMI listen port of a Geronimo server to deploy to.  This option is "
                        + "not compatible with --uri, but is often used with --host.  The default port is 1099.");
    }

    protected void addHost() {
        addOptionWithParam(ARGUMENT_HOST_SHORTFORM,
                ARGUMENT_HOST_SHORTFORM,
                "hostname",
                "The host name of a Geronimo server to deploy to.  This option is "
                        + "not compatible with --uri, but is often used with --port.");
    }

    protected void addURI() {
        addOptionWithParam(ARGUMENT_URI,
                ARGUMENT_URI_SHORTFORM,
                "uri",
                "A URI to contact the server.  If not specified, the deployer defaults to "
                        + "operating on a Geronimo server running on the standard port on localhost.\n"
                        + "A URI to connect to Geronimo (including optional host and port parameters) has the form: "
                        + "deployer:geronimo:jmx[://host[:port]] (though you could also just use --host and --port instead).");
    }

}
