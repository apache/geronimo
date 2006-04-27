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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEDeployedObject;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.JDBCDataSource;
import org.apache.geronimo.management.JDBCDriver;
import org.apache.geronimo.management.JDBCResource;
import org.apache.geronimo.management.JMSResource;
import org.apache.geronimo.management.Servlet;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEDomain;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.system.logging.SystemLog;

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
        Set domainNames = kernel.listGBeans(new AbstractNameQuery(J2EEDomain.class.getName()));
        J2EEDomain[] result = new J2EEDomain[domainNames.size()];
        int i = 0;
        for (Iterator iterator = domainNames.iterator(); iterator.hasNext();) {
            AbstractName domainName = (AbstractName) iterator.next();
            result[i++] = (J2EEDomain) kernel.getProxyManager().createProxy(domainName, J2EEDomain.class);
        }
        return result;
    }

    public J2EEServer[] getServers(J2EEDomain domain) {
        return domain.getServerInstances();
    }

    public J2EEDeployedObject[] getDeployedObjects(J2EEServer server) {
        return server.getDeployedObjectInstances();
    }

    public J2EEApplication[] getApplications(J2EEServer server) {
        return server.getApplications();
    }

    public AppClientModule[] getAppClients(J2EEServer server) {
        return server.getAppClients();
    }

    public WebModule[] getWebModules(J2EEServer server) {
        return server.getWebModules();
    }

    public EJBModule[] getEJBModules(J2EEServer server) {
        return server.getEJBModules();
    }

    public ResourceAdapterModule[] getRAModules(J2EEServer server) {
        return server.getResourceAdapterModules();
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(J2EEServer server, String connectionFactoryInterface) {
        List list = new ArrayList();
        ResourceAdapterModule[] modules = server.getResourceAdapterModules();
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                for (int k = 0; k < resources.length; k++) {
                    JCAResource resource = resources[k];
                    JCAManagedConnectionFactory[] outboundFactories = resource.getOutboundFactories();
                    list.addAll(Arrays.asList(outboundFactories));
                }
            }

        }
        return (JCAManagedConnectionFactory[]) list.toArray(new JCAManagedConnectionFactory[list.size()]);
    }

    public ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String connectionFactoryInterface) {
        return getOutboundRAModules(server, new String[]{connectionFactoryInterface});
    }

    public ResourceAdapterModule[] getOutboundRAModules(J2EEServer server, String[] connectionFactoryInterfaces) {
        List list = new ArrayList();

        ResourceAdapterModule[] modules = server.getResourceAdapterModules();

        outer:
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                for (int k = 0; k < resources.length; k++) {
                    JCAResource resource = resources[k];
                    JCAManagedConnectionFactory[] outboundFactories = resource.getOutboundFactories(connectionFactoryInterfaces);
                    if (outboundFactories.length > 0) {
                        list.add(module);
                        continue outer;
                    }
                }
            }

        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }

    public ResourceAdapterModule[] getAdminObjectModules(J2EEServer server, String[] adminObjectInterfaces) {
        List list = new ArrayList();

        ResourceAdapterModule[] modules = server.getResourceAdapterModules();

        outer:
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                for (int k = 0; k < resources.length; k++) {
                    JCAResource resource = resources[k];
                    JCAAdminObject[] adminObjects = resource.getAdminObjectInstances(adminObjectInterfaces);
                    if (adminObjects.length > 0) {
                        list.add(module);
                        continue outer;
                    }
                }
            }

        }
        return (ResourceAdapterModule[]) list.toArray(new ResourceAdapterModule[list.size()]);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module) {
        return getOutboundFactories(module, (String[]) null);
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String connectionFactoryInterface) {
        return getOutboundFactories(module, new String[]{connectionFactoryInterface});
    }

    public JCAManagedConnectionFactory[] getOutboundFactories(ResourceAdapterModule module, String[] connectionFactoryInterfaces) {
        List list = new ArrayList();

        ResourceAdapter[] resourceAdapters = module.getResourceAdapterInstances();
        for (int i = 0; i < resourceAdapters.length; i++) {
            ResourceAdapter resourceAdapter = resourceAdapters[i];
            JCAResource[] jcaResources = resourceAdapter.getJCAResourceImplementations();
            for (int j = 0; j < jcaResources.length; j++) {
                JCAResource jcaResource = jcaResources[j];
                JCAManagedConnectionFactory[] outboundFactories = jcaResource.getOutboundFactories(connectionFactoryInterfaces);
                list.addAll(Arrays.asList(outboundFactories));
            }
        }

        return (JCAManagedConnectionFactory[]) list.toArray(new JCAManagedConnectionFactory[list.size()]);
    }

    public JCAAdminObject[] getAdminObjects(ResourceAdapterModule module, String[] adminObjectInterfaces) {
        List list = new ArrayList();
        ResourceAdapter[] resourceAdapters = module.getResourceAdapterInstances();
        for (int i = 0; i < resourceAdapters.length; i++) {
            ResourceAdapter resourceAdapter = resourceAdapters[i];
            JCAResource[] jcaResources = resourceAdapter.getJCAResourceImplementations();
            for (int j = 0; j < jcaResources.length; j++) {
                JCAResource jcaResource = jcaResources[j];
                JCAAdminObject[] adminObjects  = jcaResource.getAdminObjectInstances(adminObjectInterfaces);
                list.addAll(Arrays.asList(adminObjects));
            }
        }

        return (JCAAdminObject[]) list.toArray(new JCAAdminObject[list.size()]);
    }

    public J2EEResource[] getResources(J2EEServer server) {
        return server.getResourceInstances();
    }

    public JCAResource[] getJCAResources(J2EEServer server) {
        List list = new ArrayList();
        ResourceAdapterModule[] modules = server.getResourceAdapterModules();
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                list.addAll(Arrays.asList(resources));
            }

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
        return server.getJavaVMInstances();
    }

    public SystemLog getSystemLog(JVM jvm) {
        return jvm.getSystemLog();
    }

    // application properties
    public J2EEModule[] getModules(J2EEApplication application) {
        return application.getModulesInstances();
    }

    public AppClientModule[] getAppClients(J2EEApplication application) {
        return application.getClientModules();
    }

    public WebModule[] getWebModules(J2EEApplication application) {
        return application.getWebModules();
    }

    public EJBModule[] getEJBModules(J2EEApplication application) {
        return application.getEJBModules();
    }

    public ResourceAdapterModule[] getRAModules(J2EEApplication application) {
        return application.getRAModules();
    }


    public JCAResource[] getJCAResources(J2EEApplication application) {
        List list = new ArrayList();
        ResourceAdapterModule[] modules = application.getRAModules();
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            ResourceAdapter[] adapters = module.getResourceAdapterInstances();
            for (int j = 0; j < adapters.length; j++) {
                ResourceAdapter adapter = adapters[j];
                JCAResource[] resources = adapter.getJCAResourceImplementations();
                list.addAll(Arrays.asList(resources));
            }

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
        return module.getResourceAdapterInstances();
    }

    // resource adapter properties
    public JCAResource[] getRAResources(ResourceAdapter adapter) {
        return adapter.getJCAResourceImplementations();
    }

    // resource properties
    public JDBCDataSource[] getDataSource(JDBCResource resource) {
        return new JDBCDataSource[0];  //todo
    }

    public JDBCDriver[] getDriver(JDBCDataSource dataSource) {
        return new JDBCDriver[0];  //todo
    }

    public JCAConnectionFactory[] getConnectionFactories(JCAResource resource) {
        return resource.getConnectionFactoryInstances();
    }

    public JCAAdminObject[] getAdminObjects(JCAResource resource) {
        return resource.getAdminObjectInstances();
    }

    public JCAManagedConnectionFactory getManagedConnectionFactory(JCAConnectionFactory factory) {
        return factory.getManagedConnectionFactoryInstance();
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
                    if (type == null || type.getValue() == info.getType().getValue()) {
                        J2EEDeployedObject module = getModuleForConfiguration(info.getConfigID());
                        results.add(new ConfigurationData(info.getConfigID(), configuration, null, info.getState(), info.getType(), module == null ? null : kernel.getAbstractNameFor(module)));
                    }
                    if (includeChildModules && info.getType().getValue() == ConfigurationModuleType.EAR.getValue() && info.getState().toInt() == State.RUNNING_INDEX)
                    {
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
                        for (int k = 0; k < modules.length; k++) {
                            Object module = modules[k];
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
        if (config == null) {
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
            } else if (type.equals(ConfigurationModuleType.WAR)) {
                result = config.findGBean(new AbstractNameQuery(WebModule.class.getName()));
            } else {
                return null;
            }
            return (J2EEDeployedObject) kernel.getProxyManager().createProxy(result, getClass().getClassLoader());
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("Bad config ID: " + e.getMessage());
        }
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
}
