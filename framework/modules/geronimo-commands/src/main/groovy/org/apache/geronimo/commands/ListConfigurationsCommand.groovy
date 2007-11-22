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

package org.apache.geronimo.commands


import jline.ConsoleReader

import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.gshell.command.CommandSupport
import org.apache.geronimo.deployment.cli.ServerConnection
import org.apache.geronimo.cli.deployer.BaseCommandArgs
import org.apache.geronimo.deployment.cli.CommandListConfigurations

/**
 * install plugins.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:list-plugins', description="Install plugins into a geronimo server")
class ListConfigurationsCommand
    extends ConnectCommand
{
    @Option(name='-r', aliases=['--repository'], description='refresh repository')
    boolean refreshRepo = false

    @Option(name='-l', aliases=['--list'], description='refresh plugin list')
    boolean refreshList = false

    protected Object doExecute() throws Exception {
        io.out.println("Listing configurations from Geronimo server")

        def connection = variables.get("ServerConnection")
        if (!connection) {
            //def connectCommand = new ConnectCommand()
            //connectCommand.init(context)
            connection = super.doExecute()
        }
        def command = new CommandListConfigurations()
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
        def repo = variables.get("PluginRepository")
        if (refreshRepo || !repo) {
            repo = command.getRepository(consoleReader, connection.getDeploymentManager())
            variables.parent.set("PluginRepository", repo)
        }
        def plugins = variables.get("AvailablePlugins")
        if (refreshList || !plugins) {
            plugins = command.getPluginCategories(repo, connection.getDeploymentManager(), consoleReader)
            variables.parent.set("AvailablePlugins", plugins)
        }
        def pluginsToInstall = command.getInstallList(plugins, consoleReader, repo)
        if (pluginsToInstall) {
            command.installPlugins(connection.getDeploymentManager(), pluginsToInstall, consoleReader, connection)
        }
        io.out.println("list ended")
    }
}