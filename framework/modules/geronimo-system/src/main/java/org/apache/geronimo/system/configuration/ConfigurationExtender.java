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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.util.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@Component
public class ConfigurationExtender {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationExtender.class);

    private final Map<Long, Configuration> configurationMap = new ConcurrentHashMap<Long, Configuration>();

    private BundleTracker bt;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY)
    private ManageableAttributeStore manageableAttributeStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private Kernel kernel;

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public void unsetKernel(Kernel kernel) {
        if (kernel == this.kernel) {
            this.kernel = null;
        }
    }

    public void setManageableAttributeStore(ManageableAttributeStore manageableAttributeStore) {
        this.manageableAttributeStore = manageableAttributeStore;
    }

    public void unsetManageableAttributeStore(ManageableAttributeStore manageableAttributeStore) {
        if (manageableAttributeStore == this.manageableAttributeStore) {
            this.manageableAttributeStore = null;
        }
    }

    @Activate
    public void start(BundleContext bundleContext) {
        bt = new BundleTracker(bundleContext, Bundle.RESOLVED | Bundle.ACTIVE, new ConfigurationBundleTrackerCustomizer());
        bt.open();

    }

    @Deactivate
    public void stop() {
        bt.close();
        bt = null;
    }

    private class ConfigurationBundleTrackerCustomizer implements BundleTrackerCustomizer {
        @Override
        public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
            if (bundle.getState() == Bundle.RESOLVED) {
                return loadConfiguration(bundle);
            } else if (bundle.getState() == Bundle.ACTIVE) {
                return startConfiguration(bundle);
            }
            return null;
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
            if (bundleEvent.getType() == BundleEvent.STOPPED) {
                stopConfiguration(bundle, (Configuration)o);
            } else if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
                unloadConfiguration(bundle, (Configuration)o);
            }
        }
    }


//    protected Configuration loadConfiguration(Bundle bundle) {
//        PluginArtifactType pluginArtifact = dependencyManager.getCachedPluginMetadata(bundle);
//        if (pluginArtifact == null) {
//            if (BundleUtils.isResolved(bundle)) {
//                loadedBundleIds.add(bundle.getBundleId());
//            }
//            return;
//        }
//        Set<Long> dependentBundleIds = new HashSet<Long>();
//        for (DependencyType dependency : pluginArtifact.getDependency()) {
//            Long dependentBundleId = dependencyManager.getBundle(dependency.toArtifact()).getBundleId();
//            if (!loadedBundleIds.contains(dependentBundleId)) {
//                dependentBundleIds.add(dependentBundleId);
//            }
//        }
//        if (dependentBundleIds.size() > 0) {
//            bundleIdListenerMap.put(bundle.getBundleId(), new BundleListener(bundle, dependentBundleIds));
//            return;
//        }
//        _loadConfiguration(bundle);
//        loadedBundleIds.add(bundle.getBundleId());
//
//        boolean bundleStatusChanged;
//        do {
//            bundleStatusChanged = false;
//            for (Iterator<Map.Entry<Long, BundleListener>> it = bundleIdListenerMap.entrySet().iterator(); it.hasNext();) {
//                Map.Entry<Long, BundleListener> entry = it.next();
//                if (entry.getValue().bundleChanged(bundle)) {
//                    bundleStatusChanged = true;
//                    it.remove();
//                }
//            }
//        } while (bundleStatusChanged);
//    }

    private Configuration loadConfiguration(Bundle bundle) {
//        loadedBundleIds.add(bundle.getBundleId());
        URL configSerURL = bundle.getEntry("META-INF/config.ser");
        if (configSerURL == null) {
            return null;
        }
        InputStream in = null;
        try {
            in = configSerURL.openStream();
            //TODO there are additional consistency checks in RepositoryConfigurationStore that we should use.
            ConfigurationData data = ConfigurationUtil.readConfigurationData(in);
            data.setBundle(bundle);
            Configuration configuration = new Configuration(data, manageableAttributeStore);
            configurationMap.put(bundle.getBundleId(), configuration);
            for (GBeanData gBeanData: configuration.getGBeans().values()) {
                kernel.loadGBean(gBeanData, configuration.getBundle().getBundleContext());
            }
            return configuration;

//            configurationManager.loadConfiguration(data);
//            bundleIdArtifactMap.put(bundle.getBundleId(), data.getId());
        } catch (IOException e) {
            logger.error("Could not read the config.ser file from bundle " + bundle.getLocation(), e);
        } catch (ClassNotFoundException e) {
            logger.error("Could not load required classes from bundle " + bundle.getLocation(), e);
        } catch (InvalidConfigException e) {
            logger.error("Could not load Configuration from bundle " + bundle.getLocation(), e);
        } catch (GBeanAlreadyExistsException e) {
            logger.error("Duplicate gbean in bundle " + bundle.getLocation(), e);
        } finally {
            IOUtils.close(in);
        }
        return null;
    }

    private Configuration startConfiguration(Bundle bundle) {
        Configuration configuration = configurationMap.get(bundle.getBundleId());
        if (configuration != null) {
            try {
                ConfigurationUtil.startConfigurationGBeans(configuration.getAbstractName(), configuration, kernel);
            } catch (InvalidConfigException e) {
                logger.error("Could not start Configuration from bundle " + bundle.getLocation(), e);
            }
        }

        return configuration;
    }


    protected void stopConfiguration(Bundle bundle, Configuration configuration) {
        if (configuration != null) {
            for (AbstractName abstractName: configuration.getGBeans().keySet()) {
                try {
                    kernel.stopGBean(abstractName);
                } catch (GBeanNotFoundException e) {
                    logger.error("Could not stop gbean " + abstractName + " from bundle " + bundle.getLocation(), e);
                }
            }
        }
//        Artifact id = getArtifact(bundle);
//        if (id == null) {
//            return;
//        }
//        ServiceReference kernelReference = null;
//        try {
//            kernelReference = bundleContext.getServiceReference(Kernel.class.getName());
//            if (kernelReference == null) {
//                return;
//            }
//            Kernel kernel = (Kernel) bundleContext.getService(kernelReference);
//            AbstractName name = Configuration.getConfigurationAbstractName(id);
//            //TODO investigate how this is called and whether just stopping/unloading the configuration gbean will
//            //leave the configuration model in a consistent state.  We might need a shutdown flag set elsewhere to avoid
//            //overwriting the load attribute in config.xml. This code mimics the shutdown hook in KernelConfigurationManager
//            //see https://issues.apache.org/jira/browse/GERONIMO-4909
//            try {
//                kernel.stopGBean(name);
//            } catch (Exception e) {
//                //ignore
//            }
//            try {
//                kernel.unloadGBean(name);
//            } catch (Exception e) {
//            }
//            //TODO this code is more symmetrical with start, but currently sets the load attribute to false in config.xml,
//            //which prevents restarting the server.
//            //ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
//            //manager.unloadConfiguration(id);
//        } catch (InvalidConfigException e) {
//            //
//        } finally {
//            if (kernelReference != null) {
//                try {
//                    bundleContext.ungetService(kernelReference);
//                } catch (Exception e) {
//                }
//            }
//        }
    }

    private void unloadConfiguration(Bundle bundle, Configuration configuration) {
        if (configuration != null) {
            for (AbstractName abstractName: configuration.getGBeans().keySet()) {
                try {
                    kernel.unloadGBean(abstractName);
                } catch (GBeanNotFoundException e) {
                    logger.error("Could not unload gbean " + abstractName + " from bundle " + bundle.getLocation(), e);
                }
            }

        }
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
//                _loadConfiguration(hostBundle);
                return true;
            }
            return false;
        }
    }

}
