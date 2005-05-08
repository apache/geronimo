/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.jmx.MBeanServerKernelBridge;
import org.apache.geronimo.system.serverinfo.DirectoryUtils;
import org.apache.geronimo.system.url.GeronimoURLFactory;

/**
 * @version $Rev$ $Date$
 */
public class Daemon {
    private static Log log;
    private static final ObjectName PERSISTENT_CONFIGURATION_LIST_NAME_QUERY = JMXUtil.getObjectName("*:j2eeType=PersistentConfigurationList,*");

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);
        log = LogFactory.getLog(Daemon.class.getName());

        // Install our url factory
        GeronimoURLFactory.install();

        // Install the lame tools jar hack
        ToolsJarHack.install();
    }

    private Daemon() {
    }

    /**
     * Static entry point allowing a Kernel to be run from the command line.
     * Arguments are:
     * <li>the filename of the directory to use for the configuration store.
     * This will be created if it does not exist.</li>
     * <li>the id of a configuation to load</li>
     * Once the Kernel is booted and the configuration is loaded, the process
     * will remain running until the shutdown() method on the kernel is
     * invoked or until the JVM exits.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        log.info("Server startup begun");

        try {
            // Determine the geronimo installation directory
            File geronimoInstallDirectory = DirectoryUtils.getGeronimoInstallDirectory();
            if (geronimoInstallDirectory == null) {
                System.err.println("Could not determine geronimo installation directory");
                System.exit(1);
                throw new AssertionError();
            }

            // setup the endorsed dir entry
            CommandLineManifest manifestEntries = CommandLineManifest.getManifestEntries();
            String endorsedDirs = System.getProperty("java.endorsed.dirs", "");
            for (Iterator iterator = manifestEntries.getEndorsedDirs().iterator(); iterator.hasNext();) {
                String directoryName = (String) iterator.next();
                File directory = new File(directoryName);
                if (!directory.isAbsolute()) {
                    directory = new File(geronimoInstallDirectory, directoryName);
                }

                if (endorsedDirs.length() > 0) {
                    endorsedDirs += File.pathSeparatorChar;
                }
                endorsedDirs += directory.getAbsolutePath();
            }
            if (endorsedDirs.length() > 0) {
                System.setProperty("java.endorsed.dirs", endorsedDirs);
            }
            log.info("java.endorsed.dirs=" + System.getProperty("java.endorsed.dirs"));

            // get a list of the configuration uris from the command line
            List configs = new ArrayList();
            for (int i = 0; i < args.length; i++) {
                try {
                    configs.add(new URI(args[i]));
                } catch (URISyntaxException e) {
                    System.err.println("Invalid configuration-id: " + args[i]);
                    e.printStackTrace();
                    System.exit(1);
                    throw new AssertionError();
                }
            }

            // load this configuration
            ClassLoader classLoader = Daemon.class.getClassLoader();
            GBeanData configuration = new GBeanData();
            ObjectInputStream ois = new ObjectInputStream(classLoader.getResourceAsStream("META-INF/config.ser"));
            try {
                configuration.readExternal(ois);
            } finally {
                ois.close();
            }
            URI configurationId = (URI) configuration.getAttribute("id");
            ObjectName configName = Configuration.getConfigurationObjectName(configurationId);
            configuration.setName(configName);

            // create a mbean server
            MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer("geronimo");
            String mbeanServerId = (String) mbeanServer.getAttribute(new ObjectName("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId");

            // create the kernel
            final Kernel kernel = KernelFactory.newInstance().createKernel("geronimo");

            // boot the kernel
            try {
                kernel.boot();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(2);
                throw new AssertionError();
            }

            // load this configuration into the kernel
            kernel.loadGBean(configuration, classLoader);
            kernel.setAttribute(configName, "baseURL", classLoader.getResource("/"));

            // add our shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread") {
                public void run() {
                    log.info("Server shutdown begun");
                    kernel.shutdown();
                    log.info("Server shutdown completed");
                }
            });

            // add the jmx bridge
            ObjectName mbeanServerKernelBridgeName = new ObjectName("geronimo.boot:role=MBeanServerKernelBridge"); 
            GBeanData mbeanServerKernelBridge = new GBeanData(mbeanServerKernelBridgeName, MBeanServerKernelBridge.GBEAN_INFO);
            mbeanServerKernelBridge.setAttribute("mbeanServerId", mbeanServerId);
            kernel.loadGBean(mbeanServerKernelBridge, classLoader);
            kernel.startGBean(mbeanServerKernelBridgeName);

            // start this configuration
            kernel.startRecursiveGBean(configuration.getName());

            if (configs.isEmpty()) {
                // nothing explicit, see what was running before
                Set configLists = kernel.listGBeans(PERSISTENT_CONFIGURATION_LIST_NAME_QUERY);
                for (Iterator i = configLists.iterator(); i.hasNext();) {
                    ObjectName configListName = (ObjectName) i.next();
                    try {
                        configs.addAll((List) kernel.invoke(configListName, "restore"));
                    } catch (IOException e) {
                        System.err.println("Unable to restore last known configurations");
                        e.printStackTrace();
                        kernel.shutdown();
                        System.exit(3);
                        throw new AssertionError();
                    }
                }
            }

            // load the rest of the configurations
            try {
                ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    URI configID = (URI) i.next();
                    List list = configurationManager.loadRecursive(configID);
                    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                        ObjectName name = (ObjectName) iterator.next();
                        kernel.startRecursiveGBean(name);
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception caught when starting configurations, starting kernel shutdown");
                e.printStackTrace();
                try {
                    kernel.shutdown();
                } catch (Exception e1) {
                    System.err.println("Exception caught during kernel shutdown");
                    e1.printStackTrace();
                }
                System.exit(3);
                throw new AssertionError();
            }

            // Tell every persistent configuration list that the kernel is now fully started
            Set configLists = kernel.listGBeans(PERSISTENT_CONFIGURATION_LIST_NAME_QUERY);
            for (Iterator i = configLists.iterator(); i.hasNext();) {
                ObjectName configListName = (ObjectName) i.next();
                kernel.setAttribute(configListName, "kernelFullyStarted", Boolean.TRUE);
            }

            Set allGBeans = kernel.listGBeans(JMXUtil.getObjectName("*:*"));
            for (Iterator iterator = allGBeans.iterator(); iterator.hasNext();) {
                ObjectName objectName = (ObjectName) iterator.next();
                try {
                    int state = kernel.getGBeanState(objectName);
                    if (state != State.RUNNING_INDEX) {
                        log.info("GBean " + objectName + " is not running. Current state: " + State.fromInt(state).getName());
                    }
                } catch (GBeanNotFoundException e) {
                    log.info("Alleged GBean " + objectName + " is not a GBean");
                }
            }
            log.info("Server startup completed");

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
            e.printStackTrace();
            System.exit(3);
            throw new AssertionError();
        }
    }
}
