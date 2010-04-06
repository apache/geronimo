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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    public static final DiscoveryFilter FULL_DISCOVERY_FILTER = new DummyDiscoveryFilter();
    private final Bundle bundle;
    private final PackageAdmin packageAdmin;
    private final String prefix;
    private final String suffix;
    private DiscoveryFilter discoveryFilter;

    public BundleResourceFinder(PackageAdmin packageAdmin, Bundle bundle, String prefix, String suffix) {
        this(packageAdmin, bundle, prefix, suffix, FULL_DISCOVERY_FILTER);
    }

    public BundleResourceFinder(PackageAdmin packageAdmin, Bundle bundle, String prefix, String suffix, DiscoveryFilter discoveryFilter) {
        this.packageAdmin = packageAdmin;
        this.bundle = bundle;
        this.prefix = prefix.trim();
        this.suffix = suffix.trim();
        this.discoveryFilter = discoveryFilter;
    }

    public void find(ResourceFinderCallback callback) throws Exception {
        if (discoveryFilter.rangeDiscoveryRequired(DiscoveryRange.BUNDLE_CLASSPATH)) {
            scanBundleClassPath(callback, bundle);
        }
        if (packageAdmin != null && discoveryFilter.rangeDiscoveryRequired(DiscoveryRange.FRAGMENT_BUNDLES)) {
            Bundle[] fragments = packageAdmin.getFragments(bundle);
            if (fragments != null) {
                for (Bundle fragment : fragments) {
                    scanBundleClassPath(callback, fragment);
                }
            }
        }
    }

    public Set<URL> find() {
        Set<URL> resources = new LinkedHashSet<URL>();
        try {
            find(new DefaultResourceFinderCallback(resources));
        } catch (Exception e) {
            // this should not happen
            throw new RuntimeException("Resource discovery failed", e);
        }
        return resources;
    }

    private void scanBundleClassPath(ResourceFinderCallback callback, Bundle bundle) throws Exception {
        BundleDescription desc = new BundleDescription(bundle.getHeaders());
        List<HeaderEntry> paths = desc.getBundleClassPath();
        if (paths.isEmpty()) {
            scanDirectory(callback, bundle, prefix);
        } else {
            for (HeaderEntry path : paths) {
                String name = path.getName();
                if (name.equals(".") || name.equals("/")) {
                    // scan root
                    scanDirectory(callback, bundle, prefix);
                } else if (name.endsWith(".jar") || name.endsWith(".zip")) {
                    // scan embedded jar/zip
                    scanZip(callback, bundle, name);
                } else {
                    // assume it's a directory
                    scanDirectory(callback, bundle, addSlash(prefix) + name);
                }
            }
        }
    }

    private void scanDirectory(ResourceFinderCallback callback, Bundle bundle, String basePath) throws Exception {
        if (!discoveryFilter.directoryDiscoveryRequired(basePath)) {
            return;
        }
        Enumeration e = bundle.findEntries(basePath, "*" + suffix, true);
        if (e != null) {
            while (e.hasMoreElements()) {
                callback.foundInDirectory(bundle, basePath, (URL) e.nextElement());
            }
        }
    }

    private void scanZip(ResourceFinderCallback callback, Bundle bundle, String zipName) throws Exception {
        if (!discoveryFilter.zipFileDiscoveryRequired(zipName)) {
            return;
        }
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
                    callback.foundInJar(bundle, zipName, entry, new ZipEntryInputStream(in));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ZipEntryInputStream extends FilterInputStream {
        public ZipEntryInputStream(ZipInputStream in) {
            super(in);
        }
        public void close() throws IOException {
            // not really necessary
            // ((ZipInputStream) in).closeEntry();
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

    private static String addSlash(String name) {
        if (!name.endsWith("/")) {
            name = name + "/";
        }
        return name;
    }

    public interface ResourceFinderCallback {
        void foundInDirectory(Bundle bundle, String baseDir, URL url) throws Exception;

        void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception;
    }

    public static class DefaultResourceFinderCallback implements ResourceFinderCallback {

        private Set<URL> resources;

        public DefaultResourceFinderCallback() {
            this(new LinkedHashSet<URL>());
        }

        public DefaultResourceFinderCallback(Set<URL> resources) {
            this.resources = resources;
        }

        public Set<URL> getResources() {
            return resources;
        }

        public void foundInDirectory(Bundle bundle, String baseDir, URL url) throws Exception {
            resources.add(url);
        }

        public void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
            URL jarURL = bundle.getEntry(jarName);
            URL url = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());
            resources.add(url);
        }

    }

    public static class DummyDiscoveryFilter implements DiscoveryFilter {

        @Override
        public boolean directoryDiscoveryRequired(String url) {
            return true;
        }

        @Override
        public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
            return true;
        }

        @Override
        public boolean zipFileDiscoveryRequired(String url) {
            return true;
        }

    }
}
