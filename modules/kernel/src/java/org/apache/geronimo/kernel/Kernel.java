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
package org.apache.geronimo.kernel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.DependencyService;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.jmx.JMXUtil;


/**
 * The core of a Geronimo instance.
 * A Kernel is responsible for managing the Configurations that comprise a
 * Geronimo system and exposing them using JMX. Each Kernel is associated
 * with an MBeanServer that is used to register the Configurations themselves
 * and the MBeans they define.
 *
 * Dependencies between MBeans are handled by a dedicated DependencyService
 * that is responsible for tracking those dependencies and ensuring that the
 * dependent objects follow the appropriate lifecycle and receive appropriate
 * notifications.
 *
 * The Kernel also provides a ConfigurationStore which is used to stage
 * installed Configurations (providing a local filesystem based classpath) and
 * used hold the persistent state of each Configuration. This allows
 * Configurations to restart in he event of system failure.
 *
 * @version $Revision: 1.18 $ $Date: 2004/02/13 23:21:07 $
 */
public class Kernel extends NotificationBroadcasterSupport implements Serializable, KernelMBean {

    /**
     * The JMX name used by a Kernel to register itself when it boots.
     */
    public static final ObjectName KERNEL = JMXUtil.getObjectName("geronimo.boot:role=Kernel");

    /**
     * The JMX name of the DependencyService.
     */
    public static final ObjectName DEPENDENCY_SERVICE = JMXUtil.getObjectName("geronimo.boot:role=DependencyService2");

    /**
     * The JMX name of the ConfigurationStore.
     */
    public static final ObjectName CONFIG_STORE = JMXUtil.getObjectName("geronimo.boot:role=ConfigurationStore");

    private static final Map kernels = new Hashtable();
    private static final ReferenceQueue queue = new ReferenceQueue();
    private final String kernelName;
    private final String domainName;
    private final GBeanInfo storeInfo;
    private final File configStore;

    private transient Log log;
    private transient boolean running;
    private transient MBeanServer mbServer;
    private transient GBeanMBean storeGBean;
    private transient ConfigurationStore store;

    /**
     * No-arg constructor allowing this class to be used as a GBean reference.
     */
    public Kernel() {
        kernelName = null;
        domainName = null;
        storeInfo = null;
        configStore = null;
    }

    /**
     * Construct a Kernel using the specified JMX domain and supply the
     * information needed to create the ConfigurationStore.
     * @param kernelName the name of the kernel that uniquely indentifies the kernel in a VM
     * @param domainName the domain name to be used for the JMX MBeanServer
     * @param storeInfo the info for the GBeanMBean to be used for the ConfigurationStore
     * @param configStore a local directory to be used by the ConfigurationStore;
     *                    this must be present and writable when the kernel is booted
     */
    public Kernel(String kernelName, String domainName, GBeanInfo storeInfo, File configStore) {
        this.kernelName = kernelName;
        this.domainName = domainName;
        this.storeInfo = storeInfo;
        this.configStore = configStore;
    }

    /**
     * Construct a Kernel using the specified JMX domain and supply the
     * information needed to create the ConfigurationStore.
     * @param domainName the domain name to be used for the JMX MBeanServer
     * @param storeInfo the info for the GBeanMBean to be used for the ConfigurationStore
     * @param configStore a local directory to be used by the ConfigurationStore;
     *                    this must be present and writable when the kernel is booted
     */
    public Kernel(String domainName, GBeanInfo storeInfo, File configStore) {
        this.kernelName = null;
        this.domainName = domainName;
        this.storeInfo = storeInfo;
        this.configStore = configStore;
    }

    /**
     * Construct a Kernel which does not have a config store.
     * @param kernelName the name of the kernel that uniquely indentifies the kernel in a VM
     * @param domainName the domain name to be used for the JMX MBeanServer
     */
    public Kernel(String kernelName, String domainName) {
        this(kernelName, domainName, null, null);
    }

    /**
     * Construct a Kernel which does not have a config store.
     * @param domainName the domain name to be used for the JMX MBeanServer
     */
    public Kernel(String domainName) {
        this(domainName, null, null);
    }

    public MBeanServer getMBeanServer() {
        return mbServer;
    }

    public String getKernelName() {
        return kernelName;
    }

    /**
     * Get a particular kernel indexed by a name
     * @param name the name of the kernel to be obtained
     * @return the kernel that was registered with that name
     */
    public static Kernel getKernel(String name) {
        synchronized (kernels) {
            processQueue();
            KernelReference ref = (KernelReference) kernels.get(name);
            if (ref != null) {
                Kernel kernel = (Kernel) ref.get();
                if (kernel != null) {
                    return kernel;
                }
            }
        }
        return null;
    }

    /**
     * Obtain the single kernel that's registered.
     *
     * <p>This method assumes that there is only one kernel registered and will throw an
     * <code>IllegalStateException</code> if more than one has been registered.
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

    public static ObjectName getConfigObjectName(URI configID) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configID.toString()));
    }

    public void install(URL source) throws IOException, InvalidConfigException {
        if (store == null) {
            throw new UnsupportedOperationException("Kernel does not have a ConfigurationStore");
        }
        store.install(source);
    }

    public List loadRecursive(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        try {
            LinkedList ancestors = new LinkedList();
            while (configID != null && !isLoaded(configID)) {
                ObjectName name = load(configID);
                ancestors.addFirst(name);
                configID = (URI) mbServer.getAttribute(name, "ParentID");
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

    public ObjectName load(URI configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (!running) {
            throw new IllegalStateException("Kernel is not running");
        }
        if (store == null) {
            throw new UnsupportedOperationException("Kernel does not have a ConfigurationStore");
        }

        GBeanMBean config = store.getConfig(configID);
        URL baseURL = store.getBaseURL(configID);
        return load(config, baseURL);
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
        if (!running) {
            throw new IllegalStateException("Kernel is not running");
        }
        try {
            mbServer.registerMBean(config, configName);
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to register configuraton", e);
        }

        try {
            config.setAttribute("BaseURL", rootURL);
        } catch (Exception e) {
            try {
                mbServer.unregisterMBean(configName);
            } catch (Exception e1) {
                // ignore
            }
            throw new InvalidConfigException("Cannot set BaseURL", e);
        }

        log.info("Loaded Configuration " + configName);
    }

    public boolean isLoaded(URI configID) {
        try {
            ObjectName name = getConfigObjectName(configID);
            return mbServer.isRegistered(name);
        } catch (MalformedObjectNameException e) {
            return false;
        }
    }

    public void unload(ObjectName configName) throws NoSuchConfigException {
        if (!running) {
            throw new IllegalStateException("Kernel is not running");
        }
        try {
            mbServer.unregisterMBean(configName);
        } catch (InstanceNotFoundException e) {
            throw new NoSuchConfigException("No config registered: " + configName, e);
        } catch (MBeanRegistrationException e) {
            throw (IllegalStateException) new IllegalStateException("Error deregistering configuration " + configName).initCause(e);
        }
        log.info("Unloaded Configuration " + configName);
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
            mbServer.invoke(name, "start", null, null);
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            throw new InvalidConfigException("Invalid GBean configuration for " + name, cause);
        }
    }

    public void startRecursiveGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException {
        try {
            mbServer.invoke(name, "startRecursive", null, null);
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            throw new InvalidConfigException("Invalid GBean configuration for " + name, cause);
        }
    }

    public void stopGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException {
        try {
            mbServer.invoke(name, "stop", null, null);
        } catch (JMException e) {
            Throwable cause = e;
            while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
                cause = cause.getCause();
            }
            throw new InvalidConfigException("Invalid GBean configuration for " + name, cause);
        }
    }

    public void unloadGBean(ObjectName name) throws InstanceNotFoundException {
        try {
            mbServer.unregisterMBean(name);
        } catch (MBeanRegistrationException e) {
            throw (IllegalStateException) new IllegalStateException("Error unloading GBean " + name).initCause(e);
        }
    }

    /**
     * Boot this Kernel, triggering the instanciation of the MBeanServer and
     * the registration of the DependencyService and ConfigurationStore
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
        if (storeInfo != null) {
            storeGBean = new GBeanMBean(storeInfo);
            storeGBean.setAttribute("root", configStore);
            mbServer.registerMBean(storeGBean, CONFIG_STORE);
            storeGBean.start();
            store = (ConfigurationStore) storeGBean.getTarget();
        }
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

        store = null;
        try {
            if (storeGBean != null) {
                storeGBean.stop();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            if (storeGBean != null) {
                mbServer.unregisterMBean(CONFIG_STORE);
            }
        } catch (Exception e) {
            // ignore
        }
        storeGBean = null;

        try {
            mbServer.unregisterMBean(DEPENDENCY_SERVICE);
        } catch (Exception e) {
            // ignore
        }
        try {
            mbServer.unregisterMBean(KERNEL);
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
