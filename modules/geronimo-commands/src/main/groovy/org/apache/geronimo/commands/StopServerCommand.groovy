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

import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.gshell.command.CommandSupport

/**
 * Stops a running Geronimo server instance.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id='stop-server')
class StopServerCommand
    extends CommandSupport
{
    //
    // TODO: Expose as options, maybe expose a single URI-ish thingy?
    //

    String hostname = 'localhost'

    int port = 1099

    String username = 'system'

    String password = 'manager'
    
    protected Object doExecute() throws Exception {
        io.out.println("Stopping Geronimo server: ${hostname}:${port}")

        def server = new ServerProxy(hostname, port, username, password)

        server.shutdown();

        io.out.println("Shutdown request has been issued");
    }
}