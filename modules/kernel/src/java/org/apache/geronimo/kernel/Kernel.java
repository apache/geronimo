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
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanName;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.jmx.JMXLifecycleBroadcaster;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.lifecycle.BasicLifecycleMonitor;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitorFlyweight;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.registry.BasicGBeanRegistry;
import org.apache.geronimo.kernel.registry.GBeanRegistry;


/**
 * The core of a Geronimo instance.
 * A Kernel is responsible for managing the Configurations that comprise a
 * Geronimo system and exposing them using JMX. Each Kernel is associated
 * with an MBeanServer that is used to register the Configurations themselves
 * and the MBeans they define.
 * <p/>
 * Dependencies between MBeans are handled by a dedicated DependencyManager
 * that is responsible for tracking those dependencies and ensuring that the
 * dependent objects follow the appropriate lifecycle and receive appropriate
 * notifications.
 * <p/>
 * The Kernel also provides a ConfigurationStore which is used to stage
 * installed Configurations (providing a local filesystem based classpath) and
 * used hold the persistent state of each Configuration. This allows
 * Configurations to restart in the event of system failure.
 * 
 * TODO: Describe the order of method invocation (e.g. if loadGbean may be before boot)
 *
 * @version $Rev$ $Date$
 */
public class Kernel {

    /**
     * The JMX name used by a Kernel to register itself when it boots.
     * todo drop "geronimo.boot:" from this name so the kernel shows up in the kernel default domain
     */
    public static final ObjectName KERNEL = JMXUtil.getObjectName("geronimo.boot:role=Kernel");

    /**
     * Index of kernel (Weak) references by kernel name
     */
    private static final Map kernels = new HashMap();

    /**
     * ReferenceQueue that watches the weak references to our kernels
     */
    private static final ReferenceQueue queue = new ReferenceQueue();

    /**
     * Helper objects for invoke and getAttribute
     */
    private static final String[] NO_TYPES = new String[0];
    private static final Object[] NO_ARGS = new Object[0];

    /**
     * Name of the configuration manager
     * todo drop "geronimo.boot:" from this name so the configuration manger shows up in the kernel default domain
     */
    private static final ObjectName CONFIGURATION_MANAGER_NAME = JMXUtil.getObjectName("geronimo.boot:role=ConfigurationManager");

    /**
     * Te pattern we use to find all the configuation stores registered with the kernel
     */
    private static final ObjectName CONFIGURATION_STORE_PATTERN = JMXUtil.getObjectName("*:j2eeType=ConfigurationStore,*");

    /**
     * Name of this kernel
     */
    private final String kernelName;

    /**
     * The log
     */
    private Log log;

    /**
     * Is this kernel running?
     */
    private boolean running;

    /**
     * The timestamp when the kernel was started
     */
    private Date bootTime;

    /**
     * The gbean registry
     */
    private final GBeanRegistry gbeanRegistry;

    /**
     * Listeners for when the kernel shutdown
     */
    private LinkedList shutdownHooks = new LinkedList();

    /**
     * This manager is used by the kernel to manage dependencies between gbeans
     */
    private DependencyManager dependencyManager;

    /**
     * The kernel uses this manager to load configurations which are collections of GBeans
     */
    private ConfigurationManager configurationManager;

    /**
     * The GBeanMbean that wraps the configuration manager
     */
    private GBeanInstance configurationManagerInstance;

    /**
     * Monitors the lifecycle of all gbeans.
     */
    private BasicLifecycleMonitor lifecycleMonitor;
    private LifecycleMonitor publicLifecycleMonitor;

    /**
     * This factory gbean proxies, and tracks all proxies in the system
     */
    private ProxyManager proxyManager;

    /**
     * No-arg constructor allowing this class to be used as a GBean reference.
     */
    protected Kernel() {
        kernelName = null;
        gbeanRegistry = null;
    }

    /**
     * Construct a Kernel with the specified name and GBeanRegistry implementation.
     *
     * @param kernelName the name of the kernel
     * @param gbeanRegistry the GBeanRegistry implementation to use for this contianer
     */
    public Kernel(String kernelName, GBeanRegistry gbeanRegistry) {
        if (kernelName.indexOf(':') >= 0 || kernelName.indexOf('*') >= 0 || kernelName.indexOf('?') >= 0) {
            throw new IllegalArgumentException("Kernel name may not contain a ':', '*' or '?' character");
        }
        this.kernelName = kernelName;
        this.gbeanRegistry = gbeanRegistry;
    }

    /**
     * Construct a Kernel with the specified name and an unspecified GBeanRegistry implementation.
     *
     * @param kernelName the name of the kernel
     */
    public Kernel(String kernelName) {
        this(kernelName, new BasicGBeanRegistry());
    }

    public String getKernelName() {
        return kernelName;
    }

    public static Set getKernelNames() {
        synchronized(kernels) {
            return Collections.unmodifiableSet(kernels.keySet());
        }
    }
    
    /**
     * Get a particular kernel indexed by a name
     *
     * @param name the name of the kernel to be obtained
     * @return the kernel that was registered with that name
     */
    public static Kernel getKernel(String name) {
        if (name == null) {
            return getSingleKernel();
        }
        synchronized (kernels) {
            processQueue();
            KernelReference ref = (KernelReference) kernels.get(name);
            if (ref != null) {
                return (Kernel) ref.get();
            }
        }
        return null;
    }

    /**
     * Obtain the single kernel that's registered.
     * <p/>
     * <p>This method assumes that there is only one kernel registered and will throw an
     * <code>IllegalStateException</code> if more than one has been registered.
     *
     * @return the single kernel that's registered
     * @throws IllegalStateException if more than one
     */
    public static Kernel getSingleKernel() {
        synchronized (kernels) {
            processQueue();

            int size = kernels.size();
            if (size > 1) throw new IllegalStateException("More than one kernel has been registered.");
            if (size < 1) return null;

            Kernel result = (Kernel) ((KernelReference) kernels.values().iterator().next()).get();
            if (result == null) {
                kernels.clear();
            }
            return result;
        }
    }

    /**
     * @deprecated this will be removed as when we add generalized dependencies to gbeans... the only current user is Configuration
     */
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Gets the lifecycle monitor.
     * @deprecated don't use this yet... it may change or go away
     */
    public LifecycleMonitor getLifecycleMonitor() {
        return publicLifecycleMonitor;
    }

    /**
     * Gets the proxy manager.
     * @deprecated don't use this yet... it may change or go away
     */
    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(objectName));
        return gbeanInstance.getAttribute(attributeName);
    }

    public void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(objectName));
        gbeanInstance.setAttribute(attributeName, attributeValue);
    }

    public Object invoke(ObjectName objectName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invoke(objectName, methodName, NO_ARGS, NO_TYPES);
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(objectName));
        return gbeanInstance.invoke(methodName, args, types);
    }

    public boolean isLoaded(ObjectName name) {
        return gbeanRegistry.isRegistered(new GBeanName(name));
    }

    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(name));
        return gbeanInstance.getGBeanInfo();
    }

    public GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(name));
        return gbeanInstance.getGBeanData();
    }

    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException {
        ObjectName objectName = gbeanData.getName();
        GBeanInstance gbeanInstance = new GBeanInstance(gbeanData, this, dependencyManager, lifecycleMonitor.createLifecycleBroadcaster(objectName), classLoader);
        gbeanRegistry.register(gbeanInstance);
    }

    /**
     * @deprecated use loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public void loadGBean(ObjectName name, org.apache.geronimo.gbean.jmx.GBeanMBean gbean) throws GBeanAlreadyExistsException, InternalKernelException {
        GBeanData gbeanData = gbean.getGBeanData();
        gbeanData.setName(name);
        ClassLoader classLoader = gbean.getClassLoader();
        loadGBean(gbeanData, classLoader);
    }

    public void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(name));
        gbeanInstance.start();
    }

    public void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(name));
        gbeanInstance.startRecursive();
    }

    public void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(name));
        gbeanInstance.stop();
    }

    public void unloadGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanName gbeanName = new GBeanName(name);
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(gbeanName);
        gbeanInstance.die();
        gbeanRegistry.unregister(gbeanName);
    }

    public Set listGBeans(ObjectName pattern) {
        String domain = (pattern == null || pattern.isDomainPattern()) ? null : pattern.getDomain();
        Map props = pattern == null ? null : pattern.getKeyPropertyList();
        Set gbeans = gbeanRegistry.listGBeans(domain, props);
        Set result = new HashSet(gbeans.size());
        for (Iterator i = gbeans.iterator(); i.hasNext();) {
            GBeanInstance instance = (GBeanInstance) i.next();
            result.add(instance.getObjectNameObject());
        }
        return result;
    }

    public Set listGBeans(Set patterns) {
        Set gbeans = new HashSet();
        for (Iterator iterator = patterns.iterator(); iterator.hasNext();) {
            ObjectName pattern = (ObjectName) iterator.next();
            gbeans.addAll(listGBeans(pattern));
        }
        return gbeans;
    }

    public List listConfigurationStores() {
        return getConfigurationManager().listStores();
    }

    public List listConfigurations(ObjectName storeName) throws NoSuchStoreException {
        return getConfigurationManager().listConfigurations(storeName);
    }

    public ObjectName startConfiguration(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException, InternalKernelException {
        ObjectName configName = getConfigurationManager().load(configID);
		try {
		    startRecursiveGBean(configName);
		} catch (GBeanNotFoundException e) {
		    // should not happen as we just loaded it
		    throw new InvalidConfigException(e);
		}
		return configName;
    }

    public void stopConfiguration(URI configID) throws NoSuchConfigException, InternalKernelException {
        ConfigurationManager configurationManager = getConfigurationManager();
        try {
            ObjectName configName = Configuration.getConfigurationObjectName(configID);
            stopGBean(configName);
        } catch (MalformedObjectNameException e) {
            throw new NoSuchConfigException(e);
        } catch (GBeanNotFoundException e) {
            throw new NoSuchConfigException(e);
        }
        configurationManager.unload(configID);
    }

    public int getConfigurationState(URI configID) throws NoSuchConfigException, InternalKernelException {
        GBeanInstance gbeanInstance = null;
        try {
            ObjectName configName = Configuration.getConfigurationObjectName(configID);
            gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(configName));
        } catch (MalformedObjectNameException e) {
            throw new NoSuchConfigException(e);
        } catch (GBeanNotFoundException e) {
            throw new NoSuchConfigException(e);
        }
        return gbeanInstance.getState();
    }

    /**
     * Boot this Kernel, triggering the instantiation of the MBeanServer and DependencyManager,
     * and the registration of ConfigurationStore
     *
     * @throws java.lang.Exception if the boot fails
     */
    public void boot() throws Exception {
        if (running) {
            return;
        }
        bootTime = new Date();
        log = LogFactory.getLog(Kernel.class.getName());
        log.info("Starting boot");

        // todo cleanup when boot fails
        synchronized (kernels) {
            if (kernels.containsKey(kernelName)) {
                throw new IllegalStateException("A kernel is already running this kernel name: " + kernelName);
            }
            kernels.put(kernelName, new KernelReference(kernelName, this));
        }

        gbeanRegistry.start(this);

        lifecycleMonitor = new BasicLifecycleMonitor(this);
        publicLifecycleMonitor = new LifecycleMonitorFlyweight(lifecycleMonitor);
        dependencyManager = new DependencyManager(publicLifecycleMonitor);
        proxyManager = new ProxyManager(this);

        // set up the data for the new configuration manager instance
        GBeanData configurationData = new GBeanData(CONFIGURATION_MANAGER_NAME, ConfigurationManagerImpl.GBEAN_INFO);
        configurationData.setReferencePatterns("Stores", Collections.singleton(CONFIGURATION_STORE_PATTERN));

        // create the connfiguration manager instance
        JMXLifecycleBroadcaster lifecycleBroadcaster = new JMXLifecycleBroadcaster(CONFIGURATION_MANAGER_NAME, lifecycleMonitor.createLifecycleBroadcaster(CONFIGURATION_MANAGER_NAME));
        configurationManagerInstance = new GBeanInstance(configurationData, this, dependencyManager, lifecycleBroadcaster, getClass().getClassLoader());
        configurationManagerInstance.start();
        configurationManager = (ConfigurationManager) configurationManagerInstance.getTarget();
        assert configurationManager != null: "ConfigurationManager failed to start";
        gbeanRegistry.register(configurationManagerInstance);

        // load and start the kernel gbean
        GBeanData kernelGBeanData = new GBeanData(KERNEL, KernelGBean.GBEAN_INFO);
        loadGBean(kernelGBeanData, getClass().getClassLoader());
        startGBean(KERNEL);

        running = true;
        log.info("Booted");
    }

    public Date getBootTime() {
        return bootTime;
    }

    public void registerShutdownHook(Runnable hook) {
        assert hook != null : "Shutdown hook was null";
        synchronized (shutdownHooks) {
            shutdownHooks.add(hook);
        }
    }

    public void unregisterShutdownHook(Runnable hook) {
        synchronized (shutdownHooks) {
            shutdownHooks.remove(hook);
        }
    }

    /**
     * Shut down this kernel instance, unregistering the MBeans and releasing
     * the MBeanServer.
     */
    public void shutdown() {
        if (!running) {
            return;
        }
        running = false;
        log.info("Starting kernel shutdown");

        notifyShutdownHooks();
        shutdownConfigManager();

        gbeanRegistry.stop();

        dependencyManager.close();
        dependencyManager = null;

        synchronized (this) {
            notify();
        }

        synchronized (kernels) {
            kernels.remove(kernelName);
        }

        log.info("Kernel shutdown complete");
    }

    private void notifyShutdownHooks() {
        while (!shutdownHooks.isEmpty()) {
            Runnable hook;
            synchronized (shutdownHooks) {
                hook = (Runnable) shutdownHooks.removeFirst();
            }
            try {
                hook.run();
            } catch (Throwable e) {
                log.warn("Error from kernel shutdown hook", e);
            }
        }
    }

    private void shutdownConfigManager() {
        configurationManager = null;
        if (configurationManagerInstance != null) {
            try {
                configurationManagerInstance.stop();
            } catch (Exception e) {
                // ignore
            }
            try {
                gbeanRegistry.unregister(new GBeanName(CONFIGURATION_MANAGER_NAME));
            } catch (Exception e) {
                // ignore
            }
            configurationManagerInstance = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public ClassLoader getClassLoaderFor(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = gbeanRegistry.getGBeanInstance(new GBeanName(name));
        return gbeanInstance.getClassLoader();
    }

    private static void processQueue() {
        KernelReference kernelRef;
        while ((kernelRef = (KernelReference) queue.poll()) != null) {
            synchronized (kernels) {
                kernels.remove(kernelRef.key);
            }
        }
    }

    private static class KernelReference extends WeakReference {
        private final Object key;

        public KernelReference(Object key, Object kernel) {
            super(kernel, queue);
            this.key = key;
        }
    }
}
