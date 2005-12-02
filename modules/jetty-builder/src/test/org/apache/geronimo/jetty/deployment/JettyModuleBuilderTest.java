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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.UnavailableWebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.jetty.JettyContainerImpl;
import org.apache.geronimo.jetty.connector.HTTPConnector;
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
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.transaction.context.TransactionContextManagerGBean;
import org.apache.geronimo.transaction.manager.TransactionManagerImplGBean;

/**
 * @version $Rev$ $Date$
 */
public class JettyModuleBuilderTest extends TestCase {

    protected Kernel kernel;
    private GBeanData container;
    private ObjectName containerName;
    private ObjectName connectorName;
    private GBeanData connector;
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
    private J2eeContext moduleContext = new J2eeContextImpl("jetty.test", "test", "null", NameFactory.WEB_MODULE, "jettyTest", null, null);
    private JettyModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));
    private List parentId = Arrays.asList(new URI[] {URI.create("org/apache/geronimo/Foo")});

    public void testDeployWar4() throws Exception {
        File outputPath = new File(basedir, "target/test-resources/deployables/war4");
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        File path = new File(basedir, "src/test-resources/deployables/war4");
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile);
        URI id = new URI("war4");
        EARContext earContext = createEARContext(outputPath, id);
        ObjectName serverName = earContext.getServerObjectName();
        GBeanData server = new GBeanData(serverName, J2EEServerImpl.GBEAN_INFO);
        start(server);
        builder.initContext(earContext, module, cl);
        builder.addGBeans(earContext, module, cl);
        earContext.close();
        module.close();

        GBeanData configData = earContext.getConfigurationGBeanData();
        ObjectName configName = configData.getName();
        configData.setAttribute("baseURL", path.toURL());
        kernel.loadGBean(configData, cl);
        kernel.startGBean(configName);
        kernel.invoke(configName, "loadGBeans", new Object[] {null}, new String[] {ManageableAttributeStore.class.getName()});
        kernel.invoke(configName, "startRecursiveGBeans");
        if (kernel.getGBeanState(configData.getName()) != State.RUNNING_INDEX) {
            fail("gbean not started: " + configData.getName());
        }
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(ObjectName.getInstance("test:J2EEApplication=null,J2EEServer=bar,j2eeType=WebModule,name=war4")));
        Set names = kernel.listGBeans(ObjectName.getInstance("test:J2EEApplication=null,J2EEServer=bar,WebModule=war4,*"));
        System.out.println("Object names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }
        GBeanData filterMapping2Data = kernel.getGBeanData(ObjectName.getInstance("test:J2EEApplication=null,J2EEServer=bar,Servlet=Servlet1,WebFilter=Filter2,WebModule=war4,j2eeType=WebFilterMapping"));
//        assertEquals(Collections.singleton(ObjectName.getInstance("test:J2EEApplication=null,J2EEServer=bar,Servlet=Servlet1,WebFilter=Filter1,WebModule=war4,j2eeType=WebFilterMapping")), filterMapping2Data.getReferencePatterns("Previous"));

        kernel.stopGBean(configName);
        kernel.unloadGBean(configName);


        //what is this testing?
        kernel.loadGBean(configData, cl);
        kernel.startGBean(configName);
        kernel.invoke(configName, "loadGBeans", new Object[] {null}, new String[] {ManageableAttributeStore.class.getName()});
        kernel.invoke(configName, "startRecursiveGBeans");
        kernel.stopGBean(configName);
        kernel.unloadGBean(configName);
    }

    private EARContext createEARContext(File outputPath, URI id) throws MalformedObjectNameException, DeploymentException {
        EARContext earContext = new EARContext(outputPath,
                id,
                ConfigurationModuleType.WAR,
                parentId,
                kernel,
                moduleContext.getJ2eeApplicationName(),
                tcmName,
                ctcName,
                null,
                null,
                null, new RefContext(new EJBReferenceBuilder() {

                    public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) throws DeploymentException {
                        return null;
                    }

                    public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) throws DeploymentException {
                        return null;
                    }

                    public Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) throws DeploymentException {
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
                },
                new ResourceReferenceBuilder() {

                    public Reference createResourceRef(String containerId, Class iface) throws DeploymentException {
                        return null;
                    }

                    public Reference createAdminObjectRef(String containerId, Class iface) throws DeploymentException {
                        return null;
                    }

                    public ObjectName locateResourceName(ObjectName query) throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateActivationSpecInfo(GBeanData resourceAdapterModuleData, String messageListenerInterface) throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) throws DeploymentException {
                        return null;
                    }

                    public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) throws DeploymentException {
                        return null;
                    }
                },
                new ServiceReferenceBuilder() {
                    //it could return a Service or a Reference, we don't care
                    public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
                        return null;
                    }
                }, kernel));
                return earContext;
    }

    private void recursiveDelete(File path) {
        //does not delete top level dir passed in
        File[] listing = path.listFiles();
        for (int i = 0; i < ((listing == null) ? 0 : listing.length); i++) {
            File file = listing[i];
            if (file.isDirectory()) {
                recursiveDelete(file);
            }
            file.delete();
        }
    }

    protected void setUp() throws Exception {
        cl = this.getClass().getClassLoader();
        containerName = NameFactory.getWebComponentName(null, null, null, null, "jettyContainer", "WebResource", moduleContext);
        connectorName = NameFactory.getWebComponentName(null, null, null, null, "jettyConnector", "WebResource", moduleContext);
        //        webModuleName = NameFactory.getWebComponentName(null, null, null, null, NameFactory.WEB_MODULE, "WebResource", moduleContext);

        tmName = NameFactory.getComponentName(null, null, null, null, null, "TransactionManager", NameFactory.TRANSACTION_MANAGER, moduleContext);
        tcmName = NameFactory.getComponentName(null, null, null, null, null, "TransactionContextManager", NameFactory.TRANSACTION_CONTEXT_MANAGER, moduleContext);
        ctcName = new ObjectName("geronimo.test:role=ConnectionTrackingCoordinator");

        kernel = KernelFactory.newInstance().createKernel("foo");
        kernel.boot();

        GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
        kernel.loadGBean(store, this.getClass().getClassLoader());
        kernel.startGBean(store.getName());

        ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
        kernel.startGBean(configurationManagerName);
        ConfigurationManager configurationManager = (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);

        configurationManager.load((URI) parentId.get(0));
        configurationManager.loadGBeans((URI) parentId.get(0));
        configurationManager.start((URI) parentId.get(0));

        Collection defaultServlets = new HashSet();
        Collection defaultFilters = new HashSet();
        Collection defaultFilterMappings = new HashSet();
        Object pojoWebServiceTemplate = null;
        WebServiceBuilder webServiceBuilder = new UnavailableWebServiceBuilder();

        serverInfoName = new ObjectName("geronimo.system:name=ServerInfo");
        serverInfoGBean = new GBeanData(serverInfoName, BasicServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");
        start(serverInfoGBean);

        //install the policy configuration factory
        securityServiceName = new ObjectName("foo:j2eeType=SecurityService");
        securityServiceGBean = new GBeanData(securityServiceName, SecurityServiceImpl.GBEAN_INFO);
        securityServiceGBean.setReferencePattern("ServerInfo", serverInfoName);
        securityServiceGBean.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");
        securityServiceGBean.setAttribute("policyProvider", "org.apache.geronimo.security.jacc.GeronimoPolicy");
        start(securityServiceGBean);


        builder = new JettyModuleBuilder(new URI[] {new URI("null")}, new Integer(1800), false, Collections.EMPTY_LIST, containerName, defaultServlets, defaultFilters, defaultFilterMappings, pojoWebServiceTemplate, webServiceBuilder, null, kernel);

        container = new GBeanData(containerName, JettyContainerImpl.GBEAN_INFO);

        connector = new GBeanData(connectorName, HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setAttribute("maxThreads", new Integer(50));
        connector.setAttribute("minThreads", new Integer(10));
        connector.setReferencePattern("JettyContainer", containerName);

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
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, NameFactory.CONFIGURATION_STORE);
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
