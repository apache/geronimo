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

import jline.ConsoleReader
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager
import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.system.plugin.NewServerInstance

/**
* List plugins.
*
* @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
*/
@CommandComponent (id = 'geronimo-commands:new-server-instance', description = 'create new server instance in a server')
class NewServerInstanceCommand
   extends ConnectCommand
{
   @Option(name='-n', aliases=['--server-name'], description='Name of new server instance')
   String serverName

   protected Object doExecute() throws Exception {
       def connection = connect()

       def ConsoleReader consoleReader = new ConsoleReader(io.inputStream, io.out)
       def repo = null
       def plugins = null

       def deploymentManager = connection.getDeploymentManager();
       def NewServerInstance newServerInstance = (NewServerInstance)((RemoteDeploymentManager)deploymentManager).getImplementation(NewServerInstance.class);
       newServerInstance.newServerInstance(serverName);
       io.out.println("Server created")
   }
}
