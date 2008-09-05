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
 * Extract a bunch of plugins into a custom assembly server.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent (id = 'geronimo-commands:assemble-server', description = "Extract a geronimo server from the current one")
class AssembleServerCommand
    extends ConnectCommand
{
    @Option (name = '-l', aliases = ['--list'], description = 'refresh plugin list')
    boolean refreshList = false
    
    @Option (name = '-m', aliases = ['--mode'], description = 'custom assembly mode')
    String mode

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
    
        def connection = connect()        
        def command = new CommandListConfigurations()
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
	    
        io.out.println('Available custom assembly modes:')
        io.out.println(' 1:    Function Centric')
        io.out.println(' 2:    Application Centric')
        io.out.println(' 3:    Expert Users')
        
        if (!mode) {
            mode = prompter.readLine('Please select a custom assembly mode [1,2,3]').trim()
            if (!mode || (mode.compareTo("1") != 0 && mode.compareTo("2") != 0 && mode.compareTo("3") != 0)) {
                throw new IllegalArgumentException('Please enter a valid Assembly server mode')
            } 
        }
              
        if (!group) {
            group = prompter.readLine('Assembly server group name: ').trim()
            if (!group) {
                throw new IllegalArgumentException('Assembly server group name is required')
            }
        }
        
        if (!artifact) {
            artifact = prompter.readLine('Assembly server artifact name: ').trim()
            if (!artifact) {
                throw new IllegalArgumentException('Assembly server artifact name is required')
            }
        }
        
	    def plugins = variables.get('LocalPlugins')
	        
	    if (refreshList || !plugins) {
	        plugins = command.getLocalPluginCategories(connection.getDeploymentManager(), consoleReader)
	        variables.parent.set('LocalPlugins', plugins)
	    }
	    
	    if (pluginArtifacts) {
	        command.assembleServer(connection.getDeploymentManager(), pluginArtifacts, plugins, 'repository', relativeServerPath, consoleReader)
            connection.getDeploymentManager().archive(relativeServerPath, 'var/temp', new Artifact(group, artifact, (String)version, format));
	    } else {
	        def pluginsToInstall;
	          	                
            if (mode.compareTo("1") == 0) {
                io.out.println('Listing plugin groups from the local Geronimo server')
	            def pluginGroups = variables.get('LocalPluginGroups')
	        
	            if (refreshList || !pluginGroups) {
	                pluginGroups = command.getLocalPluginGroups(connection.getDeploymentManager(), consoleReader)
	                variables.parent.set('LocalPluginGroups', pluginGroups)
	            }
	            pluginsToInstall = command.getInstallList(pluginGroups, consoleReader, null)
	                    
            } else if (mode.compareTo("2") == 0) {
                io.out.println('Listing application plugins from the local Geronimo server')
	            def appPlugins = variables.get('LocalAppPlugins')
	        
	            if (refreshList || !appPlugins) {
	                appPlugins = command.getLocalApplicationPlugins(connection.getDeploymentManager(), consoleReader)
	                variables.parent.set('LocalAppPlugins', appPlugins)
	            }
	            pluginsToInstall = command.getInstallList(appPlugins, consoleReader, null)
            } else {
                io.out.println('Listing plugins from the local Geronimo server')
                pluginsToInstall = command.getInstallList(plugins, consoleReader, null)
            } 
	            
            if (pluginsToInstall) {
                command.assembleServer(connection.getDeploymentManager(), pluginsToInstall, 'repository', relativeServerPath, consoleReader)
                connection.getDeploymentManager().archive(relativeServerPath, 'var/temp', new Artifact(group, artifact, (String)version, format));            
            }
        }       
    }
}