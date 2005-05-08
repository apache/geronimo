/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.mavenplugin;

import java.io.File;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.url.GeronimoURLFactory;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class StartServer {

    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);

        // Install our url factory
        GeronimoURLFactory.install();
    }

    private String geronimoHome;
    private String kernelName;
    private String domainName;
    private String configs;

    public String getGeronimoHome() {
        return geronimoHome;
    }

    public void setGeronimoHome(String geronimoHome) {
        this.geronimoHome = geronimoHome;
    }

    public String getKernelName() {
        return kernelName;
    }

    public void setKernelName(String kernelName) {
        this.kernelName = kernelName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getConfigs() {
        return configs;
    }

    public void setConfigs(String configs) {
        this.configs = configs;
    }

    public void execute() throws Exception {
        System.setProperty("geronimo.base.dir", getGeronimoHome());
        List configList = new ArrayList();
        for (StringTokenizer st = new StringTokenizer(configs); st.hasMoreTokens();) {
            configList.add(new URI(st.nextToken()));
        }
        File root = new File(getGeronimoHome());
        URL systemURL = new File(root, "bin/server.jar").toURL();
        URL configURL = new URL("jar:" + systemURL.toString() + "!/META-INF/config.ser");
        GBeanData configuration = new GBeanData();
        ObjectInputStream ois = new ObjectInputStream(configURL.openStream());
        try {
            configuration.readExternal(ois);
        } finally {
            ois.close();
        }
        URI configurationId = (URI) configuration.getAttribute("id");
        ObjectName configName = Configuration.getConfigurationObjectName(configurationId);
        configuration.setName(configName);

        // build a basic kernel without a configuration-store, our configuration store is
        Kernel kernel = KernelFactory.newInstance().createKernel(getKernelName());
        kernel.boot();

        kernel.loadGBean(configuration, this.getClass().getClassLoader());
        kernel.setAttribute(configName, "baseURL", systemURL);
        kernel.startRecursiveGBean(configName);

        // load the rest of the configuration listed on the command line
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        for (Iterator i = configList.iterator(); i.hasNext();) {
            URI configID = (URI) i.next();
            List list = configurationManager.loadRecursive(configID);
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                ObjectName name = (ObjectName) iterator.next();
                kernel.startRecursiveGBean(name);
                System.out.println("started gbean: " + name);
            }
        }


    }
}
