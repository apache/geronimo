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
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/16 12:28:26 $
 */
public final class DeploymentFactoryManager {
    private static DeploymentFactoryManager instance;

    private ArrayList deploymentFactories = new ArrayList();

    private DeploymentFactoryManager() {
    }

    public static DeploymentFactoryManager getInstance() {
        if (instance == null) {
            instance = new DeploymentFactoryManager();
        }
        return instance;
    }

    public DeploymentFactory[] getDeploymentFactories() {
        return (DeploymentFactory[]) deploymentFactories.toArray(new DeploymentFactory[]{});
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        // RI doesn't care about uri being null, neither do we

        for (Iterator i = deploymentFactories.iterator(); i.hasNext();) {
            DeploymentFactory factory = (DeploymentFactory) i.next();
            if (factory != null) {
                if (factory.handlesURI(uri)) {
                    try {
                        return factory.getDeploymentManager(uri, username, password);
                    } catch (DeploymentManagerCreationException e) {
                        // Just like the RI we throw a new exception with a generic message
                        throw new DeploymentManagerCreationException("Could not get DeploymentManager");
                    }
                }
            }
        }
        throw new DeploymentManagerCreationException("Could not get DeploymentManager");
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        // RI doesn't care about uri being null, neither do we

        for (Iterator i = deploymentFactories.iterator(); i.hasNext();) {
            DeploymentFactory factory = (DeploymentFactory) i.next();
            if (factory != null) {
                if (factory.handlesURI(uri)) {
                    try {
                        return factory.getDisconnectedDeploymentManager(uri);
                    } catch (DeploymentManagerCreationException e) {
                        // Just like the RI we throw a new exception with a generic message
                        throw new DeploymentManagerCreationException("Could not get DeploymentManager");
                    }
                }
            }
        }
        throw new DeploymentManagerCreationException("Could not get DeploymentManager");
    }

    public void registerDeploymentFactory(DeploymentFactory factory) {
        // apparently we dont care about null values, the Sun RI even adds the null
        // to the list. So after registerDeploymentFactory(null) getDeploymentFactories()
        // return an array with length 1.
        deploymentFactories.add(factory);
    }
}