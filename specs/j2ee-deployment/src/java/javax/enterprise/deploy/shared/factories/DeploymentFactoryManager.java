/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
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
 * @version $Revision: 1.2 $ $Date: 2003/08/16 15:37:52 $
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