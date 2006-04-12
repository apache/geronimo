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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
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
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;

/**
 * @version $Rev:386276 $ $Date$
 */
public class EARConfigBuilderTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    private static String WEB_NAMESPACE = "foo";
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

    private static final Naming naming = new Jsr77Naming();

    private static final AbstractName rootConfig = naming.createRootName(new Artifact("test", "stuff", "", "car"), "test", "test") ;
    private static final AbstractName transactionManagerObjectName = naming.createChildName(rootConfig, "TransactionManager", "TransactionManager");
    private static final AbstractName connectionTrackerObjectName = naming.createChildName(rootConfig, "ConnectionTracker", "ConnectionTracker");
    private static final AbstractName transactionalTimerObjectName = naming.createChildName(rootConfig, "TransactionalThreaPooledTimer", "ThreadPooledTimer");
    private static final AbstractName nonTransactionalTimerObjectName = naming.createChildName(rootConfig, "NonTransactionalThreaPooledTimer", "ThreadPooledTimer");
    private static final AbstractName serverName = naming.createChildName(rootConfig, "J2EEServer", "Server");

    private static final AbstractName earName = naming.createRootName(new Artifact("test", "test-ear", "", "ear"), "test", NameFactory.J2EE_APPLICATION) ;
    private static final AbstractName ejbModuleName = naming.createChildName(earName, "ejb-jar", NameFactory.EJB_MODULE);
    private static final AbstractName webModuleName = naming.createChildName(earName, "war", NameFactory.WEB_MODULE);
    private static final AbstractName raModuleName = naming.createChildName(earName, "rar", NameFactory.RESOURCE_ADAPTER_MODULE);

    private Environment defaultParentId;
    private static String contextRoot = "test";
    private static final Map portMap = null;
    private final AbstractNameQuery transactionContextManagerAbstractNameQuery = new AbstractNameQuery(transactionManagerObjectName, null);
    private final AbstractNameQuery connectionTrackerAbstractNameQuery = new AbstractNameQuery(connectionTrackerObjectName, null);
    private final AbstractNameQuery transactionalTimerAbstractNameQuery = new AbstractNameQuery(transactionalTimerObjectName, null);
    private final AbstractNameQuery nonTransactionalTimerAbstractNameQuery = new AbstractNameQuery(nonTransactionalTimerObjectName, null);
    private final AbstractNameQuery corbaGBeanAbstractNameQuery = new AbstractNameQuery(serverName, null);

    protected void setUp() throws Exception {
        super.setUp();
        defaultParentId = new Environment();
        defaultParentId.addDependency(new Artifact("geronimo", "test", "1", "car"), ImportType.ALL);
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
        ConfigurationData configurationData = null;
        try {
            EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                    transactionContextManagerAbstractNameQuery,
                    connectionTrackerAbstractNameQuery,
                    transactionalTimerAbstractNameQuery,
                    nonTransactionalTimerAbstractNameQuery,
                    corbaGBeanAbstractNameQuery,
                    null,
                    null,
                    ejbConfigBuilder,
                    ejbConfigBuilder,
                    webConfigBuilder,
                    connectorConfigBuilder,
                    resourceReferenceBuilder,
                    appClientConfigBuilder,
                    serviceReferenceBuilder,
                    naming);

            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
        } finally {
            if (configurationData != null) {
                DeploymentUtil.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadEJBJARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-ejb-jar.xml"), earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if (e.getCause() instanceof IOException) {
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
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-war.xml"), earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if (e.getCause() instanceof IOException) {
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
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-rar.xml"), earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if (e.getCause() instanceof IOException) {
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
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(new File(basedir, "target/plans/test-bad-car.xml"), earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            if (e.getCause() instanceof IOException) {
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
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                null,
                null,
                webConfigBuilder,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);


        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
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
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                ejbConfigBuilder,
                ejbConfigBuilder,
                null,
                connectorConfigBuilder,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
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
                transactionContextManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                transactionalTimerAbstractNameQuery,
                nonTransactionalTimerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                null,
                ejbConfigBuilder,
                null,
                webConfigBuilder,
                null,
                resourceReferenceBuilder,
                appClientConfigBuilder,
                serviceReferenceBuilder,
                naming);

        ConfigurationData configurationData = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile);
            List configurations = configBuilder.buildConfiguration(false, plan, earFile, Collections.singleton(configStore), configStore);
            configurationData = (ConfigurationData) configurations.get(0);
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

        public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            ConfigurationData configurationData = new ConfigurationData(configId, naming);
            configurationData.setConfigurationStore(this);
            return configurationData;
        }

        public boolean containsConfiguration(Artifact configID) {
            return true;
        }

        public String getObjectName() {
            return null;
        }

        public AbstractName getAbstractName() {
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

        public URL resolve(Artifact configId, String moduleName, URI uri) throws NoSuchConfigException, MalformedURLException {
            File file = (File) locations.get(configId);
            if (file == null) {
                throw new NoSuchConfigException("nothing for configid " + configId);
            }
            return new URL(file.toURL(), uri.toString());
        }
    }
}
