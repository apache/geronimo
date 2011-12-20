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

package org.apache.geronimo.shell.deploy;

import org.apache.karaf.shell.commands.Command;
import org.apache.geronimo.deployment.cli.ServerConnection;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "disconnect", description = "Disconnect from a Geronimo server")
public class DisconnectCommand extends ConnectCommand {
    @Override
    protected Object doExecute() throws Exception {

        ServerConnection connection = (ServerConnection) session.get(ConnectCommand.SERVER_CONNECTION);

        if (connection != null) {
            println("Disconnecting from Geronimo server");

            try {
                connection.close();
            } catch (Exception e) {
                // ignore
            }

            session.put(SERVER_CONNECTION, null);

            println("Connection ended");
        } else {
            println("Not connected");
        }
        return null;
    }

}
