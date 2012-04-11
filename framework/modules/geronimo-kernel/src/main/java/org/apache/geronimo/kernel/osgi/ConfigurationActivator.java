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

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.CircularReferencesException;
import org.apache.geronimo.kernel.util.IllegalNodeConfigException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationActivator.class);

    private Artifact id;

    public void start(BundleContext bundleContext) throws Exception {
        ServiceReference kernelReference = null;
        InputStream in = null;
        try {
            kernelReference = bundleContext.getServiceReference(Kernel.class.getName());
            if (kernelReference == null) {
                return;
            }
            Kernel kernel = (Kernel) bundleContext.getService(kernelReference);
            ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
            Bundle bundle = bundleContext.getBundle();
            in = bundle.getEntry("META-INF/config.ser").openStream();
            //TODO there are additional consistency checks in RepositoryConfigurationStore that we should use.
            ConfigurationData data = ConfigurationUtil.readConfigurationData(in);
            data.setBundleContext(bundleContext);
            manager.loadConfiguration(data);
            id = data.getId();
            //            manager.startConfiguration(id);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e) {
                }
            if (kernelReference != null)
                try {
                    bundleContext.ungetService(kernelReference);
                } catch (Exception e) {
                }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        ServiceReference kernelReference = null;
        try {
            kernelReference = bundleContext.getServiceReference(Kernel.class.getName());
            if (kernelReference == null) {
                return;
            }
            Kernel kernel = (Kernel) bundleContext.getService(kernelReference);
            //Stop the child configurations and child GBeans, the stopRecursive method is the same with KernelConfigurationManager
            //In this method, we will pre-sort the child GBeans before stopping them, with that, those GBeans could be stopped in order
            //Or it is possible to get the error message [GBeanDependency] Illegal state: current target for a single valued reference stopped
            ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
            Configuration configuration = manager.getConfiguration(id);
            if(configuration != null) {
                stopRecursive(kernel, configuration);
            }

            AbstractName name = Configuration.getConfigurationAbstractName(id);
            //TODO investigate how this is called and whether just stopping/unloading the configuration gbean will
            //leave the configuration model in a consistent state.  We might need a shutdown flag set elsewhere to avoid
            //overwriting the load attribute in config.xml. This code mimics the shutdown hook in KernelConfigurationManager
            //see https://issues.apache.org/jira/browse/GERONIMO-4909
            try {
                kernel.stopGBean(name);
            } catch (GBeanNotFoundException e) {
            } catch (InternalKernelException e) {
            } catch (IllegalStateException e) {
            }
            try {
                kernel.unloadGBean(name);
            } catch (GBeanNotFoundException e) {
            } catch (InternalKernelException e) {
            } catch (IllegalStateException e) {
            }
            //TODO this code is more symmetrical with start, but currently sets the load attribute to false in config.xml,
            //which prevents restarting the server.
            //            ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
            //            manager.unloadConfiguration(id);
        } finally {
            if (kernelReference != null)
                try {
                    bundleContext.ungetService(kernelReference);
                } catch (Exception e) {
                }
        }
    }

    private void stopRecursive(Kernel kernel , Configuration configuration) {
        // stop all of the child configurations first
        for (Iterator<Configuration> iterator = configuration.getChildren().iterator(); iterator.hasNext();) {
            Configuration childConfiguration = iterator.next();
            stopRecursive(kernel, childConfiguration);
        }
        Collection<GBeanData> gbeans;
        try {
            List<GBeanData> sortedGBeans = ConfigurationUtil.sortGBeanDataByDependency(configuration.getGBeans().values());
            Collections.reverse(sortedGBeans);
            gbeans = sortedGBeans;
        } catch (IllegalNodeConfigException e) {
            gbeans = configuration.getGBeans().values();
        } catch (CircularReferencesException e) {
            gbeans = configuration.getGBeans().values();
        }
        // stop the gbeans
        for (Iterator<GBeanData> iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = iterator.next();
            AbstractName gbeanName = gbeanData.getAbstractName();
            try {
                kernel.stopGBean(gbeanName);
            } catch (GBeanNotFoundException ignored) {
            } catch (IllegalStateException ignored) {
            } catch (InternalKernelException kernelException) {
                logger.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
            }
        }

        // unload the gbeans
        for (Iterator<GBeanData> iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = iterator.next();
            AbstractName gbeanName = gbeanData.getAbstractName();
            try {
                kernel.unloadGBean(gbeanName);
            } catch (GBeanNotFoundException ignored) {
            } catch (IllegalStateException ignored) {
            } catch (InternalKernelException kernelException) {
                logger.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
            }
        }
    }
}
