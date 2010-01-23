/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.kernel.osgi;

import java.io.File;
import java.io.InputStream;
import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * BundleContext for DelegatingBundle. 
 * 
 * @version $Rev$ $Date$
 */
public class DelegatingBundleContext implements BundleContext {

    private DelegatingBundle bundle;
    private BundleContext bundleContext;
    
    public DelegatingBundleContext(DelegatingBundle bundle, BundleContext bundleContext) {
        this.bundle = bundle;
        this.bundleContext = bundleContext;
    }
    
    public Bundle getBundle() {
        return bundle;
    }
        
    public void addBundleListener(BundleListener arg0) {
        bundleContext.addBundleListener(arg0);
    }

    public void addFrameworkListener(FrameworkListener arg0) {
        bundleContext.addFrameworkListener(arg0);
    }

    public void addServiceListener(ServiceListener arg0, String arg1) throws InvalidSyntaxException {
        bundleContext.addServiceListener(arg0, arg1);
    }

    public void addServiceListener(ServiceListener arg0) {
        bundleContext.addServiceListener(arg0);
    }

    public Filter createFilter(String arg0) throws InvalidSyntaxException {
        return bundleContext.createFilter(arg0);
    }

    public ServiceReference[] getAllServiceReferences(String arg0, String arg1)
            throws InvalidSyntaxException {
        return bundleContext.getAllServiceReferences(arg0, arg1);
    }

    public Bundle getBundle(long arg0) {
        return bundleContext.getBundle(arg0);
    }

    public Bundle[] getBundles() {
        return bundleContext.getBundles();
    }

    public File getDataFile(String arg0) {
        return bundleContext.getDataFile(arg0);
    }

    public String getProperty(String arg0) {
        return bundleContext.getProperty(arg0);
    }

    public Object getService(ServiceReference arg0) {
        return bundleContext.getService(arg0);
    }

    public ServiceReference getServiceReference(String arg0) {
        return bundleContext.getServiceReference(arg0);
    }

    public ServiceReference[] getServiceReferences(String arg0, String arg1)
            throws InvalidSyntaxException {
        return bundleContext.getServiceReferences(arg0, arg1);
    }

    public Bundle installBundle(String arg0, InputStream arg1) throws BundleException {
        return bundleContext.installBundle(arg0, arg1);
    }

    public Bundle installBundle(String arg0) throws BundleException {
        return bundleContext.installBundle(arg0);
    }

    public ServiceRegistration registerService(String arg0, Object arg1, Dictionary arg2) {
        return bundleContext.registerService(arg0, arg1, arg2);
    }

    public ServiceRegistration registerService(String[] arg0, Object arg1, Dictionary arg2) {
        return bundleContext.registerService(arg0, arg1, arg2);
    }

    public void removeBundleListener(BundleListener arg0) {
        bundleContext.removeBundleListener(arg0);
    }

    public void removeFrameworkListener(FrameworkListener arg0) {
        bundleContext.removeFrameworkListener(arg0);
    }

    public void removeServiceListener(ServiceListener arg0) {
        bundleContext.removeServiceListener(arg0);
    }

    public boolean ungetService(ServiceReference arg0) {
        return bundleContext.ungetService(arg0);
    }
    
}
