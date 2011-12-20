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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.shell.deploy.ConnectCommand;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "geronimo", name = "wait-for-server", description = "Wait for a Geronimo server to start")
public class WaitForServerCommand extends ConnectCommand {

    boolean timedOut = false;

    @Option(name = "-t", aliases = { "--timeout" }, description = "Specify the time in seconds to wait while verifying that the server has started. Default 60 seconds")
    int timeout = 60;

    private class TimingTimerTask extends TimerTask {
        @Override
        public void run() {
            timedOut = true;
        }
    }

    protected Object doExecute() throws Exception {
        if (isEmbedded()) {
            // do nothing in embedded mode
            return null;
        }
        
        Timer timer = new Timer(true);

        TimerTask timeoutTask = new TimingTimerTask();
        if (timeout > 0) {
            timer.schedule(timeoutTask, timeout * 1000);

        }
        println("Waiting for Geronimo server: " + hostname + " :" + port);
        boolean started = false;

        while (!started) {
            if (timedOut) {
                throw new Exception("Unable to verify if the server was started in the given time");
            }
            ServerConnection connection;
            ServerProxy server = null;
            try {
                connection = connect();
                server = new ServerProxy(((RemoteDeploymentManager)connection.getDeploymentManager()).getJMXConnector());
            } catch (Exception error) {
                log.debug("Server query failed; ignoring", error);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (server != null) {
                started = server.isFullyStarted();
            }
            if (!started) {
                Throwable error = server.getLastError();
                if (error != null) {
                    log.debug("Server query failed; ignoring", error);
                }
                Thread.sleep(1000);

            }
        }
        if (started) {
            timeoutTask.cancel();
            timer.cancel();
        }

        println("Geronimo server is started");
        return null;
    }

    protected ServerConnection connect() {
        ServerConnection connection = (ServerConnection) session.get(SERVER_CONNECTION);

        if (connection == null) {
            try {
                connection = openConnection(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return connection;
    }

}
