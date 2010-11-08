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
package org.apache.geronimo.derby;

import java.util.Date;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * 
 *
 * @version $Rev$ $Date$
 */
public class MockKernel implements Kernel{
    
    private static final String DERBYNETWORK_GBEAN_NAME = "DerbyNetwork";    
    private static final String DERBYNETWORK_GBEAN_ATTRIBUTE_USERNAME = "userName";    
    private static final String DERBYNETWORK_GBEAN_ATTRIBUTE_USERPASSWORD = "userPassword";

    /* 
     * Mock implementation for test purpose
     */
    public Object getAttribute(String shortName, String attributeName) throws GBeanNotFoundException,
            NoSuchAttributeException, Exception {
        
        if (DERBYNETWORK_GBEAN_NAME.equals(shortName)) {
            
            if (DERBYNETWORK_GBEAN_ATTRIBUTE_USERNAME.equals(attributeName)) {
                return new String("dbadmin");
            } else if (DERBYNETWORK_GBEAN_ATTRIBUTE_USERNAME.equals(attributeName)) {
                return new String ("manager");
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#boot()
     */
    public void boot() throws Exception {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getAbstractNameFor(java.lang.Object)
     */
    public AbstractName getAbstractNameFor(Object service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getAttribute(org.apache.geronimo.gbean.AbstractName, java.lang.String)
     */
    public Object getAttribute(AbstractName name, String attributeName) throws GBeanNotFoundException,
            NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getAttribute(java.lang.Class, java.lang.String)
     */
    public Object getAttribute(Class type, String attributeName) throws GBeanNotFoundException,
            NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getAttribute(java.lang.String, java.lang.Class, java.lang.String)
     */
    public Object getAttribute(String shortName, Class type, String attributeName) throws GBeanNotFoundException,
            NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getAttribute(javax.management.ObjectName, java.lang.String)
     */
    public Object getAttribute(ObjectName name, String attributeName) throws GBeanNotFoundException,
            NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getBootTime()
     */
    public Date getBootTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getClassLoaderFor(org.apache.geronimo.gbean.AbstractName)
     */
    public ClassLoader getClassLoaderFor(AbstractName name) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getClassLoaderFor(java.lang.String)
     */
    public ClassLoader getClassLoaderFor(String shortName) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getClassLoaderFor(java.lang.Class)
     */
    public ClassLoader getClassLoaderFor(Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getClassLoaderFor(java.lang.String, java.lang.Class)
     */
    public ClassLoader getClassLoaderFor(String shortName, Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getDependencyManager()
     */
    public DependencyManager getDependencyManager() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBean(org.apache.geronimo.gbean.AbstractName)
     */
    public Object getGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBean(java.lang.String)
     */
    public Object getGBean(String shortName) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBean(java.lang.Class)
     */
    public Object getGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBean(java.lang.String, java.lang.Class)
     */
    public Object getGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBean(javax.management.ObjectName)
     */
    public Object getGBean(ObjectName name) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanData(org.apache.geronimo.gbean.AbstractName)
     */
    public GBeanData getGBeanData(AbstractName name) throws GBeanNotFoundException, InternalKernelException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanData(java.lang.String)
     */
    public GBeanData getGBeanData(String shortName) throws GBeanNotFoundException, InternalKernelException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanData(java.lang.Class)
     */
    public GBeanData getGBeanData(Class type) throws GBeanNotFoundException, InternalKernelException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanData(java.lang.String, java.lang.Class)
     */
    public GBeanData getGBeanData(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanInfo(org.apache.geronimo.gbean.AbstractName)
     */
    public GBeanInfo getGBeanInfo(AbstractName name) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanInfo(java.lang.String)
     */
    public GBeanInfo getGBeanInfo(String shortName) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanInfo(java.lang.Class)
     */
    public GBeanInfo getGBeanInfo(Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanInfo(java.lang.String, java.lang.Class)
     */
    public GBeanInfo getGBeanInfo(String shortName, Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanInfo(javax.management.ObjectName)
     */
    public GBeanInfo getGBeanInfo(ObjectName name) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanStartTime(org.apache.geronimo.gbean.AbstractName)
     */
    public long getGBeanStartTime(AbstractName name) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanStartTime(java.lang.String)
     */
    public long getGBeanStartTime(String shortName) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanStartTime(java.lang.Class)
     */
    public long getGBeanStartTime(Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanStartTime(java.lang.String, java.lang.Class)
     */
    public long getGBeanStartTime(String shortName, Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanState(org.apache.geronimo.gbean.AbstractName)
     */
    public int getGBeanState(AbstractName name) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanState(java.lang.String)
     */
    public int getGBeanState(String shortName) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanState(java.lang.Class)
     */
    public int getGBeanState(Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanState(java.lang.String, java.lang.Class)
     */
    public int getGBeanState(String shortName, Class type) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getGBeanState(javax.management.ObjectName)
     */
    public int getGBeanState(ObjectName name) throws GBeanNotFoundException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getKernelName()
     */
    public String getKernelName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getLifecycleMonitor()
     */
    public LifecycleMonitor getLifecycleMonitor() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getNaming()
     */
    public Naming getNaming() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getProxyManager()
     */
    public ProxyManager getProxyManager() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getShortNameFor(java.lang.Object)
     */
    public String getShortNameFor(Object service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#getStateReason(org.apache.geronimo.gbean.AbstractName)
     */
    public String getStateReason(AbstractName abstractName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(org.apache.geronimo.gbean.AbstractName, java.lang.String)
     */
    public Object invoke(AbstractName name, String methodName) throws GBeanNotFoundException, NoSuchOperationException,
            InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(java.lang.String, java.lang.String)
     */
    public Object invoke(String shortName, String methodName) throws GBeanNotFoundException, NoSuchOperationException,
            InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(java.lang.Class, java.lang.String)
     */
    public Object invoke(Class type, String methodName) throws GBeanNotFoundException, NoSuchOperationException,
            InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(java.lang.String, java.lang.Class, java.lang.String)
     */
    public Object invoke(String shortName, Class type, String methodName) throws GBeanNotFoundException,
            NoSuchOperationException, InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(org.apache.geronimo.gbean.AbstractName, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(AbstractName name, String methodName, Object[] args, String[] types)
            throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(java.lang.String, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(String shortName, String methodName, Object[] args, String[] types)
            throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(java.lang.Class, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(Class type, String methodName, Object[] args, String[] types) throws GBeanNotFoundException,
            NoSuchOperationException, InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(java.lang.String, java.lang.Class, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(String shortName, Class type, String methodName, Object[] args, String[] types)
            throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(javax.management.ObjectName, java.lang.String)
     */
    public Object invoke(ObjectName name, String methodName) throws GBeanNotFoundException, NoSuchOperationException,
            InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(ObjectName name, String methodName, Object[] args, String[] types)
            throws GBeanNotFoundException, NoSuchOperationException, InternalKernelException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isLoaded(org.apache.geronimo.gbean.AbstractName)
     */
    public boolean isLoaded(AbstractName name) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isLoaded(java.lang.String)
     */
    public boolean isLoaded(String shortName) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isLoaded(java.lang.Class)
     */
    public boolean isLoaded(Class type) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isLoaded(java.lang.String, java.lang.Class)
     */
    public boolean isLoaded(String shortName, Class type) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isRunning(org.apache.geronimo.gbean.AbstractName)
     */
    public boolean isRunning(AbstractName name) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isRunning(java.lang.String)
     */
    public boolean isRunning(String shortName) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isRunning(java.lang.Class)
     */
    public boolean isRunning(Class type) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isRunning(java.lang.String, java.lang.Class)
     */
    public boolean isRunning(String shortName, Class type) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#isRunning()
     */
    public boolean isRunning() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#listGBeans(org.apache.geronimo.gbean.AbstractNameQuery)
     */
    public Set listGBeans(AbstractNameQuery abstractNameQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#listGBeans(java.util.Set)
     */
    public Set listGBeans(Set abstractNameQueries) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#listGBeans(javax.management.ObjectName)
     */
    public Set listGBeans(ObjectName pattern) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#loadGBean(org.apache.geronimo.gbean.GBeanData, java.lang.ClassLoader)
     */
    public void loadGBean(GBeanData gbeanData, ClassLoader classLoader) throws GBeanAlreadyExistsException,
            InternalKernelException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#registerShutdownHook(java.lang.Runnable)
     */
    public void registerShutdownHook(Runnable hook) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#setAttribute(org.apache.geronimo.gbean.AbstractName, java.lang.String, java.lang.Object)
     */
    public void setAttribute(AbstractName name, String attributeName, Object attributeValue)
            throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#setAttribute(java.lang.String, java.lang.String, java.lang.Object)
     */
    public void setAttribute(String shortName, String attributeName, Object attributeValue)
            throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#setAttribute(java.lang.Class, java.lang.String, java.lang.Object)
     */
    public void setAttribute(Class type, String attributeName, Object attributeValue) throws GBeanNotFoundException,
            NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#setAttribute(java.lang.String, java.lang.Class, java.lang.String, java.lang.Object)
     */
    public void setAttribute(String shortName, Class type, String attributeName, Object attributeValue)
            throws GBeanNotFoundException, NoSuchAttributeException, Exception {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#shutdown()
     */
    public void shutdown() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startGBean(org.apache.geronimo.gbean.AbstractName)
     */
    public void startGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startGBean(java.lang.String)
     */
    public void startGBean(String shortName) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startGBean(java.lang.Class)
     */
    public void startGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startGBean(java.lang.String, java.lang.Class)
     */
    public void startGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startRecursiveGBean(org.apache.geronimo.gbean.AbstractName)
     */
    public void startRecursiveGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startRecursiveGBean(java.lang.String)
     */
    public void startRecursiveGBean(String shortName) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startRecursiveGBean(java.lang.Class)
     */
    public void startRecursiveGBean(Class type) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#startRecursiveGBean(java.lang.String, java.lang.Class)
     */
    public void startRecursiveGBean(String shortName, Class type) throws GBeanNotFoundException,
            InternalKernelException, IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#stopGBean(org.apache.geronimo.gbean.AbstractName)
     */
    public void stopGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#stopGBean(java.lang.String)
     */
    public void stopGBean(String shortName) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#stopGBean(java.lang.Class)
     */
    public void stopGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#stopGBean(java.lang.String, java.lang.Class)
     */
    public void stopGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#unloadGBean(org.apache.geronimo.gbean.AbstractName)
     */
    public void unloadGBean(AbstractName name) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#unloadGBean(java.lang.String)
     */
    public void unloadGBean(String shortName) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#unloadGBean(java.lang.Class)
     */
    public void unloadGBean(Class type) throws GBeanNotFoundException, InternalKernelException, IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#unloadGBean(java.lang.String, java.lang.Class)
     */
    public void unloadGBean(String shortName, Class type) throws GBeanNotFoundException, InternalKernelException,
            IllegalStateException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.kernel.Kernel#unregisterShutdownHook(java.lang.Runnable)
     */
    public void unregisterShutdownHook(Runnable hook) {
        // TODO Auto-generated method stub
        
    }

}
