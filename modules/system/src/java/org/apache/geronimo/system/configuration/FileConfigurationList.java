/**
 *
 *  Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.system.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * GBean that saves a list of configurations, for example to allow
 * a server to restart automatically.
 *
 * @version $Rev$ $Date$
 */
public class FileConfigurationList implements GBeanLifecycle, PersistentConfigurationList {
    private static final Log log = LogFactory.getLog(PersistentConfigurationList.class);

    /**
     * The kernel for which we are persisting the configuration list.
     */
    private final Kernel kernel;

    /**
     * The ConfigurationManager for the kernel
     */
    private final ConfigurationManager configurationManager;

    /**
     * Used to resolve the location of the configuration file.
     */
    private final ServerInfo serverInfo;

    /**
     * The file to which we are saving the configurations.  This is relative to the
     * server base directory in server info.
     */
    private final String configFile;

    /**
     * Is the kernel fully started?  Until the kernel is fully started, we will
     * not write out a new configuration list.  This stops a crtl^c during start up
     * from completely overwriting the startup file with no content.
     */
    private boolean kernelFullyStarted = false;

    /**
     * The actual absolute file where we write the configuration list.
     */
    private File configList;

    /**
     * Listener to make this GBean save every time a configuration is started
     * or stopped.
     */
    private LifecycleListener listener;

    public FileConfigurationList(Kernel kernel, ServerInfo serverInfo, ConfigurationManager configurationManager, String configDir) {
        this.kernel = kernel;
        this.configurationManager = configurationManager;
        this.serverInfo = serverInfo;
        this.configFile = configDir;
        this.listener = new LifecycleAdapter() {
            public void stopped(ObjectName objectName) {
                if(kernelFullyStarted && FileConfigurationList.this.kernel.isRunning()) {
                    doSave();
                }
            }

            public void running(ObjectName objectName) {
                if(kernelFullyStarted && FileConfigurationList.this.kernel.isRunning()) {
                    doSave();
                }
            }

            private void doSave() {
                try {
                    save();
                } catch (IOException e) {
                    log.error("Unable to save list of running configurations", e);
                }
            }
        };
    }


    public void doStart() throws Exception {
        configList = serverInfo.resolve(configFile);
        File parent = configList.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory for list:" + parent);
            }
        }
        kernel.getLifecycleMonitor().addLifecycleListener(listener, new ObjectName("geronimo.config:*"));
    }

    public void doStop() throws Exception {
        doFail();
    }

    public void doFail() {
        kernel.getLifecycleMonitor().removeLifecycleListener(listener);
        configList = null;
    }

    public synchronized boolean isKernelFullyStarted() {
        return kernelFullyStarted;
    }

    public synchronized void setKernelFullyStarted(boolean kernelFullyStarted) {
        this.kernelFullyStarted = kernelFullyStarted;
    }

    public synchronized void save() throws IOException {
        if (!kernelFullyStarted) {
            log.debug("Configuration list was not saved.  Kernel was never fully started.");
            return;
        }
        if (!kernel.isRunning()) {
            log.debug("Configuration list was not saved.  Kernel is shutting down.");
            return;
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(configList));
        try {
            List stores = configurationManager.listStores();
            for (Iterator i = stores.iterator(); i.hasNext();) {
                ObjectName storeName = (ObjectName) i.next();
                List configList = configurationManager.listConfigurations(storeName);
                for (Iterator j = configList.iterator(); j.hasNext();) {
                    ConfigurationInfo info = (ConfigurationInfo) j.next();
                    if (info.getState() == State.RUNNING) {
                        writer.write(info.getConfigID().toString());
                        writer.newLine();
                    }
                }
            }
            writer.close();
        } catch (NoSuchStoreException e) {
            writer.close();
            configList.delete();
        }
        log.info("Saved running configuration list");
    }

    public List restore() throws IOException {
        FileReader fileReader;
        try {
            fileReader = new FileReader(configList);
        } catch (FileNotFoundException e) {
            return Collections.EMPTY_LIST;
        }
        BufferedReader reader = new BufferedReader(fileReader);
        try {
            List configs = new ArrayList();
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    configs.add(new URI(line));
                } catch (URISyntaxException e) {
                    throw new IOException("Invalid URI in config list: " + line);
                }
            }
            return configs;
        } finally {
            reader.close();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(FileConfigurationList.class, "PersistentConfigurationList");
        infoFactory.addInterface(PersistentConfigurationList.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("kernelFullyStarted", boolean.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("ConfigurationManager", ConfigurationManager.class, "ConfigurationManager");
        infoFactory.addAttribute("configFile", String.class, true);
        infoFactory.setConstructor(new String[]{"kernel", "ServerInfo", "ConfigurationManager", "configFile"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
