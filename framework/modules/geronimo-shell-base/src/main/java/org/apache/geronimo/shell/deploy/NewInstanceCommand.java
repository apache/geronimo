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

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.system.plugin.NewServerInstance;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "new-server-instance", description = "Create new server instance in a server")
public class NewInstanceCommand extends ConnectCommand {

    @Argument(required = true, description = "Name of new server instance")
    String serverName;

    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        DeploymentManager deploymentManager = connection.getDeploymentManager();
        NewServerInstance newServerInstance = 
            (NewServerInstance) ((GeronimoDeploymentManager) deploymentManager).getImplementation(NewServerInstance.class);
        newServerInstance.newServerInstance(serverName);
        println("Server created");
        return null;
    }
}
