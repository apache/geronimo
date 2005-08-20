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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.geronimo.axis.builder.AxisBuilder;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingContext;
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
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.tomcat.ConnectorGBean;
import org.apache.geronimo.tomcat.EngineGBean;
import org.apache.geronimo.tomcat.HostGBean;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;

/**
 * @version $Rev: 159325 $ $Date: 2005-03-28 15:53:03 -0700 (Mon, 28 Mar 2005) $
 */
public class TomcatModuleBuilderTest extends TestCase {

    protected Kernel kernel;

    private GBeanData container;

    private ObjectName containerName;

    private ObjectName connectorName;

    private GBeanData connector;

    private ObjectName engineName;

    private GBeanData engine;

    private ObjectName hostName;

    private GBeanData host;

    private ObjectName realmName;

    private GBeanData realm;

    private GBeanData securityServiceGBean;

    private ObjectName securityServiceName;

    private ObjectName serverInfoName;

    private GBeanData serverInfoGBean;

    private ObjectName tmName;

    private ObjectName ctcName;

    private GBeanData tm;

    private GBeanData ctc;

    private ObjectName tcmName;

    private GBeanData tcm;

    private ClassLoader cl;

    private J2eeContext moduleContext = new J2eeContextImpl("tomcat.test",
            "test", "null", NameFactory.WEB_MODULE, "Test", null, null);

    private TomcatModuleBuilder builder;

    private File basedir = new File(System.getProperty("basedir", "."));

    private URI parentId = URI.create("org/apache/geronimo/Foo");

    public void testDeployWar4() throws Exception {
        deployWar("war4", "org/apache/geronimo/test");
    }

    public void testDeployWar5() throws Exception {
        deployWar("war5", "hello");
    }

    public void deployWar(String warName, String name) throws Exception {

        File outputPath = new File(basedir,
                "target/test-resources/deployables/" + warName);
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        File path = new File(basedir, "src/test-resources/deployables/" + warName);
        File dest = new File(basedir, "target/test-resources/deployables/" + warName + "/war");
        recursiveCopy(path, dest);
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile);
        URI id = new URI(warName);

        ObjectName jaccBeanName = NameFactory.getComponentName(null, null, null, null, "foo", NameFactory.JACC_MANAGER, moduleContext);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        PermissionCollection excludedPermissions= new Permissions();
        PermissionCollection uncheckedPermissions= new Permissions();
        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, new HashMap());
        Map contextIDToPermissionsMap = new HashMap();
        contextIDToPermissionsMap.put("test_J2EEApplication=null_J2EEServer=bar_j2eeType=WebModule_name=org/apache/geronimo/test", componentPermissions);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        jaccBeanData.setAttribute("principalRoleMap", new HashMap());
        jaccBeanData.setAttribute("roleDesignates", new HashMap());
        start(jaccBeanData);

        EARContext earContext = createEARContext(outputPath, id);
        earContext.setJaccManagerName(jaccBeanName);
        ObjectName serverName = earContext.getServerObjectName();
        GBeanData server = new GBeanData(serverName, J2EEServerImpl.GBEAN_INFO);
        start(server);
        builder.initContext(earContext, module, cl);
        builder.addGBeans(earContext, module, cl);
        earContext.close();
        module.close();
        GBeanData configData = earContext.getConfigurationGBeanData();
        configData.setAttribute("baseURL", path.toURL());
        kernel.loadGBean(configData, cl);

        kernel.startRecursiveGBean(configData.getName());
        if (kernel.getGBeanState(configData.getName()) != State.RUNNING_INDEX) {
            fail("gbean not started: " + configData.getName());
        }

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(ObjectName.getInstance("test:J2EEApplication=null,J2EEServer=bar,j2eeType=WebModule,name=" + name)));

        Set names = kernel.listGBeans(ObjectName.getInstance("test:J2EEApplication=null,J2EEServer=bar,*"));
        System.out.println("Object names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            System.out.println("STATE: " + kernel.getGBeanState(objectName) + " - " + objectName.getCanonicalName());
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }

        //If we got here with no errors, then Tomcat deployed the war and loaded the classes

        kernel.stopGBean(configData.getName());
        kernel.unloadGBean(configData.getName());

        kernel.loadGBean(configData, cl);
        kernel.startRecursiveGBean(configData.getName());
        kernel.stopGBean(configData.getName());
        kernel.unloadGBean(configData.getName());
    }

    private EARContext createEARContext(File outputPath, URI id)
            throws MalformedObjectNameException, DeploymentException {
        EARContext earContext = new EARContext(outputPath, id,
                ConfigurationModuleType.WAR, parentId, kernel, moduleContext
                        .getJ2eeApplicationName(), tcmName, ctcName, null,
                null, null, new RefContext(new EJBReferenceBuilder() {

                    public Reference createEJBLocalReference(String objectName,
                                                             GBeanData gbeanData, boolean isSession, String localHome, String local)
                            throws DeploymentException {
                        return null;
                    }

                    public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home,
                                                              String remote) throws DeploymentException {
                        return null;
                    }

                    public Reference createCORBAReference(URI corbaURL,
                                                          String objectName, ObjectName containerName,
                                                          String home) throws DeploymentException {
                        return null;
                    }

                    public Object createHandleDelegateReference() {
                        return null;
                    }

                    public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) throws DeploymentException {
                        return null;
                    }

                    public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) throws DeploymentException {
                        return null;
                    }
                }, new ResourceReferenceBuilder() {

                    public Reference createResourceRef(String containerId,
                                                       Class iface) throws DeploymentException {
                        return null;
                    }

                    public Reference createAdminObjectRef(String containerId,
                                                          Class iface) throws DeploymentException {
                        return null;
                    }

                    public ObjectName locateResourceName(ObjectName query)
                            throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateActivationSpecInfo(
                            GBeanData resourceAdapterModuleData,
                            String messageListenerInterface)
                            throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateResourceAdapterGBeanData(
                            GBeanData resourceAdapterModuleData)
                            throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateAdminObjectInfo(
                            GBeanData resourceAdapterModuleData,
                            String adminObjectInterfaceName)
                            throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateConnectionFactoryInfo(
                            GBeanData resourceAdapterModuleData,
                            String connectionFactoryInterfaceName)
                            throws DeploymentException {
                        return null;
                    }
                }, new ServiceReferenceBuilder() {
                    //it could return a Service or a Reference, we don't care
                    public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
                        return null;
                    }
                }, kernel));
        return earContext;
    }

    private void recursiveDelete(File path) {
        // does not delete top level dir passed in
        File[] listing = path.listFiles();
        for (int i = 0; i < ((listing == null) ? 0 : listing.length); i++) {
            File file = listing[i];
            if (file.isDirectory()) {
                recursiveDelete(file);
            }
            file.delete();
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
        cl = this.getClass().getClassLoader();
        containerName = NameFactory.getWebComponentName(null, null, null, null,
                "tomcatContainer", "WebResource", moduleContext);
        connectorName = NameFactory.getWebComponentName(null, null, null, null,
                "tomcatConnector", "WebResource", moduleContext);
        realmName = NameFactory.getWebComponentName(null, null, null, null,
                "tomcatRealm", "WebResource", moduleContext);
        engineName = NameFactory.getWebComponentName(null, null, null, null,
                "tomcatEngine", "WebResource", moduleContext);
        hostName = NameFactory.getWebComponentName(null, null, null, null,
                "tomcatHost", "WebResource", moduleContext);

        tmName = NameFactory.getComponentName(null, null, null, null, null,
                "TransactionManager", NameFactory.JTA_RESOURCE, moduleContext);
        tcmName = NameFactory.getComponentName(null, null, null, null, null,
                "TransactionContextManager", NameFactory.JTA_RESOURCE,
                moduleContext);
        ctcName = new ObjectName(
                "geronimo.server:role=ConnectionTrackingCoordinator");

        kernel = KernelFactory.newInstance().createKernel("foo");
        kernel.boot();

        GBeanData store = new GBeanData(JMXUtil
                .getObjectName("foo:j2eeType=ConfigurationStore,name=mock"),
                MockConfigStore.GBEAN_INFO);
        kernel.loadGBean(store, this.getClass().getClassLoader());
        kernel.startGBean(store.getName());

        ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
        kernel.startGBean(configurationManagerName);
        ConfigurationManager configurationManager = (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);

        ObjectName baseConfigName = configurationManager.load(parentId);
        kernel.startGBean(baseConfigName);

        serverInfoName = new ObjectName("geronimo.system:name=ServerInfo");
        serverInfoGBean = new GBeanData(serverInfoName, BasicServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");
        start(serverInfoGBean);

        // install the policy configuration factory
        securityServiceName = new ObjectName("foo:j2eeType=SecurityService");
        securityServiceGBean = new GBeanData(securityServiceName,
                SecurityServiceImpl.GBEAN_INFO);
        securityServiceGBean.setReferencePattern("ServerInfo", serverInfoName);
        securityServiceGBean
                .setAttribute("policyConfigurationFactory",
                        "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");
        securityServiceGBean.setAttribute("policyProvider",
                "org.apache.geronimo.security.jacc.GeronimoPolicy");
        start(securityServiceGBean);

        WebServiceBuilder webServiceBuilder = new AxisBuilder();

        builder = new TomcatModuleBuilder(new URI("null"), false, containerName, webServiceBuilder, null);

        // Default Realm
        Map initParams = new HashMap();

        initParams.put("userClassNames",
                        "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        initParams.put("roleClassNames",
                        "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
        realm = new GBeanData(realmName, RealmGBean.GBEAN_INFO);
        realm.setAttribute("className",
                "org.apache.geronimo.tomcat.realm.TomcatJAASRealm");
        realm.setAttribute("initParams", initParams);
        start(realm);

        // Default Host
        initParams.clear();
        initParams.put("workDir", "work");
        initParams.put("name", "localhost");
        initParams.put("appBase", "");
        host = new GBeanData(hostName, HostGBean.GBEAN_INFO);
        host.setAttribute("className", "org.apache.catalina.core.StandardHost");
        host.setAttribute("initParams", initParams);
        start(host);

        // Default Engine
        initParams.clear();
        initParams.put("name", "Geronimo");
        initParams.put("defaultHost", "localhost");
        engine = new GBeanData(engineName, EngineGBean.GBEAN_INFO);
        engine.setAttribute("className", "org.apache.geronimo.tomcat.TomcatEngine");
        engine.setAttribute("initParams", initParams);
        engine.setReferencePattern("RealmGBean", realmName);
        engine.setReferencePattern("Hosts", hostName);
        start(engine);

        container = new GBeanData(containerName, TomcatContainer.GBEAN_INFO);
        container.setAttribute("classLoader", cl);
        container.setAttribute("catalinaHome", "target/var/catalina");
        container.setReferencePattern("EngineGBean", engineName);
        container.setReferencePattern("ServerInfo", serverInfoName);

        connector = new GBeanData(connectorName, ConnectorGBean.GBEAN_INFO);
        connector.setAttribute("name", "HTTP");
        connector.setAttribute("port", new Integer(8080));
        connector.setReferencePattern("TomcatContainer", containerName);

        start(container);
        start(connector);

        tm = new GBeanData(tmName, TransactionManagerImplGBean.GBEAN_INFO);
        Set patterns = new HashSet();
        patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));
        tm.setReferencePatterns("ResourceManagers", patterns);
        start(tm);
        tcm = new GBeanData(tcmName, TransactionContextManagerGBean.GBEAN_INFO);
        tcm.setReferencePattern("TransactionManager", tmName);
        start(tcm);
        ctc = new GBeanData(ctcName, ConnectionTrackingCoordinatorGBean.GBEAN_INFO);
        start(ctc);

    }

    protected void tearDown() throws Exception {
        stop(ctcName);
        stop(tmName);
        stop(serverInfoName);
        stop(securityServiceName);
        stop(connectorName);
        stop(containerName);
        kernel.shutdown();
    }

    private void start(GBeanData gbeanData) throws Exception {
        kernel.loadGBean(gbeanData, cl);
        kernel.startGBean(gbeanData.getName());
        if (kernel.getGBeanState(gbeanData.getName()) != State.RUNNING_INDEX) {
            fail("gbean not started: " + gbeanData.getName());
        }
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
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

        public List listConfiguations() {
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
