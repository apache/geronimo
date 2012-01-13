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

package org.apache.geronimo.system.main;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
//import org.apache.geronimo.main.Main;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO is this used?  How?
 * @version $Rev:385659 $ $Date$
 */
public class MainBridge implements /*Main,*/ GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MainBridge.class);

    protected final Kernel kernel;
    protected final Bundle bundle;

    public MainBridge(Kernel kernel, Bundle bundle) {
        this.kernel = kernel;
        this.bundle = bundle;
    }

    public int execute(Object opaque) {
        org.apache.geronimo.kernel.util.Main main;
        try {
            loadPersistentConfigurations();
            main = getMain();
        } catch (Exception e) {
            e.printStackTrace();
            shutdownKernel();
            return 1;
        }
        return main.execute(opaque);
    }

    protected void loadPersistentConfigurations() throws Exception {
        List<Artifact> configs = new ArrayList<Artifact>();

        AbstractNameQuery query = new AbstractNameQuery(PersistentConfigurationList.class.getName());

        if (configs.isEmpty()) {
            Set<AbstractName> configLists = kernel.listGBeans(query);
            for (AbstractName configListName : configLists) {
                configs.addAll((List<Artifact>) kernel.invoke(configListName, "restore"));
            }
        }

        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Artifact config : configs) {
                configurationManager.loadConfiguration(config);
                configurationManager.startConfiguration(config);
            }
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }
    }
    
    protected org.apache.geronimo.kernel.util.Main getMain() throws Exception {
        return (org.apache.geronimo.kernel.util.Main) kernel.getGBean(org.apache.geronimo.kernel.util.Main.class);
    }

    protected void shutdownKernel() {
        try {
            kernel.shutdown();
        } catch (Exception e1) {
            System.err.println("Exception caught during kernel shutdown");
            e1.printStackTrace();
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MainBridge.class, "MainBridge");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("bundle", Bundle.class, false);
        infoFactory.setConstructor(new String[]{"kernel", "bundle"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public void doFail() {
    }

    public void doStart() throws Exception {
//        bundle.getBundleContext().registerService(Main.class.getName(), this, new Hashtable());
    }

    public void doStop() throws Exception {
        // TODO: unregister Main service?
    }

}
