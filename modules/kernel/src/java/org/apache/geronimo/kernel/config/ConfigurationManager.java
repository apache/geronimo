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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 * 
 * 
 * @version $Revision: 1.5 $ $Date: 2004/06/01 16:06:50 $
 */
public interface ConfigurationManager {
    boolean isLoaded(URI configID);

    ObjectName getConfigObjectName(URI configID) throws MalformedObjectNameException;

    ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    ObjectName load(GBeanMBean config, URL rootURL) throws InvalidConfigException;

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
