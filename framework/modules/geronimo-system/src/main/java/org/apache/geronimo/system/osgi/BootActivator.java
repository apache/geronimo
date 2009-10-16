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


package org.apache.geronimo.system.osgi;

import java.util.Dictionary;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.osgi.ConfigurationActivator;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.DebugLoggingLifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.system.main.StartupMonitor;
import org.apache.geronimo.system.main.LongStartupMonitor;
import org.apache.geronimo.system.main.ServerStatus;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class BootActivator implements BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(BootActivator.class);

    private ServiceRegistration kernelRegistration;
    private BundleActivator configurationActivator;

    public void start(BundleContext bundleContext) throws Exception {
        if (bundleContext.getServiceReference(Kernel.class.getName()) == null) {
            StartupMonitor monitor = new LongStartupMonitor();
            monitor.systemStarting(System.currentTimeMillis());
            Kernel kernel = KernelFactory.newInstance(bundleContext).createKernel("geronimo");
            kernel.boot();
            monitor.systemStarted(kernel);
            Dictionary dictionary = null;//new Hashtable();
            kernelRegistration = bundleContext.registerService(Kernel.class.getName(), kernel, dictionary);
            //boot the root configuration
            Bundle bundle = bundleContext.getBundle();
            URL plan = bundle.getEntry("META-INF/config.ser");
            InputStream in = plan.openStream();
            try {
                //TODO there are additional consistency checks in RepositoryConfigurationStore that we should use.
                ConfigurationData data = ConfigurationUtil.readConfigurationData(in);
                data.setBundleContext(bundleContext);
                AbstractName name = ConfigurationUtil.loadBootstrapConfiguration(kernel, data, bundleContext, false);
//                Artifact id = data.getId();
//                manager.startConfiguration(id);
            } finally {
                in.close();
            }

            List<Artifact> configs = new ArrayList<Artifact>();
            AbstractNameQuery query = new AbstractNameQuery(PersistentConfigurationList.class.getName());

//        if (configs.isEmpty()) {
            // --override wasn't used (nothing explicit), see what was running before
            Set<AbstractName> configLists = kernel.listGBeans(query);
            for (AbstractName configListName : configLists) {
                try {
                    configs.addAll((List<Artifact>) kernel.invoke(configListName, "restore"));
                } catch (IOException e) {
                    System.err.println("Unable to restore last known configurations");
                    e.printStackTrace();
//                    shutdownKernel();
                    return;
                }
            }
//        }
            LifecycleMonitor lifecycleMonitor = new DebugLoggingLifecycleMonitor(log);

            monitor.foundModules(configs.toArray(new Artifact[configs.size()]));

            // load the rest of the configurations
            try {
                ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
                try {
                    List<Artifact> unloadedConfigs = new ArrayList<Artifact>(configs);
                    int unloadedConfigsCount;
                    do {
                        unloadedConfigsCount = unloadedConfigs.size();
                        LinkedHashSet<Artifact> sorted = configurationManager.sort(unloadedConfigs, lifecycleMonitor);
                        for (Artifact configID : sorted) {
                            monitor.moduleLoading(configID);
                            configurationManager.loadConfiguration(configID, lifecycleMonitor);
                            unloadedConfigs.remove(configID);
                            monitor.moduleLoaded(configID);
                            monitor.moduleStarting(configID);
                            configurationManager.startConfiguration(configID, lifecycleMonitor);
                            monitor.moduleStarted(configID);
                        }
                    } while (unloadedConfigsCount > unloadedConfigs.size());
                    if (!unloadedConfigs.isEmpty()) {
                        throw new InvalidConfigException("Could not locate configs to start: " + unloadedConfigs);
                    }
                    // the server has finished loading the persistent configuration so inform the gbean
                    AbstractNameQuery startedQuery = new AbstractNameQuery(ServerStatus.class.getName());
                    Set<AbstractName> statusBeans = kernel.listGBeans(startedQuery);
                    for (AbstractName statusName : statusBeans) {
                        ServerStatus status = (ServerStatus) kernel.getGBean(statusName);
                        if (status != null) {
                            status.setServerStarted(true);
                        }
                    }
                } finally {
                    ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
                }
            } catch (Exception e) {
                //Exception caught when starting configurations, starting kernel shutdown
                monitor.serverStartFailed(e);
//            shutdownKernel();
                return;
            }

            // Tell every persistent configuration list that the kernel is now fully started
            configLists = kernel.listGBeans(query);
            for (AbstractName configListName : configLists) {
                kernel.setAttribute(configListName, "kernelFullyStarted", Boolean.TRUE);
            }

            // Startup sequence is finished
            monitor.startupFinished();
        } else {
            configurationActivator = new ConfigurationActivator();
            configurationActivator.start(bundleContext);
        }

    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (configurationActivator == null) {
            Kernel kernel = (Kernel) bundleContext.getService(kernelRegistration.getReference());
            kernel.shutdown();
            kernelRegistration.unregister();
            kernelRegistration = null;
        } else {
            configurationActivator.stop(bundleContext);
        }
    }
}