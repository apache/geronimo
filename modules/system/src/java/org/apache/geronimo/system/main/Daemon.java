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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.url.GeronimoURLFactory;

/**
 * @version $Revision: 1.10 $ $Date: 2004/09/09 02:27:31 $
 */
public class Daemon {
    private static Log log;

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
            GBeanMBean configuration = new GBeanMBean(Configuration.GBEAN_INFO);
            ObjectInputStream ois = new ObjectInputStream(classLoader.getResourceAsStream("META-INF/config.ser"));
            try {
                Configuration.loadGMBeanState(configuration, ois);
            } finally {
                ois.close();
            }

            // build a basic kernel without a configuration-store, our configuration store is
            final Kernel kernel = new Kernel("geronimo.kernel", "geronimo");

            // boot the kernel
            try {
                kernel.boot();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(2);
                throw new AssertionError();
            }

            // add our shutdown hook
            ConfigurationManager configurationManager = kernel.getConfigurationManager();
            final ObjectName configName = configurationManager.load(configuration, classLoader.getResource("/"));
            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread") {
                public void run() {
                    log.info("Server shutdown begun");
                    kernel.shutdown();
                    log.info("Server shutdown completed");
                }
            });

            // start this configuration
            kernel.startRecursiveGBean(configName);

            if (configs.isEmpty()) {
                // nothing explicit, see what was running before
                if (kernel.isLoaded(PersistentConfigurationList.OBJECT_NAME)) {
                    try {
                        configs = (List) kernel.invoke(PersistentConfigurationList.OBJECT_NAME, "restore");
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
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    URI configID = (URI) i.next();
                    List list = configurationManager.loadRecursive(configID);
                    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                        ObjectName name = (ObjectName) iterator.next();
                        kernel.startRecursiveGBean(name);
                    }
                }
            } catch (Exception e) {
                try {
                    kernel.shutdown();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                System.exit(3);
                throw new AssertionError();
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
