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

package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.net.URI;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 1.5 $ $Date: 2004/07/23 06:01:50 $
 */
public class EARConfigBuilderTest extends TestCase {
    private static final String j2eeServerName = "someDomain";
    private static final ObjectName j2eeServer = JMXUtil.getObjectName(j2eeServerName + ":j2eeType=J2EEServer,name=J2EEServerName");
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ConnectionTracker");
    private static final ObjectName transactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    private static final ObjectName nonTransactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");

    public void testBuildConfiguration() throws Exception {
        MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
        MockWARConfigBuilder webConfigBuilder = new MockWARConfigBuilder();
        MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File("target/test-ear.ear");

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar.jar", URI.create("test-ejb-jar.jar"));
            webConfigBuilder.contextRoot = "test";
            webConfigBuilder.webModule = new WebModule("test-war.war", URI.create("test-war.war"), "test");
            connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));

            XmlObject plan = configBuilder.getDeploymentPlan(earFile.toURL());
            configBuilder.buildConfiguration(carFile, null, earFile, plan);
        } finally {
            carFile.delete();
        }
    }

    public void testNakedEarBuildConfiguration() throws Exception {
        MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
        MockWARConfigBuilder webConfigBuilder = new MockWARConfigBuilder();
        MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File("target/test-naked-ear.ear");

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar.jar", URI.create("test-ejb-jar.jar"));
            webConfigBuilder.contextRoot = "test";
            webConfigBuilder.webModule = new WebModule("test-war.war", URI.create("test-war.war"), "test");
            connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));

            XmlObject plan = configBuilder.getDeploymentPlan(earFile.toURL());
            configBuilder.buildConfiguration(carFile, null, earFile, plan);
        } finally {
            carFile.delete();
        }
    }

    public void testNoEJBDeployer() throws Exception {
        MockWARConfigBuilder warConfigBuilder = new MockWARConfigBuilder();
        MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, warConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File("target/test-ear.ear");

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            XmlObject plan = configBuilder.getDeploymentPlan(earFile.toURL());
            configBuilder.buildConfiguration(carFile, null, earFile, plan);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            carFile.delete();
        }
    }

    public void testNoWARDeployer() throws Exception {
        MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
        MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, connectorConfigBuilder, null);
        File earFile = new File("target/test-ear.ear");

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            XmlObject plan = configBuilder.getDeploymentPlan(earFile.toURL());
            configBuilder.buildConfiguration(carFile, null, earFile, plan);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            carFile.delete();
        }
    }

    public void testNoConnectorDeployer() throws Exception {
        MockWARConfigBuilder warConfigBuilder = new MockWARConfigBuilder();
        MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, warConfigBuilder, null, null);
        File earFile = new File("target/test-ear.ear");

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            XmlObject plan = configBuilder.getDeploymentPlan(earFile.toURL());
            configBuilder.buildConfiguration(carFile, null, earFile, plan);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            carFile.delete();
        }
    }

}
