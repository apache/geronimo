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
 * @version $Revision: 1.8 $ $Date: 2004/08/07 11:22:12 $
 */
public class EARConfigBuilderTest extends TestCase {

    private static String EAR_BASE_DIR;
    private static String EAR_PATH;
    private static MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
    private static MockWARConfigBuilder webConfigBuilder = new MockWARConfigBuilder();
    private static MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
    
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
                EAR_BASE_DIR = "target/test-ear14";
                EAR_PATH = "test-ear.ear";
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar.jar", URI.create("test-ejb-jar.jar"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war.war", URI.create("test-war.war"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        TestSetup setupNaked14 = new TestSetup(inner) {
            protected void setUp() {
                EAR_BASE_DIR = "target/test-ear14";
                EAR_PATH = "test-naked-ear.ear";
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar.jar", URI.create("test-ejb-jar.jar"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war.war", URI.create("test-war.war"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        TestSetup setup13 = new TestSetup(inner) {
            protected void setUp() {
                EAR_BASE_DIR = "target/test-ear13";
                EAR_PATH = "test-ear.ear";
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar.jar", URI.create("test-ejb-jar.jar"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war.war", URI.create("test-war.war"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        TestSetup setupNaked13 = new TestSetup(inner) {
            protected void setUp() {
                EAR_BASE_DIR = "target/test-ear13";
                EAR_PATH = "test-naked-ear.ear";
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar.jar", URI.create("test-ejb-jar.jar"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war.war", URI.create("test-war.war"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        TestSetup setupUnpacked = new TestSetup(inner) {
            protected void setUp() {
                EAR_BASE_DIR = "target/test-unpacked-ear";
                EAR_PATH = "full/";
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar/", URI.create("test-ejb-jar/"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war/", URI.create("test-war/"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        TestSetup setupUnpackedNaked = new TestSetup(inner) {
            protected void setUp() {
                EAR_BASE_DIR = "target/test-unpacked-ear";
                EAR_PATH = "naked/";
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar/", URI.create("test-ejb-jar/"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war/", URI.create("test-war/"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        
        TestSuite suite = new TestSuite();
        suite.addTest(setup14);
        suite.addTest(setupNaked14);
        suite.addTest(setup13);
        suite.addTest(setupNaked13);
        suite.addTest(setupUnpacked);
        suite.addTest(setupUnpackedNaked);
        return suite;
    }

    public void testBuildConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/" + EAR_PATH);

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            XmlObject plan = configBuilder.getDeploymentPlan(earFile.toURL());
            configBuilder.buildConfiguration(carFile, null, earFile, plan);
        } finally {
            carFile.delete();
        }
    }

    public void testNoEJBDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, null, webConfigBuilder, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/" + EAR_PATH);

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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, null, connectorConfigBuilder, null);
        File earFile = new File(EAR_BASE_DIR + "/" + EAR_PATH);

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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, null, null);
        File earFile = new File(EAR_BASE_DIR + "/" + EAR_PATH);

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
