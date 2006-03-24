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
package org.apache.geronimo.jetty.deployment;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.deployment.DeploymentContext;
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
import org.apache.geronimo.jetty.JettyContainerImpl;
import org.apache.geronimo.jetty.connector.HTTPConnector;
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
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;

import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev:385232 $ $Date$
 */
public class JettyModuleBuilderTest extends TestCase {
    private String DOMAIN_NAME = "geronimo.test";
    private String SERVER_NAME = "geronimo";
    private String BASE_NAME = DOMAIN_NAME + ":J2EEServer=" + SERVER_NAME;

    private Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private final AbstractName serverName = naming.createRootName(baseId, "Server", "J2EEServer");

    protected Kernel kernel;
    private AbstractName ctcName;
    private AbstractName tcmName;
    private ClassLoader cl;
    private JettyModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));
    private Artifact webModuleArtifact = new Artifact("foo", "bar", "1", "car");
    private Environment defaultEnvironment = new Environment();
    private ConfigurationManager configurationManager;
    private ConfigurationStore configStore;

    public void testDeployWar4() throws Exception {
        File outputPath = new File(basedir, "target/test-resources/deployables/war4");
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        new File(outputPath, "war").mkdir();
        File path = new File(basedir, "src/test-resources/deployables/war4");
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile, kernel.getNaming());
        Repository repository = null;

        AbstractName moduleName = module.getModuleName();
        EARContext earContext = createEARContext(outputPath, defaultEnvironment, repository, configStore, moduleName);
        module.setEarContext(earContext);
        builder.initContext(earContext, module, cl);
        builder.addGBeans(earContext, module, cl, Collections.EMPTY_SET);
        ConfigurationData configurationData = earContext.getConfigurationData();
        earContext.close();
        module.close();

        Configuration configuration = configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configuration);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(moduleName));
        Set names = configuration.findGBeans(new AbstractNameQuery(moduleName.getArtifact(), Collections.EMPTY_MAP));
        System.out.println("names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName objectName = (AbstractName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }

        configurationManager.stopConfiguration(configuration);
        configurationManager.unloadConfiguration(configuration);

    }

    private EARContext createEARContext(File outputPath, Environment environment, Repository repository, ConfigurationStore configStore, AbstractName moduleName) throws DeploymentException {
        return new EARContext(outputPath,
                environment,
                ConfigurationModuleType.WAR,
                naming,
                repository,
                configStore,
                new AbstractNameQuery(serverName),
                moduleName,
                new AbstractNameQuery(tcmName),
                new AbstractNameQuery(ctcName),
                null,
                null,
                null,
                new RefContext(new EJBReferenceBuilder() {


                    public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) {
                        return null;
                    }

                    public Object createHandleDelegateReference() {
                        return null;
                    }

                    public Reference createEJBRemoteRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote, Configuration configuration) {
                        return null;
                    }

                    public Reference createEJBLocalRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local, Configuration configuration) {
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

    protected void setUp() throws Exception {
        defaultEnvironment.getProperties().put(NameFactory.JSR77_BASE_NAME_PROPERTY, BASE_NAME);
        cl = this.getClass().getClassLoader();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);

        bootstrap.addGBean("ServerInfo", BasicServerInfo.GBEAN_INFO).setAttribute("baseDirectory", ".");

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

        Collection defaultServlets = new HashSet();
        Collection defaultFilters = new HashSet();
        Collection defaultFilterMappings = new HashSet();
        Object pojoWebServiceTemplate = null;
        WebServiceBuilder webServiceBuilder = new UnavailableWebServiceBuilder();

        GBeanData containerData = bootstrap.addGBean("JettyContainer", JettyContainerImpl.GBEAN_INFO);
        AbstractName containerName = containerData.getAbstractName();

        GBeanData connector = bootstrap.addGBean("JettyConnector", HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setAttribute("maxThreads", new Integer(50));
        connector.setAttribute("minThreads", new Integer(10));
        connector.setReferencePattern("JettyContainer", containerName);

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
        builder = new JettyModuleBuilder(defaultEnvironment, new Integer(1800), false, Collections.EMPTY_LIST, new AbstractNameQuery(containerName), defaultServlets, defaultFilters, defaultFilterMappings, pojoWebServiceTemplate, webServiceBuilder, kernel);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }


    public static class MockConfigStore implements ConfigurationStore {
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

        public GBeanData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            AbstractName configurationObjectName = Configuration.getConfigurationAbstractName(configId);
            GBeanData configData = new GBeanData(configurationObjectName, Configuration.GBEAN_INFO);
            if (configs.containsKey(configId)) {
                ConfigurationData configurationData = (ConfigurationData) configs.get(configId);
                configData.setAttribute("moduleType", configurationData.getModuleType());
                Environment environment = configurationData.getEnvironment();
                configData.setAttribute("environment", environment);
                configData.setAttribute("gBeanState", Configuration.storeGBeans(configurationData.getGBeans()));
                configData.setAttribute("classPath", configurationData.getClassPath());

                ConfigurationResolver configurationResolver = new ConfigurationResolver(configurationData.getEnvironment().getConfigId(), this, Collections.EMPTY_SET, new DefaultArtifactResolver(null, Collections.EMPTY_SET));
                configData.setAttribute("configurationResolver", configurationResolver);

            } else {
                Environment environment = new Environment();
                environment.setConfigId(configId);
                environment.getProperties().put(NameFactory.JSR77_BASE_NAME_PROPERTY, "geronimo.test:J2EEServer=geronimo");
                configData.setAttribute("environment", environment);
                configData.setAttribute("moduleType", ConfigurationModuleType.WAR);
                configData.setAttribute("gBeanState", NO_OBJECTS_OS);
            }
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
            return null;
        }

        public URL resolve(Artifact configId, URI uri) throws NoSuchConfigException, MalformedURLException {
            return baseURL;
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
