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

package org.apache.geronimo.kernel;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;

/**
 * @version $Revision: 1.12 $ $Date: 2004/06/04 04:35:20 $
 */
public interface KernelMBean {
    /**
     * Get the MBeanServer used by this kernel
     *
     * @return the MBeanServer used by this kernel
     */
    MBeanServer getMBeanServer();

    /**
     * Get the name of this kernel
     *
     * @return the name of this kernel
     */
    String getKernelName();

    /**
     * Load a specific GBean into this kernel.
     * This is intended for applications that are embedding the kernel.
     *
     * @param name the name to register the GBean under
     * @param gbean the GBean to register
     * @throws InstanceAlreadyExistsException if the name is already used
     * @throws InvalidConfigException if there is a problem during registration
     */
    void loadGBean(ObjectName name, GBeanMBean gbean) throws InstanceAlreadyExistsException, InvalidConfigException;

    /**
     * Start a specific GBean.
     *
     * @param name the GBean to start
     * @throws InstanceNotFoundException if the GBean could not be found
     */
    void startGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException;

    /**
     * Start a specific GBean and its children.
     *
     * @param name the GBean to start
     * @throws InstanceNotFoundException if the GBean could not be found
     */
    void startRecursiveGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException;

    /**
     * Stop a specific GBean.
     *
     * @param name the GBean to stop
     * @throws InstanceNotFoundException if the GBean could not be found
     */
    void stopGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException;

    /**
     * Unload a specific GBean.
     * This is intended for applications that are embedding the kernel.
     *
     * @param name the name of the GBean to unregister
     * @throws InstanceNotFoundException if the GBean could not be found
     */
    void unloadGBean(ObjectName name) throws InstanceNotFoundException;

    boolean isRunning();

    ConfigurationManager getConfigurationManager();

    /**
     * Return a list of the stores this kernel knows about.
     * @return a List<ObjectName> of the stores this kernel controls
     */
    List listConfigurationStores();

    /**
     * Return info about the configurations in a store.
     * @param storeName the store
     * @return a List<ConfigurationInfo> of information about the store's configurations
     * @throws NoSuchStoreException if this store does not exist
     */
    List listConfigurations(ObjectName storeName) throws NoSuchStoreException;

    ObjectName startConfiguration(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException;

    void stopConfiguration(URI configID) throws NoSuchConfigException;

    Object getAttribute(ObjectName objectName, String attributeName) throws Exception;

    void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws Exception;

    Object invoke(ObjectName objectName, String methodName) throws Exception;

    Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception;

    boolean isLoaded(ObjectName name);

    /**
     * Return the names of GBeans that match the query.
     * @param query the query to be performed
     * @return a Set<ObjectName> of the names of online GBeans that match the query
     */
    Set listGBeans(ObjectName query);
}
