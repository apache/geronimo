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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationActivator implements BundleActivator {

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
}
