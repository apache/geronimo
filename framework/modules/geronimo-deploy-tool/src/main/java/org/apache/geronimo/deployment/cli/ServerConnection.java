/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.cli;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.cli.DeployUtils.SavedAuthentication;

/**
 *
 * @version $Rev$ $Date$
 */
public abstract class ServerConnection {

    protected DeploymentManager manager;
    protected SavedAuthentication auth;

    protected ServerConnection() {        
    }

    public void close() throws DeploymentException {
        if (manager != null) {
            manager.release();
        }
    }

    Serializable getAuthentication() {
        return auth;
    }

    String getServerURI() {
        return (auth == null) ? null : auth.getURI();
    }

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    public boolean isGeronimo() {
        return manager.getClass().getName().startsWith("org.apache.geronimo.");
    }
    
    public static interface UsernamePasswordHandler {

        String getUsername() throws IOException;

        String getPassword() throws IOException;
    }
}
