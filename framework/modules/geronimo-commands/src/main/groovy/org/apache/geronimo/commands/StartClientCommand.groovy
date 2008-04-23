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
import org.apache.geronimo.gshell.clp.Argument
import org.apache.geronimo.gshell.clp.Option

import org.apache.tools.ant.ExitStatusException

// Make sure we use our custom builder
import org.apache.geronimo.commands.AntBuilder
import org.apache.geronimo.gshell.shell.ShellInfo

/**
 * Starts Geronimo application client.
 *
 * @version $Rev: 601585 $ $Date: 2007-12-05 19:14:26 -0500 (Wed, 05 Dec 2007) $
 */
@CommandComponent(id='geronimo-commands:start-client', description="Start a Geronimo application client")
class StartClientCommand
    extends BaseJavaCommand
{
    int verbose = 0
    
    @Option(name='-v', aliases=['--verbose'], description='Enable verbose output; specify multipule times to increase verbosity')
    private void increaseVerbosity(boolean flag) {
        if (flag) {
            verbose++
        }
        else {
            verbose--
        }
    }
       
    @Argument(metaVar="CONFIG-NAME", required=true, index=0, description="Configuration name of application client")
    String moduleName;
    
    @Argument(metaVar="ARGS", index=1, description="Application client arguments")
    List<String> moduleArguments;
           
    protected Object doExecute() throws Exception {
        ant = new AntBuilder(log, io)
        
        if (!geronimoHome) {
            geronimoHome = shellInfo.homeDir
        }
        
        log.debug("Geronimo home: $geronimoHome")
        
        // Setup the default properties required to boot the server
        properties['org.apache.geronimo.base.dir'] = geronimoHome
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
        
        def launcher = new ProcessLauncher(log: log, io: io, name: 'Geronimo Client', background: background)
        
        //
        // TODO: Add spawn support?
        //
        
        launcher.process = {
            try {
                ant.java(jar: "$geronimoHome/bin/client.jar", dir: geronimoHome, failonerror: true, fork: true) {
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
                        node.setAttribute('jvm', javaVirtualMachine)
                    }
                    
                    javaFlags.each {
                        jvmarg(value: it)
                    }
                    
                    properties.each { key, value ->
                        sysproperty(key: key, value: value)
                    }
                    
                    if (verbose == 1) {
                        arg(value: '--verbose')
                    } else if (verbose > 1) {
                        arg(value: '--veryverbose')
                    }
                                        
                    arg(value: moduleName)
                    
                    if (moduleArguments) {                                                
                        moduleArguments.each {
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
                
        launcher.launch()
        
        return SUCCESS
    }
       
}
