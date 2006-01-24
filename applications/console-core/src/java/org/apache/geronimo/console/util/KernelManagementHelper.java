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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEDomain;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.JCAConnectionFactory;
import org.apache.geronimo.management.JDBCDataSource;
import org.apache.geronimo.management.JDBCDriver;
import org.apache.geronimo.management.JDBCResource;
import org.apache.geronimo.management.JMSResource;
import org.apache.geronimo.management.ResourceAdapter;
import org.apache.geronimo.management.Servlet;
import org.apache.geronimo.management.WebModule;
import org.apache.geronimo.management.geronimo.EJBConnector;
import org.apache.geronimo.management.geronimo.EJBManager;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
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
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.system.logging.SystemLog;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * An implementation of the ManagementHelper interface that uses a Geronimo
 * kernel. That may be an in-VM kernel or a remote kernel, we don't really
 * care.
 *
 * @version $Rev$ $Date$
 */
public class KernelManagementHelper implements ManagementHelper {
    private final static Log log = LogFactory.getLog(KernelManagementHelper.class);
    private Kernel kernel;
    private ProxyManager pm;

    public KernelManagementHelper(Kernel kernel) {
        this.kernel = kernel;
        pm = kernel.getProxyManager();
    }

    public J2EEDomain[] getDomains() {
        String[] names = Util.getObjectNames(kernel, "*:", new String[]{"J2EEDomain"});
        J2EEDomain[] domains = new J2EEDomain[names.length];
        for (int i = 0; i < domains.length; i++) {
            try {
                domains[i] = (J2EEDomain)kernel.getProxyManager().createProxy(ObjectName.getInstance(names[i]), J2EEDomain.class);
            } catch (MalformedObjectNameException e) {
                log.error("Unable to look up related GBean", e);
            }
        }
        return domains;
    }

    public J2EEServer[] getServers(J2EEDomain domain) {
        J2EEServer[] servers = new J2EEServer[0];
        try {
            String[] names = domain.getServers();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            servers = new J2EEServer[temp.length];
            System.arraycopy(temp, 0, servers, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return servers;
    }

    public J2EEDeployedObject[] getDeployedObjects(J2EEServer server) {
        J2EEDeployedObject[] result = new J2EEDeployedObject[0];
        try {
            String[] names = server.getDeployedObjects();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new J2EEDeployedObject[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public J2EEApplication[] getApplications(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.J2EE_APPLICATION)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (J2EEApplication[]) list.toArray(new J2EEApplication[list.size()]);
    }

    public AppClientModule[] getAppClients(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.APP_CLIENT_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (AppClientModule[]) list.toArray(new AppClientModule[list.size()]);
    }

    public WebModule[] getWebModules(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.WEB_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (WebModule[]) list.toArray(new WebModule[list.size()]);
    }

    public EJBModule[] getEJBModules(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.EJB_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (EJBModule[]) list.toArray(new EJBModule[list.size()]);
    }

    public ResourceAdapterModule[] getRAModules(J2EEServer server) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(J2EEServer server, String connectionFactoryInterface) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    ResourceAdapterModule module = (ResourceAdapterModule) pm.createProxy(name, KernelManagementHelper.class.getClassLoader());
                    ResourceAdapter[] adapters = getResourceAdapters(module);
                    for (int j = 0; j < adapters.length; j++) {
                        ResourceAdapter adapter = adapters[j];
                        JCAResource[] resources = getRAResources(adapter);
                        for (int k = 0; k < resources.length; k++) {
                            JCAResource resource = resources[k];
                            JCAConnectionFactory[] factories = getConnectionFactories(resource);
                            for (int l = 0; l < factories.length; l++) {
                                JCAConnectionFactory factory = factories[l];
                                JCAManagedConnectionFactory mcf = getManagedConnectionFactory(factory);
                                if(mcf.getConnectionFactoryInterface().equals(connectionFactoryInterface)) {
                                    list.add(mcf);
                                    continue;
                                }
                                for (int m = 0; m < mcf.getImplementedInterfaces().length; m++) {
                                    String iface = mcf.getImplementedInterfaces()[m];
                                    if(iface.equals(connectionFactoryInterface)) {
                                        list.add(mcf);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (JCAManagedConnectionFactory[]) list.toArray(new JCAManagedConnectionFactory[list.size()]);
    }

    public ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String connectionFactoryInterface) {
        return getOutboundRAModules(server, new String[]{connectionFactoryInterface});
    }

    public ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String[] connectionFactoryInterfaces) {
        Set targets = new HashSet(Arrays.asList(connectionFactoryInterfaces));
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    ResourceAdapterModule module = (ResourceAdapterModule) pm.createProxy(name, KernelManagementHelper.class.getClassLoader());
                    ResourceAdapter[] adapters = getResourceAdapters(module);
                    outer:
                    for (int j = 0; j < adapters.length; j++) {
                        ResourceAdapter adapter = adapters[j];
                        JCAResource[] resources = getRAResources(adapter);
                        for (int k = 0; k < resources.length; k++) {
                            JCAResource resource = resources[k];
                            JCAConnectionFactory[] factories = getConnectionFactories(resource);
                            for (int l = 0; l < factories.length; l++) {
                                JCAConnectionFactory factory = factories[l];
                                JCAManagedConnectionFactory mcf = getManagedConnectionFactory(factory);
                                if(targets.contains(mcf.getConnectionFactoryInterface())) {
                                    list.add(module);
                                    break outer;
                                }
                                for (int m = 0; m < mcf.getImplementedInterfaces().length; m++) {
                                    String iface = mcf.getImplementedInterfaces()[m];
                                    if(targets.contains(iface)) {
                                        list.add(module);
                                        break outer;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }

    public ResourceAdapterModule[] getAdminObjectModules(J2EEServer server, String[] adminObjectInterfaces) {
        List list = new ArrayList();
        try {
            String[] names = server.getDeployedObjects();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    ResourceAdapterModule module = (ResourceAdapterModule) pm.createProxy(name, KernelManagementHelper.class.getClassLoader());
                    ResourceAdapter[] adapters = getResourceAdapters(module);
                    outer:
                    for (int j = 0; j < adapters.length; j++) {
                        ResourceAdapter adapter = adapters[j];
                        JCAResource[] resources = getRAResources(adapter);
                        for (int k = 0; k < resources.length; k++) {
                            JCAResource resource = resources[k];
                            JCAAdminObject[] admins = getAdminObjects(resource);
                            for (int l = 0; l < admins.length; l++) {
                                JCAAdminObject admin = admins[l];
                                String adminIface = admin.getAdminObjectInterface();
                                for (int m = 0; m < adminObjectInterfaces.length; m++) {
                                    if(adminIface.equals(adminObjectInterfaces[m])) {
                                        list.add(module);
                                        break outer;
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module) {
        List list = new ArrayList();
        try {
            ResourceAdapter[] adapters = getResourceAdapters(module);
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = getRAResources(adapter);
                for (int k = 0; k < resources.length; k++) {
                    JCAResource resource = resources[k];
                    JCAConnectionFactory[] factories = getConnectionFactories(resource);
                    for (int l = 0; l < factories.length; l++) {
                        JCAConnectionFactory factory = factories[l];
                        JCAManagedConnectionFactory mcf = getManagedConnectionFactory(factory);
                        list.add(mcf);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (JCAManagedConnectionFactory[]) list.toArray(new JCAManagedConnectionFactory[list.size()]);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String connectionFactoryInterface) {
        return getOutboundFactories(module, new String[]{connectionFactoryInterface});
    }
    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String[] connectionFactoryInterfaces) {
        Set targets = new HashSet(Arrays.asList(connectionFactoryInterfaces));
        List list = new ArrayList();
        try {
            ResourceAdapter[] adapters = getResourceAdapters(module);
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = getRAResources(adapter);
                for (int k = 0; k < resources.length; k++) {
                    JCAResource resource = resources[k];
                    JCAConnectionFactory[] factories = getConnectionFactories(resource);
                    for (int l = 0; l < factories.length; l++) {
                        JCAConnectionFactory factory = factories[l];
                        JCAManagedConnectionFactory mcf = getManagedConnectionFactory(factory);
                        if(targets.contains(mcf.getConnectionFactoryInterface())) {
                            list.add(mcf);
                            continue;
                        }
                        for (int m = 0; m < mcf.getImplementedInterfaces().length; m++) {
                            String iface = mcf.getImplementedInterfaces()[m];
                            if(targets.contains(iface)) {
                                list.add(mcf);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (JCAManagedConnectionFactory[]) list.toArray(new JCAManagedConnectionFactory[list.size()]);
    }

    public JCAAdminObject[] getAdminObjects(ResourceAdapterModule module, String[] adminObjectInterfaces) {
        List list = new ArrayList();
        try {
            ResourceAdapter[] adapters = getResourceAdapters(module);
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = getRAResources(adapter);
                for (int k = 0; k < resources.length; k++) {
                    JCAResource resource = resources[k];
                    JCAAdminObject[] admins = getAdminObjects(resource);
                    for (int l = 0; l < admins.length; l++) {
                        JCAAdminObject admin = admins[l];
                        String adminIface = admin.getAdminObjectInterface();
                        for (int m = 0; m < adminObjectInterfaces.length; m++) {
                            if(adminIface.equals(adminObjectInterfaces[m])) {
                                list.add(admin);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (JCAAdminObject[]) list.toArray(new JCAAdminObject[list.size()]);
    }

    public J2EEResource[] getResources(J2EEServer server) {
        J2EEResource[] result = new J2EEResource[0];
        try {
            String[] names = server.getResources();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new J2EEResource[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JCAResource[] getJCAResources(J2EEServer server) {
        List list = new ArrayList();
        try {
            //todo: filter based on ObjectName or something, but what counts as a "JCAResource"?
            J2EEResource[] all = getResources(server);
            for (int i = 0; i < all.length; i++) {
                if(all[i] instanceof JCAResource) {
                    list.add(all[i]);
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (JCAResource[]) list.toArray(new JCAResource[list.size()]);
    }

    public JDBCResource[] getJDBCResources(J2EEServer server) {
        return new JDBCResource[0]; // Geronimo uses JCA resources for this
    }

    public JMSResource[] getJMSResources(J2EEServer server) {
        return new JMSResource[0];  // Geronimo uses JCA resources for this
    }

    public JVM[] getJavaVMs(J2EEServer server) {
        JVM[] result = new JVM[0];
        try {
            String[] names = server.getJavaVMs();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JVM[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up JVMs for J2EEServer", e);
        }
        return result;
    }

    public Repository[] getRepositories(J2EEServer server) {
        Repository[] result = new Repository[0];
        try {
            String[] names = server.getRepositories();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new Repository[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up repositories for J2EEServer", e);
        }
        return result;
    }

    public SecurityRealm[] getSecurityRealms(J2EEServer server) {
        SecurityRealm[] result = new SecurityRealm[0];
        try {
            String[] names = server.getSecurityRealms();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new SecurityRealm[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up security realms for J2EEServer", e);
        }
        return result;
    }

    public ServerInfo getServerInfo(J2EEServer server) {
        try {
            String name = server.getServerInfo();
            return (ServerInfo) pm.createProxy(ObjectName.getInstance(name), KernelManagementHelper.class.getClassLoader());
        } catch (Exception e) {
            log.error("Unable to look up ServerInfo for J2EEServer", e);
            return null;
        }
    }

    public JaasLoginServiceMBean getLoginService(J2EEServer server) {
        try {
            String name = server.getServerInfo();
            return (JaasLoginServiceMBean) pm.createProxy(ObjectName.getInstance(name), KernelManagementHelper.class.getClassLoader());
        } catch (Exception e) {
            log.error("Unable to look up LoginService for J2EEServer", e);
            return null;
        }
    }

    public WebManager[] getWebManagers(J2EEServer server) {
        WebManager[] result = new WebManager[0];
        try {
            String[] names = server.getWebManagers();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new WebManager[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up WebManagers for J2EEServer", e);
        }
        return result;
    }

    public WebAccessLog getWebAccessLog(WebManager manager, WebContainer container) {
        return getWebAccessLog(manager, kernel.getObjectNameFor(container).getCanonicalName());
    }

    public WebAccessLog getWebAccessLog(WebManager manager, String container) {
        WebAccessLog result = null;
        try {
            String name = manager.getAccessLog(container);
            Object temp = pm.createProxy(ObjectName.getInstance(name), KernelManagementHelper.class.getClassLoader());
            result = (WebAccessLog) temp;
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public WebContainer[] getWebContainers(WebManager manager) {
        WebContainer[] result = new WebContainer[0];
        try {
            String[] names = manager.getContainers();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new WebContainer[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public WebConnector[] getWebConnectorsForContainer(WebManager manager, WebContainer container, String protocol) {
        return getWebConnectorsForContainer(manager, kernel.getObjectNameFor(container).getCanonicalName(), protocol);
    }

    public WebConnector[] getWebConnectorsForContainer(WebManager manager, WebContainer container) {
        return getWebConnectorsForContainer(manager, kernel.getObjectNameFor(container).getCanonicalName());
    }

    public WebConnector[] getWebConnectorsForContainer(WebManager manager, String containerObjectName, String protocol) {
        WebConnector[] result = new WebConnector[0];
        try {
            String[] names = manager.getConnectorsForContainer(containerObjectName, protocol);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new WebConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public WebConnector[] getWebConnectorsForContainer(WebManager manager, String containerObjectName) {
        WebConnector[] result = new WebConnector[0];
        try {
            String[] names = manager.getConnectorsForContainer(containerObjectName);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new WebConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public WebConnector[] getWebConnectors(WebManager manager, String protocol) {
        WebConnector[] result = new WebConnector[0];
        try {
            String[] names = manager.getConnectors(protocol);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new WebConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public WebConnector[] getWebConnectors(WebManager manager) {
        WebConnector[] result = new WebConnector[0];
        try {
            String[] names = manager.getConnectors();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new WebConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public EJBManager[] getEJBManagers(J2EEServer server) {
        EJBManager[] result = null;
        try {
            String names[] = server.getEJBManagers();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new EJBManager[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public EJBConnector[] getEJBConnectors(EJBManager container, String protocol) {
        EJBConnector[] result = new EJBConnector[0];
        try {
            String[] names = container.getConnectors(protocol);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new EJBConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public EJBConnector[] getEJBConnectors(EJBManager container) {
        EJBConnector[] result = new EJBConnector[0];
        try {
            String[] names = container.getConnectors();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new EJBConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JMSManager[] getJMSManagers(J2EEServer server) {
        JMSManager[] result = null;
        try {
            String[] names = server.getJMSManagers();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JMSManager[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JMSBroker[] getJMSBrokers(JMSManager manager) {
        JMSBroker[] result = null;
        try {
            String[] names = manager.getContainers();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JMSBroker[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JMSConnector[] getJMSConnectors(JMSManager manager, String protocol) {
        JMSConnector[] result = null;
        try {
            String[] names = manager.getConnectors(protocol);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JMSConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JMSConnector[] getJMSConnectors(JMSManager manager) {
        JMSConnector[] result = null;
        try {
            String[] names = manager.getConnectors();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JMSConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, JMSBroker broker, String protocol) {
        return getJMSConnectorsForContainer(manager, kernel.getObjectNameFor(broker).getCanonicalName(), protocol);
    }

    public JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, JMSBroker broker) {
        return getJMSConnectorsForContainer(manager, kernel.getObjectNameFor(broker).getCanonicalName());
    }

    public JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, String brokerObjectName, String protocol) {
        JMSConnector[] result = null;
        try {
            String[] names = manager.getConnectorsForContainer(brokerObjectName, protocol);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JMSConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JMSConnector[] getJMSConnectorsForContainer(JMSManager manager, String brokerObjectName) {
        JMSConnector[] result = null;
        try {
            String[] names = manager.getConnectorsForContainer(brokerObjectName);
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JMSConnector[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public GeronimoExecutor[] getThreadPools(J2EEServer server) {
        GeronimoExecutor[] result = new GeronimoExecutor[0];
        try {
            String[] names = server.getThreadPools();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new GeronimoExecutor[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public SystemLog getSystemLog(JVM jvm) {
        SystemLog result = null;
        try {
            String name = jvm.getSystemLog();
            Object temp = pm.createProxy(ObjectName.getInstance(name), KernelManagementHelper.class.getClassLoader());
            result = (SystemLog)temp;
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    // application properties
    public J2EEModule[] getModules(J2EEApplication application) {
        J2EEModule[] result = new J2EEModule[0];
        try {
            String[] names = application.getModules();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new J2EEModule[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public AppClientModule[] getAppClients(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.APP_CLIENT_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (AppClientModule[]) list.toArray(new AppClientModule[list.size()]);
    }

    public WebModule[] getWebModules(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.WEB_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (WebModule[]) list.toArray(new WebModule[list.size()]);
    }

    public EJBModule[] getEJBModules(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.EJB_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (EJBModule[]) list.toArray(new EJBModule[list.size()]);
    }

    public ResourceAdapterModule[] getRAModules(J2EEApplication application) {
        List list = new ArrayList();
        try {
            String[] names = application.getModules();
            for (int i = 0; i < names.length; i++) {
                ObjectName name = ObjectName.getInstance(names[i]);
                String type = name.getKeyProperty(NameFactory.J2EE_TYPE);
                if(type.equals(NameFactory.RESOURCE_ADAPTER_MODULE)) {
                    list.add(pm.createProxy(name, KernelManagementHelper.class.getClassLoader()));
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }


    public J2EEResource[] getResources(J2EEApplication application) {
        J2EEResource[] result = new J2EEResource[0];
        try {
            String[] names = application.getResources();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new J2EEResource[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return result;
    }

    public JCAResource[] getJCAResources(J2EEApplication application) {
        List list = new ArrayList();
        try {
            //todo: filter based on ObjectName or something, but what counts as a "JCAResource"?
            J2EEResource[] all = getResources(application);
            for (int i = 0; i < all.length; i++) {
                if(all[i] instanceof JCAResource) {
                    list.add(all[i]);
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return (JCAResource[]) list.toArray(new JCAResource[list.size()]);
    }

    public JDBCResource[] getJDBCResources(J2EEApplication application) {
        return new JDBCResource[0];  // Geronimo uses JCAResources for this
    }

    public JMSResource[] getJMSResources(J2EEApplication application) {
        return new JMSResource[0];  // Geronimo uses JCAResources for this
    }

    // module properties
    public EJB[] getEJBs(EJBModule module) {
        return new EJB[0];  //todo
    }

    public Servlet[] getServlets(WebModule module) {
        return new Servlet[0];  //todo
    }

    public ResourceAdapter[] getResourceAdapters(ResourceAdapterModule module) {
        ResourceAdapter[] result = new ResourceAdapter[0];
        try {
            String[] names = module.getResourceAdapters();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new ResourceAdapter[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up resource adapters for module", e);
        }
        return result;
    }

    // resource adapter properties
    public JCAResource[] getRAResources(ResourceAdapter adapter) {
        JCAResource[] result = new JCAResource[0];
        try {
            String[] names = adapter.getJCAResources();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JCAResource[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up JCA resources for resource adapter", e);
        }
        return result;
    }

    // resource properties
    public JDBCDataSource[] getDataSource(JDBCResource resource) {
        return new JDBCDataSource[0];  //todo
    }

    public JDBCDriver[] getDriver(JDBCDataSource dataSource) {
        return new JDBCDriver[0];  //todo
    }

    public JCAConnectionFactory[] getConnectionFactories(JCAResource resource) {
        JCAConnectionFactory[] result = new JCAConnectionFactory[0];
        try {
            String[] names = resource.getConnectionFactories();
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JCAConnectionFactory[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (Exception e) {
            log.error("Unable to look up connection factories for JCA resource", e);
        }
        return result;
    }

    public JCAAdminObject[] getAdminObjects(JCAResource resource) {
        JCAAdminObject[] result = new JCAAdminObject[0];
        String objectName = resource.getObjectName();
        try {
            String name = ObjectName.getInstance(objectName).getKeyProperty(NameFactory.J2EE_NAME);
            String query = "*:JCAResource="+name+",j2eeType=JCAAdminObject,*";
            Set results = kernel.listGBeans(ObjectName.getInstance(query));
            String[] names = new String[results.size()];
            int i = 0;
            for (Iterator it = results.iterator(); it.hasNext();) {
                ObjectName next = (ObjectName) it.next();
                names[i++] = next.getCanonicalName();
            }
            Object[] temp = pm.createProxies(names, KernelManagementHelper.class.getClassLoader());
            result = new JCAAdminObject[temp.length];
            System.arraycopy(temp, 0, result, 0, temp.length);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to look up admin objects for resource adapter", e);
        }
        return result;
    }

    public JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory) {
        try {
            String name = factory.getManagedConnectionFactory();
            return (JCAManagedConnectionFactory) pm.createProxy(ObjectName.getInstance(name), KernelManagementHelper.class.getClassLoader());
        } catch (Exception e) {
            log.error("Unable to look up managed connection factory for connection factory", e);
            return null;
        }
    }

    public Object getObject(String objectName) {
        try {
            return kernel.getProxyManager().createProxy(ObjectName.getInstance(objectName), KernelManagementHelper.class.getClassLoader());
        } catch (MalformedObjectNameException e) {
            log.error("Unable to look up related GBean", e);
            return null;
        }
    }

    public URI getConfigurationNameFor(String objectName) {
        try {
            Set parents = kernel.getDependencyManager().getParents(ObjectName.getInstance(objectName));
            if(parents.size() == 0) {
                throw new IllegalStateException("No parents for GBean '"+objectName+"'");
            }
            for (Iterator it = parents.iterator(); it.hasNext();) {
                ObjectName name = (ObjectName) it.next();
                if(Configuration.isConfigurationObjectName(name)) {
                    return Configuration.getConfigurationID(name);
                }
            }
        } catch (Exception e) {
            log.error("Unable to look up related GBean", e);
        }
        return null;
    }

    public String getGBeanDescription(String objectName) {
        try {
            return kernel.getGBeanInfo(ObjectName.getInstance(objectName)).getName();
        } catch (GBeanNotFoundException e) {
            return null;
        } catch (MalformedObjectNameException e) {
            return "Invalid object name";
        }
    }

    public void testLoginModule(J2EEServer server, LoginModule module, Map options) {
        options.put(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION, kernel.getKernelName());
        options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, module.getClass().getClassLoader());
        try {
            options.put(JaasLoginModuleUse.SERVERINFO_LM_OPTION, pm.createProxy(ObjectName.getInstance(server.getServerInfo()),module.getClass().getClassLoader()));
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Unable to look up server info: "+e.getMessage());
        }
        module.initialize(null, null, new HashMap(), options);
    }

    public Subject testLoginModule(final J2EEServer server, final LoginModule module, final Map options, final String username, final String password) throws LoginException {
        options.put(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION, kernel.getKernelName());
        options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, module.getClass().getClassLoader());
        try {
            options.put(JaasLoginModuleUse.SERVERINFO_LM_OPTION, pm.createProxy(ObjectName.getInstance(server.getServerInfo()),module.getClass().getClassLoader()));
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Unable to look up server info: "+e.getMessage());
        }
        Subject sub = new Subject();
        CallbackHandler handler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    Callback callback = callbacks[i];
                    if(callback instanceof PasswordCallback) {
                        ((PasswordCallback)callback).setPassword(password.toCharArray());
                    } else if(callback instanceof NameCallback) {
                        ((NameCallback)callback).setName(username);
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }
        };
        module.initialize(sub, handler, new HashMap(), options);
        if(module.login() && module.commit()) {
            return sub;
        } else {
            module.abort();
        }
        return null;
    }

    public Object[] findByInterface(Class iface) {
        Set set = kernel.listGBeans(new GBeanQuery(null, iface.getName()));
        Object[] result = new Object[set.size()];
        int i=0;
        for (Iterator it = set.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            result[i++] = kernel.getProxyManager().createProxy(name, iface.getClassLoader());
        }
        return result;
    }

    /**
     * Helper method to connect to a remote kernel.
     */
    public static KernelManagementHelper getRemoteKernelManager(String host, String user, String password) throws java.io.IOException {
        String uri = "jmx:rmi://"+host+"/jndi/rmi:/JMXConnector";
        java.util.Map environment = new java.util.HashMap();
        String[] credentials = new String[]{user, password};
        environment.put(javax.management.remote.JMXConnector.CREDENTIALS, credentials);
        javax.management.remote.JMXServiceURL address = new javax.management.remote.JMXServiceURL("service:" + uri);
        javax.management.remote.JMXConnector jmxConnector = javax.management.remote.JMXConnectorFactory.connect(address, environment);
        javax.management.MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        Kernel kernel = new org.apache.geronimo.kernel.jmx.KernelDelegate(mbServerConnection);
        return new KernelManagementHelper(kernel);
    }

    /**
     * For test purposes; start the server, deploy an app or two, and run this.
     * Should be changed to a JUnit test with the Maven plugin to start and
     * stop the server.
     */
    public static void main(String[] args) {
        try {
            ManagementHelper mgr = getRemoteKernelManager("localhost", "system", "manager");
            J2EEDomain domain = mgr.getDomains()[0];
            System.out.println("Found domain "+domain.getObjectName()+" with "+domain.getServers().length+" servers");
            J2EEServer server = mgr.getServers(domain)[0];
            System.out.println("Found server "+server.getObjectName()+" with "+server.getDeployedObjects().length+" deployments");
            System.out.println("  "+mgr.getApplications(server).length+" applications");
            System.out.println("  "+mgr.getAppClients(server).length+" app clients");
            System.out.println("  "+mgr.getEJBModules(server).length+" EJB JARs");
            System.out.println("  "+mgr.getWebModules(server).length+" web apps");
            System.out.println("  "+mgr.getRAModules(server).length+" RA modules");
            J2EEDeployedObject[] deployments = mgr.getDeployedObjects(server);
            for (int i = 0; i < deployments.length; i++) {
                J2EEDeployedObject deployment = deployments[i];
                System.out.println("Deployment "+i+": "+deployment.getObjectName());
            }
            J2EEApplication[] applications = mgr.getApplications(server);
            for (int i = 0; i < applications.length; i++) {
                J2EEApplication app = applications[i];
                System.out.println("Application "+i+": "+app.getObjectName());
                J2EEModule[] modules = mgr.getModules(app);
                for (int j = 0; j < modules.length; j++) {
                    J2EEModule deployment = modules[j];
                    System.out.println("  Module "+j+": "+deployment.getObjectName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
