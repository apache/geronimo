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
import java.net.URL;
import javax.management.ObjectName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EARConfigBuilderTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

//    private static String EAR_BASE_DIR;
//    private static String EAR_PATH;
    private static File earFile;
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
                earFile = new File(basedir,  "target/test-ear14/test-ear.ear");
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
                earFile = new File(basedir,  "target/test-ear14/test-naked-ear.ear");
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
                earFile = new File(basedir,  "target/test-ear13/test-ear.ear");
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
                earFile = new File(basedir, "target/test-ear13/test-naked-ear.ear");
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
                earFile = new File(basedir,  "target/test-unpacked-ear/full/");
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
                earFile = new File(basedir,  "target/test-unpacked-ear/naked/");
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar/", URI.create("test-ejb-jar/"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war/", URI.create("test-war/"), "test");
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
            }

            protected void tearDown() {
            }
        };
        TestSetup setupUnpackedAltDD = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = new File(basedir,  "target/test-unpacked-ear/alt-dd/");
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar/", URI.create("test-ejb-jar/"));
                ejbConfigBuilder.ejbModule.setAltSpecDD(new File(earFile, "alt-ejb-jar.xml").toURL());
                ejbConfigBuilder.ejbModule.setAltVendorDD(new File(earFile, "alt-ger-ejb-jar.xml").toURL());
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war/", URI.create("test-war/"), "test");
                webConfigBuilder.webModule.setAltSpecDD(new File(earFile, "alt-web.xml").toURL());
                webConfigBuilder.webModule.setAltVendorDD(new File(earFile, "alt-ger-war.xml").toURL());
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
                connectorConfigBuilder.connectorModule.setAltSpecDD(new File(earFile, "alt-ra.xml").toURL());
                connectorConfigBuilder.connectorModule.setAltVendorDD(new File(earFile, "alt-ger-ra.xml").toURL());
            }

            protected void tearDown() {
            }
        };
        TestSetup setupPackedAltDD = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = new File(basedir,  "target/test-unpacked-ear/alt-dd.ear");
                ejbConfigBuilder.ejbModule = new EJBModule("test-ejb-jar/", URI.create("test-ejb-jar/"));
                String baseURI = "jar:" + earFile.toURL() + "!/";
                ejbConfigBuilder.ejbModule.setAltSpecDD(new URL(baseURI + "alt-ejb-jar.xml"));
                ejbConfigBuilder.ejbModule.setAltVendorDD(new URL(baseURI + "alt-ger-ejb-jar.xml"));
                webConfigBuilder.contextRoot = "test";
                webConfigBuilder.webModule = new WebModule("test-war/", URI.create("test-war/"), "test");
                webConfigBuilder.webModule.setAltSpecDD(new URL(baseURI + "alt-web.xml"));
                webConfigBuilder.webModule.setAltVendorDD(new URL(baseURI + "alt-ger-war.xml"));
                connectorConfigBuilder.connectorModule = new ConnectorModule("test-rar.rar", URI.create("test-rar.rar"));
                connectorConfigBuilder.connectorModule.setAltSpecDD(new URL(baseURI + "alt-ra.xml"));
                connectorConfigBuilder.connectorModule.setAltVendorDD(new URL(baseURI + "alt-ger-ra.xml"));
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
        EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, ejbConfigBuilder, null, webConfigBuilder, connectorConfigBuilder, null);

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
