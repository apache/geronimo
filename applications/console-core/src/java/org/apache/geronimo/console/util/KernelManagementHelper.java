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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.JCAConnectionFactory;
import org.apache.geronimo.management.JDBCDataSource;
import org.apache.geronimo.management.JDBCDriver;
import org.apache.geronimo.management.JDBCResource;
import org.apache.geronimo.management.JMSResource;
import org.apache.geronimo.management.ResourceAdapter;
import org.apache.geronimo.management.Servlet;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEDomain;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.system.logging.SystemLog;

/**
 * An implementation of the ManagementHelper interface that uses a Geronimo
 * kernel. That may be an in-VM kernel or a remote kernel, we don't really
 * care.
 *
 * @version $Rev:386276 $ $Date$
 */
public class KernelManagementHelper implements ManagementHelper {
    private final static Log log = LogFactory.getLog(KernelManagementHelper.class);
    private final Kernel kernel;
    private final ProxyManager pm;

    public KernelManagementHelper(Kernel kernel) {
        this.kernel = kernel;
        pm = kernel.getProxyManager();
    }

    public J2EEDomain[] getDomains() {
        Set domainNames = kernel.listGBeans(new AbstractNameQuery(J2EEDomain.class.getName()));
        J2EEDomain[] result = new J2EEDomain[domainNames.size()];
        int i = 0;
        for (Iterator iterator = domainNames.iterator(); iterator.hasNext();) {
            AbstractName domainName = (AbstractName) iterator.next();
            result[i++] = (J2EEDomain) pm.createProxy(domainName, J2EEDomain.class);
        }
        return result;
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


    public JCAResource[] getJCAResources(J2EEApplication application) {
        List list = new ArrayList();
        try {
            //todo: filter based on ObjectName or something, but what counts as a "JCAResource"?
            J2EEResource[] all = application.getResources();
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

    public Object getObject(AbstractName objectName) {
        return kernel.getProxyManager().createProxy(objectName, KernelManagementHelper.class.getClassLoader());
    }

    public Artifact getConfigurationNameFor(AbstractName abstractName) {
        return abstractName.getArtifact();
    }

    public String getGBeanDescription(AbstractName abstractName) {
        try {
            return kernel.getGBeanInfo(abstractName).getName();
        } catch (GBeanNotFoundException e) {
            return null;
        }
    }

    public void testLoginModule(J2EEServer server, LoginModule module, Map options) {
        options.put(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION, kernel.getKernelName());
        options.put(JaasLoginModuleUse.SERVERINFO_LM_OPTION, server.getServerInfo());
        if(!options.containsKey(JaasLoginModuleUse.CLASSLOADER_LM_OPTION)) {
            options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, module.getClass().getClassLoader());
        }
        module.initialize(null, null, new HashMap(), options);
    }

    public Subject testLoginModule(final J2EEServer server, final LoginModule module, final Map options, final String username, final String password) throws LoginException {
        options.put(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION, kernel.getKernelName());
        if(!options.containsKey(JaasLoginModuleUse.CLASSLOADER_LM_OPTION)) {
            options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, module.getClass().getClassLoader());
        }
        options.put(JaasLoginModuleUse.SERVERINFO_LM_OPTION, server.getServerInfo());
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
        Set set = kernel.listGBeans(new AbstractNameQuery(iface.getName()));
        Object[] result = new Object[set.size()];
        int i=0;
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            result[i++] = kernel.getProxyManager().createProxy(name, iface.getClassLoader());
        }
        return result;
    }

    public AbstractName getNameFor(Object component) {
        return kernel.getAbstractNameFor(component);
    }

    public ConfigurationData[] getConfigurations(ConfigurationModuleType type, boolean includeChildModules) {
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
        List stores = mgr.listStores();
        List results = new ArrayList();
        for (Iterator i = stores.iterator(); i.hasNext();) {
            AbstractName storeName = (AbstractName) i.next();
            try {
                List infos = mgr.listConfigurations(storeName);
                for (Iterator j = infos.iterator(); j.hasNext();) {
                    ConfigurationInfo info = (ConfigurationInfo) j.next();
                    AbstractName configuration = Configuration.getConfigurationAbstractName(info.getConfigID());
                    if(type == null || type.getValue() == info.getType().getValue()) {
                        results.add(new ConfigurationData(info.getConfigID(), configuration, null, info.getState(), info.getType(), kernel.getAbstractNameFor(getModuleForConfiguration(info.getConfigID()))));
                    }
                    if(includeChildModules && info.getType().getValue() == ConfigurationModuleType.EAR.getValue() && info.getState().toInt() == State.RUNNING_INDEX) {
                        J2EEApplication app = (J2EEApplication) getModuleForConfiguration(info.getConfigID());
                        if(app == null) {
                            throw new IllegalStateException("Unable to load children for J2EE Application '"+info.getConfigID()+"' (no J2EEApplication found)");
                        }
                        Object[] modules = null;
                        if(type == null) {
                            modules = app.getModulesInstances();
                        } else if(type.equals(ConfigurationModuleType.CAR)) {
                            modules = app.getClientModules();
                        } else if(type.equals(ConfigurationModuleType.EJB)) {
                            modules = app.getEJBModules();
                        } else if(type.equals(ConfigurationModuleType.RAR)) {
                            modules = app.getRAModules();
                        } else if(type.equals(ConfigurationModuleType.WAR)) {
                            modules = app.getWebModules();
                        } //todo: handle dynamically registered module types, etc.
                        if(modules == null) continue;
                        for (int k = 0; k < modules.length; k++) {
                            Object module = modules[k];
                            ConfigurationModuleType moduleType = type;
                            if(moduleType == null) {
                                if(module instanceof WebModule) moduleType = ConfigurationModuleType.WAR;
                                else if(module instanceof EJBModule) moduleType = ConfigurationModuleType.EJB;
                                else if(module instanceof ResourceAdapterModule) moduleType = ConfigurationModuleType.RAR;
                                else if(module instanceof AppClientModule) moduleType = ConfigurationModuleType.CAR;
                            }
                            String moduleName;
                            if(type != null && type.equals(ConfigurationModuleType.WAR)) {
                                moduleName = ((WebModule)module).getWARName();
                            } else {
                                //todo: solutions for other module types
                                moduleName = (String) kernel.getAbstractNameFor(module).getName().get(NameFactory.J2EE_NAME);
                            }
                            results.add(new ConfigurationData(info.getConfigID(), configuration, moduleName, info.getState(), moduleType, kernel.getAbstractNameFor(module)));
                        }
                    }
                }
            } catch (NoSuchStoreException e) {
                // we just got this list so this should not happen
                // in the unlikely event it does, just continue
            } catch (InvalidConfigException e) {
                throw new RuntimeException("Bad configID; should never happen");
            }
        }
        Collections.sort(results);
        return (ConfigurationData[]) results.toArray(new ConfigurationData[results.size()]);
    }

    /**
     * Gets a JSR-77 Module (WebModule, EJBModule, etc.) for the specified configuration.
     * Note: this only works if the configuration is running at the time you ask.
     *
     * @return The Module, or null if the configuration is not running.
     */
    public J2EEDeployedObject getModuleForConfiguration(Artifact configuration) {
        ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
        Configuration config = manager.getConfiguration(configuration);
        if(config == null) {
            return null; // The configuration is not running, so we can't get its contents
        }
        ConfigurationModuleType type = config.getModuleType();
        AbstractName result;
        try {
            if(type.equals(ConfigurationModuleType.CAR)) {
                result = config.findGBean(new AbstractNameQuery(AppClientModule.class.getName()));
            } else if(type.equals(ConfigurationModuleType.EAR)) {
                result = config.findGBean(new AbstractNameQuery(J2EEApplication.class.getName()));
            } else if(type.equals(ConfigurationModuleType.EJB)) {
                result = config.findGBean(new AbstractNameQuery(EJBModule.class.getName()));
            } else if(type.equals(ConfigurationModuleType.RAR)) {
                result = config.findGBean(new AbstractNameQuery(ResourceAdapterModule.class.getName()));
            } else if(type.equals(ConfigurationModuleType.WAR)) {
                result = config.findGBean(new AbstractNameQuery(WebModule.class.getName()));
            } else {
                return null;
            }
            return (J2EEDeployedObject) kernel.getProxyManager().createProxy(result, getClass().getClassLoader());
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("Bad config ID: "+e.getMessage());
        }
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
        Kernel kernel = new org.apache.geronimo.system.jmx.KernelDelegate(mbServerConnection);
        return new KernelManagementHelper(kernel);
    }
}
