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
package org.apache.geronimo.kernel;

import java.util.Date;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * @version $Rev: 385487 $ $Date$
 */
public class KernelGBean implements Kernel{
    private final Kernel kernel;

    public KernelGBean(Kernel kernel) {
        this.kernel = kernel;
    }

    public DependencyManager getDependencyManager() {
        return kernel.getDependencyManager();
    }

    public LifecycleMonitor getLifecycleMonitor() {
        return kernel.getLifecycleMonitor();
    }

    public ProxyManager getProxyManager() {
        return kernel.getProxyManager();
    }

    public void boot() throws Exception {
        throw new UnsupportedOperationException();
    }

    public Naming getNaming() {
        throw new UnsupportedOperationException();
    }

    public Date getBootTime() {
        return kernel.getBootTime();
    }

    public String getKernelName() {
        return kernel.getKernelName();
    }

    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException, InternalKernelException {
        kernel.loadGBean(gbeanData, classLoader);
    }

    public void startGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startGBean(name);
    }

    public void startGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startGBean(name);
    }

    public void startRecursiveGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startRecursiveGBean(name);
    }

    public void startRecursiveGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startRecursiveGBean(name);
    }

    public void stopGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.stopGBean(name);
    }

    public void stopGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.stopGBean(name);
    }

    public void unloadGBean(ObjectName name) throws GBeanNotFoundException {
        kernel.unloadGBean(name);
    }

    public void unloadGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.unloadGBean(name);
    }

    public int getGBeanState(ObjectName name) throws GBeanNotFoundException {
        return kernel.getGBeanState(name);
    }

    public int getGBeanState(AbstractName name) throws GBeanNotFoundException {
        return kernel.getGBeanState(name);
    }

    public long getGBeanStartTime(ObjectName name) throws GBeanNotFoundException {
        return kernel.getGBeanStartTime(name);
    }

    public long getGBeanStartTime(AbstractName name) throws GBeanNotFoundException {
        return kernel.getGBeanStartTime(name);
    }

    public boolean isGBeanEnabled(ObjectName name) throws GBeanNotFoundException {
        return kernel.isGBeanEnabled(name);
    }

    public boolean isGBeanEnabled(AbstractName name) throws GBeanNotFoundException {
        return kernel.isGBeanEnabled(name);
    }

    public void setGBeanEnabled(ObjectName name, boolean enabled) throws GBeanNotFoundException {
        kernel.setGBeanEnabled(name, enabled);
    }

    public void setGBeanEnabled(AbstractName name, boolean enabled) throws GBeanNotFoundException {
        kernel.setGBeanEnabled(name, enabled);
    }

    public boolean isRunning() {
        return kernel.isRunning();
    }

    public Set listGBeans(AbstractNameQuery refInfoQuery) {
        return kernel.listGBeans(refInfoQuery);
    }

    public ClassLoader getClassLoaderFor(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getClassLoaderFor(name);
    }

    public GBeanData getGBeanData(ObjectName name) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getGBeanData(name);
    }

    public GBeanData getGBeanData(AbstractName name) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getGBeanData(name);
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws Exception {
        return kernel.getAttribute(objectName, attributeName);
    }

    public Object getAttribute(AbstractName abstractName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return kernel.getAttribute(abstractName, attributeName);
    }

    public void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws Exception {
        kernel.setAttribute(objectName, attributeName, attributeValue);
    }

    public void setAttribute(AbstractName abstractName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        kernel.setAttribute(abstractName, attributeName, attributeValue);
    }

    public Object invoke(ObjectName objectName, String methodName) throws Exception {
        return kernel.invoke(objectName, methodName);
    }

    public Object invoke(AbstractName abstractName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(abstractName, methodName);
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception {
        return kernel.invoke(objectName, methodName, args, types);
    }

    public Object invoke(AbstractName abstractName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(abstractName, methodName, args, types);
    }

    public boolean isLoaded(ObjectName name) {
        return kernel.isLoaded(name);
    }

    public boolean isLoaded(AbstractName name) {
        return kernel.isLoaded(name);
    }

    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(name);
    }

    public GBeanInfo getGBeanInfo(AbstractName name) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(name);
    }

    public Set listGBeans(ObjectName pattern) throws InternalKernelException {
        return kernel.listGBeans(pattern);
    }

    public Set listGBeans(Set patterns) throws InternalKernelException {
        return kernel.listGBeans(patterns);
    }

    public Set listGBeans(GBeanQuery query) {
        return kernel.listGBeans(query);
    }

    public void registerShutdownHook(Runnable hook) {
        kernel.registerShutdownHook(hook);
    }

    public void unregisterShutdownHook(Runnable hook) {
        kernel.unregisterShutdownHook(hook);
    }

    public void shutdown() {
        kernel.shutdown();
    }

    public AbstractName getAbstractNameFor(Object service) {
        return kernel.getAbstractNameFor(service);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(KernelGBean.class);
        infoFactory.addInterface(Kernel.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.setConstructor(new String[]{"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
