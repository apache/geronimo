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
import java.util.Collection;
import java.util.HashSet;
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
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/02 05:33:03 $
 */
public class ConfigurationManagerImpl implements ConfigurationManager {
    private static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);
    private final Kernel kernel;
    private final Collection stores;

    public ConfigurationManagerImpl(Kernel kernel, Collection stores) {
        this.kernel = kernel;
        this.stores = stores;
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
        Set storeSnapshot = getStoreSnapshot();

        for (Iterator iterator = storeSnapshot.iterator(); iterator.hasNext();) {
            ConfigurationStore store = (ConfigurationStore) iterator.next();
            if (store.containsConfiguration(configID)) {
                GBeanMBean config = store.getConfiguration(configID);
                URL baseURL = store.getBaseURL(configID);
                return load(config, baseURL);
            }
        }
        throw new NoSuchConfigException("A configuration with the specifiec id could not be found: " + configID);
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
            config.setAttribute("BaseURL", rootURL);
        } catch (Exception e) {
            try {
                kernel.unloadGBean(configName);
            } catch (Exception ignored) {
                // ignore
            }
            throw new InvalidConfigException("Cannot set BaseURL", e);
        }
        log.info("Loaded Configuration " + configName);
    }

    public List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            LinkedList ancestors = new LinkedList();
            while (configID != null && !isLoaded(configID)) {
                ObjectName name = load(configID);
                ancestors.addFirst(name);
                configID = (URI) kernel.getAttribute(name, "ParentID");
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

    private Set getStoreSnapshot() {
        Set storeSnapshot = new HashSet(stores);
        if (storeSnapshot.size() == 0) {
            throw new UnsupportedOperationException("There are no installed ConfigurationStores");
        }
        return storeSnapshot;
    }

    public ObjectName getConfigObjectName(URI configID) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configID.toString()));
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfigurationManagerImpl.class);
        infoFactory.addReference("Kernel", Kernel.class);
        infoFactory.addReference("Stores", ConfigurationStore.class);
        infoFactory.addInterface(ConfigurationManager.class);
        infoFactory.setConstructor(new String[]{"Kernel", "Stores"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
