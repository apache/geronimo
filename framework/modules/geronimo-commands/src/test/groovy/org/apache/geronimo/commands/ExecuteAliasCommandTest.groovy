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

package org.apache.geronimo.commands;

import org.apache.geronimo.gshell.command.CommandExecutor
import org.apache.geronimo.gshell.command.IO

import org.apache.geronimo.testsupport.TestSupport

/**
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
class ExecuteAliasCommandTest extends GroovyTestCase {

    def command
    def executedCommand
    
    protected void setUp() {
		def testSupport = new GroovyTestSupport()
        
        File aliasFile = testSupport.resolveFile('src/test/resources/etc/aliases.xml')

        command = new ExecuteAliasCommand([io: new IO(), aliasFileName: aliasFile.absolutePath])
        command.executor = { executedCommand = it } as CommandExecutor
    }
    
	void testAliasFileDoesNotExistThrowsISE() {
	    shouldFail(IllegalStateException.class) {
	        command.aliasFileName = 'doesNotExist'
	        command.doExecute()
	    }
	}

	void testExecuteUndefinedAliasThrowsIAE() {
	    shouldFail(IllegalArgumentException.class) {
	        command.aliasName = 'undefined'
	        command.doExecute()
	    }
	}

	void testExecuteAliasWithoutCLIThrowsUOE() {
	    shouldFail(UnsupportedOperationException.class) {
	        command.aliasName = 'alias_without_cli'
	        command.doExecute()
	    }
	}
	
	void testExecuteAlias() {
        command.aliasName = 'start_DEFAULT_SERVER'
        command.doExecute()
        
        assert 'start-server -d' == executedCommand
	}
	
}
