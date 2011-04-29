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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.myfaces.FacesConfigDigester;
import org.apache.geronimo.myfaces.config.resource.osgi.api.ConfigRegistry;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.myfaces.config.element.FacesConfig;
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

    public void addBundle(Bundle bundle) {
        findFacesConfigs(bundle);
        findFaceletsConfigResources(bundle);
    }

    protected void findFaceletsConfigResources(Bundle bundle) {
        Enumeration<URL> metaInfEn = bundle.findEntries("META-INF/", "*.taglib.xml", false);
        if (metaInfEn != null) {
            List<URL> faceletsConfigResources = new ArrayList<URL>();
            while (metaInfEn.hasMoreElements()) {
                faceletsConfigResources.add(metaInfEn.nextElement());
            }
            bundleIdFaceletsConfigResourcesMap.put(bundle.getBundleId(), faceletsConfigResources);
        }
    }

    protected void findFacesConfigs(Bundle bundle) {
        log(LogService.LOG_DEBUG, "examining bundle for META-INF/faces-config.xml " + bundle.getSymbolicName());
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
        }
        if (facesConfigURLs != null) {
            bundleIdFacesConfigURLsMap.put(bundle.getBundleId(), facesConfigURLs);
        }
    }

    public void removeBundle(Bundle bundle, Object object) {
        Long removeBundleId = bundle.getBundleId();
        bundleIdFacesConfigsMap.remove(removeBundleId);
        bundleIdFacesConfigURLsMap.remove(removeBundleId);
        bundleIdFaceletsConfigResourcesMap.remove(removeBundleId);
    }

    public List<FacesConfig> getDependentFacesConfigs(Long bundleId) {
        BundleContext bundleContext = activator.getBundleContext();
        ServiceReference serviceReference = null;
        try {
            serviceReference = bundleContext.getServiceReference(DependencyManager.class.getName());
            if (serviceReference == null) {
                return Collections.<FacesConfig> emptyList();
            }
            DependencyManager dependencyManager = (DependencyManager) bundleContext.getService(serviceReference);
            List<Bundle> dependentBundles = dependencyManager.getFullDependentBundles(bundleId);
            List<FacesConfig> dependentFacesConfigs = new ArrayList<FacesConfig>();
            for (Bundle dependentBundle : dependentBundles) {
                List<FacesConfig> facesConfigs = bundleIdFacesConfigsMap.get(dependentBundle.getBundleId());
                if (facesConfigs != null) {
                    dependentFacesConfigs.addAll(facesConfigs);
                }
            }
            return dependentFacesConfigs;
        } finally {
            if (serviceReference != null) {
                bundleContext.ungetService(serviceReference);
            }
        }
    }

    @Override
    public List<URL> getDependentFaceletsConfigResources(Long bundleId) {
        BundleContext bundleContext = activator.getBundleContext();
        ServiceReference serviceReference = null;
        try {
            serviceReference = bundleContext.getServiceReference(DependencyManager.class.getName());
            if (serviceReference == null) {
                return Collections.<URL> emptyList();
            }
            DependencyManager dependencyManager = (DependencyManager) bundleContext.getService(serviceReference);
            List<Bundle> dependentBundles = dependencyManager.getFullDependentBundles(bundleId);
            List<URL> faceletsConfigResources = new ArrayList<URL>();
            for (Bundle dependentBundle : dependentBundles) {
                List<URL> faceletConfigResources = bundleIdFaceletsConfigResourcesMap.get(dependentBundle.getBundleId());
                if (faceletConfigResources != null) {
                    faceletsConfigResources.addAll(faceletConfigResources);
                }
            }
            return faceletsConfigResources;
        } finally {
            if (serviceReference != null) {
                bundleContext.ungetService(serviceReference);
            }
        }
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
