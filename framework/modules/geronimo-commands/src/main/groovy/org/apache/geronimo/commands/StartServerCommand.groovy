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
class StartServerCommand
    extends CommandSupport
{
    private AntBuilder ant

    @Requirement
    ShellInfo shellInfo
    
    @Option(name='-H', aliases=['--home'], metaVar='DIR', description='Use a specific Geronimo home directory')
    File geronimoHome
    
    @Option(name='-j', aliases=['--jvm'], metaVar='DIR', description='Use a specific Java Virtual Machine for server process')
    File javaVirtualMachine
    
    @Option(name='-A', aliases=['--javaagent'], metaVar='JAR', description='Use a specific Java Agent, set to \'none\' to disable')
    String javaAgent
    
    @Option(name='-l', aliases=['--logfile'], description='Capture console output to file')
    File logFile
    
    @Option(name='-b', aliases=['--background'], description='Run the server process in the background')
    boolean background = false
    
    @Option(name='-q', aliases=['--quiet'], description='Suppress informative and warning messages')
    boolean quiet = false
    
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
    
    @Option(name='-t', aliases=['--timeout'], description='Specify the timeout for the server process in seconds')
    int timeout = -1
    
    Map properties = [:]
    
    private void addPropertyFrom(final String nameValue, final String prefix) {
        assert nameValue

        String name, value
        int i = nameValue.indexOf('=')

        if (i == -1) {
            name = nameValue
            value = Boolean.TRUE.toString()
        }
        else {
            name = nameValue.substring(0, i)
            value = nameValue.substring(i + 1, nameValue.length())
        }
        name = name.trim()
        
        if (prefix) {
            name = "${prefix}.$name"
        }
        
        properties[name] = value
    }
    
    @Option(name='-D', aliases=['--property'], metaVar='NAME=VALUE', description='Define system properties')
    private void setPropertyFrom(final String nameValue) {
        addPropertyFrom(nameValue, null)
    }
    
    @Option(name='-G', aliases=['--gproperty'], metaVar='NAME=VALUE', description='Define an org.apache.geronimo property')
    private void setGeronimoPropertyFrom(final String nameValue) {
        addPropertyFrom(nameValue, 'org.apache.geronimo')
    }
    
    @Option(name='-J', aliases=['--javaopt'], metaVar='FLAG', description='Set a JVM flag')
    List<String> javaFlags = []
    
    @Option(name='-m', aliases=['--module'], metaVar='NAME', description='Start up a specific module by name')
    List<String> startModules = []
    
    //
    // TODO: Expose as options, maybe expose a single URI-ish thingy?
    //
    
    String hostname = 'localhost'
    
    int port = 1099
    
    String username = 'system'
    
    String password = 'manager'
    
    private File getJavaAgentJar() {
        def file = new File(geronimoHome, 'bin/jpa.jar')
        
        if (javaAgent) {
            if (javaAgent.toLowerCase() == 'none') {
                file = null
            }
            else {
                file = new File(javaAgent)
                
                if (!file.exists()) {
                    log.warn("Disabling Java Agent support; missing jar: $file")
                    file = null
                }
            }
        }
        
        return file
    }
    
    protected Object doExecute() throws Exception {
        ant = new AntBuilder(log, io)
        
        if (!geronimoHome) {
            geronimoHome = shellInfo.homeDir
        }
        
        log.debug("Geronimo home: $geronimoHome")
        
        // Setup default java flags
        if (javaAgentJar) {
            javaFlags << "-javaagent:${javaAgentJar.canonicalPath}"
        }
        
        // Setup the default properties required to boot the server
        properties['org.apache.geronimo.base.dir'] = geronimoHome
        properties['java.io.tmpdir'] = 'var/temp' // Use relative path
        properties['java.endorsed.dirs'] = prefixSystemPath('java.endorsed.dirs', new File(geronimoHome, 'lib/endorsed'))
        properties['java.ext.dirs'] = prefixSystemPath('java.ext.dirs', new File(geronimoHome, 'lib/ext'))
        
        processScripts()
        
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
                        node.setAttribute('jvm', javaVirtualMachine)
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
        
        def server = new ServerProxy(hostname, port, username, password)
        
        launcher.verifier = {
            return server.fullyStarted
        }
        
        launcher.launch()
        
        return SUCCESS
    }
    
    /**
     * Process custom rc.d scripts.
     */
    private void processScripts() {
        //
        // FIXME: Make the base directory configurable
        //
        
        def basedir = new File(geronimoHome, 'etc/rc.d')
        if (!basedir.exists()) {
            log.debug("Skipping script processing; missing base directory: $basedir")
            return
        }
        
        def name = id
        
        def scanner = ant.fileScanner {
            fileset(dir: basedir) {
                include(name: "${name},*.groovy")
            }
        }
        
        def binding = new Binding([command: this, log: log, io: io])
        def shell = new GroovyShell(binding)
        
        for (file in scanner) {
            log.debug("Evaluating script: $file")
            
            // Use InputStream method to avoid classname problems from the file's name
            shell.evaluate(file.newInputStream())
        }
    }
    
    private String prefixSystemPath(final String name, final File file) {
        assert name
        assert file

        def path = file.path
        def prop = System.getProperty(name, '')
        if (prop) {
            path += File.pathSeparator + prop
        }
        
        return path
    }
}
