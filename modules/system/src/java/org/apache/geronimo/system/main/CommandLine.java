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

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.url.GeronimoURLFactory;


/**
 * @version $Revision: 1.6 $ $Date: 2004/04/05 05:54:11 $
 */
public class CommandLine {
    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.ERROR);

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

            // invoke the main method
            kernel.invoke(
                    manifest.getMainGBean(),
                    manifest.getMainMethod(),
                    new Object[]{args},
                    new String[]{String[].class.getName()});

            // stop this configuration
            kernel.stopGBean(configName);

            // shutdown the kernel
            kernel.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }

}
