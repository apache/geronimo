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

package org.apache.geronimo.kernel.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.ArtifactManager;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Collections;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev$ $Date$
 * @see EditableConfigurationManager
 */
public class ConfigurationManagerImpl implements ConfigurationManager, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);
    protected final Kernel kernel;
    private final Collection stores;
    protected final ManageableAttributeStore attributeStore;
    protected final PersistentConfigurationList configurationList;
    private final ShutdownHook shutdownHook;
    private final ArtifactManager artifactManager;
    private final ArtifactResolver artifactResolver;
    private final ClassLoader classLoader;
    private static final ObjectName CONFIGURATION_NAME_QUERY;

    static {
        try {
            CONFIGURATION_NAME_QUERY = new ObjectName("geronimo.config:*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("could not create object name... bug", e);
        }
    }

    public ConfigurationManagerImpl(Kernel kernel,
            Collection stores,
            ManageableAttributeStore attributeStore,
            PersistentConfigurationList configurationList,
            ArtifactManager artifactManager,
            ArtifactResolver artifactResolver,
            ClassLoader classLoader) {
        this.kernel = kernel;
        this.stores = stores;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        this.artifactManager = artifactManager;
        this.artifactResolver = artifactResolver;
        this.classLoader = classLoader;
        shutdownHook = new ShutdownHook(kernel);
    }

    public List listStores() {
        List storeSnapshot = getStores();
        List result = new ArrayList(storeSnapshot.size());
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            result.add(JMXUtil.getObjectName(store.getObjectName()));
        }
        return result;
    }

    public List listConfigurations(ObjectName storeName) throws NoSuchStoreException {
        List storeSnapshot = getStores();
        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (storeName.equals(JMXUtil.getObjectName(store.getObjectName()))) {
                return store.listConfigurations();
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    public Configuration getConfiguration(Artifact configId) {
        try {
            ObjectName objectName = Configuration.getConfigurationObjectName(configId);
            Configuration configuration = (Configuration) kernel.getProxyManager().createProxy(objectName, Configuration.class);
            return configuration;
        } catch (InvalidConfigException e) {
            return null;
        }
    }

    public boolean isLoaded(Artifact configId) {
        try {
            ObjectName configurationName = Configuration.getConfigurationObjectName(configId);
            return State.RUNNING_INDEX == kernel.getGBeanState(configurationName);
        } catch (Exception e) {
            return false;
        }
    }

    public List loadConfiguration(Artifact configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (isLoaded(configID)) {
            return Collections.EMPTY_LIST;
        }

        // load the GBeanData for the new configuration
        GBeanData gbeanData = loadConfigurationGBeanData(configID);

        // load the configuration
        return loadConfiguration(configID, gbeanData);
    }

    public Configuration loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, IOException, InvalidConfigException {
        return loadConfiguration(configurationData, null);
    }

    public Configuration loadConfiguration(ConfigurationData configurationData, ConfigurationStore configurationStore) throws NoSuchConfigException, IOException, InvalidConfigException {
        Artifact id = configurationData.getId();
        ObjectName objectName = Configuration.getConfigurationObjectName(id);
        try {
            GBeanData gbeanData = new GBeanData(objectName, Configuration.GBEAN_INFO);
            gbeanData.setAttribute("type", configurationData.getModuleType());
            Environment environment = configurationData.getEnvironment();
            gbeanData.setAttribute("environment", environment);
            gbeanData.setAttribute("gBeanState", Configuration.storeGBeans(configurationData.getGBeans()));
            gbeanData.setAttribute("classPath", configurationData.getClassPath());
            gbeanData.setAttribute("configurationStore", configurationStore);
            gbeanData.setReferencePattern("Repositories", new ObjectName("*:j2eeType=Repository,*"));
            if (artifactManager != null) {
                gbeanData.setReferencePattern("ArtifactManager", kernel.getProxyManager().getProxyTarget(artifactManager));
            }
            if (artifactResolver != null) {
                gbeanData.setReferencePattern("ArtifactResolver", kernel.getProxyManager().getProxyTarget(artifactResolver));
            }

            loadConfiguration(id, gbeanData);
            Configuration configuration = getConfiguration(id);
            return configuration;
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException(e);
        }
    }

    private List loadConfiguration(Artifact configID, GBeanData gbeanData) throws NoSuchConfigException, IOException, InvalidConfigException {
        Set preloaded = kernel.listGBeans(CONFIGURATION_NAME_QUERY);
        for (Iterator it = preloaded.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                if (kernel.getGBeanState(name) != State.RUNNING_INDEX) {
                    it.remove();
                }
            } catch (GBeanNotFoundException e) {
                it.remove();
            }
        }

        // load configurations from the new child to the parents
        LinkedList ancestors = new LinkedList();
        loadRecursive(configID, gbeanData, ancestors, preloaded);

        // start the unloaded configurations from the prents to the chidren
        List parentToChild = new LinkedList(ancestors);
        for (Iterator iterator = parentToChild.iterator(); iterator.hasNext();) {
            Artifact parent = (Artifact) iterator.next();
            ObjectName configurationName = Configuration.getConfigurationObjectName(parent);

            // start configuration and assure it started
            try {
                kernel.startGBean(configurationName);
                if (State.RUNNING_INDEX != kernel.getGBeanState(configurationName)) {
                    throw new InvalidConfigurationException("Configuration " + parent + " failed to start");
                }
            } catch (InvalidConfigurationException e) {
                throw e;
            } catch (GBeanNotFoundException e) {
                throw new InvalidConfigException("Unable to start configuration gbean", e);
            }

            log.debug("Loaded Configuration " + configurationName);

            // todo move this to startConfiguration when deployment code has been update to not search kernel
            Configuration configuration = (Configuration) kernel.getProxyManager().createProxy(configurationName, Configuration.class);
            registerGBeans(configuration);
        }

        // todo clean up after failure
        return ancestors;
    }

    private void loadRecursive(Artifact configId, GBeanData gbeanData, LinkedList ancestors, Set preloaded) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            ObjectName name = Configuration.getConfigurationObjectName(configId);
            if (preloaded.contains(name)) {
                return;
            }

            try {
                kernel.loadGBean(gbeanData, classLoader);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to register configuration", e);
            }

            log.debug("Loaded Configuration " + configId);

            //put the earliest ancestors first, even if we have already started them.
            ancestors.remove(configId);
            ancestors.addFirst(configId);

            Environment environment = (Environment) kernel.getAttribute(name, "environment");
            LinkedHashSet imports = environment.getImports();
            for (Iterator iterator = imports.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                if (!artifact.isResolved()) {
                    if (artifactResolver == null) {
                        throw new IllegalStateException("Parent artifact is not resolved, and no artifact resolver is available: " + artifact);
                    }
                    imports = artifactResolver.resolve(imports);
                    environment.setImports(imports);
                    break;
                }
            }

            for (Iterator iterator = environment.getImports().iterator(); iterator.hasNext();) {
                Artifact parent = (Artifact) iterator.next();
                if (!isLoaded(parent)) {
                    GBeanData parentGBeanData = loadConfigurationGBeanData(parent);
                    loadRecursive(parent, parentGBeanData, ancestors, preloaded);
                }
            }
        } catch (NoSuchConfigException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (InvalidConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException(e);
        }
    }

    private GBeanData loadConfigurationGBeanData(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStores();

        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configId)) {
                GBeanData configurationGBean = store.loadConfiguration(configId);

                ObjectName configurationName = Configuration.getConfigurationObjectName(configId);
                configurationGBean.setName(configurationName);
                configurationGBean.setAttribute("configurationStore", store);

                return configurationGBean;
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configId);
    }

    private void registerGBeans(Configuration configuration) throws InvalidConfigException, NoSuchConfigException, MalformedURLException {
        // load the attribute overrides from the attribute store
        Collection gbeans = configuration.getGBeans().values();
        if (attributeStore != null) {
            gbeans = attributeStore.setAttributes(configuration.getId(), gbeans, configuration.getConfigurationClassLoader());
        }

        // register all the GBeans
        ConfigurationStore configurationStore = configuration.getConfigurationStore();
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();

            // copy the gbeanData object as not to mutate the original
            gbeanData = new GBeanData(gbeanData);

            // If the GBean has a configurationBaseUrl attribute, set it
            // todo remove this when web app cl are config. cl.
            GAttributeInfo attribute = gbeanData.getGBeanInfo().getAttribute("configurationBaseUrl");
            if (attribute != null && attribute.getType().equals("java.net.URL")) {
                URL baseURL = configurationStore.resolve(configuration.getId(), URI.create(""));
                gbeanData.setAttribute("configurationBaseUrl", baseURL);
            }

            // add a dependency from the gbean to the configuration
            gbeanData.getDependencies().add(Configuration.getConfigurationObjectName(configuration.getId()));

            log.trace("Registering GBean " + gbeanData.getName());

            try {
                kernel.loadGBean(gbeanData, configuration.getConfigurationClassLoader());
            } catch (GBeanAlreadyExistsException e) {
                throw new InvalidConfigException(e);
            }
        }
    }

    public void startConfiguration(Artifact configId) throws InvalidConfigException {
        if (!isLoaded(configId)) {
            throw new InvalidConfigurationException("Configuration " + configId + " failed to start");
        }

        Configuration configuration = getConfiguration(configId);
        startConfiguration(configuration);
    }

    public void startConfiguration(Configuration configuration) throws InvalidConfigException {
        if (!isLoaded(configuration.getId())) {
            throw new InvalidConfigurationException("Configuration " + configuration.getId() + " failed to start");
        }

        // todo move this from loadConfiguration when deployment code has been update to not search kernel
        // registerGBeans(configuration);

        try {
            // start the gbeans
            Map gbeans = configuration.getGBeans();
            for (Iterator iterator = gbeans.keySet().iterator(); iterator.hasNext();) {
                ObjectName gbeanName = (ObjectName) iterator.next();
                if (kernel.isGBeanEnabled(gbeanName)) {
                    kernel.startRecursiveGBean(gbeanName);
                }
            }

            // assure all of the gbeans are started
            for (Iterator iterator = gbeans.keySet().iterator(); iterator.hasNext();) {
                ObjectName gbeanName = (ObjectName) iterator.next();
                if (State.RUNNING_INDEX != kernel.getGBeanState(gbeanName)) {
                    throw new InvalidConfigurationException("Configuration " + configuration.getId() + " failed to start because gbean " + gbeanName + " did not start");
                }
            }
        } catch (GBeanNotFoundException e) {
            throw new InvalidConfigException(e);
        }
        // todo clean up after failure

        if (configurationList != null) {
            configurationList.addConfiguration(configuration.getId().toString());
        }
    }

    public void stopConfiguration(Artifact configId) throws InvalidConfigException {
        Configuration configuration = getConfiguration(configId);

        stopConfiguration(configuration);
    }

    public void stopConfiguration(Configuration configuration) throws InvalidConfigException {
        try {
            Collection gbeans = configuration.getGBeans().keySet();

            // stop the gbeans
            for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
                ObjectName gbeanName = (ObjectName) iterator.next();
                kernel.stopGBean(gbeanName);
            }
        } catch (Exception e) {
            throw new InvalidConfigException("Could not stop gbeans in configuration", e);
        }
        if (configurationList != null) {
            configurationList.removeConfiguration(configuration.getId().toString());
        }
    }

    public void unloadConfiguration(Configuration configuration) throws NoSuchConfigException {
        unloadConfiguration(configuration.getId());
    }

    public void unloadConfiguration(Artifact configId) throws NoSuchConfigException {
        ObjectName configName;
        try {
            configName = Configuration.getConfigurationObjectName(configId);
        } catch (InvalidConfigException e) {
            throw new NoSuchConfigException("Could not construct configuration object name", e);
        }
        try {
            if (State.RUNNING_INDEX == kernel.getGBeanState(configName)) {
                try {
                    Map gbeans = (Map) kernel.getAttribute(configName, "GBeans");

                    // unload the gbeans
                    // todo move this to stopConfiguration
                    for (Iterator iterator = gbeans.keySet().iterator(); iterator.hasNext();) {
                        ObjectName gbeanName = (ObjectName) iterator.next();
                        kernel.unloadGBean(gbeanName);
                    }
                } catch (Exception e) {
                    throw new InvalidConfigException("Could not stop gbeans in configuration", e);
                }
                kernel.stopGBean(configName);
            }
            kernel.unloadGBean(configName);
        } catch (GBeanNotFoundException e) {
            throw new NoSuchConfigException("No config registered: " + configName, e);
        } catch (Exception e) {
            throw new NoSuchConfigException("Problem unloading config: " + configName, e);
        }
    }

    private List getStores() {
        return new ArrayList(stores);
    }

    public void doStart() {
        kernel.registerShutdownHook(shutdownHook);
    }

    private static final ObjectName CONFIG_QUERY = JMXUtil.getObjectName("geronimo.config:*");

    public void doStop() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    public void doFail() {
        log.error("Cofiguration manager failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigurationManagerImpl.class, "ConfigurationManager");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("Stores", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("AttributeStore", ManageableAttributeStore.class, ManageableAttributeStore.ATTRIBUTE_STORE);
        infoFactory.addReference("PersistentConfigurationList", PersistentConfigurationList.class, PersistentConfigurationList.PERSISTENT_CONFIGURATION_LIST);
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addReference("ArtifactResolver", ArtifactResolver.class, "ArtifactResolver");
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList", "ArtifactManager", "ArtifactResolver", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private static class ShutdownHook implements Runnable {
        private final Kernel kernel;

        public ShutdownHook(Kernel kernel) {
            this.kernel = kernel;
        }

        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(CONFIG_QUERY);
                if (configs.isEmpty()) {
                    return;
                }
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    ObjectName configName = (ObjectName) i.next();
                    if (kernel.isLoaded(configName)) {
                        try {
                            kernel.stopGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        } catch (InternalKernelException e) {
                            log.warn("Could not stop configuration: " + configName, e);
                        }
                        try {
                            kernel.unloadGBean(configName);
                        } catch (GBeanNotFoundException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }
}
