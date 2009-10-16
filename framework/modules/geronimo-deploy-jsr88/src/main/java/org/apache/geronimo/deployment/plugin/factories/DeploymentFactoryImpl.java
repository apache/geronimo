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

package org.apache.geronimo.deployment.plugin.factories;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

/**
 * Implementation of JSR88 DeploymentFactory.
 *
 * This will create a DeploymentManager using a local Geronimo kernel
 * to contain the GBeans that are responsible for deploying each module
 * type.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentFactoryImpl extends BaseDeploymentFactory implements DeploymentFactory {

    static {
        DeploymentFactoryManager manager = DeploymentFactoryManager.getInstance();
        manager.registerDeploymentFactory(new DeploymentFactoryImpl());
    }

}
