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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.*;
import org.apache.geronimo.kernel.management.State;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.logging.SystemLog;
import org.apache.geronimo.management.*;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEDomain;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.*;
import org.apache.geronimo.management.geronimo.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.lang.reflect.Array;
import java.util.*;

/**
 * An implementation of the ManagementHelper interface that uses a Geronimo
 * kernel. That must be an in-VM kernel.
 *
 * @version $Rev:386276 $ $Date$
 */
public class KernelManagementHelper implements ManagementHelper {
    private final Kernel kernel;

    public KernelManagementHelper(Kernel kernel) {
        this.kernel = kernel;
    }

    public J2EEDomain[] getDomains() {
        Set<AbstractName> domainNames = kernel.listGBeans(new AbstractNameQuery(J2EEDomain.class.getName()));
        List<J2EEDomain> domains = new ArrayList<J2EEDomain>();
        for (AbstractName domainName: domainNames) {
            try {
                domains.add((J2EEDomain) kernel.getGBean(domainName));
            } catch (GBeanNotFoundException e) {
                //ignore
            }
        }
        return domains.toArray(new J2EEDomain[domains.size()]);
//        J2EEDomain[] result = new J2EEDomain[domainNames.size()];
//        int i = 0;
//        for (Iterator iterator = domainNames.iterator(); iterator.hasNext();) {
//            AbstractName domainName = (AbstractName) iterator.next();
//            result[i++] = (J2EEDomain) kernel.getProxyManager().createProxy(domainName, J2EEDomain.class);
//        }
//        return result;
    }

    public J2EEServer[] getServers(J2EEDomain domain) {
        return proxify(domain.getServerInstances(), J2EEServer.class);
    }

    public J2EEDeployedObject[] getDeployedObjects(J2EEServer server) {
        return proxify(server.getDeployedObjectInstances(), J2EEDeployedObject.class);
    }

    public J2EEApplication[] getApplications(J2EEServer server) {
        return proxify(server.getApplications(), J2EEApplication.class);
    }

    public AppClientModule[] getAppClients(J2EEServer server) {
        return proxify(server.getAppClients(), AppClientModule.class);
    }

    public WebModule[] getWebModules(J2EEServer server) {
        return proxify(server.getWebModules(), WebModule.class);
    }

    public EJBModule[] getEJBModules(J2EEServer server) {
        return proxify(server.getEJBModules(), EJBModule.class);
    }

    public ResourceAdapterModule[] getRAModules(J2EEServer server) {
        return proxify(server.getResourceAdapterModules(), ResourceAdapterModule.class);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(J2EEServer server, String connectionFactoryInterface) {
        List<JCAManagedConnectionFactory> list = new ArrayList<JCAManagedConnectionFactory>();
        ResourceAdapterModule[] modules = server.getResourceAdapterModules();
        for (ResourceAdapterModule module : modules) {
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (ResourceAdapter adapter : adapters) {
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                for (JCAResource resource : resources) {
                    JCAManagedConnectionFactory[] outboundFactories = resource.getOutboundFactories();
                    list.addAll(Arrays.asList(outboundFactories));
                }
            }

        }
        return proxify(list.toArray(new JCAManagedConnectionFactory[list.size()]), JCAManagedConnectionFactory.class);
    }

    public ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String connectionFactoryInterface) {
        return getOutboundRAModules(server, new String[]{connectionFactoryInterface});
    }

    public ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String[] connectionFactoryInterfaces) {
        List<ResourceAdapterModule> list = new ArrayList<ResourceAdapterModule>();

        ResourceAdapterModule[] modules = server.getResourceAdapterModules();

        outer:
        for (ResourceAdapterModule module : modules) {
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (ResourceAdapter adapter : adapters) {
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                for (JCAResource resource : resources) {
                    JCAManagedConnectionFactory[] outboundFactories = resource.getOutboundFactories(connectionFactoryInterfaces);
                    if (outboundFactories.length > 0) {
                        list.add(module);
                        continue outer;
                    }
                }
            }

        }
        return proxify(list.toArray(new ResourceAdapterModule[list.size()]), ResourceAdapterModule.class);
    }

    public ResourceAdapterModule[] getAdminObjectModules(J2EEServer server, String[] adminObjectInterfaces) {
        List<ResourceAdapterModule> list = new ArrayList<ResourceAdapterModule>();

        ResourceAdapterModule[] modules = server.getResourceAdapterModules();

        outer:
        for (ResourceAdapterModule module : modules) {
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (ResourceAdapter adapter : adapters) {
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                for (JCAResource resource : resources) {
                    JCAAdminObject[] adminObjects = resource.getAdminObjectInstances(adminObjectInterfaces);
                    if (adminObjects.length > 0) {
                        list.add(module);
                        continue outer;
                    }
                }
            }

        }
        return proxify(list.toArray(new ResourceAdapterModule[list.size()]), ResourceAdapterModule.class);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module) {
        return getOutboundFactories(module, (String[]) null);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String connectionFactoryInterface) {
        return getOutboundFactories(module, new String[]{connectionFactoryInterface});
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String[] connectionFactoryInterfaces) {
        List<JCAManagedConnectionFactory> list = new ArrayList<JCAManagedConnectionFactory>();

        ResourceAdapter[] resourceAdapters = module.getResourceAdapterInstances();
        for (ResourceAdapter resourceAdapter : resourceAdapters) {
            JCAResource[] jcaResources = resourceAdapter.getJCAResourceImplementations();
            for (JCAResource jcaResource : jcaResources) {
                JCAManagedConnectionFactory[] outboundFactories = jcaResource.getOutboundFactories(connectionFactoryInterfaces);
                list.addAll(Arrays.asList(outboundFactories));
            }
        }

        return proxify(list.toArray(new JCAManagedConnectionFactory[list.size()]), JCAManagedConnectionFactory.class);
    }

    public JCAAdminObject[] getAdminObjects(ResourceAdapterModule module, String[] adminObjectInterfaces) {
        List<JCAAdminObject> list = new ArrayList<JCAAdminObject>();
        ResourceAdapter[] resourceAdapters = module.getResourceAdapterInstances();
        for (ResourceAdapter resourceAdapter : resourceAdapters) {
            JCAResource[] jcaResources = resourceAdapter.getJCAResourceImplementations();
            for (JCAResource jcaResource : jcaResources) {
                JCAAdminObject[] adminObjects = jcaResource.getAdminObjectInstances(adminObjectInterfaces);
                list.addAll(Arrays.asList(adminObjects));
            }
        }

        return proxify(list.toArray(new JCAAdminObject[list.size()]), JCAAdminObject.class);
    }

    public J2EEResource[] getResources(J2EEServer server) {
        return proxify(server.getResourceInstances(), J2EEResource.class);
    }

    public JCAResource[] getJCAResources(J2EEServer server) {
        List<JCAResource> list = new ArrayList<JCAResource>();
        ResourceAdapterModule[] modules = server.getResourceAdapterModules();
        for (ResourceAdapterModule module : modules) {
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (ResourceAdapter adapter : adapters) {
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                list.addAll(Arrays.asList(resources));
            }

        }
        return proxify(list.toArray(new JCAResource[list.size()]), JCAResource.class);
    }

    public JDBCResource[] getJDBCResources(J2EEServer server) {
        return new JDBCResource[0]; // Geronimo uses JCA resources for this
    }

    public JMSResource[] getJMSResources(J2EEServer server) {
        return new JMSResource[0];  // Geronimo uses JCA resources for this
    }

    public JVM[] getJavaVMs(J2EEServer server) {
        return proxify(server.getJavaVMInstances(), JVM.class);
    }

    public SystemLog getSystemLog(JVM jvm) {
        return proxify(jvm.getSystemLog(), SystemLog.class);
    }

    // application properties
    public J2EEModule[] getModules(J2EEApplication application) {
        return proxify(application.getModulesInstances(), J2EEModule.class);
    }

    public AppClientModule[] getAppClients(J2EEApplication application) {
        return proxify(application.getClientModules(), AppClientModule.class);
    }

    public WebModule[] getWebModules(J2EEApplication application) {
        return proxify(application.getWebModules(), WebModule.class);
    }

    public EJBModule[] getEJBModules(J2EEApplication application) {
        return proxify(application.getEJBModules(), EJBModule.class);
    }

    public ResourceAdapterModule[] getRAModules(J2EEApplication application) {
        return proxify(application.getRAModules(), ResourceAdapterModule.class);
    }


    public JCAResource[] getJCAResources(J2EEApplication application) {
        List<JCAResource> list = new ArrayList<JCAResource>();
        ResourceAdapterModule[] modules = application.getRAModules();
        for (ResourceAdapterModule module : modules) {
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (ResourceAdapter adapter : adapters) {
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                list.addAll(Arrays.asList(resources));
            }

        }
        return proxify(list.toArray(new JCAResource[list.size()]), JCAResource.class);
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
        return proxify(module.getResourceAdapterInstances(), ResourceAdapter.class);
    }

    // resource adapter properties
    public JCAResource[] getRAResources(ResourceAdapter adapter) {
        return proxify(adapter.getJCAResourceImplementations(), JCAResource.class);
    }

    // resource properties
    public JDBCDataSource[] getDataSource(JDBCResource resource) {
        return new JDBCDataSource[0];  //todo
    }

    public JDBCDriver[] getDriver(JDBCDataSource dataSource) {
        return new JDBCDriver[0];  //todo
    }

    public JCAConnectionFactory[] getConnectionFactories(JCAResource resource) {
        return proxify(resource.getConnectionFactoryInstances(), JCAConnectionFactory.class);
    }

    public JCAAdminObject[] getAdminObjects(JCAResource resource) {
        return proxify(resource.getAdminObjectInstances(), JCAAdminObject.class);
    }

    public JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory) {
        return proxify(factory.getManagedConnectionFactoryInstance(), JCAManagedConnectionFactory.class);
    }

    public Object getObject(AbstractName objectName) {
        ClassLoader cl = null;
        try {
            cl = new BundleClassLoader(kernel.getBundleFor(objectName));
        } catch(GBeanNotFoundException e) {
            cl = KernelManagementHelper.class.getClassLoader();
        }
        return kernel.getProxyManager().createProxy(objectName, cl);
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
        if (!options.containsKey(JaasLoginModuleUse.CLASSLOADER_LM_OPTION)) {
            options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, module.getClass().getClassLoader());
        }
        module.initialize(null, null, new HashMap(), options);
    }

    public Subject testLoginModule(final J2EEServer server, final LoginModule module, final Map options, final String username, final String password) throws LoginException {
        options.put(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION, kernel.getKernelName());
        if (!options.containsKey(JaasLoginModuleUse.CLASSLOADER_LM_OPTION)) {
            options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, module.getClass().getClassLoader());
        }
        options.put(JaasLoginModuleUse.SERVERINFO_LM_OPTION, server.getServerInfo());
        Subject sub = new Subject();
        CallbackHandler handler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    Callback callback = callbacks[i];
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                    } else if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(username);
                    } else {
                        throw new UnsupportedCallbackException(callback);
                    }
                }
            }
        };
        module.initialize(sub, handler, new HashMap(), options);
        if (module.login() && module.commit()) {
            return sub;
        } else {
            module.abort();
        }
        return null;
    }

    public Object[] findByInterface(Class iface) {
        Set set = kernel.listGBeans(new AbstractNameQuery(iface.getName()));
        Object[] result = new Object[set.size()];
        int i = 0;
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                result[i++] = kernel.getGBean(name);
            } catch (GBeanNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public AbstractName getNameFor(Object component) {
        return kernel.getAbstractNameFor(component);
    }

    public ConfigurationData[] getConfigurations(ConfigurationModuleType type, boolean includeChildModules) {
        ConfigurationManager mgr = null;
        try {
            mgr = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            return null;
        }
        List<AbstractName> stores = mgr.listStores();
        List<ConfigurationData> results = new ArrayList<ConfigurationData>();
        for (AbstractName storeName : stores) {
            try {
                List<ConfigurationInfo> infos = mgr.listConfigurations(storeName);
                for (ConfigurationInfo info : infos) {
                    AbstractName configuration = Configuration.getConfigurationAbstractName(info.getConfigID());
                    if (type == null || type.getValue() == info.getType().getValue()) {
                        J2EEDeployedObject module = getModuleForConfiguration(info.getConfigID());
                        results.add(new ConfigurationData(info.getConfigID(), configuration, null, info.getState(), info.getType(), module == null ? null : kernel.getAbstractNameFor(module)));
                    }
                    if (includeChildModules && info.getType().getValue() == ConfigurationModuleType.EAR.getValue() && info.getState().toInt() == State.RUNNING_INDEX) {
                        J2EEApplication app = (J2EEApplication) getModuleForConfiguration(info.getConfigID());
                        if (app == null) {
                            throw new IllegalStateException("Unable to load children for J2EE Application '" + info.getConfigID() + "' (no J2EEApplication found)");
                        }
                        Object[] modules = null;
                        if (type == null) {
                            modules = app.getModulesInstances();
                        } else if (type.equals(ConfigurationModuleType.CAR)) {
                            modules = app.getClientModules();
                        } else if (type.equals(ConfigurationModuleType.EJB)) {
                            modules = app.getEJBModules();
                        } else if (type.equals(ConfigurationModuleType.RAR)) {
                            modules = app.getRAModules();
                        } else if (type.equals(ConfigurationModuleType.WAR)) {
                            modules = app.getWebModules();
                        } //todo: handle dynamically registered module types, etc.
                        if (modules == null) continue;
                        for (Object module : modules) {
                            ConfigurationModuleType moduleType = type;
                            if (moduleType == null) {
                                if (module instanceof WebModule) {
                                    moduleType = ConfigurationModuleType.WAR;
                                } else if (module instanceof EJBModule) {
                                    moduleType = ConfigurationModuleType.EJB;
                                } else if (module instanceof ResourceAdapterModule) {
                                    moduleType = ConfigurationModuleType.RAR;
                                } else if (module instanceof AppClientModule) moduleType = ConfigurationModuleType.CAR;
                            }
                            String moduleName;
                            if (type != null && type.equals(ConfigurationModuleType.WAR)) {
                                moduleName = ((WebModule) module).getWARName();
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
                throw new RuntimeException("Bad configID; should never happen", e);
            }
        }
        Collections.sort(results);
        return results.toArray(new ConfigurationData[results.size()]);
    }

    /**
     * Gets a JSR-77 Module (WebModule, EJBModule, etc.) for the specified configuration.
     * Note: this only works if the configuration is running at the time you ask.
     *
     * @return The Module, or null if the configuration is not running.
     */
    public J2EEDeployedObject getModuleForConfiguration(Artifact configuration) {
        ConfigurationManager manager = null;
        try {
            manager = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            return null;
        }
        Configuration config = manager.getConfiguration(configuration);
        if (config == null || !manager.isRunning(configuration)) {
            return null; // The configuration is not running, so we can't get its contents
        }
        ConfigurationModuleType type = config.getModuleType();
        AbstractName result;
        try {
            if (type.equals(ConfigurationModuleType.CAR)) {
                result = config.findGBean(new AbstractNameQuery(AppClientModule.class.getName()));
            } else if (type.equals(ConfigurationModuleType.EAR)) {
                result = config.findGBean(new AbstractNameQuery(J2EEApplication.class.getName()));
            } else if (type.equals(ConfigurationModuleType.EJB)) {
                result = config.findGBean(new AbstractNameQuery(EJBModule.class.getName()));
            } else if (type.equals(ConfigurationModuleType.RAR)) {
                result = config.findGBean(new AbstractNameQuery(ResourceAdapterModule.class.getName()));
            } else if (type.equals(ConfigurationModuleType.WAR)||type.equals(ConfigurationModuleType.WAB)) {
                result = config.findGBean(new AbstractNameQuery(WebModule.class.getName()));
            } else {
                return null;
            }
            ClassLoader classLoader = new BundleClassLoader(kernel.getBundleFor(result));
            return (J2EEDeployedObject) kernel.getProxyManager().createProxy(result, classLoader);
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("Bad config ID: " + e.getMessage(), e);
        }
    }

    public Object[] getGBeansImplementing(Class iface) {
        Set set = kernel.listGBeans(new AbstractNameQuery(iface.getName()));
        Object[] result = (Object[]) Array.newInstance(iface, set.size());
        int index = 0;
        ProxyManager mgr = kernel.getProxyManager();
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            result[index++] = mgr.createProxy(name, iface);
        }
        return result;
    }    
    
    /**
     * Adds a new GBean to an existing Configuration.
     * @param configID  The configuration to add the GBean to.
     * @param gbean     The data representing the GBean to add.
     * @param start     If true, the GBean should be started as part of this call.
     */
    public void addGBeanToConfiguration(Artifact configID, GBeanData gbean, boolean start) {
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        try {
            mgr.addGBeanToConfiguration(configID, gbean, start);
        } catch (InvalidConfigException e) {
            throw new RuntimeException("Bad configID. configID = "+configID, e);
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
        }
    }

    /**
     * This method returns the Naming object of the kernel.
     */
    public Naming getNaming() {
        return kernel.getNaming();
    }

    /**
     * Helper method to connect to a remote kernel.
     */
    public static KernelManagementHelper getRemoteKernelManager(String host, String user, String password) throws java.io.IOException {
        String uri = "jmx:rmi://" + host + "/jndi/rmi:/JMXConnector";
        java.util.Map environment = new java.util.HashMap();
        String[] credentials = new String[]{user, password};
        environment.put(javax.management.remote.JMXConnector.CREDENTIALS, credentials);
        javax.management.remote.JMXServiceURL address = new javax.management.remote.JMXServiceURL("service:" + uri);
        javax.management.remote.JMXConnector jmxConnector = javax.management.remote.JMXConnectorFactory.connect(address, environment);
        javax.management.MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
        Kernel kernel = new org.apache.geronimo.system.jmx.KernelDelegate(mbServerConnection);
        return new KernelManagementHelper(kernel);
    }

    private<T>  T[] proxify(T[] array, Class<T> clazz) {
        for (int i = 0; i < array.length; i++) {
            array[i] = proxify(array[i], clazz);
        }
        return array;
    }

    private<T> T proxify(T t, Class<T> clazz) {
//        if (!(t instanceof GeronimoManagedBean)) {
//            AbstractName name = kernel.getAbstractNameFor(t);
//            t = (T) kernel.getProxyManager().createProxy(name, clazz);
//        }
        return t;
    }

}
