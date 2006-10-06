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
import org.apache.tools.ant.types.Environment;

import org.apache.geronimo.genesis.ObjectHolder;
import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;

import org.codehaus.plexus.util.FileUtils;

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
        installAssembly();

        log.info("Starting Geronimo server...");
        
        // Setup the JVM to start the server with
        final Java java = (Java)createTask("java");
        java.setJar(new File(geronimoHome, "bin/server.jar"));
        java.setDir(geronimoHome);
        java.setFailonerror(true);
        java.setFork(true);
        
        if (timeout > 0) {
            java.setTimeout(new Long(timeout * 1000));
        }

        if (maximumMemory != null) {
            java.setMaxmemory(maximumMemory);
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
        setSystemProperty(java, "org.apache.geronimo.base.dir", geronimoHome);
        setSystemProperty(java, "java.io.tmpdir", new File(geronimoHome, "var/temp"));
        setSystemProperty(java, "java.endorsed.dirs", appendSystemPath("java.endorsed.dirs", new File(geronimoHome, "lib/endorsed")));
        setSystemProperty(java, "java.ext.dirs", appendSystemPath("java.ext.dirs", new File(geronimoHome, "lib/ext")));
        
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
            
            java.setLogError(true);
            java.setOutput(file);
        }

        // Holds any exception that was thrown during startup
        final ObjectHolder errorHolder = new ObjectHolder();

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

        log.debug("Waiting for Geronimo server...");

        // Setup a callback to time out verification
        final ObjectHolder verifyTimedOut = new ObjectHolder();

        TimerTask timeoutTask = new TimerTask() {
            public void run() {
                verifyTimedOut.set(Boolean.TRUE);
            }
        };

        if (verifyTimeout > 0) {
            log.debug("Starting verify timeout task; triggers in: " + verifyTimeout + "s");
            timer.schedule(timeoutTask, verifyTimeout * 1000);
        }

        // Verify server started
        ServerProxy server = new ServerProxy(hostname, port, username, password);
        boolean started = false;
        while (!started) {
            if (verifyTimedOut.isSet()) {
                throw new MojoExecutionException("Unable to verify if the server was started in the given time");
            }

            if (errorHolder.isSet()) {
                throw new MojoExecutionException("Failed to start Geronimo server", (Throwable)errorHolder.get());
            }

            started = server.isFullyStarted();

            if (!started) {
                Throwable error = server.getLastError();
                if (error != null) {
                    log.debug("Server query failed; ignoring", error);
                }

                Thread.sleep(1000);
            }
        }

        // Stop the timer, server should be up now
        timeoutTask.cancel();
        
        log.info("Geronimo server started");

        if (!background) {
            log.info("Waiting for Geronimo server to shutdown...");

            t.join();
        }
    }

    private String appendSystemPath(final String name, final File file) {
        assert name != null;
        assert file != null;

        return System.getProperty(name) + File.pathSeparator + file.getPath();
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
            OptionSet set = (OptionSet)map.get(options);
            
            if (set == null) {
                if ("default".equals(options)) {
                    log.debug("Default optionSet selected, but no optionSet defined with that id; ignoring");
                }
                else {
                    throw new MojoExecutionException("Missing optionSet for id: " + id);
                }
            }
            else {
                selected.add(set);
            }
        }

        return (OptionSet[]) selected.toArray(new OptionSet[selected.size()]);
    }

    private void setSystemProperty(final Java java, final String name, final String value) {
        Environment.Variable var = new Environment.Variable();
        var.setKey(name);
        var.setValue(value);
        java.addSysproperty(var);
    }

    private void setSystemProperty(final Java java, final String name, final File value) {
        Environment.Variable var = new Environment.Variable();
        var.setKey(name);
        var.setFile(value);
        java.addSysproperty(var);
    }

    protected String getGoalName() {
        //
        //FIXME: There has to be way this can be computed instead of hardcoded absolutely.
        //
        return "start-server";
    }
}
