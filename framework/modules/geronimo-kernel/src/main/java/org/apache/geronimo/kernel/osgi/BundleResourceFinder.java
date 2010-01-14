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
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.geronimo.kernel.osgi.BundleDescription.HeaderEntry;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Finds all available resources to a bundle by scanning Bundle-ClassPath header 
 * of the given bundle and its fragments.
 * DynamicImport-Package header is not considered during scanning.
 * 
 * @version $Rev$ $Date$
 */
public class BundleResourceFinder {
   
    private Bundle bundle;
    private PackageAdmin packageAdmin;
    private String prefix;
    private String suffix;
    
    public BundleResourceFinder(PackageAdmin packageAdmin, Bundle bundle, String prefix, String suffix) {
        this.packageAdmin = packageAdmin;
        this.bundle = bundle;
        this.prefix = prefix.trim();
        this.suffix = suffix.trim();
    }
    
    public Set<URL> find() {
        Set<URL> resources = new LinkedHashSet<URL>();

        scanBundleClassPath(resources, bundle);
        
        Bundle[] fragments = packageAdmin.getFragments(bundle);
        if (fragments != null) {
            for (Bundle fragment : fragments) {
                scanBundleClassPath(resources, fragment);
            }
        }
        
        return resources;
    }
    
    private void scanBundleClassPath(Collection<URL> resources, Bundle bundle) {
        BundleDescription desc = new BundleDescription(bundle.getHeaders());
        List<HeaderEntry> paths = desc.getBundleClassPath();
        if (paths.isEmpty()) {
            scanDirectory(resources, bundle, prefix);
        } else {
            for (HeaderEntry path : paths) {
                String name = path.getName();
                if (name.equals(".") || name.equals("/")) {
                    // scan root
                    scanDirectory(resources, bundle, prefix);
                } else if (name.endsWith(".jar") || name.endsWith(".zip")) {
                    // scan embedded jar/zip
                    scanZip(resources, bundle, name);
                } else {
                    // assume it's a directory
                    scanDirectory(resources, bundle, addSlash(prefix) + name);
                }
            }
        }
    }
    
    private void scanDirectory(Collection<URL> resources, Bundle bundle, String basePath) {
        Enumeration e = bundle.findEntries(basePath, "*" + suffix, true);
        if (e != null) {
            while (e.hasMoreElements()) {
                resources.add((URL) e.nextElement());
            }
        }
    }
    
    private void scanZip(Collection<URL> resources, Bundle bundle, String zipName) {   
        URL zipEntry = bundle.getEntry(zipName);
        if (zipEntry == null) {
            return;
        }
        try {
            ZipInputStream in = new ZipInputStream(zipEntry.openStream());
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (prefixMatches(name) && suffixMatches(name)) {
                    /**
                     * XXX: The bundle.getResource() uses bundle class loader to find the resource.
                     * That means that the returned URL might actually come from another bundle
                     * that also has a resource with the same name.
                     * 
                     * Possible solution 1:
                     *  Build the URL to the right resource.
                     *   - Pros: Would not use bundle classloader
                     *   - Cons: The "bundle" url is not standardized so the implementation might be
                     *     very framework specific.  
                     * 
                     * Possible solution 2:
                     *   Use bundle.getResources() and find the right resource by comparing urls.
                     *   - Pros: 
                     *   - Cons: Uses bundle classloader to find the resources
                     *           Might need to understand the "bundle" url to compare the returned
                     *           urls.  
                     */
                    URL u = getRightResource(name);
                    resources.add(u);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean prefixMatches(String name) {
        if (prefix.length() == 0 || prefix.equals(".") || prefix.equals("/")) {
            return true;
        } else if (prefix.startsWith("/")) {
            return name.startsWith(prefix, 1);
        } else {
            return name.startsWith(prefix);
        }
    }
    
    private boolean suffixMatches(String name) {
        return (suffix.length() == 0) ? true : name.endsWith(suffix);
    }
        
    private URL getRightResource(String name) throws IOException {
        Enumeration e = bundle.getResources(name);
        URL firstResource = (URL) e.nextElement();
        if (e.hasMoreElements()) {
            // TODO: multiple resources found - must pick right one
        }
        return firstResource;
    }
       
    private static String addSlash(String name) {
        if (!name.endsWith("/")) {
            name = name + "/";
        }
        return name;
    }
}
