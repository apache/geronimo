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

import java.util.Date;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev:386515 $ $Date$
 */
public class KernelGBean implements Kernel{
    private final Kernel kernel;

    public KernelGBean(Kernel kernel) {
        this.kernel = kernel;
    }

    public String getKernelName() {
        return kernel.getKernelName();
    }

    public Naming getNaming() {
        return kernel.getNaming();
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

    public void loadGBean(GBeanData gbeanData, BundleContext bundleContext) throws GBeanAlreadyExistsException, InternalKernelException {
        kernel.loadGBean(gbeanData, bundleContext);
    }

    public boolean isLoaded(AbstractName name) {
        return kernel.isLoaded(name);
    }

    public boolean isLoaded(String shortName) {
        return kernel.isLoaded(shortName);
    }

    public boolean isLoaded(Class type) {
        return kernel.isLoaded(type);
    }

    public boolean isLoaded(String shortName, Class type) {
        return kernel.isLoaded(shortName, type);
    }

    public Object getGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        return kernel.getGBean(name);
    }

    public Object getGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        return kernel.getGBean(name);
    }

    public Object getGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        return kernel.getGBean(shortName);
    }

    public <T> T getGBean(Class<T> type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        return kernel.getGBean(type);
    }

    public <T> T getGBean(String shortName, Class<T> type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        return kernel.getGBean(shortName, type);
    }

    public void startGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startGBean(name);
    }

    public void startGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startGBean(shortName);
    }

    public void startGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startGBean(type);
    }

    public void startGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startGBean(shortName, type);
    }

    public void startRecursiveGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startRecursiveGBean(name);
    }

    public void startRecursiveGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startRecursiveGBean(shortName);
    }

    public void startRecursiveGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startRecursiveGBean(type);
    }

    public void startRecursiveGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.startRecursiveGBean(shortName, type);
    }

    public boolean isRunning(AbstractName name) {
        return kernel.isRunning(name);
    }

    public boolean isRunning(String shortName) {
        return kernel.isRunning(shortName);
    }

    public boolean isRunning(Class type) {
        return kernel.isRunning(type);
    }

    public boolean isRunning(String shortName, Class type) {
        return kernel.isRunning(shortName, type);
    }

    public void stopGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.stopGBean(name);
    }

    public void stopGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.stopGBean(shortName);
    }

    public void stopGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.stopGBean(type);
    }

    public void stopGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.stopGBean(shortName, type);
    }

    public void unloadGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.unloadGBean(name);
    }

    public void unloadGBean(String shortName) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.unloadGBean(shortName);
    }

    public void unloadGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.unloadGBean(type);
    }

    public void unloadGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        kernel.unloadGBean(shortName, type);
    }

    public int getGBeanState(ObjectName name) throws GBeanNotFoundException {
        return kernel.getGBeanState(name);
    }

    public int getGBeanState(AbstractName name) throws GBeanNotFoundException {
        return kernel.getGBeanState(name);
    }

    public int getGBeanState(String shortName) throws GBeanNotFoundException {
        return kernel.getGBeanState(shortName);
    }

    public int getGBeanState(Class type) throws GBeanNotFoundException {
        return kernel.getGBeanState(type);
    }

    public int getGBeanState(String shortName, Class type) throws GBeanNotFoundException {
        return kernel.getGBeanState(shortName, type);
    }

    public long getGBeanStartTime(AbstractName name) throws GBeanNotFoundException {
        return kernel.getGBeanStartTime(name);
    }

    public long getGBeanStartTime(String shortName) throws GBeanNotFoundException {
        return kernel.getGBeanStartTime(shortName);
    }

    public long getGBeanStartTime(Class type) throws GBeanNotFoundException {
        return kernel.getGBeanStartTime(type);
    }

    public long getGBeanStartTime(String shortName, Class type) throws GBeanNotFoundException {
        return kernel.getGBeanStartTime(shortName, type);
    }

    public Bundle getBundleFor(AbstractName name) throws GBeanNotFoundException {
        return kernel.getBundleFor(name);
    }

    public Bundle getBundleFor(String shortName) throws GBeanNotFoundException {
        return kernel.getBundleFor(shortName);
    }

    public Bundle getBundleFor(Class type) throws GBeanNotFoundException {
        return kernel.getBundleFor(type);
    }

    public Bundle getBundleFor(String shortName, Class type) throws GBeanNotFoundException {
        return kernel.getBundleFor(shortName, type);
    }

    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(name);
    }

    public GBeanInfo getGBeanInfo(AbstractName name) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(name);
    }

    public GBeanInfo getGBeanInfo(String shortName) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(shortName);
    }

    public GBeanInfo getGBeanInfo(Class type) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(type);
    }

    public GBeanInfo getGBeanInfo(String shortName, Class type) throws GBeanNotFoundException {
        return kernel.getGBeanInfo(shortName, type);
    }

    public GBeanData getGBeanData(AbstractName name) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getGBeanData(name);
    }

    public GBeanData getGBeanData(String shortName) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getGBeanData(shortName);
    }

    public GBeanData getGBeanData(Class type) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getGBeanData(type);
    }

    public GBeanData getGBeanData(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException {
        return kernel.getGBeanData(shortName, type);
    }

    public Set<AbstractName> listGBeans(ObjectName pattern) {
        return kernel.listGBeans(pattern);
    }

    public Set<AbstractName> listGBeans(Set patterns) {
        return kernel.listGBeans(patterns);
    }

    public Object getAttribute(ObjectName objectName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return kernel.getAttribute(objectName, attributeName);
    }

    public Object getAttribute(AbstractName abstractName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return kernel.getAttribute(abstractName, attributeName);
    }

    public Object getAttribute(String shortName, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return kernel.getAttribute(shortName, attributeName);
    }

    public Object getAttribute(Class type, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return kernel.getAttribute(type, attributeName);
    }

    public Object getAttribute(String shortName, Class type, String attributeName) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        return kernel.getAttribute(shortName, type, attributeName);
    }

    public void setAttribute(AbstractName abstractName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        kernel.setAttribute(abstractName, attributeName, attributeValue);
    }

    public void setAttribute(String shortName, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        kernel.setAttribute(shortName, attributeName, attributeValue);
    }

    public void setAttribute(Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        kernel.setAttribute(type, attributeName, attributeValue);
    }

    public void setAttribute(String shortName, Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        kernel.setAttribute(shortName, type, attributeName, attributeValue);
    }

    public Object invoke(ObjectName objectName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(objectName, methodName);
    }

    public Object invoke(AbstractName abstractName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(abstractName, methodName);
    }

    public Object invoke(String shortName, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(shortName, methodName);
    }

    public Object invoke(Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(type, methodName);
    }

    public Object invoke(String shortName, Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(shortName, type, methodName);
    }

    public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(objectName, methodName, args, types);
    }

    public String getStateReason(AbstractName abstractName) {
        return kernel.getStateReason(abstractName);
    }

    public Object invoke(AbstractName abstractName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(abstractName, methodName, args, types);
    }

    public Object invoke(String shortName, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(shortName, methodName, args, types);
    }

    public Object invoke(Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(type, methodName, args, types);
    }

    public Object invoke(String shortName, Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        return kernel.invoke(shortName, type, methodName, args, types);
    }

    public AbstractName getAbstractNameFor(Object service) {
        return kernel.getAbstractNameFor(service);
    }

    public String getShortNameFor(Object service) {
        return kernel.getShortNameFor(service);
    }

    public void boot(BundleContext bundleContext) throws Exception {
//        kernel.boot();
    }

    public Date getBootTime() {
        return kernel.getBootTime();
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

    public boolean isRunning() {
        return kernel.isRunning();
    }

    public Set<AbstractName> listGBeans(AbstractNameQuery refInfoQuery) {
        return kernel.listGBeans(refInfoQuery);
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
