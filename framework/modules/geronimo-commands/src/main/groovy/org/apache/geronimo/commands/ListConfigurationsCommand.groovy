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
import org.apache.geronimo.deployment.cli.CommandListConfigurations
import org.apache.geronimo.gshell.clp.Argument
import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent

/**
 * install plugins.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent (id = 'geronimo-commands:list-plugins', description = "Install plugins into a geronimo server")
class ListConfigurationsCommand
extends ConnectCommand
{
    @Option (name = '-rr', aliases = ['--refresh-repository'], description = 'refresh repository')
    boolean refreshRepo = false

    @Option (name = '-rl', aliases = ['--refresh-list'], description = 'refresh plugin list')
    boolean refreshList = false

    @Option (name = '-r', aliases = ['--respository'], description = "Repository URL")
    String mavenRepoURL

    @Argument (multiValued = true)
    List<String> pluginArtifacts

    protected Object doExecute() throws Exception {
        def connection = connect()

        def command = new CommandListConfigurations()
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
        def repo = null
        def plugins = null

        if (mavenRepoURL) {
            plugins = command.getPluginCategories(mavenRepoURL, connection.getDeploymentManager(), consoleReader)
            repo = mavenRepoURL
        } else {
            io.out.println("Listing configurations from Geronimo server")

            repo = variables.get("PluginRepository")
            if (refreshRepo || !repo) {
                repo = command.getRepository(consoleReader, connection.getDeploymentManager())
                variables.parent.set("PluginRepository", repo)
            }

            plugins = variables.get("AvailablePlugins")
            if (refreshList || !plugins) {
                plugins = command.getPluginCategories(repo, connection.getDeploymentManager(), consoleReader)
                variables.parent.set("AvailablePlugins", plugins)
            }
        }

        if (plugins) {
            if (pluginArtifacts) {
                command.installPlugins(connection.getDeploymentManager(), pluginArtifacts, plugins, repo, consoleReader, connection)
            } else {
                def pluginsToInstall = command.getInstallList(plugins, consoleReader, repo)
                if (pluginsToInstall) {
                    command.installPlugins(connection.getDeploymentManager(), pluginsToInstall, repo, consoleReader, connection)
                }
            }
        }
        io.out.println("list ended")
    }
}
