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
package org.apache.geronimo.jetty8.deployment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.wrapper.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.deployment.UnavailableWebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.jetty8.JettyContainerImpl;
import org.apache.geronimo.jetty8.connector.HTTPSocketConnector;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.util.UnpackedJarFile;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicy;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.transaction.wrapper.manager.GeronimoTransactionManagerGBean;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;

/**
 * @version $Rev:385232 $ $Date$
 */
public class JettyModuleBuilderTest extends TestSupport {

    private static Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private final AbstractName serverName = naming.createRootName(baseId, "Server", "J2EEServer");

    protected Kernel kernel;
    private AbstractName tmName;
    private AbstractName ctcName;
    private Bundle bundle;
    private JettyModuleBuilder builder;
    private Artifact webModuleArtifact = new Artifact("foo", "bar", "1", "car");
    private Environment defaultEnvironment = new Environment();
    private ConfigurationManager configurationManager;
    private ConfigurationStore configStore;

    public void testDeployWar4() throws Exception {
    /* TODO:  Temporarily disabled
        String appName = "war4";
        FakeEarBuilder fake = new FakeEarBuilder(appName);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(fake.moduleName));
        Set names = fake.configuration.findGBeans(new AbstractNameQuery(fake.moduleName.getArtifact(), Collections.EMPTY_MAP));
        log.debug("names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName objectName = (AbstractName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }

        configurationManager.stopConfiguration(fake.configurationId);
        configurationManager.unloadConfiguration(fake.configurationId);
      */
    }

    public void testContextRootWithSpaces() throws Exception {
        /* TODO:  Temporarily disabled
        String appName = "war-spaces-in-context";
        FakeEarBuilder fake = new FakeEarBuilder(appName);


        String contextRoot = (String) kernel.getAttribute(fake.moduleName, "contextPath");
        assertNotNull(contextRoot);
        assertEquals(contextRoot, contextRoot.trim());

        configurationManager.stopConfiguration(fake.configurationId);
        configurationManager.unloadConfiguration(fake.configurationId);
        */
    }

    public void testDeployJaspiConfigProvider() throws Exception {
        /* TODO:  Temporarily disabled
        FakeEarBuilder fake = new FakeEarBuilder("jaspi1");

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(fake.moduleName));
        Set names = fake.configuration.findGBeans(new AbstractNameQuery(fake.moduleName.getArtifact(), Collections.EMPTY_MAP));
        log.debug("names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName objectName = (AbstractName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }

        configurationManager.stopConfiguration(fake.configurationId);
        configurationManager.unloadConfiguration(fake.configurationId);
        */
    }

    private class FakeEarBuilder {
        private AbstractName moduleName;
        private Artifact configurationId;
        private Configuration configuration;

        private FakeEarBuilder(String appName) throws IOException, DeploymentException, LifecycleException, NoSuchConfigException {
            File outputPath = new File(BASEDIR, "target/test-resources/deployables/" + appName);
            recursiveDelete(outputPath);
            outputPath.mkdirs();
            new File(outputPath, "war").mkdir();
            File path = new File(BASEDIR, "src/test/resources/deployables/" + appName);
            UnpackedJarFile jarFile = new UnpackedJarFile(path);
            Module module = builder.createModule(null, jarFile, kernel.getNaming(), new ModuleIDBuilder());
            ListableRepository repository = null;

            moduleName = module.getModuleName();
            EARContext earContext = createEARContext(outputPath, defaultEnvironment, repository, configStore, moduleName);
            module.setEarContext(earContext);
            module.setRootEarContext(earContext);
            builder.initContext(earContext, module, bundle);
//            earContext.initializeConfiguration();
            builder.addGBeans(earContext, module, bundle, Collections.EMPTY_SET);
            ConfigurationData configurationData = earContext.getConfigurationData();
            earContext.close();
            module.close();

            configurationId = configurationData.getId();
            configurationManager.loadConfiguration(configurationData);
            configuration = configurationManager.getConfiguration(configurationId);
//            ((MockConfigStore)configStore).installFake(configurationId, outputPath);
            configurationManager.startConfiguration(configurationId);
        }
    }

    private EARContext createEARContext(File outputPath, Environment environment, ListableRepository repository, ConfigurationStore configStore, AbstractName moduleName) throws DeploymentException {
        Set repositories = repository == null ? Collections.EMPTY_SET : Collections.singleton(repository);
        ArtifactManager artifactManager = new DefaultArtifactManager();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, repository);
        return new EARContext(outputPath,
                null,
                environment,
                ConfigurationModuleType.WAR,
                naming,
                configurationManager,
                bundle.getBundleContext(),
                new AbstractNameQuery(serverName),
                moduleName,
                new AbstractNameQuery(tmName),
                new AbstractNameQuery(ctcName),
                null
        );
    }

    private void recursiveDelete(File path) {
        //does not delete top level dir passed in
        File[] listing = path.listFiles();
        if (listing != null) {
            for (int i = 0; i < listing.length; i++) {
                File file = listing[i];
                if (file.isDirectory()) {
                    recursiveDelete(file);
                }
                file.delete();
            }
        }
    }
    protected void setUpSecurityService() throws Exception {
        ServerInfo serverInfo = new BasicServerInfo(".");

        new SecurityServiceImpl(this.getClass().getClassLoader(), serverInfo, GeronimoPolicyConfigurationFactory.class.getName(), GeronimoPolicy.class.getName(), null, null, null, null);
    }

    protected void setUp() throws Exception {
        super.setUp();
        bundle = new MockBundle(getClass().getClassLoader(), "test", 100);
        setUpSecurityService();

        ((SchemaTypeImpl) GerSecurityDocument.type).addSubstitutionGroupMember(org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument.type.getDocumentElementName());

        kernel = KernelFactory.newInstance(bundle.getBundleContext()).createKernel("test");
        kernel.boot(bundle.getBundleContext());

        ArtifactManager artifactManager = new DefaultArtifactManager();

        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver();
        artifactResolver.setArtifactManager(artifactManager);

        KernelConfigurationManager configurationManager = new KernelConfigurationManager();
        configurationManager.setArtifactManager(artifactManager);
        configurationManager.setArtifactResolver(artifactResolver);
        configurationManager.setKernel(kernel);
        ConfigurationStore configStore = new MockConfigStore();
        configurationManager.bindConfigurationStore(configStore);
        configurationManager.activate(bundle.getBundleContext());
        this.configurationManager = configurationManager;

        artifactResolver.setConfigurationManager(configurationManager);

        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);

        GBeanData serverInfo = bootstrap.addGBean("ServerInfo", BasicServerInfo.class);
        serverInfo.setAttribute("baseDirectory", ".");

//        AbstractName configStoreName = bootstrap.addGBean("MockConfigurationStore", MockConfigStore.GBEAN_INFO).getAbstractName();

//        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

//        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.class);
//        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());

//        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", KernelConfigurationManager.class);
//        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
//        configurationManagerData.setReferencePattern("Stores", configStoreName);
//        bootstrap.addGBean(configurationManagerData);

        GBeanData serverData = new GBeanData(serverName, J2EEServerImpl.class);
        bootstrap.addGBean(serverData);

        Collection defaultServlets = new HashSet();
        Collection defaultFilters = new HashSet();
        Collection defaultFilterMappings = new HashSet();
        Object pojoWebServiceTemplate = null;
        WebServiceBuilder webServiceBuilder = new UnavailableWebServiceBuilder();

        GBeanData containerData = bootstrap.addGBean("JettyContainer", JettyContainerImpl.class);
        AbstractName containerName = containerData.getAbstractName();
        containerData.setAttribute("jettyHome", new File(BASEDIR, "target/var/jetty").toString());
        containerData.setReferencePattern("ServerInfo", serverInfo.getAbstractName());

        GBeanData connector = bootstrap.addGBean("JettyConnector", HTTPSocketConnector.class);
        connector.setAttribute("port", new Integer(5678));
        connector.setAttribute("maxThreads", new Integer(50));
//        connector.setAttribute("minThreads", new Integer(10));
        connector.setReferencePattern("JettyContainer", containerName);

        GBeanData tm = bootstrap.addGBean("TransactionManager", GeronimoTransactionManagerGBean.class);
        tmName = tm.getAbstractName();
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));

        GBeanData ctc = bootstrap.addGBean("ConnectionTrackingCoordinator", ConnectionTrackingCoordinatorGBean.class);
        ctcName = ctc.getAbstractName();
        ctc.setReferencePattern("TransactionManager", tmName);

//        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, new MockBundleContext(getClass().getClassLoader(), null, null, null));
        configStore.install(bootstrap);
        configurationManager.loadConfiguration(bootstrap.getId());
//        configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
//        configStore = (ConfigurationStore) kernel.getGBean(configStoreName);

        defaultEnvironment.addDependency(baseId, ImportType.ALL);
        defaultEnvironment.setConfigId(webModuleArtifact);
        Collection<ModuleBuilderExtension> moduleBuilderExtensions = new ArrayList<ModuleBuilderExtension>();
        GeronimoSecurityBuilderImpl securityBuilder = new GeronimoSecurityBuilderImpl(null, null, null);
        MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
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
        builder = new JettyModuleBuilder(defaultEnvironment,
                new Integer(1800),
                new AbstractNameQuery(containerName),
                //new AbstractNameQuery(containerName),
                new WebAppInfo(),
                pojoWebServiceTemplate,
                Collections.singleton(webServiceBuilder),
                null,
                Arrays.asList(new GBeanBuilder(), securityBuilder),
                new NamingBuilderCollection(null),
                moduleBuilderExtensions,
                new MockResourceEnvironmentSetter(),
                kernel,
                bundleContext);
        builder.doStart();

        securityBuilder.doStart();
    }

    protected void tearDown() throws Exception {
        builder.doStop();
        kernel.shutdown();
        super.tearDown();
    }

}
