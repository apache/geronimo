/**
 *
 * Copyright 2004 The Apache Software Foundation
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
import java.util.List;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public interface ConfigurationManager {
    boolean isLoaded(URI configID);

    /**
     * Return a list of the stores this manager knows about.
     * @return a List<ObjectName> of the stores this manager controls
     */
    List listStores();

    /**
     * Return a list of the configurations in a specific store.
     * @param store the store to list
     * @return a List<ConfigurationInfo> of all the configurations in the store
     * @throws NoSuchStoreException if the store could not be located
     */
    List listConfigurations(ObjectName store) throws NoSuchStoreException;

    ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    ObjectName load(GBeanMBean config, URL rootURL) throws InvalidConfigException;

    void load(GBeanData config, URL rootURL, ClassLoader classLoader) throws InvalidConfigException;

    /**
     * Load the supplied Configuration into the Kernel and override the default JMX name.
     * This method should be used with discretion as it is possible to create
     * Configurations that cannot be located by management or monitoring tools.
     *
     * @param config the GBeanMBean representing the Configuration
     * @param rootURL the URL to be used to resolve relative paths in the configuration
     * @param configName the JMX ObjectName to register the Configuration under
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if the Configuration is not valid
     */
    void load(GBeanMBean config, URL rootURL, ObjectName configName) throws InvalidConfigException;

    List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    void unload(URI configID) throws NoSuchConfigException;

    void unload(ObjectName configName) throws NoSuchConfigException;

}
