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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.shared.factories;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * The DeploymentFactoryManager class is a central registry for J2EE
 * DeploymentFactory objects. The DeploymentFactoryManager retains references
 * to DeploymentFactory objects loaded by a tool. A DeploymentFactory object
 * provides a reference to a DeploymentManager. The DeploymentFactoryManager
 * has been implemented as a singleton. A tool gets a reference to the
 * DeploymentFactoryManager via the getInstance method. The
 * DeploymentFactoryManager can return two types of DeploymentManagers, a
 * connected DeploymentManager and a disconnected DeploymentManager. The
 * connected DeploymentManager provides access to any product resources that
 * may be required for configurations and deployment. The method to retrieve a
 * connected DeploymentManager is getDeploymentManager. This method provides
 * parameters for user name and password that the product may require for user
 * authentication. A disconnected DeploymentManager does not provide access to
 * a running J2EE product. The method to retrieve a disconnected
 * DeploymentManager is getDisconnectedDeploymentManager. A disconnected
 * DeploymentManager does not need user authentication information.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:51 $
 */
public final class DeploymentFactoryManager {
    private static DeploymentFactoryManager instance;

    private ArrayList deploymentFactories = new ArrayList();

    private DeploymentFactoryManager() {
    }

    /**
     * Retrieve the Singleton DeploymentFactoryManager
     *
     * @return DeploymentFactoryManager instance
     */
    public static DeploymentFactoryManager getInstance() {
        if(instance == null) {
            instance = new DeploymentFactoryManager();
        }
        return instance;
    }

    /**
     * Retrieve the lists of currently registered DeploymentFactories.
     *
     * @return the list of DeploymentFactory objects or an empty array if there are none.
     */
    public DeploymentFactory[] getDeploymentFactories() {
        return (DeploymentFactory[])deploymentFactories.toArray(new DeploymentFactory[deploymentFactories.size()]);
    }

    /**
     * Retrieves a DeploymentManager instance to use for deployment. The caller
     * provides a URI and optional username and password, and all registered
     * DeploymentFactories will be checked. The first one to understand the URI
     * provided will attempt to initiate a server connection and return a ready
     * DeploymentManager instance.
     *
     * @param uri      The uri to check
     * @param username An optional username (may be <tt>null</tt> if no
     *                 authentication is required for this platform).
     * @param password An optional password (may be <tt>null</tt> if no
     *                 authentication is required for this platform).
     *
     * @return A ready DeploymentManager instance.
     *
     * @throws DeploymentManagerCreationException Occurs when the factory
     *         appropriate to the specified URI was unable to initialize a
     *         DeploymentManager instance (server down, unable to authenticate,
     *         etc.).
     */
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        if(uri == null) {
            throw new IllegalArgumentException("URI for DeploymentManager should not be null");
        }
        DeploymentManager manager = null;
        for(Iterator i = deploymentFactories.iterator(); i.hasNext();) {
            DeploymentFactory factory = (DeploymentFactory)i.next();
            if(factory.handlesURI(uri)) {
                manager = factory.getDeploymentManager(uri, username, password);
                if(manager != null) {
                    return manager;
                }
            }
        }
        throw new DeploymentManagerCreationException("Could not get DeploymentManager; No registered DeploymentFactory handles this URI");
    }

    /**
     * Return a disconnected DeploymentManager instance.
     *
     * @param uri identifier of the disconnected DeploymentManager to return.
     *
     * @return A DeploymentManager instance.
     *
     * @throws DeploymentManagerCreationException occurs if the
     *         DeploymentManager could not be created.
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if(uri == null) {
            throw new IllegalArgumentException("URI for DeploymentManager should not be null");
        }
        DeploymentManager manager = null;
        for(Iterator i = deploymentFactories.iterator(); i.hasNext();) {
            DeploymentFactory factory = (DeploymentFactory)i.next();
            if(factory.handlesURI(uri)) {
                manager = factory.getDisconnectedDeploymentManager(uri);
                if(manager != null) {
                    return manager;
                }
            }
        }
        throw new DeploymentManagerCreationException("Could not get DeploymentManager; No registered DeploymentFactory handles this URI");
    }

    /**
     * Registers a DeploymentFactory so it will be able to handle requests.
     */ 
    public void registerDeploymentFactory(DeploymentFactory factory) {
        if(factory == null) {
            throw new IllegalArgumentException("DeploymentFactory to register should not be null");
        }
        if(!deploymentFactories.contains(factory)) {
            deploymentFactories.add(factory);
        }
    }
}