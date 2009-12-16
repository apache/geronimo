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
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

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
            LinkedHashSet<Bundle> wiredBundles = getWiredBundles();
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
            LinkedHashSet<Bundle> wiredBundles = getWiredBundles();
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
    
    private LinkedHashSet<Bundle> getWiredBundles() {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
        
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        
        // handle static wire via Import-Package
        List<BundleDescription.Package> imports = description.getExternalImports();
        LinkedHashSet<Bundle> wiredBundles = new LinkedHashSet<Bundle>();
        for (BundleDescription.Package packageImport : imports) {
            ExportedPackage[] exports = packageAdmin.getExportedPackages(packageImport.getName());
            Bundle wiredBundle = getWiredBundle(exports);
            if (wiredBundle != null) {
                wiredBundles.add(wiredBundle);
            }
        }
                
        // handle dynamic wire via DynamicImport-Package
        if (!description.getDynamicImportPackage().isEmpty()) {
            for (Bundle b : bundle.getBundleContext().getBundles()) {
                if (!wiredBundles.contains(b)) {
                    ExportedPackage[] exports = packageAdmin.getExportedPackages(b);
                    Bundle wiredBundle = getWiredBundle(exports); 
                    if (wiredBundle != null) {
                        wiredBundles.add(wiredBundle);
                    }
                }
            }
        }
        
        bundle.getBundleContext().ungetService(reference);
        
        return wiredBundles;
    }
    
    private Bundle getWiredBundle(ExportedPackage[] exports) {
        if (exports != null) {
            for (ExportedPackage exportedPackage : exports) {
                Bundle[] importingBundles = exportedPackage.getImportingBundles();
                if (importingBundles != null) {
                    for (Bundle importingBundle : importingBundles) {
                        if (importingBundle == bundle) {
                            return exportedPackage.getExportingBundle();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void addToList(List<URL> list, Enumeration<URL> enumeration) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                list.add(enumeration.nextElement());
            }
        }
    }
    
    /**
     * Return the bundle instance backing this classloader.
     *
     * @return The bundle used to source the classloader.
     */
    public Bundle getBundle() {
        return bundle;
    }
}
