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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.cli.daemon.DaemonCLParser;
import org.apache.geronimo.common.GeronimoEnvironment;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.DebugLoggingLifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.Main;
import org.apache.geronimo.system.serverinfo.DirectoryUtils;


/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class EmbeddedDaemon implements Main {
    private static final Log log = LogFactory.getLog(EmbeddedDaemon.class);

    protected final Kernel kernel;
    private StartupMonitor monitor;
    private LifecycleMonitor lifecycleMonitor;
    private List<Artifact> configs = new ArrayList<Artifact>();

    public EmbeddedDaemon(Kernel kernel) {
        this.kernel = kernel;
    }
    
    public int execute(Object opaque) {
        if (! (opaque instanceof DaemonCLParser)) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected [" + DaemonCLParser.class + "]");
        }
        DaemonCLParser parser = (DaemonCLParser) opaque;
        initializeMonitor(parser);
        initializeOverride(parser);

        long start = System.currentTimeMillis();

        System.out.println("Booting Geronimo Kernel (in Java " + System.getProperty("java.version") + ")...");
        System.out.flush();
        
        // Perform initialization tasks common with the various Geronimo environments
        GeronimoEnvironment.init();
        
        monitor.systemStarting(start);
        return doStartup();
    }

    protected void initializeOverride(DaemonCLParser parser) {
        String[] override = parser.getOverride();
        if (null != override) {
            for (String anOverride : override) {
                configs.add(Artifact.create(anOverride));
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
            // Check that the tmpdir exists - if not give friendly msg and exit
            // since we allow it to be configured in geronimo.bat and geronimo.sh
            // (since 1.0 release) the same way Tomcat allows it to be configured.
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (tmpDir == null || (!(new File(tmpDir)).exists()) || (!(new File(tmpDir)).isDirectory())) {
                System.err.println("The java.io.tmpdir system property specifies a non-existent directory: "  + tmpDir);
                return 1;
            }

            // Determine the geronimo installation directory
            File geronimoInstallDirectory = DirectoryUtils.getGeronimoInstallDirectory();
            if (geronimoInstallDirectory == null) {
                System.err.println("Could not determine geronimo installation directory");
                return 1;
            }

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
                    LinkedHashSet<Artifact> sorted = configurationManager.sort(configs, lifecycleMonitor);
                    for (Artifact configID : sorted) {
                        monitor.moduleLoading(configID);
                        configurationManager.loadConfiguration(configID, lifecycleMonitor);
                        monitor.moduleLoaded(configID);
                        monitor.moduleStarting(configID);
                        configurationManager.startConfiguration(configID, lifecycleMonitor);
                        monitor.moduleStarted(configID);
                    }
                    // the server has finished loading the persistent configuration so inform the gbean
                    AbstractNameQuery startedQuery = new AbstractNameQuery(ServerStatus.class.getName());
                    Set statusBeans = kernel.listGBeans(startedQuery);
                    for(Iterator itr = statusBeans.iterator();itr.hasNext();) {
                        AbstractName statusName = (AbstractName)itr.next();
                        ServerStatus status = (ServerStatus)kernel.getGBean(statusName);
                        if(status != null) {
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
        infoFactory.setConstructor(new String[]{"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
