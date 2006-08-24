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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * This starts a geronimo server in vm.  It might not have ever been used.
 *
 * @goal startServer
 * 
 * @version $Rev$ $Date$
 */
public class StartServerMojo extends AbstractModuleMojo {

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);
    }

    /**
     * @parameter expression="${basedir}/target"
     */
    private File geronimoHome;

    /**
     * @parameter
     */
    private String domainName;

    /**
     * @parameter
     */
    private String[] configs;

    private String kernelName;

    protected void doExecute() throws Exception {
        if (!geronimoHome.exists()) {
            throw new RuntimeException("No such directory: " + geronimoHome);
        }
        System.setProperty("org.apache.geronimo.base.dir", this.geronimoHome.getAbsolutePath());

        List configList = new ArrayList();
        if (this.configs != null && this.configs.length > 0) {
            for (int i=0; i < this.configs.length; i++) {
                configList.add(Artifact.create(this.configs[i]));
            }
        }

        URL systemURL = new File(geronimoHome, "bin/server.jar").toURL();
        URL configURL = new URL("jar:" + systemURL.toString() + "!/META-INF/config.ser");

        GBeanData configuration = new GBeanData();
        ObjectInputStream ois = new ObjectInputStream(configURL.openStream());
        configuration.readExternal(ois);

        URI configurationId = (URI) configuration.getAttribute("id");
        AbstractName abstractName = new AbstractName(configurationId);

        configuration.setAbstractName(abstractName);
        configuration.setAttribute("baseURL", systemURL);

        // build a basic kernel without a configuration-store, our configuration store is
        Kernel kernel = KernelFactory.newInstance().createKernel(this.kernelName);
        kernel.boot();

        kernel.loadGBean(configuration, this.getClass().getClassLoader());
        kernel.startGBean(abstractName);
        kernel.invoke(abstractName, "loadGBeans", new Object[] { null }, new String[] { ManageableAttributeStore.class.getName() });
        kernel.invoke(abstractName, "startRecursiveGBeans");

        // load the rest of the configuration listed on the command line
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Iterator i = configList.iterator(); i.hasNext();) {
                Artifact configID = (Artifact) i.next();
                configurationManager.loadConfiguration(configID);
                configurationManager.startConfiguration(configID);

                log.info("Started GBean: " + configID);
            }
        }
        finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }
    }
}
