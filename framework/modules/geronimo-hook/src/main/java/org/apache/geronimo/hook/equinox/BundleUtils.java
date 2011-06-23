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
package org.apache.geronimo.hook.equinox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

public class BundleUtils {

    public static LinkedHashSet<Bundle> getWiredBundles(Bundle bundle) {
        BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext == null) {
            return new LinkedHashSet<Bundle>(0);
        } else {
            ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
            try {
                return getWiredBundles(packageAdmin, bundle);
            } finally {
                bundle.getBundleContext().ungetService(reference);
            }
        }
    }
    
    public static LinkedHashSet<Bundle> getWiredBundles(PackageAdmin packageAdmin, Bundle bundle) {
        // handle static wire via Import-Package
        List<String> imports = getExternalImports(bundle);
        LinkedHashSet<Bundle> wiredBundles = new LinkedHashSet<Bundle>();
        for (String packageImport : imports) {
            ExportedPackage[] exports = packageAdmin.getExportedPackages(packageImport);
            Bundle wiredBundle = getWiredBundle(bundle, exports);
            if (wiredBundle != null) {
                wiredBundles.add(wiredBundle);
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
    
    public static List<String> getExternalImports(Bundle bundle) {
        List<String> imports = new ArrayList<String>();
        
        String importPackages = (String) bundle.getHeaders().get(Constants.IMPORT_PACKAGE);
        String exportPackages = (String) bundle.getHeaders().get(Constants.EXPORT_PACKAGE);
        try {
            ManifestElement[] importElements = ManifestElement.parseHeader(Constants.IMPORT_PACKAGE, importPackages);
            ManifestElement[] exportElements = ManifestElement.parseHeader(Constants.EXPORT_PACKAGE, exportPackages);
            
            if (importElements != null) {
                for (ManifestElement importElement : importElements) {
                    if (exportElements == null || !isExported(exportElements, importElement)) {
                        imports.add(importElement.getValue());
                    }
                }
            }
        } catch (BundleException e) {
            // ignore
        }
        
        return imports;
    }
    
    private static boolean isExported(ManifestElement[] exportElements, ManifestElement importElement) {
        for (ManifestElement exportElement : exportElements) {
            if (exportElement.getValue().equals(importElement.getValue())) {
                return true;
            }
        }
        return false;
    }
    
}
