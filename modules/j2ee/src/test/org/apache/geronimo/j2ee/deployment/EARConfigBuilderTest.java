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
import java.util.jar.JarFile;
import javax.management.ObjectName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.JarUtil;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * @version $Rev$ $Date$
 */
public class EARConfigBuilderTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    private static JarFile earFile;
    private static MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();
    private static MockWARConfigBuilder webConfigBuilder = new MockWARConfigBuilder();
    private static MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();
    private static ModuleBuilder appClientConfigBuilder;

    private static final String j2eeServerName = "someDomain";
    private static final ObjectName j2eeServer = JMXUtil.getObjectName(j2eeServerName + ":j2eeType=J2EEServer,name=J2EEServerName");
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ConnectionTracker");
    private static final ObjectName transactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    private static final ObjectName nonTransactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");

    public static Test suite() throws Exception {
        TestSuite inner = new TestSuite(EARConfigBuilderTest.class);
        TestSetup setup14 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-ear14/test-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setupNaked14 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-ear14/test-naked-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setup13 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-ear13/test-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setupNaked13 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir, "target/test-ear13/test-naked-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setupUnpacked = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-unpacked-ear/full/"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war/", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setupUnpackedNaked = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-unpacked-ear/naked/"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setupUnpackedAltDD = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-unpacked-ear/alt-dd/"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war/", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
            }
        };
        TestSetup setupPackedAltDD = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = JarUtil.createJarFile(new File(basedir,  "target/test-unpacked-ear/alt-dd.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war/", null, null, null);
                webConfigBuilder.webModule.setContextRoot("test");
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
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
        suite.addTest(setupUnpackedAltDD);
        suite.addTest(setupPackedAltDD);
        return suite;
    }

    public void testBuildConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, connectorConfigBuilder, appClientConfigBuilder, null);

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(carFile, null, plan, earFile);
        } finally {
            carFile.delete();
        }
    }

    public void testNoEJBDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, null, webConfigBuilder, connectorConfigBuilder, appClientConfigBuilder, null);

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(carFile, null, plan, earFile);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            carFile.delete();
        }
    }

    public void testNoWARDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, null, connectorConfigBuilder, appClientConfigBuilder, null);

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(carFile, null, plan, earFile);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            carFile.delete();
        }
    }

    public void testNoConnectorDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, null, appClientConfigBuilder, null);

        File carFile = File.createTempFile("EARTest", ".car");
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(carFile, null, plan, earFile);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            carFile.delete();
        }
    }

}
