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

import java.io.ObjectInputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoEnvironment;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.log.GeronimoLogging;


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
    private GBeanData configuration;

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
        // load and start the configuration in this jar
        configuration = new GBeanData();
        ClassLoader classLoader = CommandLine.class.getClassLoader();
        ObjectInputStream ois = new ObjectInputStream(classLoader.getResourceAsStream("META-INF/config.ser"));
        try {
            configuration.readExternal(ois);
        } finally {
            ois.close();
        }
        URI configurationId = (URI) configuration.getAttribute("id");
        ObjectName configName = Configuration.getConfigurationObjectName(configurationId);
        configuration.setName(configName);
        configuration.setAttribute("baseURL", classLoader.getResource("/"));

        // boot the kernel
        kernel = KernelFactory.newInstance().createKernel("geronimo");
        kernel.boot();

        // load this configuration into the kernel
        kernel.loadGBean(configuration, classLoader);
        kernel.startGBean(configName);
        kernel.invoke(configName, "loadGBeans", new Object[] {null}, new String[] {ManageableAttributeStore.class.getName()});
        kernel.invoke(configName, "startRecursiveGBeans");

        // load and start the configurations
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Iterator i = configurations.iterator(); i.hasNext();) {
                URI configID = (URI) i.next();
                List list = configurationManager.loadRecursive(configID);
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    URI name = (URI) iterator.next();
                    configurationManager.loadGBeans(name);
                    configurationManager.start(name);
                }
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
        kernel.stopGBean(configuration.getName());

        // shutdown the kernel
        kernel.shutdown();
    }
}
