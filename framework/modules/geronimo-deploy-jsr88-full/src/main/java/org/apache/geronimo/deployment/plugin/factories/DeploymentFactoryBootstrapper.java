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

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.MainBootstrapper;
import org.osgi.framework.BundleContext;

/**
 *
 * @version $Rev: 503905 $ $Date: 2007-02-06 09:20:49 +1100 (Tue, 06 Feb 2007) $
 */
public class DeploymentFactoryBootstrapper implements DeploymentFactory {
    private final Kernel kernel;
    private final DeploymentFactory delegate;
    
    public DeploymentFactoryBootstrapper() throws DeploymentManagerCreationException {
        kernel = newKernel();
        
        try {
            delegate = (DeploymentFactory) kernel.getGBean(DeploymentFactory.class);
        } catch (Exception e) {
            throw (DeploymentManagerCreationException) new DeploymentManagerCreationException("See nested").initCause(e);
        }
    }
    

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        return delegate.getDeploymentManager(uri, username, password);
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return delegate.getDisconnectedDeploymentManager(uri);
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public String getProductVersion() {
        return delegate.getProductVersion();
    }

    public boolean handlesURI(String uri) {
        return delegate.handlesURI(uri);
    }
    
    protected Kernel newKernel() throws DeploymentManagerCreationException {
//        ClassLoader classLoader = DeploymentFactoryBootstrapper.class.getClassLoader();
        BundleContext bundleContext = null;
        MainBootstrapper bootstrapper = new MainBootstrapper();
        try {
            bootstrapper.bootKernel(bundleContext);
            bootstrapper.loadBootConfiguration(bundleContext);
            bootstrapper.loadPersistentConfigurations();
        } catch (Exception e) {
            throw (DeploymentManagerCreationException) new DeploymentManagerCreationException("See nested").initCause(e);
        }
        return bootstrapper.getKernel();
    }

}
