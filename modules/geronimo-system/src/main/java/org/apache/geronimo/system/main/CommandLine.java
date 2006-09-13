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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Enumeration;
import java.util.Set;
import java.util.Collection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoEnvironment;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;


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
            AbstractNameQuery mainGBean = manifest.getMainGBeanQuery();
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

    public void invokeMainGBean(List configurations, AbstractNameQuery mainGBeanQuery, String mainMethod, String[] args) throws Exception {
        startKernel();
        Runtime.getRuntime().addShutdownHook(new Thread("Geronimo shutdown thread") {
            public void run() {
                log.info("Server shutdown begun");
                try {
                    stopKernel();
                } catch (GBeanNotFoundException e) {

                }
            }
        });
        loadConfigurations(configurations);

        log.info("Server startup completed");
        Set matches = kernel.listGBeans(mainGBeanQuery);
        if (matches.isEmpty()) {
            throw new Exception("No match for AbstractNameQuery: " + mainGBeanQuery);
        }
        if (matches.size() > 1) {
            throw new Exception("Ambiguous AbstractNameQuery: " + mainGBeanQuery + " matches: " + matches);
        }
        AbstractName mainGBean = (AbstractName) matches.iterator().next();

        // invoke the main method
        kernel.invoke(
                mainGBean,
                mainMethod,
                new Object[]{args},
                new String[]{String[].class.getName()});

    }

    protected void startKernel() throws Exception {
        ClassLoader classLoader = CommandLine.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream("META-INF/config.ser");
        try {
            // boot the kernel
            kernel = KernelFactory.newInstance().createKernel("geronimo");
            kernel.boot();
    
            // load the configuration
            configurationName = ConfigurationUtil.loadBootstrapConfiguration(kernel, in, classLoader);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    protected void startKernel(Artifact moduleId) throws Exception {
        // boot the kernel
        kernel = KernelFactory.newInstance().createKernel("geronimo");
        kernel.boot();
        ClassLoader classLoader = CommandLine.class.getClassLoader();
        for (Enumeration modules = classLoader.getResources("META-INF/config.ser"); modules.hasMoreElements(); ) {
            URL moduleDataURL = (URL) modules.nextElement();
            InputStream in = moduleDataURL.openStream();
            try {
                ConfigurationData moduleData = ConfigurationUtil.readConfigurationData(in);
                if (moduleId.matches(moduleData.getId())) {
                    // load the configuration
                    configurationName = ConfigurationUtil.loadBootstrapConfiguration(kernel, moduleData, classLoader);
                    return;
                }
            } finally {
                in.close();
            }
        }
        throw new NoSuchConfigException(moduleId);
    }

    protected void loadConfigurations(List configurations) throws NoSuchConfigException, LifecycleException, MissingDependencyException {
        // load and start the configurations
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        Collection resolvedConfigurations = configurationManager.getArtifactResolver().resolveInClassLoader(configurations);
        try {
            for (Iterator i = resolvedConfigurations.iterator(); i.hasNext();) {
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
