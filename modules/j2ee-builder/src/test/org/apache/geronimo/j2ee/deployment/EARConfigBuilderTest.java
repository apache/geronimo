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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
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
    private static ResourceReferenceBuilder resourceReferenceBuilder = connectorConfigBuilder;
    private static ModuleBuilder appClientConfigBuilder = null;
    private static ServiceReferenceBuilder serviceReferenceBuilder = new ServiceReferenceBuilder() {

        //it could return a Service or a Reference, we don't care
        public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) {
            return null;
        }
    };

    private static final String j2eeServerName = "someDomain";
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ConnectionTracker");
    private static final ObjectName transactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    private static final ObjectName nonTransactionalTimerObjectName = JMXUtil.getObjectName(j2eeServerName + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");
    private URI[] defaultParentId;
    private static String contextRoot = "test";
    private static final Map portMap = null;

    protected void setUp() throws Exception {
        defaultParentId = new URI[] {new URI("org/apache/geronimo/Server")};
    }

    public static Test suite() throws Exception {
        TestSuite inner = new TestSuite(EARConfigBuilderTest.class);
        TestSetup setup14 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear14/test-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setupNaked14 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear14/test-naked-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setup13 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear13/test-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setupNaked13 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear13/test-naked-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setupUnpacked = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-unpacked-ear/full/"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war/", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setupUnpackedNaked = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-unpacked-ear/naked/"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setupUnpackedAltDD = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-unpacked-ear/alt-dd/"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war/", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
            }
        };
        TestSetup setupPackedAltDD = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-unpacked-ear/alt-dd.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, null, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, null, null, null, "test-war.war/", null, null, null, contextRoot, portMap);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, null, null, null, "test-rar.rar", null, null, null);
            }

            protected void tearDown() {
                DeploymentUtil.close(earFile);
                close(ejbConfigBuilder.ejbModule);
                close(webConfigBuilder.webModule);
                close(connectorConfigBuilder.connectorModule);
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
        Kernel kernel = KernelFactory.newInstance().createKernel("foo");
        kernel.boot();

        GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
        kernel.loadGBean(store, this.getClass().getClassLoader());
        kernel.startGBean(store.getName());

        ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
        kernel.startGBean(configurationManagerName);

        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, kernel);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
            kernel.shutdown();
        }
    }

    public void testBadEJBJARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-ejb-jar.xml"), earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testBadWARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-war.xml"), earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testBadRARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-rar.xml"), earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testBadCARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, ejbConfigBuilder, webConfigBuilder, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-car.xml"), earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testNoEJBDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, null, null, webConfigBuilder, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testNoWARDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, null, null, connectorConfigBuilder, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testNoConnectorDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, transactionManagerObjectName, connectionTrackerObjectName, transactionalTimerObjectName, nonTransactionalTimerObjectName, null, null, ejbConfigBuilder, null, webConfigBuilder, null, resourceReferenceBuilder, appClientConfigBuilder, serviceReferenceBuilder, null);

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configBuilder.buildConfiguration(plan, earFile, tempDir);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    private static void close(Module module) {
        if (module != null) {
            module.close();
        }
    }

    public static class MockConfigStore implements ConfigurationStore {
        private final Kernel kernel;

        public MockConfigStore(Kernel kernel) {
            this.kernel = kernel;
        }

        public URI install(URL source) throws IOException, InvalidConfigException {
            return null;
        }

        public void install(ConfigurationData configurationData, File source) throws IOException, InvalidConfigException {
        }

        public void uninstall(URI configID) throws NoSuchConfigException, IOException {
        }

        public ObjectName loadConfiguration(URI configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            ObjectName configurationObjectName = null;
            try {
                configurationObjectName = Configuration.getConfigurationObjectName(configId);
            } catch (MalformedObjectNameException e) {
                throw new InvalidConfigException(e);
            }
            GBeanData configData = new GBeanData(configurationObjectName, Configuration.GBEAN_INFO);
            configData.setAttribute("id", configId);
            configData.setAttribute("domain", "test");
            configData.setAttribute("server", "bar");
            configData.setAttribute("gBeanState", NO_OBJECTS_OS);

            try {
                kernel.loadGBean(configData, Configuration.class.getClassLoader());
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to register configuration", e);
            }

            return configurationObjectName;
        }

        public boolean containsConfiguration(URI configID) {
            return true;
        }

        public void updateConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, Exception {

        }

        public String getObjectName() {
            return null;
        }

        public List listConfigurations() {
            return null;
        }

        public File createNewConfigurationDir() {
            return null;
        }

        public final static GBeanInfo GBEAN_INFO;

        private static final byte[] NO_OBJECTS_OS;

        static {
            GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(MockConfigStore.class, NameFactory.CONFIGURATION_STORE);
            infoBuilder.addInterface(ConfigurationStore.class);
            infoBuilder.addAttribute("kernel", Kernel.class, false);
            infoBuilder.setConstructor(new String[] {"kernel"});
            GBEAN_INFO = infoBuilder.getBeanInfo();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.flush();
                NO_OBJECTS_OS = baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
