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
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;

/**
 * @version $Rev$ $Date$
 */
public interface KernelMBean {
    /**
     * Returns the time this kernel was last booted.
     * @return the time this kernel was last booted; null if the kernel has not been 
     */
    Date getBootTime();

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
     * @param gbeanData the GBean to load
     * @param classLoader the class loader to use to load the gbean
     * @throws GBeanAlreadyExistsException if the name is already used
     * @throws InternalKernelException if there is a problem during registration
     */
    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException;

    /**
     * Load a specific GBean into this kernel.
     * This is intended for applications that are embedding the kernel.
     *
     * @param name the name to register the GBean under
     * @param gbean the GBean to register
     * @throws GBeanAlreadyExistsException if the name is already used
     * @throws InternalKernelException if there is a problem during registration
     * @deprecated use loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    void loadGBean(ObjectName name, GBeanMBean gbean) throws GBeanAlreadyExistsException, InternalKernelException;

    /**
     * Start a specific GBean.
     *
     * @param name the GBean to start
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     */
    void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException;

    /**
     * Start a specific GBean and its children.
     *
     * @param name the GBean to start
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     */
    void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException;

    /**
     * Stop a specific GBean.
     *
     * @param name the GBean to stop
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     */
    void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException;

    /**
     * Unload a specific GBean.
     * This is intended for applications that are embedding the kernel.
     *
     * @param name the name of the GBean to unregister
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is a problem while unloading the GBean
     */
    void unloadGBean(ObjectName name) throws GBeanNotFoundException;

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

    int getConfigurationState(URI configId) throws NoSuchConfigException;

    Object getAttribute(ObjectName objectName, String attributeName) throws Exception;

    void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws Exception;

    Object invoke(ObjectName objectName, String methodName) throws Exception;

    Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception;

    boolean isLoaded(ObjectName name);

    /**
     * Return the GBean info for a gbean instance.
     * @param name the name of the gbean whose info should be returned
     * @return the info for that instance
     * @throws GBeanNotFoundException if there is no instance with the supplied name
     */
    GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException;

    /**
     * Return the names of GBeans that match the pattern.
     * @param pattern the name pattern to match
     * @return a Set<ObjectName> of the names of online GBeans that match the pattern
     * @throws InternalKernelException if a problem occures while searching
     */
    Set listGBeans(ObjectName pattern) throws InternalKernelException;

    /**
     * Return all of the names of GBeans that match the set of patterns.
     * @param patterns a set of name patterns to match
     * @return a Set<ObjectName> of the names of online GBeans that match the patterns
     * @throws InternalKernelException if a problem occures while searching
     */
    Set listGBeans(Set patterns) throws InternalKernelException;

    void registerShutdownHook(Runnable hook);

    void unregisterShutdownHook(Runnable hook);

    void shutdown();

    /**
     * Gets the class loader use for a GBean
     * @param name name of the GBean
     * @return the class loader used to create the GBean
     * @throws GBeanNotFoundException if there is no instance with the supplied name
     * @throws InternalKernelException if there was a problem getting the class loader
     */
    ClassLoader getClassLoaderFor(ObjectName name) throws GBeanNotFoundException, InternalKernelException;

    /**
     * Gets the gbean data for the gbean held by this gbean mbean.
     * @return the gbean data
     * @throws GBeanNotFoundException if no such gbean exists with the specified name
     */
    GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException;
}
