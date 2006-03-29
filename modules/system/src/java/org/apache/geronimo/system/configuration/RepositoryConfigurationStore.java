/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.system.repository.IOUtil;

/**
 * Implementation of ConfigurationStore that loads Configurations from a repository.
 * This implementation is read-only on the assumption that a separate maven task will
 * handle installation of a built package into the repository.
 *
 * @version $Rev: 378459 $ $Date: 2006-02-17 00:37:43 -0800 (Fri, 17 Feb 2006) $
 */
public class RepositoryConfigurationStore implements ConfigurationStore {
    private final Kernel kernel;
    private final ObjectName objectName;
    protected final WritableListableRepository repository;

    public RepositoryConfigurationStore(WritableListableRepository repository) {
        this(null, null, repository);
    }

    public RepositoryConfigurationStore(Kernel kernel, String objectName, WritableListableRepository repository) {
        this.kernel = kernel;
        this.objectName = objectName == null ? null : JMXUtil.getObjectName(objectName);
        this.repository = repository;
    }

    public String getObjectName() {
        return objectName.toString();
    }

    public GBeanData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        File location = repository.getLocation(configId);

        if (!location.exists() && location.canRead()) {
            throw new NoSuchConfigException("Configuration not found: " + configId);
        }

        GBeanData config = new GBeanData();
        try {
            if (location.isDirectory()) {
                File serFile = new File(location, "META-INF");
                serFile = new File(serFile, "config.ser");

                if (!serFile.exists()) {
                    throw new InvalidConfigException("Configuration does not contain a META-INF/config.ser file: " + serFile);
                } else if (!serFile.canRead()) {
                    throw new InvalidConfigException("Can not read configuration META-INF/config.ser file: " + serFile);
                }

                InputStream in = new FileInputStream(serFile);
                try {
                    ObjectInputStream ois = new ObjectInputStream(in);
                    config.readExternal(ois);
                } finally {
                    IOUtil.close(in);
                }
            } else {
                JarFile jarFile = new JarFile(location);
                InputStream in = null;
                try {
                    ZipEntry entry = jarFile.getEntry("META-INF/config.ser");
                    in = jarFile.getInputStream(entry);
                    ObjectInputStream ois = new ObjectInputStream(in);
                    config.readExternal(ois);
                } finally {
                    IOUtil.close(in);
                    IOUtil.close(jarFile);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigException("Unable to load class from config: " + configId, e);
        }

        return config;
    }


    public boolean containsConfiguration(Artifact configId) {
        File location = repository.getLocation(configId);
        if (location.isDirectory()) {
            location = new File(location, "META-INF");
            location = new File(location, "config.ser");
            return location.isFile() && location.canRead();
        } else {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(location);
                ZipEntry entry = jarFile.getEntry("META-INF/config.ser");
                return entry != null && !entry.isDirectory();
            } catch (IOException e) {
                return false;
            } finally {
                IOUtil.close(jarFile);
            }
        }
    }

    public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
        File location = repository.getLocation(configId);
        if (location.exists()) {
            throw new ConfigurationAlreadyExistsException("Configuration already exists: " + configId);
        }
        location.mkdirs();
        if (!location.exists()) {
            throw new ConfigurationAlreadyExistsException("Could not create configuration directory: " + location);
        }
        return location;
    }

    public URL resolve(Artifact configId, URI uri) throws NoSuchConfigException, MalformedURLException {
        File location = repository.getLocation(configId);
        if (location.isDirectory()) {
            URL locationUrl = location.toURL();
            URL resolvedUrl = new URL(locationUrl, uri.toString());
            return resolvedUrl;
        } else {
            URL baseURL = new URL("jar:" + repository.getLocation(configId).toURL().toString() + "!/");
            return new URL(baseURL, uri.toString());
        }
    }

    public void install(InputStream in, Artifact configId, FileWriteMonitor fileWriteMonitor) throws IOException {
        try {
            repository.copyToRepository(in, configId, fileWriteMonitor);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtil.close(in);
        }
    }

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        // determine the source file/dir
        File source = configurationData.getConfigurationDir();
        if (!source.exists()) {
            throw new InvalidConfigException("Source does not exist " + source);
        } else if (source.canRead()) {
            throw new InvalidConfigException("Source is not readable " + source);
        }

        // determine the target location
        Artifact configId = configurationData.getId();
        File destination = repository.getLocation(configId);

        if (destination.exists()) {
            throw new ConfigurationAlreadyExistsException(configId.toString());
        }

        // if directory in the correct place -- noop
        if (source.equals(destination)) {
            return;
        }

        if (source.isFile()) {
            // Assume this is a jar file
            // copy it into the repository; repository should unpack it
            repository.copyToRepository(source, configId, null);
        } else if (source.isDirectory()) {
            // directory is in wrong place -- directory copy
            IOUtil.recursiveCopy(source, destination);
        } else {
            throw new InvalidConfigException("Unable to install configuration from " + source);
        }
    }

    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
        File location = repository.getLocation(configId);
        IOUtil.recursiveDelete(location);
    }

    public List listConfigurations() {
        SortedSet artifacts = repository.list();

        List configs;
        synchronized (this) {
            configs = new ArrayList();
            for (Iterator i = artifacts.iterator(); i.hasNext();) {
                Artifact configId = (Artifact) i.next();
                if (configId.getType().equals("car")) {
                    try {
                        AbstractName configName = Configuration.getConfigurationAbstractName(configId);
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

                        GBeanData bean = loadConfiguration(configId);
                        ConfigurationModuleType type = (ConfigurationModuleType) bean.getAttribute("type");

                        configs.add(new ConfigurationInfo(objectName, configId, state, type));
                    } catch (Exception e) {
                    }
                }
            }
        }
        return configs;
    }

//    /**
//     * Thread to cleanup unused Config Store entries.
//     * On Windows, open files can't be deleted. Until MultiParentClassLoaders
//     * are GC'ed, we won't be able to delete Config Store directories/files.
//     */
//    class ConfigStoreReaper implements Runnable {
//        private final int reaperInterval;
//        private volatile boolean done = false;
//
//        public ConfigStoreReaper(int reaperInterval) {
//            this.reaperInterval = reaperInterval;
//        }
//
//        public void close() {
//            this.done = true;
//        }
//
//        public void run() {
//            log.debug("ConfigStoreReaper started");
//            while (!done) {
//                try {
//                    Thread.sleep(reaperInterval);
//                } catch (InterruptedException e) {
//                    continue;
//                }
//                reap();
//            }
//        }
//
//        /**
//         * For every directory in the pendingDeletionIndex, attempt to delete all
//         * sub-directories and files.
//         */
//        public void reap() {
//            // return, if there's nothing to do
//            if (pendingDeletionIndex.size() == 0)
//                return;
//            // Otherwise, attempt to delete all of the directories
//            Enumeration list = pendingDeletionIndex.propertyNames();
//            boolean dirDeleted = false;
//            while (list.hasMoreElements()) {
//                String dirName = (String) list.nextElement();
//                File deleteFile = new File(dirName);
//                try {
//                    delete(deleteFile);
//                }
//                catch (IOException ioe) { // ignore errors
//                }
//                if (!deleteFile.exists()) {
//                    String configName = pendingDeletionIndex.getProperty(dirName);
//                    pendingDeletionIndex.remove(dirName);
//                    dirDeleted = true;
//                    log.debug("Reaped configuration " + configName + " in directory " + dirName);
//                }
//            }
//            // If we deleted any directories, persist the list of directories to disk...
//            if (dirDeleted) {
//                try {
//                    synchronized (pendingDeletionIndex) {
//                        saveDeleteIndex();
//                    }
//                }
//                catch (IOException ioe) {
//                    log.warn("Error saving " + DELETE_NAME + " file.", ioe);
//                }
//            }
//        }
//    }
//
    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(RepositoryConfigurationStore.class, "ConfigurationStore");
        builder.addInterface(ConfigurationStore.class);
        builder.addAttribute("kernel", Kernel.class, false);
        builder.addAttribute("objectName", String.class, false);
        builder.addReference("Repository", WritableListableRepository.class, "Repository");
        builder.setConstructor(new String[]{"kernel", "objectName", "Repository"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
