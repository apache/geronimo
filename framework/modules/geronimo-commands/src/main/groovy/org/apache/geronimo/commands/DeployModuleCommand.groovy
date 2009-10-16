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
import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.deployment.cli.CommandDeploy

/**
 * Deploy module.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:deploy-module', description='Deploy a module')
class DeployModuleCommand
    extends ConnectCommand
{
    @Option(name='-i', aliases=['--inPlace'], description='In-place deployment')
    boolean inPlace
    
    @Option(name='-t', aliases=['--targets'], metaVar='TARGET;TARGET;...', description='Targets')
    String targets
         
    @Argument(metaVar='MODULE-FILE', required=true, index=0, description='Module file')
    String module
    
    @Argument(metaVar='MODULE-PLAN', index=1, description='Module plan')
    String modulePlan
    
    protected Object doExecute() throws Exception {
        def connection = connect()
        
        def command = new CommandDeploy()
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
        
        def commandArgs = []
        commandArgs << module
        
        if (modulePlan) {
            commandArgs << modulePlan
        }
        
        def args = new DistributeCommandArgsImpl(
            args: (String[])commandArgs,
            targets: (targets == null ? [] : targets.split(';')),
            inPlace: inPlace)
        
        command.execute(consoleReader, connection, args)
    }
}