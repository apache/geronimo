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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import javax.management.Attribute;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.gbean.jmx.JMXLifecycleBroadcaster;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.proxy.ProxyManager;


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
 * Configurations to restart in he event of system failure.
 *
 * @version $Rev$ $Date$
 */
public class Kernel extends NotificationBroadcasterSupport implements KernelMBean {

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
    private static final ObjectName CONFIGURATION_STORE_PATTERN = JMXUtil.getObjectName("*:role=ConfigurationStore,*");

    /**
     * Name of this kernel
     */
    private final String kernelName;

    /**
     * JMX domain name of this kernel
     */
    private final String domainName;

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
     * The MBean server used by this kernel
     */
    private MBeanServer mbServer;

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
     * @deprecated don't use this yet... it may go away
     */
    private LifecycleMonitor lifecycleMonitor;

    /**
     * This factory gbean proxies, and tracks all proxies in the system
     * @deprecated don't use this yet... it may go away
     */
    private ProxyManager proxyManager;

    /**
     * No-arg constructor allowing this class to be used as a GBean reference.
     */
    public Kernel() {
        kernelName = null;
        domainName = null;
    }

    /**
     * Construct a Kernel which does not have a config store.
     *
     * @param kernelName the domain name to be used for the JMX MBeanServer
     */
    public Kernel(String kernelName) {
        this.kernelName = kernelName;
        this.domainName = kernelName;
    }

    /**
     * Construct a Kernel which does not have a config store.
     *
     * @param kernelName the name of the kernel that uniquely indentifies the kernel in a VM
     * @param domainName the domain name to be used for the JMX MBeanServer
     * @deprecated we are dropping the ability to have multiple kernels in a single mbean server, as the kernels will
     * stomp on each others namespace
     */
    public Kernel(String kernelName, String domainName) {
        this.kernelName = kernelName;
        this.domainName = domainName;
    }

    public MBeanServer getMBeanServer() {
        return mbServer;
    }

    public String getKernelName() {
        return kernelName;
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
        return lifecycleMonitor;
    }

    /**
     * Gets the proxy manager.
     * @deprecated don't use this yet... it may change or go away
     */
    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, InternalKernelException, Exception {
        try {
            return mbServer.getAttribute(objectName, attributeName);
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new GBeanNotFoundException(objectName.getCanonicalName());
            } else if (cause instanceof AttributeNotFoundException) {
                throw new NoSuchAttributeException(cause.getMessage());
            } else if (cause instanceof JMException) {
                throw new InternalKernelException(cause);
            } else if (cause instanceof JMRuntimeException) {
                throw new InternalKernelException(cause);
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new InternalKernelException("Unknown throwable", cause);
            }
        }
    }

    public void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, InternalKernelException, Exception {
        try {
            mbServer.setAttribute(objectName, new Attribute(attributeName, attributeValue));
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new GBeanNotFoundException(objectName.getCanonicalName());
            } else if (cause instanceof AttributeNotFoundException) {
                throw new NoSuchAttributeException(cause.getMessage());
            } else if (cause instanceof JMException) {
                throw new InternalKernelException(cause);
            } else if (cause instanceof JMRuntimeException) {
                throw new InternalKernelException(cause);
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new InternalKernelException("Unknown throwable", cause);
            }
        }
    }

    public Object invoke(ObjectName objectName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invoke(objectName, methodName, NO_ARGS, NO_TYPES);
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        try {
            return mbServer.invoke(objectName, methodName, args, types);
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new GBeanNotFoundException(objectName.getCanonicalName());
            } else if (cause instanceof NoSuchMethodException) {
                throw new NoSuchOperationException(cause.getMessage());
            } else if (cause instanceof JMException) {
                throw new InternalKernelException(cause);
            } else if (cause instanceof JMRuntimeException) {
                throw new InternalKernelException(cause);
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new InternalKernelException("Unknown throwable", cause);
            }
        }
    }

    private Throwable unwrapJMException(Throwable cause) {
        while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }


    public boolean isLoaded(ObjectName name) {
        try {
            return mbServer != null && mbServer.isRegistered(name);
        } catch (RuntimeException e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanInfo) getAttribute(name, "gbeanInfo");
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (InternalKernelException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanData) getAttribute(name, GBeanInstance.GBEAN_DATA);
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (InternalKernelException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException {
        try {
            GBeanMBean gbean = new GBeanMBean(this, gbeanData, classLoader);
            mbServer.registerMBean(gbean, gbeanData.getName());
        } catch (InstanceAlreadyExistsException e) {
            throw new GBeanAlreadyExistsException("A GBean is alreayd registered witht then name " + gbeanData.getName());
        } catch (Exception e) {
            throw new InternalKernelException("Error loading GBean " + gbeanData.getName().getCanonicalName(), unwrapJMException(e));
        }
    }

    /**
     * @deprecated use loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public void loadGBean(ObjectName name, GBeanMBean gbean) throws GBeanAlreadyExistsException, InternalKernelException {
        try {
            mbServer.registerMBean(gbean, name);
        } catch (InstanceAlreadyExistsException e) {
            throw new GBeanAlreadyExistsException(name.getCanonicalName());
        } catch (Exception e) {
            throw new InternalKernelException("Error loading GBean " + name.getCanonicalName(), unwrapJMException(e));
        }
    }

    public void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            invoke(name, "start");
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (InternalKernelException e) {
            throw e;
        } catch (NoSuchOperationException e) {
            throw new InternalKernelException("GBean is not state manageable: " + name.getCanonicalName(), e);
        } catch (Exception e) {
            throw new InternalKernelException("Invalid GBean configuration for " + name, unwrapJMException(e));
        }
    }

    public void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            invoke(name, "startRecursive");
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (InternalKernelException e) {
            throw e;
        } catch (NoSuchOperationException e) {
            throw new InternalKernelException("GBean is not state manageable: " + name.getCanonicalName(), e);
        } catch (Exception e) {
            throw new InternalKernelException("Invalid GBean configuration for " + name, e);
        }
    }

    public void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            invoke(name, "stop");
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (InternalKernelException e) {
            throw e;
        } catch (NoSuchOperationException e) {
            throw new InternalKernelException("GBean is not state manageable: " + name.getCanonicalName(), e);
        } catch (Exception e) {
            throw new InternalKernelException("Invalid GBean configuration for " + name, e);
        }
    }

    public void unloadGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
       try {
            mbServer.unregisterMBean(name);
       } catch (InstanceNotFoundException e) {
           throw new GBeanNotFoundException(name.getCanonicalName());
       } catch (Exception e) {
           throw new InternalKernelException("Error unloading GBean " + name, unwrapJMException(e));
       }
    }

    public Set listGBeans(ObjectName pattern) {
        try {
            return mbServer.queryNames(pattern, null);
        } catch (RuntimeException e) {
            throw new InternalKernelException("Error while applying pattern " + pattern, e);
        }
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
         try {
             ObjectName configName = Configuration.getConfigurationObjectName(configID);
             return ((Integer)getAttribute(configName, "state")).intValue();
         } catch (MalformedObjectNameException e) {
             throw new NoSuchConfigException(e);
         } catch (GBeanNotFoundException e) {
             throw new NoSuchConfigException(e);
         } catch (InternalKernelException e) {
             throw e;
         } catch (Exception e) {
             throw new InternalKernelException(e);
         }
    }

    /**
     * Boot this Kernel, triggering the instantiation of the MBeanServer and DependencyManager,
     * and the registration ConfigurationStore
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

        synchronized (kernels) {
            if (kernels.containsKey(kernelName)) {
                throw new IllegalStateException("A kernel is already running this kernel name: " + kernelName);
            }
            kernels.put(kernelName, new KernelReference(kernelName, this));
        }

        mbServer = MBeanServerFactory.createMBeanServer(domainName);
        mbServer.registerMBean(this, KERNEL);
        lifecycleMonitor = new LifecycleMonitor(mbServer);
        dependencyManager = new DependencyManager(mbServer);
        proxyManager = new ProxyManager(this);

        // set up the data for the new configuration manager instance
        GBeanData configurationData = new GBeanData(CONFIGURATION_MANAGER_NAME, ConfigurationManagerImpl.GBEAN_INFO);
        configurationData.setReferencePatterns("Stores", Collections.singleton(CONFIGURATION_STORE_PATTERN));

        // create the connfiguration manager instance
        JMXLifecycleBroadcaster lifecycleBroadcaster = new JMXLifecycleBroadcaster();
        configurationManagerInstance = new GBeanInstance(this, configurationData, lifecycleBroadcaster, getClass().getClassLoader());
        configurationManagerInstance.start();
        configurationManager = (ConfigurationManager) configurationManagerInstance.getTarget();

        // wrap it in an mbean and register it
        GBeanMBean configurationManagerGBean = new GBeanMBean(this, configurationManagerInstance, lifecycleBroadcaster);
        mbServer.registerMBean(configurationManagerGBean, CONFIGURATION_MANAGER_NAME);

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

        try {
            mbServer.unregisterMBean(KERNEL);
        } catch (Exception e) {
            // ignore
        }

        dependencyManager.close();
        dependencyManager = null;

        MBeanServerFactory.releaseMBeanServer(mbServer);
        mbServer = null;
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
                mbServer.unregisterMBean(CONFIGURATION_MANAGER_NAME);
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
        try {
            return mbServer.getClassLoaderFor(name);
        } catch (InstanceNotFoundException e) {
            throw new GBeanNotFoundException(name.getCanonicalName());
        } catch (RuntimeException e) {
            throw new InternalKernelException("Error while attemping to get class loader for " + name.getCanonicalName(), e);
        }
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
