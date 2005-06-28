/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.deployment.plugin.DisconnectedDeploymentManager;

/**
 * Implementation of JSR88 DeploymentFactory.
 *
 * This will create a DeploymentManager using a local Geronimo kernel
 * to contain the GBeans that are responsible for deploying each module
 * type.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentFactoryImpl implements DeploymentFactory {
    public static final String URI_PREFIX = "deployer:geronimo:";

    public String getDisplayName() {
        return "Apache Geronimo";
    }

    public String getProductVersion() {
        return "1.0";
    }

    public boolean handlesURI(String uri) {
        return uri.startsWith(URI_PREFIX);
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }

        return new DisconnectedDeploymentManager();
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            return null;
        }

        try {
            uri = uri.substring(URI_PREFIX.length());
            if (uri.startsWith("jmx")) {

                Map environment = new HashMap();
                String[] credentials = new String[]{username, password};
                environment.put(JMXConnector.CREDENTIALS, credentials);
                
                try {
                    JMXServiceURL address = new JMXServiceURL("service:" + uri);
                    JMXConnector jmxConnector = JMXConnectorFactory.connect(address, environment);
                    JMXDeploymentManager manager = new JMXDeploymentManager(jmxConnector);
                    return manager;
                } catch (IOException e) {
                    throw (DeploymentManagerCreationException)new DeploymentManagerCreationException(e.getMessage()).initCause(e);
                } catch (SecurityException e) {
                    throw (AuthenticationFailedException) new AuthenticationFailedException("Invalid login.").initCause(e);
                }
            } else {
                throw new DeploymentManagerCreationException("Invalid URI: " + uri);
            }
        } catch (RuntimeException e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            e.printStackTrace();
            throw e;
        } catch (Error e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            e.printStackTrace();
            throw e;
        }
    }

	static {
        DeploymentFactoryManager manager = DeploymentFactoryManager.getInstance();
        manager.registerDeploymentFactory(new DeploymentFactoryImpl());
    }
}
