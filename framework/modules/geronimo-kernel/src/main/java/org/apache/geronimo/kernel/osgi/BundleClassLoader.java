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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * ClassLoader for a {@link Bundle}. 
 * <br/>
 * In OSGi, resource lookup on resources in the <i>META-INF</i> directory using {@link Bundle#getResource(String)} or 
 * {@link Bundle#getResources(String)} does not return the resources found in the wired bundles of the bundle 
 * (wired via <i>Import-Package</i> or <i>DynamicImport-Package</i>). This class loader implementation provides 
 * {@link #getResource(String) and {@link #getResources(String)} methods that do delegate such resource lookups to
 * the wired bundles. 
 * 
 * @version $Rev$ $Date$
 */
public class BundleClassLoader extends ClassLoader implements BundleReference {

    private final static String META_INF_1 = "META-INF/";
    private final static String META_INF_2 = "/META-INF/";
    
    private final Bundle bundle;
    private boolean searchWiredBundles;

    public BundleClassLoader(Bundle bundle) {
        this(bundle, true);
    }
    
    public BundleClassLoader(Bundle bundle, boolean searchWiredBundles) {
        this.bundle = bundle;
        this.searchWiredBundles = searchWiredBundles;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = bundle.loadClass(name);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public String toString() {
        return "[BundleClassLoader] " + bundle;
    }

    @Override
    public URL getResource(String name) {
        URL resource = bundle.getResource(name);
        if (resource == null && isMetaInfResource(name)) {
            LinkedHashSet<Bundle> wiredBundles = BundleUtils.getWiredBundles(bundle);
            Iterator<Bundle> iterator = wiredBundles.iterator();
            while (iterator.hasNext() && resource == null) {                
                resource = iterator.next().getResource(name);
            }
        }
        return resource;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> e = (Enumeration<URL>) bundle.getResources(name);
        if (isMetaInfResource(name)) {
            ArrayList<URL> allResources = new ArrayList<URL>();
            addToList(allResources, e);
            LinkedHashSet<Bundle> wiredBundles = BundleUtils.getWiredBundles(bundle);
            for (Bundle wiredBundle : wiredBundles) {
                Enumeration<URL> resources = wiredBundle.getResources(name);
                addToList(allResources, resources);
            }
            return Collections.enumeration(allResources);            
        } else {
            if (e == null) {
                return Collections.enumeration(Collections.EMPTY_LIST);
            } else {
                return e;
            }
        }
    }

    public void setSearchWiredBundles(boolean search) {
        searchWiredBundles = search;
    }
    
    public boolean getSearchWiredBundles() {
        return searchWiredBundles;
    }
    
    private boolean isMetaInfResource(String name) {
        return searchWiredBundles && name != null && (name.startsWith(META_INF_1) || name.startsWith(META_INF_2));
    }
      
    private void addToList(List<URL> list, Enumeration<URL> enumeration) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                list.add(enumeration.nextElement());
            }
        }
    }
        
    /**
     * Return the bundle associated with this classloader.
     * 
     * In most cases the bundle associated with the classloader is a regular framework bundle. 
     * However, in some cases the bundle associated with the classloader is a {@link DelegatingBundle}.
     * In such cases, the <tt>unwrap</tt> parameter controls whether this function returns the
     * {@link DelegatingBundle} instance or the main application bundle backing with the {@link DelegatingBundle}.
     *
     * @param unwrap If true and if the bundle associated with this classloader is a {@link DelegatingBundle}, 
     *        this function will return the main application bundle backing with the {@link DelegatingBundle}. 
     *        Otherwise, the bundle associated with this classloader is returned as is.
     * @return The bundle associated with this classloader.
     */
    public Bundle getBundle(boolean unwrap) {
        if (unwrap && bundle instanceof DelegatingBundle) {
            return ((DelegatingBundle) bundle).getMainBundle();
        }
        return bundle;
    }
    
    /**
     * Return the bundle associated with this classloader.
     * 
     * This method calls {@link #getBundle(boolean) getBundle(true)} and therefore always returns a regular 
     * framework bundle.  
     * <br><br>
     * Note: Some libraries use {@link BundleReference#getBundle()} to obtain a bundle for the given 
     * classloader and expect the returned bundle instance to be work with any OSGi API. Some of these API might
     * not work if {@link DelegatingBundle} is returned. That is why this function will always return
     * a regular framework bundle. See {@link #getBundle(boolean)} for more information.
     *
     * @return The bundle associated with this classloader.
     */
    public Bundle getBundle() {
        return getBundle(true);
    }
}
