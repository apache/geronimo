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

import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.cli.deployer.ListModulesCommandArgs;
import org.apache.geronimo.deployment.cli.AbstractCommand;
import org.apache.geronimo.deployment.cli.CommandListModules;
import org.apache.geronimo.deployment.cli.ServerConnection;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "list-modules", description = "List modules")
public class ListModulesCommand extends ConnectCommand {

    @Option(name = "-a", aliases = { "--all" }, description = "Show started or stopped modules")
    boolean all = true;

    @Option(name = "-t", aliases = { "--stopped" }, description = "Show stopped modules only")
    boolean stopped = false;

    @Option(name = "-r", aliases = { "--started" }, description = "Show started modules only")
    boolean started = false;

    @Argument(description = "Target name")
    List<String> targets;

    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        AbstractCommand command = new CommandListModules();

        ListModulesCommandArgs args = new ListModulesCommandArgsImpl((targets == null ? new String[0]
                : (String[]) targets.toArray()), all, started, stopped);

        command.execute(this, connection, args);
        return null;
    }

}

class ListModulesCommandArgsImpl implements ListModulesCommandArgs {
    String[] args;

    boolean all;

    boolean started;

    boolean stopped;

    public ListModulesCommandArgsImpl(String[] args, boolean all, boolean started, boolean stopped) {
        this.all = all;
        this.args = args;
        this.started = started;
        this.stopped = stopped;
    }

    public boolean isAll() {
        return all;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String[] getArgs() {
        return args;
    }
}
