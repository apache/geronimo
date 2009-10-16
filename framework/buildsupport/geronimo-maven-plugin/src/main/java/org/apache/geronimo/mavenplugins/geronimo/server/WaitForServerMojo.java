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

import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.mojo.pluginsupport.util.ObjectHolder;

import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;
import org.apache.geronimo.mavenplugins.geronimo.reporting.ReportingMojoSupport;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Wait for a Geronimo server to start.
 *
 * @goal wait-for-server
 *
 * @version $Rev$ $Date$
 */
public class WaitForServerMojo
    extends ReportingMojoSupport
{
    /**
     * Time in seconds to wait while verifing that the server has started.
     *
     * @parameter expression="${timeout}" default-value="-1"
     */
    private int timeout = -1;

    private Timer timer = new Timer(true);

    //
    // TODO: See if start-server can share some of this code
    //

    protected void doExecute() throws Exception {
        log.info("Waiting for Geronimo server...");

        // Setup a callback to time out verification
        final ObjectHolder verifyTimedOut = new ObjectHolder();

        TimerTask timeoutTask = new TimerTask() {
            public void run() {
                verifyTimedOut.set(Boolean.TRUE);
            }
        };

        if (timeout > 0) {
            log.debug("Starting verify timeout task; triggers in: " + timeout + "s");
            timer.schedule(timeoutTask, timeout * 1000);
        }

        // Verify server started
        ServerProxy server = new ServerProxy(hostname, port, username, password);
        boolean started = false;
        while (!started) {
            if (verifyTimedOut.isSet()) {
                throw new MojoExecutionException("Unable to verify if the server was started in the given time");
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
        server.closeConnection();

        // Stop the timer, server should be up now
        timeoutTask.cancel();

        log.info("Geronimo server started");
    }

    protected String getFullClassName() {
        return this.getClass().getName();
    } 
}
