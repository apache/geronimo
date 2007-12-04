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

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.gshell.command.annotation.Requirement
import org.apache.geronimo.gshell.command.CommandExecutor
import org.apache.geronimo.gshell.command.CommandSupport
import org.apache.geronimo.gshell.command.IO

/**
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent(id='geronimo-commands:execute-alias', description="Execute an alias")
class ExecuteAliasCommand extends AbstractAliasCommand {

    @Requirement
    CommandExecutor executor

    protected Object doExecute() throws Exception {
        def aliasFile = checkAliasFile()
        
        def result
        aliasFile.withInputStream({
            def aliases = new XmlSlurper().parse(it)
            
            def alias = aliases.alias.find { it.@id.text().equals(aliasName) }
            if (alias.isEmpty()) {
                def availables = buildAvailableAliases(aliases)
                throw new IllegalArgumentException("${aliasName} does not exist.${availables}")
            }
            
            def cli = alias.cli.text()
            if ('' == cli) {
                throw new UnsupportedOperationException("alias.cli is not defined by alias ${aliasName}")
            }
            
            result = executor.execute(cli)
        })
        
        result
    }

}
