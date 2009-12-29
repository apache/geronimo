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

package org.apache.geronimo.shell.deploy;

import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.geronimo.cli.deployer.InstallLibraryCommandArgs;
import org.apache.geronimo.deployment.cli.CommandInstallLibrary;
import org.apache.geronimo.deployment.cli.ConsoleReader;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.cli.StreamConsoleReader;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "install-library", description = "Install library")
public class InstallLibraryCommand extends ConnectCommand {

    @Option(name = "-g", aliases = { "--groupId" }, description = "Group id")
    String groupId;

    @Argument(required = true, description = "Library file")
    String libraryFile;

    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        CommandInstallLibrary command = new CommandInstallLibrary();

        ConsoleReader consoleReader = new StreamConsoleReader(session.getKeyboard(),new PrintWriter(session.getConsole(),true));

        InstallLibraryCommandArgs args = new InstallLibraryCommandArgsImpl(Arrays.asList(libraryFile).toArray(
                new String[1]), groupId);

        command.execute(consoleReader, connection, args);
        return null;
    }

}

class InstallLibraryCommandArgsImpl implements InstallLibraryCommandArgs {
    String[] args;

    String groupId;

    public InstallLibraryCommandArgsImpl(String[] args, String groupId) {
        this.args = args;
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String[] getArgs() {
        return args;
    }
}
