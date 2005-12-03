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

package org.apache.geronimo.kernel.basic;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanName;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelGBean;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
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
 * Configurations to restart in the event of system failure.
 * 
 * TODO: Describe the order of method invocation (e.g. if loadGbean may be before boot)
 *
 * @version $Rev: 154947 $ $Date: 2005-02-22 20:10:45 -0800 (Tue, 22 Feb 2005) $
 */
public class BasicKernel implements Kernel {
    /**
     * Helper objects for invoke and getAttribute
     */
    private static final String[] NO_TYPES = new String[0];
    private static final Object[] NO_ARGS = new Object[0];

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
     * The registry
     */
    private final BasicRegistry registry;

    /**
     * Listeners for when the kernel shutdown
     */
    private LinkedList shutdownHooks = new LinkedList();

    /**
     * This manager is used by the kernel to manage dependencies between gbeans
     */
    private DependencyManager dependencyManager;

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
     * Construct a Kernel with the specified name.
     *
     * @param kernelName the name of the kernel
     */
    public BasicKernel(String kernelName) {
        if (kernelName.indexOf(':') >= 0 || kernelName.indexOf('*') >= 0 || kernelName.indexOf('?') >= 0) {
            throw new IllegalArgumentException("Kernel name may not contain a ':', '*' or '?' character");
        }
        this.kernelName = kernelName;
        this.registry = new BasicRegistry();
    }

    public String getKernelName() {
        return kernelName;
    }

    /**
     * @deprecated this will be removed as when we add generalized dependencies to gbeans... the only current user is Configuration
     */
    public DependencyManager getDependencyManager() {
        return dependencyManager;
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
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(objectName));
        return gbeanInstance.getAttribute(attributeName);
    }

    public void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(objectName));
        gbeanInstance.setAttribute(attributeName, attributeValue);
    }

    public Object invoke(ObjectName objectName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invoke(objectName, methodName, NO_ARGS, NO_TYPES);
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(objectName));
        return gbeanInstance.invoke(methodName, args, types);
    }

    public boolean isLoaded(ObjectName name) {
        return registry.isRegistered(createGBeanName(name));
    }

    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        return gbeanInstance.getGBeanInfo();
    }

    public GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        return gbeanInstance.getGBeanData();
    }

    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException {
        ObjectName objectName = gbeanData.getName();
        GBeanInstance gbeanInstance = new GBeanInstance(gbeanData, this, dependencyManager, lifecycleMonitor.createLifecycleBroadcaster(objectName), classLoader);
        registry.register(gbeanInstance);
    }

    public void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        gbeanInstance.start();
    }

    public void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        gbeanInstance.startRecursive();
    }

    public void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        gbeanInstance.stop();
    }

    public void unloadGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        GBeanName gbeanName = createGBeanName(name);
        GBeanInstance gbeanInstance = registry.getGBeanInstance(gbeanName);
        gbeanInstance.die();
        registry.unregister(gbeanName);
    }

    public int getGBeanState(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        return gbeanInstance.getState();
    }

    public long getGBeanStartTime(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        return gbeanInstance.getStartTime();
    }

    public boolean isGBeanEnabled(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        return gbeanInstance.isEnabled();
    }

    public void setGBeanEnabled(ObjectName name, boolean enabled) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        gbeanInstance.setEnabled(enabled);
    }

    public Set listGBeans(ObjectName pattern) {
        String domain = (pattern == null || pattern.isDomainPattern()) ? null : pattern.getDomain();
        Map props = pattern == null ? null : pattern.getKeyPropertyList();
        Set gbeans = registry.listGBeans(domain, props);
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

    public Set listGBeans(String[] patterns) {
        Set gbeans = new HashSet();
        for(int i=0; i<patterns.length; i++) {
            ObjectName pattern = null;
            try {
                pattern = ObjectName.getInstance(patterns[i]);
            } catch (MalformedObjectNameException e) {}
            gbeans.addAll(listGBeans(pattern));
        }
        return gbeans;
    }

    public Set listGBeansByInterface(String[] interfaces) {
        Set gbeans = new HashSet();
        Set all = null;
        try {
            all = listGBeans(ObjectName.getInstance("*:*"));
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("How can *:* be an invalid pattern");
        }
        for (Iterator it = all.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                GBeanInfo info = getGBeanInfo(name);
                Set intfs = info.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    String candidate = interfaces[i];
                    if(intfs.contains(candidate)) {
                        gbeans.add(name);
                        break;
                    }
                }
            } catch (GBeanNotFoundException e) {}
        }
        return gbeans;
    }

    /**
     * Returns a Set of all GBeans matching the specified criteria
     *
     * @return a List of javax.management.ObjectName of matching GBeans registered with this kernel
     */
    public Set listGBeans(GBeanQuery query) {
        Set results = new HashSet();
        if(query.getGBeanNames() != null && query.getGBeanNames().length > 0) {
            results.addAll(listGBeans(query.getGBeanNames()));
        }
        if(query.getInterfaces() != null && query.getInterfaces().length > 0) {
            results.addAll(listGBeansByInterface(query.getInterfaces()));
        }
        return results;
    }

    public ObjectName getObjectNameFor(Object service) {
        if(!running) {return null;}
        return proxyManager.getProxyTarget(service);
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
        log = LogFactory.getLog(BasicKernel.class.getName());
        log.debug("Starting boot");

        // todo cleanup when boot fails
        KernelRegistry.registerKernel(this);

        registry.start(this);

        lifecycleMonitor = new BasicLifecycleMonitor(this);
        publicLifecycleMonitor = new LifecycleMonitorFlyweight(lifecycleMonitor);
        dependencyManager = new BasicDependencyManager(publicLifecycleMonitor);
        proxyManager = new BasicProxyManager(this);

        // load and start the kernel gbean
        GBeanData kernelGBeanData = new GBeanData(KERNEL, KernelGBean.GBEAN_INFO);
        loadGBean(kernelGBeanData, getClass().getClassLoader());
        startGBean(KERNEL);

        running = true;
        log.debug("Booted");
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
        log.debug("Starting kernel shutdown");

        notifyShutdownHooks();

        registry.stop();

        dependencyManager.close();
        dependencyManager = null;

        synchronized (this) {
            notify();
        }

        KernelRegistry.unregisterKernel(this);

        log.debug("Kernel shutdown complete");
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

    public boolean isRunning() {
        return running;
    }

    public ClassLoader getClassLoaderFor(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance = registry.getGBeanInstance(createGBeanName(name));
        return gbeanInstance.getClassLoader();
    }

    private GBeanName createGBeanName(ObjectName objectName) {
        if (objectName.getDomain().length() == 0) {
            return new GBeanName(kernelName, objectName.getKeyPropertyList());
        }
        return new GBeanName(objectName);
    }
}
