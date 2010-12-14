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
import java.util.HashSet;
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
 * @version $Rev:$ $Date:$
 */
public class ConfigRegistryImpl implements ConfigRegistry {
    private final Activator activator;

    private final Set<URL> urls = new HashSet<URL>();

    private Map<Long, FacesConfig> bundleIdFacesConfigMap = new ConcurrentHashMap<Long, FacesConfig>();

    private FacesConfigDigester facesConfigDigester = new FacesConfigDigester();

    public ConfigRegistryImpl(Activator activator) {
        this.activator = activator;
    }

    public Object addBundle(Bundle bundle) {
        log(LogService.LOG_DEBUG, "examining bundle for META-INF/faces-config.xml " + bundle.getSymbolicName());
        URL url = bundle.getEntry("META-INF/faces-config.xml");
        if (url != null) {
            log(LogService.LOG_DEBUG, "found META-INF/faces-config.xml");
            urls.add(url);
            InputStream in = null;
            try {
                in = url.openStream();
                bundleIdFacesConfigMap.put(bundle.getBundleId(), facesConfigDigester.getFacesConfig(in, url.toExternalForm()));
            } catch (Exception e) {
            } finally {
                IOUtils.close(in);
            }
        }
        return url;
    }

    public void removeBundle(Bundle bundle, Object object) {
        log(LogService.LOG_DEBUG, "unregistering bundle for META-INF/faces-config.xml " + bundle.getSymbolicName() + " url: " + object);
        if (object != null) {
            urls.remove(object);
        }
        bundleIdFacesConfigMap.remove(bundle.getBundleId());
    }

    @Override
    public Set<URL> getRegisteredConfigUrls() {
        return Collections.unmodifiableSet(urls);
    }

    public Set<URL> getDependentConfigUrls(Bundle bundle) {
        return null;
    }

    public List<FacesConfig> getDependentFacesConfigs(Bundle bundle) {
        BundleContext bundleContext = activator.getBundleContext();
        ServiceReference serviceReference = null;
        try {
            serviceReference = bundleContext.getServiceReference(DependencyManager.class.getName());
            if (serviceReference == null) {
                return Collections.<FacesConfig> emptyList();
            }
            DependencyManager dependencyManager = (DependencyManager) bundleContext.getService(serviceReference);
            List<Bundle> dependentBundles = dependencyManager.getDependentBundles(bundle);
            List<FacesConfig> dependentFacesConfigs = new ArrayList<FacesConfig>();
            for (Bundle dependentBundle : dependentBundles) {
                FacesConfig facesConfig = bundleIdFacesConfigMap.get(dependentBundle.getBundleId());
                if (facesConfig != null) {
                    dependentFacesConfigs.add(facesConfig);
                }
            }
            return dependentFacesConfigs;
        } finally {
            if (serviceReference != null) {
                bundleContext.ungetService(serviceReference);
            }
        }
    }


    private void log(int level, String message) {
        activator.log(level, message);
    }

    private void log(int level, String message, Throwable th) {
        activator.log(level, message, th);
    }


}
