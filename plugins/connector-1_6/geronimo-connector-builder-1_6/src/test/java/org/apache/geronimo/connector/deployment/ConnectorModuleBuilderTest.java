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

package org.apache.geronimo.connector.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.naming.Reference;
import javax.sql.DataSource;

import org.apache.geronimo.bval.ValidatorFactoryGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.mock.MockRepository;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.configuration.OsgiMetaDataProvider;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.transaction.wrapper.manager.GeronimoTransactionManagerGBean;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;

/**
 * @version $Rev:385232 $ $Date$
 */
public class ConnectorModuleBuilderTest extends TestSupport {

    private boolean defaultXATransactionCaching = true;
    private boolean defaultXAThreadCaching = false;
    private int defaultMaxSize = 10;
    private int defaultMinSize = 0;
    private int defaultBlockingTimeoutMilliseconds = 5000;
    private int defaultidleTimeoutMinutes = 15;
    private String defaultWorkManagerName = "DefaultWorkManager";
    private Environment defaultEnvironment;
    private ConfigurationStore configurationStore = new MockConfigStore();
    private MockRepository repository;

    private ActivationSpecInfoLocator activationSpecInfoLocator = new ActivationSpecInfoLocator() {

        public Reference createResourceRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
            return null;
        }

        public Reference createAdminObjectRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
            return null;
        }

        public GBeanData locateActivationSpecInfo(AbstractNameQuery nameQuery, String messageListenerInterface, Configuration configuration) {
            return null;
        }
    };

    private Kernel kernel;
    private ConfigurationManager configurationManager;
    private static final Naming naming = new Jsr77Naming();
    private static final Artifact bootId = new Artifact("test", "test", "42", "car");

    private static final AbstractNameQuery connectionTrackerName = new AbstractNameQuery(null, Collections.singletonMap("name", "ConnectionTracker"));
    private AbstractName serverName;
    private static final AbstractNameQuery transactionManagerName = new AbstractNameQuery(null, Collections.singletonMap("name", "TransactionManager"));

    public void testBuildEar() throws Exception {
        JarFile rarFile = null;
        try {
            rarFile = JarUtils.createJarFile(new File(BASEDIR, "target/test-ear-noger.ear"));
            GBeanBuilder serviceBuilder = new GBeanBuilder();
            EARConfigBuilder configBuilder = new EARConfigBuilder(defaultEnvironment,
                    transactionManagerName,
                    connectionTrackerName,
                    null,
                    null,
                    new AbstractNameQuery(serverName, J2EEServerImpl.GBEAN_INFO.getInterfaces()),
                    Collections.singleton(repository),
                    null,
                    null,
                    new ConnectorModuleBuilder(defaultEnvironment, defaultMaxSize, defaultMinSize, defaultBlockingTimeoutMilliseconds, defaultidleTimeoutMinutes, defaultXATransactionCaching, defaultXAThreadCaching, defaultWorkManagerName, Collections.<NamespaceDrivenBuilder>singleton(serviceBuilder), null, bundleContext),
                    activationSpecInfoLocator,
                    null,
                    serviceBuilder,
                    null, 
                    null,
                    new NamingBuilderCollection(Collections.<NamingBuilder>emptyList()),
                    kernel.getNaming(),
                    null,
                    bundleContext);
            configBuilder.doStart();
            ConfigurationData configData = null;
            DeploymentContext context = null;
            ArtifactManager artifactManager = new DefaultArtifactManager();
            ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, repository);

            try {
                File planFile = new File(BASEDIR, "src/test/resources/data/external-application-plan.xml");
                ModuleIDBuilder idBuilder = new ModuleIDBuilder();
                Object plan = configBuilder.getDeploymentPlan(planFile, rarFile, idBuilder);
                context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, rarFile, idBuilder), plan, rarFile, Collections.singleton(configurationStore), artifactResolver, configurationStore);
                // add the a j2ee server so the application context reference can be resolved
                context.addGBean("geronimo", J2EEServerImpl.GBEAN_INFO);
                // add the module validator so the connector artifacts will resolve to an instance
                AbstractName abstractName = context.getNaming().createChildName(((Module)plan).getModuleName(), "ValidatorFactory", NameFactory.VALIDATOR_FACTORY);
                GBeanData gbeanData = new GBeanData(abstractName, ValidatorFactoryGBean.class);
                context.addGBean(gbeanData);
                configData = context.getConfigurationData();
            } finally {
                if (context != null) {
                    context.close();
                }
                if (configData != null) {
                    FileUtils.recursiveDelete(configData.getConfigurationDir());
                }
            }
            configBuilder.doStop();
        } finally {
            JarUtils.close(rarFile);

        }
    }

    public void testBuildUnpackedModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(BASEDIR, "target/test-rar-10");
            }
        };
        executeTestBuildModule(action, false);
    }

    public void testConnectionFactoryValidation() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(BASEDIR, "target/test-cf-validation");
            }
        };
        try {
            executeTestBuildModule(action, true);
            fail("ConstraintViolation not thrown");
        } catch (org.apache.geronimo.kernel.config.LifecycleException e) {
            // we'll get a deployment failure.  The root reason will be a ValidationException,
            // but for now, that's difficult to root out and locate.
        }
    }

    public void testResourceAdaptorValidation() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(BASEDIR, "target/test-ra-validation");
            }
        };
        try {
            executeTestBuildModule(action, true);
            fail("ConstraintViolation not thrown");
        } catch (org.apache.geronimo.kernel.config.LifecycleException e) {
            // we'll get a deployment failure.  The root reason will be a ValidationException,
            // but for now, that's difficult to root out and locate.
        }
    }

    public void testAdminObjectValidation() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(BASEDIR, "target/test-ao-validation");
            }
        };
        try {
            executeTestBuildModule(action, true);
            fail("ConstraintViolation not thrown");
        } catch (org.apache.geronimo.kernel.config.LifecycleException e) {
            // we'll get a deployment failure.  The root reason will be a ValidationException,
            // but for now, that's difficult to root out and locate.
        }
    }

/* TODO:  figure out what the lifecycle is here so this can be processed
    public void testActivationSpecBeanValidation() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(BASEDIR, "target/test-asb-validation");
            }
        };
        try {
            executeTestBuildModule(action, true);
            fail("ConstraintViolation not thrown");
        } catch (org.apache.geronimo.kernel.config.LifecycleException e) {
            // we'll get a deployment failure.  The root reason will be a ValidationException,
            // but for now, that's difficult to root out and locate.
        }
    }
 */

    public void testBuildUnpackedAltSpecDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                return new File(BASEDIR, "target/test-rar-10/META-INF/geronimo-ra.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(BASEDIR, "target/test-rar-10/dummy.xml").toURI().toURL();
            }

            public File getRARFile() {
                return new File(BASEDIR, "target/test-rar-10");
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
                return new File(BASEDIR, "target/test-rar-10/dummy.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(BASEDIR, "target/test-rar-10/META-INF/ra.xml").toURI().toURL();
            }

            public File getRARFile() {
                return new File(BASEDIR, "target/test-rar-10");
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
                return new File(BASEDIR, "target/test-rar-10/META-INF/geronimo-ra.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(BASEDIR, "target/test-rar-10/META-INF/ra.xml").toURI().toURL();
            }

            public File getRARFile() {
                return new File(BASEDIR, "target/test-rar-10");
            }
        };
        executeTestBuildModule(action, false);
    }

    public void testBuildPackedModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(BASEDIR, "target/test-rar-10.rar");
            }
        };
        executeTestBuildModule(action, false);
    }

    //1.5 tests
    public void testBuildUnpackedModule15() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(BASEDIR, "target/test-rar-15");

            public File getRARFile() {
                return rarFile;
            }

        };
        executeTestBuildModule(action, true);
    }


    public void testBuildPackedModule15() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(BASEDIR, "target/test-rar-15.rar");

            public File getRARFile() {
                return rarFile;
            }

        };
        executeTestBuildModule(action, true);
    }

    public void testBuildPackedModule15LocalTx() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(BASEDIR, "target/test-rar-15-localtx.rar");

            public File getRARFile() {
                return rarFile;
            }

        };
        try {
            executeTestBuildModule(action, true);
            fail("transaction setting mismatch not detected");
        } catch (DeploymentException e) {

        }
    }

    public void testBuildPackedModule15NoTx() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(BASEDIR, "target/test-rar-15-notx.rar");

            public File getRARFile() {
                return rarFile;
            }

        };
        try {
            executeTestBuildModule(action, true);
            fail("transaction setting mismatch not detected");
        } catch (DeploymentException e) {

        }
    }

    //1.6 tests
    public void testBuildUnpackedModule16() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(BASEDIR, "target/test-rar-16");

            public File getRARFile() {
                return rarFile;
            }

        };
        executeTestBuildModule(action, true);
    }


    public void testBuildPackedModule16() throws Exception {
        InstallAction action = new InstallAction() {
            private File rarFile = new File(BASEDIR, "target/test-rar-16.rar");

            public File getRARFile() {
                return rarFile;
            }

        };
        executeTestBuildModule(action, true);
    }

    private void executeTestBuildModule(InstallAction action, boolean is15) throws Exception {
        String resourceAdapterName = "testRA";

        try {
            ConnectorModuleBuilder moduleBuilder = new ConnectorModuleBuilder(defaultEnvironment, defaultMaxSize, defaultMinSize, defaultBlockingTimeoutMilliseconds, defaultidleTimeoutMinutes, defaultXATransactionCaching, defaultXAThreadCaching, defaultWorkManagerName, Collections.<NamespaceDrivenBuilder>singleton(new GBeanBuilder()), null, bundleContext);
            File rarFile = action.getRARFile();

            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

            JarFile rarJarFile = JarUtils.createJarFile(rarFile);
            String moduleName = "geronimo/test-ear/1.0/car";
            Module module = moduleBuilder.createModule(action.getVendorDD(), rarJarFile, moduleName, action.getSpecDD(), null, null, null, naming, new ModuleIDBuilder());
            if (module == null) {
                throw new DeploymentException("Was not a connector module");
            }
            assertEquals(moduleName, module.getEnvironment().getConfigId().toString());

            File tempDir = null;
            try {
                tempDir = FileUtils.createTempDir();
                EARContext earContext = new EARContext(tempDir,
                        null,
                        module.getEnvironment(),
                        module.getType(),
                        naming,
                        configurationManager,
                        bundleContext,
                        new AbstractNameQuery(serverName, J2EEServerImpl.GBEAN_INFO.getInterfaces()),
                        module.getModuleName(), //hardcode standalone here.
                        transactionManagerName,
                        connectionTrackerName,
                        null
                );
                earContext.initializeConfiguration();
                action.install(moduleBuilder, earContext, module, configurationStore);
                earContext.initializeConfiguration();
                Bundle bundle = earContext.getDeploymentBundle();
                moduleBuilder.initContext(earContext, module, bundle);
                moduleBuilder.addGBeans(earContext, module, bundle, Collections.singleton(repository));
                // add the module validator so the connector artifacts will resolve to an instance
                AbstractName abstractName = earContext.getNaming().createChildName(module.getModuleName(), "ValidatorFactory", NameFactory.VALIDATOR_FACTORY);
                GBeanData gbeanData = new GBeanData(abstractName, ValidatorFactoryGBean.class);
                earContext.addGBean(gbeanData);

                ConfigurationData configurationData = earContext.getConfigurationData();
                AbstractName moduleAbstractName = earContext.getModuleName();
                earContext.close();

                verifyDeployment(configurationData, oldCl, moduleAbstractName, resourceAdapterName, is15, module.getName());
            } finally {
                module.close();
                FileUtils.recursiveDelete(tempDir);
            }
        } finally {
            kernel.shutdown();
        }
    }

    private void verifyDeployment(ConfigurationData configurationData, ClassLoader cl, AbstractName moduleAbstractName, String resourceAdapterName, boolean is15, String moduleName) throws Exception {
        configurationData.setBundleContext(bundleContext);
        DataSource ds = null;
        try {

            Artifact configurationId = configurationData.getId();

            // load the configuration
            configurationManager.loadConfiguration(configurationData);
            configurationManager.startConfiguration(configurationId);
            Configuration configuration = configurationManager.getConfiguration(configurationId);
            Set<AbstractName> gb = configuration.getGBeans().keySet();
            for (AbstractName name : gb) {
                if (State.RUNNING_INDEX != kernel.getGBeanState(name)) {
                    log.debug("Not running: " + name);
                }
            }

            assertRunning(kernel, moduleAbstractName);
            AbstractName resourceAdapterjsr77Name = naming.createChildName(moduleAbstractName, moduleName, NameFactory.RESOURCE_ADAPTER);
            assertRunning(kernel, resourceAdapterjsr77Name);
            AbstractName jcaResourcejsr77Name = naming.createChildName(resourceAdapterjsr77Name, moduleName, NameFactory.JCA_RESOURCE);
            assertRunning(kernel, jcaResourcejsr77Name);

            //1.5 only
            if (is15) {
                Map activationSpecInfoMap = (Map) kernel.getAttribute(moduleAbstractName, "activationSpecInfoMap");
                assertEquals(1, activationSpecInfoMap.size());
                GBeanData activationSpecInfo = (GBeanData) activationSpecInfoMap.get("javax.jms.MessageListener");
                assertNotNull(activationSpecInfo);
                GBeanInfo activationSpecGBeanInfo = activationSpecInfo.getGBeanInfo();
                List attributes1 = activationSpecGBeanInfo.getPersistentAttributes();
                assertEquals(3, attributes1.size());

                Map adminObjectInfoMap = (Map) kernel.getAttribute(moduleAbstractName, "adminObjectInfoMap");
                assertEquals(1, adminObjectInfoMap.size());
                GBeanData adminObjectInfo = (GBeanData) adminObjectInfoMap.get("org.apache.geronimo.connector.mock.MockAdminObject");
                assertNotNull(adminObjectInfo);
                GBeanInfo adminObjectGBeanInfo = adminObjectInfo.getGBeanInfo();
                List attributes2 = adminObjectGBeanInfo.getPersistentAttributes();
                assertEquals(3, attributes2.size());

                // ResourceAdapter
                AbstractName resourceAdapterObjectName = naming.createChildName(jcaResourcejsr77Name, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER);

                assertRunning(kernel, resourceAdapterObjectName);
                assertAttributeValue(kernel, resourceAdapterObjectName, "RAStringProperty", "NewStringValue");

                //both, except 1.0 has only one mcf type
                Map managedConnectionFactoryInfoMap = (Map) kernel.getAttribute(moduleAbstractName, "managedConnectionFactoryInfoMap");
                assertEquals(2, managedConnectionFactoryInfoMap.size());
                GBeanData managedConnectionFactoryInfo = (GBeanData) managedConnectionFactoryInfoMap.get("javax.resource.cci.ConnectionFactory");
                assertNotNull(managedConnectionFactoryInfo);
                GBeanInfo managedConnectionFactoryGBeanInfo = managedConnectionFactoryInfo.getGBeanInfo();
                List attributes3 = managedConnectionFactoryGBeanInfo.getPersistentAttributes();
                assertEquals(12, attributes3.size());
            }

            // FirstTestOutboundConnectionFactory
            AbstractName firstOutCF = naming.createChildName(jcaResourcejsr77Name, "FirstTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY);
            assertRunning(kernel, firstOutCF);

            AbstractName firstOutMCF = naming.createChildName(firstOutCF, "FirstTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
            assertRunning(kernel, firstOutMCF);
            assertAttributeValue(kernel, firstOutMCF, "OutboundStringProperty1", "newvalue1");
            assertAttributeValue(kernel, firstOutMCF, "OutboundStringProperty2", "originalvalue2");
            assertAttributeValue(kernel, firstOutMCF, "OutboundStringProperty3", "newvalue2");

            AbstractName firstConnectionManagerFactory = naming.createChildName(firstOutMCF, "FirstTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER);
            assertRunning(kernel, firstConnectionManagerFactory);

            // SecondTestOutboundConnectionFactory
            AbstractName secondOutCF = naming.createChildName(jcaResourcejsr77Name, "SecondTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY);
            assertRunning(kernel, secondOutCF);

            AbstractName secondOutMCF = naming.createChildName(secondOutCF, "SecondTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
            assertRunning(kernel, secondOutMCF);

            AbstractName secondConnectionManagerFactory = naming.createChildName(secondOutMCF, "SecondTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER);
            assertRunning(kernel, secondConnectionManagerFactory);

            // ThirdTestOutboundConnectionFactory
            AbstractName thirdOutCF = naming.createChildName(jcaResourcejsr77Name, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY);
            assertRunning(kernel, thirdOutCF);

            AbstractName thirdOutMCF = naming.createChildName(thirdOutCF, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
            assertRunning(kernel, thirdOutMCF);

            AbstractName thirdConnectionManagerFactory = naming.createChildName(thirdOutMCF, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER);
            assertRunning(kernel, thirdConnectionManagerFactory);

            // 1.5 only
            //  Admin objects
            //

            if (is15) {
                AbstractName tweedledeeAdminObject = naming.createChildName(jcaResourcejsr77Name, "tweedledee", NameFactory.JCA_ADMIN_OBJECT);
                assertRunning(kernel, tweedledeeAdminObject);

                AbstractName tweedledumAdminObject = naming.createChildName(jcaResourcejsr77Name, "tweedledum", NameFactory.JCA_ADMIN_OBJECT);
                assertRunning(kernel, tweedledumAdminObject);
            }


            configurationManager.stopConfiguration(configurationId);
            configurationManager.unloadConfiguration(configurationId);
        } finally {
            if (ds != null) {
                Connection connection =  ds.getConnection();
                try {
                    Statement statement = connection.createStatement();
                    try {
                        statement.execute("SHUTDOWN");
                    } finally {
                        statement.close();
                    }
                } finally {
                    connection.close();
                }
            }

            if (kernel != null) {
                kernel.shutdown();
            }
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void assertAttributeValue(Kernel kernel, AbstractName name, String attributeName, String attributeValue) throws Exception {
        Object value = kernel.getAttribute(name, attributeName);
        assertEquals(attributeValue, value);
    }

    private void assertRunning(Kernel kernel, AbstractName name) throws Exception {
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(name));
    }

    protected void setUp() throws Exception {
        super.setUp();
        Artifact artifact = new Artifact("foo", "bar", "1.0", "car");
        Map<String, Artifact> locations = new HashMap<String, Artifact>();
        locations.put(null, artifact);
        bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, locations);
        PackageAdmin packageAdmin = new PackageAdmin() {

                @Override
                public ExportedPackage[] getExportedPackages(Bundle bundle) {
                    return new ExportedPackage[0];
                }

                @Override
                public ExportedPackage[] getExportedPackages(String s) {
                    return new ExportedPackage[0];
                }

                @Override
                public ExportedPackage getExportedPackage(String s) {
                    return null;
                }

                @Override
                public void refreshPackages(Bundle[] bundles) {
                }

                @Override
                public boolean resolveBundles(Bundle[] bundles) {
                    return false;
                }

                @Override
                public RequiredBundle[] getRequiredBundles(String s) {
                    return new RequiredBundle[0];
                }

                @Override
                public Bundle[] getBundles(String s, String s1) {
                    return new Bundle[0];
                }

                @Override
                public Bundle[] getFragments(Bundle bundle) {
                    return new Bundle[0];
                }

                @Override
                public Bundle[] getHosts(Bundle bundle) {
                    return new Bundle[0];
                }

                @Override
                public Bundle getBundle(Class aClass) {
                    return null;
                }

                @Override
                public int getBundleType(Bundle bundle) {
                    return 0;
                }
            };
        bundleContext.registerService(PackageAdmin.class.getName(), packageAdmin, null);
        bundleContext.registerService(OsgiMetaDataProvider.class.getName(), new MockDependencyManager(bundleContext, Collections.<Repository> emptyList(), null), new Hashtable());
        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);

        ArtifactManager artifactManager = new DefaultArtifactManager();

        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver();
        artifactResolver.setArtifactManager(artifactManager);

        repository = new MockRepository();
        ConfigurationStore configStore = new MockConfigStore();

        KernelConfigurationManager configurationManager = new KernelConfigurationManager();
        configurationManager.setArtifactManager(artifactManager);
        configurationManager.setArtifactResolver(artifactResolver);
        configurationManager.setKernel(kernel);
        configurationManager.activate(bundleContext);
        configurationManager.bindRepository(repository);
        configurationManager.bindConfigurationStore(configStore);
        this.configurationManager = configurationManager;

        artifactResolver.setConfigurationManager(configurationManager);

        ConfigurationData bootstrap = new ConfigurationData(bootId, naming);
        bootstrap.setBundleContext(bundleContext);

//        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
//
//        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.GBEAN_INFO);
//        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//
//        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", EditableKernelConfigurationManager.GBEAN_INFO);
//        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
//        bootstrap.addGBean(configurationManagerData);
//        bootstrap.addGBean("ServerInfo", BasicServerInfo.class).setAttribute("baseDirectory", ".");
//
//        AbstractName repositoryName = bootstrap.addGBean("Repository", MockRepository.GBEAN_INFO).getAbstractName();
//
//        AbstractName configStoreName = bootstrap.addGBean("MockConfigurationStore", MockConfigStore.GBEAN_INFO).getAbstractName();
//
//        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
//
//        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.class);
//        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//
//        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", KernelConfigurationManager.class);
//        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
//        configurationManagerData.setReferencePattern("Stores", configStoreName);
//        configurationManagerData.setReferencePattern("Repositories", repositoryName);
//        bootstrap.addGBean(configurationManagerData);

        GBeanData serverData = bootstrap.addGBean("geronimo", J2EEServerImpl.GBEAN_INFO);
        serverName = serverData.getAbstractName();
        bootstrap.addGBean(serverData);


        // add fake TransactionManager so refs will resolve
        GBeanData tm = bootstrap.addGBean("TransactionManager", GeronimoTransactionManagerGBean.class);
        tm.setAttribute("defaultTransactionTimeoutSeconds", 10);

        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, bundleContext, configurationManager);

        Map<Artifact, File> repo = repository.getRepo();
        repo.put(Artifact.create("org.apache.geronimo.tests/test/1/car"), null);
        repo.put(bootId, null);


//        configurationManager.getConfiguration(bootstrap.getId());
//        ConfigurationStore configStore = (ConfigurationStore) kernel.getGBean(configStoreName);
        configStore.install(bootstrap);

        defaultEnvironment = new Environment();
        defaultEnvironment.addDependency(bootstrap.getId(), ImportType.ALL);

    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        ((MockConfigStore)configurationStore).cleanup();
        super.tearDown();
    }

    private abstract class InstallAction {
        public File getVendorDD() {
            return null;
        }

        public URL getSpecDD() throws MalformedURLException {
            return null;
        }

        public abstract File getRARFile();

        public void install(ModuleBuilder moduleBuilder, EARContext earContext, Module module, ConfigurationStore configurationStore) throws Exception {
            moduleBuilder.installModule(module.getModuleFile(), earContext, module, Collections.singleton(configurationStore), configurationStore, Collections.singleton(repository));
        }
    }

    private class MockDependencyManager extends DependencyManager {

        public MockDependencyManager(BundleContext bundleContext, Collection<Repository> repositories, ArtifactResolver artifactResolver) {
            super(bundleContext, repositories, artifactResolver);
        }

        @Override
        public synchronized Set<ExportPackage> getExportedPackages(Bundle bundle) {
           return Collections.<ExportPackage>emptySet();
        }

        @Override
        public List<Bundle> getDependentBundles(Bundle bundle) {
            return Collections.<Bundle>emptyList();
        }

        @Override
        public Bundle getBundle(Artifact artifact) {
            return null;
        }

    }
}
