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
