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

package org.apache.geronimo.deployment.tools;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class DDBeanRootTest extends TestCase {
    private DDBeanRoot root;
    private ClassLoader classLoader;

    public void testRoot() throws Exception {
        DeployableObject deployable = new MockDeployable();
        URL descriptor = classLoader.getResource("descriptors/app-client1.xml");
        root = new DDBeanRootImpl(deployable, descriptor);
        assertEquals("1.4", root.getDDBeanRootVersion());
        assertEquals(deployable, root.getDeployableObject());
        assertEquals(ModuleType.CAR, root.getType());
        assertEquals("/", root.getXpath());
        assertNull(root.getText("foo"));
        assertTrue(Arrays.equals(new String[] {"Test DD for app-client1"}, root.getText("application-client/description")));
        assertTrue(Arrays.equals(new String[] {"http://localhost"}, root.getText("application-client/env-entry/env-entry-value")));
        assertTrue(Arrays.equals(new String[] {"url/test1", "url/test2"}, root.getText("application-client/env-entry/env-entry-name")));

        DDBean description = root.getChildBean("application-client/description")[0];
        assertEquals("Test DD for app-client1", description.getText());
        assertEquals("application-client/description", description.getXpath());
        assertEquals(description, description.getChildBean("/application-client/description")[0]);
    }

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    private class MockDeployable implements DeployableObject {
        public Enumeration entries() {
            fail();
            throw new AssertionError();
        }

        public DDBean[] getChildBean(String xpath) {
            fail();
            throw new AssertionError();
        }

        public Class getClassFromScope(String className) {
            fail();
            throw new AssertionError();
        }

        public DDBeanRoot getDDBeanRoot() {
            fail();
            throw new AssertionError();
        }

        public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
            fail();
            throw new AssertionError();
        }

        public InputStream getEntry(String name) {
            fail();
            throw new AssertionError();
        }

        public String getModuleDTDVersion() {
            fail();
            throw new AssertionError();
        }

        public String[] getText(String xpath) {
            fail();
            throw new AssertionError();
        }

        public ModuleType getType() {
            return ModuleType.CAR;
        }
    }
}
