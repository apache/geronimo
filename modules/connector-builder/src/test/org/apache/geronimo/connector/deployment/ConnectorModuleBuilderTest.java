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

package org.apache.geronimo.connector.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorModuleBuilderTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private boolean defaultXATransactionCaching = true;
    private boolean defaultXAThreadCaching = false;
    private int defaultMaxSize = 10;
    private int defaultMinSize = 0;
    private int defaultBlockingTimeoutMilliseconds = 5000;
    private int defaultidleTimeoutMinutes = 15;
    private URI[] defaultParentId;
    private Repository repository = new Repository() {

                public boolean hasURI(URI uri) {
                    return false;
                }

                public URL getURL(URI uri) {
                    return null;
                }
            };

    private EJBReferenceBuilder ejbReferenceBuilder = new EJBReferenceBuilder() {

        public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) {
            return null;
        }

        public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) {
            return null;
        }

        public Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) {
            return null;
        }

        public Object createHandleDelegateReference() {
            return null;
        }

        public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) {
            return null;
        }

        public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) {
            return null;
        }
    };

    private ResourceReferenceBuilder resourceReferenceBuilder = new ResourceReferenceBuilder() {

        public Reference createResourceRef(String containerId, Class iface)  {
            return null;
        }

        public Reference createAdminObjectRef(String containerId, Class iface)  {
            return null;
        }

        public ObjectName locateResourceName(ObjectName query)  {
            return null;
        }

        public GBeanData locateActivationSpecInfo(GBeanData resourceAdapterModuleData, String messageListenerInterface)  {
            return null;
        }

        public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData)  {
            return null;
        }

        public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName)  {
            return null;
        }

        public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) {
            return null;
        }
    };
    private ServiceReferenceBuilder serviceReferenceBuilder = new ServiceReferenceBuilder() {
                                        //it could return a Service or a Reference, we don't care
                                        public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) {
                                            return null;
                                        }
                                    };
    private ObjectName configurationManagerName;


    public void testBuildEar() throws Exception {
        ObjectName connectionTrackerName = new ObjectName("geronimo.connector:service=ConnectionTracker");
        JarFile rarFile = null;
        Kernel kernel = KernelFactory.newInstance().createKernel("foo");
        try {
            kernel.boot();

            GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
            kernel.loadGBean(store, this.getClass().getClassLoader());
            kernel.startGBean(store.getName());

            GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
            configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
            kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
            kernel.startGBean(configurationManagerName);

            rarFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear-noger.ear"));
            EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, null, connectionTrackerName, null, null, null, null, null, ejbReferenceBuilder, null, new ConnectorModuleBuilder(defaultParentId, defaultMaxSize, defaultMinSize, defaultBlockingTimeoutMilliseconds, defaultidleTimeoutMinutes, defaultXATransactionCaching, defaultXAThreadCaching, repository, kernel), resourceReferenceBuilder, null, serviceReferenceBuilder, kernel);
            File tempDir = null;
            try {
                tempDir = DeploymentUtil.createTempDir();
                File planFile = new File(basedir, "src/test-data/data/external-application-plan.xml");
                Object plan = configBuilder.getDeploymentPlan(planFile, rarFile);
                configBuilder.buildConfiguration(plan, rarFile, tempDir);
            } finally {
                DeploymentUtil.recursiveDelete(tempDir);
            }
        } finally {
            kernel.shutdown();
            DeploymentUtil.close(rarFile);
        }
    }

    public void testBuildUnpackedModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        executeTestBuildModule(action, false);
    }

    public void testBuildUnpackedAltSpecDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                return new File(basedir, "target/test-rar-10/META-INF/geronimo-ra.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(basedir, "target/test-rar-10/dummy.xml").toURL();
            }

            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        try {
            executeTestBuildModule(action, false);
            fail("Spec alt-dd does not exist.");
        } catch (DeploymentException e) {
        }
    }

    public void testBuildUnpackedAltVendorDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                // this file does not exist, one expects a DeploymentException.
                return new File(basedir, "target/test-rar-10/dummy.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(basedir, "target/test-rar-10/META-INF/ra.xml").toURL();
            }

            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        try {
            executeTestBuildModule(action, false);
            fail("Vendor alt-dd does not exist.");
        } catch (DeploymentException e) {
        }
    }

    public void testBuildUnpackedAltSpecVendorDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                // this file exists
                return new File(basedir, "target/test-rar-10/META-INF/geronimo-ra.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(basedir, "target/test-rar-10/META-INF/ra.xml").toURL();
            }

            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        executeTestBuildModule(action, false);
    }

    public void testBuildPackedModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(basedir, "target/test-rar-10.rar");
            }
        };
        executeTestBuildModule(action, false);
    }

    //1.5 tests
    public void testBuildUnpackedModule15() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(basedir, "target/test-rar-15");

            public File getRARFile() {
                return rarFile;
            }

        };
        executeTestBuildModule(action, true);
    }


    public void testBuildPackedModule15() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(basedir, "target/test-rar-15.rar");

            public File getRARFile() {
                return rarFile;
            }

        };
        executeTestBuildModule(action, true);
    }


    private void executeTestBuildModule(InstallAction action, boolean is15) throws Exception {
        J2eeContext j2eeContext = new J2eeContextImpl("test", "bar", "null", "JCAResource", "org/apache/geronimo/j2ee/deployment/test", null, null);
        String resourceAdapterName = "testRA";
        //N.B. short version of getComponentName
        ObjectName connectionTrackerName = NameFactory.getComponentName(null, null, null, null, "ConnectionTracker", ConnectionTrackingCoordinatorGBean.GBEAN_INFO.getJ2eeType(), j2eeContext);
                //new ObjectName("test:J2EEServer=bar,J2EEModule=org/apache/geronimo/j2ee/deployment/test,service=ConnectionTracker");

        Kernel kernel = KernelFactory.newInstance().createKernel("foo");
        try {
            kernel.boot();

            GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
            kernel.loadGBean(store, this.getClass().getClassLoader());
            kernel.startGBean(store.getName());

            GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
            configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
            kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
            kernel.startGBean(configurationManagerName);

            ConnectorModuleBuilder moduleBuilder = new ConnectorModuleBuilder(defaultParentId, defaultMaxSize, defaultMinSize, defaultBlockingTimeoutMilliseconds, defaultidleTimeoutMinutes, defaultXATransactionCaching, defaultXAThreadCaching, repository, kernel);
            File rarFile = action.getRARFile();

            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            ClassLoader cl = new URLClassLoader(new URL[]{rarFile.toURL()}, oldCl);

            Thread.currentThread().setContextClassLoader(cl);

            JarFile rarJarFile = DeploymentUtil.createJarFile(rarFile);
            Module module = moduleBuilder.createModule(action.getVendorDD(), rarJarFile, j2eeContext.getJ2eeModuleName(), action.getSpecDD(), null, null);
            if (module == null) {
                throw new DeploymentException("Was not a connector module");
            }
            assertEquals(j2eeContext.getJ2eeModuleName(), module.getConfigId().toString());

            File tempDir = null;
            try {
                tempDir = DeploymentUtil.createTempDir();
                EARContext earContext = new EARContext(tempDir,
                        module.getConfigId(),
                        module.getType(),
                        module.getParentId(),
                        kernel,
                        j2eeContext.getJ2eeApplicationName(),
                        null,
                        connectionTrackerName,
                        null,
                        null,
                        null, new RefContext(ejbReferenceBuilder,
                                moduleBuilder,
                                serviceReferenceBuilder, kernel));

                action.install(moduleBuilder, earContext, module);
                earContext.getClassLoader(null);
                moduleBuilder.initContext(earContext, module, cl);
                moduleBuilder.addGBeans(earContext, module, cl);
                earContext.close();

                verifyDeployment(earContext.getConfigurationData(), tempDir, oldCl, j2eeContext, resourceAdapterName, is15);
            } finally {
                module.close();
                DeploymentUtil.recursiveDelete(tempDir);
            }
        } finally {
            kernel.shutdown();
        }
    }

    private void verifyDeployment(ConfigurationData configurationData, File unpackedDir, ClassLoader cl, J2eeContext j2eeContext, String resourceAdapterName, boolean is15) throws Exception {
        DataSource ds = null;
        Kernel kernel = null;
        try {
            GBeanData config = ExecutableConfigurationUtil.getConfigurationGBeanData(configurationData);
            ObjectName objectName = ObjectName.getInstance("test:configuration=test-ejb-jar");
            config.setName(objectName);

            kernel = KernelFactory.newInstance().createKernel("bar");
            kernel.boot();

            GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
            kernel.loadGBean(store, this.getClass().getClassLoader());
            kernel.startGBean(store.getName());

            GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
            configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
            kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
            kernel.startGBean(configurationManagerName);
            ConfigurationManager configurationManager = (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);

            ObjectName baseConfigName = configurationManager.load(defaultParentId[0]);
            kernel.startGBean(baseConfigName);

            ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeContext.getJ2eeDomainName() + ":name=ServerInfo");
            GBeanData serverInfoGBean = new GBeanData(serverInfoObjectName, BasicServerInfo.GBEAN_INFO);
            serverInfoGBean.setAttribute("baseDirectory", ".");
            kernel.loadGBean(serverInfoGBean, cl);
            kernel.startGBean(serverInfoObjectName);
            assertRunning(kernel, serverInfoObjectName);

            ObjectName j2eeServerObjectName = NameFactory.getServerName(null, null, j2eeContext);
            GBeanData j2eeServerGBean = new GBeanData(j2eeServerObjectName, J2EEServerImpl.GBEAN_INFO);
            j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
            kernel.loadGBean(j2eeServerGBean, cl);
            kernel.startGBean(j2eeServerObjectName);
            assertRunning(kernel, j2eeServerObjectName);

            // load the configuration
            config.setAttribute("baseURL", unpackedDir.toURL());
            kernel.loadGBean(config, cl);

            // start the configuration
            kernel.startRecursiveGBean(objectName);
            Set gb = kernel.listGBeans(JMXUtil.getObjectName("test:*"));
            for (Iterator iterator = gb.iterator(); iterator.hasNext();) {
                ObjectName name = (ObjectName) iterator.next();
                if (State.RUNNING_INDEX != kernel.getGBeanState(name)) {
                    System.out.println("Not running: " + name);
                }
            }
            assertRunning(kernel, objectName);

            ObjectName applicationObjectName = NameFactory.getApplicationName(null, null, null, j2eeContext);
            if (!j2eeContext.getJ2eeApplicationName().equals("null")) {
                assertRunning(kernel, applicationObjectName);
            } else {
                Set applications = kernel.listGBeans(applicationObjectName);
                assertTrue("No application object should be registered for a standalone module", applications.isEmpty());
            }

            ObjectName moduleName = NameFactory.getModuleName(null, null, null, NameFactory.RESOURCE_ADAPTER_MODULE, null, j2eeContext);
            assertRunning(kernel, moduleName);

            //1.5 only
            if (is15) {
                Map activationSpecInfoMap = (Map) kernel.getAttribute(moduleName, "activationSpecInfoMap");
                assertEquals(1, activationSpecInfoMap.size());
                GBeanData activationSpecInfo = (GBeanData) activationSpecInfoMap.get("javax.jms.MessageListener");
                assertNotNull(activationSpecInfo);
                GBeanInfo activationSpecGBeanInfo = activationSpecInfo.getGBeanInfo();
                List attributes1 = activationSpecGBeanInfo.getPersistentAttributes();
                assertEquals(2, attributes1.size());

                Map adminObjectInfoMap = (Map) kernel.getAttribute(moduleName, "adminObjectInfoMap");
                assertEquals(1, adminObjectInfoMap.size());
                GBeanData adminObjectInfo = (GBeanData) adminObjectInfoMap.get("org.apache.geronimo.connector.mock.MockAdminObject");
                assertNotNull(adminObjectInfo);
                GBeanInfo adminObjectGBeanInfo = adminObjectInfo.getGBeanInfo();
                List attributes2 = adminObjectGBeanInfo.getPersistentAttributes();
                assertEquals(3, attributes2.size());

                // ResourceAdapter
                ObjectName resourceAdapterObjectName = NameFactory.getComponentName(null, null, null, null, null, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER, j2eeContext);

                assertRunning(kernel, resourceAdapterObjectName);
                assertAttributeValue(kernel, resourceAdapterObjectName, "RAStringProperty", "NewStringValue");

                //both, except 1.0 has only one mcf type
                Map managedConnectionFactoryInfoMap = (Map) kernel.getAttribute(moduleName, "managedConnectionFactoryInfoMap");
                assertEquals(2, managedConnectionFactoryInfoMap.size());
                GBeanData managedConnectionFactoryInfo = (GBeanData) managedConnectionFactoryInfoMap.get("javax.resource.cci.ConnectionFactory");
                assertNotNull(managedConnectionFactoryInfo);
                GBeanInfo managedConnectionFactoryGBeanInfo = managedConnectionFactoryInfo.getGBeanInfo();
                List attributes3 = managedConnectionFactoryGBeanInfo.getPersistentAttributes();
                assertEquals(10, attributes3.size());
            }


            // FirstTestOutboundConnectionFactory
            ObjectName firstConnectionManagerFactory = NameFactory.getComponentName(null, null, null, null, null, "FirstTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            assertRunning(kernel, firstConnectionManagerFactory);


            ObjectName firstOutCF = NameFactory.getComponentName(null, null, null, null, null, "FirstTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, firstOutCF);

//            ObjectName firstOutSecurity = new ObjectName("geronimo.security:service=Realm,type=PasswordCredential,name=FirstTestOutboundConnectionFactory");
//            assertRunning(kernel, firstOutSecurity);

            ObjectName firstOutMCF = NameFactory.getComponentName(null, null, null, null, null, "FirstTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, firstOutMCF);
            assertAttributeValue(kernel, firstOutMCF, "OutboundStringProperty1", "newvalue1");
            assertAttributeValue(kernel, firstOutMCF, "OutboundStringProperty2", "originalvalue2");
            assertAttributeValue(kernel, firstOutMCF, "OutboundStringProperty3", "newvalue2");

            // SecondTestOutboundConnectionFactory
            ObjectName secondConnectionManagerFactory = NameFactory.getComponentName(null, null, null, null, null, "SecondTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            assertRunning(kernel, secondConnectionManagerFactory);


            ObjectName secondOutCF = NameFactory.getComponentName(null, null, null, null, null, "SecondTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, secondOutCF);

            ObjectName secondOutMCF = NameFactory.getComponentName(null, null, null, null, null, "SecondTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, secondOutMCF);

            // ThirdTestOutboundConnectionFactory
            ObjectName thirdConnectionManagerFactory = NameFactory.getComponentName(null, null, null, null, null, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            assertRunning(kernel, thirdConnectionManagerFactory);


            ObjectName thirdOutCF = NameFactory.getComponentName(null, null, null, null, null, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, thirdOutCF);

            ObjectName thirdOutMCF = NameFactory.getComponentName(null, null, null, null, null, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, thirdOutMCF);

            // 1.5 only
            //  Admin objects
            //

            if (is15) {
                ObjectName tweedledeeAdminObject = NameFactory.getComponentName(null, null, null, null, null, "tweedledee", NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
                assertRunning(kernel, tweedledeeAdminObject);

                ObjectName tweedledumAdminObject = NameFactory.getComponentName(null, null, null, null, null, "tweedledum", NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
                assertRunning(kernel, tweedledumAdminObject);
            }


            kernel.stopGBean(objectName);
        } finally {
            if (ds != null) {
                Connection connection = null;
                Statement statement = null;
                try {
                    connection = ds.getConnection();
                    statement = connection.createStatement();
                    statement.execute("SHUTDOWN");
                } finally {
                    JDBCUtil.close(statement);
                    JDBCUtil.close(connection);
                }
            }

            if (kernel != null) {
                kernel.shutdown();
            }
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void assertAttributeValue(Kernel kernel, ObjectName objectName, String attributeName, String attributeValue) throws Exception {
        Object value = kernel.getAttribute(objectName, attributeName);
        assertEquals(attributeValue, value);
    }

    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
    }

    protected void setUp() throws Exception {
        configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        defaultParentId = new URI[] {new URI("org/apache/geronimo/Server")};
    }

    private abstract class InstallAction {
        public File getVendorDD() {
            return null;
        }

        public URL getSpecDD() throws MalformedURLException {
            return null;
        }

        public abstract File getRARFile();

        public void install(ModuleBuilder moduleBuilder, EARContext earContext, Module module) throws Exception {
            moduleBuilder.installModule(module.getModuleFile(), earContext, module);
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
