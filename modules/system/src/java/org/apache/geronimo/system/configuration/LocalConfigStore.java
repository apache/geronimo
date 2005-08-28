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

package org.apache.geronimo.system.configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * Implementation of ConfigurationStore using the local filesystem.
 *
 * @version $Rev$ $Date$
 */
public class LocalConfigStore implements ConfigurationStore, GBeanLifecycle {
    private static final String INDEX_NAME = "index.properties";
    private static final String BACKUP_NAME = "index.backup";
    private final Kernel kernel;
    private final ObjectName objectName;
    private final URI root;
    private final ManageableAttributeStore attributeStore;
    private final ServerInfo serverInfo;
    private final Properties index = new Properties();
    private final Log log;
    private File rootDir;
    private int maxId;

    /**
     * Constructor is only used for direct testing with out a kernel.
     */
    public LocalConfigStore(File rootDir) {
        kernel = null;
        objectName = null;
        serverInfo = null;
        this.root = null;
        this.attributeStore = null;
        this.rootDir = rootDir;
        log = LogFactory.getLog("LocalConfigStore:"+rootDir.getName());
    }

    public LocalConfigStore(Kernel kernel, String objectName, URI root, ServerInfo serverInfo, ManageableAttributeStore attributeStore) throws MalformedObjectNameException {
        this.kernel = kernel;
        this.objectName = new ObjectName(objectName);
        this.root = root;
        this.serverInfo = serverInfo;
        this.attributeStore = attributeStore;
        log = LogFactory.getLog("LocalConfigStore:"+root.toString());
    }

    public String getObjectName() {
        return objectName.toString();
    }

    public synchronized void doStart() throws FileNotFoundException, IOException {
        // resolve the root dir if not alredy resolved
        if (rootDir == null) {
            if (serverInfo == null) {
                rootDir = new File(root);
            } else {
                rootDir = new File(serverInfo.resolve(root));
            }
            if (!rootDir.isDirectory()) {
                throw new FileNotFoundException("Store root does not exist or is not a directory: " + rootDir);
            }
        }

        index.clear();
        File indexfile = new File(rootDir, INDEX_NAME);
        try {
            index.load(new BufferedInputStream(new FileInputStream(indexfile)));
            for (Iterator i = index.values().iterator(); i.hasNext();) {
                String id = (String) i.next();
                maxId = Math.max(maxId, Integer.parseInt(id));
            }
        } catch (FileNotFoundException e) {
            maxId = 0;
        }
    }

    public void doStop() {
    }

    public void doFail() {
    }

    private void saveIndex() throws IOException {
        // todo provide a backout mechanism
        File indexFile = new File(rootDir, INDEX_NAME);
        File backupFile = new File(rootDir, BACKUP_NAME);
        if (backupFile.exists()) {
            backupFile.delete();
        }
        indexFile.renameTo(backupFile);

        FileOutputStream fos = new FileOutputStream(indexFile);
        try {
            BufferedOutputStream os = new BufferedOutputStream(fos);
            index.store(os, null);
            os.close();
            fos = null;
        } catch (IOException e) {
            if (fos != null) {
                fos.close();
            }
            indexFile.delete();
            backupFile.renameTo(indexFile);
            throw e;
        }
    }

    public File createNewConfigurationDir() {
        // loop until we find a directory that doesn't alredy exist
        // this can happen when a deployment fails (leaving an bad directory)
        // and the server reboots without saving out the index.propreties file
        // the is rare but we should check for it
        File configurationDir;
        do {
            String newId;
            synchronized (this) {
                newId = Integer.toString(++maxId);
            }
            configurationDir = new File(rootDir, newId);
        } while (configurationDir.exists());
        configurationDir.mkdir();
        return configurationDir;
    }

    public URI install(URL source) throws IOException, InvalidConfigException {
        File configurationDir = createNewConfigurationDir();

        InputStream is = source.openStream();
        try {
            unpack(configurationDir, is);
        } catch (IOException e) {
            delete(configurationDir);
            throw e;
        } finally {
            is.close();
        }

        URI configId;
        try {
            GBeanData config = loadConfig(configurationDir);
            configId = (URI) config.getAttribute("id");
            index.setProperty(configId.toString(), configurationDir.getName());
        } catch (Exception e) {
            delete(configurationDir);
            throw new InvalidConfigException("Unable to get ID from downloaded configuration", e);
        }

        synchronized (this) {
            saveIndex();
        }

        log.info("Installed configuration " + configId + " in location " + configurationDir.getName());
        return configId;
    }

    public void install(ConfigurationData configurationData, File source) throws IOException, InvalidConfigException {
        if (!source.isDirectory()) {
            throw new InvalidConfigException("Source must be a directory: source=" + source);
        }
        if (!source.getParentFile().equals(rootDir)) {
            throw new InvalidConfigException("Source must be within the config store: source=" + source + ", configStoreDir=" + rootDir);
        }

        ExecutableConfigurationUtil.writeConfiguration(configurationData, source);

        // update the index
        synchronized (this) {
            index.setProperty(configurationData.getId().toString(), source.getName());
            saveIndex();
        }

        log.info("Installed configuration " + configurationData.getId() + " in location " + source.getName());
    }

    public void uninstall(URI configID) throws NoSuchConfigException, IOException {
        String id = configID.toString();
        File configDir;
        synchronized(this) {
            String storeID = index.getProperty(id);
            if (storeID == null) {
                throw new NoSuchConfigException();
            }
            configDir = new File(rootDir, storeID);
            File tempDir = new File(rootDir, storeID + ".tmp");
            if (configDir.renameTo(tempDir)) {
                configDir = tempDir;
            }
            index.remove(id);
            saveIndex();
        }
        log.info("Uninstalled configuration " + configID);
        delete(configDir);
    }

    public synchronized ObjectName loadConfiguration(URI configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        GBeanData config = loadConfig(getRoot(configId));

        ObjectName name;
        try {
            name = Configuration.getConfigurationObjectName(configId);
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Cannot convert id to ObjectName: ", e);
        }
        config.setName(name);
        ObjectName pattern;
        try {
            pattern = attributeStore == null ? null : new ObjectName(attributeStore.getObjectName());
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Invalid ObjectName for AttributeStore: " + attributeStore.getObjectName());
        }
        config.setReferencePattern("AttributeStore", pattern);

        try {
            kernel.loadGBean(config, Configuration.class.getClassLoader());
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to register configuration", e);
        }

        try {
            kernel.setAttribute(name, "baseURL", getRoot(configId).toURL());
        } catch (Exception e) {
            try {
                kernel.unloadGBean(name);
            } catch (Exception ignored) {
                // ignore
            }
            throw new InvalidConfigException("Cannot set baseURL", e);
        }
        log.info("Loaded Configuration " + name);

        return name;
    }

    public synchronized void updateConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, Exception {
        File root = getRoot(configurationData.getId());
        File stateFile = new File(root, "META-INF/state.ser");
        try {
            FileOutputStream fos = new FileOutputStream(stateFile);
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(fos);
                GBeanData configurationGBeanData = ExecutableConfigurationUtil.getConfigurationGBeanData(configurationData);
                configurationGBeanData.writeExternal(oos);
                oos.flush();
            } finally {
                fos.close();
            }
        } catch (Exception e) {
            log.error("state store failed", e);
            stateFile.delete();
            throw e;
        }
    }

    public List listConfiguations() {
        List configs;
        synchronized (this) {
            configs = new ArrayList(index.size());
            for (Iterator i = index.keySet().iterator(); i.hasNext();) {
                URI configId = URI.create((String) i.next());
                try {
                    ObjectName configName = Configuration.getConfigurationObjectName(configId);
                    State state;
                    if (kernel.isLoaded(configName)) {
                        try {
                            state = State.fromInt(kernel.getGBeanState(configName));
                        } catch (Exception e) {
                            state = null;
                        }
                    } else {
                        // If the configuration is not loaded by the kernel
                        // and defined by the store, then it is stopped.
                        state = State.STOPPED;
                    }

                    GBeanData bean = loadConfig(getRoot(configId));
                    ConfigurationModuleType type = (ConfigurationModuleType) bean.getAttribute("type");

                    configs.add(new ConfigurationInfo(objectName, configId, state, type));
                } catch (Exception e) {
                    // bad configuration in store - ignored for this purpose
                    log.info("Unable get configuration info for configuration " + configId, e);
                }
            }
        }
        return configs;
    }

    public synchronized boolean containsConfiguration(URI configID) {
        return index.getProperty(configID.toString()) != null;
    }

    private synchronized File getRoot(URI configID) throws NoSuchConfigException {
        String id = index.getProperty(configID.toString());
        if (id == null) {
            throw new NoSuchConfigException("No such config: " + configID);
        }
        return new File(rootDir, id);
    }

    private GBeanData loadConfig(File configRoot) throws IOException, InvalidConfigException {
        File file = new File(configRoot, "META-INF/state.ser");
        if (!file.isFile()) {
            file = new File(configRoot, "META-INF/config.ser");
            if (!file.isFile()) {
                throw new InvalidConfigException("Configuration does not contain a META-INF/config.ser file");
            }
        }

        FileInputStream fis = new FileInputStream(file);
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fis));
            GBeanData config = new GBeanData();
            try {
                config.readExternal(ois);
            } catch (ClassNotFoundException e) {
                //TODO more informative exceptions
                throw new InvalidConfigException("Unable to read attribute ", e);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to set attribute ", e);
            }
            config.setReferencePattern("ConfigurationStore", objectName);
            return config;
        } finally {
            fis.close();
        }
    }

    public static void unpack(File to, InputStream from) throws IOException {
        ZipInputStream zis = new ZipInputStream(from);
        try {
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zis.getNextEntry()) != null) {
                File out = new File(to, entry.getName());
                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    if (!entry.getName().equals("META-INF/startup-jar")) {
                        out.getParentFile().mkdirs();
                        OutputStream os = new FileOutputStream(out);
                        try {
                            int count;
                            while ((count = zis.read(buffer)) > 0) {
                                os.write(buffer, 0, count);
                            }
                        } finally {
                            os.close();
                        }
                        zis.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            delete(to);
            throw e;
        }
    }

    private static void delete(File root) throws IOException {
        File[] files = root.listFiles();
        if ( null == files ) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                delete(file);
            } else {
                if (!file.delete()) {
                    file.deleteOnExit();
                };
            }
        }
        root.delete();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(LocalConfigStore.class, "ConfigurationStore"); //NameFactory.CONFIGURATION_STORE

        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("root", URI.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("AttributeStore", ManageableAttributeStore.class, "AttributeStore");
        infoFactory.addInterface(ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{"kernel", "objectName", "root", "ServerInfo", "AttributeStore"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
