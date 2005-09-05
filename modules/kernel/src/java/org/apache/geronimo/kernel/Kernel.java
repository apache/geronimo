/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import java.util.Date;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * @version $Rev$ $Date$
 */
public interface Kernel {
    /**
     * The JMX name used by a Kernel to register itself when it boots.
     */
    ObjectName KERNEL = JMXUtil.getObjectName(":role=Kernel");

    /**
     * Get the name of this kernel
     *
     * @return the name of this kernel
     */
    String getKernelName();

    /**
     * Gets the dependency manager kernel service
     * @return the dependency manager or null if the kernel is not running
     */
    DependencyManager getDependencyManager();

    /**
     * Gets the lifecycle monitor kernel service
     * @return the lifecycle monitor or null if the kernel is not running
     */
    LifecycleMonitor getLifecycleMonitor();

    /**
     * Gets the proxy manager kernel service
     * @return the proxy manager or null if the kernel is not running
     */
    ProxyManager getProxyManager();

    /**
     * Load a specific GBean into this kernel.
     * This is intended for applications that are embedding the kernel.
     *
     * @param gbeanData the GBean to load
     * @param classLoader the class loader to use to load the gbean
     * @throws org.apache.geronimo.kernel.GBeanAlreadyExistsException if the name is already used
     * @throws org.apache.geronimo.kernel.InternalKernelException if there is a problem during registration
     */
    void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException;

    /**
     * Is there a GBean registered with the kernel under the specified name?
     * @param name the name to check
     * @return true if there is a gbean registered under the specified name; false otherwise
     */
    boolean isLoaded(ObjectName name);

    /**
     * Start a specific GBean.
     *
     * @param name the GBean to start
     * @throws org.apache.geronimo.kernel.GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Start a specific GBean and its children.
     *
     * @param name the GBean to start
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Stop a specific GBean.
     *
     * @param name the GBean to stop
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Unload a specific GBean.
     * This is intended for applications that are embedding the kernel.
     *
     * @param name the name of the GBean to unregister
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is a problem while unloading the GBean
     */
    void unloadGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Gets the state of the specified GBean.
     * @param name the name of the GBean
     * @return the state of the GBean
     * @throws GBeanNotFoundException if the GBean could not be found
     */
    int getGBeanState(ObjectName name) throws GBeanNotFoundException;

    /**
     * Gets the time the specified GBean was started
     * @param name the name of the GBean
     * @return the start time of the GBean or 0 if not running
     * @throws GBeanNotFoundException if the GBean could not be found
     */
    long getGBeanStartTime(ObjectName name) throws GBeanNotFoundException;

    /**
     * Is the specified GBean enabled?
     * @param name the name if the GBean
     * @return true if the gbean is enabled
     * @throws GBeanNotFoundException if the GBean could not be found
     */
    boolean isGBeanEnabled(ObjectName name) throws GBeanNotFoundException;

    /**
     * Sets the eneabled status of the specified GBean.  A disabled gbean can not be started, and
     * will not be started via startRecursive.
     * @param name the name if the GBean
     * @param enabled the new enabled status
     * @throws GBeanNotFoundException if the GBean could not be found
     */
    void setGBeanEnabled(ObjectName name, boolean enabled) throws GBeanNotFoundException;

    /**
     * Gets the ClassLoader used to register the specified GBean
     * @param name the name of the gbean from which the class loader should be extracted
     * @return the class loader associated with the specified GBean
     * @throws GBeanNotFoundException if the specified GBean is not registered with the kernel
     */
    ClassLoader getClassLoaderFor(ObjectName name) throws GBeanNotFoundException;

    /**
     * Return the GBeanInfo for a registered GBean instance.
     * @param name the name of the GBean whose info should be returned
     * @return the info for that instance
     * @throws GBeanNotFoundException if there is no instance with the supplied name
     */
    GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException;

    /**
     * Return the GBeanData for a GBean instance.
     * @param name the name of the GBean whose info should be returned
     * @return the info for that instance
     * @throws GBeanNotFoundException if there is no instance with the supplied name
     */
    GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException;

    /**
     * Returns a Set of all GBeans matching the object name pattern
     * @return a List of javax.management.ObjectName of matching GBeans registered with this kernel
     */
    Set listGBeans(ObjectName pattern);

    /**
     * Returns a Set of all GBeans matching the set of object name pattern
     * @return a List of javax.management.ObjectName of matching GBeans registered with this kernel
     */
    Set listGBeans(Set patterns);

    /**
     * Returns a Set of all GBeans matching any of the specified criteria
     * @return a List of javax.management.ObjectName of matching GBeans registered with this kernel
     */
    Set listGBeans(GBeanQuery query);

    /**
     * Gets the value of an attribute on the specified gbean
     * @param objectName the name of the gbean from which the attribute will be retrieved
     * @param attributeName the name of the attribute to fetch
     * @return the value of the attribute
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchAttributeException if the gbean does not contain the specified attribute
     * @throws Exception if the gbean throws an exception from the getter
     */
    Object getAttribute(ObjectName objectName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception;

    /**
     * Sets the value of an attribute on the specified gbean
     * @param objectName the name of the gbean from in which the new attribute value will be set
     * @param attributeName the name of the attribute to set
     * @param attributeValue the new value of the attribute
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchAttributeException if the gbean does not contain the specified attribute
     * @throws Exception if the gbean throws an exception from the setter
     */
    void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception;

    /**
     * Invokes a no-argument method on the specified GBean
     * @param objectName the name of the gbean from in which the new attribute value will be set
     * @param methodName the name of the method to invoke
     * @return the return value of the method or null if the specified method does not return a value
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchOperationException if the gbean does not have the specified operation
     * @throws InternalKernelException if an error occurs within the kernel itself
     * @throws Exception if the method throws an exception
     */
    Object invoke(ObjectName objectName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;

    /**
     * Invokes a method on the specified GBean with the specified arguments
     * @param objectName the name of the gbean from in which the new attribute value will be set
     * @param methodName the name of the method to invoke
     * @param args the arguments to pass to the method
     * @param types the types of the arguments; the types are used to determine the signature of the mehod that should be invoked
     * @return the return value of the method or null if the specified method does not return a value
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchOperationException if the gbean does not have the specified operation
     * @throws InternalKernelException if an error occurs within the kernel itself
     * @throws Exception if the method throws an exception
     */
    Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;

    /**
     * Assuming the argument represents a service running in the kernel,
     * returns an ObjectName for it.  If the argument is not a service or the
     * kernel cannot produce an ObjectName for it, returns null.
     */
    ObjectName getObjectNameFor(Object service);

    /**
     * Brings the kernel online
     * @throws Exception if the kernel can not boot
     */
    void boot() throws Exception;

    /**
     * Returns the time this kernel was last booted.
     * @return the time this kernel was last booted; null if the kernel has not been
     */
    Date getBootTime();

    /**
     * Registers a runnable to execute when the kernel is shutdown
     * @param hook a runnable to execute when the kernel is shutdown
     */
    void registerShutdownHook(Runnable hook);

    /**
     * Unregisters a runnable from the list to execute when the kernel is shutdown
     * @param hook the runnable that should be removed
     */
    void unregisterShutdownHook(Runnable hook);

    /**
     * Stops the kernel
     */
    void shutdown();

    /**
     * Has the kernel been booted
     * @return true if the kernel has been booted; false otherwise
     */
    boolean isRunning();

}
