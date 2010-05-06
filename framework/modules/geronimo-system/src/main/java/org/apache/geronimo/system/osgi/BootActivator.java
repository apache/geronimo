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

import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.osgi.ConfigurationActivator;
import org.apache.geronimo.kernel.util.Main;
import org.apache.geronimo.system.main.LongStartupMonitor;
import org.apache.geronimo.system.main.StartupMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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

            // register Main service if Main GBean present
            if (bundleContext.getServiceReference(org.apache.geronimo.main.Main.class.getName()) == null) { 
                registerMainService(bundleContext, kernel);
            }

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
    
    private void registerMainService(BundleContext bundleContext, Kernel kernel) {
        try {
            final Main main = kernel.getGBean(Main.class);
            bundleContext.registerService(
                    org.apache.geronimo.main.Main.class.getName(), 
                    new org.apache.geronimo.main.Main() {
                        public int execute(Object opaque) {
                            return main.execute(opaque);
                        }                    
                    }, 
                    null);
        } catch (GBeanNotFoundException e) {
            // ignore
        }
    }
}
