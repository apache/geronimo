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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoEnvironment;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.Main;
import org.apache.geronimo.kernel.util.MainConfigurationBootstrapper;
import org.apache.geronimo.system.serverinfo.DirectoryUtils;


/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class EmbeddedDaemon implements Main {
    private final static String ARGUMENT_NO_PROGRESS = "--quiet";
    private final static String ARGUMENT_LONG_PROGRESS = "--long";
    private final static String ARGUMENT_MODULE_OVERRIDE = "--override";
    private static boolean started = false;
    private static Log log;
    private StartupMonitor monitor;
    protected Kernel kernel;
    private List configs = new ArrayList();
    private String noProgressArg = null;
    private String longProgressArg = null;
    private String verboseArg;

    public EmbeddedDaemon(Kernel kernel) {
        this.kernel = kernel;
    }
    
    public int execute(String[] args) {
        // Very first startup tasks
        long start = System.currentTimeMillis();
        // Command line arguments affect logging configuration, etc.
        if (processArguments(args)) {
            System.out.println("Booting Geronimo Kernel (in Java " + System.getProperty("java.version") + ")...");
            System.out.flush();

            // Initialization tasks that must run before anything else
            initializeSystem();

            monitor.systemStarting(start);
            return doStartup();
        } else {
            return 1;
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
        out.println("  "+MainConfigurationBootstrapper.ARGUMENT_VERBOSE_SHORTFORM +" " +MainConfigurationBootstrapper.ARGUMENT_VERBOSE);
        out.println("             Reduces the console log level to DEBUG, resulting in more\n" +
                    "             console output than is normally present.");
        out.println("  "+MainConfigurationBootstrapper.ARGUMENT_MORE_VERBOSE_SHORTFORM +" " +MainConfigurationBootstrapper.ARGUMENT_MORE_VERBOSE);
        out.println("             Reduces the console log level to TRACE, resulting in still\n" +
                    "             more console output.");
        out.println();
        out.println("  "+ARGUMENT_MODULE_OVERRIDE+" [moduleId] [moduleId] ...");
        out.println("             USE WITH CAUTION!  Overrides the modules in\n" +
                    "             var/config/config.xml such that only the modules listed on\n" +
                    "             the command line will be started.  Note that many J2EE\n" +
                    "             features depend on certain modules being started, so you\n" +
                    "             should be very careful what you omit.  Any arguments after\n" +
                    "             this are assumed to be module names.");
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
        verboseArg = MainConfigurationBootstrapper.getVerboseLevel(args);

        boolean override = false;
        boolean help = false;
        for (int i = 0; i < args.length; i++) {
            if (override) {
                configs.add(Artifact.create(args[i]));
            } else if (args[i].equals(ARGUMENT_NO_PROGRESS)) {
                noProgressArg = ARGUMENT_NO_PROGRESS;
            } else if (args[i].equals(ARGUMENT_LONG_PROGRESS)) {
                longProgressArg = ARGUMENT_LONG_PROGRESS;
            } else if (args[i].equals(ARGUMENT_MODULE_OVERRIDE)) {
                override = true;
            } else if (null != MainConfigurationBootstrapper.filterVerboseArgument(args[i])) {
                ;
            } else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("--help") ||
                       args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("/?")) {
                help = true;
            } else {
                System.out.println("Unrecognized argument: "+args[i]);
                help = true;
            }
        }
        if (help) {
            printHelp(System.out);
        }
        return !help;
    }

    protected void initializeSystem() {
        if (!started) {
            started = true;

            // Perform initialization tasks common with the various Geronimo environments
            GeronimoEnvironment.init();
            
            initializeLogging();
        }

        initializeMonitor();
    }

    protected void initializeLogging() {
        //
        // FIXME: Allow -v -> INFO, -vv -> DEBUG, -vvv -> TRACE
        //
        
        // This MUST be done before the first log is acquired (which the startup monitor below does)
        // Generally we want to suppress anything but WARN until the log GBean starts up
        GeronimoLogging level = GeronimoLogging.WARN;
        if (verboseArg != null) {
            if (MainConfigurationBootstrapper.isVerboseLevel(verboseArg)) {
                level = GeronimoLogging.DEBUG;
            } else if (MainConfigurationBootstrapper.isMoreVerboseLevel(verboseArg)) {
                level = GeronimoLogging.TRACE;
            }
        }
        GeronimoLogging.initialize(level);
        
        log = LogFactory.getLog(EmbeddedDaemon.class.getName());
    }

    protected void initializeMonitor() {
        if (verboseArg != null || noProgressArg != null) {
            monitor = new SilentStartupMonitor();
        } else {
            if (longProgressArg != null) {
                monitor = new LongStartupMonitor();
            } else {
                monitor = new ProgressBarStartupMonitor();
            }
        }
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
                Set configLists = kernel.listGBeans(query);
                for (Iterator i = configLists.iterator(); i.hasNext();) {
                    AbstractName configListName = (AbstractName) i.next();
                    try {
                        configs.addAll((List) kernel.invoke(configListName, "restore"));
                    } catch (IOException e) {
                        System.err.println("Unable to restore last known configurations");
                        e.printStackTrace();
                        shutdownKernel();
                        return 1;
                    }
                }
            }

            monitor.foundModules((Artifact[]) configs.toArray(new Artifact[configs.size()]));

            // load the rest of the configurations
            try {
                ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
                try {
                    for (Iterator i = configs.iterator(); i.hasNext();) {
                        Artifact configID = (Artifact) i.next();
                        monitor.moduleLoading(configID);
                        configurationManager.loadConfiguration(configID);
                        monitor.moduleLoaded(configID);
                        monitor.moduleStarting(configID);
                        configurationManager.startConfiguration(configID);
                        monitor.moduleStarted(configID);
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
            Set configLists = kernel.listGBeans(query);
            for (Iterator i = configLists.iterator(); i.hasNext();) {
                AbstractName configListName = (AbstractName) i.next();
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
