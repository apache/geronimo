/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.apache.geronimo.jasper.internal;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;

import org.apache.geronimo.jasper.TldProvider;
import org.apache.geronimo.jasper.TldRegistry;
import org.apache.geronimo.kernel.osgi.FrameworkUtils;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.configuration.OsgiMetaDataProvider;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder.ResourceFinderCallback;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TldRegistryImpl implements TldRegistry, BundleTrackerCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TldRegistryImpl.class);
    
    private Map<Bundle, Collection<TldProvider.TldEntry>> map = new ConcurrentHashMap<Bundle, Collection<TldProvider.TldEntry>>();
    
    private final BundleContext bundleContext;

    public TldRegistryImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    
    public Collection<TldProvider.TldEntry> getTls() {
        List<TldProvider.TldEntry> allTlds = new ArrayList<TldProvider.TldEntry>();
        for (Collection<TldProvider.TldEntry> tlds : map.values()) {
            allTlds.addAll(tlds);
        }
        return allTlds;
    }
    
    public Collection<TldProvider.TldEntry> getTlds(Bundle bundle) {
        Collection<TldProvider.TldEntry> tlds = map.get(bundle);
        return (tlds == null) ? Collections.<TldProvider.TldEntry>emptyList() : tlds;
    }

    private Set<Bundle> getDependentBundles(Bundle bundle) {
        Set<Bundle> dependentBundles = new HashSet<Bundle>();
        
        // add in bundles from dependency manager
        ServiceReference serviceReference = bundleContext.getServiceReference(OsgiMetaDataProvider.class.getName());
        if (serviceReference != null) {
            OsgiMetaDataProvider osgiMetaDataProvider = (OsgiMetaDataProvider) bundleContext.getService(serviceReference);
            try {
                dependentBundles.addAll(osgiMetaDataProvider.getFullDependentBundles(bundle.getBundleId()));
            } finally {
                bundleContext.ungetService(serviceReference);
            }
        }
        
        // add in wired bundles if WAB        
        String contextPath = (String) bundle.getHeaders().get("Web-ContextPath");
        if (contextPath != null) {
            Set<Bundle> wiredBundles = BundleUtils.getWiredBundles(bundle);
            dependentBundles.addAll(wiredBundles);
        }

        return dependentBundles;
    }
    
    public Collection<TldProvider.TldEntry> getDependentTlds(Bundle bundle) {
        List<TldProvider.TldEntry> allTlds = new ArrayList<TldProvider.TldEntry>();
        Set<Bundle> dependentBundles = getDependentBundles(bundle);
        for (Bundle dependentBundle : dependentBundles) {
            Collection<TldProvider.TldEntry> tlds = map.get(dependentBundle);
            if (tlds != null) {
                allTlds.addAll(tlds);
            }
        }
        return allTlds;
    }
    
    @Override
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        Collection<TldProvider.TldEntry> tlds = scanBundle(bundle);
        map.put(bundle, tlds);        
        return bundle;        
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        map.remove(bundle);        
    }

    private Collection<TldProvider.TldEntry> scanBundle(Bundle bundle) {
        ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(reference);
        try {
            BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/", ".tld");        
            TldResourceFinderCallback callback = new TldResourceFinderCallback();
            resourceFinder.find(callback);
            return callback.getTlds();
        } catch (Exception e) {
            LOGGER.warn("Error scanning bundle for JSP tag libraries", e);
            return Collections.emptyList();
        } finally {
            bundleContext.ungetService(reference);
        }
    }
    
    private static class TldResourceFinderCallback implements ResourceFinderCallback, TldProvider {

        private final List<TldProvider.TldEntry> tlds = new ArrayList<TldProvider.TldEntry>();

        private TldResourceFinderCallback() {
        }
        
        public Collection<TldProvider.TldEntry> getTlds() {
            return tlds;
        }
        
        public boolean foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception {
            LOGGER.debug("Found {} TLD in bundle {}", url, bundle);
            URL jarURL = null;
            /* 
             * Try to convert to jar: url if possible. Makes life easier for Japser. 
             * See GERONIMO-6295. 
             */
            URL convertedURL = FrameworkUtils.convertURL(url);
            if ("jar".equals(convertedURL.getProtocol())) {
                String urlString = convertedURL.toExternalForm();
                int pos = urlString.indexOf("!/");
                if (pos != -1) {
                    jarURL = new URL(urlString.substring(4 /* jar: */, pos));
                }
            }
            tlds.add(new TldProvider.TldEntry(bundle, url, jarURL));            
            return true;
        }

        public boolean foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
            URL jarURL = bundle.getEntry(jarName);
            URL url = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());
            LOGGER.debug("Found {} TLD in bundle {}", url, bundle);
            tlds.add(new TldProvider.TldEntry(bundle, url, jarURL));
            return false;
        }
        
    }

}
