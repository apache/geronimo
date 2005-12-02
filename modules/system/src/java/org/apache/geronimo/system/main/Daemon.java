/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoEnvironment;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.jmx.MBeanServerKernelBridge;
import org.apache.geronimo.system.serverinfo.DirectoryUtils;

/**
 * @version $Rev$ $Date$
 */
public class Daemon {
    private final static String ARGUMENT_NO_PROGRESS = "--quiet";
    private final static String ARGUMENT_LONG_PROGRESS = "--long";
    private final static String ARGUMENT_VERBOSE_SHORTFORM = "-v";
    private final static String ARGUMENT_VERBOSE = "--verbose";
    private final static String ARGUMENT_MORE_VERBOSE_SHORTFORM = "-vv";
    private final static String ARGUMENT_MORE_VERBOSE = "--veryverbose";
    private final static String ARGUMENT_CONFIG_OVERRIDE = "--override";
    private static boolean started = false;
    private static Log log;
    private StartupMonitor monitor;
    private List configs = new ArrayList();
    private String verboseArg = null;
    private String noProgressArg = null;
    private String longProgressArg = null;

    private Daemon(String[] args) {
        // Very first startup tasks
        long start = System.currentTimeMillis();
        // Command line arguments affect logging configuration, etc.
        if(processArguments(args)) {
            System.out.println("Booting Geronimo Kernel (in Java " + System.getProperty("java.version") + ")...");
            System.out.flush();

            // Initialization tasks that must run before anything else
            initializeSystem();

            // Now logging is available and
            log.info("Server startup begun");
            monitor.systemStarting(start);
            doStartup();
        } else {
            System.exit(1);
            throw new AssertionError();
        }
    }

    private void printHelp(PrintStream out) {
        out.println();
        out.println("Syntax: java -jar bin/server.jar [options]");
        out.println();
        out.println("Available options are: ");
        out.println("  "+ARGUMENT_NO_PROGRESS);
        out.println("             Suppress the normal startup progress bar.  This is typically\n" +
                    "             used when redirecting console output to a file, or starting\n" +
                    "             the server from an IDE or other tool.");
        out.println("  "+ARGUMENT_LONG_PROGRESS);
        out.println("             Write startup progress to the console in a format that is\n" +
                    "             suitable for redirecting console output to a file, or starting\n" +
                    "             the server from an IDE or other tool (doesn't use linefeeds to\n" +
                    "             update the progress information that is used by default if you\n" +
                    "             don't specify " +ARGUMENT_NO_PROGRESS +" or "+ARGUMENT_LONG_PROGRESS+").\n");
        out.println("  "+ARGUMENT_VERBOSE_SHORTFORM +" " +ARGUMENT_VERBOSE);
        out.println("             Reduces the console log level to INFO, resulting in more\n" +
                    "             console output than is normally present.");
        out.println("  "+ARGUMENT_MORE_VERBOSE_SHORTFORM +" " +ARGUMENT_MORE_VERBOSE);
        out.println("             Reduces the console log level to DEBUG, resulting in still\n" +
                    "             more console output.");
        out.println();
        out.println("  "+ARGUMENT_CONFIG_OVERRIDE+" [configId] [configId] ...");
        out.println("             USE WITH CAUTION!  Overrides the configurations in\n" +
                    "             var/config/config.xml such that only the configurations listed on\n" +
                    "             the command line will be started.  Note that many J2EE\n" +
                    "             features depend on certain configs being started, so you\n" +
                    "             should be very careful what you omit.  Any arguments after\n" +
                    "             this are assumed to be configuration names.");
        out.println();
        out.println("In addition you may specify a replacement for var/config/config.xml using by setting the property\n" +
                    "-Dorg.apache.geronimo.config.file=var/config/<my-config.xml>\n" +
                    "This is resolved relative to the geronimo base directory.");
        out.println();
    }

    /**
     * @return true if the server startup should proceed (all arguments
     *              make sense and the user didn't ask for help)
     */
    private boolean processArguments(String[] args) {
        boolean override = false;
        boolean help = false;
        for (int i = 0; i < args.length; i++) {
            if(override) {
                try {
                    configs.add(new URI(args[i]));
                } catch (URISyntaxException e) {
                    System.err.println("Invalid configuration-id: " + args[i]);
                    e.printStackTrace();
                    System.exit(1);
                    throw new AssertionError();
                }
            } else if (args[i].equals(ARGUMENT_NO_PROGRESS)) {
                noProgressArg = ARGUMENT_NO_PROGRESS;
            } else if (args[i].equals(ARGUMENT_LONG_PROGRESS)) {
                longProgressArg = ARGUMENT_LONG_PROGRESS;
            } else if (args[i].equals(ARGUMENT_VERBOSE_SHORTFORM) ||
                    args[i].equals(ARGUMENT_VERBOSE)) {
                if (verboseArg == null) {
                    verboseArg = ARGUMENT_VERBOSE;
                }
            } else if (args[i].equals(ARGUMENT_MORE_VERBOSE_SHORTFORM) ||
                    args[i].equals(ARGUMENT_MORE_VERBOSE)) {
                if (verboseArg == null) {
                    verboseArg = ARGUMENT_MORE_VERBOSE;
                }
            } else if (args[i].equals(ARGUMENT_CONFIG_OVERRIDE)) {
                override = true;
            } else if(args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("--help") ||
                    args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("/?")) {
                help = true;
            } else {
                System.out.println("Unrecognized argument: "+args[i]);
                help = true;
            }
        }
        if(help) {
            printHelp(System.out);
        }
        return !help;
    }

    private void initializeSystem() {
        if (!started) {
            started = true;

            // Perform initialization tasks common with the various Geronimo environments
            GeronimoEnvironment.init();

            // This MUST be done before the first log is acquired (WHICH THE STARTUP MONITOR 5 LINES LATER DOES!)
            GeronimoLogging.initialize(verboseArg == null ? GeronimoLogging.WARN : verboseArg.equals(ARGUMENT_VERBOSE) ? GeronimoLogging.INFO : GeronimoLogging.DEBUG);
            log = LogFactory.getLog(Daemon.class.getName());
        }

        if (verboseArg != null || noProgressArg != null) {
            monitor = new SilentStartupMonitor();
        } else {
            if (longProgressArg != null)
                monitor = new LongStartupMonitor();
            else
                monitor = new ProgressBarStartupMonitor();
        }
    }

    private void doStartup() {
        try {
            // Check that the tmpdir exists - if not give friendly msg and exit
            // since we allow it to be configured in geronimo.bat and geronimo.sh
            // (since 1.0 release) the same way Tomcat allows it to be configured.
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (tmpDir == null || (!(new File(tmpDir)).exists()) ||
                    (!(new File(tmpDir)).isDirectory())) {
                    System.err.println("The java.io.tmpdir system property specifies the "+
                            "non-existent directory " +tmpDir);
                    System.exit(1);
                    throw new AssertionError();
                }
            
            // Determine the geronimo installation directory
            File geronimoInstallDirectory = DirectoryUtils.getGeronimoInstallDirectory();
            if (geronimoInstallDirectory == null) {
                System.err.println("Could not determine geronimo installation directory");
                System.exit(1);
                throw new AssertionError();
            }

            // setup the endorsed dir entry
            CommandLineManifest manifestEntries = CommandLineManifest.getManifestEntries();

            String endorsedDirs = "java.endorsed.dirs";
            List endorsedDirsFromManifest = manifestEntries.getEndorsedDirs();
            AddToSystemProperty(endorsedDirs, endorsedDirsFromManifest, geronimoInstallDirectory);

            String extensionDirs = "java.ext.dirs";
            List extensionDirsFromManifest = manifestEntries.getExtensionDirs();
            AddToSystemProperty(extensionDirs, extensionDirsFromManifest, geronimoInstallDirectory);


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

            // todo: JNB for now we clear out the dependency list but we really need a way to resolve them
            configuration.setAttribute("dependencies", Collections.EMPTY_LIST);
            configuration.setAttribute("baseURL", classLoader.getResource("/"));

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
                System.exit(1);
                throw new AssertionError();
            }

            // load this configuration into the kernel
            kernel.loadGBean(configuration, classLoader);
            kernel.startGBean(configName);

            // add our shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread("Geronimo shutdown thread") {
                public void run() {
                    log.info("Server shutdown begun");
                    System.out.println("\rServer shutdown begun              ");
                    kernel.shutdown();
                    log.info("Server shutdown completed");
                    System.out.println("Server shutdown completed");
                }
            });

            // add the jmx bridge
            ObjectName mbeanServerKernelBridgeName = new ObjectName("geronimo.boot:role=MBeanServerKernelBridge");
            GBeanData mbeanServerKernelBridge = new GBeanData(mbeanServerKernelBridgeName, MBeanServerKernelBridge.GBEAN_INFO);
            mbeanServerKernelBridge.setAttribute("mbeanServerId", mbeanServerId);
            kernel.loadGBean(mbeanServerKernelBridge, classLoader);
            kernel.startGBean(mbeanServerKernelBridgeName);

            // start this configuration
            kernel.invoke(configName, "loadGBeans", new Object[]{null}, new String[]{ManageableAttributeStore.class.getName()});
            kernel.invoke(configName, "startRecursiveGBeans");
            monitor.systemStarted(kernel);

            GBeanQuery query = new GBeanQuery(null, PersistentConfigurationList.class.getName());

            if (configs.isEmpty()) {
                // -override wasn't used (nothing explicit), see what was running before
                Set configLists = kernel.listGBeans(query);
                for (Iterator i = configLists.iterator(); i.hasNext();) {
                    ObjectName configListName = (ObjectName) i.next();
                    try {
                        configs.addAll((List) kernel.invoke(configListName, "restore"));
                    } catch (IOException e) {
                        System.err.println("Unable to restore last known configurations");
                        e.printStackTrace();
                        kernel.shutdown();
                        System.exit(1);
                        throw new AssertionError();
                    }
                }
            }

            monitor.foundConfigurations((URI[]) configs.toArray(new URI[configs.size()]));

            // load the rest of the configurations
            try {
                ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
                try {
                    for (Iterator i = configs.iterator(); i.hasNext();) {
                        URI configID = (URI) i.next();
                        monitor.configurationLoading(configID);
                        List list = configurationManager.loadRecursive(configID);
                        monitor.configurationLoaded(configID);
                        monitor.configurationStarting(configID);
                        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                            URI name = (URI) iterator.next();
                            configurationManager.loadGBeans(name);
                            configurationManager.start(name);
                        }
                        monitor.configurationStarted(configID);
                    }
                } finally {
                    ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
                }
            } catch (Exception e) {
                //Exception caught when starting configurations, starting kernel shutdown
                monitor.serverStartFailed(e);
                try {
                    kernel.shutdown();
                } catch (Exception e1) {
                    System.err.println("Exception caught during kernel shutdown");
                    e1.printStackTrace();
                }
                System.exit(1);
                throw new AssertionError();
            }

            // Tell every persistent configuration list that the kernel is now fully started
            Set configLists = kernel.listGBeans(query);
            for (Iterator i = configLists.iterator(); i.hasNext();) {
                ObjectName configListName = (ObjectName) i.next();
                kernel.setAttribute(configListName, "kernelFullyStarted", Boolean.TRUE);
            }

            // Startup sequence is finished
            monitor.startupFinished();
            monitor = null;
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
            if (monitor != null) {
                monitor.serverStartFailed(e);
            }
            e.printStackTrace();
            System.exit(1);
            throw new AssertionError();
        }
    }

    private void AddToSystemProperty(String propertyName, List dirsFromManifest, File geronimoInstallDirectory) {
        String dirs = System.getProperty(propertyName, "");
        for (Iterator iterator = dirsFromManifest.iterator(); iterator.hasNext();) {
            String directoryName = (String) iterator.next();
            File directory = new File(directoryName);
            if (!directory.isAbsolute()) {
                directory = new File(geronimoInstallDirectory, directoryName);
            }

            if (dirs.length() > 0) {
                dirs += File.pathSeparatorChar;
            }
            dirs += directory.getAbsolutePath();
        }
        if (dirs.length() > 0) {
            System.setProperty(propertyName, dirs);
        }
        log.debug(propertyName + "=" + System.getProperty(propertyName));
    }

    /**
     * Static entry point allowing a Kernel to be run from the command line.
     *
     * Once the Kernel is booted and the configuration is loaded, the process
     * will remain running until the shutdown() method on the kernel is
     * invoked or until the JVM exits.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Daemon(args);
    }

}
