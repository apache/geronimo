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
import org.apache.geronimo.deployment.cli.ServerConnection

/**
 * Stops a running Geronimo server instance.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:disconnect', description="Disconnect from a Geronimo server")
class DisconnectCommand
    extends CommandSupport
{

    protected Object doExecute() throws Exception {
        def connection = variables.get("ServerConnection")
        if (connection) {
            io.out.println("Disconnecting from Geronimo server")
            try {
            	connection.close()
            } catch (Exception e) {
                // ignore
            }           
            variables.parent.unset("ServerConnection")            
            io.out.println("Connection ended")
        } else {
            io.out.println("Not connected")
        }
    }
}