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

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.geronimo.mavenplugins.geronimo.GeronimoMojoSupport;
import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;

/**
 * Stop the Geronimo server.
 *
 * @goal stop-server
 *
 * @version $Rev$ $Date$
 */
public class StopServerMojo
    extends GeronimoMojoSupport
{
    protected void doExecute() throws Exception {
        ServerProxy server = new ServerProxy(hostname, port, username, password);

        //
        // TODO: Maybe we just need isStarted() not need to be fully started?
        //
        
        if (!server.isFullyStarted()) {
            throw new MojoExecutionException("Server does not appear to be started");
        }
        else {
            log.info("Stopping Geronimo server...");
            
            server.shutdown();

            //
            // TODO: Verify its down?
            //
        }
    }
}
