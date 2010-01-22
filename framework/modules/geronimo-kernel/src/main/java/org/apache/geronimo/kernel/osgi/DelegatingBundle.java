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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Bundle that delegates ClassLoader operations to a collection of {@link Bundle} objects. 
 * 
 * @version $Rev$ $Date$
 */
public class DelegatingBundle implements Bundle {

    private Collection<Bundle> bundles;
    private Bundle bundle;
    private BundleContext bundleContext;

    public DelegatingBundle(Collection<Bundle> bundles) {
        this.bundles = bundles;
        if (bundles.isEmpty()) {
            throw new IllegalArgumentException("At least one bundle is required");
        }
        // assume first Bundle is the main bundle
        this.bundle = bundles.iterator().next();
        this.bundleContext = new DelegatingBundleContext(this, bundle.getBundleContext());
    }
       
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (Bundle bundle : bundles) {
            try {
                return bundle.loadClass(name);
            } catch (ClassNotFoundException ex) {
                // ignore
            }
        }
        throw new ClassNotFoundException(name);
    }

    public URL getResource(String name) {
        URL resource = null;
        for (Bundle bundle : bundles) {
            resource = bundle.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        ArrayList<URL> allResources = new ArrayList<URL>();
        for (Bundle bundle : bundles) {
            Enumeration<URL> e = (Enumeration<URL>) bundle.getResources(name);
            addToList(allResources, e);
        }
        return Collections.enumeration(allResources); 
    }    
    
    private static void addToList(List<URL> list, Enumeration<URL> enumeration) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                list.add(enumeration.nextElement());
            }
        }
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }
    
    public Enumeration findEntries(String arg0, String arg1, boolean arg2) {
        return bundle.findEntries(arg0, arg1, arg2);
    }

    public long getBundleId() {
        return bundle.getBundleId();
    }

    public URL getEntry(String arg0) {
        return bundle.getEntry(arg0);
    }

    public Enumeration getEntryPaths(String arg0) {
        return bundle.getEntryPaths(arg0);
    }

    public Dictionary getHeaders() {
        return bundle.getHeaders();
    }

    public Dictionary getHeaders(String arg0) {
        return bundle.getHeaders(arg0);
    }

    public long getLastModified() {
        return bundle.getLastModified();
    }

    public String getLocation() {
        return bundle.getLocation();
    }

    public ServiceReference[] getRegisteredServices() {
        return bundle.getRegisteredServices();
    }

    public ServiceReference[] getServicesInUse() {
        return bundle.getServicesInUse();
    }

    public Map getSignerCertificates(int arg0) {
        return bundle.getSignerCertificates(arg0);
    }

    public int getState() {
        return bundle.getState();
    }

    public String getSymbolicName() {
        return bundle.getSymbolicName();
    }

    public Version getVersion() {
        return bundle.getVersion();
    }

    public boolean hasPermission(Object arg0) {
        return bundle.hasPermission(arg0);
    }

    public void start() throws BundleException {
        bundle.start();
    }

    public void start(int arg0) throws BundleException {
        bundle.start(arg0);
    }

    public void stop() throws BundleException {
        bundle.stop();
    }

    public void stop(int arg0) throws BundleException {
        bundle.stop(arg0);
    }

    public void uninstall() throws BundleException {
        bundle.uninstall();
    }

    public void update() throws BundleException {
        bundle.update();
    }

    public void update(InputStream arg0) throws BundleException {
        bundle.update(arg0);
    }

    public String toString() {
        return "[MultiBundle] " + bundles;
    }
           
}
