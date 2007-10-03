/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.openejb.deployment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.openejb.EjbContainer;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Rev$ $Date$
 */
public class OfflineOpenEjbSystemGBean {
    private static final Log log = LogFactory.getLog(OfflineOpenEjbSystemGBean.class);
    private final ConfigurationFactory configurationFactory;
    private final Set<String> registeredResouceAdapters = new TreeSet<String>();
    private final ConcurrentMap<String,ResourceAdapterWrapper> processedResourceAdapterWrappers =  new ConcurrentHashMap<String,ResourceAdapterWrapper>() ;
    private final Collection<PersistenceUnitGBean> persistenceUnitGBeans;
    private final ClassLoader classLoader;

    public OfflineOpenEjbSystemGBean() throws Exception {
        this(null, null, null, null, OfflineOpenEjbSystemGBean.class.getClassLoader());
    }
    public OfflineOpenEjbSystemGBean(Configuration configuration, Collection<ResourceAdapterWrapper> resourceAdapters, Collection<PersistenceUnitGBean> persistenceUnitGBeans, Kernel kernel, ClassLoader classLoader) throws Exception {
        this.classLoader = classLoader;
        if (persistenceUnitGBeans == null) {
            this.persistenceUnitGBeans = Collections.emptySet();
        } else {
            this.persistenceUnitGBeans = persistenceUnitGBeans;
        }
        System.setProperty("duct tape","");
        System.setProperty("admin.disabled", "true");
        System.setProperty("openejb.logger.external", "true");

        setDefaultProperty("openejb.deploymentId.format", "{moduleId}/{ejbName}");
        setDefaultProperty("openejb.jndiname.strategy.class", "org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy");
        setDefaultProperty("openejb.jndiname.format", "{deploymentId}/{interfaceClass}");

        System.setProperty("openejb.naming", "xbean");

        OpenEjbConfiguration openEjbConfiguration = new OpenEjbConfiguration();
        openEjbConfiguration.containerSystem = new ContainerSystemInfo();
        addContainerInfos(configuration, openEjbConfiguration.containerSystem);
        openEjbConfiguration.facilities = new FacilitiesInfo();
        boolean offline = true;
        configurationFactory = new ConfigurationFactory(offline, openEjbConfiguration);

        // process all resource adapters
        processResourceAdapterWrappers(resourceAdapters);
    }

    private void addContainerInfos(Configuration configuration, ContainerSystemInfo containerSystem) throws OpenEJBException {
        LinkedHashSet<GBeanData> containerDatas = configuration.findGBeanDatas(Collections.singleton(new AbstractNameQuery(EjbContainer.class.getName())));
        for (GBeanData containerData: containerDatas) {
            Class<? extends ContainerInfo> infoClass = (Class<? extends ContainerInfo>) containerData.getAttribute("infoType");
            if (infoClass == null) {
                String type = (String) containerData.getAttribute("type");
                infoClass = EjbContainer.getInfoType(type);
            }
            String serviceId = (String) containerData.getAttribute("id");
            Properties declaredProperties = (Properties) containerData.getAttribute("properties");
            String providerId = (String) containerData.getAttribute("provider");
            ContainerInfo containerInfo = configurationFactory.configureService(infoClass, serviceId, declaredProperties, providerId, "Container");
            containerSystem.containers.add(containerInfo);
        }
    }

    private void setDefaultProperty(String key, String value) {
        SystemInstance systemInstance = SystemInstance.get();


        String format = systemInstance.getProperty(key);
        if (format == null){
            systemInstance.setProperty(key, value);
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
                        ContainerInfo containerInfo = configurationFactory.configureService(MdbContainerInfo.class);
                        containerInfo.id = containerName;
                        containerInfo.displayName = containerName;

                        // set ra specific properties
                        containerInfo.properties.put("MessageListenerInterface",
                                resourceAdapter.getClass().getClassLoader().loadClass(messageListenerInterface));
                        containerInfo.properties.put("ActivationSpecClass",
                                resourceAdapter.getClass().getClassLoader().loadClass(activationSpecClass));
                        containerInfo.properties.put("ResourceAdapter", resourceAdapter);

                        // create the container
//                        assembler.createContainer(containerInfo);
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
//                assembler.removeContainer(containerName);
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

    public AppInfo configureApplication(AppModule appModule) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
        try {
            return configurationFactory.configureApplication(appModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OfflineOpenEjbSystemGBean.class);
        infoBuilder.addReference("ResourceAdapterWrappers", ResourceAdapterWrapper.class);
        infoBuilder.addReference("PersistenceUnitGBeans", PersistenceUnitGBean.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.setConstructor(new String[] {
                "ResourceAdapterWrappers",
                "PersistenceUnitGBeans",
                "kernel",
                "classLoader",
        });
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
