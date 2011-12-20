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

package org.apache.geronimo.shell.deploy;

import java.io.IOException;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.cli.deployer.ConnectionParamsImpl;
import org.apache.geronimo.deployment.cli.OfflineServerConnection;
import org.apache.geronimo.deployment.cli.OnlineServerConnection;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.cli.ServerConnection.UsernamePasswordHandler;
import org.apache.geronimo.deployment.plugin.factories.BaseDeploymentFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.shell.BaseCommandSupport;
import org.apache.felix.service.command.CommandSession;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "connect", description = "Connect to a Geronimo server")
public class ConnectCommand extends BaseCommandSupport {
    // the environment variable name use to store the session. 1
    public static final String SERVER_CONNECTION = "geronimo.ServerConnection";

    @Option(name="-s", aliases={"--hostname", "--server"}, description="Hostname, default localhost", required = false, multiValued = false)
    protected String hostname = "localhost";

    @Option(name="-p", aliases={"--port"}, description="Port, default 1099", required = false, multiValued = false)
    protected int port = 1099;

    @Option(name="-u", aliases={"--username"}, description="Username", required = false, multiValued = false)
    protected String username;

    @Option(name="-w", aliases={"--password"}, description="Password", required = false, multiValued = false)
    protected String password;

    @Option(name="--secure", description="Use secure channel", required = false, multiValued = false)
    protected boolean secure = false;

    /**
     * Base execution method.  This serves as both the body
     * of the CONNECT command and the forwarder for any other
     * command that is a connection subclass.
     *
     * @return
     * @exception Exception
     */
    protected Object doExecute() throws Exception {
        ServerConnection connection = openConnection(false);
        // Forward to the override
        return doExecute(connection);
    }

    protected Object doExecute(ServerConnection connection) throws Exception {
        return null;
    }


    /**
     * Conditionally obtain a session connection.  If there
     * is one cached in the session, use it, otherwise connect
     * to the target host and use it.
     *
     * @return A ServerConnection object for talking to the target server.
     */
    protected ServerConnection connect() throws Exception {
        ServerConnection connection = (ServerConnection)session.get(SERVER_CONNECTION);

        if (connection == null) {
            connection = openConnection(false);
        }

        return connection;
    }

    /**
     * Test if we have a valid server connection set in the
     * shell environment.
     *
     * @return true if there is an active server connection, false otherwise.
     */
    protected boolean isConnected() {
        return session.get(SERVER_CONNECTION) != null;
    }

    /**
     * Disconnect the current server connection, if any.
     */
    protected void disconnect() {
        // disconnect the session if there is something set in the command session
        ServerConnection connection = (ServerConnection)session.get(SERVER_CONNECTION);

        if (connection != null) {
            try {
                connection.close();
            }
            catch (Exception e) {
                // ignore
            }
        }
        // remove the old session variable
        session.put(SERVER_CONNECTION, null);
    }


    /**
     * Open a connection to a target server and save the
     * connection in the session environment so it may
     * be reused for subsequent commands.
     *
     * @param quiet  Indicates where this should give connection status indicatores.
     *
     * @return The ServerConnection object for the connection.
     * @exception Exception
     */
    protected ServerConnection openConnection(boolean quiet) throws Exception {
        Kernel kernel = getKernel();

        ServerConnection connection;

        if (isEmbeddedServer(hostname, port)) {
            connection = new OfflineServerConnection(kernel, false);
        } else {
            if (!quiet) {
                println("Connecting to Geronimo server: " + hostname + ":" + port);
            }

            ConnectionParamsImpl connectionParams = new ConnectionParamsImpl(null, hostname, port, null, username, password, false, false, false, secure);
            connection = new OnlineServerConnection(connectionParams, new ShellUserPasswordHandler(session));
        }

        // Disconnect previous connection if any
        disconnect();
        // store this in the permanent session
        session.put(SERVER_CONNECTION, connection);

        if (!quiet) {
            println("Connection established");
        }

        return connection;
    }

    /**
     * Simple password handler for the gogo shell.
     */
    class ShellUserPasswordHandler implements UsernamePasswordHandler {
        CommandSession session;

        public ShellUserPasswordHandler(CommandSession session) {
            this.session = session;
        }

        public String getPassword() throws IOException {
            return readPassword("Password: ");
        }

        public String getUsername() throws IOException {
            return readLine("Username: ");
        }
    }
}
