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
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.io.InputStream;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.model.DDBeanRoot;

import org.apache.geronimo.deployment.tools.loader.ClientDeployable;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.osgi.framework.Bundle;
import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class ClientDeployableTest extends TestCase {
    private ClassLoader classLoader;

    public void testLoadClient() throws Exception {
        URL resource = classLoader.getResource("deployables/app-client1.jar");
        ClassLoader cl = new URLClassLoader(new URL[] {resource});
        Bundle bundle = new MockBundle(cl, resource.toString(), 0L);
        ClientDeployable deployable = new ClientDeployable(bundle);
        assertEquals(ModuleType.CAR, deployable.getType());
        Set entrySet = new HashSet(Collections.list(deployable.entries()));
        Set resultSet = new HashSet();
        resultSet.add("META-INF/");
        resultSet.add("META-INF/MANIFEST.MF");
        resultSet.add("META-INF/application-client.xml");
        resultSet.add("Main.java");
        resultSet.add("Main.class");
        //TODO implement getEntryPaths, reenable this check
//        assertEquals(resultSet, entrySet);
        InputStream entry = deployable.getEntry("META-INF/application-client.xml");
        assertNotNull(entry);
        entry.close();
        Class main = deployable.getClassFromScope("Main");
        assertEquals("Main", main.getName());

        DDBeanRoot root = deployable.getDDBeanRoot();
        assertNotNull(root);
        assertEquals(ModuleType.CAR, root.getType());
        assertEquals(deployable, root.getDeployableObject());
    }

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
    }
}
