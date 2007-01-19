/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.Set;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.IOUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of ConfigurationStore GBean that installs/loads Configurations from a 
 * repository.
 *
 * @version $Rev$ $Date$
 */
public class RepositoryConfigurationStore implements ConfigurationStore {
    private final static Log log = LogFactory.getLog(RepositoryConfigurationStore.class);
    private final Kernel kernel;
    private final ObjectName objectName;
    protected final WritableListableRepository repository;
    private final InPlaceConfigurationUtil inPlaceConfUtil;

    public RepositoryConfigurationStore(WritableListableRepository repository) {
        this(null, null, repository);
    }

    public RepositoryConfigurationStore(Kernel kernel, String objectName, WritableListableRepository repository) {
        this.kernel = kernel;
        this.objectName = objectName == null ? null : ObjectNameUtil.getObjectName(objectName);
        this.repository = repository;

        inPlaceConfUtil = new InPlaceConfigurationUtil();
    }

    public String getObjectName() {
        return objectName.getCanonicalName();
    }

    public AbstractName getAbstractName() {
        return kernel == null? null:kernel.getAbstractNameFor(this);
    }

    public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        File location = repository.getLocation(configId);

        if (existsReadable(location)) {
            throw new NoSuchConfigException(configId);
        }

        ConfigurationData configurationData;
        try {
            if (location.isDirectory()) {
                File serFile = new File(location, "META-INF");
                serFile = new File(serFile, "config.ser");

                if (!serFile.exists()) {
                    throw new InvalidConfigException("Configuration does not contain a META-INF/config.ser file: " + serFile);
                } else if (!serFile.canRead()) {
                    throw new InvalidConfigException("Can not read configuration META-INF/config.ser file: " + serFile);
                }

                ConfigurationStoreUtil.verifyChecksum(serFile);

                InputStream in = new FileInputStream(serFile);
                try {
                    configurationData = ConfigurationUtil.readConfigurationData(in);
                } finally {
                    IOUtil.close(in);
                }
            } else {
                JarFile jarFile = new JarFile(location);
                InputStream in = null;
                try {
                    ZipEntry entry = jarFile.getEntry("META-INF/config.ser");
                    in = jarFile.getInputStream(entry);
                    configurationData = ConfigurationUtil.readConfigurationData(in);
                } finally {
                    IOUtil.close(in);
                    IOUtil.close(jarFile);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigException("Unable to load class from config: " + configId, e);
        }

        configurationData.setConfigurationDir(location);
        configurationData.setConfigurationStore(this);
        if (kernel != null) {
            configurationData.setNaming(kernel.getNaming());
        }

        return configurationData;
    }

    private boolean existsReadable(File location) {
        return !location.exists() || !location.canRead();
    }

    public boolean containsConfiguration(Artifact configId) {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
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
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
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

    public Set resolve(Artifact configId, String moduleName, String path) throws NoSuchConfigException, MalformedURLException {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        File location = repository.getLocation(configId);
        if (location.isDirectory()) {
            File inPlaceLocation = null;
            try {
                inPlaceLocation = inPlaceConfUtil.readInPlaceLocation(location);
            } catch (IOException e) {
            }
            if (null != inPlaceLocation) {
                location = inPlaceLocation;
            }

            if (moduleName != null) {
                location = new File(location, moduleName);
            }

            if(path == null) {
                return Collections.singleton(location.toURL());
            } else {
                if (location.isDirectory()) {
                    Set matches = IOUtil.search(location, path);
                    return matches;
                } else {
                    Set matches = IOUtil.search(location, path);
                    return matches;
                }
            }
        } else {
            Set matches = IOUtil.search(location, moduleName + "/" +path);
            return matches;
        }
    }

    public void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        File dir = repository.getLocation(configId);
        if (dir == null) {
            throw new NoSuchConfigException(configId);
        }
        if (existsReadable(dir)) {
            throw new IOException("Cannot read config store directory for " + configId + " (" + dir.getAbsolutePath() + ")");
        }
        ZipOutputStream out = new ZipOutputStream(output);
        byte[] buf = new byte[10240];
        writeToZip(dir, out, "", buf);
        if (inPlaceConfUtil.isInPlaceConfiguration(dir)) {
            dir = inPlaceConfUtil.readInPlaceLocation(dir);
            writeToZip(dir, out, "", buf);
        }
        out.closeEntry();
        out.finish();
        out.flush();
    }

    private void writeToZip(File dir, ZipOutputStream out, String prefix, byte[] buf) throws IOException {
        File[] all = dir.listFiles();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];
            if (file.isDirectory()) {
                writeToZip(file, out, prefix + file.getName() + "/", buf);
            } else {
                ZipEntry entry = new ZipEntry(prefix + file.getName());
                out.putNextEntry(entry);
                writeToZipStream(file, out, buf);
            }
        }
    }

    private void writeToZipStream(File file, OutputStream out, byte[] buf) throws IOException {
        FileInputStream in = new FileInputStream(file);
        int count;
        try {
            while ((count = in.read(buf, 0, buf.length)) > -1) {
                out.write(buf, 0, count);
            }
        } finally {
            in.close();
        }
    }

    public void install(InputStream in, int size, Artifact configId, FileWriteMonitor fileWriteMonitor) throws IOException {
        try {
            repository.copyToRepository(in, size, configId, fileWriteMonitor);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtil.close(in);
        }
    }

    public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        File location = repository.getLocation(configId);
        if (location.isDirectory()) {
            return inPlaceConfUtil.isInPlaceConfiguration(location);
        } else {
            return false;
        }
    }

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        // determine the source file/dir
        File source = configurationData.getConfigurationDir();
        if (!source.exists()) {
            throw new InvalidConfigException("Source does not exist " + source);
        } else if (!source.canRead()) {
            throw new InvalidConfigException("Source is not readable " + source);
        }

        // determine the target location
        Artifact configId = configurationData.getId();
        File destination = repository.getLocation(configId);

        // if directory in the correct place -- noop
        if (!source.equals(destination)) {
            if (destination.exists()) {
                throw new ConfigurationAlreadyExistsException(configId.toString());
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

        ExecutableConfigurationUtil.writeConfiguration(configurationData, destination);

        // write in-place configuration config file, if need be.
        inPlaceConfUtil.writeInPlaceLocation(configurationData, destination);
    }

    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        ConfigurationInfo configurationInfo = null;
        try {
            configurationInfo = loadConfigurationInfo(configId);
        } catch (IOException e) {
            // don't really care
        }
        File location = repository.getLocation(configId);
        IOUtil.recursiveDelete(location);
        // Number of directory levels up, to check and delete empty parent directories in the repo
        int dirDepth = 0;

        // FIXME: Determine the repository type
        // For now assume the repo is a Maven2Repository.  This should not cause any harm even if it is an
        // Maven1Repository, for it would be deleting the 'repository' directory if it happens to be empty.
        boolean m2repo = true;
        if(m2repo) {
            // Check version, artifact and group directories, i.e. 3 levels up
            dirDepth = 3;
        }

        File temp = location;
        for(int i = 0; i < dirDepth; ++i) {
            if((temp = temp.getParentFile()).listFiles().length == 0) {
                // Directory is empty.  Remove it.
                temp.delete();
            } else {
                // Directory is not empty.  No need to check any more parent directories
                break;
            }
        }

        try {
            // Is this the right way to get hold of PersistentConfigurationList?
            PersistentConfigurationList configList = (PersistentConfigurationList) kernel.getGBean(PersistentConfigurationList.class);
            if(!configList.hasGBeanAttributes(configId)) configList.removeConfiguration(configId);
        } catch (Exception e) {
            log.warn("Unable to remove configuration from persistent configurations. id = "+configId, e);
        }

        if (configurationInfo != null) {
            IOException ioException = null;
            for (Iterator iterator = configurationInfo.getOwnedConfigurations().iterator(); iterator.hasNext();) {
                Artifact ownedConfiguration = (Artifact) iterator.next();
                try {
                    uninstall(ownedConfiguration);
                } catch (NoSuchConfigException e) {
                    // ignored - already deleted or never installed
                } catch (IOException e) {
                    if (ioException != null) {
                        ioException = e;
                    }
                }
                if (ioException != null) {
                    throw ioException;
                }
            }
        }
    }

    public List listConfigurations() {
        SortedSet artifacts = repository.list();

        List configs;
        synchronized (this) {
            configs = new ArrayList();
            for (Iterator i = artifacts.iterator(); i.hasNext();) {
                Artifact configId = (Artifact) i.next();
                File dir = repository.getLocation(configId);
                File meta = new File(dir, "META-INF");
                if(!meta.isDirectory() || !meta.canRead()) {
                    continue;
                }
                File ser = new File(meta, "config.ser");
                if(!ser.isFile() || !ser.canRead() || ser.length() == 0) {
                    continue;
                }
                try {
                    ConfigurationInfo configurationInfo = loadConfigurationInfo(configId);
                    configs.add(configurationInfo);
                } catch (NoSuchConfigException e) {
                    log.error("Unexpected error: found META-INF/config.ser for "+configId+" but couldn't load ConfigurationInfo", e);
                } catch (IOException e) {
                    log.error("Unable to load ConfigurationInfo for "+configId, e);
                }
            }
        }
        return configs;
    }

    private ConfigurationInfo loadConfigurationInfo(Artifact configId) throws NoSuchConfigException, IOException {
        File location = repository.getLocation(configId);

        if (!location.exists() && !location.canRead()) {
            throw new NoSuchConfigException(configId);
        }

        File inPlaceLocation = inPlaceConfUtil.readInPlaceLocation(location);

        ConfigurationInfo configurationInfo;
        if (location.isDirectory()) {
            File infoFile = new File(location, "META-INF");
            infoFile = new File(infoFile, "config.info");

            InputStream in = new FileInputStream(infoFile);
            try {
                configurationInfo = ConfigurationUtil.readConfigurationInfo(in, getAbstractName(), inPlaceLocation);
            } finally {
                IOUtil.close(in);
            }
        } else {
            JarFile jarFile = new JarFile(location);
            InputStream in = null;
            try {
                ZipEntry entry = jarFile.getEntry("META-INF/config.info");
                in = jarFile.getInputStream(entry);
                configurationInfo = ConfigurationUtil.readConfigurationInfo(in, getAbstractName(), inPlaceLocation);
            } finally {
                IOUtil.close(in);
                IOUtil.close(jarFile);
            }
        }

        return configurationInfo;
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
        builder.addAttribute("kernel", Kernel.class, false);
        builder.addAttribute("objectName", String.class, false);
        builder.addReference("Repository", WritableListableRepository.class, "Repository");
        builder.setConstructor(new String[]{"kernel", "objectName", "Repository"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
