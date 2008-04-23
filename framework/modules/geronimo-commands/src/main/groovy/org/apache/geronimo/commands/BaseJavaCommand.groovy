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
 * Support for commands which spin off a Java process.
 *
 * @version $Rev: 650911 $ $Date: 2008-04-23 22:37:46 +0700 (Wed, 23 Apr 2008) $
 */
abstract class BaseJavaCommand extends CommandSupport {
    AntBuilder ant

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
          
    @Option(name='-t', aliases=['--timeout'], description='Specify the timeout for the server process in seconds')
    int timeout = -1
    
    List<String> profiles = []

    @Option(name='-P', aliases=['--profile'], metaVar='NAME', description='Select a configuration profile')
    private void appendProfile(String name) {
        assert name
        
        profiles << name
    }

    Map properties = [:]
    
    protected void addPropertyFrom(final String nameValue, final String prefix) {
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
    protected void setPropertyFrom(final String nameValue) {
        addPropertyFrom(nameValue, null)
    }
    
    @Option(name='-G', aliases=['--gproperty'], metaVar='NAME=VALUE', description='Define an org.apache.geronimo property')
    protected void setGeronimoPropertyFrom(final String nameValue) {
        addPropertyFrom(nameValue, 'org.apache.geronimo')
    }
    
    @Option(name='-J', aliases=['--javaopt'], metaVar='FLAG', description='Set a JVM flag')
    List<String> javaFlags = []
      
    protected File getJavaAgentJar() {
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

    /**
     * Process custom rc.d scripts.
     */
    protected void processScripts() {
        //
        // FIXME: Make the base directory configurable
        //
        
        def basedir = new File(geronimoHome, 'etc/rc.d')
        if (!basedir.exists()) {
            log.debug("Skipping script processing; missing base directory: $basedir")
            return
        }
        
        // Use the target commands name (not the alias name)
        def name = context.info.name
        
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
    
    protected String prefixSystemPath(final String name, final File file) {
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
