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

package org.apache.geronimo.openjpa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.openjpa.enhance.PCRegistry;
import org.osgi.framework.Bundle; 
import org.osgi.framework.BundleReference; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor configuration lifecycle events. Whenever a configuration is stopped, inform OpenJPA that the ClassLoader is no longer needed.
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationMonitorGBean implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationMonitorGBean.class);

    private final Kernel kernel;
    private final LifecycleListener listener;
    private HashMap<AbstractName, Bundle> bundleMap = new HashMap<AbstractName, Bundle>();

    public ConfigurationMonitorGBean(Kernel kernel) {
        this.kernel = kernel;
        this.listener = createLifecycleListener();
    }

    /**
     * Create a LifecycleListenr that will be informed of lifecycle events.
     * We only care about running (the configuration ClassLoader cannot be retrieved when the Configuration is stopping)
     * and stopped.
     */
    private LifecycleListener createLifecycleListener() {
        return new LifecycleListener() {
            public void loaded(AbstractName abstractName) {
            }
            public void starting(AbstractName abstractName) {
            }
            public void running(AbstractName abstractName) {
                configurationRunning(abstractName);
            }
            public void stopping(AbstractName abstractName) {
            }
            public void stopped(AbstractName abstractName) {
                configurationStopped(abstractName);
            }
            public void failed(AbstractName abstractName) {
            }
            public void unloaded(AbstractName abstractName) {
            }

        };
    }

    /**
     * Cache the ClassLoader for a newly started Configuration.
     */
    private void configurationRunning(AbstractName name) {
        try {
            Configuration config = (Configuration)kernel.getGBean(name);
            bundleMap.put(name, config.getBundle());
        } catch (GBeanNotFoundException gnfe) {
            log.warn("Could not retrieve GBean for artifact: " + name.toString(), gnfe);
        }
    }

    /**
     * Notify OpenJPA that the ClassLoader will no longer be used. This allows OpenJPA to free up
     * HARD references that would otherwise prevent Geronimo ClassLoaders from being GCed.
     */
    private void configurationStopped(AbstractName name) {
        Bundle bundle = bundleMap.remove(name);
        if (bundle == null) {
            log.debug("Could not locate Bundle for artifact: " + name.toString());
            return; 
        }
        
        // iterate over the registry types looking for one that is loaded from this bundle 
        for (Class<?> clz : PCRegistry.getRegisteredTypes()) {
            ClassLoader loader = clz.getClassLoader(); 
            // if we find a class that is loaded from this bundle, then deregister all classes associated 
            // with that bundle.  Unfortunately, PCRegistry doesn't have a deRegister() capability for a 
            // single class, we have to do it by class loader.  Once we get a hit, we can assume we're finished. 
            if (loader != null && loader instanceof BundleReference && ((BundleReference)loader).getBundle() == bundle) {
                PCRegistry.deRegister(loader);
                return; 
            }
        }
    }

    /**
     * This GBean is being started. Register our listener with the Lifecycle monitor.
     */
    public void doStart() {
        AbstractNameQuery configurationQuery = new AbstractNameQuery(Configuration.class.getName());
        kernel.getLifecycleMonitor().addLifecycleListener(listener, configurationQuery);
     }

    /**
     * This GBean is being stopped. Remove the LifecycleListener.
     */
    public void doStop() {
        kernel.getLifecycleMonitor().removeLifecycleListener(listener);
    }

    public void doFail() {
        doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ConfigurationMonitorGBean.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.setConstructor(new String[] {
                "kernel",
        });
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
