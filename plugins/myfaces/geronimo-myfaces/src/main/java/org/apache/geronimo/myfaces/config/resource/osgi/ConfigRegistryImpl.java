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

package org.apache.geronimo.myfaces.config.resource.osgi;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.myfaces.FacesConfigDigester;
import org.apache.geronimo.myfaces.config.resource.osgi.api.ConfigRegistry;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.configuration.OsgiMetaDataProvider;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * @version $Rev$ $Date$
 */
public class ConfigRegistryImpl implements ConfigRegistry {

    private final Activator activator;

    private Map<Long, List<FacesConfig>> bundleIdFacesConfigsMap = new ConcurrentHashMap<Long, List<FacesConfig>>();

    private Map<Long, List<URL>> bundleIdFacesConfigURLsMap = new ConcurrentHashMap<Long, List<URL>>();

    private Map<Long, List<URL>> bundleIdFaceletsConfigResourcesMap = new ConcurrentHashMap<Long, List<URL>>();

    private FacesConfigDigester facesConfigDigester = new FacesConfigDigester();

    public ConfigRegistryImpl(Activator activator) {
        this.activator = activator;
    }

    public boolean addBundle(Bundle bundle) {
        boolean facesConfigsFound = findFacesConfigs(bundle);
        boolean faceletsConfigResourcesFound = findFaceletsConfigResources(bundle);
        return facesConfigsFound || faceletsConfigResourcesFound;
    }

    protected boolean findFaceletsConfigResources(Bundle bundle) {
        Enumeration<URL> metaInfEn = bundle.findEntries("META-INF/", "*.taglib.xml", false);
        if (metaInfEn == null) {
            return false;
        }
        List<URL> faceletsConfigResources = new ArrayList<URL>();
        while (metaInfEn.hasMoreElements()) {
            faceletsConfigResources.add(metaInfEn.nextElement());
        }
        bundleIdFaceletsConfigResourcesMap.put(bundle.getBundleId(), faceletsConfigResources);
        return true;
    }

    protected boolean findFacesConfigs(Bundle bundle) {
        log(LogService.LOG_DEBUG, "examining bundle for META-INF/faces-config.xml " + bundle.getSymbolicName());
        boolean facesConfigsFound = false;
        URL url = bundle.getEntry("META-INF/faces-config.xml");
        List<FacesConfig> facesConfigs = null;
        List<URL> facesConfigURLs = null;
        if (url != null) {
            facesConfigs = new ArrayList<FacesConfig>();
            facesConfigURLs = new ArrayList<URL>();
            facesConfigs.add(parseFacesConfig(bundle, url));
            facesConfigURLs.add(url);
        }
        Enumeration<URL> metaInfEn = bundle.findEntries("META-INF/", "*.faces-config.xml", false);
        if (metaInfEn != null) {
            if (facesConfigs == null) {
                facesConfigs = new ArrayList<FacesConfig>();
            }
            if (facesConfigURLs == null) {
                facesConfigURLs = new ArrayList<URL>();
            }
            while (metaInfEn.hasMoreElements()) {
                URL currURL = metaInfEn.nextElement();
                facesConfigURLs.add(currURL);
                facesConfigs.add(parseFacesConfig(bundle, currURL));
            }
        }
        if (facesConfigs != null) {
            bundleIdFacesConfigsMap.put(bundle.getBundleId(), facesConfigs);
            facesConfigsFound = true;
        }
        if (facesConfigURLs != null) {
            bundleIdFacesConfigURLsMap.put(bundle.getBundleId(), facesConfigURLs);
            facesConfigsFound = true;
        }
        return facesConfigsFound;
    }

    public void removeBundle(Bundle bundle, Object object) {
        Long removeBundleId = bundle.getBundleId();
        bundleIdFacesConfigsMap.remove(removeBundleId);
        bundleIdFacesConfigURLsMap.remove(removeBundleId);
        bundleIdFaceletsConfigResourcesMap.remove(removeBundleId);
    }

    private Set<Bundle> getDependentBundles(Long bundleId) {
        BundleContext bundleContext = activator.getBundleContext();
        
        Set<Bundle> dependentBundles = new HashSet<Bundle>();
        
        // add in bundles from dependency manager
        ServiceReference serviceReference = bundleContext.getServiceReference(OsgiMetaDataProvider.class.getName());
        if (serviceReference != null) {
            OsgiMetaDataProvider dependencyManager = (DependencyManager) bundleContext.getService(serviceReference);
            try {
                dependentBundles.addAll(dependencyManager.getFullDependentBundles(bundleId));
            } finally {
                bundleContext.ungetService(serviceReference);
            }
        }
        
        // add in wired bundles if WAB        
        Bundle bundle = bundleContext.getBundle(bundleId);
        String contextPath = (String) bundle.getHeaders().get("Web-ContextPath");
        if (contextPath != null) {
            Set<Bundle> wiredBundles = BundleUtils.getWiredBundles(bundle);
            dependentBundles.addAll(wiredBundles);
        }

        return dependentBundles;
    }
    
    public List<FacesConfig> getDependentFacesConfigs(Long bundleId) {
        Set<Bundle> dependentBundles = getDependentBundles(bundleId);
        List<FacesConfig> dependentFacesConfigs = new ArrayList<FacesConfig>();
        for (Bundle dependentBundle : dependentBundles) {
            List<FacesConfig> facesConfigs = bundleIdFacesConfigsMap.get(dependentBundle.getBundleId());
            if (facesConfigs != null) {
                dependentFacesConfigs.addAll(facesConfigs);
            }
        }
        return dependentFacesConfigs;
    }

    @Override
    public List<URL> getDependentFaceletsConfigResources(Long bundleId) {
        Set<Bundle> dependentBundles = getDependentBundles(bundleId);
        List<URL> faceletsConfigResources = new ArrayList<URL>();
        for (Bundle dependentBundle : dependentBundles) {
            List<URL> faceletConfigResources = bundleIdFaceletsConfigResourcesMap.get(dependentBundle.getBundleId());
            if (faceletConfigResources != null) {
                faceletsConfigResources.addAll(faceletConfigResources);
            }
        }
        return faceletsConfigResources;
    }
    
    @Override
    public Set<Long> getFacesConfigsBundleIds() {
        return bundleIdFacesConfigsMap.keySet();
    }

    @Override
    public List<FacesConfig> getFacesConfigs(Long bundleId) {
        return bundleIdFacesConfigsMap.get(bundleId);
    }

    @Override
    public Set<Long> getFaceletsConfigResourcesBundleIds() {
        return bundleIdFaceletsConfigResourcesMap.keySet();
    }

    @Override
    public List<URL> getFaceletsConfigResources(Long bundleId) {
        return bundleIdFaceletsConfigResourcesMap.get(bundleId);
    }

    @Override
    public List<URL> getFacesConfigURLs(Long bundleId) {
        return bundleIdFacesConfigURLsMap.get(bundleId);
    }

    private void log(int level, String message) {
        activator.log(level, message);
    }

    private void log(int level, String message, Throwable th) {
        activator.log(level, message, th);
    }

    private FacesConfig parseFacesConfig(Bundle bundle, URL url) {
        log(LogService.LOG_DEBUG, "found META-INF/faces-config.xml");
        InputStream in = null;
        try {
            in = url.openStream();
            return facesConfigDigester.getFacesConfig(in, url.toExternalForm());
        } catch (Exception e) {
            log(LogService.LOG_WARNING, "fail to parse " + url + " in the bundle " + bundle.getSymbolicName() + " due to " + e.getMessage(), e);
            return null;
        } finally {
            IOUtils.close(in);
        }
    }
}
