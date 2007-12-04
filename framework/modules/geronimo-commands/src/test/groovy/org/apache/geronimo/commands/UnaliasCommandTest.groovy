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

import org.apache.geronimo.gshell.command.IO

import org.apache.geronimo.testsupport.TestSupport

/**
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
class UnaliasCommandTest extends GroovyTestCase {

    def command
    
    protected void setUp() {
		def testSupport = new GroovyTestSupport()
        
        File baseFile = testSupport.resolveFile('src/test/resources/etc/aliases.xml')
        def lines = baseFile.readLines()
       
        def workingFile = testSupport.resolveFile('build/etc/aliases.xml')
        workingFile.delete()
        workingFile.parentFile.mkdirs()
        workingFile.createNewFile()
        workingFile.withPrintWriter { pw ->
            lines.each { pw.println(it) }
        }
        
        command = new UnaliasCommand([io: new IO(), aliasFileName: workingFile.absolutePath])
    }
    
	void testAliasFileDoesNotExistThrowsISE() {
	    shouldFail(IllegalStateException.class) {
	        command.aliasFileName = 'doesNotExist'
	        command.doExecute()
	    }
	}

	void testNoAliasArgumentThrowsIAE() {
	    shouldFail(IllegalArgumentException.class) {
	        command.doExecute()
	    }
	}

	void testUnalias() {
        command.aliasName = 'start_DEFAULT_SERVER'
        assertTrue(command.doExecute())
        
//        assertFalse(command.doExecute())
	}
	
}
