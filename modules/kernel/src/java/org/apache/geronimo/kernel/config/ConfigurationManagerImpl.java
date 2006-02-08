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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    private static final ObjectName CONFIGURATION_NAME_QUERY;

    static {
        try {
            CONFIGURATION_NAME_QUERY = new ObjectName("geronimo.config:*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("could not create object name... bug", e);
        }
    }

    public ConfigurationManagerImpl(Kernel kernel, Collection stores, ManageableAttributeStore attributeStore, PersistentConfigurationList configurationList) {
        this.kernel = kernel;
        this.stores = stores;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
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

    public boolean isLoaded(Artifact configID) {
        try {
            ObjectName name = Configuration.getConfigurationObjectName(configID);
            return kernel.isLoaded(name);
        } catch (InvalidConfigException e) {
            //todo really?
            return false;
        }
    }

    public ObjectName load(Artifact configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStores();

        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configID)) {
                ObjectName configName = store.loadConfiguration(configID);
                return configName;
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configID);
    }

    public void loadGBeans(Artifact configID) throws InvalidConfigException {
        ObjectName configName = Configuration.getConfigurationObjectName(configID);
        try {
            kernel.startGBean(configName);
            kernel.invoke(configName, "loadGBeans", new Object[]{attributeStore}, new String[]{ManageableAttributeStore.class.getName()});
        } catch (Exception e) {
            throw new InvalidConfigException("Could not extract gbean data from configuration", e);
        }
    }

    public void start(Artifact configID) throws InvalidConfigException {
        ObjectName configName = Configuration.getConfigurationObjectName(configID);
        try {
            kernel.invoke(configName, "startRecursiveGBeans");
        } catch (Exception e) {
            throw new InvalidConfigException("Could not start gbeans in configuration", e);
        }
        if (configurationList != null) {
            configurationList.addConfiguration(configID.toString());
        }
    }

    public void stop(Artifact configID) throws InvalidConfigException {
        ObjectName configName = Configuration.getConfigurationObjectName(configID);
        try {
            kernel.invoke(configName, "stopGBeans");
        } catch (Exception e) {
            throw new InvalidConfigException("Could not stop gbeans in configuration", e);
        }
        if (configurationList != null) {
            configurationList.removeConfiguration(configID.toString());
        }
    }

    public List loadRecursive(Artifact configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        LinkedList ancestors = new LinkedList();
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
        loadRecursive(configID, ancestors, preloaded);
        return ancestors;
    }

    private void loadRecursive(Artifact configID, LinkedList ancestors, Set preloaded) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            ObjectName name = Configuration.getConfigurationObjectName(configID);
            if (preloaded.contains(name)) {
                return;
            }
            if (!isLoaded(configID)) {
                load(configID);
            }
            //put the earliest ancestors first, even if we have already started them.
            ancestors.remove(configID);
            ancestors.addFirst(configID);
            Artifact[] parents = (Artifact[]) kernel.getAttribute(name, "parentId");
            if (parents != null) {
                for (int i = 0; i < parents.length; i++) {
                    Artifact parent = parents[i];
                    loadRecursive(parent, ancestors, preloaded);
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

    public void unload(Artifact configID) throws NoSuchConfigException {
        ObjectName configName;
        try {
            configName = Configuration.getConfigurationObjectName(configID);
        } catch (InvalidConfigException e) {
            throw new NoSuchConfigException("Could not construct configuration object name", e);
        }
        try {
            if (State.RUNNING_INDEX == kernel.getGBeanState(configName)) {
                kernel.invoke(configName, "unloadGBeans");
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
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores", "AttributeStore", "PersistentConfigurationList"});
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
