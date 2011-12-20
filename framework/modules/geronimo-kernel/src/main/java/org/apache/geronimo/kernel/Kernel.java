/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.management.ObjectName;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev:386515 $ $Date$
 */
public interface Kernel {
    /**
     * The JMX name used by a Kernel to register itself when it boots.
     */
    ObjectName KERNEL = ObjectNameUtil.getObjectName(":role=Kernel");
    AbstractName KERNEL_NAME = new AbstractName(new Artifact("geronimo", "boot", "none", "car"), Collections.singletonMap("role", "kernel"), KERNEL);

    /**
     * Get the name of this kernel
     *
     * @return the name of this kernel
     */
    String getKernelName();

    /**
     * Gets the naming system used by this kernel.
     * @return the naming system used by this kernel
     */
    Naming getNaming();

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
     * @param bundleContext
     * @throws org.apache.geronimo.kernel.GBeanAlreadyExistsException if the name is already used
     * @throws org.apache.geronimo.kernel.InternalKernelException if there is a problem during registration
     */
    void loadGBean(GBeanData gbeanData, BundleContext bundleContext) throws GBeanAlreadyExistsException, InternalKernelException;

    /**
     * Is there a GBean registered with the kernel under the specified name?
     * @param name the name to check
     * @return true if there is a gbean registered under the specified name; false otherwise
     */
    boolean isLoaded(AbstractName name);
    boolean isLoaded(String shortName);
    boolean isLoaded(Class type);
    boolean isLoaded(String shortName, Class type);

    /**
     * Gets the specified GBean instance.
     *
     * @param name the GBean instance to get
     * @throws org.apache.geronimo.kernel.GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    Object getGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    Object getGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    <T> T getGBean(Class<T> type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    <T> T getGBean(String shortName, Class<T> type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Start a specific GBean.
     *
     * @param name the GBean to start
     * @throws org.apache.geronimo.kernel.GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    void startGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void startGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void startGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void startGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Start a specific GBean and its children.
     *
     * @param name the GBean to start
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    void startRecursiveGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void startRecursiveGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void startRecursiveGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void startRecursiveGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Is there a GBean registered with the kernel under the specified name and is it running?
     * @param name the name to check
     * @return true if there is a gbean registered under the specified name and is it running; false otherwise
     */
    boolean isRunning(AbstractName name);
    boolean isRunning(String shortName);
    boolean isRunning(Class type);
    boolean isRunning(String shortName, Class type);

    /**
     * Stop a specific GBean.
     *
     * @param name the GBean to stop
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is not state manageable or if there is a general error
     * @throws IllegalStateException If the gbean is disabled
     */
    void stopGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void stopGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void stopGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void stopGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Unload a specific GBean.
     * This is intended for applications that are embedding the kernel.
     *
     * @param name the name of the GBean to unregister
     * @throws GBeanNotFoundException if the GBean could not be found
     * @throws InternalKernelException if there GBean is a problem while unloading the GBean
     */
    void unloadGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void unloadGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void unloadGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    void unloadGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;

    /**
     * Gets the state of the specified GBean.
     * @param name the name of the GBean
     * @return the state of the GBean
     * @throws GBeanNotFoundException if the GBean could not be found
     */
    int getGBeanState(AbstractName name) throws GBeanNotFoundException;
    int getGBeanState(String shortName) throws GBeanNotFoundException;
    int getGBeanState(Class type) throws GBeanNotFoundException;
    int getGBeanState(String shortName, Class type) throws GBeanNotFoundException;

    /**
     * Gets the time the specified GBean was started
     * @param name the name of the GBean
     * @return the start time of the GBean or 0 if not running
     * @throws GBeanNotFoundException if the GBean could not be found
     */
    long getGBeanStartTime(AbstractName name) throws GBeanNotFoundException;
    long getGBeanStartTime(String shortName) throws GBeanNotFoundException;
    long getGBeanStartTime(Class type) throws GBeanNotFoundException;
    long getGBeanStartTime(String shortName, Class type) throws GBeanNotFoundException;

    /**
     * Gets the ClassLoader used to register the specified GBean
     * @param name the name of the gbean from which the class loader should be extracted
     * @return the class loader associated with the specified GBean
     * @throws GBeanNotFoundException if the specified GBean is not registered with the kernel
     */
    Bundle getBundleFor(AbstractName name) throws GBeanNotFoundException;
    Bundle getBundleFor(String shortName) throws GBeanNotFoundException;
    Bundle getBundleFor(Class type) throws GBeanNotFoundException;
    Bundle getBundleFor(String shortName, Class type) throws GBeanNotFoundException;

    /**
     * Return the GBeanInfo for a registered GBean instance.
     * @param name the name of the GBean whose info should be returned
     * @return the info for that instance
     * @throws GBeanNotFoundException if there is no instance with the supplied name
     */
    GBeanInfo getGBeanInfo(AbstractName name) throws GBeanNotFoundException;
    GBeanInfo getGBeanInfo(String shortName) throws GBeanNotFoundException;
    GBeanInfo getGBeanInfo(Class type) throws GBeanNotFoundException;
    GBeanInfo getGBeanInfo(String shortName, Class type) throws GBeanNotFoundException;

    /**
     * Return the GBeanData for a GBean instance.
     * @param name the name of the GBean whose info should be returned
     * @return the info for that instance
     * @throws GBeanNotFoundException if there is no instance with the supplied name
     */
    GBeanData getGBeanData(AbstractName name) throws GBeanNotFoundException, InternalKernelException;
    GBeanData getGBeanData(String shortName) throws GBeanNotFoundException, InternalKernelException;
    GBeanData getGBeanData(Class type) throws GBeanNotFoundException, InternalKernelException;
    GBeanData getGBeanData(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException;

    /**
     * Gets the AbstractNames of all GBeans matching the abstractNameQuery.
     * @param abstractNameQuery the query to execute
     * @return the AbstractNames of all matching GBeans
     */
    Set<AbstractName> listGBeans(AbstractNameQuery abstractNameQuery);

    /**
     * Returns a Set of all GBeans matching the set of object name pattern
     * @param abstractNameQueries the queries to execute
     * @return a List of AbstractNameName of matching GBeans registered with this kernel
     */
    Set<AbstractName> listGBeans(Set abstractNameQueries);

    /**
     * Gets the value of an attribute on the specified gbean
     * @param name the name of the gbean from which the attribute will be retrieved
     * @param attributeName the name of the attribute to fetch
     * @return the value of the attribute
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchAttributeException if the gbean does not contain the specified attribute
     * @throws Exception if the gbean throws an exception from the getter
     */
    Object getAttribute(AbstractName name, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    Object getAttribute(String shortName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    Object getAttribute(Class type, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    Object getAttribute(String shortName, Class type, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception;

    /**
     * Sets the value of an attribute on the specified gbean
     * @param name the name of the gbean from in which the new attribute value will be set
     * @param attributeName the name of the attribute to set
     * @param attributeValue the new value of the attribute
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchAttributeException if the gbean does not contain the specified attribute
     * @throws Exception if the gbean throws an exception from the setter
     */
    void setAttribute(AbstractName name, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    void setAttribute(String shortName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    void setAttribute(Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    void setAttribute(String shortName, Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception;

    /**
     * Invokes a no-argument method on the specified GBean
     * @param name the name of the gbean from in which the new attribute value will be set
     * @param methodName the name of the method to invoke
     * @return the return value of the method or null if the specified method does not return a value
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchOperationException if the gbean does not have the specified operation
     * @throws InternalKernelException if an error occurs within the kernel itself
     * @throws Exception if the method throws an exception
     */
    Object invoke(AbstractName name, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    Object invoke(String shortName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    Object invoke(Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    Object invoke(String shortName, Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;

    /**
     * Invokes a method on the specified GBean with the specified arguments
     * @param name the name of the gbean from in which the new attribute value will be set
     * @param methodName the name of the method to invoke
     * @param args the arguments to pass to the method
     * @param types the types of the arguments; the types are used to determine the signature of the mehod that should be invoked
     * @return the return value of the method or null if the specified method does not return a value
     * @throws GBeanNotFoundException if there is not a gbean under the specified name
     * @throws NoSuchOperationException if the gbean does not have the specified operation
     * @throws InternalKernelException if an error occurs within the kernel itself
     * @throws Exception if the method throws an exception
     */
    Object invoke(AbstractName name, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    Object invoke(String shortName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    Object invoke(Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    Object invoke(String shortName, Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;

    /**
     * Assuming the argument represents a service running in the kernel,
     * returns an AbstractName for it.  If the argument is not a service or the
     * kernel cannot produce an AbstractName for it, returns null.
     */
    AbstractName getAbstractNameFor(Object service);

    /**
     * Assuming the argument represents a service running in the kernel,
     * returns the short name of the service.  If the argument is not a service, returns null.
     */
    String getShortNameFor(Object service);

    /**
     * Brings the kernel online
     * @throws Exception if the kernel can not boot
     * @param bundleContext
     */
    void boot(BundleContext bundleContext) throws Exception;

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

    /**
     * @deprecated Use AbstractName version instead
     */
    Object getGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException;
    /**
     * @deprecated Use AbstractName version instead
     */
    int getGBeanState(ObjectName name) throws GBeanNotFoundException;
    /**
     * @deprecated Use AbstractName version instead
     */
    GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException;
    /**
     * Returns a Set with elements of type ObjectName
     *
     * @deprecated Use AbstractNameQuery version instead
     */
    Set<AbstractName> listGBeans(ObjectName pattern);
    /**
     * @deprecated Use AbstractName version instead
     */
    Object getAttribute(ObjectName name, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception;
    /**
     * @deprecated Use AbstractName version instead
     */
    Object invoke(ObjectName name, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;
    /**
     * @deprecated Use AbstractName version instead
     */
    Object invoke(ObjectName name, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception;

    String getStateReason(AbstractName abstractName);
}
