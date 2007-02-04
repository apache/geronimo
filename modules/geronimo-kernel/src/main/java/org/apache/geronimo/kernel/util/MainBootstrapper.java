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
package org.apache.geronimo.kernel.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 *
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class MainBootstrapper {

    public static void main(String[] args) {
        MainBootstrapper bootstrapper = new MainBootstrapper();
        Main main = bootstrapper.getMain(MainBootstrapper.class.getClassLoader());

        int exitCode;
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newTCCL = main.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newTCCL);
            exitCode = main.execute(args);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        bootstrapper.shutdown();
        
        System.exit(exitCode);
    }
    
    protected Kernel kernel;
    
    public Main getMain(ClassLoader classLoader) {
        try {
            bootKernel();
            loadBootConfiguration(classLoader);
            loadPersistentConfigurations();
            return getMain();
        } catch (Exception e) {
            if (null != kernel) {
                kernel.shutdown();
            }
            e.printStackTrace();
            System.exit(1);
            throw new AssertionError();
        }
    }
    
    protected void shutdown() {
        kernel.shutdown();
    }

    protected void bootKernel() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("MainBootstrapper");
        kernel.boot();

        Runtime.getRuntime().addShutdownHook(new Thread("MainBootstrapper shutdown thread") {
            public void run() {
                kernel.shutdown();
            }
        });
    }
    
    protected void loadBootConfiguration(ClassLoader classLoader) throws Exception {
        InputStream in = classLoader.getResourceAsStream("META-INF/config.ser");
        try {
            ConfigurationUtil.loadBootstrapConfiguration(kernel, in, classLoader);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }
    
    protected void loadPersistentConfigurations() throws Exception {
        List<Artifact> configs = new ArrayList<Artifact>();

        AbstractNameQuery query = new AbstractNameQuery(PersistentConfigurationList.class.getName());
        Set configLists = kernel.listGBeans(query);
        for (Iterator i = configLists.iterator(); i.hasNext();) {
            AbstractName configListName = (AbstractName) i.next();
            configs.addAll((List<Artifact>) kernel.invoke(configListName, "restore"));
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

    protected Main getMain() throws Exception {
        return (Main) kernel.getGBean(Main.class);
    }
    
}
