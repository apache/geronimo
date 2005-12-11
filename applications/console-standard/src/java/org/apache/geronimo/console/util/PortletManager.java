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
package org.apache.geronimo.console.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.management.J2EEDomain;
import org.apache.geronimo.management.geronimo.EJBManager;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.pool.GeronimoExecutor;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.system.logging.SystemLog;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderResponse;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class PortletManager {
    private final static Log log = LogFactory.getLog(PortletManager.class);
    // The following are currently static due to having only one server/JVM/etc. per Geronimo
    private final static String HELPER_KEY = "org.apache.geronimo.console.ManagementHelper";
    private final static String DOMAIN_KEY = "org.apache.geronimo.console.J2EEDomain";
    private final static String SERVER_KEY = "org.apache.geronimo.console.J2EEServer";
    private final static String JVM_KEY = "org.apache.geronimo.console.JVM";
    private final static String SYSTEM_LOG_KEY = "org.apache.geronimo.console.SystemLog";
    // The following may change based on the user's selections
        // nothing yet

    private static ManagementHelper createHelper() {
        //todo: consider making this configurable; we could easily connect to a remote kernel if we wanted to
        Kernel kernel = null;
        try {
            kernel = (Kernel) new InitialContext().lookup("java:comp/GeronimoKernel");
        } catch (NamingException e) {
//            log.error("Unable to look up kernel in JNDI", e);
        }
        if(kernel == null) {
            log.debug("Unable to find kernel in JNDI; using KernelRegistry instead");
            kernel = KernelRegistry.getSingleKernel();
        }
        return new KernelManagementHelper(kernel);
    }

    public static DeploymentManager getDeploymentManager(PortletRequest request) {
        DeploymentFactoryImpl factory = new DeploymentFactoryImpl();
        try {
            return factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
        } catch (DeploymentManagerCreationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ManagementHelper getManagementHelper(PortletRequest request) {
        ManagementHelper helper = (ManagementHelper) request.getPortletSession(true).getAttribute(HELPER_KEY, PortletSession.APPLICATION_SCOPE);
        if(helper == null) {
            helper = createHelper();
            request.getPortletSession().setAttribute(HELPER_KEY, helper, PortletSession.APPLICATION_SCOPE);
        }
        return helper;
    }

    public static ManagementHelper getManagementHelper(HttpSession session) {
        ManagementHelper helper = (ManagementHelper) session.getAttribute(HELPER_KEY);
        if(helper == null) {
            helper = createHelper();
            session.setAttribute(HELPER_KEY, helper);
        }
        return helper;
    }

    public static J2EEDomain getCurrentDomain(PortletRequest request) {
        J2EEDomain domain = (J2EEDomain) request.getPortletSession(true).getAttribute(DOMAIN_KEY, PortletSession.APPLICATION_SCOPE);
        if(domain == null) {
            domain = getManagementHelper(request).getDomains()[0]; //todo: some day, select a domain
            request.getPortletSession().setAttribute(DOMAIN_KEY, domain, PortletSession.APPLICATION_SCOPE);
        }
        return domain;

    }

    public static J2EEServer getCurrentServer(PortletRequest request) {
        J2EEServer server = (J2EEServer) request.getPortletSession(true).getAttribute(SERVER_KEY, PortletSession.APPLICATION_SCOPE);
        if(server == null) {
            ManagementHelper helper = getManagementHelper(request);
            server = helper.getServers(getCurrentDomain(request))[0]; //todo: some day, select a server from the domain
            request.getPortletSession().setAttribute(SERVER_KEY, server, PortletSession.APPLICATION_SCOPE);
        }
        else {
            // to do     handle "should not occur" error   - message?
        }
        return server;
    }

    public static JVM getCurrentJVM(PortletRequest request) {
        JVM jvm = (JVM) request.getPortletSession(true).getAttribute(JVM_KEY, PortletSession.APPLICATION_SCOPE);
        if(jvm == null) {
            ManagementHelper helper = getManagementHelper(request);
            jvm = helper.getJavaVMs(getCurrentServer(request))[0]; //todo: some day, select a JVM from the server
            request.getPortletSession().setAttribute(JVM_KEY, jvm, PortletSession.APPLICATION_SCOPE);
        }
        return jvm;
    }

    public static Repository[] getRepositories(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getRepositories(getCurrentServer(request));
    }

    public static SecurityRealm[] getSecurityRealms(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getSecurityRealms(getCurrentServer(request));
    }

    public static ServerInfo getServerInfo(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getServerInfo(getCurrentServer(request));
    }

    public static void testLoginModule(PortletRequest request, LoginModule module, Map options) {
        ManagementHelper helper = getManagementHelper(request);
        helper.testLoginModule(getCurrentServer(request), module, options);
    }

    public static Subject testLoginModule(PortletRequest request, LoginModule module, Map options, String username, String password) throws LoginException {
        ManagementHelper helper = getManagementHelper(request);
        return helper.testLoginModule(getCurrentServer(request), module, options, username, password);
    }

    public static ListableRepository[] getListableRepositories(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        Repository[] list = helper.getRepositories(getCurrentServer(request));
        List result = new ArrayList();
        for (int i = 0; i < list.length; i++) {
            Repository repository = list[i];
            if(repository instanceof ListableRepository) {
                result.add(repository);
            }
        }
        return (ListableRepository[]) result.toArray(new ListableRepository[result.size()]);
    }

    public static WriteableRepository[] getWritableRepositories(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        Repository[] list = helper.getRepositories(getCurrentServer(request));
        List result = new ArrayList();
        for (int i = 0; i < list.length; i++) {
            Repository repository = list[i];
            if(repository instanceof WriteableRepository) {
                result.add(repository);
            }
        }
        return (WriteableRepository[]) result.toArray(new WriteableRepository[result.size()]);
    }

    public static ResourceAdapterModule[] getOutboundRAModules(PortletRequest request, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundRAModules(getCurrentServer(request), iface);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesOfType(PortletRequest request, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(getCurrentServer(request), iface);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, String resourceAdapterModuleName) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories((ResourceAdapterModule)helper.getObject(resourceAdapterModuleName));
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, String resourceAdapterModuleName, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories((ResourceAdapterModule)helper.getObject(resourceAdapterModuleName), iface);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, ResourceAdapterModule module) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(module);
    }

    public static JCAManagedConnectionFactory[] getOutboundFactoriesForRA(PortletRequest request, ResourceAdapterModule module, String iface) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getOutboundFactories(module, iface);
    }

    public static String[] getWebManagerNames(PortletRequest request) {
        return getCurrentServer(request).getWebManagers();
    }

    public static WebManager[] getWebManagers(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getWebManagers(getCurrentServer(request));
    }

    public static WebManager getWebManager(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        return (WebManager) helper.getObject(managerObjectName);
    }

    public static String[] getWebContainerNames(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        return manager.getContainers();
    }

    public static WebAccessLog getWebAccessLog(PortletRequest request, String managerObjectName, String containerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        return helper.getWebAccessLog(manager, containerObjectName);
    }

    public static WebContainer getWebContainer(PortletRequest request, String containerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        return (WebContainer) helper.getObject(containerObjectName);
    }

    public static WebConnector createWebConnector(PortletRequest request, String managerObjectName, String containerObjectName, String name, String protocol, String host, int port) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        String objectName = manager.addConnector(containerObjectName, name, protocol, host, port);
        return (WebConnector)helper.getObject(objectName);
    }

    public static WebConnector[] getWebConnectors(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        return helper.getWebConnectors(manager);
    }

    public static WebConnector[] getWebConnectors(PortletRequest request, String managerObjectName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        return helper.getWebConnectors(manager, protocol);
    }

    public static WebConnector[] getWebConnectorsForContainer(PortletRequest request, String managerObjectName, String containerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        return helper.getWebConnectorsForContainer(manager, containerObjectName);
    }

    public static WebConnector[] getWebConnectorsForContainer(PortletRequest request, String managerObjectName, String containerObjectName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        WebManager manager = (WebManager) helper.getObject(managerObjectName);
        return helper.getWebConnectorsForContainer(manager, containerObjectName, protocol);
    }

    public static EJBManager[] getEJBManagers(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getEJBManagers(getCurrentServer(request));
    }

    public static EJBManager getEJBManager(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        return (EJBManager) helper.getObject(managerObjectName);
    }

    public static String[] getJMSManagerNames(PortletRequest request) {
        return getCurrentServer(request).getJMSManagers();
    }

    public static JMSManager getJMSManager(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        return (JMSManager) helper.getObject(managerObjectName);
    }

    public static String[] getJMSBrokerNames(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerObjectName);
        return manager.getContainers();
    }

    public static JMSBroker getJMSBroker(PortletRequest request, String brokerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        return (JMSBroker) helper.getObject(brokerObjectName);
    }

    public static JMSConnector createJMSConnector(PortletRequest request, String managerObjectName, String containerObjectName, String name, String protocol, String host, int port) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerObjectName);
        String objectName = manager.addConnector(containerObjectName, name, protocol, host, port);
        return (JMSConnector)helper.getObject(objectName);
    }

    public static JMSConnector[] getJMSConnectors(PortletRequest request, String managerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerObjectName);
        return helper.getJMSConnectors(manager);
    }

    public static JMSConnector[] getJMSConnectors(PortletRequest request, String managerObjectName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerObjectName);
        return helper.getJMSConnectors(manager, protocol);
    }

    public static JMSConnector[] getJMSConnectorsForContainer(PortletRequest request, String managerObjectName, String brokerObjectName) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerObjectName);
        return helper.getJMSConnectorsForContainer(manager, brokerObjectName);
    }

    public static JMSConnector[] getJMSConnectorsForContainer(PortletRequest request, String managerObjectName, String brokerObjectName, String protocol) {
        ManagementHelper helper = getManagementHelper(request);
        JMSManager manager = (JMSManager) helper.getObject(managerObjectName);
        return helper.getJMSConnectorsForContainer(manager, brokerObjectName, protocol);
    }

    public static GeronimoExecutor[] getThreadPools(PortletRequest request) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getThreadPools(getCurrentServer(request));
    }

    public static String getGBeanDescription(PortletRequest request, String objectName) {
        ManagementHelper helper = getManagementHelper(request);
        return helper.getGBeanDescription(objectName);
    }

    public static SystemLog getCurrentSystemLog(PortletRequest request) {
        SystemLog log = (SystemLog) request.getPortletSession(true).getAttribute(SYSTEM_LOG_KEY, PortletSession.APPLICATION_SCOPE);
        if(log == null) {
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

    public static GeronimoManagedBean getManagedBean(PortletRequest request, String name) {
        ManagementHelper helper = getManagementHelper(request);
        return (GeronimoManagedBean) helper.getObject(name);
    }

    /**
     * Looks up the context prefix used by the portal, even if the thing running
     * is in one of the portlets.  We're kind of hacking our way there, but hey,
     * it beats hardcoding.
     */
    public static String getConsoleFrameworkServletPath (HttpServletRequest request) {
        String contextPath = "";
        Object o = request.getAttribute("javax.portlet.response");
        if (o!=null && o instanceof RenderResponse) { // request came from a portlet
            RenderResponse renderResponse = (RenderResponse)o;
            contextPath = renderResponse.createRenderURL().toString();
            int index = contextPath.indexOf(request.getPathInfo());
            contextPath = contextPath.substring(0,index);
        } else { // request did not come from a portlet
            contextPath = request.getContextPath();
        }
        return contextPath;
    }
}
