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

import groovy.xml.StreamingMarkupBuilder

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
abstract class AbstractAliasCommand extends CommandSupport {

    @Option(name='-c', aliases=['--alias-configuration'], description='Alias configuration file')
    String aliasFileName = 'etc/aliases.xml'

    @Argument(index=0, description="alias")
    String aliasName

    def buildAvailableAliases = { aliases ->
        def availables = '\nAvailable aliases:\n'
        aliases.alias.list().sort{ it.@id.text() }.each {
            availables += "    ${it.@id.text()} '${it.'*'.text()}'\n"
        }
        availables
    }
    
    def checkAliasFile = {
        def aliasFile = new File(aliasFileName)
        if (!aliasFile.exists()) {
            throw new IllegalStateException("Alias file ${aliasFile.absolutePath} does not exist")
        }
        aliasFile
    }
    
    def serializeToXML = { aliases ->
            def outputBuilder = new StreamingMarkupBuilder()
            outputBuilder.bind { mkp.yield aliases }
    }
    
    def createNewAliasFile = { aliasFile, aliasFileName, xml ->
        aliasFile.renameTo(new File(aliasFileName + ".bak"))

        aliasFile = new File(aliasFileName)
        aliasFile.createNewFile()
        aliasFile.withPrintWriter {
            it.write(xml)
        }
    }
    
}
