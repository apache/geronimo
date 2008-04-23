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
import org.apache.geronimo.gshell.command.annotation.Requirement
import org.apache.geronimo.gshell.console.PromptReader
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager

/**
 * Stops a running Geronimo server instance.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id='geronimo-commands:stop-server', description='Stop a Geronimo server')
class StopServerCommand
    extends ConnectCommand
{
    protected Object doExecute() throws Exception {
        def connection = connect()
        
        def server = new ServerProxy(connection.deploymentManager.getJMXConnector())

        io.out.println('Stopping Geronimo server...')
        
        try {
            server.shutdown()
            
            io.out.println('Shutdown request has been issued')
        }
        catch (Exception e) {
            log.debug("Failed to request shutdown: $e", e)
            
            io.err.println("Unable to shutdown the server: ${e.message}")
        }
    }
}
