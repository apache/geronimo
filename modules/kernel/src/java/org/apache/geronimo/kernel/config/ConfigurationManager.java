/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/24 06:05:37 $
 */
public class ConfigurationManager {
    private static final Log log = LogFactory.getLog(ConfigurationManager.class);
    private final Kernel kernel;
    private final Collection stores;

    public ConfigurationManager() {
        kernel = null;
        stores = null;
    }

    public ConfigurationManager(Kernel kernel, Collection stores) {
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

    /**
     * Load the supplied Configuration into the Kernel and override the default JMX name.
     * This method should be used with discretion as it is possible to create
     * Configurations that cannot be located by management or monitoring tools.
     * @param config the GBeanMBean representing the Configuration
     * @param rootURL the URL to be used to resolve relative paths in the configuration
     * @param configName the JMX ObjectName to register the Configuration under
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if the Configuration is not valid
     */
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

    public static ObjectName getConfigObjectName(URI configID) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configID.toString()));
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfigurationManager.class);
        infoFactory.addReference("Kernel", Kernel.class);
        infoFactory.addReference("Stores", ConfigurationStore.class);
        infoFactory.addOperation("isLoaded", new Class[]{URI.class});
        infoFactory.addOperation("load", new Class[]{URI.class});
        infoFactory.addOperation("load", new Class[]{GBeanMBean.class, URL.class});
        infoFactory.addOperation("load", new Class[]{GBeanMBean.class, URL.class, ObjectName.class});
        infoFactory.addOperation("loadRecursive", new Class[]{URI.class});
        infoFactory.addOperation("unload", new Class[]{URI.class});
        infoFactory.addOperation("unload", new Class[]{ObjectName.class});

        infoFactory.setConstructor(
                new String[]{"Kernel", "Stores"},
                new Class[]{Kernel.class, Collection.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
