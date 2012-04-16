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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.cli.daemon.DaemonCLParser;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.DebugLoggingLifecycleMonitor;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.karaf.info.ServerInfo;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This starts geronimo inside karaf.
 *
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */

@Component
public class EmbeddedDaemon {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedDaemon.class);

    private BundleContext bundleContext;
    private StartupMonitor monitor;
    private LifecycleMonitor lifecycleMonitor;
    private List<Artifact> configs = new ArrayList<Artifact>();

    @Reference(name = "persistentConfigurationList", referenceInterface = PersistentConfigurationList.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private List<PersistentConfigurationList> configurationLists = new ArrayList<PersistentConfigurationList>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ConfigurationManager configurationManager;

    //NB karaf server info
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ServerInfo serverInfo;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private Kernel kernel;

    static String KEYSTORE_TRUSTSTORE_PASSWORD_FILE = "org.apache.geronimo.keyStoreTrustStorePasswordFile";
    static String DEFAULT_TRUSTSTORE_KEYSTORE_LOCATION = "/var/security/keystores/geronimo-default";
    static String GERONIMO_HOME = "org.apache.geronimo.home.dir";
    static String GERONIMO_SERVER = "org.apache.geronimo.server.dir";
    static String DEFAULT_KEYSTORE_TRUSTSTORE_PASSWORD_FILE = System.getProperty(GERONIMO_SERVER)
            + "/var/config/config-substitutions.properties";

//    public EmbeddedDaemon(Kernel kernel, Bundle bundle) {
//        this.kernel = kernel;
//        this.bundle = bundle;
//    }

    public void bindPersistentConfigurationList(PersistentConfigurationList config) {
        configurationLists.add(config);
    }

    public void unbindPersistentConfigurationList(PersistentConfigurationList config) {
        configurationLists.remove(config);
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void unsetConfigurationManager(ConfigurationManager configurationManager) {
        if (configurationManager == this.configurationManager) {
            this.configurationManager = null;
        }
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void unsetServerInfo(ServerInfo serverInfo) {
        if (serverInfo == this.serverInfo) {
            this.serverInfo = null;
        }
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public void unsetKernel(Kernel kernel) {
        if (kernel == this.kernel) {
            this.kernel = null;
        }
    }

    @Activate
    public void execute(BundleContext bundleContext) throws CLParserException {
        this.bundleContext = bundleContext;
        DaemonCLParser parser = new DaemonCLParser(System.out);
        parser.parse(serverInfo.getArgs());

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
        doStartup();
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
            } catch (IOException e) {
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
            String[] dirList = getCleanDirectoryList();
            for (String dir : dirList) {
                File file = new File(dir);
                FileUtils.recursiveDelete(file);
            }
        }
    }

    private Set<Artifact> getLoadOnlyConfigList() {
        String list = System.getProperty("geronimo.loadOnlyConfigList");
        Set<Artifact> artifacts = new HashSet<Artifact>();
        if (list != null) {
            for (String name : list.split("\\s*,\\s*")) {
                try {
                    artifacts.add(Artifact.create(name));
                } catch (Exception e) {
                    System.err.println("Error parsing configuration name [" + name + "]: " + e.getMessage());
                }
            }
        }
        return artifacts;
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


            if (configs.isEmpty()) {
                // --override wasn't used (nothing explicit), see what was running before
                for (PersistentConfigurationList persistentConfigurationList : configurationLists) {
                    try {
                        configs.addAll(persistentConfigurationList.restore());
                    } catch (IOException e) {
                        System.err.println("Unable to restore last known configurations");
                        e.printStackTrace();
                        shutdownKernel();
                        return 1;
                    }
                }
            }

            monitor.foundModules(configs.toArray(new Artifact[configs.size()]));

            final Set<Artifact> loadOnlyConfigs = getLoadOnlyConfigList();

            new Thread() {
                public void run() {
                    try {
                        List<Artifact> unloadedConfigs = new ArrayList<Artifact>(configs);
                        int unloadedConfigsCount;
                        do {
                            unloadedConfigsCount = unloadedConfigs.size();
                            LinkedHashSet<Artifact> sorted = configurationManager.sort(unloadedConfigs, lifecycleMonitor);
                            for (Artifact configID : sorted) {
                                monitor.moduleLoading(configID);
                                configurationManager.loadConfiguration(configID, lifecycleMonitor);
                                int configModuleType = configurationManager.getConfiguration(configID).getModuleType().getValue();
                                unloadedConfigs.remove(configID);
                                monitor.moduleLoaded(configID);
                                try {
                                    monitor.moduleStarting(configID);
                                    if (!loadOnlyConfigs.contains(configID)) {
                                        configurationManager.startConfiguration(configID, lifecycleMonitor);
                                    }
                                    monitor.moduleStarted(configID);
                                } catch (Exception e) {
                                    if ( configModuleType != ConfigurationModuleType.SERVICE.getValue() ) {
                                        log.warn("Failed to start module " + configID + "; Cause by " + e.getCause());
                                        log.warn("Please try to correct the problem by referring to the logged exception. " +
                                    		"If you want to bypass it in future restart, you can set load=\"false\" on " +
                                    		"that module.");
                                        // Only log warning and let user decide what action to take
                                        //configurationManager.unloadConfiguration(configID);
                                        continue;
                                    }
                                }
                            }
                        } while (unloadedConfigsCount > unloadedConfigs.size());
                        if (!unloadedConfigs.isEmpty()) {
                            // GERONIMO-5802 Not simply fail server when unloadedConfigs is not empty, thus, server could
                            // start in most cases as long as the system modules are started OK.
                            // throw new InvalidConfigException("Could not locate configs to start: " + unloadedConfigs);
                            for (Artifact configID:unloadedConfigs) {
                                for (PersistentConfigurationList persistentConfigurationList : configurationLists) {
                                    persistentConfigurationList.stopConfiguration(configID);
                                }                            
                            }
                            log.warn("Could not start configs: " + unloadedConfigs);
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
                        // Tell every persistent configuration list that the kernel is now fully started
                        for (PersistentConfigurationList persistentConfigurationList : configurationLists) {
                            persistentConfigurationList.setKernelFullyStarted(true);
                        }

                        // Startup sequence is finished
                        monitor.startupFinished();
                        monitor = null;

                        // Because currently we start Geronimo bundles out of the osgi framework life cycle,
                        // so there might be some bundles, which depends on geronimo bundles, can not be resovled during osgi framework launch.
                        // we need re-try start it after geronimo start.
                        // This could be deleted after we smooth out geronimo life cycle with osgi.
                        for (Bundle b : bundleContext.getBundles()) {
                            if (BundleUtils.canStart(b)) {
                                try {
                                    b.start(Bundle.START_TRANSIENT);
                                } catch (BundleException e) {
                                    log.warn("Bundle " + b.getBundleId() + " failed to start: " + e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        //Exception caught when starting configurations, starting kernel shutdown
                        monitor.serverStartFailed(e);
                        shutdownKernel();
                    }

                }
            }.start();
            // load the rest of the configurations

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
//        try {
//            kernel.shutdown();
//        } catch (Exception e1) {
//            System.err.println("Exception caught during kernel shutdown");
//            e1.printStackTrace();
//        }
    }

    protected int initializeKernel() throws Exception {
        return 0;
    }

//    public static final GBeanInfo GBEAN_INFO;
//
//    static {
//        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EmbeddedDaemon.class, "EmbeddedDaemon");
//        infoFactory.addAttribute("kernel", Kernel.class, false);
//        infoFactory.addAttribute("bundle", Bundle.class, false);
//        infoFactory.setConstructor(new String[]{"kernel", "bundle"});
//        GBEAN_INFO = infoFactory.getBeanInfo();
//    }
//
//    public static GBeanInfo getGBeanInfo() {
//        return GBEAN_INFO;
//    }

}
