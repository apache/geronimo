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
package org.apache.geronimo.connector.deployment.jsr88;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DConfigBean;
import junit.framework.TestCase;
import org.apache.geronimo.connector.deployment.RARConfiguration;
import org.apache.geronimo.deployment.tools.loader.ConnectorDeployable;
import org.apache.geronimo.deployment.service.jsr88.EnvironmentData;
import org.apache.geronimo.deployment.service.jsr88.Artifact;
import org.apache.geronimo.naming.deployment.jsr88.GBeanLocator;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class Connector15DCBTest extends TestCase {
    private ClassLoader classLoader;

    public void testCreateDatabase() throws Exception {
        Bundle bundle = new MockBundle(classLoader, "", 0L);
        // Create and test the DDBeanRoot
        URL resource = classLoader.getResource("database.rar");
        assertNotNull(resource);
        ConnectorDeployable deployable = new ConnectorDeployable(bundle);
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
        assertNull(connector.getEnvironment());
        EnvironmentData environment = new EnvironmentData();
        connector.setEnvironment(environment);
        Artifact configId = new Artifact();
        environment.setConfigId(configId);
        assertNull(configId.getArtifactId());
        assertNull(configId.getGroupId());
        assertNull(configId.getType());
        assertNull(configId.getVersion());
        configId.setGroupId("test");
        configId.setArtifactId("product");
        configId.setType("rar");
        configId.setVersion("1.0");
        Artifact parent = new Artifact();
        Artifact dependency = new Artifact();
        environment.setDependencies(new Artifact[]{parent, dependency});
        assertNull(parent.getArtifactId());
        assertNull(parent.getGroupId());
        assertNull(parent.getType());
        assertNull(parent.getVersion());
        assertNull(dependency.getArtifactId());
        assertNull(dependency.getGroupId());
        assertNull(dependency.getType());
        assertNull(dependency.getVersion());
        parent.setGroupId("org.apache.geronimo.configs");
        parent.setArtifactId("j2ee-server");
        parent.setType("car");
        assertNull(parent.getVersion());
        dependency.setGroupId("postgresql");
        dependency.setArtifactId("postgresql-8.0");
        dependency.setType("jar");
        dependency.setVersion("313.jdbc3");
        // todo: Try the /connector/environment/hidden-classes element
        // todo: Try the /connector/environment/non-overridable-classes element
        // Try the /connector/resourceadapter element
        assertNotNull(connector.getResourceAdapter());
        assertEquals(1, connector.getResourceAdapter().length);
        ResourceAdapter adapter = connector.getResourceAdapter()[0];
        assertNotNull(adapter);
        // Try the /connector/resourceadapter/outbound-resourceadapter/connection-definition element
        assertNotNull(adapter.getConnectionDefinition());
        assertEquals(0, adapter.getConnectionDefinition().length);
        ConnectionDefinition definition = new ConnectionDefinition();
        adapter.setConnectionDefinition(new ConnectionDefinition[]{definition});
        assertEquals(1, adapter.getConnectionDefinition().length);
        definition.setConnectionFactoryInterface("javax.sql.DataSource");
        // Try the .../connection-definition/connectiondefinition-instance elements
        assertNotNull(definition.getConnectionInstances());
        assertEquals(0, definition.getConnectionInstances().length);
        ConnectionDefinitionInstance instance = new ConnectionDefinitionInstance();
        definition.setConnectionInstance(new ConnectionDefinitionInstance[]{instance});
        assertEquals(1, definition.getConnectionInstances().length);
        assertNotNull(instance.getDDBean());
        // Try the .../connection-definition/connectiondefinition-instance/config-property-setting elements
        assertNotNull(instance.getConfigPropertySetting());
        assertEquals(6, instance.getConfigPropertySetting().length);
        int found = 0;
        for (int i = 0; i < 6; i++) {
            ConfigPropertySetting setting = instance.getConfigPropertySetting(i);
            if(setting.getName().equals("Driver")) {
                assertNull(setting.getValue());
                setting.setValue("org.postgresql.Driver");
                ++found;
            } else if(setting.getName().equals("ConnectionURL")) {
                assertNull(setting.getValue());
                setting.setValue("jdbc:postgresql://localhost/TestDatabase");
                ++found;
            } else if(setting.getName().equals("UserName")) {
                assertNull(setting.getValue());
                setting.setValue("dbuser");
                ++found;
            } else if(setting.getName().equals("Password")) {
                assertNull(setting.getValue());
                setting.setValue("dbpass");
                ++found;
            } else {
                assertNotNull(setting.getValue());
            }
        }
        assertEquals(4, found);
        // Try the .../connection-definition/connectionmanager elements
        ConnectionManager manager = instance.getConnectionManager();
        assertNotNull(manager);
        assertFalse(manager.isContainerManagedSecurity());
        assertFalse(manager.isPoolNone());
        assertNotNull(manager.getPoolSingle());
        assertNull(manager.getPoolPartitioned());
        assertFalse(manager.isTransactionLog());
        assertFalse(manager.isTransactionNone());
        assertFalse(manager.isTransactionXA());
        assertFalse(manager.isTransactionXACachingThread());
        assertFalse(manager.isTransactionXACachingTransaction());
        assertTrue(manager.isTransactionLocal());
        SinglePool pool = manager.getPoolSingle();
        assertNull(pool.getMinSize());
        assertNull(pool.getMaxSize());
        assertNull(pool.getIdleTimeoutMinutes());
        assertNull(pool.getBlockingTimeoutMillis());
        assertTrue(pool.isMatchOne());
        assertFalse(pool.isMatchAll());
        assertFalse(pool.isSelectOneAssumeMatch());
        pool.setMinSize(new Integer(2));
        pool.setMaxSize(new Integer(30));
        pool.setBlockingTimeoutMillis(new Integer(5000));
        //todo: Look at the XmlBeans tree and make sure the right stuff is in there
        System.out.println(dcbRoot.getConnectorDocument());
    }

    public void testWriteWithNulls() throws Exception {
        InputStream in = classLoader.getResource("plan-with-nulls.xml").openStream();

        Bundle bundle = new MockBundle(classLoader, "", 0L);
        // Create and test the DDBeanRoot
        URL resource = classLoader.getResource("database.rar");
        assertNotNull(resource);
        ConnectorDeployable deployable = new ConnectorDeployable(bundle);
        assertEquals(ModuleType.RAR, deployable.getType());
        DDBeanRoot root = deployable.getDDBeanRoot();
        assertNotNull(root);
        assertEquals(ModuleType.RAR, root.getType());
        assertEquals(deployable, root.getDeployableObject());
        // Create the DConfigBeanRoot
        Connector15DCBRoot dcbRoot = new Connector15DCBRoot(root);
        RARConfiguration configuration = new RARConfiguration(deployable, dcbRoot);
        configuration.restore(in);
        // Try the /connector element
        ConnectorDCB connector = (ConnectorDCB) dcbRoot.getDConfigBean(root.getChildBean(dcbRoot.getXpaths()[0])[0]);
        assertNotNull(connector);
        // Try the /connector/resourceadapter element
        assertNotNull(connector.getResourceAdapter());
        assertEquals(1, connector.getResourceAdapter().length);
        ResourceAdapter adapter = connector.getResourceAdapter()[0];
        assertNotNull(adapter);
        // Try the /connector/resourceadapter/outbound-resourceadapter/connection-definition element
        assertNotNull(adapter.getConnectionDefinition());
        assertEquals(1, adapter.getConnectionDefinition().length);
        ConnectionDefinition definition = adapter.getConnectionDefinition(0);
        // Try the .../connection-definition/connectiondefinition-instance elements
        assertNotNull(definition.getConnectionInstances());
        assertEquals(1, definition.getConnectionInstances().length);
        ConnectionDefinitionInstance instance = definition.getConnectionInstances()[0];
        // Try the .../connection-definition/connectiondefinition-instance/config-property-setting elements
        assertNotNull(instance.getConfigPropertySetting());
        assertEquals(6, instance.getConfigPropertySetting().length);
        int nullCount = 0;
        for (int i = 0; i < 6; i++) {
            if(instance.getConfigPropertySetting(i).getValue() == null) {
                ++nullCount;
            } else if(instance.getConfigPropertySetting(i).getValue().equals("")) {
                instance.getConfigPropertySetting()[i].setValue(null);
                ++nullCount;
            }
        }
        // Read the generated XML and count config property setting elements (should be 4)
        assertEquals(2, nullCount);
        ByteArrayOutputStream pout = new ByteArrayOutputStream();
        dcbRoot.toXML(pout);
        pout.close();
        ByteArrayInputStream pin = new ByteArrayInputStream(pout.toByteArray());
        GerConnectorDocument doc = GerConnectorDocument.Factory.parse(pin);
        pin.close();
        GerConfigPropertySettingType[] settings = doc.getConnector().getResourceadapterArray(0).
                getOutboundResourceadapter().getConnectionDefinitionArray(0).
                getConnectiondefinitionInstanceArray(0).getConfigPropertySettingArray();
        assertEquals(2, settings.length);
        Set seen = new HashSet();
        for (int i = 0; i < settings.length; i++) {
            GerConfigPropertySettingType setting = settings[i];
            if(seen.contains(setting.getName())) {
                fail("Duplicate for config property setting '"+setting.getName()+"'");
            }
            seen.add(setting.getName());
// These two are set to their defaults and now no longer appear
//            if(setting.getName().equals("CommitBeforeAutocommit")) {
//                assertEquals("false", setting.getStringValue());
//            } else if(setting.getName().equals("ExceptionSorterClass")) {
//                assertEquals("org.tranql.connector.AllExceptionsAreFatalSorter", setting.getStringValue());
            if(setting.getName().equals("Driver")) {
                assertEquals("org.apache.derby.jdbc.EmbeddedDriver", setting.getStringValue());
            } else if(setting.getName().equals("ConnectionURL")) {
                assertEquals("jdbc:derby:TestDatabase;create=true", setting.getStringValue());
            } else fail("Unknown connection setting '"+setting.getName()+"'");
        }
        // Make sure the original objects didn't lose track of the null config settings
        assertEquals(6, instance.getConfigPropertySetting().length);
        // Now set them to blank
        nullCount = 0;
        for (int i = 0; i < 6; i++) {
            if(instance.getConfigPropertySetting()[i].getValue() == null || instance.getConfigPropertySetting()[i].isSetToDefault()) {
                instance.getConfigPropertySetting()[i].setValue("");
                ++nullCount;
            }
        }
        assertEquals(4, nullCount);
        // Now make sure we write out with 6
        pout = new ByteArrayOutputStream();
        dcbRoot.toXML(pout);
        pout.close();
        pin = new ByteArrayInputStream(pout.toByteArray());
        doc = GerConnectorDocument.Factory.parse(pin);
        pin.close();
        settings = doc.getConnector().getResourceadapterArray(0).
                getOutboundResourceadapter().getConnectionDefinitionArray(0).
                getConnectiondefinitionInstanceArray(0).getConfigPropertySettingArray();
        assertEquals(6, settings.length);
        seen.clear();
        for (int i = 0; i < settings.length; i++) {
            GerConfigPropertySettingType setting = settings[i];
            if(seen.contains(setting.getName())) {
                fail("Duplicate for config property setting '"+setting.getName()+"'");
            }
            seen.add(setting.getName());
            if(setting.getName().equals("UserName")) {
                assertEquals("", setting.getStringValue());
            } else if(setting.getName().equals("Password")) {
                assertEquals("", setting.getStringValue());
            } else if(setting.getName().equals("CommitBeforeAutocommit")) {
                assertEquals("", setting.getStringValue());
            } else if(setting.getName().equals("Driver")) {
                assertEquals("org.apache.derby.jdbc.EmbeddedDriver", setting.getStringValue());
            } else if(setting.getName().equals("ExceptionSorterClass")) {
                assertEquals("", setting.getStringValue());
            } else if(setting.getName().equals("ConnectionURL")) {
                assertEquals("jdbc:derby:TestDatabase;create=true", setting.getStringValue());
            } else fail("Unknown connection setting '"+setting.getName()+"'");
        }
    }

    public void testCreateJMSResource() throws Exception {
        // Create and test the DDBeanRoot
        URL resource = classLoader.getResource("jms.rar");
        assertNotNull(resource);
        Bundle bundle = new MockBundle(classLoader, "", 0L);
        ConnectorDeployable deployable = new ConnectorDeployable(bundle);
        assertEquals(ModuleType.RAR, deployable.getType());
        Set entrySet = new HashSet(Collections.list(deployable.entries()));
        assertTrue(entrySet.contains("META-INF/ra.xml"));
        assertTrue(entrySet.contains("activemq-ra-3.2.1.jar"));
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
//        assertNull(connector.getConfigID());
//        assertNull(connector.getInverseClassLoading());
//        assertNull(connector.getParentID());
//        assertNull(connector.getSuppressDefaultParentID());
//        connector.setConfigID("MyJMS");
//        connector.setParentID("geronimo/activemq/1.0/car");
        // Try the /connector/dependency element
//        assertNotNull(connector.getArtifactType());
//        assertEquals(0, connector.getArtifactType().length);
//        Artifact dep = new Artifact();
//        connector.setDependency(new Artifact[]{dep});
//        assertEquals(1, connector.getArtifactType().length);
//        dep.setURI("postgresql/postgresql-8.0/313.jdbc3/jar");
//        assertNull(dep.getArtifactId());
//        assertNull(dep.getGroupId());
//        assertNull(dep.getType());
//        assertNull(dep.getVersion());
//        dep.setGroupId("postgresql");
//        dep.setArtifactId("postgresql-8.0");
//        dep.setVersion("313.jdbc3");
//        assertNull(dep.getURI());
//        assertNull(dep.getType());
        // todo: Try the /connector/import element
        // todo: Try the /connector/hidden-classes element
        // todo: Try the /connector/non-overridable-classes element
        // Try the /connector/resourceadapter element
        assertNotNull(connector.getResourceAdapter());
        assertEquals(1, connector.getResourceAdapter().length);
        ResourceAdapter adapter = connector.getResourceAdapter()[0];
        assertNotNull(adapter);
        // Try the /connector/resourceadapter/resourceadapter-instance element
        assertNull(adapter.getResourceAdapterInstance());
        adapter.setResourceAdapterInstance(new ResourceAdapterInstance());
        ResourceAdapterInstance raInstance = adapter.getResourceAdapterInstance();
        assertNull(raInstance.getResourceAdapterName());
        assertNotNull(raInstance.getConfigProperties());
        assertEquals(7, raInstance.getConfigPropertySetting().length);
        for (int i = 0; i < raInstance.getConfigPropertySetting().length; i++) {
            ConfigPropertySetting config = raInstance.getConfigPropertySetting()[i];
            assertTrue(config.isSetToDefault());
        }
        int found = 0;
        for (int i = 0; i < 7; i++) {
            ConfigPropertySetting setting = raInstance.getConfigPropertySetting(i);
            if(setting.getName().equals("ServerUrl")) {
                setting.setValue("tcp://localhost:12345");
                ++found;
            } else if(setting.getName().equals("UserName")) {
                setting.setValue("test-user");
                ++found;
            } else if(setting.getName().equals("Password")) {
                setting.setValue("test-password");
                ++found;
            }
        }
        assertEquals(found, 3);
        // Try the /connector/resourceadapter/resourceadapter-instance/workmanager
        assertNull(raInstance.getWorkManager());
        GBeanLocator workManager = new GBeanLocator();
        raInstance.setWorkManager(workManager);
        workManager.setGBeanLink("DefaultWorkManager");
        // Try the /connector/resourceadapter/outbound-resourceadapter/connection-definition element
        assertNotNull(adapter.getConnectionDefinition());
        assertEquals(0, adapter.getConnectionDefinition().length);
        ConnectionDefinition definition = new ConnectionDefinition();
        adapter.setConnectionDefinition(new ConnectionDefinition[]{definition});
        assertEquals(1, adapter.getConnectionDefinition().length);
        definition.setConnectionFactoryInterface("javax.jms.ConnectionFactory");
        // Try the .../connection-definition/connectiondefinition-instance elements
        assertNotNull(definition.getConnectionInstances());
        assertEquals(0, definition.getConnectionInstances().length);
        ConnectionDefinitionInstance instance = new ConnectionDefinitionInstance();
        definition.setConnectionInstance(new ConnectionDefinitionInstance[]{instance});
        assertEquals(1, definition.getConnectionInstances().length);
        assertNotNull(instance.getDDBean());
        instance.setName("Test JMS Resource");
        instance.setImplementedInterface(new String[]{"javax.jms.QueueConnectorFactory","javax.jms.TopicConnectionFactory"});
        // Try the .../connection-definition/connectiondefinition-instance/config-property-setting elements
        assertNotNull(instance.getConfigPropertySetting());
        assertEquals(0, instance.getConfigPropertySetting().length);
        // Try the .../connection-definition/connectionmanager elements
        ConnectionManager manager = instance.getConnectionManager();
        assertNotNull(manager);
        assertFalse(manager.isContainerManagedSecurity());
        assertFalse(manager.isPoolNone());
        assertNotNull(manager.getPoolSingle());
        assertNull(manager.getPoolPartitioned());
        assertFalse(manager.isTransactionLog());
        assertFalse(manager.isTransactionNone());
        assertTrue(manager.isTransactionXA());
        assertFalse(manager.isTransactionXACachingThread());
        assertTrue(manager.isTransactionXACachingTransaction());
        assertFalse(manager.isTransactionLocal());
        SinglePool pool = manager.getPoolSingle();
        assertNull(pool.getMinSize());
        assertNull(pool.getMaxSize());
        assertNull(pool.getIdleTimeoutMinutes());
        assertNull(pool.getBlockingTimeoutMillis());
        assertTrue(pool.isMatchOne());
        assertFalse(pool.isMatchAll());
        assertFalse(pool.isSelectOneAssumeMatch());
        pool.setMinSize(new Integer(2));
        pool.setMaxSize(new Integer(30));
        pool.setBlockingTimeoutMillis(new Integer(5000));
        DDBean[] ddBeans = connector.getDDBean().getChildBean(connector.getXpaths()[0]);
        assertNotNull(ddBeans);
        assertEquals(2, ddBeans.length);
        DConfigBean[] adminDCBs = new DConfigBean[ddBeans.length];
        for (int i = 0; i < adminDCBs.length; i++) {
            adminDCBs[i] = connector.getDConfigBean(ddBeans[i]);
            AdminObjectDCB dcb = (AdminObjectDCB) adminDCBs[i];
            if(dcb.getAdminObjectClass().indexOf("Queue") > -1) {
                assertEquals(0, dcb.getAdminObjectInstance().length);
                AdminObjectInstance one = new AdminObjectInstance();
                AdminObjectInstance two = new AdminObjectInstance();
                dcb.setAdminObjectInstance(new AdminObjectInstance[]{one, two});
                one.setMessageDestinationName("TestQueue1");
                one.getConfigPropertySetting()[0].setValue("PhysicalName1");
                two.setMessageDestinationName("TestQueue2");
                two.getConfigPropertySetting()[0].setValue("PhysicalName2");
            } else if(dcb.getAdminObjectClass().indexOf("Topic") > -1) {
                assertEquals(0, dcb.getAdminObjectInstance().length);
            } else fail("Unknown AdminobjectDCB admin object class '"+dcb.getAdminObjectClass()+"'");
        }


        //todo: Look at the XmlBeans tree and make sure the right stuff is in there
        //dcbRoot.toXML(System.out);
    }

    public void testLoadJMSResources() throws Exception {
        InputStream in = classLoader.getResource("jms-plan.xml").openStream();
        // Create and test the DDBeanRoot
        URL resource = classLoader.getResource("jms.rar");
        assertNotNull(resource);
        Bundle bundle = new MockBundle(classLoader, "", 0L);
        ConnectorDeployable deployable = new ConnectorDeployable(bundle);
        assertEquals(ModuleType.RAR, deployable.getType());
        Set entrySet = new HashSet(Collections.list(deployable.entries()));
        assertTrue(entrySet.contains("META-INF/ra.xml"));
        assertTrue(entrySet.contains("activemq-ra-3.2.1.jar"));
        DDBeanRoot root = deployable.getDDBeanRoot();
        assertNotNull(root);
        assertEquals(ModuleType.RAR, root.getType());
        assertEquals(deployable, root.getDeployableObject());
        // Create the DConfigBeanRoot
        Connector15DCBRoot dcbRoot = new Connector15DCBRoot(root);
        RARConfiguration configuration = new RARConfiguration(deployable, dcbRoot);
        configuration.restore(in);
        // Try the /connector element
        ConnectorDCB connector = (ConnectorDCB) dcbRoot.getDConfigBean(root.getChildBean(dcbRoot.getXpaths()[0])[0]);
        assertNotNull(connector);
        // Try the /connector/resourceadapter element
        assertNotNull(connector.getResourceAdapter());
        assertEquals(1, connector.getResourceAdapter().length);
        ResourceAdapter adapter = connector.getResourceAdapter()[0];
        assertNotNull(adapter);
        // Try the /connector/resourceadapter/resourceadapter-instance element
        ResourceAdapterInstance raInstance = adapter.getResourceAdapterInstance();
        assertNotNull(raInstance);
        assertEquals("Test ActiveMQ RA", raInstance.getResourceAdapterName());
        assertEquals(7, raInstance.getConfigPropertySetting().length);
        int found = 0;
        Set seen = new HashSet();
        for (int i = 0; i < 7; i++) {
            ConfigPropertySetting setting = raInstance.getConfigPropertySetting(i);
            if(seen.contains(setting.getName())) {
                fail("Duplicate for config property setting '"+setting.getName()+"'");
            }
            seen.add(setting.getName());
            if(setting.getName().equals("ServerUrl")) {
                assertEquals("tcp://localhost:61616", setting.getValue());
                ++found;
            } else if(setting.getName().equals("UserName")) {
                assertEquals("geronimo-user", setting.getValue());
                ++found;
            } else if(setting.getName().equals("Password")) {
                assertEquals("geronimo-pw", setting.getValue());
                ++found;
            }
        }
        assertEquals(3, found);
        //todo: check the work manager
        // Try the /connector/resourceadapter/outbound-resourceadapter/connection-definition element
        assertNotNull(adapter.getConnectionDefinition());
        assertEquals(1, adapter.getConnectionDefinition().length);
        ConnectionDefinition definition = adapter.getConnectionDefinition(0);
        // Try the .../connection-definition/connectiondefinition-instance elements
        assertNotNull(definition.getConnectionInstances());
        assertEquals(1, definition.getConnectionInstances().length);
        ConnectionDefinitionInstance instance = definition.getConnectionInstances()[0];
        // Try the .../connection-definition/connectiondefinition-instance/config-property-setting elements
        assertNotNull(instance.getConfigPropertySetting());
        assertEquals(0, instance.getConfigPropertySetting().length);
        // Try the /connector/adminobject element
        DDBean[] ddBeans = connector.getDDBean().getChildBean(connector.getXpaths()[0]);
        assertNotNull(ddBeans);
        assertEquals(2, ddBeans.length);
        DConfigBean[] adminDCBs = new DConfigBean[ddBeans.length];
        for (int i = 0; i < adminDCBs.length; i++) {
            adminDCBs[i] = connector.getDConfigBean(ddBeans[i]);
            AdminObjectDCB dcb = (AdminObjectDCB) adminDCBs[i];
            if(dcb.getAdminObjectClass().indexOf("Queue") > -1) {
                assertEquals(3, dcb.getAdminObjectInstance().length);
                assertEquals("TestQueue1Name", dcb.getAdminObjectInstance()[0].getMessageDestinationName());
                assertEquals("TestQueue2Name", dcb.getAdminObjectInstance()[1].getMessageDestinationName());
                assertEquals("TestQueue3Name", dcb.getAdminObjectInstance()[2].getMessageDestinationName());
            } else if(dcb.getAdminObjectClass().indexOf("Topic") > -1) {
                assertEquals(1, dcb.getAdminObjectInstance().length);
                assertEquals("TestTopic1Name", dcb.getAdminObjectInstance()[0].getMessageDestinationName());
            } else fail("Unknown AdminobjectDCB admin object class '"+dcb.getAdminObjectClass()+"'");
        }
        // Make sure that saving clears out the null/default values
        ByteArrayOutputStream pout = new ByteArrayOutputStream();
        dcbRoot.toXML(pout);
        pout.close();
        ByteArrayInputStream pin = new ByteArrayInputStream(pout.toByteArray());
        GerConnectorDocument doc = GerConnectorDocument.Factory.parse(pin);
        pin.close();
        GerConfigPropertySettingType[] settings = doc.getConnector().getResourceadapterArray(0).
                getResourceadapterInstance().getConfigPropertySettingArray();
        assertEquals(2, settings.length);
        seen.clear();
        for (int i = 0; i < settings.length; i++) {
            GerConfigPropertySettingType setting = settings[i];
            if(seen.contains(setting.getName())) {
                fail("Duplicate for config property setting '"+setting.getName()+"'");
            }
            seen.add(setting.getName());
            if(setting.getName().equals("UserName")) {
                assertEquals("geronimo-user", setting.getStringValue());
            } else if(setting.getName().equals("Password")) {
                assertEquals("geronimo-pw", setting.getStringValue());
            } else fail("Unknown connection setting '"+setting.getName()+"'");
        }
    }

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
    }
}
