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

import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @version $Rev$ $Date$
 */
public class BundleUtils {
   
    public static boolean canStart(Bundle bundle) {
        return (bundle.getState() != Bundle.STARTING)  && (!isFragment(bundle));
    }
    
    public static boolean isFragment(Bundle bundle) {
        Dictionary headers = bundle.getHeaders();
        return (headers != null && headers.get(Constants.FRAGMENT_HOST) != null);
    }
    
    /**
     * Returns bundle (if any) associated with current thread's context classloader.
     * 
     * @param unwrap if true and if the bundle associated with the context classloader is a 
     *        {@link DelegatingBundle}, this function will return the main application bundle 
     *        backing with the {@link DelegatingBundle}. Otherwise, the bundle associated with 
     *        the context classloader is returned as is. See {@link BundleClassLoader#getBundle(boolean)}
     *        for more information.
     * @return The bundle associated with the current thread's context classloader. Might be null.
     */
    public static Bundle getContextBundle(boolean unwrap) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader instanceof BundleClassLoader) {
            return ((BundleClassLoader) classLoader).getBundle(unwrap);        
        } else if (classLoader instanceof BundleReference) {
            return ((BundleReference) classLoader).getBundle();
        } else {
            return null;
        }
    }
    
    /**
     * Works like {@link Bundle#getEntryPaths(String)} but also returns paths
     * in attached fragment bundles.
     * 
     * @param bundle
     * @param name
     * @return
     */
    public static Enumeration<String> getEntryPaths(Bundle bundle, String name) {
        Enumeration<URL> entries = bundle.findEntries(name, null, false);
        if (entries == null) {
            return null;
        }
        LinkedHashSet<String> paths = new LinkedHashSet<String>();
        while (entries.hasMoreElements()) {
            URL url = entries.nextElement();
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            paths.add(path);
        }
        return Collections.enumeration(paths);
    }
    
    /**
     * Works like {@link Bundle#getEntry(String)} but also checks
     * attached fragment bundles for the given entry.
     * 
     * @param bundle
     * @param name
     * @return
     */
    public static URL getEntry(Bundle bundle, String name) {
        if (name.equals("/")) {
            return bundle.getEntry(name);
        } else if (name.endsWith("/")) {
             name = name.substring(0, name.length() - 1);       
        }
        String path;
        String pattern;
        int pos = name.lastIndexOf("/");
        if (pos == -1) {
            path = "/";
            pattern = name;
        } else if (pos == 0) {
            path = "/";
            pattern = name.substring(1);
        } else {
            path = name.substring(0, pos);
            pattern = name.substring(pos + 1);
        }
        Enumeration<URL> entries = bundle.findEntries(path, pattern, false);
        if (entries != null && entries.hasMoreElements()) {
            return entries.nextElement();
        } else {
            return null;
        }
    }
    
    public static LinkedHashSet<Bundle> getWiredBundles(Bundle bundle) {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);        
        try {
            return getWiredBundles(packageAdmin, bundle);
        } finally {
            bundle.getBundleContext().ungetService(reference);
        }
    }
    
    public static LinkedHashSet<Bundle> getWiredBundles(PackageAdmin packageAdmin, Bundle bundle) {
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        
        // handle static wire via Import-Package
        List<BundleDescription.ImportPackage> imports = description.getExternalImports();
        LinkedHashSet<Bundle> wiredBundles = new LinkedHashSet<Bundle>();
        for (BundleDescription.ImportPackage packageImport : imports) {
            ExportedPackage[] exports = packageAdmin.getExportedPackages(packageImport.getName());
            Bundle wiredBundle = getWiredBundle(bundle, exports);
            if (wiredBundle != null) {
                wiredBundles.add(wiredBundle);
            }
        }
                
        // handle dynamic wire via DynamicImport-Package
        if (!description.getDynamicImportPackage().isEmpty()) {
            for (Bundle b : bundle.getBundleContext().getBundles()) {
                if (!wiredBundles.contains(b)) {
                    ExportedPackage[] exports = packageAdmin.getExportedPackages(b);
                    Bundle wiredBundle = getWiredBundle(bundle, exports); 
                    if (wiredBundle != null) {
                        wiredBundles.add(wiredBundle);
                    }
                }
            }
        }
        
        return wiredBundles;
    }
    
    private static Bundle getWiredBundle(Bundle bundle, ExportedPackage[] exports) {
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

}
