/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.cli.controller;

import java.io.IOException;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.TextController;

/**
 * Connect to a DeploymentManager.
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/16 04:39:41 $
 */
public class ConnectDeploymentManager
    extends TextController
{

    private static final Log log = LogFactory.getLog(ConnectDeploymentManager.class);

    /**
     * Default URL of a Geronimo DeploymentManager.
     */
    public static final String DEFAULT_URI = "deployer:geronimo:jmx:rmi://localhost/jndi/rmi:/JMXConnector";
    
    public ConnectDeploymentManager(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        context.out.println("\n\nEnter deployment server URI. Leave blank for the default URL '" + DEFAULT_URI + "'");
        context.out.print("URI: ");
        context.out.flush();
        try {
            String uri = context.in.readLine();
            if( uri.equals("") ) {
                uri = DEFAULT_URI;
            }

            context.out.println("Enter an optional username");
            context.out.print("Username: ");
            context.out.flush();
            String username = context.in.readLine();
            if( username.equals("") ) {
                username = null;
            }

            context.out.println("Enter an optional password");
            context.out.print("Password: ");
            context.out.flush();
            String password = context.in.readLine();
            if( password.equals("") ) {
                password = null;
            }

            context.out.println("\nConnecting to DeploymentManager " +
                username + "@" + uri + "...");
            context.deployer = DeploymentFactoryManager.getInstance().getDeploymentManager(uri, username, password);
            context.out.println("Connected.");
            context.connected = true;
            context.uri = uri;
            context.username = username;
            context.password = password;
        } catch(DeploymentManagerCreationException e) {
            log.error("Can't create deployment manager", e);
            return;
        } catch(IOException e) {
            log.error("Unable to read user input", e);
            return;
        }
    }

}
