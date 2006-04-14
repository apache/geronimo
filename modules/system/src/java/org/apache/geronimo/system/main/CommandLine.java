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

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoEnvironment;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractName;


/**
 * @version $Rev$ $Date$
 */
public class CommandLine {
    protected static final Log log;

    static {
        // Perform initialization tasks common with the various Geronimo environments.
        GeronimoEnvironment.init();

        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.ERROR);
        log = LogFactory.getLog(CommandLine.class.getName());
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
    private AbstractName configurationName;

    /**
     * @deprecated use the next one with AbstractName
     *
     * @param configurations
     * @param mainGBean
     * @param mainMethod
     * @param args
     * @throws Exception
     */
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

    public void invokeMainGBean(List configurations, AbstractName mainGBean, String mainMethod, String[] args) throws Exception {
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
        ClassLoader classLoader = CommandLine.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream("META-INF/config.ser");

        // boot the kernel
        kernel = KernelFactory.newInstance().createKernel("geronimo");
        kernel.boot();

        // load the configuration
        configurationName = ConfigurationUtil.loadBootstrapConfiguration(kernel, in, classLoader);

        // load and start the configurations
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Iterator i = configurations.iterator(); i.hasNext();) {
                Artifact configID = (Artifact) i.next();
                configurationManager.loadConfiguration(configID);
                configurationManager.startConfiguration(configID);
            }
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }
    }

    protected Kernel getKernel() {
        return kernel;
    }

    protected void stopKernel() throws GBeanNotFoundException, InternalKernelException {
        // stop this configuration
        kernel.stopGBean(configurationName);

        // shutdown the kernel
        kernel.shutdown();
    }
}
