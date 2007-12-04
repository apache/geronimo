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

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.gshell.command.annotation.Requirement
import org.apache.geronimo.gshell.command.CommandExecutor
import org.apache.geronimo.gshell.command.CommandSupport
import org.apache.geronimo.gshell.command.IO

/**
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:remote-server-control', description="Remote server control")
class RemoteServerControlCommand extends CommandSupport {

    @Requirement
    CommandExecutor executor

    @Option(name='-c', aliases=['--server-configuration'], description='Server configuration file')
    String configurationFileName = 'etc/server-configuration.xml'

    @Argument(metaVar="CONTROL", required=true, index=0, description="Control")
    String control

    @Argument(metaVar="SERVER", required=true, index=1, description="Server")
    String serverName

    protected Object doExecute() throws Exception {
        def configurationFile = new File(configurationFileName)
        if (!configurationFile.exists()) {
            throw new IllegalStateException("File ${configurationFile.absolutePath} does not exist")
        }
        
        def result
        configurationFile.withInputStream {
            result = executeRshCommand(it)
        }
        result
    }

	protected executeRshCommand(is) {
        def configuration = new XmlSlurper().parse(is)

        def retrieveByNameAttribute = { baseNode, name ->
	        def node = baseNode.find { it.@name.text().equals(name) }
	        if (node.isEmpty()) {
	            def availableNames = baseNode.list().sort{ it.@name.text() }.'@name'*.text()
	            throw new IllegalArgumentException("${name} does not exist. Available: ${availableNames}")
	        }
	        node
        }

        def server = retrieveByNameAttribute(configuration.servers.server, serverName)
        def host = retrieveByNameAttribute(configuration.hosts.host, server.host.@name.text())

        def rshCmd = host.gshell.'remote-login-cmd'.text()
        if ('' == rshCmd) {
            throw new IllegalStateException("gshell.remote-login-cmd is not defined by host ${host.@name.text()}")
        }
        
	    def command = server.controls."${control}".text()
	    if ('' == command) {
            def controls = server.controls.'*'.list().sort{ it.name() }*.name()
	        throw new IllegalStateException("server.controls.${control} is not defined by server "
	            + "${server.@name.text()}.\nAvailable controls: ${controls}")
	    }
        
        executor.execute("${rshCmd} ${command}")
	}

}
