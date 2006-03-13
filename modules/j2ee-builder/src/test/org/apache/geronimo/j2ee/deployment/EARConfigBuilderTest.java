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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import javax.xml.namespace.QName;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;

/**
 * @version $Rev: 385487 $ $Date$
 */
public class EARConfigBuilderTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    private static String WEB_NAMESPACE="foo";
    private static JarFile earFile;
    private static MockConfigStore configStore = new MockConfigStore();
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

    private static final AbstractName rootConfig = Naming.createRootName(new Artifact("test", "stuff", "", "car"), "test", "test") ;
    private static final AbstractName transactionManagerObjectName = Naming.createChildName(rootConfig, "TransactionManager", "TransactionManager");
    private static final AbstractName connectionTrackerObjectName = Naming.createChildName(rootConfig, "ConnectionTracker", "ConnectionTracker");
    private static final AbstractName transactionalTimerObjectName = Naming.createChildName(rootConfig, "ThreadPooledTimer", "TransactionalThreaPooledTimer");
    private static final AbstractName nonTransactionalTimerObjectName = Naming.createChildName(rootConfig, "ThreadPooledTimer", "NonTransactionalThreaPooledTimer");

    private static final AbstractName earName = Naming.createRootName(new Artifact("test", "test-ear", "", "ear"), "test", NameFactory.J2EE_APPLICATION) ;
    private static final AbstractName ejbModuleName = Naming.createChildName(earName, NameFactory.EJB_MODULE, "ejb-jar");
    private static final AbstractName webModuleName = Naming.createChildName(earName, NameFactory.WEB_MODULE, "war");
    private static final AbstractName raModuleName = Naming.createChildName(earName, NameFactory.RESOURCE_ADAPTER_MODULE, "rar");

    private Environment defaultParentId;
    private static String contextRoot = "test";
    private static final Map portMap = null;

    protected void setUp() throws Exception {
        super.setUp();
        defaultParentId = new Environment();
        defaultParentId.addDependency(new Artifact("geronimo", "test", "1", "car"), ImportType.ALL);
        defaultParentId.getProperties().put(NameFactory.JSR77_BASE_NAME_PROPERTY, "geronimo.test:J2EEServer=geronimo");
    }

    public static Test suite() throws Exception {
        TestSuite inner = new TestSuite(EARConfigBuilderTest.class);
        TestSetup setup14 = new TestSetup(inner) {
            protected void setUp() throws Exception {
                earFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear14/test-ear.ear"));
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war/", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war/", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
                ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "test-ejb-jar.jar/", null, null, null);
                webConfigBuilder.contextRoot = contextRoot;
                webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "test-war.war/", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
                connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "test-rar.rar", null, null, null);
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
//        Kernel kernel = KernelFactory.newInstance().createKernel("foo");
//        kernel.boot();
//
//        GBeanData store = new GBeanData(Naming.createChildName(rootConfig, "ConfigStore", "ConfigStore"), MockConfigStore.GBEAN_INFO);
//        kernel.loadGBean(store, this.getClass().getClassLoader());
//        kernel.startGBean(store.getAbstractName());
//
//        GBeanData configurationManagerData = new GBeanData(Naming.createChildName(rootConfig, "ConfigurationManager", "ConfigurationManager"), ConfigurationManagerImpl.GBEAN_INFO);
//        configurationManagerData.setReferencePattern("Stores", new AbstractNameQuery(store.getAbstractName()));
//        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
//        kernel.startGBean(configurationManagerData.getAbstractName());
//
//        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
//                new AbstractNameQuery(transactionManagerObjectName),
//                new AbstractNameQuery(connectionTrackerObjectName),
//                new AbstractNameQuery(transactionalTimerObjectName),
//                new AbstractNameQuery(nonTransactionalTimerObjectName),
//                null,
//                null,
//                ejbConfigBuilder,
//                ejbConfigBuilder,
//                webConfigBuilder,
//                connectorConfigBuilder,
//                resourceReferenceBuilder,
//                appClientConfigBuilder,
//                serviceReferenceBuilder,
//                kernel);
//
//        ConfigurationData configurationData = null;
//        try {
//            Object plan = configBuilder.getDeploymentPlan(null, earFile);
//            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
//        } finally {
//            if (configurationData != null) {
//                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
//            }
//            kernel.shutdown();
//        }
    }

    public void testBadEJBJARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-ejb-jar.xml"), earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadWARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-war.xml"), earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadRARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-rar.xml"), earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadCARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-car.xml"), earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if(e.getCause() instanceof IOException) {
                fail("Should not be complaining about bad vendor DD for invalid module entry");
            }
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testNoEJBDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                null,
                null,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);


        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testNoWARDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                null,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testNoConnectorDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                new AbstractNameQuery(transactionManagerObjectName),
                new AbstractNameQuery(connectionTrackerObjectName),
                new AbstractNameQuery(transactionalTimerObjectName),
                new AbstractNameQuery(nonTransactionalTimerObjectName),
                null,
                null,
                ejbConfigBuilder,
                null,
                webConfigBuilder,
                null,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                null);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            configurationData = configBuilder.buildConfiguration(plan, earFile, configStore);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    private static void close(Module module) {
        if (module != null) {
            module.close();
        }
    }
    public static class MockConfigStore implements ConfigurationStore {
        private final Map locations = new HashMap();

        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        }

        public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
        }

        public GBeanData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            AbstractName configurationName = Configuration.getConfigurationAbstractName(configId);
            GBeanData configData = new GBeanData(configurationName, Configuration.GBEAN_INFO);
            Environment environment = new Environment();
            environment.setConfigId(configId);
            environment.getProperties().put(NameFactory.JSR77_BASE_NAME_PROPERTY, "geronimo.test:J2EEServer=geronimo");
            configData.setAttribute("environment", environment);
            configData.setAttribute("gBeanState", NO_OBJECTS_OS);
            configData.setAttribute("type", ConfigurationModuleType.CAR);

            return configData;
        }

        public boolean containsConfiguration(Artifact configID) {
            return true;
        }

        public String getObjectName() {
            return null;
        }

        public List listConfigurations() {
            return null;
        }

        public File createNewConfigurationDir(Artifact configId) {
            try {
                File file = DeploymentUtil.createTempDir();
                locations.put(configId, file);
                return file;
            } catch (IOException e) {
                return null;
            }
        }

        public URL resolve(Artifact configId, URI uri) throws NoSuchConfigException, MalformedURLException {
            File file = (File) locations.get(configId);
            if (file == null) {
                throw new NoSuchConfigException("nothing for configid " + configId);
            }
            return new URL(file.toURL(), uri.toString());
        }

        public final static GBeanInfo GBEAN_INFO;

        private static final byte[] NO_OBJECTS_OS;

        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, NameFactory.CONFIGURATION_STORE);
            infoBuilder.addInterface(ConfigurationStore.class);
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
