/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.kernel.jmx;

import java.util.Date;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * @version $Rev$ $Date$
 */
public class KernelDelegate implements Kernel {
    private final MBeanServerConnection mbeanServer;
    private final ProxyManager proxyManager;

    public KernelDelegate(MBeanServerConnection mbeanServer) {
        this.mbeanServer = mbeanServer;
        proxyManager = new JMXProxyManager(this);
    }

    public Date getBootTime() {
        return (Date) getKernelAttribute("bootTime");
    }

    public String getKernelName() {
        return (String) getKernelAttribute("kernelName");
    }

    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException {
        try {
            invokeKernel("loadGBean", new Object[] {gbeanData, classLoader}, new String[] {GBeanData.class.getName(), ClassLoader.class.getName()});
        } catch (GBeanAlreadyExistsException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startGBean", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startRecursiveGBean", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("stopGBean", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void unloadGBean(ObjectName name) throws GBeanNotFoundException {
        try {
            invokeKernel("unloadGBean", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public int getGBeanState(ObjectName name) throws GBeanNotFoundException {
        try {
            return ((Integer) invokeKernel("getGBeanState", new Object[]{name}, new String[]{ObjectName.class.getName()})).intValue();
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public long getGBeanStartTime(ObjectName name) throws GBeanNotFoundException {
        try {
            return ((Long) invokeKernel("getGBeanStartTime", new Object[]{name}, new String[]{ObjectName.class.getName()})).longValue();
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isGBeanEnabled(ObjectName name) throws GBeanNotFoundException {
        try {
            return ((Boolean) invokeKernel("isGBeanEnabled", new Object[] {name}, new String[] {ObjectName.class.getName()})).booleanValue();
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void setGBeanEnabled(ObjectName name, boolean enabled) throws GBeanNotFoundException {
        try {
            invokeKernel("setGBeanEnabled", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws Exception {
        try {
            return invokeKernel("getAttribute", new Object[]{objectName, attributeName}, new String[]{ObjectName.class.getName(), String.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws Exception {
        try {
            invokeKernel("setAttribute", new Object[]{objectName, attributeName, attributeValue}, new String[]{ObjectName.class.getName(), String.class.getName(), Object.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Object invoke(ObjectName objectName, String methodName) throws Exception {
        try {
            return invokeKernel("invoke", new Object[]{objectName, methodName}, new String[]{ObjectName.class.getName(), String.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception {
        try {
            return invokeKernel("invoke", new Object[]{objectName, methodName, args, types}, new String[]{ObjectName.class.getName(), String.class.getName(), Object[].class.getName(), String[].class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isLoaded(ObjectName name) {
        try {
            return ((Boolean) invokeKernel("isLoaded", new Object[]{name}, new String[]{ObjectName.class.getName()})).booleanValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException {
        try {
            return (GBeanInfo) invokeKernel("getGBeanInfo", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Set listGBeans(ObjectName pattern) throws InternalKernelException {
        try {
            return (Set) invokeKernel("listGBeans", new Object[] {pattern}, new String[] {ObjectName.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Set listGBeans(Set patterns) throws InternalKernelException {
        try {
            return (Set) invokeKernel("listGBeans", new Object[] {patterns}, new String[] {Set.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void registerShutdownHook(Runnable hook) {
        try {
            invokeKernel("registerShutdownHook", new Object[] {hook}, new String[] {Runnable.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void unregisterShutdownHook(Runnable hook) {
        try {
            invokeKernel("unregisterShutdownHook", new Object[] {hook}, new String[] {Runnable.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void shutdown() {
        try {
            invokeKernel("shutdown", new Object[] {}, new String[] {});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public ClassLoader getClassLoaderFor(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (ClassLoader) invokeKernel("getClassLoaderFor", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanData) invokeKernel("getGBeanData", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    private Object getKernelAttribute(String attributeName) throws InternalKernelException {
        try {
            return mbeanServer.getAttribute(Kernel.KERNEL, attributeName);
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new InternalKernelException("Kernel is not loaded");
            } else if (cause instanceof AttributeNotFoundException) {
                throw new InternalKernelException("KernelDelegate is out of synch with Kernel");
            } else {
                throw new InternalKernelException(cause);
            }
        }
    }

    private Object invokeKernel(String methodName, Object[] args, String[] types) throws InternalKernelException, Exception {
        try {
            return mbeanServer.invoke(Kernel.KERNEL, methodName, args, types);
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new InternalKernelException("Kernel is not loaded");
            } else if (cause instanceof NoSuchMethodException) {
                throw new InternalKernelException("KernelDelegate is out of synch with Kernel");
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

    public boolean isRunning() {
        return ((Boolean) getKernelAttribute("running")).booleanValue();
    }

    public DependencyManager getDependencyManager() {
        throw new UnsupportedOperationException("Dependency manager is not accessable by way of a remote connection");
    }

    public LifecycleMonitor getLifecycleMonitor() {
        throw new UnsupportedOperationException("Lifecycle monitor is not accessable by way of a remote connection");
    }

    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    public void boot() throws Exception {
        throw new UnsupportedOperationException("A remote kernel can not be booted");
    }

    private Throwable unwrapJMException(Throwable cause) {
        while ((cause instanceof JMException || cause instanceof JMRuntimeException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
