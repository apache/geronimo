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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.cli.daemon.DaemonCLParser;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.DebugLoggingLifecycleMonitor;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.Main;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class EmbeddedDaemon implements Main {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedDaemon.class);

    protected final Kernel kernel;
    private Bundle bundle;
    private StartupMonitor monitor;
    private LifecycleMonitor lifecycleMonitor;
    private List<Artifact> configs = new ArrayList<Artifact>();
    static String KEYSTORE_TRUSTSTORE_PASSWORD_FILE = "org.apache.geronimo.keyStoreTrustStorePasswordFile";
    static String DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION = "/var/security/keystores/geronimo-default";
    static String GERONIMO_HOME = "org.apache.geronimo.home.dir";
    static String DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE = System.getProperty(GERONIMO_HOME)
            + "/var/config/config-substitutions.properties";
    
    public EmbeddedDaemon(Kernel kernel, Bundle bundle) {
        this.kernel = kernel;
        this.bundle = bundle;
    }

    public int execute(Object opaque) {
        if (!(opaque instanceof DaemonCLParser)) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected [" + DaemonCLParser.class + "]");
        }
        DaemonCLParser parser = (DaemonCLParser) opaque;
        
        cleanCache(parser);
        initializeMonitor(parser);
        initializeOverride(parser);
        initializeSecure(parser);

        long start = System.currentTimeMillis();

        System.out.println("Booting Geronimo Kernel (in Java " + System.getProperty("java.version") + ")...");
        System.out.flush();
        
        // Perform initialization tasks common with the various Geronimo environments
        //GeronimoEnvironment.init();

        monitor.systemStarting(start);
        return doStartup();       
    }

    protected void initializeSecure(DaemonCLParser parser) {
        if (parser.isSecure()) {
            try {
                Properties props = new Properties();

                String keyStorePassword = null;
                String trustStorePassword = null;

                FileInputStream fstream = new FileInputStream(System.getProperty(KEYSTORE_TRUSTSTORE_PASSWORD_FILE,
                        DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE));
                props.load(fstream);

                keyStorePassword = (String) EncryptionManager.decrypt(props.getProperty("keyStorePassword"));
                trustStorePassword = (String) EncryptionManager.decrypt(props.getProperty("trustStorePassword"));

                fstream.close();

                String value = System.getProperty("javax.net.ssl.keyStore", System.getProperty(GERONIMO_HOME)
                        + DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
                String value1 = System.getProperty("javax.net.ssl.trustStore", System.getProperty(GERONIMO_HOME)
                        + DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION);
                System.setProperty("javax.net.ssl.keyStore", value);
                System.setProperty("javax.net.ssl.trustStore", value1);
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void initializeOverride(DaemonCLParser parser) {
        String[] override = parser.getOverride();
        if (null != override) {
            for (String anOverride : override) {
                configs.add(Artifact.create(anOverride));
            }
        }
    }
    
    private static String[] getCleanDirectoryList() {
        String directoryList = System.getProperty("geronimo.cleanDirectoryList");
        if (directoryList != null) {
            return directoryList.split(",");
        } else {
            return new String[0];
        }
    }
    
    protected void cleanCache(DaemonCLParser parser) {
        if (parser.isCleanCache()) {
            String [] dirList = getCleanDirectoryList();
            for (String dir : dirList) {
                File file = new File(dir);
                FileUtils.recursiveDelete(file);
            }
        }
    }

    protected void initializeMonitor(DaemonCLParser parser) {
        if (parser.isVerboseInfo() || parser.isVerboseDebug() || parser.isVerboseTrace() || parser.isNoProgress()) {
            monitor = new SilentStartupMonitor();
        } else {
            if (parser.isLongProgress()) {
                monitor = new LongStartupMonitor();
            } else {
                monitor = new SimpleProgressBarStartupMonitor();
            }
        }
        lifecycleMonitor = new DebugLoggingLifecycleMonitor(log);
    }

    protected int doStartup() {
        try {            
            int exitCode = initializeKernel();
            if (0 != exitCode) {
                return exitCode;
            }

            monitor.systemStarted(kernel);

            AbstractNameQuery query = new AbstractNameQuery(PersistentConfigurationList.class.getName());

            if (configs.isEmpty()) {
                // --override wasn't used (nothing explicit), see what was running before
                Set<AbstractName> configLists = kernel.listGBeans(query);
                for (AbstractName configListName : configLists) {
                    try {
                        configs.addAll((List<Artifact>) kernel.invoke(configListName, "restore"));
                    } catch (IOException e) {
                        System.err.println("Unable to restore last known configurations");
                        e.printStackTrace();
                        shutdownKernel();
                        return 1;
                    }
                }
            }

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
                shutdownKernel();
                return 1;
            }

            // Tell every persistent configuration list that the kernel is now fully started
            Set<AbstractName> configLists = kernel.listGBeans(query);
            for (AbstractName configListName : configLists) {
                kernel.setAttribute(configListName, "kernelFullyStarted", Boolean.TRUE);
            }

            // Startup sequence is finished
            monitor.startupFinished();
            monitor = null;

            /*
            // capture this thread until the kernel is ready to exit
            while (kernel.isRunning()) {
                try {
                    synchronized (kernel) {
                        kernel.wait();
                    }
                } catch (InterruptedException e) {
                    // continue
                }
            }
            */
        } catch (Exception e) {
            if (monitor != null) {
                monitor.serverStartFailed(e);
            }
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    protected void shutdownKernel() {
        try {
            kernel.shutdown();
        } catch (Exception e1) {
            System.err.println("Exception caught during kernel shutdown");
            e1.printStackTrace();
        }
    }

    protected int initializeKernel() throws Exception {
        return 0;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EmbeddedDaemon.class, "EmbeddedDaemon");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("bundle", Bundle.class, false);
        infoFactory.setConstructor(new String[]{"kernel", "bundle"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
