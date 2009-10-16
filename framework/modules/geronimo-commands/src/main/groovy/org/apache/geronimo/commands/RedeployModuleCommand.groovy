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

import org.apache.geronimo.gshell.clp.Argument
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.cli.deployer.BaseCommandArgs
import org.apache.geronimo.deployment.cli.CommandRedeploy

/**
 * Redeploy module.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:redeploy-module', description='Redeploy a module')
class RedeployModuleCommand
    extends ConnectCommand
{     
    @Argument(metaVar='MODULE', index=0, description='Module file')
    String moduleName
    
    @Argument(metaVar='PLAN', index=1, description='Module plan')
    String modulePlan
    
    @Argument(metaVar='MODULE_ID', index=2, description='Module id')
    List<String> moduleIds

    protected Object doExecute() throws Exception {
        def connection = connect()
        
        def command = new CommandRedeploy()
        
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
        
        def commandArgs = []
        
        if (moduleName) {
            commandArgs << moduleName
        }
        
        if (modulePlan) {
            commandArgs << modulePlan
        }
        
        if (moduleIds) {
            commandArgs.addAll(moduleIds)
        }
        
        def args = new BaseCommandArgs((String[])commandArgs)
        
        command.execute(consoleReader, connection, args)
    }
}