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

import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.gshell.command.CommandSupport
import org.apache.geronimo.deployment.cli.ServerConnection
import org.apache.geronimo.deployment.cli.ServerConnection.UsernamePasswordHandler
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryWithKernel
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager
import org.apache.geronimo.cli.deployer.ConnectionParamsImpl
import org.apache.geronimo.kernel.basic.BasicKernel
import org.apache.geronimo.gshell.command.annotation.Requirement
import org.apache.geronimo.gshell.console.PromptReader
import java.util.Collections

/**
 * Connect the current shell to a running Geronimo server instance.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:connect', description="Connect to a Geronimo server")
class ConnectCommand
    extends CommandSupport
{
    public static final String SERVER_CONNECTION = 'geronimo.ServerConnection'
    
    @Option(name='-s', aliases=['--hostname', '--server'], description='Hostname, default localhost')
    String hostname = 'localhost'

    @Option(name='-p', aliases=['--port'], description='Port, default 1099')
    int port = 1099

    @Option(name='-u', aliases=['--username'], description='Username')
    String username
    
    @Option(name='-w', aliases=['--password'], description='Password')
    String password
    
    @Option(name='--secure', description='Use secure channel')
    boolean secure = false

    @Requirement
    PromptReader prompter

    protected Object doExecute() throws Exception {
        return openConnection(false)
    }
    
    protected ServerConnection openConnection(boolean quiet) throws Exception {
        if (!quiet) {
        	io.out.println("Connecting to Geronimo server: ${hostname}:${port}")
        }
                
        def kernel = new BasicKernel('gshell deployer')
        def deploymentManager = new RemoteDeploymentManager(Collections.emptySet())
        def deploymentFactory = new DeploymentFactoryWithKernel(kernel, deploymentManager)
        def connectionParams = new ConnectionParamsImpl(host: hostname, port: port, user: username, password: password, offline: false, secure: secure)
        def connection = new ServerConnection(connectionParams, new GShellUserPasswordHandler(prompter), kernel, deploymentFactory)

        // Disconnect previous connection if any
        disconnect()
        
        variables.parent.set(SERVER_CONNECTION, connection)

        if (!quiet) {
            io.out.println('Connection established')
        }
        
        return connection
    }
    
    protected ServerConnection connect() {
        def connection = variables.get(SERVER_CONNECTION)
        
        if (!connection) {
            connection = openConnection(false)
        }
        
        return connection
    }
    
    protected boolean isConnected() {
        return variables.contains(SERVER_CONNECTION)
    }
    
    protected void disconnect() {
        def connection = variables.get(SERVER_CONNECTION)
        
        if (connection) {
            try {
            	connection.close()
            }
            catch (Exception e) {
                // ignore
            }
        }
        
        variables.parent.unset(SERVER_CONNECTION)
    }
}

class GShellUserPasswordHandler implements UsernamePasswordHandler {

    PromptReader prompter
    
    public GShellUserPasswordHandler(PromptReader prompter) {
        this.prompter = prompter
    }
    
    public String getPassword() throws IOException {  
        return prompter.readPassword('Password: ') 
    }

    public String getUsername() throws IOException {
        return prompter.readLine('Username: ')        
    }
}
