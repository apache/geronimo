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

package org.apache.geronimo.system.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
@OsgiService
public class ConfigurationExtender implements SynchronousBundleListener, GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationExtender.class);

    private BundleContext bundleContext;

    private ConfigurationManager configurationManager;

    private DependencyManager dependencyManager;

    private Map<Long, Artifact> bundleIdArtifactMap = new ConcurrentHashMap<Long, Artifact>();

    private Set<Long> loadedBundleIds = Collections.synchronizedSet(new HashSet<Long>());

    private Map<Long, BundleListener> bundleIdListenerMap = new ConcurrentHashMap<Long, BundleListener>();

    public ConfigurationExtender(@ParamReference(name = "ConfigurationManager", namingType = "ConfigurationManager") ConfigurationManager configurationManager,
            @ParamReference(name = "DependencyManager") DependencyManager dependencyManager, @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.configurationManager = configurationManager;
        this.dependencyManager = dependencyManager;
        for (Bundle bundle : bundleContext.getBundles()) {
            //After the first start, all the bundles are cached, and some of them will be resolved by some rules by OSGi container.
            //And DependencyManager is not installed while those bundles are resolved, so we need to invoke the install method to make sure
            //that all the dependent bundles are resolved first, especially those car bundles.
            if (!loadedBundleIds.contains(bundle.getBundleId())) {
                recursiveLoadConfigurationData(bundle);
            }
        }
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        try {
            int eventType = event.getType();
            Bundle bundle = event.getBundle();
            if (eventType == BundleEvent.RESOLVED) {
                if (!loadedBundleIds.contains(bundle.getBundleId())) {
                    recursiveLoadConfigurationData(bundle);
                }
            } else if (eventType == BundleEvent.STOPPING) {
                stopConfiguration(bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadConfiguration(Bundle bundle) {
        PluginArtifactType pluginArtifact = dependencyManager.getCachedPluginMetadata(bundle);
        if (pluginArtifact == null) {
            if (BundleUtils.isResolved(bundle)) {
                loadedBundleIds.add(bundle.getBundleId());
            }
            return;
        }
        Set<Long> dependentBundleIds = new HashSet<Long>();
        for (DependencyType dependency : pluginArtifact.getDependency()) {
            Long dependentBundleId = dependencyManager.getBundle(dependency.toArtifact()).getBundleId();
            if (!loadedBundleIds.contains(dependentBundleId)) {
                dependentBundleIds.add(dependentBundleId);
            }
        }
        if (dependentBundleIds.size() > 0) {
            bundleIdListenerMap.put(bundle.getBundleId(), new BundleListener(bundle, dependentBundleIds));
            return;
        }
        _loadConfiguration(bundle);
        loadedBundleIds.add(bundle.getBundleId());

        boolean bundleStatusChanged;
        do {
            bundleStatusChanged = false;
            for (Iterator<Map.Entry<Long, BundleListener>> it = bundleIdListenerMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Long, BundleListener> entry = it.next();
                if (entry.getValue().bundleChanged(bundle)) {
                    bundleStatusChanged = true;
                    it.remove();
                }
            }
        } while (bundleStatusChanged);
    }

    private void _loadConfiguration(Bundle bundle) {
        loadedBundleIds.add(bundle.getBundleId());
        URL configSerURL = bundle.getEntry("META-INF/config.ser");
        if (configSerURL == null) {
            return;
        }
        InputStream in = null;
        try {
            in = configSerURL.openStream();
            //TODO there are additional consistency checks in RepositoryConfigurationStore that we should use.
            ConfigurationData data = ConfigurationUtil.readConfigurationData(in);
            data.setBundle(bundle);
            configurationManager.loadConfiguration(data);
            bundleIdArtifactMap.put(bundle.getBundleId(), data.getId());
        } catch (IOException e) {
            logger.error("Could not read the config.ser file from bundle " + bundle.getLocation(), e);
        } catch (ClassNotFoundException e) {
            logger.error("Could not load required classes from bundle " + bundle.getLocation(), e);
        } catch (NoSuchConfigException e) {
            logger.error("Could not load configuration from bundle " + bundle.getLocation(), e);
        } catch (LifecycleException e) {
            logger.error("Could not load configuration from bundle " + bundle.getLocation(), e);
        } finally {
            IOUtils.close(in);
        }
    }

    protected void stopConfiguration(Bundle bundle) {
        Artifact id = getArtifact(bundle);
        if (id == null) {
            return;
        }
        ServiceReference kernelReference = null;
        try {
            kernelReference = bundleContext.getServiceReference(Kernel.class.getName());
            if (kernelReference == null) {
                return;
            }
            Kernel kernel = (Kernel) bundleContext.getService(kernelReference);
            AbstractName name = Configuration.getConfigurationAbstractName(id);
            //TODO investigate how this is called and whether just stopping/unloading the configuration gbean will
            //leave the configuration model in a consistent state.  We might need a shutdown flag set elsewhere to avoid
            //overwriting the load attribute in config.xml. This code mimics the shutdown hook in KernelConfigurationManager
            //see https://issues.apache.org/jira/browse/GERONIMO-4909
            try {
                kernel.stopGBean(name);
            } catch (Exception e) {
                //ignore
            }
            try {
                kernel.unloadGBean(name);
            } catch (Exception e) {
            }
            //TODO this code is more symmetrical with start, but currently sets the load attribute to false in config.xml,
            //which prevents restarting the server.
            //ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
            //manager.unloadConfiguration(id);
        } catch (InvalidConfigException e) {
            //
        } finally {
            if (kernelReference != null) {
                try {
                    bundleContext.ungetService(kernelReference);
                } catch (Exception e) {
                }
            }
        }
    }

    private Artifact getArtifact(Bundle bundle) {
        return bundleIdArtifactMap.get(bundle.getBundleId());
    }

    @Override
    public void doStart() throws Exception {
        bundleContext.addBundleListener(this);
    }

    @Override
    public void doStop() throws Exception {
        bundleContext.removeBundleListener(this);
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    private void recursiveLoadConfigurationData(Bundle bundle) {
        PluginArtifactType pluginArtifact = dependencyManager.getCachedPluginMetadata(bundle);
        if (pluginArtifact != null) {
            for (DependencyType dependency : pluginArtifact.getDependency()) {
                Bundle dependentBundle = dependencyManager.getBundle(dependency.toArtifact());
                if (dependentBundle == null || loadedBundleIds.contains(dependentBundle.getBundleId())) {
                    continue;
                }
                recursiveLoadConfigurationData(dependentBundle);
                loadConfiguration(dependentBundle);
            }
        }
        loadConfiguration(bundle);
    }

    private class BundleListener {

        private Bundle hostBundle;

        private Set<Long> dependentBundleIds = new HashSet<Long>();

        public BundleListener(Bundle hostBundle, Set<Long> dependentBundleIds) {
            this.hostBundle = hostBundle;
            this.dependentBundleIds = dependentBundleIds;
        }

        public boolean bundleChanged(Bundle bundle) {
            Long dependentBundleId = bundle.getBundleId();
            if (!dependentBundleIds.contains(dependentBundleId)) {
                return false;
            }
            dependentBundleIds.remove(dependentBundleId);
            if (dependentBundleIds.size() == 0) {
                _loadConfiguration(hostBundle);
                return true;
            }
            return false;
        }
    }
}
