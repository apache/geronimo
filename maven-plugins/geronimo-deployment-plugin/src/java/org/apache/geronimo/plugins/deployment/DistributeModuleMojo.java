/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import java.io.File;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.plugins.util.DeploymentClient;

//
// TODO: Rename to DistributeMojo
//

/**
 * ???
 *
 * @goal distribute
 * 
 * @version $Rev$ $Date$
 */
public class DistributeModuleMojo extends AbstractModuleMojo {

    /**
     * @parameter
     */
    private File module;

    /**
     * @parameter
     */
    private File plan;

    protected void doExecute() throws Exception {
        DeploymentManager manager = getDeploymentManager();
        Target[] targets = manager.getTargets();

        ProgressObject progress = manager.distribute(targets, module, plan);
        DeploymentClient.waitFor(progress);
    }
}
