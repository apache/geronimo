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

package org.apache.geronimo.deployment.plugin;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.DDBeanRoot;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/28 10:08:47 $
 */
public class DeploymentConfigurationTest extends TestCase {
    private DeploymentFactory factory;
    private DDBeanRoot root;
    private Application deployable;

    public void testInit() throws Exception {
        DeploymentManager manager = factory.getDisconnectedDeploymentManager("deployer:geronimo:test");
        //DeploymentConfiguration config = manager.createConfiguration(deployable);
        //assertEquals(deployable, config.getDeployableObject());
        //assertNull(config.getDConfigBeanRoot(root));
    }

    protected void setUp() throws Exception {
        factory = new DeploymentFactoryImpl();
        root = new ApplicationRoot();
        deployable = new Application(root);
    }
}
