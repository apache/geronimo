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

package org.apache.geronimo.mavenplugins.geronimo;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.geronimo.genesis.ObjectHolder;

/**
 * Start the Geronimo server.
 *
 * @goal start
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

    private Timer timer = new Timer(true);

    protected void doExecute() throws Exception {
        log.info("Starting Geronimo server...");

        doInstall();

        // Setup the JVM to start the server with
        final Java java = (Java)createTask("java");
        java.setJar(new File(installDir, "bin/server.jar"));
        java.setDir(installDir);
        java.setFailonerror(true);
        java.setFork(true);
        java.setLogError(true);

        if (timeout > 0) {
            java.setTimeout(new Long(timeout * 1000));
        }

        //
        // TODO: Capture output/error to files
        //

        if (maximumMemory != null) {
            java.setMaxmemory(maximumMemory);
        }

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

        //
        // TODO: Support --override
        //

        //
        // TODO: Support JVM args for debug mode, add debug flag to enable or disable
        //

        // Holds any exception that was thrown during startup (as the cause)
        final Throwable errorHolder = new Throwable();

        // Start the server int a seperate thread
        Thread t = new Thread("Geronimo Server Runner") {
            public void run() {
                try {
                    java.execute();
                }
                catch (Exception e) {
                    errorHolder.initCause(e);

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

        log.debug("Starting verify timeout task; triggers in: " + verifyTimeout + "s");

        TimerTask timeoutTask = new TimerTask() {
            public void run() {
                verifyTimedOut.set(Boolean.TRUE);
            }
        };

        if (verifyTimeout > 0) {
            timer.schedule(timeoutTask, verifyTimeout * 1000);
        }

        // Verify server started
        ServerProxy server = new ServerProxy(port, username, password);
        boolean started = false;
        while (!started) {
            if (verifyTimedOut.isSet()) {
                throw new MojoExecutionException("Unable to verify if the server was started in the given time");
            }

            if (errorHolder.getCause() != null) {
                throw new MojoExecutionException("Failed to start Geronimo server", errorHolder.getCause());
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
}
