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

import org.apache.geronimo.gshell.command.CommandSupport
import org.apache.geronimo.gshell.command.CommandException
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.gshell.command.annotation.Requirement

import org.apache.geronimo.gshell.clp.Option

import org.apache.tools.ant.ExitStatusException

// Make sure we use our custom builder
import org.apache.geronimo.commands.AntBuilder
import org.apache.geronimo.gshell.shell.ShellInfo

/**
 * Starts a new Geronimo server instance.
 *
 * @version $Rev$ $Date$
 */
@CommandComponent(id='geronimo-commands:start-server', description="Start a Geronimo server")
class StartServerCommand extends BaseJavaCommand {
        
    @Option(name='-q', aliases=['--quiet'], description='Suppress informative and warning messages')
    boolean quiet = false
    
    int verbose = 0
    
    @Option(name='-v', aliases=['--verbose'], description='Enable verbose output; specify multiple times to increase verbosity')
    private void increaseVerbosity(boolean flag) {
        if (flag) {
            verbose++
        }
        else {
            verbose--
        }
    }
    
    @Option(name='-m', aliases=['--module'], metaVar='NAME', description='Start up a specific module by name')
    List<String> startModules = []
    
    //
    // TODO: Expose as options, maybe expose a single URI-ish thingy?
    //
    
    String hostname = 'localhost'
    
    @Option(name='-p', aliases=['--port'], description='RMI Naming port (used to check server startup status)')
    int port = 1099
    
    @Option(name='-u', aliases=['--username'], description='Username (used to check server startup status)')
    String username = 'system'
    
    @Option(name='-w', aliases=['--password'], description='Password (used to check server startup status)')
    String password = 'manager'
    
    @Option(name='--secure', description='Use secure channel')
    boolean secure = false
        
    protected Object doExecute() throws Exception {
        ant = new AntBuilder(log, io)
        
        if (!geronimoHome) {
            geronimoHome = shellInfo.homeDir
        }
        
        log.debug("Geronimo home: $geronimoHome")
        
        // Setup the default properties required to boot the server
        properties['org.apache.geronimo.home.dir'] = geronimoHome
        properties['java.io.tmpdir'] = 'var/temp' // Use relative path
        properties['java.endorsed.dirs'] = prefixSystemPath('java.endorsed.dirs', new File(geronimoHome, 'lib/endorsed'))
        properties['java.ext.dirs'] = prefixSystemPath('java.ext.dirs', new File(geronimoHome, 'lib/ext'))
        
        processScripts()
        
        // Setup default java flags
        if (javaAgentJar) {
            javaFlags << "-javaagent:${javaAgentJar.canonicalPath}"
        }

        // If we are not backgrounding, then add a nice message for the user when ctrl-c gets hit
        if (!background) {
            addShutdownHook({
                io.out.println('Shutting down...')
                io.flush()
            })
        }
        
        def launcher = new ProcessLauncher(log: log, io: io, name: 'Geronimo Server', background: background)
        
        //
        // TODO: Add spawn support?
        //
        
        launcher.process = {
            try {
                ant.java(jar: "$geronimoHome/bin/server.jar", dir: geronimoHome, failonerror: true, fork: true) {
                    def node = current.wrapper
                    
                    if (timeout > 0) {
                        log.info("Timeout after: ${timeout} seconds")
                        node.setAttribute('timeout', "${timeout * 1000}")
                    }
                    
                    if (logFile) {
                        log.info("Redirecting output to: $logFile")
                        logFile.parentFile.mkdirs()
                        redirector(output: logFile)
                    }
                    
                    if (javaVirtualMachine) {
                        if (!javaVirtualMachine.exists()) {
                            fail("Java virtual machine is not valid: $javaVirtualMachine")
                        }
                        
                        log.info("Using Java virtual machine: $javaVirtualMachine")
                        node.setAttribute('jvm', javaVirtualMachine.absolutePath)
                    }
                    
                    javaFlags.each {
                        jvmarg(value: it)
                    }
                    
                    properties.each { key, value ->
                        sysproperty(key: key, value: value)
                    }
                    
                    if (quiet) {
                        arg(value: '--quiet')
                    }
                    else {
                        arg(value: '--long')
                    }
                    
                    if (verbose == 1) {
                        arg(value: '--verbose')
                    }
                    else if (verbose > 1) {
                        arg(value: '--veryverbose')
                    }
                    
                    if (startModules) {
                        log.info('Overriding the set of modules to be started')
                        
                        arg(value: '--override')
                        
                        startModules.each {
                            arg(value: it)
                        }
                    }
                }
            }
            catch (ExitStatusException e) {
                def tmp = log.&info
                
                if (e.status != 0) {
                    tmp = log.&warn
                }
                
                tmp("Process exited with status: $e.status")
                
                throw e
            }
        }
        
        def server = new ServerProxy(hostname, port, username, password, secure)
        
        launcher.verifier = {
             if(server.fullyStarted) {
                 server.closeConnection();
                 return true;
             } else {
                 return false
             }
        }
        
        launcher.launch()
        
        return SUCCESS
    }
    
}
