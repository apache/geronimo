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

import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;
import org.apache.geronimo.mavenplugins.geronimo.reporting.ReportingMojoSupport;

/**
 * Stop the Geronimo server.
 *
 * @goal stop-server
 *
 * @version $Rev$ $Date$
 */
public class StopServerMojo
    extends ReportingMojoSupport
{
    /**
     * Fail the build if the server is not started.
     *
     * @parameter expression="${failIfNotStarted}" default-value="true"
     */
    private boolean failIfNotStarted = true;
    
    protected void doExecute() throws Exception {
        ServerProxy server = new ServerProxy(hostname, port, username, password);

        //
        // TODO: Maybe we just need isStarted() not need to be fully started?
        //
        
        if (!server.isFullyStarted()) {
            String msg = "Server does not appear to be started";
            
            if (failIfNotStarted) {
                throw new MojoExecutionException(msg);
            }
            else {
                log.warn(msg);
            }
        }
        else {
            log.info("Stopping Geronimo server...");
            
            server.shutdown();
            server.waitForStop();
            server.closeConnection();

            //
            // TODO: Verify its down?
            //
        }
    }

    protected String getFullClassName() {
        return this.getClass().getName();
    } 
}
