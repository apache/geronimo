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
 * ====================================================================
 */
package javax.enterprise.deploy.shared.factories;

import junit.framework.TestCase;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;

import javax.enterprise.deploy.spi.factories.MockDeploymentFactory;

/**
 * Low level tests on the DeploymentFactoryManager.
 */
public class DeploymentFactoryManagerTest extends TestCase {
    private DeploymentFactoryManager factoryManager;

    protected void setUp() throws Exception {
        super.setUp();
        factoryManager = DeploymentFactoryManager.getInstance();
    }

    protected void tearDown() throws Exception {
        factoryManager = null;
        super.tearDown();
    }

    public void testGetDeploymentManagerWithoutAnyRegisteredFactories() {
        try {
            factoryManager.getDeploymentManager("invalid-uri", null, null);
            fail("Expected a DeploymentManagerCreationException");
        } catch (DeploymentManagerCreationException e) {
            assertTrue(e.getMessage().startsWith("Could not get DeploymentManager"));
        }
    }

    public void testDisconnectedGetDeploymentManagerWithoutAnyRegisteredFactories() {
        try {
            factoryManager.getDisconnectedDeploymentManager("invalid-uri");
            fail("Expected a DeploymentManagerCreationException");
        } catch (DeploymentManagerCreationException e) {
            assertTrue(e.getMessage().startsWith("Could not get DeploymentManager"));
        }
    }

    public void testGetDeploymentManagerWithNullURI() {
        try {
            factoryManager.getDeploymentManager(null, null, null);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch(DeploymentManagerCreationException e) {
            fail("Unexpected Exception: "+e.getMessage());
        }
    }

    public void testDisconnectedGetDeploymentManagerWithNullURI() {
        try {
            factoryManager.getDisconnectedDeploymentManager(null);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch(DeploymentManagerCreationException e) {
            fail("Unexpected Exception: "+e.getMessage());
        }
    }

    public void testRegisterNull() {
        try {
            factoryManager.registerDeploymentFactory(null);
            fail("Should have gotten an IllegalArgumentException");
        } catch(IllegalArgumentException e) {
        }
    }

    public void testRegisterDeploymentFactory() {
        int initialNumberOfFactories = factoryManager.getDeploymentFactories().length;

        DeploymentFactory factory = new MockDeploymentFactory();
        factoryManager.registerDeploymentFactory(factory);

        int expectedNumberOfFactories = initialNumberOfFactories + 1;
        int currentNumberOfFactories = factoryManager.getDeploymentFactories().length;

        assertEquals(expectedNumberOfFactories, currentNumberOfFactories);
    }

    public void testGetDeploymentManager() {
        ensureFactoryRegistered();
        DeploymentManager deploymentManager = null;
        try {
            deploymentManager = factoryManager.getDeploymentManager("deployer:geronimo://server:port/application", "username", "password");
        } catch (DeploymentManagerCreationException e) {
            fail("Didn't expect a DeploymentManagerException here.");
        }
        assertNotNull("Expected an instance of the DeploymentManager", deploymentManager);
    }

    public void testGetDisconnectedDeploymentManager() {
        ensureFactoryRegistered();
        DeploymentManager deploymentManager = null;
        try {
            deploymentManager = factoryManager.getDeploymentManager("deployer:geronimo:", null, null);
        } catch (DeploymentManagerCreationException e) {
            fail("Didn't expect a DeploymentManagerException here.");
        }
        assertNotNull("Expected an instance of the DeploymentManager", deploymentManager);
    }

    public void testDeploymentManagerCreationException() {
        ensureFactoryRegistered();
        try {
            factoryManager.getDisconnectedDeploymentManager("throw-exception");
            fail("Expected a DeploymentManagerCreationException");
        } catch (DeploymentManagerCreationException e) {
            //
            // jason: probably not a hot idea to validate the message here
            //
            // assertTrue(e.getMessage().startsWith("Could not get DeploymentManager"));
        }
    }

    private void ensureFactoryRegistered() {
        int numberOfFactories = factoryManager.getDeploymentFactories().length;
        if(numberOfFactories == 0) {
            factoryManager.registerDeploymentFactory(new MockDeploymentFactory());
        }
        assertTrue("We should have a registered DeploymentFactory", numberOfFactories > 0);
    }
}
