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
package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.UnavailableWebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
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
import org.apache.geronimo.kernel.config.EditableKernelConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.tomcat.ConnectorGBean;
import org.apache.geronimo.tomcat.EngineGBean;
import org.apache.geronimo.tomcat.HostGBean;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;

/**
 * @version $Rev:385232 $ $Date$
 */
public class TomcatModuleBuilderTest extends TestCase {
    private static Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private final AbstractName serverName = naming.createRootName(baseId, "Server", "J2EEServer");

    protected Kernel kernel;
    private AbstractName ctcName;
    private AbstractName tcmName;
    private ClassLoader cl;
    private TomcatModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));
    private Artifact webModuleArtifact = new Artifact("foo", "bar", "1", "car");
    private Environment defaultEnvironment = new Environment();
    private ConfigurationManager configurationManager;
    private ConfigurationStore configStore;


    public void testDeployWar4() throws Exception {
        verifyStartable("war4");
    }

    public void testDeployWar5() throws Exception {
        verifyStartable("war5");
    }

    public void testContextRootWithSpaces() throws Exception {
        WebModuleInfo info = deployWar("war-spaces-in-context");
        String contextRoot = (String) kernel.getAttribute(info.moduleName, "contextPath");
        assertNotNull(contextRoot);
        assertEquals(contextRoot, contextRoot.trim());
        undeployWar(info.configuration);
    }

    private void verifyStartable(String warName) throws Exception {
        WebModuleInfo info = deployWar(warName);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(info.moduleName));
        Set names = info.configuration.findGBeans(new AbstractNameQuery(info.moduleName.getArtifact(), Collections.EMPTY_MAP));
        System.out.println("names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName objectName = (AbstractName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }
        undeployWar(info.configuration);
    }

    private WebModuleInfo deployWar(String warName) throws Exception {
        File outputPath = new File(basedir, "target/test-resources/deployables/" + warName);
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        File path = new File(basedir, "src/test-resources/deployables/" + warName);
        File dest = new File(basedir, "target/test-resources/deployables/" + warName + "/war");
        recursiveCopy(path, dest);
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile, kernel.getNaming(), new ModuleIDBuilder());
        Repository repository = null;

        AbstractName moduleName = module.getModuleName();
        EARContext earContext = createEARContext(outputPath, defaultEnvironment, repository, configStore, moduleName);
        AbstractName jaccBeanName = kernel.getNaming().createChildName(moduleName, "foo", NameFactory.JACC_MANAGER);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        PermissionCollection excludedPermissions= new Permissions();
        PermissionCollection uncheckedPermissions= new Permissions();
        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, new HashMap());
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put("test_J2EEApplication=null_J2EEServer=bar_j2eeType=WebModule_name=geronimo/test/1.0/war", componentPermissions);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
//        jaccBeanData.setAttribute("principalRoleMap", new HashMap());
        jaccBeanData.setAttribute("roleDesignates", new HashMap());
        earContext.addGBean(jaccBeanData);
        earContext.setJaccManagerName(jaccBeanName);
        module.setEarContext(earContext);
        builder.initContext(earContext, module, cl);
        builder.addGBeans(earContext, module, cl, null);
        ConfigurationData configurationData = earContext.getConfigurationData();
        earContext.close();
        module.close();

        Artifact configurationId = configurationData.getId();
        configurationManager.loadConfiguration(configurationData);
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        configurationManager.startConfiguration(configurationId);

        return new WebModuleInfo(moduleName, configuration);
    }

    private void undeployWar(Configuration configuration) throws Exception{
        configurationManager.stopConfiguration(configuration.getId());
        configurationManager.unloadConfiguration(configuration.getId());
    }

    private EARContext createEARContext(File outputPath, Environment environment, Repository repository, ConfigurationStore configStore, AbstractName moduleName) throws DeploymentException {
        Set repositories = repository == null ? Collections.EMPTY_SET : Collections.singleton(repository);
        ArtifactManager artifactManager = new DefaultArtifactManager();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, repositories, null);
        return new EARContext(outputPath,
                null,
                environment,
                ConfigurationModuleType.WAR,
                naming,
                configurationManager,
                repositories,
                new AbstractNameQuery(serverName),
                moduleName,
                null,
                new AbstractNameQuery(tcmName),
                new AbstractNameQuery(ctcName),
                null,
                null,
                null,
                new RefContext(new EJBReferenceBuilder() {


                    public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) {
                        return null;
                    }

                    public Reference createEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) {
                        return null;
                    }

                    public Reference createEJBLocalRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local) {
                        return null;
                    }

                },
                        new ResourceReferenceBuilder() {

                            public Reference createResourceRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
                                return null;
                            }

                            public Reference createAdminObjectRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
                                return null;
                            }

                            public ObjectName locateResourceName(ObjectName query) {
                                return null;
                            }

                            public GBeanData locateActivationSpecInfo(AbstractNameQuery nameQuery, String messageListenerInterface, Configuration configuration) {
                                return null;
                            }

                            public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) {
                                return null;
                            }

                            public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) {
                                return null;
                            }

                            public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) {
                                return null;
                            }
                        },
                        new ServiceReferenceBuilder() {
                            //it could return a Service or a Reference, we don't care
                            public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) {
                                return null;
                            }
                        }));
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
        Collection files = FileUtils.listFiles(src,null,true);
        Iterator iterator = files.iterator();
        while(iterator.hasNext()){
            File file = (File) iterator.next();
            if (file.getAbsolutePath().indexOf(".svn") < 0){
                String pathToFile = file.getPath();
                String relativePath = pathToFile.substring(src.getPath().length(), pathToFile.length() - (file.getName().length()));
                FileUtils.copyFileToDirectory(file,new File(dest.getPath() + relativePath));
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        cl = this.getClass().getClassLoader();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);

        GBeanData serverInfo = bootstrap.addGBean("ServerInfo", BasicServerInfo.GBEAN_INFO);
        serverInfo.setAttribute("baseDirectory", ".");

        AbstractName configStoreName = bootstrap.addGBean("MockConfigurationStore", MockConfigStore.GBEAN_INFO).getAbstractName();

        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());

        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", EditableKernelConfigurationManager.GBEAN_INFO);
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
        configurationManagerData.setReferencePattern("Stores", configStoreName);
        bootstrap.addGBean(configurationManagerData);

        GBeanData serverData = new GBeanData(serverName, J2EEServerImpl.GBEAN_INFO);
        bootstrap.addGBean(serverData);

        GBeanData securityService = bootstrap.addGBean("SecurityService", SecurityServiceImpl.GBEAN_INFO);
        securityService.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");
        securityService.setAttribute("policyProvider", "org.apache.geronimo.security.jacc.GeronimoPolicy");
        securityService.setReferencePattern("ServerInfo", serverInfo.getAbstractName());

        // Default Realm
        Map initParams = new HashMap();

        initParams.put("userClassNames",
                "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        initParams.put("roleClassNames",
                "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
        GBeanData realm = bootstrap.addGBean("tomcatRealm", RealmGBean.GBEAN_INFO);
        realm.setAttribute("className",
                "org.apache.geronimo.tomcat.realm.TomcatJAASRealm");
        realm.setAttribute("initParams", initParams);

        // Default Host
        initParams = new HashMap();
        initParams.put("workDir", "work");
        initParams.put("name", "localhost");
        initParams.put("appBase", "");
        GBeanData host = bootstrap.addGBean("tomcatHost", HostGBean.GBEAN_INFO);
        host.setAttribute("className", "org.apache.catalina.core.StandardHost");
        host.setAttribute("initParams", initParams);

        // Default Engine
        initParams = new HashMap();
        initParams.put("name", "Geronimo");
        GBeanData engine = bootstrap.addGBean("tomcatEngine", EngineGBean.GBEAN_INFO);
        engine.setAttribute("className", "org.apache.geronimo.tomcat.TomcatEngine");
        engine.setAttribute("initParams", initParams);
        engine.setReferencePattern("DefaultHost", host.getAbstractName());
        engine.setReferencePattern("RealmGBean", realm.getAbstractName());
        engine.setReferencePattern("Hosts", host.getAbstractName());

        WebServiceBuilder webServiceBuilder = new UnavailableWebServiceBuilder();

        GBeanData containerData = bootstrap.addGBean("TomcatContainer", TomcatContainer.GBEAN_INFO);
        containerData.setAttribute("catalinaHome", new File(basedir, "target/var/catalina").toString());
        containerData.setReferencePattern("EngineGBean", engine.getAbstractName());
        containerData.setReferencePattern("ServerInfo", serverInfo.getAbstractName());
        AbstractName containerName = containerData.getAbstractName();

        GBeanData connector = bootstrap.addGBean("TomcatConnector", ConnectorGBean.GBEAN_INFO);
        connector.setAttribute("name", "HTTP");
        connector.setAttribute("port", new Integer(8181));
        connector.setReferencePattern("TomcatContainer", containerName);

        GBeanData tm = bootstrap.addGBean("TransactionManager", TransactionManagerImplGBean.GBEAN_INFO);
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));

        GBeanData tcm = bootstrap.addGBean("TransactionContextManager", TransactionContextManagerGBean.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tm.getAbstractName());
        tcmName = tcm.getAbstractName();
        ctcName = bootstrap.addGBean("ConnectionTrackingCoordinator", ConnectionTrackingCoordinatorGBean.GBEAN_INFO).getAbstractName();

        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, getClass().getClassLoader());

        configurationManager = ConfigurationUtil.getEditableConfigurationManager(kernel);
        configStore = (ConfigurationStore) kernel.getGBean(configStoreName);
        configStore.install(bootstrap);

        defaultEnvironment.addDependency(baseId, ImportType.ALL);
        defaultEnvironment.setConfigId(webModuleArtifact);
        builder = new TomcatModuleBuilder(defaultEnvironment, new AbstractNameQuery(containerName), Collections.singleton(webServiceBuilder), null);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }

    public static class MockConfigStore extends NullConfigurationStore {
        private Map configs = new HashMap();

        URL baseURL;

        public MockConfigStore() {
        }

        public MockConfigStore(URL baseURL) {
            this.baseURL = baseURL;
        }

        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
            configs.put(configurationData.getId(), configurationData);
        }

        public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
            configs.remove(configID);
        }

        public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            if (configs.containsKey(configId)) {
                ConfigurationData configurationData = (ConfigurationData) configs.get(configId);
                configurationData.setConfigurationStore(this);
                return configurationData;
            } else {
                ConfigurationData configurationData = new ConfigurationData(configId, naming);
                configurationData.setConfigurationStore(this);
                return configurationData;
            }
        }

        public boolean containsConfiguration(Artifact configID) {
            return true;
        }

        public Set resolve(Artifact configId, String moduleName, String pattern) throws NoSuchConfigException, MalformedURLException {
            return Collections.singleton(baseURL);
        }

        public final static GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, NameFactory.CONFIGURATION_STORE);
            infoBuilder.addInterface(ConfigurationStore.class);
            GBEAN_INFO = infoBuilder.getBeanInfo();
        }
    }

    private static class WebModuleInfo {
        AbstractName moduleName;
        Configuration configuration;

        public WebModuleInfo (AbstractName moduleName, Configuration configuration){
            this.moduleName = moduleName;
            this.configuration = configuration;
        }
    }

}
