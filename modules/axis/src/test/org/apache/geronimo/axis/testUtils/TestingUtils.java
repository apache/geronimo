/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis.testUtils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.axis.AxisGeronimoUtils;
import org.apache.geronimo.axis.WSPlan;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.xbeans.j2ee.ServiceRefHandlerType;
//import org.openejb.deployment.OpenEJBModuleBuilder;

/**
 * @version $Rev: $ $Date: $
 */
public class TestingUtils {

    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";

    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");
    protected static J2EEManager j2eeManager = new J2EEManager();
    public static ResourceReferenceBuilder resourceReferenceBuilder = new ResourceReferenceBuilder() {

        public Reference createResourceRef(String containerId, Class iface) {
            return null;
        }

        public Reference createAdminObjectRef(String containerId, Class iface) {
            return null;
        }

        public ObjectName locateResourceName(ObjectName query) {
            return AxisGeronimoConstants.RESOURCE_ADAPTER_NAME;
        }

        public GBeanData locateActivationSpecInfo(ObjectName resourceAdapterModuleName, String messageListenerInterface) {
            return AxisGeronimoConstants.ACTIVATION_SPEC_INFO;
        }

        public GBeanData locateResourceAdapterGBeanData(ObjectName resourceAdapterModuleName) {
            return null;
        }

        public GBeanData locateAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName) {
            return null;
        }

        public GBeanData locateConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName) {
            return null;
        }
    };

    private static ServiceReferenceBuilder serviceReferenceBuilder = new ServiceReferenceBuilder() {

        //it could return a Service or a Reference, we don't care
        public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Map portLocationMap, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
            return null;
        }
    };


    public static void startJ2EEContainerAndAxisServlet(Kernel kernel) throws Exception {
        //This does the work need to be done by plan
        j2eeManager.startJ2EEContainer(kernel);
        //start the Axis Serverlet which would be started by the service plan
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        GBeanData app = new GBeanData(AxisGeronimoConstants.APPLICATION_NAME, JettyWebAppContext.GBEAN_INFO);
        URL url = classLoader.getResource("deployables/axis/");
        System.out.print(url);
        app.setAttribute("uri", URI.create(url.toString()));
        app.setAttribute("contextPath", "/axis");
        app.setAttribute("componentContext", Collections.EMPTY_MAP);
        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setAttribute("webClassPath", new URI[0]);
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setReferencePatterns("JettyContainer", Collections.singleton(AxisGeronimoConstants.WEB_CONTAINER_NAME));
        app.setAttribute("configurationBaseUrl", Thread.currentThread().getContextClassLoader().getResource("deployables/"));
        app.setReferencePattern("TransactionContextManager", AxisGeronimoConstants.TRANSACTION_CONTEXT_MANAGER_NAME);
        app.setReferencePattern("TrackedConnectionAssociator", AxisGeronimoConstants.TRACKED_CONNECTION_ASSOCIATOR_NAME);
        AxisGeronimoUtils.startGBean(app, kernel, classLoader);

    }

    public static void stopJ2EEContinerAndAxisServlet(Kernel kernel) throws Exception {
        j2eeManager.stopJ2EEContainer(kernel);
    }


    protected static EARConfigBuilder getEARConfigBuilder() throws Exception {
        URI defaultParentId = new URI("org/apache/geronimo/Server");
//        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(defaultParentId, null, null, null);
//
//       EARConfigBuilder earConfigBuilder =
//                new EARConfigBuilder(defaultParentId,
//                        transactionManagerObjectName,
//                        connectionTrackerObjectName,
//                        null,
//                        null,
//                        null,
//                        moduleBuilder,
//                        moduleBuilder,
//                        null,
//                        null,
//                        resourceReferenceBuilder,
//                        null,
//                        serviceReferenceBuilder,
//                        null);
//        return earConfigBuilder;
        return null;
    }

    public static void buildConfiguration(File jarfile,
            ConfigurationStore store,
            Kernel kernel,
            ObjectName wsConfgBuilderName) throws Exception {
        EARConfigBuilder earConfigBuilder = getEARConfigBuilder();

        ObjectName wsconf = new ObjectName("geronimo.test:name=" + jarfile.getName() + ",value=check");
        URI wsURI = new URI("new");

        WSPlan plan = null;

        Enumeration entires = new JarFile(jarfile).entries();
        while (entires.hasMoreElements()) {
            ZipEntry zipe = (ZipEntry) entires.nextElement();
            String name = zipe.getName();
            if (name.endsWith("/ejb-jar.xml")) {
                ObjectName ejbConfName = TestingUtils.installAndStartEJB(jarfile, store, earConfigBuilder, kernel);
                plan = WSPlan.createPlan(wsURI, wsconf, ejbConfName, jarfile);
                break;
            }
        }

        if (plan == null) {
            plan = WSPlan.createPlan(wsURI, wsconf, jarfile);
        }
        File wsinstallDir = store.createNewConfigurationDir();

        kernel.invoke(wsConfgBuilderName, "buildConfiguration",
                new Object[]{plan, null, wsinstallDir},
                new String[]{Object.class.getName(),
                             JarFile.class.getName(),
                             File.class.getName()});
        //wsconfBuilder.buildConfiguration(plan, null, wsinstallDir);
        
        URI wsInstalledURI = store.install(wsinstallDir);
        GBeanData config = store.getConfiguration(wsInstalledURI);
        ConfigurationManager configurationManager = kernel.getConfigurationManager();
        ObjectName configName = configurationManager.load(config, null, Configuration.class.getClassLoader());
        kernel.startRecursiveGBean(configName);

    }

    public static ObjectName installAndStartEJB(File jarfile,
            ConfigurationStore store,
            EARConfigBuilder earConfigBuilder,
            Kernel kernel) throws Exception {

        JarFile module = new JarFile(jarfile);
        File unpackedDir = store.createNewConfigurationDir();
        System.out.println("\nHI HELLO" + unpackedDir.getAbsolutePath());
        
        //Install the EJB
        Object ejbplan = earConfigBuilder.getDeploymentPlan(null, module);
        earConfigBuilder.buildConfiguration(ejbplan, module, unpackedDir);
        URI ejbURI = store.install(unpackedDir);


        GBeanData config = store.getConfiguration(ejbURI);
        ConfigurationManager configurationManager = kernel.getConfigurationManager();
        ObjectName configName = configurationManager.load(config, store.getBaseURL(ejbURI), Thread.currentThread().getContextClassLoader());
        kernel.startRecursiveGBean(configName);
        return configName;
    }

}
