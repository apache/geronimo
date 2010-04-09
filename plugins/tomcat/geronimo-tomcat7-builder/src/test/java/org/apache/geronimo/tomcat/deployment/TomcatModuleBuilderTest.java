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
package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.io.IOException;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.UnpackedJarFile;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.testsupport.TestSupport;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:385232 $ $Date$
 */
public class TomcatModuleBuilderTest extends TestSupport {
    private static Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private final AbstractName serverName = naming.createRootName(baseId, "Server", "J2EEServer");

    protected Kernel kernel;
    private AbstractName tmName;
    private AbstractName ctcName;
    private Bundle bundle;
    private TomcatModuleBuilder builder;
    private Artifact webModuleArtifact = new Artifact("foo", "bar", "1", "car");
    private Environment defaultEnvironment = new Environment();
    private ConfigurationManager configurationManager;
    private ConfigurationStore configStore;
    private AbstractName baseRootName = naming.createRootName(baseId, "root", NameFactory.SERVICE_MODULE);
    private AbstractNameQuery credentialStoreName = new AbstractNameQuery(naming.createChildName(baseRootName, "CredentialStore", GBeanInfoBuilder.DEFAULT_J2EE_TYPE));

    public void testDeployWar4() throws Exception {
// TODO:  Temporarily disabled because we don't yet have edittable configurations
//      verifyStartable("war4");
    }

    public void testDeployWar5() throws Exception {
// TODO:  Temporarily disabled because we don't yet have edittable configurations
//      verifyStartable("war5");
    }

    public void testDeployWar6() throws Exception {
// TODO:  Temporarily disabled because we don't yet have edittable configurations
//      verifyStartable("war6-jee5");
    }

    public void testContextRootWithSpaces() throws Exception {
// TODO:  Temporarily disabled because we don't yet have edittable configurations
//      WebModuleInfo info = deployWar("war-spaces-in-context");
//      String contextRoot = (String) kernel.getAttribute(info.moduleName, "contextPath");
//      assertNotNull(contextRoot);
//      assertEquals(contextRoot, contextRoot.trim());
//      undeployWar(info.configuration);
    }

    private void verifyStartable(String warName) throws Exception {
        WebModuleInfo info = deployWar(warName);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(info.moduleName));
        Set names = info.configuration.findGBeans(new AbstractNameQuery(info.moduleName.getArtifact(), Collections.EMPTY_MAP));
        log.debug("names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName objectName = (AbstractName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }
        undeployWar(info.configuration);
    }

    private WebModuleInfo deployWar(String warName) throws Exception {
        File outputPath = new File(BASEDIR, "target/test-resources/deployables/" + warName);
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        File path = new File(BASEDIR, "src/test/resources/deployables/" + warName);
        //File dest = new File(BASEDIR, "target/test-resources/deployables/" + warName + "/war");
        File dest = new File(BASEDIR, "target/test-resources/deployables/" + warName);
        recursiveCopy(path, dest);
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile, kernel.getNaming(), new ModuleIDBuilder());
        Repository repository = null;

        AbstractName moduleName = module.getModuleName();
        EARContext earContext = createEARContext(outputPath, defaultEnvironment, repository, configStore, moduleName);
        AbstractName jaccBeanName = kernel.getNaming().createChildName(moduleName, "foo", SecurityNames.JACC_MANAGER);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        PermissionCollection excludedPermissions = new Permissions();
        PermissionCollection uncheckedPermissions = new Permissions();
        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, new HashMap());
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put("test_J2EEApplication=null_J2EEServer=bar_j2eeType=WebModule_name=geronimo/test/1.0/war", componentPermissions);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
//        jaccBeanData.setAttribute("principalRoleMap", new HashMap());
        jaccBeanData.setAttribute("roleDesignates", new HashMap());
        earContext.addGBean(jaccBeanData);
        earContext.getGeneralData().put(GeronimoSecurityBuilderImpl.ROLE_MAPPER_DATA_NAME, jaccBeanName);
        module.setEarContext(earContext);
        module.setRootEarContext(earContext);
        builder.initContext(earContext, module, bundle);
        builder.addGBeans(earContext, module, bundle, null);
        ConfigurationData configurationData = earContext.getConfigurationData();
        earContext.close();
        module.close();

        Artifact configurationId = configurationData.getId();
        configurationManager.loadConfiguration(configurationData);
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        configurationManager.startConfiguration(configurationId);

        return new WebModuleInfo(moduleName, configuration);
    }

    private void undeployWar(Configuration configuration) throws Exception {
        configurationManager.stopConfiguration(configuration.getId());
        configurationManager.unloadConfiguration(configuration.getId());
    }

    private EARContext createEARContext(File outputPath, Environment environment, Repository repository, ConfigurationStore configStore, AbstractName moduleName) throws DeploymentException {
        Set repositories = repository == null ? Collections.EMPTY_SET : Collections.singleton(repository);
        Set configurationManagers = configurationManager == null ? Collections.EMPTY_SET : Collections.singleton(configurationManager);
        ArtifactManager artifactManager = new DefaultArtifactManager();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, repositories, null, configurationManagers);
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

    public void recursiveCopy(File src, File dest) throws IOException {
        Collection files = FileUtils.listFiles(src, null, true);
        Iterator iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = (File) iterator.next();
            if (file.getAbsolutePath().indexOf(".svn") < 0) {
                String pathToFile = file.getPath();
                String relativePath = pathToFile.substring(src.getPath().length(), pathToFile.length() - (file.getName().length()));
                FileUtils.copyFileToDirectory(file, new File(dest.getPath() + relativePath));
            }
        }
    }

//    protected void setUp() throws Exception {
//        super.setUp();
//        bundle = new MockBundle(getClass().getClassLoader(), "test", 100);
//        kernel = KernelFactory.newInstance(bundle.getBundleContext()).createKernel("test");
//        kernel.boot();

//        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);

//        GBeanData serverInfo = bootstrap.addGBean("ServerInfo", BasicServerInfo.class);
//        serverInfo.setAttribute("baseDirectory", BASEDIR.getAbsolutePath());

//        AbstractName configStoreName = bootstrap.addGBean("MockConfigurationStore", MockConfigStore.GBEAN_INFO).getAbstractName();

//        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

//        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.class);
//        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());

//        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", EditableKernelConfigurationManager.class);
//        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
//        configurationManagerData.setReferencePattern("Stores", configStoreName);
//        bootstrap.addGBean(configurationManagerData);

//        GBeanData serverData = new GBeanData(serverName, J2EEServerImpl.GBEAN_INFO);
//        bootstrap.addGBean(serverData);

//        GBeanData securityService = bootstrap.addGBean("SecurityService", SecurityServiceImpl.class);
//        securityService.setAttribute("policyConfigurationFactory", GeronimoPolicyConfigurationFactory.class.getName());
//        securityService.setAttribute("policyProvider", GeronimoPolicy.class.getName());
//        securityService.setReferencePattern("ServerInfo", serverInfo.getAbstractName());

        // Default Realm
//        Map initParams = new HashMap();
//
//        initParams.put("userClassNames",
//                "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
//        initParams.put("roleClassNames",
//                "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
//        GBeanData realm = bootstrap.addGBean("tomcatRealm", RealmGBean.GBEAN_INFO);
//        realm.setAttribute("className",
//                "org.apache.geronimo.tomcat.realm.TomcatJAASRealm");
//        realm.setAttribute("initParams", initParams);

        // Default Host
//        Map initParams = new HashMap();
//        initParams.put("workDir", "work");
//        initParams.put("name", "localhost");
//        initParams.put("appBase", "");
//        GBeanData host = bootstrap.addGBean("tomcatHost", HostGBean.GBEAN_INFO);
//        host.setAttribute("className", "org.apache.catalina.core.StandardHost");
//        host.setAttribute("initParams", initParams);

        // Default Engine
//        initParams = new HashMap();
//        initParams.put("name", "Geronimo");
//        GBeanData engine = bootstrap.addGBean("tomcatEngine", EngineGBean.class);
//        engine.setAttribute("className", "org.apache.geronimo.tomcat.TomcatEngine");
//        engine.setAttribute("initParams", initParams);
//        engine.setReferencePattern("DefaultHost", host.getAbstractName());
//        engine.setReferencePattern("RealmGBean", realm.getAbstractName());

//        WebServiceBuilder webServiceBuilder = new MockWebServiceBuilder();

//        GBeanData containerData = bootstrap.addGBean("TomcatContainer", TomcatContainer.class);
//        containerData.setAttribute("catalinaHome", new File(BASEDIR, "target/var/catalina").toString());
//        containerData.setReferencePattern("EngineGBean", engine.getAbstractName());
//        containerData.setReferencePattern("ServerInfo", serverInfo.getAbstractName());
//        AbstractName containerName = containerData.getAbstractName();

//        GBeanData connector = bootstrap.addGBean("TomcatConnector", new AnnotationGBeanInfoFactory().getGBeanInfo(Http11ConnectorGBean.class));
//        connector.setAttribute("name", "HTTP");
//        connector.setAttribute("port", new Integer(8181));
//        connector.setReferencePattern("TomcatContainer", containerName);
//        connector.setReferencePattern("ServerInfo", serverInfo.getAbstractName());

//        GBeanData tm = bootstrap.addGBean("TransactionManager", GeronimoTransactionManagerGBean.GBEAN_INFO);
//        tmName = tm.getAbstractName();
//        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));

//        GBeanData ctc = bootstrap.addGBean("ConnectionTrackingCoordinator", ConnectionTrackingCoordinatorGBean.GBEAN_INFO);
//        ctcName = ctc.getAbstractName();
//        ctc.setReferencePattern("TransactionManager", tmName);

//        GBeanData cs = bootstrap.addGBean("CredentialStore", DirectConfigurationCredentialStoreImpl.GBEAN_INFO);
//        Map<String, Map<String, Map<String, String>>> csd = new HashMap<String, Map<String, Map<String, String>>>();
//        Map<String, Map<String, String>> r = new HashMap<String, Map<String, String>>();
//        csd.put("foo", r);
//        Map<String, String> creds = new HashMap<String, String>();
//        r.put("metro", creds);
//        creds.put(GeronimoUserPrincipal.class.getName(), "metro");
//        cs.setAttribute("credentialStore", csd);

//        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, bundle.getBundleContext());

//        configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
//        configStore = (ConfigurationStore) kernel.getGBean(configStoreName);
//        configStore.install(bootstrap);

//        defaultEnvironment.addDependency(baseId, ImportType.ALL);
//        defaultEnvironment.setConfigId(webModuleArtifact);
//        GeronimoSecurityBuilderImpl securityBuilder = new GeronimoSecurityBuilderImpl(credentialStoreName, null, null);
//        builder = new TomcatModuleBuilder(defaultEnvironment,
//                new AbstractNameQuery(containerName),
//                Collections.singleton(webServiceBuilder),
//                Arrays.asList(new GBeanBuilder(null, null), securityBuilder),
//                new NamingBuilderCollection(null),
//                Collections.EMPTY_LIST,
//                null,
//                new MockResourceEnvironmentSetter(),
//                null);
//        builder.doStart();
//        securityBuilder.doStart();
//    }

//    protected void tearDown() throws Exception {
//        builder.doStop();
//        kernel.shutdown();
//        super.tearDown();
//    }

    private static class WebModuleInfo {
        AbstractName moduleName;
        Configuration configuration;

        public WebModuleInfo(AbstractName moduleName, Configuration configuration) {
            this.moduleName = moduleName;
            this.configuration = configuration;
        }
    }

}
