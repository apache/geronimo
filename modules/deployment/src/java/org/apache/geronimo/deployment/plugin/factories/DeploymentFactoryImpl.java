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

package org.apache.geronimo.deployment.plugin.factories;

import java.util.Collections;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.plugin.DeploymentManagerImpl;
import org.apache.geronimo.deployment.plugin.DisconnectedServer;
import org.apache.geronimo.deployment.plugin.local.LocalServer;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 * Implementation of JSR88 DeploymentFactory.
 *
 * This will create a DeploymentManager using a local Geronimo kernel
 * to contain the GBeans that are responsible for deploying each module
 * type.
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/28 10:08:47 $
 */
public class DeploymentFactoryImpl implements DeploymentFactory {
    public static final String URI_PREFIX = "deployer:geronimo:";

    public String getDisplayName() {
        return "Geronimo";
    }

    public String getProductVersion() {
        return "0.1";
    }

    public boolean handlesURI(String uri) {
        return uri.startsWith(URI_PREFIX);
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }

        try {
            GBeanMBean server = new GBeanMBean(DisconnectedServer.GBEAN_INFO);
            return createManager(server);
        } catch (InvalidConfigurationException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to create disconnected server").initCause(e);
        }
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }

        try {
            GBeanMBean server = new GBeanMBean(LocalServer.GBEAN_INFO);
            return createManager(server);
        } catch (InvalidConfigurationException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to create disconnected server").initCause(e);
        }
    }

    private DeploymentManager createManager(GBeanMBean server) throws DeploymentManagerCreationException {
        Kernel kernel = new Kernel("geronimo.deployment", "geronimo.deployment");
        try {
            kernel.boot();
        } catch (Exception e) {
            throw (DeploymentManagerCreationException) new DeploymentManagerCreationException("Unable to boot embedded kernel").initCause(e);
        }

        GBeanMBean manager;
        try {
            ObjectName serverName = new ObjectName("geronimo.deployment:role=DeploymentServer");
            kernel.loadGBean(serverName, server);
            kernel.startGBean(serverName);

            ObjectName managerName = new ObjectName("geronimo.deployment:role=DeploymentManager");
            manager = new GBeanMBean(DeploymentManagerImpl.GBEAN_INFO);
            manager.setReferencePatterns("Server", Collections.singleton(serverName));
            manager.setReferencePatterns("Configurers", Collections.singleton(new ObjectName("geronimo.deployment:role=Configurer,*")));

            // @todo for now lets hard code the deployers to use - ultimately this should use a predefined Configuration

            kernel.loadGBean(managerName, manager);
            kernel.startGBean(managerName);

        } catch (Exception e) {
            // this should not happen - we own this kernel!
            throw (IllegalStateException) new IllegalStateException("Unable to load DeploymentManager").initCause(e);
        }
        return (DeploymentManager) manager.getTarget();
    }

    private void loadFactory(Kernel kernel, GBeanMBean manager, String factory, String className, String configurerClassName) throws Exception {
        ObjectName earFactoryName = new ObjectName("geronimo.deployment:role=Factory,type="+factory);
        GBeanMBean earFactory = new GBeanMBean(className);
        kernel.loadGBean(earFactoryName, earFactory);
        kernel.startGBean(earFactoryName);
        manager.setReferencePatterns(factory+"Factory", Collections.singleton(earFactoryName));

        ObjectName configurerName = new ObjectName("geronimo.deployment:role=Configurer,type="+factory);
        GBeanMBean configurer = new GBeanMBean(configurerClassName);
        kernel.loadGBean(configurerName, configurer);
        kernel.startGBean(configurerName);
    }
}
