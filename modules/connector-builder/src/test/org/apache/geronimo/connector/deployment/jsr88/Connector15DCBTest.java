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
package org.apache.geronimo.connector.deployment.jsr88;

import java.net.URL;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.model.DDBeanRoot;
import junit.framework.TestCase;
import org.apache.geronimo.deployment.tools.loader.ConnectorDeployable;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class Connector15DCBTest extends TestCase {
    private ClassLoader classLoader;

    public void testCreateDatabase() throws Exception {
        // Create and test the DDBeanRoot
        URL resource = classLoader.getResource("database.rar");
        assertNotNull(resource);
        ConnectorDeployable deployable = new ConnectorDeployable(resource);
        assertEquals(ModuleType.RAR, deployable.getType());
        Set entrySet = new HashSet(Collections.list(deployable.entries()));
        Set resultSet = new HashSet();
        resultSet.add("META-INF/");
        resultSet.add("META-INF/MANIFEST.MF");
        resultSet.add("META-INF/LICENSE.txt");
        resultSet.add("META-INF/ra.xml");
        resultSet.add("tranql-connector-1.0.jar");
        assertEquals(resultSet, entrySet);
        DDBeanRoot root = deployable.getDDBeanRoot();
        assertNotNull(root);
        assertEquals(ModuleType.RAR, root.getType());
        assertEquals(deployable, root.getDeployableObject());
        // Create the DConfigBeanRoot
        Connector15DCBRoot dcbRoot = new Connector15DCBRoot(root);
        assertNotNull(dcbRoot.getXpaths());
        assertEquals(1, dcbRoot.getXpaths().length);
        assertEquals("connector", dcbRoot.getXpaths()[0]);
        // Try the /connector element
        ConnectorDCB connector = (ConnectorDCB) dcbRoot.getDConfigBean(root.getChildBean(dcbRoot.getXpaths()[0])[0]);
        assertNotNull(connector);
        assertNull(connector.getConfigID());
        assertNull(connector.getInverseClassLoading());
        assertNull(connector.getParentID());
        assertNull(connector.getSuppressDefaultParentID());
        connector.setConfigID("MyDatabase");
        connector.setParentID("org/apache/geronimo/Server");
        //todo: Try the dependency element
        // Try the /connector/resourceadapter element
        assertNotNull(connector.getResourceAdapter());
        assertEquals(1, connector.getResourceAdapter().length);
        ResourceAdapter adapter = connector.getResourceAdapter()[0];
        assertNotNull(adapter);
        //todo: Try the /connector/resourceadapter/outbound-resourceadapter/connection-definition element
        assertNotNull(adapter.getConnectionDefinition());
        assertEquals(0, adapter.getConnectionDefinition().length);
        ConnectionDefinition definition = new ConnectionDefinition();
        adapter.setConnectionDefinition(new ConnectionDefinition[]{definition});
        assertEquals(1, adapter.getConnectionDefinition().length);
        definition.setConnectionFactoryInterface("javax.sql.DataSource");
        //todo: Try the .../connection-definition/connectiondefinition-instance elements
        assertNotNull(definition.getConnectionInstances());
        assertEquals(0, definition.getConnectionInstances().length);
        ConnectionDefinitionInstance instance = new ConnectionDefinitionInstance();
        definition.setConnectionInstance(new ConnectionDefinitionInstance[]{instance});
        assertEquals(1, definition.getConnectionInstances().length);
        assertNotNull(instance.getDDBean());
        //todo: Try the .../connection-definition/connectiondefinition-instance/config-property-setting elements
        assertNotNull(instance.getConfigPropertySetting());
        assertEquals(6, instance.getConfigPropertySetting().length);
        int found = 0;
        for (int i = 0; i < 6; i++) {
            ConfigPropertySetting setting = instance.getConfigPropertySetting(i);
            if(setting.getName().equals("Driver")) {
                assertEquals("", setting.getValue());
                setting.setValue("org.postgresql.Driver");
                ++found;
            } else if(setting.getName().equals("ConnectionURL")) {
                assertEquals("", setting.getValue());
                setting.setValue("jdbc:postgresql://localhost/TestDatabase");
                ++found;
            } else if(setting.getName().equals("UserName")) {
                assertEquals("", setting.getValue());
                setting.setValue("dbuser");
                ++found;
            } else if(setting.getName().equals("Password")) {
                assertEquals("", setting.getValue());
                setting.setValue("dbpass");
                ++found;
            }
        }
        assertEquals(4, found);
        //todo: Try the .../connection-definition/connectionmanager elements
        //todo: Look at the XmlBeans tree and make sure the right stuff is in there
    }

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
    }
}
