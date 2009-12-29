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

package org.apache.geronimo.shell.geronimo;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.gogo.commands.Option;
import org.apache.geronimo.shell.BaseCommandSupport;
/**
 * @version $Rev$ $Date$
 */
public class BaseJavaCommand extends BaseCommandSupport {

    AntBuilder ant;

    // @Option(required=true, name = "shellInfo")
    // ShellInfo shellInfo;

    @Option(name = "-H", aliases = { "--home" }, description = "Use a specific Geronimo home directory")
    String geronimoHome;

    @Option(name = "-j", aliases = { "--jvm" }, description = "Use a specific Java Virtual Machine for server process")
    String javaVirtualMachine;

    @Option(name = "-A", aliases = { "--javaagent" }, description = "Use a specific Java Agent, set to \"none\" to disable")
    String javaAgent;

    @Option(name = "-l", aliases = { "--logfile" }, description = "Capture console output to file")
    String logFile;

    @Option(name = "-b", aliases = { "--background" }, description = "Run the server process in the background")
    boolean background = false;

    @Option(name = "-t", aliases = { "--timeout" }, description = "Specify the timeout for the server process in seconds")
    int timeout = -1;

    @Option(name = "-P", aliases = { "--profile" }, multiValued = true, description = "Select a configuration profile")
    List<String> profiles;

    Map<String, String> properties = new HashMap<String, String>();;

    protected void addPropertyFrom(final String nameValue, final String prefix) {
        assert nameValue != null;

        String name, value;
        int i = nameValue.indexOf("=");

        if (i == -1) {
            name = nameValue;
            value = Boolean.TRUE.toString();
        } else {
            name = nameValue.substring(0, i);
            value = nameValue.substring(i + 1, nameValue.length());
        }
        name = name.trim();

        if (prefix != null) {
            name = prefix + name;
        }

        properties.put(name, value);
    }

    @Option(name = "-D", aliases = { "--property" }, multiValued = true, description = "Define system properties")
    List<String> propertyFrom;

    @Option(name = "-G", aliases = { "--gproperty" }, multiValued = true, description = "Define an org.apache.geronimo property")
    List<String> gPropertyFrom;

    @Option(name = "-J", aliases = { "--javaopt" }, description = "Set a JVM flag")
    List<String> javaFlags;

    protected File getJavaAgentJar() {
        File file = new File(geronimoHome, "bin/jpa.jar");

        if (javaAgent != null) {
            if (javaAgent.toLowerCase() == "none") {
                file = null;
            } else {
                file = new File(javaAgent);

                if (!file.exists()) {
                    log.warn("Disabling Java Agent support; missing jar: "+ file);
                    file = null;
                }
            }
        }

        return file;
    }
/*
 * No need of rc.d
 * definely need java6 and totally change the way server started
 */
    /**
     * Process custom rc.d scripts.
     
    protected void processScripts() {
        //
        // FIXME: Make the base directory configurable
        //
        
        File basedir = new File(geronimoHome, "etc/rc.d");
        if (!basedir.exists()) {
            log.debug("Skipping script processing; missing base directory: "+basedir);
            return;
        }
        
        // Use the target commands name (not the alias name)
        String name = context.info.name;
        
        def scanner = ant.fileScanner {
            fileset(dir: basedir) {
                include(name: "${name},*.groovy");
            }
        }
        
        Binding binding = new Binding([command: this, log: log, io: io]);
        GroovyShell shell = new GroovyShell(binding);
        
        for (file in scanner) {
            log.debug("Evaluating script: "+file);
            
            // Use InputStream method to avoid classname problems from the file's name
            shell.evaluate(file.newInputStream());
        }
    }
    */
    
    protected String prefixSystemPath(final String name, final File file) {
        assert name != null;
        assert file != null;

        String path = file.getPath();
        String prop = System.getProperty(name, "");
        if (prop != null) {
            path += File.pathSeparator + prop;
        }

        return path;
    }

    @Override
    protected Object doExecute() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}

interface ShellInfo {
    File getHomeDir();

    InetAddress getLocalHost();

    String getUserName();
}
