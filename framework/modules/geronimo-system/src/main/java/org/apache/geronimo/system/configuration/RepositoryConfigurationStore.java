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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.management.ObjectName;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Maven2Repository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ConfigurationStore GBean that installs/loads Configurations from a
 * repository.
 *
 * @version $Rev$ $Date$
 */
@Component(immediate = true, metatype = true)
@Service
public class RepositoryConfigurationStore implements ConfigurationStore {
    private static final Logger log = LoggerFactory.getLogger(RepositoryConfigurationStore.class);

    @Property(value = "system")
    private final static String REPOSITORY_ROOT = "repository.root";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ServerInfo serverInfo;

    private final Naming naming = new Jsr77Naming();
    private final AbstractName abstractName = new AbstractName(URI.create("geronimo/base/0.0/car?name=ConfigurationStore"));
    private final ObjectName objectName = abstractName.getObjectName();
    protected WritableListableRepository repository;
    private final InPlaceConfigurationUtil inPlaceConfUtil = new InPlaceConfigurationUtil();
    private ServiceRegistration sr;

    public RepositoryConfigurationStore(WritableListableRepository repository) {
//        this.objectName = objectName == null ? null : ObjectNameUtil.getObjectName(objectName);
//        this.abstractName = abstractName;
        this.repository = repository;

//        inPlaceConfUtil = new InPlaceConfigurationUtil();
    }


    public RepositoryConfigurationStore() {
        log.info("created");
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void unsetServerInfo(ServerInfo serverInfo) {
        if (serverInfo == this.serverInfo) {
            this.serverInfo = null;
        }
    }

    @Activate
    public void activate(ComponentContext context) {
        Dictionary<String, String> properties = context.getProperties();
        String repoRoot = properties.get(REPOSITORY_ROOT);
        File rootFile = serverInfo.resolve(repoRoot);
        repository = new Maven2Repository(rootFile);
        sr = context.getBundleContext().registerService(new String[] {Repository.class.getName(),
                ListableRepository.class.getName(),
                WritableListableRepository.class.getName(),
                WriteableRepository.class.getName()
        }, repository, null);
    }

     @Deactivate
     public void deactivate() {
         sr.unregister();
     }

    @Override
    public String getObjectName() {
        return objectName.getCanonicalName();
    }

    @Override
    public AbstractName getAbstractName() {
        return abstractName;
    }

    @Override
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
                    IOUtils.close(in);
                }
            } else {
                JarFile jarFile = new JarFile(location);
                InputStream in = null;
                try {
                    ZipEntry entry = jarFile.getEntry("META-INF/config.ser");
                    in = jarFile.getInputStream(entry);
                    configurationData = ConfigurationUtil.readConfigurationData(in);
                } finally {
                    IOUtils.close(in);
                    JarUtils.close(jarFile);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigException("Unable to load class from config: " + configId, e);
        }

        configurationData.setConfigurationDir(location);
        configurationData.setConfigurationStore(this);
        configurationData.setNaming(naming);

        return configurationData;
    }

    private boolean existsReadable(File location) {
        return !location.exists() || !location.canRead();
    }

    @Override
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
                JarUtils.close(jarFile);
            }
        }
    }

    @Override
    public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
        if (!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact " + configId + " is not fully resolved");
        }
        File location = repository.getLocation(configId);
        if (location.exists()) {
            throw new ConfigurationAlreadyExistsException("Configuration already exists: " + configId);
        }
        File parentDirectory = location.getParentFile();
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs();
            if (log.isDebugEnabled()) {
                log.debug("Configuration directory: " + parentDirectory + " is created");
            }
        }
        return parentDirectory;
    }

    @Override
    public Set<URL> resolve(Artifact configId, String moduleName, String path) throws NoSuchConfigException, MalformedURLException {
        if(!configId.isResolved()) {
            throw new IllegalArgumentException("Artifact "+configId+" is not fully resolved");
        }
        File location = repository.getLocation(configId);
        if (location.isDirectory()) {
            File inPlaceLocation = null;
            try {
                inPlaceLocation = inPlaceConfUtil.readInPlaceLocation(location);
            } catch (IOException e) {
                //ignore
            }
            if (null != inPlaceLocation) {
                location = inPlaceLocation;
            }

            if (moduleName != null) {
                location = new File(location, moduleName);
            }
            return FileUtils.search(location, path);
 /*           if(path == null) {
                return Collections.singleton(location.toURL());
            } else {
                if (location.isDirectory()) {
                    Set matches = IOUtils.search(location, path);
                    return matches;
                } else {
                    Set matches = IOUtils.search(location, path);
                    return matches;
                }
            }
*/        } else {
            if (moduleName != null) {
                path = moduleName + "/" +path;
            }
            return FileUtils.search(location, path);
        }
    }

    @Override
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
    	if (dir.isDirectory()) {
	        File[] all = dir.listFiles();
	        if (all.length == 0) {
	            // it is an empty directory
	            ZipEntry entry = new ZipEntry(prefix);
	            out.putNextEntry(entry);
	        }
	        for (File file : all) {
	            if (file.isDirectory()) {
	                writeToZip(file, out, prefix + file.getName() + "/", buf);
	            } else {
	                ZipEntry entry = new ZipEntry(prefix + file.getName());
	                out.putNextEntry(entry);
	                writeToZipStream(file, out, buf);
	            }
	        }
    	}else{
    		 ZipFile input = new ZipFile(dir);
             Enumeration en = input.entries();
             byte[] buffer = new byte[4096];
             int count;
             while (en.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry) en.nextElement();
                 out.putNextEntry(entry);
                 InputStream in = input.getInputStream(entry);
                     while ((count = in.read(buf)) > -1) {
                         out.write(buf, 0, count);
                 }
                 in.close();
                 out.closeEntry();
             }
             input.close();
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

    @Override
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

    @Override
    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        // determine the source file/dir
        if (log.isDebugEnabled()) {
            log.debug("Writing config: " + configurationData);
        }
        File source = configurationData.getInPlaceConfigurationDir() == null ? configurationData.getConfigurationDir()
                : configurationData.getInPlaceConfigurationDir();

        if (!source.exists()) {
            throw new InvalidConfigException("Source does not exist " + source);
        } else if (!source.canRead()) {
            throw new InvalidConfigException("Source is not readable " + source);
        }
        ExecutableConfigurationUtil.writeConfiguration(configurationData, source);
        // write in-place configuration config file, if need be.
        inPlaceConfUtil.writeInPlaceLocation(configurationData, source);

        // determine the target location
        Artifact configId = configurationData.getId();
        File destination = repository.getLocation(configId);
        if (!source.equals(destination)) {
            if (source.isFile()) {
                if (log.isDebugEnabled()) {
                    log.debug("copying packed bundle from " + source + " to destination " + destination);
                }
                repository.copyToRepository(source, configId, null);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Packing bundle from " + source + " to destination " + destination);
                }
                JarUtils.jarDirectory(source, destination);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Plugin is already in location " + source);
            }
        }
        // if directory in the correct place -- noop
       /*
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
                FileUtils.recursiveCopy(source, destination);
            } else {
                throw new InvalidConfigException("Unable to install configuration from " + source);
            }
        }
    */
    }

    @Override
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
        FileUtils.recursiveDelete(location);
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
            }
            if (ioException != null) {
                throw ioException;
            }
        }

    }

    @Override
    public List<ConfigurationInfo> listConfigurations() {
        SortedSet<Artifact> artifacts = repository.list();

        List<ConfigurationInfo> configs= new ArrayList<ConfigurationInfo>();
        synchronized (this) {
            for (Artifact configId : artifacts) {
                try {
                    ConfigurationInfo configurationInfo = loadConfigurationInfo(configId);
                    configs.add(configurationInfo);
                } catch (NoSuchConfigException e) {
                    continue;
                } catch (IOException e) {
                    log.error("Unable to load ConfigurationInfo for " + configId, e);
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
            if (!infoFile.exists() || !infoFile.canRead()) {
                throw new NoSuchConfigException(configId);
            }
            InputStream in = new FileInputStream(infoFile);
            try {
                configurationInfo = ConfigurationUtil.readConfigurationInfo(in, getAbstractName(), inPlaceLocation);
            } finally {
                IOUtils.close(in);
            }
        } else {
            JarFile jarFile = new JarFile(location);
            InputStream in = null;
            try {
                ZipEntry entry = jarFile.getEntry("META-INF/config.info");
                if (entry == null) {
                    throw new NoSuchConfigException(configId);
                }
                in = jarFile.getInputStream(entry);
                configurationInfo = ConfigurationUtil.readConfigurationInfo(in, getAbstractName(), inPlaceLocation);
            } finally {
                IOUtils.close(in);
                JarUtils.close(jarFile);
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

    public static final String GBEAN_REF_REPOSITORY = "Repository";
    public static final String GBEAN_REF_CONFIG_DATA_TRANSFORMER = "ConfigurationDataTransformer";
}
