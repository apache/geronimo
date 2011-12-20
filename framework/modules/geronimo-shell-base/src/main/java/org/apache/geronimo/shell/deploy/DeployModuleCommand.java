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

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.cli.deployer.DistributeCommandArgs;
import org.apache.geronimo.cli.deployer.DistributeCommandArgsImpl;
import org.apache.geronimo.deployment.cli.CommandDeploy;
import org.apache.geronimo.deployment.cli.ServerConnection;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "deploy-module", description = "Deploy a module")
public class DeployModuleCommand extends ConnectCommand {

    @Option(name = "-i", aliases = { "--inPlace" }, description = "In-place deployment")
    boolean inPlace;

    @Option(name = "-t", aliases = { "--targets" }, description = "Targets")
    String targets;

    @Argument(required = true, index = 0, description = "Module file")
    String module;

    @Argument(index = 1, description = "Module plan")
    String modulePlan;

    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        CommandDeploy command = new CommandDeploy();

        List<String> commandArgs = new LinkedList<String>();
        commandArgs.add(module);

        if (modulePlan != null) {
            commandArgs.add(modulePlan);
        }

        DistributeCommandArgs args = new DistributeCommandArgsImpl(commandArgs.toArray(new String[commandArgs.size()]));

        command.execute(this, connection, args);

        return null;
    }

}
