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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.geronimo.kernel.osgi.BundleDescription.ExportPackage;
import org.apache.geronimo.kernel.osgi.BundleDescription.HeaderEntry;
import org.apache.geronimo.kernel.osgi.BundleDescription.RequireBundle;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;

/**
 * Finds all available classes to a bundle by scanning Bundle-ClassPath, 
 * Import-Package, and Require-Bundle headers of the given bundle and its fragments.
 * DynamicImport-Package header is not considered during scanning.
 * 
 * @version $Rev$ $Date$
 */
public class BundleClassFinder {
   
    private static final String EXT = ".class";
    private static final String PATTERN = "*.class";
    
    private Bundle bundle;
    private boolean scanImportPackages;
    private PackageAdmin packageAdmin;
    private Map<Bundle, Set<String>> classMap;
    
    public BundleClassFinder(PackageAdmin packageAdmin, Bundle bundle) {
        this.packageAdmin = packageAdmin;
        this.bundle = bundle;  
        this.scanImportPackages = true;
    }
    
    public void setScanImportPackages(boolean searchImports) {
        this.scanImportPackages = searchImports;
    }
    
    public boolean getScanImportPackages() {
        return this.scanImportPackages;
    }
    
    public List<Class> loadClasses(Set<String> classes) {
        List<Class> loadedClasses = new ArrayList<Class>();
        for (String clazz : classes) {
            try {
                loadedClasses.add(bundle.loadClass(clazz));
            } catch (Exception ignore) {
                // ignore
            }
        }
        return loadedClasses;
    }
    
    /**
     * Finds all available classes to the bundle. Some of the classes in the returned set 
     * might not be loadable.
     * 
     * @return classes visible to the bundle. Not all classes returned might be loadable. 
     */
    public Set<String> find() {    
        Set<String> classes = new LinkedHashSet<String>();
        
        classMap = new HashMap<Bundle, Set<String>>();
        
        scanImportPackages(classes, bundle, bundle);    
        scanRequireBundles(classes, bundle);
        scanBundleClassPath(classes, bundle);
        
        Bundle[] fragments = packageAdmin.getFragments(bundle);
        if (fragments != null) {
            for (Bundle fragment : fragments) {
                scanImportPackages(classes, bundle, fragment);
                scanRequireBundles(classes, fragment);
                scanBundleClassPath(classes, fragment);
            }
        }
        
        classMap.clear();
        
        return classes;
    }
    
    private void scanImportPackages(Collection<String> classes, Bundle host, Bundle fragment) {
        if (!scanImportPackages) {
            return;
        }
        BundleDescription description = new BundleDescription(fragment.getHeaders());        
        List<BundleDescription.ImportPackage> imports = description.getExternalImports();
        for (BundleDescription.ImportPackage packageImport : imports) {
            ExportedPackage[] exports = packageAdmin.getExportedPackages(packageImport.getName());
            Bundle wiredBundle = isWired(host, exports);
            if (wiredBundle != null) {
                Set<String> allClasses = findAllClasses(wiredBundle);
                addMatchingClasses(classes, allClasses, packageImport.getName());
            }
        }  
    }
    
    private Set<String> findAllClasses(Bundle bundle) {
        Set<String> allClasses = classMap.get(bundle);
        if (allClasses == null) {
            BundleClassFinder finder = new BundleClassFinder(packageAdmin, bundle);
            finder.setScanImportPackages(false);
            allClasses = finder.find();
            classMap.put(bundle, allClasses);
        }
        return allClasses;
    }
    
    private void addMatchingClasses(Collection<String> classes, Set<String> allClasses, String packageName) {
        String prefix = packageName + ".";
        for (String clazz : allClasses) {
            if (clazz.startsWith(prefix) && clazz.indexOf('.', prefix.length()) == -1) {
                classes.add(clazz);
            }
        }
    }
    
    private void scanRequireBundles(Collection<String> classes, Bundle bundle) {
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        List<RequireBundle> requiredBundleList = description.getRequireBundle();
        for (RequireBundle requiredBundle : requiredBundleList) {
            RequiredBundle[] requiredBundles = packageAdmin.getRequiredBundles(requiredBundle.getName());
            Bundle wiredBundle = isWired(bundle, requiredBundles);
            if (wiredBundle != null) {
                Set<String> allClasses = findAllClasses(wiredBundle);                
                BundleDescription wiredBundleDescription = new BundleDescription(wiredBundle.getHeaders());
                List<ExportPackage> exportPackages = wiredBundleDescription.getExportPackage();
                for (ExportPackage exportPackage : exportPackages) {
                    addMatchingClasses(classes, allClasses, exportPackage.getName());
                }
            }
        }
    }
    
    private void scanBundleClassPath(Collection<String> resources, Bundle bundle) {
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        List<HeaderEntry> paths = description.getBundleClassPath();
        if (paths.isEmpty()) {
            scanDirectory(resources, bundle, "/");
        } else {
            for (HeaderEntry path : paths) {
                String name = path.getName();
                if (name.equals(".") || name.equals("/")) {
                    // scan root
                    scanDirectory(resources, bundle, "/");
                } else if (name.endsWith(".jar") || name.endsWith(".zip")) {
                    // scan embedded jar/zip
                    scanZip(resources, bundle, name);
                } else {
                    // assume it's a directory
                    scanDirectory(resources, bundle, "/" + name);
                }
            }
        }
    }    
    
    private void scanDirectory(Collection<String> classes, Bundle bundle, String basePath) {
        basePath = addSlash(basePath);
        Enumeration e = bundle.findEntries(basePath, PATTERN, true);
        if (e != null) {
            while (e.hasMoreElements()) {
                URL u = (URL) e.nextElement();
                String name = u.getPath().substring(basePath.length());                
                classes.add(toClassName(name));
            }
        }
    }
    
    private void scanZip(Collection<String> classes, Bundle bundle, String zipName) {   
        URL zipEntry = bundle.getEntry(zipName);
        if (zipEntry == null) {
            return;
        }
        ZipInputStream in = null;
        try {
            in = new ZipInputStream(zipEntry.openStream());
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(EXT)) {
                    classes.add(toClassName(name));
                }
            }
        } catch (IOException ignore) {
            // ignore
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException e) {}
            }
        }
    }
    
    private static String toClassName(String name) {
        name = name.substring(0, name.length() - EXT.length());
        name = name.replaceAll("/", ".");
        return name;
    }
    
    private static String addSlash(String name) {
        if (!name.endsWith("/")) {
            name = name + "/";
        }
        return name;
    }
    
    private static Bundle isWired(Bundle bundle, ExportedPackage[] exports) {
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
    
    private static Bundle isWired(Bundle bundle, RequiredBundle[] requiredBundles) {
        if (requiredBundles != null) {
            for (RequiredBundle requiredBundle : requiredBundles) {
                Bundle[] requiringBundles = requiredBundle.getRequiringBundles();
                if (requiringBundles != null) {
                    for (Bundle requiringBundle : requiringBundles) {
                        if (requiringBundle == bundle) {
                            return requiredBundle.getBundle();
                        }
                    }
                }
            }
        }
        return null;
    }
    
}
