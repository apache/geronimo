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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * GBean that saves a list of configurations, for example to allow
 * a server to restart automatically.
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/05 14:38:00 $
 */
public class FileConfigurationList implements GBeanLifecycle, PersistentConfigurationList {
    private static final Log log = LogFactory.getLog(PersistentConfigurationList.class);
    private final Kernel kernel;
    private final ServerInfo serverInfo;
    private final String configFile;
    private File configList;
    private Runnable hook;

    public FileConfigurationList(Kernel kernel, ServerInfo serverInfo, String configDir) {
        this.kernel = kernel;
        this.serverInfo = serverInfo;
        this.configFile = configDir;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        configList = serverInfo.resolve(configFile);
        File parent = configList.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory for list:" + parent);
            }
        }
        hook = new Runnable() {
            public void run() {
                try {
                    save();
                } catch (IOException e) {
                    log.error("Unable to save configuration on shutdown", e);
                }
            }
        };
        kernel.registerShutdownHook(hook);
    }

    public void doStop() throws Exception {
        doFail();
    }

    public void doFail() {
        kernel.unregisterShutdownHook(hook);
        hook = null;
        configList = null;
    }

    public synchronized void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(configList));
        try {
            List stores = kernel.listConfigurationStores();
            for (Iterator i = stores.iterator(); i.hasNext();) {
                ObjectName storeName = (ObjectName) i.next();
                List configList = kernel.listConfigurations(storeName);
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(FileConfigurationList.class);
        infoFactory.addInterface(PersistentConfigurationList.class);
        infoFactory.addReference("Kernel", Kernel.class);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addAttribute("ConfigFile", String.class, true);
        infoFactory.setConstructor(new String[]{"Kernel", "ServerInfo", "ConfigFile"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
