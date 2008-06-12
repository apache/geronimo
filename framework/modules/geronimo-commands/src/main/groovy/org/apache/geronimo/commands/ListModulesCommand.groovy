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
import org.apache.geronimo.deployment.cli.CommandListModules

/**
 * List modules.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:list-modules', description="List modules")
class ListModulesCommand extends ConnectCommand {
     
    @Option(name='-a', aliases=['--all'], description='Show started or stopped modules')   
    boolean all = true
    
    @Option(name='-t', aliases=['--stopped'], description='Show stopped modules only')
    boolean stopped = false
    
    @Option(name='-r', aliases=['--started'], description='Show started modules only')
    boolean started = false
     
    @Argument(metaVar="TARGET", description="Target name")
    List<String> targets = []
    
    protected Object doExecute() throws Exception {
        def connection = connect()
        
        def command = new CommandListModules()
        def consoleReader = new ConsoleReader(io.inputStream, io.out)
        def args = new ListModulesCommandArgsImpl((String[])targets, all, started, stopped)
        
        command.execute(consoleReader, connection, args)
    }
           
}