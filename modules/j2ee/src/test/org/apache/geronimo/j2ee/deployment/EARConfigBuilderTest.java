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
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 1.7 $ $Date: 2004/08/06 22:44:37 $
 */
public class EARConfigBuilderTest extends TestCase {

    private static String EAR_BASE_DIR;

    private static final String j2eeServerName = "someDomain";
    private static final ObjectName j2eeServer = JMXUtil.getObjectName(j2eeServerName + ":j2eeType=J2EEServer,name=J2EEServerName");
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ConnectionTracker");
    private static final ObjectName transactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    private static final ObjectName nonTransactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");

    public static Test suite() throws Exception {
        TestSuite inner = new TestSuite(EARConfigBuilderTest.class);
        TestSetup setup14 = new TestSetup(inner) {
            protected void setUp() {
                EARConfigBuilderTest.EAR_BASE_DIR = "target/test-ear14";
            }

            protected void tearDown() {
            }
        };
        TestSetup setup13 = new TestSetup(inner) {
            protected void setUp() {
                EARConfigBuilderTest.EAR_BASE_DIR = "target/test-ear13";
            }

            protected void tearDown() {
            }
        };
        TestSuite suite = new TestSuite();
        suite.addTest(setup14);
        suite.addTest(setup13);
        return suite;
    }

    public void testBuildConfiguration() throws Exception {
        MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
        MockWARConfigBuilder webConfigBuilder = new MockWARConfigBuilder();
        MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/test-ear.ear");

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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/test-naked-ear.ear");

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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, null, warConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/test-ear.ear");

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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, null, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/test-ear.ear");

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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, warConfigBuilder, null, null);
        File earFile = new File(EAR_BASE_DIR + "/test-ear.ear");

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
