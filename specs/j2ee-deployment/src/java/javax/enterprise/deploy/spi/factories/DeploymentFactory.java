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
 * @version $Revision: 1.2 $ $Date: 2003/09/04 05:41:21 $
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