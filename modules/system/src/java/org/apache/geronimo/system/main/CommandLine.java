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
import java.util.List;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.url.GeronimoURLFactory;


/**
 * @version $Rev$ $Date$
 */
public class CommandLine {
    protected static final Log log;

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.ERROR);
        log = LogFactory.getLog(CommandLine.class.getName());

        // Install our url factory
        GeronimoURLFactory.install();
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
            List configurations = manifest.getConfigurations();
            ObjectName mainGBean = manifest.getMainGBean();
            String mainMethod = manifest.getMainMethod();

            new CommandLine().invokeMainGBean(configurations, mainGBean, mainMethod, args);

            log.info("Server shutdown completed");
        } catch (Exception e) {
            ExceptionUtil.trimStackTrace(e);
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }

    private Kernel kernel;
    private GBeanData config;

    public void invokeMainGBean(List configurations, ObjectName mainGBean, String mainMethod, String[] args) throws Exception {
        startKernel(configurations);

        log.info("Server startup completed");

        // invoke the main method
        kernel.invoke(
                mainGBean,
                mainMethod,
                new Object[]{args},
                new String[]{String[].class.getName()});

        log.info("Server shutdown begun");

        stopKernel();
    }

    protected void startKernel(List configurations) throws Exception {
        // boot the kernel
        kernel = new Kernel("geronimo");
        kernel.boot();

        // load and start the configuration in this jar
        ConfigurationManager configurationManager = kernel.getConfigurationManager();
        config = new GBeanData();
        ClassLoader classLoader = CommandLine.class.getClassLoader();
        ObjectInputStream ois = new ObjectInputStream(classLoader.getResourceAsStream("META-INF/config.ser"));
        try {
            config.readExternal(ois);
        } finally {
            ois.close();
        }
        configurationManager.load(config, classLoader.getResource("/"), classLoader);
        kernel.startRecursiveGBean(config.getName());

        // load and start the configurations
        for (Iterator i = configurations.iterator(); i.hasNext();) {
            URI configID = (URI) i.next();
            List list = configurationManager.loadRecursive(configID);
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                ObjectName name = (ObjectName) iterator.next();
                kernel.startRecursiveGBean(name);
            }
        }
    }

    protected Kernel getKernel() {
        return kernel;
    }

    protected void stopKernel() throws GBeanNotFoundException, InternalKernelException {
        // stop this configuration
        kernel.stopGBean(config.getName());

        // shutdown the kernel
        kernel.shutdown();
    }
}
