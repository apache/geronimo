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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.jmx.LocalDeploymentManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;

/**
 * Supports offline connections to the server, via JSR-88.
 *
 * @version $Rev$ $Date$
 */
public class OfflineServerConnection extends ServerConnection {

    private final boolean startOfflineDeployer;
    
    public OfflineServerConnection(Kernel kernel, boolean startDeployer) throws DeploymentException {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        if (startDeployer) {
            startOfflineDeployer(kernel);
        }
        try {
            manager = new LocalDeploymentManager(kernel);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
        startOfflineDeployer = startDeployer;
    }

    public boolean isOfflineDeployerStarted() {
        return startOfflineDeployer;
    }
    
    protected void startOfflineDeployer(Kernel kernel) throws DeploymentException {
        try {
            OfflineDeployerStarter offlineDeployerStarter = new OfflineDeployerStarter(kernel);
            offlineDeployerStarter.start();
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException(e);
        }
    }

}
