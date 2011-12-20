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

package org.apache.geronimo.shell.geronimo;

import org.apache.karaf.shell.commands.Command;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.shell.deploy.ConnectCommand;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "geronimo", name = "stop-server",  description = "Stop Server")
public class StopServerCommand extends ConnectCommand {

    @Override
    protected Object doExecute() throws Exception {
        println("Stopping Geronimo server...");
        Kernel kernel = getKernel();
        try {
            if (isEmbedded(kernel)) {
                bundleContext.getBundle(0).stop();
            } else {
                ServerConnection connection = connect();
                ServerProxy server = 
                    new ServerProxy(((RemoteDeploymentManager)connection.getDeploymentManager()).getJMXConnector());
                server.shutdown();
            }
            println("Shutdown request has been issued");
        } catch (Exception e) {
            println("Unable to shutdown the server: " + e.getMessage());
        } finally {
            disconnect();
        }
        
        return null;
    }

}
