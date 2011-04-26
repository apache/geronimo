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

package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.geronimo.kernel.Kernel;
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

    private final Map<Long, GetConfiguration> configurationMap = new ConcurrentHashMap<Long, GetConfiguration>();

    private final Executor executor = Executors.newCachedThreadPool();


    private BundleTracker bt;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
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
            if (bundleEvent != null) {
                if (bundleEvent.getType() == BundleEvent.STOPPED) {
                    return null;
                }
            }
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
                stopConfiguration(bundle, (GetConfiguration)o);
            } else if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
                unloadConfiguration(bundle, (GetConfiguration)o);
            }
        }
    }

    private LoadConfiguration loadConfiguration(Bundle bundle) {
        URL configSerURL = bundle.getEntry("META-INF/config.ser");
        if (configSerURL == null) {
            return null;
        }
        LoadConfiguration loader = new LoadConfiguration(bundle, configSerURL);
        executor.execute(loader);
        configurationMap.put(bundle.getBundleId(), loader);
        return loader;
    }

    private StartConfiguration startConfiguration(Bundle bundle) {
        GetConfiguration loader = configurationMap.get(bundle.getBundleId());
        if (loader != null) {
            StartConfiguration startConfiguration = new StartConfiguration(loader);
            executor.execute(startConfiguration);
            configurationMap.put(bundle.getBundleId(), startConfiguration);
            return startConfiguration;
        }
        return null;
    }


    protected void stopConfiguration(Bundle bundle, GetConfiguration configuration) {
        if (configuration != null) {
            GetConfiguration start = configurationMap.get(bundle.getBundleId());
            StopConfiguration stopConfiguration = new StopConfiguration(start);
            executor.execute(stopConfiguration);
            configurationMap.put(bundle.getBundleId(), stopConfiguration);
        }
    }

    private void unloadConfiguration(Bundle bundle, GetConfiguration configuration) {
        if (configuration != null) {
            GetConfiguration start = configurationMap.get(bundle.getBundleId());
            UnloadConfiguration unloadConfiguration = new UnloadConfiguration(start);
            executor.execute(unloadConfiguration);
            configurationMap.remove(bundle.getBundleId());
        }
    }

    private interface GetConfiguration {

        Configuration getConfiguration();

        Bundle getBundle();

    }

    private class LoadConfiguration implements Runnable, GetConfiguration {

        private final URL configSerURL;
        private final Bundle bundle;
        private Configuration configuration;
        private CountDownLatch latch = new CountDownLatch(1);

        private LoadConfiguration(Bundle bundle, URL configSerURL) {
            this.bundle = bundle;
            this.configSerURL = configSerURL;
        }

        @Override
        public void run() {
            InputStream in = null;
            try {
                in = configSerURL.openStream();
                //TODO there are additional consistency checks in RepositoryConfigurationStore that we should use.
                ConfigurationData data = ConfigurationUtil.readConfigurationData(in);
                data.setBundle(bundle);
                Configuration configuration = new Configuration(data, manageableAttributeStore);
                ConfigurationUtil.loadConfigurationGBeans(configuration, kernel);
                this.configuration = configuration;
            } catch (IOException e) {
                logger.error("Could not read the config.ser file from bundle " + bundle.getLocation(), e);
            } catch (ClassNotFoundException e) {
                logger.error("Could not load required classes from bundle " + bundle.getLocation(), e);
            } catch (InvalidConfigException e) {
                logger.error("Could not load Configuration from bundle " + bundle.getLocation(), e);
            } finally {
                IOUtils.close(in);
            }
            latch.countDown();

        }

        @Override
        public Configuration getConfiguration() {
            try {
                latch.await();
            } catch (InterruptedException e) {

            }
            return configuration;
        }

        @Override
        public Bundle getBundle() {
            return bundle;
        }

    }

    private class StartConfiguration implements Runnable, GetConfiguration {

        private final GetConfiguration loader;
        private CountDownLatch latch = new CountDownLatch(1);

        private StartConfiguration(GetConfiguration loader) {
            this.loader = loader;
        }

        @Override
        public void run() {
            Configuration configuration = loader.getConfiguration();
            if (configuration != null) {
                try {
                    ConfigurationUtil.startConfigurationGBeans(configuration, kernel);
                } catch (InvalidConfigException e) {
                    logger.error("Could not start Configuration from bundle " + loader.getBundle().getLocation(), e);
                }
            }
            latch.countDown();
        }
        @Override
        public Configuration getConfiguration() {
            try {
                latch.await();
            } catch (InterruptedException e) {

            }
            return loader.getConfiguration();
        }

        @Override
        public Bundle getBundle() {
            return loader.getBundle();
        }

    }

    private class StopConfiguration implements Runnable, GetConfiguration {

        private final GetConfiguration loader;
        private CountDownLatch latch = new CountDownLatch(1);

        private StopConfiguration(GetConfiguration loader) {
            this.loader = loader;
        }

        @Override
        public void run() {
            Configuration configuration = loader.getConfiguration();
            if (configuration != null) {
                try {
                    ConfigurationUtil.stopConfigurationGBeans(configuration, kernel);
                } catch (InvalidConfigException e) {
                    logger.error("Could not start Configuration from bundle " + loader.getBundle().getLocation(), e);
                }
            }
            latch.countDown();
        }
        @Override
        public Configuration getConfiguration() {
            try {
                latch.await();
            } catch (InterruptedException e) {

            }
            return loader.getConfiguration();
        }

        @Override
        public Bundle getBundle() {
            return loader.getBundle();
        }
    }

    private class UnloadConfiguration implements Runnable {

        private final GetConfiguration loader;
        private CountDownLatch latch = new CountDownLatch(1);

        private UnloadConfiguration(GetConfiguration loader) {
            this.loader = loader;
        }

        @Override
        public void run() {
            Configuration configuration = loader.getConfiguration();
            if (configuration != null) {
                try {
                    ConfigurationUtil.unloadConfigurationGBeans(configuration, kernel);
                } catch (InvalidConfigException e) {
                    logger.error("Could not start Configuration from bundle " + loader.getBundle().getLocation(), e);
                }
            }
            latch.countDown();
        }
    }

}
