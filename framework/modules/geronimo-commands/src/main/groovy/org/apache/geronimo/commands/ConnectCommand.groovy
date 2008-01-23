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
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryWithKernel
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager
import org.apache.geronimo.cli.deployer.ConnectionParamsImpl
import org.apache.geronimo.kernel.basic.BasicKernel
import java.util.Collections

/**
 * Stops a running Geronimo server instance.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:connect', description="Connect to a Geronimo server")
class ConnectCommand extends CommandSupport {

    @Option(name='-s', aliases=['--hostname', '--server'], description='Hostname, default localhost')
    String hostname = 'localhost'

    @Option(name='-p', aliases=['--port'], description='port, default 1099')
    int port = 1099

    @Option(name='-u', aliases=['--username'], description='username')
    String username = 'system'

    @Option(name='-w', aliases=['--password'], description='password')
    String password = 'manager'

    protected Object doExecute() throws Exception {
        io.out.println("Connecting to Geronimo server: ${hostname}:${port}")
        
        //
        // TODO: If no password given, then prompt for password
        //
        
        def kernel = new BasicKernel("gshell deployer")
        def deploymentManager = new RemoteDeploymentManager(Collections.emptySet());
        def deploymentFactory = new DeploymentFactoryWithKernel(kernel, deploymentManager)
        def connectionParams = new ConnectionParamsImpl(host: hostname, port: port, user: username, password: password, offline: false)
        def connection = new ServerConnection(connectionParams, io.out, io.inputStream, kernel, deploymentFactory)

        variables.parent.set("ServerConnection", connection)

        io.out.println("Connection established")
        return connection
    }
}