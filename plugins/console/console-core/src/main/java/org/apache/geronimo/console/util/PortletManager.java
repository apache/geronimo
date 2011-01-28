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
package org.apache.geronimo.console.util;

import java.io.File;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.geronimo.J2EEDomain;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.logging.SystemLog;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class PortletManager {
    private static final Logger log = LoggerFactory.getLogger(PortletManager.class);
    // The following are currently static due to having only one server/JVM/etc. per Geronimo
    private final static String HELPER_KEY = "org.apache.geronimo.console.ManagementHelper";
    private final static String DOMAIN_KEY = "org.apache.geronimo.console.J2EEDomain";
    private final static String SERVER_KEY = "org.apache.geronimo.console.J2EEServer";
    private final static String JVM_KEY = "org.apache.geronimo.console.JVM";
    private final static String SYSTEM_LOG_KEY = "org.apache.geronimo.console.SystemLog";

    private static BundleContext bundleContext;
    static {
        ClassLoader cl = PortletManager.class.getClassLoader();
        if (cl instanceof BundleReference) {
            bundleContext = ((BundleReference)cl).getBundle().getBundleContext();
        }
    }
    // The following may change based on the user's selections
    // nothing yet

    private static ManagementHelper createHelper() {
        Kernel kernel = getKernel();
        return new KernelManagementHelper(kernel);
    }

    public static Kernel getKernel() {
        //todo: consider making this configurable; we could easily connect to a remote kernel if we wanted to
        //TODO see GERONIMO-5782 this jndi lookup can cause deadlocks
        Kernel kernel = null;
        try {
            kernel = (Kernel) new InitialContext().lookup("java:comp/GeronimoKernel");
        } catch (NamingException e) {
//            log.error("Unable to look up kernel in JNDI", e);
        }
        if (kernel == null) {
            log.debug("Unable to find kernel in JNDI; using KernelRegistry instead");
            kernel = KernelRegistry.getSingleKernel();
        }
        return kernel;
    }

    public static ConfigurationManager getConfigurationManager() {
        if (bundleContext != null) {
            ServiceReference sr = bundleContext.getServiceReference(ConfigurationManager.class.getName());
            if (sr != null) {
                return (ConfigurationManager) bundleContext.getService(sr);
            }
        }
        try {
            return ConfigurationUtil.getConfigurationManager(getKernel());
        } catch (GBeanNotFoundException e) {
            return null;
        }
    }

    public static ManagementHelper getManagementHelper(PortletRequest request) {
        ManagementHelper helper = (ManagementHelper) request.getPortletSession(true).getAttribute(HELPER_KEY, PortletSession.APPLICATION_SCOPE);
        if (helper == null) {
            helper = createHelper();
            request.getPortletSession().setAttribute(HELPER_KEY, helper, PortletSession.APPLICATION_SCOPE);
        }
        return helper;
    }

    public static ManagementHelper getManagementHelper(HttpSession session) {
        ManagementHelper helper = (ManagementHelper) session.getAttribute(HELPER_KEY);
        if (helper == null) {
            helper = createHelper();
            session.setAttribute(HELPER_KEY, helper);
        }
        return helper;
    }

    public static J2EEDomain getCurrentDomain(PortletRequest request) {
        J2EEDomain domain = (J2EEDomain) request.getPortletSession(true).getAttribute(DOMAIN_KEY, PortletSession.APPLICATION_SCOPE);
        if (domain == null) {
            domain = getManagementHelper(request).getDomains()[0]; //todo: some day, select a domain
            request.getPortletSession().setAttribute(DOMAIN_KEY, domain, PortletSession.APPLICATION_SCOPE);
        }
        return domain;

    }

    public static J2EEServer getCurrentServer(PortletRequest request) {
        J2EEServer server = (J2EEServer) request.getPortletSession(true).getAttribute(SERVER_KEY, PortletSession.APPLICATION_SCOPE);
        if (server == null) {
            server = getCurrentDomain(request).getServerInstances()[0]; //todo: some day, select a server from the domain
            request.getPortletSession().setAttribute(SERVER_KEY, server, PortletSession.APPLICATION_SCOPE);
        } else {
            // to do     handle "should not occur" error   - message?
        }
        return server;
    }

    public static JVM getCurrentJVM(PortletRequest request) {
        JVM jvm = (JVM) request.getPortletSession(true).getAttribute(JVM_KEY, PortletSession.APPLICATION_SCOPE);
        if (jvm == null) {
            ManagementHelper helper = getManagementHelper(request);
            jvm = helper.getJavaVMs(getCurrentServer(request))[0]; //todo: some day, select a JVM from the server
            request.getPortletSession().setAttribute(JVM_KEY, jvm, PortletSession.APPLICATION_SCOPE);
        }
        return jvm;
    }

    public static void testLoginModule(PortletRequest request, LoginModule module, Map options) {
        ManagementHelper helper = getManagementHelper(request);
        helper.testLoginModule(getCurrentServer(request), module, options);
    }

    public static Subject testLoginModule(PortletRequest request, LoginModule module, Map options, String username, String password) throws LoginException {
        ManagementHelper helper = getManagementHelper(request);
        return helper.testLoginModule(getCurrentServer(request), module, options, username, password);
    }

    public static ResourceAdapterModule[] getOutboundRAModules(PortletRequest request, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundRAModules(getCurrentServer(request), iface);
    }

    public static ResourceAdapterModule[] getOutboundRAModules(PortletRequest request, String[] iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundRAModules(getCurrentServer(request), iface);
    }

    public static ResourceAdapterModule[] getAdminObjectModules(PortletRequest request, String[] ifaces) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getAdminObjectModules(getCurrentServer(request), ifaces);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesOfType(PortletRequest request, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(getCurrentServer(request), iface);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, AbstractName resourceAdapterModuleName) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories((ResourceAdapterModule) helper.getObject(resourceAdapterModuleName));
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, AbstractName resourceAdapterModuleName, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories((ResourceAdapterModule) helper.getObject(resourceAdapterModuleName), iface);
    }
    
    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, AbstractName resourceAdapterModuleName, String[] ifaces) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories((ResourceAdapterModule) helper.getObject(resourceAdapterModuleName), ifaces);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, ResourceAdapterModule module) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(module);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, ResourceAdapterModule module, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(module, iface);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, ResourceAdapterModule module, String[] iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(module, iface);
    }

    //todo: Create an interface for admin objects
    public static JCAAdminObject[] getAdminObjectsForRA(PortletRequest request, ResourceAdapterModule module, String[] ifaces) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getAdminObjects(module, ifaces);
    }

    public static WebManager[] getWebManagers(PortletRequest request) {
        return getCurrentServer(request).getWebManagers();
    }

    public static WebManager getWebManager(PortletRequest request, AbstractName managerName) {
        ManagementHelper helper = getManagementHelper(request);
        return (WebManager) helper.getObject(managerName);
    }

//    private static String[] namesToStrings(AbstractName[] names) {
//        String[] result = new String[names.length];
//        for (int i = 0; i < names.length; i++) {
//            AbstractName name = names[i];
//            result[i] = name.toURI().toString();
//        }
//        return result;
//    }
//

    public static WebAccessLog getWebAccessLog(PortletRequest request, AbstractName managerName, AbstractName containerName) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerName);
        return manager.getAccessLog((WebContainer) helper.getObject(containerName));
    }

    public static WebContainer getWebContainer(PortletRequest request, AbstractName containerName) {
        ManagementHelper helper = getManagementHelper(request);
        return (WebContainer) helper.getObject(containerName);
    }

    public static NetworkConnector[] getNetworkConnectors(PortletRequest request, AbstractName managerName) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerName);
        return manager.getConnectors();
    }

    public static NetworkConnector[] getNetworkConnectors(PortletRequest request, AbstractName managerName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerName);
        return manager.getConnectors(protocol);
    }

    public static NetworkConnector getNetworkConnector(PortletRequest request, AbstractName connectorName) {
        ManagementHelper helper = getManagementHelper(request);
        return (NetworkConnector) helper.getObject(connectorName);
    }

    public static NetworkConnector[] getNetworkConnectorsForContainer(PortletRequest request, AbstractName managerName, AbstractName containerName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerName);
        return manager.getConnectorsForContainer(containerName, protocol);
    }

    public static JMSBroker getJMSBroker(PortletRequest request, AbstractName brokerName) {
        ManagementHelper helper = getManagementHelper(request);
        return (JMSBroker) helper.getObject(brokerName);
    }

    public static JMSConnector createJMSConnector(PortletRequest request, JMSManager manager, AbstractName containerName, String name, String protocol, String host, int port) {
        return manager.addConnector(getJMSBroker(request, containerName), name, protocol, host, port);
    }

    public static JMSConnector[] getJMSConnectors(PortletRequest request, AbstractName managerName) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerName);
        return (JMSConnector[]) manager.getConnectors();
    }

    public static JMSConnector[] getJMSConnectors(PortletRequest request, AbstractName managerName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerName);
        return (JMSConnector[]) manager.getConnectors(protocol);
    }

    public static JMSConnector[] getJMSConnectorsForContainer(PortletRequest request, AbstractName managerName, AbstractName brokerName) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerName);
        return (JMSConnector[]) manager.getConnectorsForContainer(brokerName);
    }

    public static JMSConnector[] getJMSConnectorsForContainer(PortletRequest request, AbstractName managerName, AbstractName brokerName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerName);
        return (JMSConnector[]) manager.getConnectorsForContainer(brokerName, protocol);
    }

    public static ResourceAdapter[] getResourceAdapters(PortletRequest request, ResourceAdapterModule module) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getResourceAdapters(module);
    }

    public static JCAResource[] getJCAResources(PortletRequest request, ResourceAdapter adapter) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getRAResources(adapter);
    }

    public static String getGBeanDescription(PortletRequest request, AbstractName objectName) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getGBeanDescription(objectName);
    }

    public static SystemLog getCurrentSystemLog(PortletRequest request) {
        SystemLog log = (SystemLog) request.getPortletSession(true).getAttribute(SYSTEM_LOG_KEY, PortletSession.APPLICATION_SCOPE);
        if (log == null) {
            ManagementHelper helper = getManagementHelper(request);
            log = helper.getSystemLog(getCurrentJVM(request));
            request.getPortletSession().setAttribute(SYSTEM_LOG_KEY, log, PortletSession.APPLICATION_SCOPE);
        }
        return log;
    }

    public static GeronimoManagedBean[] getManagedBeans(PortletRequest request, Class intrface) {
        ManagementHelper helper = getManagementHelper(request);
        Object[] obs = helper.findByInterface(intrface);
        GeronimoManagedBean[] results = new GeronimoManagedBean[obs.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = (GeronimoManagedBean) obs[i];
        }
        return results;
    }

    public static GeronimoManagedBean getManagedBean(PortletRequest request, AbstractName name) {
        ManagementHelper helper = getManagementHelper(request);
        return (GeronimoManagedBean) helper.getObject(name);
    }

    public static Artifact getConfigurationFor(PortletRequest request, AbstractName objectName) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getConfigurationNameFor(objectName);
    }

    public static AbstractName getNameFor(PortletRequest request, Object component) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getNameFor(component);
    }

    public static File getRepositoryEntry(PortletRequest request, String repositoryURI) {
        J2EEServer server = getCurrentServer(request);
        Repository[] repos = server.getRepositories();
        Artifact uri = Artifact.create(repositoryURI);
        if (!uri.isResolved()) {
            Artifact[] all = server.getConfigurationManager().getArtifactResolver().queryArtifacts(uri);
            if (all.length == 0) {
                return null;
            } else {
                uri = all[all.length - 1];
            }
        }
        for (int i = 0; i < repos.length; i++) {
            Repository repo = repos[i];
            if (repo.contains(uri)) {
                return repo.getLocation(uri);
            }
        }
        return null;
    }

    public static Bundle getRepositoryEntryBundle(PortletRequest request, String repositoryURI) {
        J2EEServer server = getCurrentServer(request);
        Artifact uri = Artifact.create(repositoryURI);
        if (!uri.isResolved()) {
            Artifact[] all = server.getConfigurationManager().getArtifactResolver().queryArtifacts(uri);
            if (all.length == 0) {
                return null;
            } else {
                uri = all[all.length - 1];
            }
        }
        try {
            Kernel kernel = getKernel();
            BundleContext bundleContext = kernel.getBundleFor(kernel.getKernelName()).getBundleContext();
            //TODO Figure out who should be responsible for uninstalling it, and whether we need to start the bundle
            //Currently, this method is only used for resource reading, seems no need to start the bundle.
            return bundleContext.installBundle("mvn:" + uri.getGroupId() + "/" + uri.getArtifactId() + "/" + uri.getVersion() + ("jar".equals(uri.getType()) ? "" : "/" + uri.getType()));
        } catch (Exception e) {
            return null;
        }
    }

    public static J2EEDeployedObject getModule(PortletRequest request, Artifact configuration) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getModuleForConfiguration(configuration);
    }

    public static ConfigurationData[] getConfigurations(PortletRequest request, ConfigurationModuleType type, boolean includeChildModules) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getConfigurations(type, includeChildModules);
    }
    
    public static Object[] getGBeansImplementing(PortletRequest request, Class iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getGBeansImplementing(iface);
    }    

    /**
     * This methods adds a GBean to an existing configuration.
     * @param request PortletRequest object to get hold of ManagementHelper
     * @param configID  The configuration to add the GBean to.
     * @param gbean     The data representing the GBean to add.
     * @param start     If true, the GBean should be started as part of this call.
     */
    public static void addGBeanToConfiguration(PortletRequest request, Artifact configID, GBeanData gbean, boolean start) {
        ManagementHelper helper = getManagementHelper(request);
        helper.addGBeanToConfiguration(configID, gbean, start);
    }

}
