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
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import javax.management.Attribute;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.jmx.DependencyService;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.jmx.JMXUtil;


/**
 * The core of a Geronimo instance.
 * A Kernel is responsible for managing the Configurations that comprise a
 * Geronimo system and exposing them using JMX. Each Kernel is associated
 * with an MBeanServer that is used to register the Configurations themselves
 * and the MBeans they define.
 * <p/>
 * Dependencies between MBeans are handled by a dedicated DependencyService
 * that is responsible for tracking those dependencies and ensuring that the
 * dependent objects follow the appropriate lifecycle and receive appropriate
 * notifications.
 * <p/>
 * The Kernel also provides a ConfigurationStore which is used to stage
 * installed Configurations (providing a local filesystem based classpath) and
 * used hold the persistent state of each Configuration. This allows
 * Configurations to restart in he event of system failure.
 *
 * @version $Revision: 1.30 $ $Date: 2004/06/02 19:50:40 $
 */
public class Kernel extends NotificationBroadcasterSupport implements Serializable, KernelMBean {

    /**
     * The JMX name used by a Kernel to register itself when it boots.
     */
    public static final ObjectName KERNEL = JMXUtil.getObjectName("geronimo.boot:role=Kernel");

    /**
     * The JMX name of the DependencyService.
     */
    public static final ObjectName DEPENDENCY_SERVICE = JMXUtil.getObjectName("geronimo.boot:role=DependencyService");


    private static final Map kernels = new Hashtable();
    private static final ReferenceQueue queue = new ReferenceQueue();
    private final String kernelName;
    private final String domainName;

    private transient Log log;
    private transient boolean running;
    private transient MBeanServer mbServer;

    private transient ConfigurationManager configurationManager;
    private transient GBeanMBean configurationManagerGBean;

    private static final String[] NO_TYPES = new String[0];
    private static final Object[] NO_ARGS = new Object[0];
    private static final ObjectName CONFIGURATION_MANAGER_NAME = JMXUtil.getObjectName("geronimo.boot:role=ConfigurationManager");
    private static final ObjectName CONFIGURATION_STORE_PATTERN = JMXUtil.getObjectName("*:role=ConfigurationStore,*");

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
     * @param domainName the domain name to be used for the JMX MBeanServer
     */
    public Kernel(String domainName) {
        this(null, domainName);
    }

    /**
     * Construct a Kernel which does not have a config store.
     *
     * @param kernelName the name of the kernel that uniquely indentifies the kernel in a VM
     * @param domainName the domain name to be used for the JMX MBeanServer
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

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws Exception {
        try {
            return mbServer.getAttribute(objectName, attributeName);
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }

    public void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws Exception {
        try {
            mbServer.setAttribute(objectName, new Attribute(attributeName, attributeValue));
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }

    public Object invoke(ObjectName objectName, String methodName) throws Exception {
        return invoke(objectName, methodName, NO_ARGS, NO_TYPES);
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception {
        try {
            return mbServer.invoke(objectName, methodName, args, types);
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new AssertionError(cause);
            }
        }
    }


    public boolean isLoaded(ObjectName name) {
        return mbServer != null && mbServer.isRegistered(name);
    }

    public void loadGBean(ObjectName name, GBeanMBean gbean) throws InstanceAlreadyExistsException, InvalidConfigException {
        try {
            mbServer.registerMBean(gbean, name);
        } catch (MBeanRegistrationException e) {
            throw new InvalidConfigException("Invalid GBean configuration for " + name, e);
        } catch (NotCompliantMBeanException e) {
            throw new InvalidConfigException("Invalid GBean configuration for " + name, e);
        }
    }

    public void startGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException {
        try {
            invoke(name, "start");
        } catch (Exception e) {
            throw new InvalidConfigException("Invalid GBean configuration for " + name, e);
        }
    }

    public void startRecursiveGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException {
        try {
            invoke(name, "startRecursive");
        } catch (Exception e) {
            throw new InvalidConfigException("Invalid GBean configuration for " + name, e);
        }
    }

    public void stopGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException {
        try {
            invoke(name, "stop");
        } catch (Exception e) {
            throw new InvalidConfigException("Invalid GBean configuration for " + name, e);
        }
    }

    public void unloadGBean(ObjectName name) throws InstanceNotFoundException {
        try {
            mbServer.unregisterMBean(name);
        } catch (MBeanRegistrationException e) {
            throw (IllegalStateException) new IllegalStateException("Error unloading GBean " + name).initCause(e);
        }
    }

    public List listConfigurationStores() {
        return getConfigurationManager().listStores();
    }

    public List listConfigurations(ObjectName storeName) throws NoSuchStoreException {
        return getConfigurationManager().listConfigurations(storeName);
    }

    public ObjectName startConfiguration(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        ObjectName configName = getConfigurationManager().load(configID);
        try {
            startRecursiveGBean(configName);
        } catch (InstanceNotFoundException e) {
            // should not happen as we just loaded it
            throw new InvalidConfigException(e);
        }
        return configName;
    }

    public void stopConfiguration(URI configID) throws NoSuchConfigException {
        ConfigurationManager configurationManager = getConfigurationManager();
        try {
            ObjectName configName = configurationManager.getConfigObjectName(configID);
            stopGBean(configName);
        } catch (MalformedObjectNameException e) {
            throw new NoSuchConfigException(e);
        } catch (InstanceNotFoundException e) {
            throw new NoSuchConfigException(e);
        } catch (InvalidConfigException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
        configurationManager.unload(configID);
    }

    /**
     * Boot this Kernel, triggering the instantiation of the MBeanServer and
     * the registration of the DependencyService and ConfigurationStore
     *
     * @throws java.lang.Exception if the boot fails
     */
    public void boot() throws Exception {
        if (running) {
            return;
        }
        log = LogFactory.getLog(Kernel.class.getName());
        log.info("Starting boot");

        if (kernelName != null) {
            synchronized (kernels) {
                kernels.put(kernelName, new KernelReference(kernelName, this));
            }
        }

        mbServer = MBeanServerFactory.createMBeanServer(domainName);
        mbServer.registerMBean(this, KERNEL);
        mbServer.registerMBean(new DependencyService(), DEPENDENCY_SERVICE);

        configurationManagerGBean = new GBeanMBean(ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerGBean.setReferencePatterns("Kernel", Collections.singleton(KERNEL));
        configurationManagerGBean.setReferencePatterns("Stores", Collections.singleton(CONFIGURATION_STORE_PATTERN));
        mbServer.registerMBean(configurationManagerGBean, CONFIGURATION_MANAGER_NAME);
        configurationManagerGBean.start();
        configurationManager = (ConfigurationManager) configurationManagerGBean.getTarget();

        running = true;
        log.info("Booted");
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

        configurationManager = null;
        configurationManagerGBean = null;
        try {
            if (configurationManagerGBean != null) {
                configurationManagerGBean.stop();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            if (configurationManagerGBean != null) {
                mbServer.unregisterMBean(CONFIGURATION_MANAGER_NAME);
            }
        } catch (Exception e) {
            // ignore
        }
        configurationManagerGBean = null;

        try {
            mbServer.unregisterMBean(KERNEL);
        } catch (Exception e) {
            // ignore
        }
        try {
            mbServer.unregisterMBean(DEPENDENCY_SERVICE);
        } catch (Exception e) {
            // ignore
        }

        MBeanServerFactory.releaseMBeanServer(mbServer);
        mbServer = null;
        synchronized (this) {
            notify();
        }

        if (kernelName != null) {
            synchronized (kernels) {
                kernels.remove(kernelName);
            }
        }

        log.info("Kernel shutdown complete");
    }

    public boolean isRunning() {
        return running;
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
