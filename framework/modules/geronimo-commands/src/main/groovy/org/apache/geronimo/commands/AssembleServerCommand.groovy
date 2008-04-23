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
import org.apache.geronimo.kernel.repository.Artifact

/**
 * Extract a bunch of plugins into a server.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent (id = 'geronimo-commands:assemble-server', description = "Extract a geronimo server from the current one")
class AssembleServerCommand
    extends ConnectCommand
{
    @Option (name = '-l', aliases = ['--list'], description = 'refresh plugin list')
    boolean refreshList = false

    @Option (name = '-t', aliases = ['--path'], description = 'assembly location')
    String relativeServerPath = "var/temp/assembly"

    @Option (name = '-g', aliases = ['--groupId'], description = 'server groupId')
    String group

    @Option (name = '-a', aliases = ['--artifact'], description = 'server artifact name')
    String artifact

    @Option (name = '-v', aliases = ['--version'], description = 'server version')
    String version = "1.0"

    @Option (name = '-f', aliases = ['--format'], description = 'zip or tar.gz')
    String format = "tar.gz"

    @Argument (multiValued = true)
    List<String> pluginArtifacts

    protected Object doExecute() throws Exception {
        io.out.println('Listing configurations from Geronimo server')

        def connection = connect()
              
        if (!artifact) {
            artifact = prompter.readLine('Server artifact name: ')
            if (!artifact) {
                throw new IllegalArgumentException('Server artifact name is required')
            }
        }
        
        def command = new CommandListConfigurations()
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
        def plugins = variables.get('LocalPlugins')
        
        if (refreshList || !plugins) {
            plugins = command.getLocalPluginCategories(connection.getDeploymentManager(), consoleReader)
            variables.parent.set('LocalPlugins', plugins)
        }

        if (pluginArtifacts) {
            command.assembleServer(connection.getDeploymentManager(), pluginArtifacts, plugins, 'repository', relativeServerPath, consoleReader)
            connection.getDeploymentManager().archive(relativeServerPath, 'var/temp', new Artifact(group, artifact, (String)version, format));
        }
        else {
            def pluginsToInstall = command.getInstallList(plugins, consoleReader, null)
            
            if (pluginsToInstall) {
                command.assembleServer(connection.getDeploymentManager(), pluginsToInstall, 'repository', relativeServerPath, consoleReader)
                connection.getDeploymentManager().archive(relativeServerPath, 'var/temp', new Artifact(group, artifact, (String)version, format));
            }
        }
        
        io.out.println('list ended')
    }
}