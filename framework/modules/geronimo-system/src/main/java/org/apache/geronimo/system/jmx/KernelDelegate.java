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
package org.apache.geronimo.system.jmx;

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
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

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

    public Naming getNaming() {
        return (Naming) getKernelAttribute("naming");
    }

    @Deprecated
    public Object getGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            return invokeKernel("getGBean", new Object[] {name}, new String[] {ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Object getGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            return invokeKernel("getGBean", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Object getGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            return invokeKernel("getGBean", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public <T> T getGBean(Class<T> type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        Set<AbstractName> set = listGBeans(new AbstractNameQuery(type.getName()));
        for (AbstractName name : set) {
            return proxyManager.createProxy(name, type);
        }
        throw new GBeanNotFoundException("No implementation found for type " + type.getName(), null, set);
    }

    @SuppressWarnings("unchecked")
    public <T> T getGBean(String shortName, Class<T> type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            return (T) invokeKernel("getGBean", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void loadGBean(GBeanData gbeanData, BundleContext bundleContext) throws GBeanAlreadyExistsException {
        try {
            invokeKernel("loadGBean", new Object[] {gbeanData, bundleContext}, new String[] {GBeanData.class.getName(), ClassLoader.class.getName()});
        } catch (GBeanAlreadyExistsException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startGBean", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startGBean", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startGBean", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startGBean", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startRecursiveGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startRecursiveGBean", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startRecursiveGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startRecursiveGBean", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startRecursiveGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startRecursiveGBean", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void startRecursiveGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("startRecursiveGBean", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isRunning(AbstractName name) {
        try {
            return (Boolean) invokeKernel("isRunning", new Object[]{name}, new String[]{AbstractName.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
     }

    public boolean isRunning(String shortName) {
        try {
            return (Boolean) invokeKernel("isRunning", new Object[]{shortName}, new String[]{String.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isRunning(Class type) {
        try {
            return (Boolean) invokeKernel("isRunning", new Object[]{type}, new String[]{Class.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isRunning(String shortName, Class type) {
        try {
            return (Boolean) invokeKernel("isRunning", new Object[]{shortName, type}, new String[]{String.class.getName(), Class.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }


    public void stopGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("stopGBean", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void stopGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("stopGBean", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void stopGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("stopGBean", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void stopGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("stopGBean", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void unloadGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("unloadGBean", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void unloadGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("unloadGBean", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void unloadGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("unloadGBean", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public void unloadGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        try {
            invokeKernel("unloadGBean", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    @Deprecated
    public int getGBeanState(ObjectName name) throws GBeanNotFoundException {
        try {
            return (Integer) invokeKernel("getGBeanState", new Object[]{name}, new String[]{ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public int getGBeanState(AbstractName name) throws GBeanNotFoundException {
        try {
            return (Integer) invokeKernel("getGBeanState", new Object[]{name}, new String[]{AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public int getGBeanState(String shortName) throws GBeanNotFoundException {
        try {
            return (Integer) invokeKernel("getGBeanState", new Object[]{shortName}, new String[]{String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public int getGBeanState(Class type) throws GBeanNotFoundException {
        try {
            return (Integer) invokeKernel("getGBeanState", new Object[]{type}, new String[]{Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public int getGBeanState(String shortName, Class type) throws GBeanNotFoundException {
        try {
            return (Integer) invokeKernel("getGBeanState", new Object[]{shortName, type}, new String[]{String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public long getGBeanStartTime(AbstractName name) throws GBeanNotFoundException {
        try {
            return (Long) invokeKernel("getGBeanStartTime", new Object[]{name}, new String[]{AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public long getGBeanStartTime(String shortName) throws GBeanNotFoundException {
        try {
            return (Long) invokeKernel("getGBeanStartTime", new Object[]{shortName}, new String[]{String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public long getGBeanStartTime(Class type) throws GBeanNotFoundException {
        try {
            return (Long) invokeKernel("getGBeanStartTime", new Object[]{type}, new String[]{Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public long getGBeanStartTime(String shortName, Class type) throws GBeanNotFoundException {
        try {
            return (Long) invokeKernel("getGBeanStartTime", new Object[]{shortName, type}, new String[]{String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    @Deprecated
    public Object getAttribute(ObjectName objectName, String attributeName) throws Exception {
        return invokeKernel("getAttribute", new Object[]{objectName, attributeName}, new String[]{ObjectName.class.getName(), String.class.getName()});
    }

    public Object getAttribute(AbstractName abstractName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return invokeKernel("getAttribute", new Object[]{abstractName, attributeName}, new String[]{AbstractName.class.getName(), String.class.getName()});
    }

    public Object getAttribute(String shortName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return invokeKernel("getAttribute", new Object[]{shortName, attributeName}, new String[]{String.class.getName(), String.class.getName()});
    }

    public Object getAttribute(Class type, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return invokeKernel("getAttribute", new Object[]{type, attributeName}, new String[]{Class.class.getName(), String.class.getName()});
    }

    public Object getAttribute(String shortName, Class type, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return invokeKernel("getAttribute", new Object[]{shortName, type, attributeName}, new String[]{String.class.getName(), Class.class.getName(), String.class.getName()});
    }

    public void setAttribute(AbstractName abstractName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        invokeKernel("setAttribute", new Object[]{abstractName, attributeName, attributeValue}, new String[]{AbstractName.class.getName(), String.class.getName(), Object.class.getName()});
    }

    public void setAttribute(String shortName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        invokeKernel("setAttribute", new Object[]{shortName, attributeName, attributeValue}, new String[]{String.class.getName(), String.class.getName(), Object.class.getName()});
    }

    public void setAttribute(Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        invokeKernel("setAttribute", new Object[]{type, attributeName, attributeValue}, new String[]{Class.class.getName(), String.class.getName(), Object.class.getName()});
    }

    public void setAttribute(String shortName, Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        invokeKernel("setAttribute", new Object[]{shortName, type, attributeName, attributeValue}, new String[]{String.class.getName(), Class.class.getName(), String.class.getName(), Object.class.getName()});
    }

    @Deprecated
    public Object invoke(ObjectName objectName, String methodName) throws Exception {
        return invokeKernel("invoke", new Object[]{objectName, methodName}, new String[]{ObjectName.class.getName(), String.class.getName()});
    }

    public Object invoke(AbstractName abstractName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{abstractName, methodName}, new String[]{AbstractName.class.getName(), String.class.getName()});
    }

    public Object invoke(String shortName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{shortName, methodName}, new String[]{String.class.getName(), String.class.getName()});
    }

    public Object invoke(Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{type, methodName}, new String[]{Class.class.getName(), String.class.getName()});
    }

    public Object invoke(String shortName, Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{shortName, type, methodName}, new String[]{String.class.getName(), Class.class.getName(), String.class.getName()});
    }

    @Deprecated
    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception {
        return invokeKernel("invoke", new Object[]{objectName, methodName, args, types}, new String[]{ObjectName.class.getName(), String.class.getName(), Object[].class.getName(), String[].class.getName()});
    }

    public String getStateReason(AbstractName abstractName) {
        try {
            return ((String) invokeKernel("getStateReason", new Object[]{abstractName}, new String[]{AbstractName.class.getName()}));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Object invoke(AbstractName abstractName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{abstractName, methodName, args, types}, new String[]{AbstractName.class.getName(), String.class.getName(), Object[].class.getName(), String[].class.getName()});
    }

    public Object invoke(String shortName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{shortName, methodName, args, types}, new String[]{String.class.getName(), String.class.getName(), Object[].class.getName(), String[].class.getName()});
    }

    public Object invoke(Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{type, methodName, args, types}, new String[]{Class.class.getName(), String.class.getName(), Object[].class.getName(), String[].class.getName()});
    }

    public Object invoke(String shortName, Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return invokeKernel("invoke", new Object[]{shortName, type, methodName, args, types}, new String[]{String.class.getName(), Class.class.getName(), String.class.getName(), Object[].class.getName(), String[].class.getName()});
    }

    public boolean isLoaded(AbstractName name) {
        try {
            return (Boolean) invokeKernel("isLoaded", new Object[]{name}, new String[]{AbstractName.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
     }

    public boolean isLoaded(String shortName) {
        try {
            return (Boolean) invokeKernel("isLoaded", new Object[]{shortName}, new String[]{String.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isLoaded(Class type) {
        try {
            return (Boolean) invokeKernel("isLoaded", new Object[]{type}, new String[]{Class.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public boolean isLoaded(String shortName, Class type) {
        try {
            return (Boolean) invokeKernel("isLoaded", new Object[]{shortName, type}, new String[]{String.class.getName(), Class.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    @Deprecated
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

    public GBeanInfo getGBeanInfo(AbstractName name) throws GBeanNotFoundException {
        try {
            return (GBeanInfo) invokeKernel("getGBeanInfo", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanInfo getGBeanInfo(String shortName) throws GBeanNotFoundException {
        try {
            return (GBeanInfo) invokeKernel("getGBeanInfo", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanInfo getGBeanInfo(Class type) throws GBeanNotFoundException {
        try {
            return (GBeanInfo) invokeKernel("getGBeanInfo", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanInfo getGBeanInfo(String shortName, Class type) throws GBeanNotFoundException {
        try {
            return (GBeanInfo) invokeKernel("getGBeanInfo", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public Set<AbstractName> listGBeans(ObjectName pattern) {
        try {
            return (Set<AbstractName>) invokeKernel("listGBeans", new Object[] {pattern}, new String[] {ObjectName.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<AbstractName> listGBeans(Set patterns) {
        try {
            return (Set<AbstractName>) invokeKernel("listGBeans", new Object[] {patterns}, new String[] {Set.class.getName()});
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

    public Bundle getBundleFor(AbstractName name) throws GBeanNotFoundException {
        try {
            return (Bundle) invokeKernel("getBundleFor", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Bundle getBundleFor(String shortName) throws GBeanNotFoundException {
        try {
            return (Bundle) invokeKernel("getBundleFor", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Bundle getBundleFor(Class type) throws GBeanNotFoundException {
        try {
            return (Bundle) invokeKernel("getBundleFor", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public Bundle getBundleFor(String shortName, Class type) throws GBeanNotFoundException {
        try {
            return (Bundle) invokeKernel("getBundleFor", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanData getGBeanData(AbstractName name) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanData) invokeKernel("getGBeanData", new Object[] {name}, new String[] {AbstractName.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanData getGBeanData(String shortName) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanData) invokeKernel("getGBeanData", new Object[] {shortName}, new String[] {String.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanData getGBeanData(Class type) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanData) invokeKernel("getGBeanData", new Object[] {type}, new String[] {Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public GBeanData getGBeanData(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException {
        try {
            return (GBeanData) invokeKernel("getGBeanData", new Object[] {shortName, type}, new String[] {String.class.getName(), Class.class.getName()});
        } catch (GBeanNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public AbstractName getAbstractNameFor(Object service) {
        AbstractName name = proxyManager.getProxyTarget(service);
        if (name != null) {
            return name;
        }
        try {
            return (AbstractName) invokeKernel("getAbstractNameFor", new Object[] {service}, new String[] {Object.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    public String getShortNameFor(Object service) {
        AbstractName name = getAbstractNameFor(service);
        return (String) name.getName().get("name");
    }

    public boolean isRunning() {
        return ((Boolean) getKernelAttribute("running")).booleanValue();
    }

    public Set<AbstractName> listGBeans(AbstractNameQuery query) {
        try {
            return (Set<AbstractName>) invokeKernel("listGBeans", new Object[] {query}, new String[] {AbstractNameQuery.class.getName()});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalKernelException(e);
        }
    }

    /**
     * Throws UnsupportedOperationException.  The dependency manager is not accesable over a remote connection.
     */
    public DependencyManager getDependencyManager() {
        throw new UnsupportedOperationException("Dependency manager is not accessable by way of a remote connection");
    }

    /**
     * Throws UnsupportedOperationException.  The lifecycle monitor is not accesable over a remote connection.
     */
    public LifecycleMonitor getLifecycleMonitor() {
        throw new UnsupportedOperationException("Lifecycle monitor is not accessable by way of a remote connection");
    }

    public ProxyManager getProxyManager() {
        return proxyManager;
    }

    /**
     * Throws UnsupportedOperationException.  A remote kernel will alreayd be booted.
     * @param bundleContext
     */
    public void boot(BundleContext bundleContext) throws Exception {
        throw new UnsupportedOperationException("A remote kernel can not be booted");
    }

    private Object getKernelAttribute(String attributeName) {
        try {
            return mbeanServer.getAttribute(Kernel.KERNEL, attributeName);
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new InternalKernelException("Kernel is not loaded", cause);
            } else if (cause instanceof AttributeNotFoundException) {
                throw new InternalKernelException("KernelDelegate is out of synch with Kernel", cause);
            } else {
                throw new InternalKernelException(cause);
            }
        }
    }

    private Object invokeKernel(String methodName, Object[] args, String[] types) throws Exception {
        if(args != null && types != null && args.length != types.length) {
            throw new IllegalArgumentException("Call to "+methodName+" has "+args.length+" arguments but "+types.length+" argument classes!");
        }
        try {
            return mbeanServer.invoke(Kernel.KERNEL, methodName, args, types);
        } catch (Exception e) {
            Throwable cause = unwrapJMException(e);
            if (cause instanceof InstanceNotFoundException) {
                throw new InternalKernelException("Kernel is not loaded", cause);
            } else if (cause instanceof NoSuchMethodException) {
                StringBuilder buf = new StringBuilder("KernelDelegate is out of synch with Kernel on ");
                buf.append(methodName).append("(");
                if(types != null) {
                    for (int i = 0; i < types.length; i++) {
                        String type = types[i];
                        if(i>0) buf.append(",");
                        buf.append(type);
                    }
                }
                buf.append(")");
                throw new InternalKernelException(buf.toString());
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
}
