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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.spi.factories;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;

/**
 * The DeploymentFactory interface is a deployment driver for a J2EE plaform
 * product.  It returns a DeploymentManager object which represents a
 * connection to a specific J2EE platform product.
 *
 * Each application server vendor must provide an implementation of this class
 * in order for the J2EE Deployment API to work with their product.
 *
 * The class implementing this interface should have a public no-argument
 * constructor, and it should be stateless (two instances of the class should
 * always behave the same).  It is suggested but not required that the class
 * have a static initializer that registers an instance of the class with the
 * DeploymentFactoryManager class.
 *
 * A <tt>connected</tt> or <tt>disconnected</tt> DeploymentManager can be
 * requested.  A DeploymentManager that runs connected to the platform can
 * provide access to J2EE resources.  A DeploymentManager that runs
 * disconnected only provides module deployment configuration support.
 *
 * @see javax.enterprise.deploy.shared.factories.DeploymentFactoryManager
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:35 $
 */
public interface DeploymentFactory {
    /**
     * Tests whether this factory can create a DeploymentManager object based
     * on the specified URI.  This does not indicate whether such an attempt
     * will be successful, only whether the factory can handle the uri.
     *
     * @param uri The uri to check
     *
     * @return <tt>true</tt> if the factory can handle the uri.
     */
    public boolean handlesURI(String uri);

    /**
     * Returns a <tt>connected</tt> DeploymentManager instance.
     *
     * @param uri      The URI that specifies the connection parameters
     * @param username An optional username (may be <tt>null</tt> if no
     *                 authentication is required for this platform).
     * @param password An optional password (may be <tt>null</tt> if no
     *                 authentication is required for this platform).
     *
     * @return A ready DeploymentManager instance.
     *
     * @throws DeploymentManagerCreationException occurs when a
     *         DeploymentManager could not be returned (server down, unable
     *         to authenticate, etc).
     */
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException;

    /**
     * Returns a <tt>disconnected</tt> DeploymentManager instance.
     *
     * @param uri the uri of the DeploymentManager to return.
     *
     * @return A DeploymentManager <tt>disconnected</tt> instance.
     *
     * @throws DeploymentManagerCreationException occurs if the
     *         DeploymentManager could not be created.
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException;

    /**
     * Provide a string with the name of this vendor's DeploymentManager.
     *
     * @return the name of the vendor's DeploymentManager.
     */
    public String getDisplayName();

    /**
     * Provides a string identifying the version of this vendor's
     * DeploymentManager.
     *
     * @return the name of the vendor's DeploymentManager.
     */
    public String getProductVersion();
}