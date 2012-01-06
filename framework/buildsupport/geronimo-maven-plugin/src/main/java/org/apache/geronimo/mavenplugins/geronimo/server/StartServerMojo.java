/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.geronimo.server;

import java.io.File;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.tools.ant.taskdefs.Java;

import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.codehaus.mojo.pluginsupport.util.ObjectHolder;

import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;

import org.codehaus.plexus.util.FileUtils;

import org.apache.commons.lang.time.StopWatch;

/**
 * Start the Geronimo server.
 *
 * @goal start-server
 *
 * @version $Rev$ $Date$
 */
public class StartServerMojo
    extends InstallerMojoSupport
{
    /**
     * Set the false to skip the installation of the assembly, re-using anything
     * that is already there.
     *
     * @parameter expression="${install}" default-value="true"
     */
    private boolean install = true;

    /**
     * Flag to control if we background the server or block Maven execution.
     *
     * @parameter expression="${background}" default-value="false"
     */
    private boolean background = false;

    /**
     * Set the maximum memory for the forked JVM.
     *
     * @parameter expression="${maximumMemory}"
     */
    private String maximumMemory = null;

    /**
     * Set the maximum permsize for the forked JVM.
     *
     * @parameter expression="${maxPermSize}"
     */
    private String maxPermSize = null;
    
    /**
     * The location of the Java Virtual Machine executable to launch the server with.
     *
     * @parameter
     */
    private File javaVirtualMachine;
    
    /**
     * Enable quiet mode.
     *
     * @parameter expression="${quiet}" default-value="false"
     */
    private boolean quiet = false;

    /**
     * Enable verbose mode.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose = false;

    /**
     * Enable veryverbose mode.
     *
     * @parameter expression="${veryverbose}" default-value="false"
     */
    private boolean veryverbose = false;

    /**
     * Time in seconds to wait before terminating the forked JVM.
     *
     * @parameter expression="${timeout}" default-value="-1"
     */
    private int timeout = -1;

    /**
     * Time in seconds to wait while verifing that the server has started.
     *
     * @parameter expression="${verifyTimeout}" default-value="-1"
     */
    private int verifyTimeout = -1;

    /**
     * Enable propagation of <tt>org.apache.geronimo.*</tt> and <tt>geronimo.*</tt>
     * properties from Maven to the forked server process.
     *
     * @parameter expression="${propagateGeronimoProperties}" default-value="true"
     */
    private boolean propagateGeronimoProperties;

    /**
     * An array of option sets which can be enabled by setting optionSetId.
     *
     * @parameter
     */
    private OptionSet[] optionSets = null;

    /**
     * A comma seperated list of optionSets to enabled.
     *
     * @parameter expression="${options}"
     */
    private String options = null;

    /**
     * A list of module names to be started using --override.
     *
     * @parameter
     */
    private String[] startModules = null;

    private Timer timer = new Timer(true);

    protected void doExecute() throws Exception {
        if (install) {
            installAssembly();
        }
        else {
            log.info("Skipping assembly installation");

            if (!geronimoHome.exists()) {
                throw new MojoExecutionException("Missing pre-installed assembly directory: " + geronimoHome);
            }
        }

        log.info("Starting Geronimo server...");

        // Setup the JVM to start the server with
        final Java java = (Java)createTask("java");
        java.setClassname("org.apache.karaf.main.Main");
        Path path = java.createClasspath();
        File libDir = new File(geronimoHome, "lib");
        FileSet fileSet = new FileSet();
        fileSet.setDir(libDir);
        path.addFileset(fileSet);
        java.setDir(geronimoHome);
        java.setFailonerror(true);
        java.setFork(true);

        if (javaVirtualMachine != null) {
            if (!javaVirtualMachine.exists()) {
                throw new MojoExecutionException("Java virtual machine is not valid: " + javaVirtualMachine);
            }
            
            log.info("Using Java virtual machine: " + javaVirtualMachine);
            java.setJvm(javaVirtualMachine.getCanonicalPath());
        }
        
        if (timeout > 0) {
            java.setTimeout(new Long(timeout * 1000));
        }

        if (maximumMemory != null) {
            java.setMaxmemory(maximumMemory);
        }
        
        if (maxPermSize != null){    
            java.createJvmarg().setValue("-XX:MaxPermSize="+maxPermSize);         
        } 
        
        // Load the Java programming language agent for JPA
        File javaAgentJar = new File(geronimoHome, "lib/agent/transformer.jar");
        if (javaAgentJar.exists()) {
            java.createJvmarg().setValue("-javaagent:" + javaAgentJar.getCanonicalPath());
        }

        // Propagate some properties from Maven to the server if enabled
        if (propagateGeronimoProperties) {
            Properties props = System.getProperties();
            Iterator iter = props.keySet().iterator();
            while (iter.hasNext()) {
                String name = (String)iter.next();
                String value = System.getProperty(name);

                if (name.equals("geronimo.bootstrap.logging.enabled")) {
                    // Skip this property, never propagate it
                }
                else if (name.startsWith("org.apache.geronimo") || name.startsWith("geronimo")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Propagating: " + name + "=" + value);
                    }
                    setSystemProperty(java, name, value);
                }
            }
        }

        // Apply option sets
        if (options != null  && (optionSets == null || optionSets.length == 0)) {
            throw new MojoExecutionException("At least one optionSet must be defined to select one using options");
        }
        else if (options == null) {
            options = "default";
        }

        if (optionSets != null && optionSets.length != 0) {
            OptionSet[] sets = selectOptionSets();

            for (int i=0; i < sets.length; i++) {
                if (log.isDebugEnabled()) {
                    log.debug("Selected option set: " + sets[i]);
                }
                else {
                    log.info("Selected option set: " + sets[i].getId());
                }

                String[] options = sets[i].getOptions();
                if (options != null) {
                    for (int j=0; j < options.length; j++) {
                        java.createJvmarg().setValue(options[j]);
                    }
                }

                Properties props = sets[i].getProperties();
                if (props != null) {
                    Iterator iter = props.keySet().iterator();
                    while (iter.hasNext()) {
                        String name = (String)iter.next();
                        String value = props.getProperty(name);

                        setSystemProperty(java, name, value);
                    }
                }
            }
        }

        // Set the properties which we pass to the JVM from the startup script
        setSystemProperty(java, "org.apache.geronimo.home.dir", geronimoHome);
        setSystemProperty(java, "karaf.home", geronimoHome);
        setSystemProperty(java, "karaf.base", geronimoHome);
        // Use relative path
        setSystemProperty(java, "java.io.tmpdir", "var/temp");
        setSystemProperty(java, "java.endorsed.dirs", prefixSystemPath("java.endorsed.dirs", new File(geronimoHome, "lib/endorsed")));
        setSystemProperty(java, "java.ext.dirs", prefixSystemPath("java.ext.dirs", new File(geronimoHome, "lib/ext")));
        // set console properties
        setSystemProperty(java, "karaf.startLocalConsole", "false");
        setSystemProperty(java, "karaf.startRemoteShell", "true");

        if (quiet) {
            java.createArg().setValue("--quiet");
        }
        else {
            java.createArg().setValue("--long");
        }

        if (verbose) {
            java.createArg().setValue("--verbose");
        }

        if (veryverbose) {
            java.createArg().setValue("--veryverbose");
        }

        if (startModules != null) {
            if (startModules.length == 0) {
                throw new MojoExecutionException("At least one module name must be configured with startModule");
            }

            log.info("Overriding the set of modules to be started");

            java.createArg().setValue("--override");

            for (int i=0; i < startModules.length; i++) {
                java.createArg().setValue(startModules[i]);
            }
        }

        //
        // TODO: Check if this really does capture STDERR or not!
        //

        if (logOutput) {
            File file = getLogFile();
            FileUtils.forceMkdir(file.getParentFile());

            log.info("Redirecting output to: " + file);

            java.setOutput(file);
        }

        // Holds any exception that was thrown during startup
        final ObjectHolder errorHolder = new ObjectHolder();

        StopWatch watch = new StopWatch();
        watch.start();

        // Start the server int a seperate thread
        Thread t = new Thread("Geronimo Server Runner") {
            public void run() {
                try {
                    java.execute();
                }
                catch (Exception e) {
                    errorHolder.set(e);

                    //
                    // NOTE: Don't log here, as when the JVM exists an exception will get thrown by Ant
                    //       but that should be fine.
                    //
                }
            }
        };
        t.start();

        log.info("Waiting for Geronimo server...");

        // Setup a callback to time out verification
        final ObjectHolder verifyTimedOut = new ObjectHolder();

        TimerTask timeoutTask = new TimerTask() {
            public void run() {
                verifyTimedOut.set(Boolean.TRUE);
            }
        };

        if (verifyTimeout > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Starting verify timeout task; triggers in: " + verifyTimeout + " seconds");
            }
            timer.schedule(timeoutTask, verifyTimeout * 1000);
        }

        // Verify server started
        ServerProxy server = new ServerProxy(hostname, port, username, password);
        boolean started = false;
        while (!started) {
            if (verifyTimedOut.isSet()) {
                throw new MojoExecutionException("Unable to verify if the server was started in the given time (" + verifyTimeout + " seconds)");
            }

            if (errorHolder.isSet()) {
                throw new MojoExecutionException("Failed to start Geronimo server", (Throwable)errorHolder.get());
            }

            started = server.isFullyStarted();

            if (!started) {
                Throwable error = server.getLastError();
                if ((error != null) && (log.isDebugEnabled())) {
                    log.debug("Server query failed; ignoring", error);
                }

                Thread.sleep(5 * 1000);
            }
        }
        server.closeConnection();

        // Stop the timer, server should be up now
        timeoutTask.cancel();

        log.info("Geronimo server started in " + watch);

        if (!background) {
            log.info("Waiting for Geronimo server to shutdown...");

            t.join();
        }
    }

    private String prefixSystemPath(final String name, final File file) {
        assert name != null;
        assert file != null;

        String dirs = file.getPath();
        String prop = System.getProperty(name, "");
        if (prop.length() > 0) {
            dirs += File.pathSeparator;
            dirs += prop;
        }
        return dirs;
    }

    private OptionSet[] selectOptionSets() throws MojoExecutionException {
        // Make a map of the option sets and validate ids
        Map map = new HashMap();
        for (int i=0; i<optionSets.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("Checking option set: " + optionSets[i]);
            }

            String id = optionSets[i].getId();

            if (id == null && optionSets.length > 1) {
                throw new MojoExecutionException("Must specify id for optionSet when more than one optionSet is configured");
            }
            else if (id == null && optionSets.length == 1) {
                id = "default";
                optionSets[i].setId(id);
            }

            assert id != null;
            id = id.trim();

            if (map.containsKey(id)) {
                throw new MojoExecutionException("Must specify unique id for optionSet: " + id);
            }
            map.put(id, optionSets[i]);
        }

        StringTokenizer stok = new StringTokenizer(options, ",");

        List selected = new ArrayList();
        while (stok.hasMoreTokens()) {
            String id = stok.nextToken();
            OptionSet set = (OptionSet)map.get(id);

            if (set == null) {
                if ("default".equals(id)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Default optionSet selected, but no optionSet defined with that id; ignoring");
                    }
                }
                else {
                    log.warn("Missing optionSet for id: " + id);
                }
            }
            else {
                selected.add(set);
            }
        }

        return (OptionSet[]) selected.toArray(new OptionSet[selected.size()]);
    }

    protected String getFullClassName() {
        return this.getClass().getName();
    }
}
