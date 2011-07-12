/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ejb.spi.HandleDelegate;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.spi.ResourceAdapter;

import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.openejb.cdi.ThreadSingletonServiceAdapter;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.cdi.OWBContextThreadListener;
import org.apache.openejb.cdi.ThreadSingletonService;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.mdb.InboundRecovery;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.osgi.core.BundleFinderFactory;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.GeronimoTransactionManagerFactory.GeronimoXAResourceWrapper;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.proxy.Jdk13ProxyFactory;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */

@GBean
public class OpenEjbSystemGBean implements OpenEjbSystem {
    private static final Logger log = LoggerFactory.getLogger(OpenEjbSystemGBean.class);
    private final ConfigurationFactory configurationFactory;
    private final Assembler assembler;
    private final Set<String> registeredResouceAdapters = new TreeSet<String>();
    private final ConcurrentMap<String,ResourceAdapterWrapper> processedResourceAdapterWrappers =  new ConcurrentHashMap<String,ResourceAdapterWrapper>() ;
    private final Kernel kernel;
    private final ClassLoader classLoader;
    // These are provided by the corba subsystem when it first initializes.  
    // Once we have a set, we ignore any additional notifications. 
    private ORB orb;
    private Properties properties; 
    
    public OpenEjbSystemGBean(RecoverableTransactionManager transactionManager) throws Exception {
        this(transactionManager, null, null, null, null, OpenEjbSystemGBean.class.getClassLoader(), new Properties());
    }

    public OpenEjbSystemGBean(@ParamReference(name = "TransactionManager", namingType = NameFactory.JTA_RESOURCE) RecoverableTransactionManager transactionManager,
                              @ParamReference(name = "ResourceAdapterWrappers", namingType = NameFactory.JCA_RESOURCE_ADAPTER) Collection<ResourceAdapterWrapper> resourceAdapters,
                              @ParamReference(name = "PersistenceUnitGBeans", namingType = NameFactory.PERSISTENCE_UNIT) Collection<PersistenceUnitGBean> persistenceUnitGBeans,
                              @ParamReference(name = "ApplicationJndis") Collection<ApplicationJndi> applicationJndis,
//                              @ParamReference(name = "OpenEjbContext")DeepBindableContext openejbContext,
                              @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                              @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                              @ParamAttribute(name = "properties") Properties properties) throws Exception {
        if (transactionManager == null) {
            throw new NullPointerException("transactionManager is null");
        }
        this.kernel = kernel;
        this.classLoader = classLoader;
        this.properties = properties;
        
        System.setProperty("openejb.geronimo", "true");
        System.setProperty("admin.disabled", "true");
        System.setProperty("openejb.logger.external", "true");

        setDefaultProperty("openejb.deploymentId.format", "{moduleUri}/{ejbName}");
        setDefaultProperty("openejb.jndiname.strategy.class", "org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy");
        setDefaultProperty("openejb.jndiname.format", "{ejbName}{interfaceType.annotationName}");
        setDefaultProperty("openejb.jndiname.failoncollision", "false");

//        System.setProperty("openejb.naming", "xbean");
        boolean offline = true;
        configurationFactory = new ConfigurationFactory(offline);
        final XBeanJndiFactory jndiFactory = new XBeanJndiFactory();
        assembler = new Assembler(jndiFactory);

        // install application server
        ApplicationServer applicationServer = new ServerFederation();
        SystemInstance.get().setComponent(ApplicationServer.class, applicationServer);

        // install transaction manager
        transactionManager = getRawService(kernel, transactionManager);
        TransactionServiceInfo transactionServiceInfo = new TransactionServiceInfo();
        PassthroughFactory.add(transactionServiceInfo, transactionManager);
        transactionServiceInfo.id = "Default Transaction Manager";
        transactionServiceInfo.service = "TransactionManager";
        assembler.createTransactionManager(transactionServiceInfo);
	    SystemInstance.get().setComponent(XAResourceWrapper.class, new GeronimoXAResourceWrapper());

        SystemInstance.get().setComponent(InboundRecovery.class, new GeronimoInboundRecovery(transactionManager));

        // install security service
        SecurityService securityService = new GeronimoSecurityService();
        SecurityServiceInfo securityServiceInfo = new SecurityServiceInfo();
        PassthroughFactory.add(securityServiceInfo, securityService);
        securityServiceInfo.id = "Default Security Service";
        securityServiceInfo.service = "SecurityService";
        assembler.createSecurityService(securityServiceInfo);

        // install proxy factory
        ProxyFactoryInfo proxyFactoryInfo = new ProxyFactoryInfo();
        proxyFactoryInfo.id = "Default JDK 1.3 ProxyFactory";
        proxyFactoryInfo.service = "ProxyFactory";
        proxyFactoryInfo.className = Jdk13ProxyFactory.class.getName();
        proxyFactoryInfo.properties = new Properties();
        assembler.createProxyFactory(proxyFactoryInfo);
        
        // add our thread context listener
        GeronimoThreadContextListener.init();

        SystemInstance.get().setComponent(FinderFactory.class, new BundleFinderFactory());
        SystemInstance.get().setComponent(ThreadSingletonService.class, new ThreadSingletonServiceAdapter());
        //probably should be in openejb...
        ThreadContext.addThreadContextListener(new OWBContextThreadListener());

        // process all resource adapters
        processResourceAdapterWrappers(resourceAdapters);
        processPersistenceUnitGBeans(persistenceUnitGBeans);

        if (applicationJndis instanceof ReferenceCollection) {
            ((ReferenceCollection)applicationJndis).addReferenceCollectionListener(new ReferenceCollectionListener() {
                @Override
                public void memberAdded(ReferenceCollectionEvent referenceCollectionEvent) {
                     jndiFactory.addGlobals(((ApplicationJndi)referenceCollectionEvent.getMember()).getGlobalMap());
                }

                @Override
                public void memberRemoved(ReferenceCollectionEvent referenceCollectionEvent) {
                    jndiFactory.removeGlobals(((ApplicationJndi) referenceCollectionEvent.getMember()).getGlobalMap());
                }
            });

        }
    }

    private void setDefaultProperty(String key, String value) {
        SystemInstance systemInstance = SystemInstance.get();


        String format = systemInstance.getProperty(key);
        if (format == null){
            systemInstance.setProperty(key, value);
        }
    }

    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    @SuppressWarnings({"unchecked"})
    private static <T> T getRawService(Kernel kernel, T proxy) {
        if (kernel == null) return proxy;

        AbstractName abstractName = kernel.getAbstractNameFor(proxy);
        if (abstractName == null) return proxy;

        try {
            Object service = kernel.getGBean(abstractName);
            return (T) service;
        } catch (GBeanNotFoundException e) {
        }

        return proxy;
    }

    private void processPersistenceUnitGBeans(Collection<PersistenceUnitGBean> persistenceUnitGBeans) {
        if (persistenceUnitGBeans == null) {
            return;
        }

        if (persistenceUnitGBeans instanceof ReferenceCollection) {
            ReferenceCollection referenceCollection = (ReferenceCollection) persistenceUnitGBeans;
            referenceCollection.addReferenceCollectionListener(new ReferenceCollectionListener() {
                public void memberAdded(ReferenceCollectionEvent event) {
                    addPersistenceUnitGBean((PersistenceUnitGBean) event.getMember());
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    removePersistenceUnitGBean((PersistenceUnitGBean) event.getMember());
                }

            });
        }
        for (PersistenceUnitGBean persistenceUnitGBean : persistenceUnitGBeans) {
            addPersistenceUnitGBean(persistenceUnitGBean);
        }
    }

    private void addPersistenceUnitGBean(PersistenceUnitGBean persistenceUnitGBean) {
        String unit = persistenceUnitGBean.getPersistenceUnitName();
        String rootUrl = persistenceUnitGBean.getPersistenceUnitRoot();
        String id = unit + " " + rootUrl.hashCode();
        Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
        try {
            context.bind("java:openejb/PersistenceUnit/" + id, persistenceUnitGBean.getEntityManagerFactory());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void removePersistenceUnitGBean(PersistenceUnitGBean persistenceUnitGBean) {
        String unit = persistenceUnitGBean.getPersistenceUnitName();
        String rootUrl = persistenceUnitGBean.getPersistenceUnitRoot();
        String id = unit + " " + rootUrl.hashCode();
        Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
        try {
            context.unbind("java:openejb/PersistenceUnit/" + id);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    private void processResourceAdapterWrappers(Collection<ResourceAdapterWrapper> resourceAdapterWrappers) {
        if (resourceAdapterWrappers == null) {
            return;
        }

        if (resourceAdapterWrappers instanceof ReferenceCollection) {
            ReferenceCollection referenceCollection = (ReferenceCollection) resourceAdapterWrappers;
            referenceCollection.addReferenceCollectionListener(new ReferenceCollectionListener() {
                public void memberAdded(ReferenceCollectionEvent event) {
                    addResourceAdapter((ResourceAdapterWrapper) event.getMember());
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    removeResourceAdapter((ResourceAdapterWrapper) event.getMember());
                }
            });
        }
        for (ResourceAdapterWrapper resourceAdapterWrapper : resourceAdapterWrappers) {
            addResourceAdapter(resourceAdapterWrapper);
        }

    }

    private void addResourceAdapter(ResourceAdapterWrapper resourceAdapterWrapper) {
        ResourceAdapter resourceAdapter = resourceAdapterWrapper.getResourceAdapter();
        if (resourceAdapter == null) {
            return;
        }
        if (registeredResouceAdapters.contains(resourceAdapterWrapper.getName())) {
            // already registered
            return;
        }
        registeredResouceAdapters.add(resourceAdapterWrapper.getName());
        
        Map<String, String> listenerToActivationSpecMap = resourceAdapterWrapper.getMessageListenerToActivationSpecMap();
        if (listenerToActivationSpecMap == null) {
            return;
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            for (Map.Entry<String, String> entry : listenerToActivationSpecMap.entrySet()) {
                String messageListenerInterface = entry.getKey();
                String activationSpecClass = entry.getValue();

                // only process RA if not previously processed
                String containerName = getResourceAdapterId(resourceAdapterWrapper) + "-" + messageListenerInterface;
                if (processedResourceAdapterWrappers.putIfAbsent(containerName,  resourceAdapterWrapper) == null) {
                    try {
                        // get default mdb config
                        ContainerInfo containerInfo = configurationFactory.configureService(MdbContainerInfo.class, containerName, null, null, "Container");
                        containerInfo.id = containerName;
                        containerInfo.displayName = containerName;
                        properties = (properties == null)?new Properties():properties;                        
                        String instanceLimit = (String)properties.get(containerName + "." + "InstanceLimit");                                                
                        if(instanceLimit != null){
                            containerInfo.properties.put("InstanceLimit", instanceLimit);
                        }

                        // set ra specific properties
                        containerInfo.properties.put("MessageListenerInterface",
                                resourceAdapter.getClass().getClassLoader().loadClass(messageListenerInterface));
                        containerInfo.properties.put("ActivationSpecClass",
                                resourceAdapter.getClass().getClassLoader().loadClass(activationSpecClass));
                        containerInfo.properties.put("ResourceAdapter", resourceAdapter);
                       // containerInfo.properties.put("TxRecovery", true);
 
                        // create the container
                        assembler.createContainer(containerInfo);
                    } catch (Exception e) {
                        log.error("Unable to deploy mdb container " + containerName, e);
                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private void removeResourceAdapter(ResourceAdapterWrapper resourceAdapterWrapper) {
        Map<String, String> listenerToActivationSpecMap = resourceAdapterWrapper.getMessageListenerToActivationSpecMap();
        if (listenerToActivationSpecMap != null) {
            for (String messageListenerInterface : listenerToActivationSpecMap.keySet()) {
                String containerName = getResourceAdapterId(resourceAdapterWrapper) + "-" + messageListenerInterface;
                processedResourceAdapterWrappers.remove(containerName);
                assembler.removeContainer(containerName);
            }
            registeredResouceAdapters.remove(resourceAdapterWrapper.getName());
        }
    }

    private String getResourceAdapterId(ResourceAdapterWrapper resourceAdapterWrapper) {
        String name = resourceAdapterWrapper.getName();
        try {
            ObjectName objectName = new ObjectName(name);
            Map properties = objectName.getKeyPropertyList();
            String shortName = (String) properties.get("name");
            String moduleName = (String) properties.get("ResourceAdapterModule");
            if (shortName != null && moduleName != null) {
                return moduleName + "." + shortName;
            }
        } catch (Exception ignored) {
        }
        return name;
    }

    public ContainerSystem getContainerSystem() {
        return assembler.getContainerSystem();
    }

    public Container createContainer(Class<? extends ContainerInfo> type, String serviceId, Properties declaredProperties, String providerId) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            ContainerInfo containerInfo = configurationFactory.configureService(type, serviceId, declaredProperties, providerId, "Container");
            assembler.createContainer(containerInfo);
            Container container = assembler.getContainerSystem().getContainer(serviceId);
            return container;
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public ClientInfo configureApplication(ClientModule clientModule) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(clientModule.getClassLoader());
        try {
            return configurationFactory.configureApplication(clientModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public AppInfo configureApplication(AppModule appModule) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
        try {
            return configurationFactory.configureApplication(appModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public EjbJarInfo configureApplication(EjbModule ejbModule) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ejbModule.getClassLoader());
        try {
            return configurationFactory.configureApplication(ejbModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void createClient(ClientInfo clientInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            assembler.createClient(clientInfo, classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public AppContext createApplication(AppInfo appInfo, ClassLoader classLoader, boolean start) throws NamingException, IOException, OpenEJBException {
        Set<AbstractName> names = kernel.listGBeans(new AbstractNameQuery(ResourceAdapterWrapper.class.getName()));
        for (AbstractName name : names) {
            try {
                ResourceAdapterWrapper resourceAdapterWrapper = (ResourceAdapterWrapper) kernel.getGBean(name);
                addResourceAdapter(resourceAdapterWrapper);
            } catch (GBeanNotFoundException ignored) {
            }
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return assembler.createApplication(appInfo, classLoader, start);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void removeApplication(AppInfo appInfo, ClassLoader classLoader) throws UndeployException, NoSuchApplicationException {
        assembler.destroyApplication(appInfo.path);
    }
    
    public BeanContext getDeploymentInfo(String deploymentId) {
        return getContainerSystem().getBeanContext(deploymentId);
    }

    public void setORBContext(ORB orb, HandleDelegate handleDelegate) {
        // this is only processed once, since these are global values. 
        if (this.orb == null) {
            this.orb = orb; 
            SystemInstance.get().setComponent(ORB.class, orb);
            SystemInstance.get().setComponent(HandleDelegate.class, handleDelegate);
        }
    }

}
