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

import java.io.OutputStream;
import java.io.InputStream;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:36 $
 */
public class DeploymentConfigurationImpl implements DeploymentConfiguration {
    private final DeployableObject deployable;

    public DeploymentConfigurationImpl(DeployableObject deployable) {
        this.deployable = deployable;
    }

    public DeployableObject getDeployableObject() {
        return deployable;
    }

    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot bean) throws ConfigurationException {
        return null;
    }

    public void removeDConfigBean(DConfigBeanRoot bean) throws BeanNotFoundException {
    }

    public void save(OutputStream outputArchive) throws ConfigurationException {
    }

    public void restore(InputStream inputArchive) throws ConfigurationException {
    }

    public void saveDConfigBean(OutputStream outputArchive, DConfigBeanRoot bean) throws ConfigurationException {
    }

    public DConfigBeanRoot restoreDConfigBean(InputStream inputArchive, DDBeanRoot bean) throws ConfigurationException {
        return null;
    }
}
