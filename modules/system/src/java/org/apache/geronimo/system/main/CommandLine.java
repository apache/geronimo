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

import java.io.ObjectInputStream;
import java.net.URI;
import java.util.Iterator;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.url.GeronimoURLFactory;


/**
 * @version $Revision: 1.7 $ $Date: 2004/07/31 15:05:12 $
 */
public class CommandLine {
    private static Log log;

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.ERROR);
        log = LogFactory.getLog(CommandLine.class.getName());

        // Install our url factory
        GeronimoURLFactory.install();
    }

    private CommandLine() {
    }

    /**
     * Command line entry point called by executable jar
     * @param args command line args
     */
    public static void main(String[] args) {

        log.info("Server startup begun");

        try {
            // the interesting entries from the manifest
            CommandLineManifest manifest = CommandLineManifest.getManifestEntries();

            // boot the kernel
            Kernel kernel = new Kernel("geronimo.kernel", "geronimo");
            kernel.boot();

            // load and start the configuration in this jar
            ConfigurationManager configurationManager = kernel.getConfigurationManager();
            GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
            ClassLoader classLoader = CommandLine.class.getClassLoader();
            ObjectInputStream ois = new ObjectInputStream(classLoader.getResourceAsStream("META-INF/config.ser"));
            try {
                Configuration.loadGMBeanState(config, ois);
            } finally {
                ois.close();
            }
            ObjectName configName = configurationManager.load(config, classLoader.getResource("/"));
            kernel.startRecursiveGBean(configName);

            // load and start the configurations listested in the manifest
            for (Iterator iterator = manifest.getConfigurations().iterator(); iterator.hasNext();) {
                URI configurationID = (URI) iterator.next();
                ObjectName configurationName = configurationManager.load(configurationID);
                kernel.startRecursiveGBean(configurationName);
            }

            log.info("Server startup completed");

            // invoke the main method
            kernel.invoke(
                    manifest.getMainGBean(),
                    manifest.getMainMethod(),
                    new Object[]{args},
                    new String[]{String[].class.getName()});

            log.info("Server shutdown begun");

            // stop this configuration
            kernel.stopGBean(configName);

            // shutdown the kernel
            kernel.shutdown();

            log.info("Server shutdown completed");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }

}
