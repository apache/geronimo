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

/**
 * Stops a running Geronimo server instance.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id='geronimo-commands:stop-server', description="Stop a Geronimo server")
class StopServerCommand
    extends CommandSupport
{
    @Option(name='-s', aliases=['--hostname', '--server'], description='Hostname, default localhost')
    String hostname = 'localhost'

    @Option(name='-p', aliases=['--port'], description='Port, default 1099')
    int port = 1099

    @Option(name='-u', aliases=['--username'], description='Username')
    String username = 'system'

    @Option(name='-w', aliases=['--password'], description='Password')
    String password = 'manager'
    
    protected Object doExecute() throws Exception {
        io.out.println("Stopping Geronimo server: ${hostname}:${port}")
        
        //
        // TODO: If no password given, then prompt for password
        //
        
        def server = new ServerProxy(hostname, port, username, password)

        server.shutdown();

        io.out.println("Shutdown request has been issued");
    }
}