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

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import javax.management.Attribute;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.jmx.DependencyService;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
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
 * @version $Revision: 1.19 $ $Date: 2004/02/24 06:05:37 $
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
     * @param domainName the domain name to be used for the JMX MBeanServer
     */
    public Kernel(String domainName) {
        this(domainName, null);
    }

    /**
     * Construct a Kernel which does not have a config store.
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
        return mbServer.isRegistered(name);
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

        configurationManagerGBean = new GBeanMBean(ConfigurationManager.GBEAN_INFO);
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
