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

package org.apache.geronimo.deployment.dconfigbean;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;

import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public abstract class DeploymentConfigurationSupport implements DeploymentConfiguration {
    private final DeployableObject deployable;

    protected DConfigBeanRootSupport dConfigRoot;

    public DeploymentConfigurationSupport(DeployableObject deployable, DConfigBeanRootSupport dConfigRoot) {
        this.deployable = deployable;
        this.dConfigRoot = dConfigRoot;
    }

    public DeployableObject getDeployableObject() {
        return deployable;
    }

    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot bean) throws ConfigurationException {
        if (getDeployableObject().getDDBeanRoot().equals(bean)) {
            return dConfigRoot;
        }
        return null;
    }

    public void removeDConfigBean(DConfigBeanRoot bean) throws BeanNotFoundException {
    }

    public void save(OutputStream outputArchive) throws ConfigurationException {
        try {
            dConfigRoot.toXML(outputArchive);
            outputArchive.flush();
        } catch (IOException e) {
            throw (ConfigurationException) new ConfigurationException("Unable to save configuration").initCause(e);
        }
    }

    public void restore(InputStream inputArchive) throws ConfigurationException {
        try {
            dConfigRoot.fromXML(inputArchive);
        } catch (IOException e) {
            throw (ConfigurationException) new ConfigurationException("Error reading configuration input").initCause(e);
        } catch (XmlException e) {
            throw (ConfigurationException) new ConfigurationException("Error parsing configuration input").initCause(e);
        }
    }

    public void saveDConfigBean(OutputStream outputArchive, DConfigBeanRoot bean) throws ConfigurationException {
        try {
            ((DConfigBeanRootSupport)bean).toXML(outputArchive);
            outputArchive.flush();
        } catch (IOException e) {
            throw (ConfigurationException) new ConfigurationException("Unable to save configuration").initCause(e);
        }
    }

    //todo figure out how to implement this.
    public DConfigBeanRoot restoreDConfigBean(InputStream inputArchive, DDBeanRoot bean) throws ConfigurationException {
        return null;
    }
}
