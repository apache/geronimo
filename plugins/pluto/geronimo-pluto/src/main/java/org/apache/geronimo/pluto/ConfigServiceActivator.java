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


package org.apache.geronimo.pluto;

import java.io.InputStream;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.apache.geronimo.pluto.impl.ResourceConfigReader;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.pluto.driver.services.impl.resource.ResourceConfig;
import org.apache.pluto.driver.services.portal.RenderConfig;

/**
 * @version $Rev$ $Date$
 */
public class ConfigServiceActivator implements BundleActivator {

    private ServiceRegistration serviceRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        InputStream in = bundleContext.getBundle().getResource(ResourceConfigReader.CONFIG_FILE).openStream();
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new BundleClassLoader(bundleContext.getBundle()));
        ResourceConfig config;
        try {
            config = new ResourceConfigReader(new BundleClassLoader(bundleContext.getBundle())).parse(in);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

        ConfigService configService = new ConfigServiceImpl(config);
        serviceRegistration = bundleContext.registerService(ConfigService.class.getName(), configService, new Hashtable());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        serviceRegistration.unregister();
    }
}
