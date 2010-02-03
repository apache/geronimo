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

import java.util.Dictionary;
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
     * Returns Bundle (if any) associated with current thread's context ClassLoader.
     */
    public static Bundle getContextBundle() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader instanceof BundleReference) {
            return ((BundleReference) classLoader).getBundle();
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
