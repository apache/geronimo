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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Revision: 1.10 $ $Date: 2004/07/12 06:07:52 $
 */
public class ConfigurationManagerImpl implements ConfigurationManager, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);
    private final Kernel kernel;
    private final Collection stores;

    public ConfigurationManagerImpl(Kernel kernel, Collection stores) {
        this.kernel = kernel;
        this.stores = stores;
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
                List ids = store.listConfiguations();
                List result = new ArrayList(ids.size());
                for (int j = 0; j < ids.size(); j++) {
                    URI configID = (URI) ids.get(j);
                    ObjectName configName;
                    try {
                        configName = getConfigObjectName(configID);
                    } catch (MalformedObjectNameException e) {
                        throw new AssertionError("Store returned invalid configID: " + configID);
                    }
                    State state;
                    if (kernel.isLoaded(configName)) {
                        try {
                            state = State.fromInteger((Integer) kernel.getAttribute(configName, "state"));
                        } catch (Exception e) {
                            state = null;
                        }
                    } else {
                        state = null;
                    }
                    result.add(new ConfigurationInfo(storeName, configID, state));
                }
                return result;
            }
        }
        throw new NoSuchStoreException("No such store: " + storeName);
    }

    public boolean isLoaded(URI configID) {
        try {
            ObjectName name = getConfigObjectName(configID);
            return kernel.isLoaded(name);
        } catch (MalformedObjectNameException e) {
            return false;
        }
    }

    public ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        List storeSnapshot = getStores();

        for (int i = 0; i < storeSnapshot.size(); i++) {
            ConfigurationStore store = (ConfigurationStore) storeSnapshot.get(i);
            if (store.containsConfiguration(configID)) {
                GBeanMBean config = store.getConfiguration(configID);
                URL baseURL = store.getBaseURL(configID);
                return load(config, baseURL);
            }
        }
        throw new NoSuchConfigException("No configuration with id: " + configID);
    }

    public ObjectName load(GBeanMBean config, URL rootURL) throws InvalidConfigException {
        URI configID;
        try {
            configID = (URI) config.getAttribute("ID");
        } catch (Exception e) {
            throw new InvalidConfigException("Cannot get config ID", e);
        }
        ObjectName configName;
        try {
            configName = getConfigObjectName(configID);
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Cannot convert ID to ObjectName: ", e);
        }
        load(config, rootURL, configName);
        return configName;
    }

    public void load(GBeanMBean config, URL rootURL, ObjectName configName) throws InvalidConfigException {
        try {
            kernel.loadGBean(configName, config);
        } catch (InvalidConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to register configuraton", e);
        }

        try {
            config.setAttribute("baseURL", rootURL);
        } catch (Exception e) {
            try {
                kernel.unloadGBean(configName);
            } catch (Exception ignored) {
                // ignore
            }
            throw new InvalidConfigException("Cannot set baseURL", e);
        }
        log.info("Loaded Configuration " + configName);
    }

    public List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            LinkedList ancestors = new LinkedList();
            while (configID != null && !isLoaded(configID)) {
                ObjectName name = load(configID);
                ancestors.addFirst(name);
                configID = (URI) kernel.getAttribute(name, "parentID");
            }
            return ancestors;
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

    public void unload(URI configID) throws NoSuchConfigException {
        ObjectName configName;
        try {
            configName = getConfigObjectName(configID);
        } catch (MalformedObjectNameException e) {
            throw new NoSuchConfigException("Cannot convert ID to ObjectName: ", e);
        }
        unload(configName);
    }

    public void unload(ObjectName configName) throws NoSuchConfigException {
        try {
            kernel.unloadGBean(configName);
        } catch (InstanceNotFoundException e) {
            throw new NoSuchConfigException("No config registered: " + configName, e);
        }
        log.info("Unloaded Configuration " + configName);
    }

    private List getStores() {
        return new ArrayList(stores);
    }

    public ObjectName getConfigObjectName(URI configID) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configID.toString()));
    }

    public void doStart() {
    }

    private static final ObjectName CONFIG_QUERY = JMXUtil.getObjectName("geronimo.config:*");

    public void doStop() {
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
                    } catch (InstanceNotFoundException e) {
                        // ignore
                    } catch (InvalidConfigException e) {
                        log.warn("Could not stop configuration: " + configName, e);
                    }
                    try {
                        kernel.unloadGBean(configName);
                    } catch (InstanceNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfigurationManagerImpl.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("Stores", ConfigurationStore.class);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "Stores"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
