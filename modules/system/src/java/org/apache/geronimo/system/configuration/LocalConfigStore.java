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
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * Implementation of ConfigurationStore using the local filesystem.
 *
 * @version $Rev$ $Date$
 */
public class LocalConfigStore implements ConfigurationStore, GBeanLifecycle {
    private static final String INDEX_NAME = "index.properties";
    private static final String BACKUP_NAME = "index.backup";
    private static final String DELETE_NAME = "index.delete";
    private final int REAPER_INTERVAL = 60 * 1000;
    private final Kernel kernel;
    private final ObjectName objectName;
    private final URI root;
    private final ServerInfo serverInfo;
    private final Properties index = new Properties();
    private final Properties pendingDeletionIndex = new Properties();
    private ConfigStoreReaper reaper;
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
        this.rootDir = rootDir;
        log = LogFactory.getLog("LocalConfigStore:" + rootDir.getName());
    }

    public LocalConfigStore(Kernel kernel, String objectName, URI root, ServerInfo serverInfo) throws MalformedObjectNameException {
        this.kernel = kernel;
        this.objectName = new ObjectName(objectName);
        this.root = root;
        this.serverInfo = serverInfo;
        log = LogFactory.getLog("LocalConfigStore:" + root.toString());
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
        InputStream indexIs = null;
        try {
            indexIs = new BufferedInputStream(new FileInputStream(indexfile));
            index.load(indexIs);
            for (Iterator i = index.values().iterator(); i.hasNext();) {
                String id = (String) i.next();
                maxId = Math.max(maxId, Integer.parseInt(id));
            }
        } catch (FileNotFoundException e) {
            maxId = 0;
        } finally {
            if (indexIs != null)
                indexIs.close();
        }

        // See if there are old directories which we should clean up...
        File pendingDeletionFile = new File(rootDir, DELETE_NAME);
        InputStream pendingIs = null;
        try {
            pendingIs = new BufferedInputStream(new FileInputStream(pendingDeletionFile));
            pendingDeletionIndex.load(pendingIs);
        } catch (FileNotFoundException e) {
            // may not be one...
        } finally {
            if (pendingIs != null)
                pendingIs.close();
        }

        // Create and start the reaper...
        reaper = new ConfigStoreReaper(REAPER_INTERVAL);
        Thread t = new Thread(reaper, "Geronimo Config Store Reaper");
        t.setDaemon(true);
        t.start();
    }

    public void doStop() {
        if (reaper != null) {
            reaper.close();
        }
    }

    public void doFail() {
        if (reaper != null) {
            reaper.close();
        }
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

    private void saveDeleteIndex() throws IOException {
        File deleteFile = new File(rootDir, DELETE_NAME);

        FileOutputStream fos = new FileOutputStream(deleteFile);
        try {
            BufferedOutputStream os = new BufferedOutputStream(fos);
            pendingDeletionIndex.store(os, null);
            os.close();
            fos = null;
        } catch (IOException e) {
            if (fos != null) {
                fos.close();
            }
            throw e;
        }
    }

    /**
     * we don't use the configId to locate the target directory.  Some callers send null.
     *
     * @param configId
     * @return directory to put the new configuration into, unpacked.
     */
    public File createNewConfigurationDir(Artifact configId) {
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
        // create the meta-inf dir
        File metaInf = new File(configurationDir, "META-INF");
        metaInf.mkdirs();
        return configurationDir;
    }

    public URL resolve(Artifact configId, URI uri) throws NoSuchConfigException, MalformedURLException {
        return new URL(getRoot(configId).toURL(), uri.toString());
    }

    public Artifact install(URL source) throws IOException, InvalidConfigException {
        return (Artifact) install2(source).getAttribute("id");
    }

    public GBeanData install2(URL source) throws IOException, InvalidConfigException {
        //this  implementation doesn't use the artifactId to locate the target
        File configurationDir = createNewConfigurationDir(null);

        InputStream is = source.openStream();
        try {
            unpack(configurationDir, is);
        } catch (IOException e) {
            delete(configurationDir);
            throw e;
        } finally {
            is.close();
        }

        Artifact configId;
        GBeanData config;
        try {
            config = loadConfig(configurationDir);
            configId = (Artifact) config.getAttribute("id");
            index.setProperty(configId.toURI().toString(), configurationDir.getName());
        } catch (Exception e) {
            delete(configurationDir);
            throw new InvalidConfigException("Unable to get ID from downloaded configuration", e);
        }

        synchronized (this) {
            saveIndex();
        }

        log.debug("Installed configuration (URL) " + configId + " in location " + configurationDir.getName());
        return config;
    }

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        File source = configurationData.getConfigurationDir();
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

        log.debug("Installed configuration (file) " + configurationData.getId() + " in location " + source.getName());
    }

    public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
        String id = null;
        try {
            id = configID.toURI().toString();
        } catch (URISyntaxException e) {
            throw new NoSuchConfigException(e);
        }
        File configDir;
        String storeID;
        synchronized (this) {
            storeID = index.getProperty(id);
            if (storeID == null) {
                throw new NoSuchConfigException();
            }
            configDir = new File(rootDir, storeID);
            index.remove(id);
            saveIndex();
        }

        delete(configDir);

        // On windoze, any open file descriptor (e.g. a MultiParentClassLoader) will prevent
        // the directory/files from being deleted. If we're unable to delete, save the directory
        // to the pendingDeletionIndex. ConfigStoreReaper will delete when the classloader has been GC'ed.
        if (!configDir.exists()) {
            log.debug("Uninstalled configuration " + configID);
        } else {
            log.debug("Uninstalled configuration, but could not delete ConfigStore directory for " + configID);
            synchronized (pendingDeletionIndex) {
                pendingDeletionIndex.setProperty(configDir.toString(), id);
                saveDeleteIndex();
            }
        }
    }

    public synchronized ObjectName loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        GBeanData config = loadConfig(getRoot(configId));

        ObjectName name = Configuration.getConfigurationObjectName(configId);
        config.setName(name);
        //TODO configId remove this
        config.setAttribute("baseURL", getRoot(configId).toURL());

        try {
            kernel.loadGBean(config, Configuration.class.getClassLoader());
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to register configuration", e);
        }

        log.debug("Loaded Configuration " + name);

        return name;
    }

    public List listConfigurations() {
        List configs;
        synchronized (this) {
            configs = new ArrayList(index.size());
            for (Iterator i = index.keySet().iterator(); i.hasNext();) {
                Artifact configId = Artifact.create((String) i.next());
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
                    log.warn("Unable get configuration info for configuration " + configId, e);
                }
            }
        }
        return configs;
    }


    public synchronized boolean containsConfiguration(Artifact configID) {
        try {
            return index.getProperty(configID.toURI().toString()) != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private synchronized File getRoot(Artifact configID) throws NoSuchConfigException {
        String id = null;
        id = index.getProperty(configID.toString());
        if (id == null) {
            throw new NoSuchConfigException("No such config: " + configID);
        }
        return new File(rootDir, id);
    }

    private GBeanData loadConfig(File configRoot) throws IOException, InvalidConfigException {
        File file = new File(configRoot, "META-INF/config.ser");
        if (!file.isFile()) {
            throw new InvalidConfigException("Configuration does not contain a META-INF/config.ser file");
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
        if (null == files) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                delete(file);
            } else {
                file.delete();
            }
        }
        root.delete();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LocalConfigStore.class, "ConfigurationStore"); //NameFactory.CONFIGURATION_STORE

        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("root", URI.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addInterface(ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{"kernel", "objectName", "root", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     * Thread to cleanup unused Config Store entries.
     * On Windows, open files can't be deleted. Until MultiParentClassLoaders
     * are GC'ed, we won't be able to delete Config Store directories/files.
     */
    class ConfigStoreReaper implements Runnable {
        private final int reaperInterval;
        private volatile boolean done = false;

        public ConfigStoreReaper(int reaperInterval) {
            this.reaperInterval = reaperInterval;
        }

        public void close() {
            this.done = true;
        }

        public void run() {
            log.debug("ConfigStoreReaper started");
            while (!done) {
                try {
                    Thread.sleep(reaperInterval);
                } catch (InterruptedException e) {
                    continue;
                }
                reap();
            }
        }

        /**
         * For every directory in the pendingDeletionIndex, attempt to delete all
         * sub-directories and files.
         */
        public void reap() {
            // return, if there's nothing to do
            if (pendingDeletionIndex.size() == 0)
                return;
            // Otherwise, attempt to delete all of the directories
            Enumeration list = pendingDeletionIndex.propertyNames();
            boolean dirDeleted = false;
            while (list.hasMoreElements()) {
                String dirName = (String) list.nextElement();
                File deleteFile = new File(dirName);
                try {
                    delete(deleteFile);
                }
                catch (IOException ioe) { // ignore errors
                }
                if (!deleteFile.exists()) {
                    String configName = pendingDeletionIndex.getProperty(dirName);
                    pendingDeletionIndex.remove(dirName);
                    dirDeleted = true;
                    log.debug("Reaped configuration " + configName + " in directory " + dirName);
                }
            }
            // If we deleted any directories, persist the list of directories to disk...
            if (dirDeleted) {
                try {
                    synchronized (pendingDeletionIndex) {
                        saveDeleteIndex();
                    }
                }
                catch (IOException ioe) {
                    log.warn("Error saving " + DELETE_NAME + " file.", ioe);
                }
            }
        }
    }
}
