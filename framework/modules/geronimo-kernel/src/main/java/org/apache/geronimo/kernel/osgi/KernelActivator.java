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
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class KernelActivator implements BundleActivator {

    private ServiceRegistration kernelRegistration;

    public void start(BundleContext bundleContext) throws Exception {
        Kernel kernel = KernelFactory.newInstance(bundleContext).createKernel("geronimo");
        kernel.boot();
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
//            Artifact id = data.getId();
//            manager.startConfiguration(id);
        } finally {
            in.close();
        }

    }

    public void stop(BundleContext bundleContext) throws Exception {
        Kernel kernel = (Kernel) bundleContext.getService(kernelRegistration.getReference());
        kernel.shutdown();
        kernelRegistration.unregister();
        kernelRegistration = null;
    }
}
