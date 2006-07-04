/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * This starts a geronimo server in vm.  It might not have ever been used.
 *
 * @goal startServer
 * 
 * @version $Rev:$ $Date:$
 *  
 */
public class StartServerMojo extends AbstractModuleMojo {

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);
    }

    /**
     * @parameter
     */
    private String geronimoHome;

    /**
     * @parameter
     */
    private String kernelName;

    /**
     * @parameter
     */
    private String domainName;

    /**
     * @parameter
     */
    private String[] configs;

    private PrintStream logStream = System.out;

    private PrintStream resultStream;

    private final String goalName = "Start Server";

    public void execute() throws MojoExecutionException {
        resultStream = getResultsStream();
        logStream = getLogStream(goalName);

        try {
            startServer();
        }
        catch (Exception e) {
            logResults(resultStream, goalName, "fail");
            handleError(e, logStream);
            return;
        }
        logResults(resultStream, goalName, "success");
    }

    /**
     * @throws MojoExecutionException
     */
    private void startServer() throws Exception {
        System.setProperty("org.apache.geronimo.base.dir", this.geronimoHome);
        List configList = new ArrayList();
        if (this.configs != null && this.configs.length > 0) {
            for (int i=0; i < this.configs.length; i++) {
                try {
                    configList.add(new URI(this.configs[i]));
                }
                catch (Exception e) {
                    throw e;
                }
            }
        }
        File root = new File(this.geronimoHome);
        if (!root.exists())
            throw new Exception(root.getAbsolutePath() + " does not exist");

        URL systemURL = null;
        URL configURL = null;
        try {
            systemURL = new File(root, "bin/server.jar").toURL();
            configURL = new URL("jar:" + systemURL.toString() + "!/META-INF/config.ser");
        }
        catch (Exception e) {
            throw e;
        }
        GBeanData configuration = new GBeanData();
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(configURL.openStream());
            configuration.readExternal(ois);
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            try {
                ois.close();
            }
            catch (Exception e1) {
                throw e1;
            }
        }
        URI configurationId = (URI) configuration.getAttribute("id");
        ObjectName configName = null;
        try {
            configName = Configuration.getConfigurationObjectName(configurationId);
        }
        catch (Exception e) {
            throw e;
        }
        configuration.setName(configName);
        configuration.setAttribute("baseURL", systemURL);

        // build a basic kernel without a configuration-store, our configuration
        // store is
        Kernel kernel = KernelFactory.newInstance().createKernel(this.kernelName);
        try {
            kernel.boot();
            kernel.loadGBean(configuration, this.getClass().getClassLoader());
            kernel.startGBean(configName);
            kernel.invoke(configName, "loadGBeans", new Object[] { null}, new String[] { ManageableAttributeStore.class.getName()});
            kernel.invoke(configName, "startRecursiveGBeans");
        }
        catch (Exception e) {
            throw e;
        }

        // load the rest of the configuration listed on the command line
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Iterator i = configList.iterator(); i.hasNext();) {
                URI configID = (URI) i.next();
                List list = configurationManager.loadRecursive(configID);
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    URI name = (URI) iterator.next();
                    configurationManager.loadGBeans(name);
                    configurationManager.start(name);
                    logStream.println("started gbean: " + name);
                }
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }
    }
}
