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
import org.apache.geronimo.deployment.cli.ServerConnection
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager

/**
 * Wait for a Geronimo server to start.
 *
 * @version $Rev: 664243 $ $Date: 2008-06-06 22:23:20 -0400 (Fri, 06 Jun 2008) $
 */
@CommandComponent(id='geronimo-commands:wait-for-server', description='Wait for a Geronimo server to start')
class WaitForServerCommand
    extends ConnectCommand
{
     
    @Option(name='-t', aliases=['--timeout'], description='Specify the time in seconds to wait while verifying that the server has started. Default 60 seconds')
    int timeout = 60
     
    protected Object doExecute() throws Exception {
        def timer = new Timer(true)
        
        def timedOut = false
        
        def timeoutTask
        if (timeout > 0) {
            timeoutTask = timer.runAfter(timeout * 1000, {
                timedOut = true
            })
        }
        
        def started = false
        
        io.out.println("Waiting for Geronimo server: ${hostname}:${port}")
        
        def connection
        def server
        while (!started) {
            if (timedOut) {
                throw new Exception("Unable to verify if the server was started in the given time")
            }
            
            try {
                connection = connect()
                server = new ServerProxy(connection.deploymentManager.getJMXConnector())
            } catch (Exception error) {
                log.debug("Server query failed; ignoring", error)
                Thread.sleep(1000)
                continue
            }
                        
            started = server.isFullyStarted()
            
            if (!started) {
                Throwable error = server.getLastError()
                if (error != null) {
                    log.debug("Server query failed; ignoring", error)
                }
                Thread.sleep(1000);
            }
        }
        
        timeoutTask?.cancel()
        timer.cancel()
                
        io.out.println("Geronimo server is started")
    }
    
    protected ServerConnection connect() {
        def connection = variables.get(SERVER_CONNECTION)
        
        if (!connection) {
            connection = openConnection(true)
        }
        
        return connection
    }
    
}
