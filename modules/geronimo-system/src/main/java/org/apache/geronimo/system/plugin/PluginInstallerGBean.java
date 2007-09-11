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
package org.apache.geronimo.system.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.system.configuration.ConfigurationStoreUtil;
import org.apache.geronimo.system.configuration.PluginAttributeStore;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.CopyFileType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.HashType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.ObjectFactory;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PrerequisiteType;
import org.apache.geronimo.system.plugin.model.PropertyType;
import org.apache.geronimo.system.resolver.AliasedArtifactResolver;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;
import org.apache.geronimo.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A GBean that knows how to download configurations from a Maven repository.
 *
 * @version $Rev$ $Date$
 */
public class PluginInstallerGBean implements PluginInstaller {
    private final static Log log = LogFactory.getLog(PluginInstallerGBean.class);

    public static final XMLInputFactory XMLINPUT_FACTORY = XMLInputFactory.newInstance();
    private static final JAXBContext PLUGIN_CONTEXT;
    private static final JAXBContext PLUGIN_LIST_CONTEXT;
    private static final JAXBContext PLUGIN_ARTIFACT_CONTEXT;

    static {
        try {
            PLUGIN_CONTEXT = JAXBContext.newInstance(PluginType.class);
            PLUGIN_LIST_CONTEXT = JAXBContext.newInstance(PluginListType.class);
            PLUGIN_ARTIFACT_CONTEXT = JAXBContext.newInstance(PluginArtifactType.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create jaxb contexts for plugin types");
        }
    }

    private static int counter;
    private final ConfigurationManager configManager;
    private final WritableListableRepository writeableRepo;
    private final ConfigurationStore configStore;
    private final AliasedArtifactResolver artifactResolver;
    private final ServerInfo serverInfo;
    private final Map<Object, DownloadResults> asyncKeys;
    private final ThreadPool threadPool;
    private final PluginAttributeStore attributeStore;

    public PluginInstallerGBean(ConfigurationManager configManager, WritableListableRepository repository, ConfigurationStore configStore, ServerInfo serverInfo, ThreadPool threadPool, PluginAttributeStore store, AliasedArtifactResolver artifactResolver) {
        this.configManager = configManager;
        this.writeableRepo = repository;
        this.configStore = configStore;
        this.serverInfo = serverInfo;
        this.threadPool = threadPool;
        asyncKeys = Collections.synchronizedMap(new HashMap<Object, DownloadResults>());
        attributeStore = store;
        this.artifactResolver = artifactResolver;
    }

    /**
     * Lists the plugins installed in the local Geronimo server, by name and
     * ID.
     *
     * @return A Map with key type String (plugin name) and value type Artifact
     *         (config ID of the plugin).
     */
    public Map getInstalledPlugins() {
        SortedSet<Artifact> artifacts = writeableRepo.list();

        Map plugins = new HashMap();
        for (Artifact configId : artifacts) {
            File dir = writeableRepo.getLocation(configId);
            if (dir.isDirectory()) {
                File meta = new File(dir, "META-INF");
                if (!meta.isDirectory() || !meta.canRead()) {
                    continue;
                }
                File xml = new File(meta, "geronimo-plugin.xml");
                if (!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                    continue;
                }
                readNameAndID(xml, plugins);
            } else {
                if (!dir.isFile() || !dir.canRead()) {
                    log.error("Cannot read artifact dir " + dir.getAbsolutePath());
                    throw new IllegalStateException("Cannot read artifact dir " + dir.getAbsolutePath());
                }
                try {
                    JarFile jar = new JarFile(dir);
                    try {
                        ZipEntry entry = jar.getEntry("META-INF/geronimo-plugin.xml");
                        if (entry == null) {
                            continue;
                        }
                        InputStream in = jar.getInputStream(entry);
                        readNameAndID(in, plugins);
                        in.close();
                    } finally {
                        jar.close();
                    }
                } catch (IOException e) {
                    log.error("Unable to read JAR file " + dir.getAbsolutePath(), e);
                }
            }
        }
        return plugins;
    }

    /**
     * Gets a ConfigurationMetadata for a configuration installed in the local
     * server.  Should load a saved one if available, or else create a new
     * default one to the best of its abilities.
     *
     * @param moduleId Identifies the configuration.  This must match a
     *                 configuration currently installed in the local server.
     *                 The configId must be fully resolved (isResolved() == true)
     */
    public PluginType getPluginMetadata(Artifact moduleId) {
        if (configManager != null) {
            if (!configManager.isConfiguration(moduleId)) {
                return null;
            }
        } else {
            if (!configStore.containsConfiguration(moduleId)) {
                return null;
            }
        }
        File dir = writeableRepo.getLocation(moduleId);
        ConfigurationData configData;
        String source = dir.getAbsolutePath();
        InputStream in;
        try {
            if (dir.isDirectory()) {
                File meta = new File(dir, "META-INF");
                if (!meta.isDirectory() || !meta.canRead()) {
                    return null;
                }
                File xml = new File(meta, "geronimo-plugin.xml");
                configData = configStore.loadConfiguration(moduleId);
                if (!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                    return createDefaultMetadata(configData);
                }
                in = new FileInputStream(xml);
            } else {
                if (!dir.isFile() || !dir.canRead()) {
                    log.error("Cannot read configuration " + dir.getAbsolutePath());
                    throw new IllegalStateException("Cannot read configuration " + dir.getAbsolutePath());
                }
                configData = configStore.loadConfiguration(moduleId);
                JarFile jar = new JarFile(dir);
                try {
                    ZipEntry entry = jar.getEntry("META-INF/geronimo-plugin.xml");
                    if (entry == null) {
                        return createDefaultMetadata(configData);
                    }
                    source = dir.getAbsolutePath() + "#META-INF/geronimo-plugin.xml";
                    in = jar.getInputStream(entry);
                } finally {
                    jar.close();
                }
            }
            PluginType result;
            try {
                result = loadPluginMetadata(in);
            } finally {
                in.close();
            }
            overrideDependencies(configData, result);
            return result;
        } catch (InvalidConfigException e) {
            e.printStackTrace();
            log.warn("Unable to generate metadata for " + moduleId, e);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Invalid XML at " + source, e);
        }
        return null;
    }

    /**
     * Saves a ConfigurationMetadata for a particular plugin, if the server is
     * able to record it.  This can be used if you later re-export the plugin,
     * or just want to review the information for a particular installed
     * plugin.
     *
     * @param metadata The data to save.  The contained configId (which must
     *                 be fully resolved) identifies the configuration to save
     *                 this for.
     */
    public void updatePluginMetadata(PluginType metadata) {
        PluginArtifactType instance = metadata.getPluginArtifact().get(0);
        Artifact artifact = toArtifact(instance.getModuleId());
        File dir = writeableRepo.getLocation(artifact);
        if (dir == null) {
            log.error(artifact + " is not installed!");
            throw new IllegalArgumentException(artifact + " is not installed!");
        }
        if (!dir.isDirectory()) { // must be a packed (JAR-formatted) plugin
            try {
                File temp = new File(dir.getParentFile(), dir.getName() + ".temp");
                JarFile input = new JarFile(dir);
                Manifest manifest = input.getManifest();
                JarOutputStream out = manifest == null ? new JarOutputStream(
                        new BufferedOutputStream(new FileOutputStream(temp)))
                        : new JarOutputStream(new BufferedOutputStream(new FileOutputStream(temp)), manifest);
                Enumeration en = input.entries();
                byte[] buf = new byte[4096];
                int count;
                while (en.hasMoreElements()) {
                    JarEntry entry = (JarEntry) en.nextElement();
                    if (entry.getName().equals("META-INF/geronimo-plugin.xml")) {
                        entry = new JarEntry(entry.getName());
                        out.putNextEntry(entry);
                        writePluginMetadata(metadata, out);
                    } else if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                        // do nothing, already passed in a manifest
                    } else {
                        out.putNextEntry(entry);
                        InputStream in = input.getInputStream(entry);
                        while ((count = in.read(buf)) > -1) {
                            out.write(buf, 0, count);
                        }
                        in.close();
                        out.closeEntry();
                    }
                }
                out.flush();
                out.close();
                input.close();
                if (!dir.delete()) {
                    log.error("Unable to delete old plugin at " + dir.getAbsolutePath());
                    throw new IOException("Unable to delete old plugin at " + dir.getAbsolutePath());
                }
                if (!temp.renameTo(dir)) {
                    log.error("Unable to move new plugin " + temp.getAbsolutePath() + " to " + dir.getAbsolutePath());
                    throw new IOException(
                            "Unable to move new plugin " + temp.getAbsolutePath() + " to " + dir.getAbsolutePath());
                }
            } catch (Exception e) {
                log.error("Unable to update plugin metadata", e);
                throw new RuntimeException("Unable to update plugin metadata", e);
            } // TODO this really should have a finally block to ensure streams are closed
        } else {
            File meta = new File(dir, "META-INF");
            if (!meta.isDirectory() || !meta.canRead()) {
                log.error(artifact + " is not a plugin!");
                throw new IllegalArgumentException(artifact + " is not a plugin!");
            }
            File xml = new File(meta, "geronimo-plugin.xml");
            FileOutputStream fos = null;
            try {
                if (!xml.isFile()) {
                    if (!xml.createNewFile()) {
                        log.error("Cannot create plugin metadata file for " + artifact);
                        throw new RuntimeException("Cannot create plugin metadata file for " + artifact);
                    }
                }
                fos = new FileOutputStream(xml);
                writePluginMetadata(metadata, fos);
            } catch (Exception e) {
                log.error("Unable to save plugin metadata for " + artifact, e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                }
            }
        }
    }

    public static void writePluginMetadata(PluginType metadata, OutputStream out) throws XMLStreamException, JAXBException {
        Marshaller marshaller = PLUGIN_CONTEXT.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        JAXBElement<PluginType> element = new ObjectFactory().createGeronimoPlugin(metadata);
        marshaller.marshal(element, out);
    }

    public static void writePluginList(PluginListType metadata, Writer out) throws XMLStreamException, JAXBException {
        Marshaller marshaller = PLUGIN_LIST_CONTEXT.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        JAXBElement<PluginListType> element = new ObjectFactory().createGeronimoPluginList(metadata);
        marshaller.marshal(element, out);
    }

    /**
     * Lists the plugins available for download in a particular Geronimo repository.
     *
     * @param mavenRepository The base URL to the maven repository.  This must
     *                        contain the file geronimo-plugins.xml
     * @param username        Optional username, if the maven repo uses HTTP Basic authentication.
     *                        Set this to null if no authentication is required.
     * @param password        Optional password, if the maven repo uses HTTP Basic authentication.
     *                        Set this to null if no authentication is required.
     */
    public PluginListType listPlugins(URL mavenRepository, String username, String password) throws IOException, FailedLoginException {
        String repository = mavenRepository.toString().trim();
        if (!repository.endsWith("/")) {
            repository = repository + "/";
        }
        //todo: Try downloading a .gz first
        String url = repository + "geronimo-plugins.xml";
        try {
            //todo: use a progress monitor
            InputStream in = openStream(null, Collections.singletonList(url), username, password, null).getStream();
            return loadPluginList(in);
        } catch (MissingDependencyException e) {
            log.error("Cannot find plugin index at site " + url);
            return null;
        } catch (Exception e) {
            log.error("Unable to load repository configuration data", e);
            return null;
        }
    }

    /**
     * Installs a configuration from a remote repository into the local Geronimo server,
     * including all its dependencies.  The caller will get the results when the
     * operation completes.  Note that this method does not throw exceptions on failure,
     * but instead sets the failure property of the DownloadResults.
     *
     * @param username         Optional username, if the maven repo uses HTTP Basic authentication.
     *                         Set this to null if no authentication is required.
     * @param password         Optional password, if the maven repo uses HTTP Basic authentication.
     *                         Set this to null if no authentication is required.
     * @param pluginsToInstall The list of configurations to install
     */
    public DownloadResults install(PluginListType pluginsToInstall, String username, String password) {
        DownloadResults results = new DownloadResults();
        install(pluginsToInstall, username, password, results);
        return results;
    }

    /**
     * Installs a configuration from a remote repository into the local Geronimo server,
     * including all its dependencies.  The method blocks until the operation completes,
     * but the caller will be notified of progress frequently along the way (using the
     * supplied DownloadPoller).  Therefore the caller is meant to create the poller and
     * then call this method in a background thread.  Note that this method does not
     * throw exceptions on failure, but instead sets the failure property of the
     * DownloadPoller.
     *
     * @param pluginsToInstall The list of configurations to install
     * @param username         Optional username, if the maven repo uses HTTP Basic authentication.
     *                         Set this to null if no authentication is required.
     * @param password         Optional password, if the maven repo uses HTTP Basic authentication.
     *                         Set this to null if no authentication is required.
     * @param poller           Will be notified with status updates as the download proceeds
     */
    public void install(PluginListType pluginsToInstall, String username, String password, DownloadPoller poller) {
        try {
            Map<Artifact, PluginType> metaMap = new HashMap<Artifact, PluginType>();
            // Step 1: validate everything
            for (PluginType metadata : pluginsToInstall.getPlugin()) {
                validatePlugin(metadata);
                PluginArtifactType instance = metadata.getPluginArtifact().get(0);

                if (instance.getModuleId() != null) {
                    metaMap.put(toArtifact(instance.getModuleId()), metadata);
                }
            }

            // Step 2: everything is valid, do the installation
            for (PluginType metadata : pluginsToInstall.getPlugin()) {
                // 2. Unload obsoleted configurations
                PluginArtifactType instance = metadata.getPluginArtifact().get(0);
                List<Artifact> obsoletes = new ArrayList<Artifact>();
                for (ArtifactType obs : instance.getObsoletes()) {
                    Artifact obsolete = toArtifact(obs);
                    Artifact[] list = configManager.getArtifactResolver().queryArtifacts(obsolete);
                    for (Artifact artifact : list) {
                        if (configManager.isLoaded(artifact)) {
                            if (configManager.isRunning(artifact)) {
                                configManager.stopConfiguration(artifact);
                            }
                            configManager.unloadConfiguration(artifact);
                            obsoletes.add(artifact);
                        }
                    }
                }
                // 3. Download the artifact if necessary, and its dependencies
                Set<Artifact> working = new HashSet<Artifact>();
                if (instance.getModuleId() != null) {
                    List<String> repos = pluginsToInstall.getDefaultRepository();
                    if (!instance.getSourceRepository().isEmpty()) {
                        repos = instance.getSourceRepository();
                    }
                    downloadArtifact(toArtifact(instance.getModuleId()), metaMap, repos,
                            username, password, new ResultsFileWriteMonitor(poller), working, false);
                } else {
                    List<DependencyType> deps = instance.getDependency();
                    for (DependencyType dep : deps) {
                        Artifact entry = toArtifact(dep);
                        List<String> repos = pluginsToInstall.getDefaultRepository();
                        if (!instance.getSourceRepository().isEmpty()) {
                            repos = instance.getSourceRepository();
                        }
                        downloadArtifact(entry, metaMap, repos,
                                username, password, new ResultsFileWriteMonitor(poller), working, false);
                    }
                }
                // 4. Uninstall obsolete configurations
                for (Artifact artifact : obsoletes) {
                    configManager.uninstallConfiguration(artifact);
                }
                // 5. Installation of this configuration finished successfully
            }

            // Step 3: Start anything that's marked accordingly
            for (PluginType metadata : pluginsToInstall.getPlugin()) {
                PluginArtifactType instance = metadata.getPluginArtifact().get(0);
                for (DependencyType dep : instance.getDependency()) {
                    if (dep.isStart()) {
                        Artifact artifact = toArtifact(dep);
                        if (configManager.isConfiguration(artifact)) {
                            poller.setCurrentFilePercent(-1);
                            poller.setCurrentMessage("Starting " + artifact);
                            configManager.loadConfiguration(artifact);
                            configManager.startConfiguration(artifact);
                        }
                    }
                }
            }
        } catch (Exception e) {
            poller.setFailure(e);
        } finally {
            poller.setFinished();
        }
    }

    /**
     * Installs a configuration from a remote repository into the local Geronimo server,
     * including all its dependencies.  The method returns immediately, providing a key
     * that can be used to poll the status of the download operation.  Note that the
     * installation does not throw exceptions on failure, but instead sets the failure
     * property of the DownloadResults that the caller can poll for.
     *
     * @param pluginsToInstall The list of configurations to install
     * @param username         Optional username, if the maven repo uses HTTP Basic authentication.
     *                         Set this to null if no authentication is required.
     * @param password         Optional password, if the maven repo uses HTTP Basic authentication.
     *                         Set this to null if no authentication is required.
     * @return A key that can be passed to checkOnInstall
     */
    public Object startInstall(final PluginListType pluginsToInstall, final String username, final String password) {
        Object key = getNextKey();
        final DownloadResults results = new DownloadResults();
        Runnable work = new Runnable() {
            public void run() {
                install(pluginsToInstall, username, password, results);
            }
        };
        asyncKeys.put(key, results);
        try {
            threadPool.execute("Configuration Installer", work);
        } catch (InterruptedException e) {
            log.error("Unable to start work", e);
            throw new RuntimeException("Unable to start work", e);
        }
        return key;
    }

    /**
     * Installs a configuration downloaded from a remote repository into the local Geronimo
     * server, including all its dependencies.  The method returns immediately, providing a
     * key that can be used to poll the status of the download operation.  Note that the
     * installation does not throw exceptions on failure, but instead sets the failure
     * property of the DownloadResults that the caller can poll for.
     *
     * @param carFile  A CAR file downloaded from a remote repository.  This is a packaged
     *                 configuration with included configuration information, but it may
     *                 still have external dependencies that need to be downloaded
     *                 separately.  The metadata in the CAR file includes a repository URL
     *                 for these downloads, and the username and password arguments are
     *                 used in conjunction with that.
     * @param username Optional username, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param password Optional password, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @return A key that can be passed to checkOnInstall
     */
    public Object startInstall(final File carFile, final String username, final String password) {
        Object key = getNextKey();
        final DownloadResults results = new DownloadResults();
        Runnable work = new Runnable() {
            public void run() {
                install(carFile, username, password, results);
            }
        };
        asyncKeys.put(key, results);
        try {
            threadPool.execute("Configuration Installer", work);
        } catch (InterruptedException e) {
            log.error("Unable to start work", e);
            throw new RuntimeException("Unable to start work", e);
        }
        return key;
    }

    /**
     * Gets the current progress of a download operation.  Note that once the
     * DownloadResults is returned for this operation shows isFinished = true,
     * the operation will be forgotten, so the caller should be careful not to
     * call this again after the download has finished.
     *
     * @param key Identifies the operation to check on
     */
    public DownloadResults checkOnInstall(Object key) {
        DownloadResults results = asyncKeys.get(key);
        results = results.duplicate();
        if (results.isFinished()) {
            asyncKeys.remove(key);
        }
        return results;
    }

    /**
     * Installs from a pre-downloaded CAR file
     *
     * @param carFile  care file to install
     * @param username repo username
     * @param password repo password
     * @param poller   monitor for reporting progress
     */
    public void install(File carFile, String username, String password, DownloadPoller poller) {
        try {
            // 1. Extract the configuration metadata
            PluginType data = loadCARFile(carFile, true);
            if (data == null) {
                log.error("Invalid Configuration Archive " + carFile.getAbsolutePath() + " see server log for details");
                throw new IllegalArgumentException(
                        "Invalid Configuration Archive " + carFile.getAbsolutePath() + " see server log for details");
            }

            // 2. Validate that we can install this
            validatePlugin(data);
            PluginArtifactType instance = data.getPluginArtifact().get(0);
            // 3. Install the CAR into the repository (it shouldn't be re-downloaded)
            if (instance.getModuleId() != null) {
                Artifact pluginArtifact = toArtifact(instance.getModuleId());
                ResultsFileWriteMonitor monitor = new ResultsFileWriteMonitor(poller);
                writeableRepo.copyToRepository(carFile, pluginArtifact, monitor);
                installConfigXMLData(pluginArtifact, instance);
                if (instance.getCopyFile() != null) {
                    extractPluginFiles(pluginArtifact, data, monitor);
                }
            }

            // 4. Use the standard logic to remove obsoletes, install dependencies, etc.
            //    This will validate all over again (oh, well)
            PluginListType pluginList = new PluginListType();
            pluginList.getPlugin().add(data);
            pluginList.getDefaultRepository().addAll(instance.getSourceRepository());
            install(pluginList, username, password, poller);
        } catch (Exception e) {
            poller.setFailure(e);
        } finally {
            poller.setFinished();
        }
    }

    /**
     * Ensures that a plugin is installable.
     *
     * @param plugin plugin to check
     * @throws org.apache.geronimo.kernel.repository.MissingDependencyException
     *          if plugin requires a dependency that is not present
     */
    private void validatePlugin(PluginType plugin) throws MissingDependencyException {
        if (plugin.getPluginArtifact().size() != 1) {
            throw new IllegalArgumentException("A plugin configuration must include one plugin artifact, not " + plugin.getPluginArtifact().size());
        }
        PluginArtifactType metadata = plugin.getPluginArtifact().get(0);
        // 1. Check that it's not already running
        if (metadata.getModuleId() != null) { // that is, it's a real configuration not a plugin list
            Artifact artifact = toArtifact(metadata.getModuleId());
            if (configManager.isRunning(artifact)) {
                boolean upgrade = false;
                for (ArtifactType obsolete : metadata.getObsoletes()) {
                    Artifact test = toArtifact(obsolete);
                    if (test.matches(artifact)) {
                        upgrade = true;
                        break;
                    }
                }
                if (!upgrade) {
                    log.error("Configuration " + artifact + " is already running!");
                    throw new IllegalArgumentException(
                            "Configuration " + artifact + " is already running!");
                }
            }
        }
        // 2. Check that we meet the prerequisites
        List<PrerequisiteType> prereqs = metadata.getPrerequisite();
        for (PrerequisiteType prereq : prereqs) {
            if (artifactResolver.queryArtifacts(toArtifact(prereq.getId())).length == 0) {
                log.error("Required configuration '" + prereq.getId() + "' is not installed.");
                throw new MissingDependencyException(
                        "Required configuration '" + prereq.getId() + "' is not installed.");
            }
        }
        // 3. Check that we meet the Geronimo, JVM versions
        if (metadata.getGeronimoVersion().size() > 0 && !checkGeronimoVersions(metadata.getGeronimoVersion())) {
            log.error("Cannot install plugin " + metadata.getModuleId() + " on Geronimo " + serverInfo.getVersion());
            throw new MissingDependencyException(
                    "Cannot install plugin " + metadata.getModuleId() + " on Geronimo " + serverInfo.getVersion());
        }
        if (metadata.getJvmVersion().size() > 0 && !checkJVMVersions(metadata.getJvmVersion())) {
            log.error("Cannot install plugin " + metadata.getModuleId() + " on JVM " + System.getProperty(
                    "java.version"));
            throw new MissingDependencyException(
                    "Cannot install plugin " + metadata.getModuleId() + " on JVM " + System.getProperty(
                            "java.version"));
        }
    }

    /**
     * Download (if necessary) and install something, which may be a Configuration or may
     * be just a JAR.  For each artifact processed, all its dependencies will be
     * processed as well.
     *
     * @param configID   Identifies the artifact to install
     * @param metadata   name to plugin map
     * @param repos      The URLs to contact the repositories (in order of preference)
     * @param username   The username used for repositories secured with HTTP Basic authentication
     * @param password   The password used for repositories secured with HTTP Basic authentication
     * @param monitor    The ongoing results of the download operations, with some monitoring logic
     * @param soFar      The set of dependencies already downloaded.
     * @param dependency Is this a dependency or the original artifact?
     * @throws IOException                When there's a problem reading or writing data
     * @throws FailedLoginException       When a repository requires authentication and either no username
     *                                    and password are supplied or the username and password supplied
     *                                    are not accepted
     * @throws MissingDependencyException When a dependency cannot be located in any of the listed repositories
     */
    private void downloadArtifact(Artifact configID, Map<Artifact, PluginType> metadata, List<String> repos, String username, String password, ResultsFileWriteMonitor monitor, Set<Artifact> soFar, boolean dependency) throws IOException, FailedLoginException, MissingDependencyException {
        if (soFar.contains(configID)) {
            return; // Avoid endless work due to circular dependencies
        } else {
            soFar.add(configID);
        }
        // Download and install the main artifact
        boolean pluginWasInstalled = false;
        Artifact[] matches = configManager.getArtifactResolver().queryArtifacts(configID);
        if (matches.length == 0) {
            // not present, needs to be downloaded
            monitor.getResults().setCurrentMessage("Downloading " + configID);
            monitor.getResults().setCurrentFilePercent(-1);
            OpenResult result = openStream(configID, repos, username, password, monitor);
            // Check if the result is already in server's repository
            if (configManager.getArtifactResolver().queryArtifacts(result.getConfigID()).length > 0) {
                String msg = "Not downloading " + configID + ". Query for " + configID + " resulted in " + result.getConfigID()
                        + " which is already available in server's repository.";
                monitor.getResults().setCurrentMessage(msg);
                log.info(msg);
                if (result.getStream() != null) {
                    try {
                        result.getStream().close();
                    } catch (IOException ignored) {
                        //ignore
                    }
                }
                return;
            }
            try {
                File tempFile = downloadFile(result, monitor);
                if (tempFile == null) {
                    log.error("Null filehandle was returned for " + configID);
                    throw new IllegalArgumentException("Null filehandle was returned for " + configID);
                }
                PluginType pluginData = metadata.get(configID);
                // Only bother with the hash if we got it from a source other than the download file itself
                HashType hash = pluginData == null ? null : pluginData.getPluginArtifact().get(0).getHash();
                if (hash != null) {
                    String actual = ConfigurationStoreUtil.getActualChecksum(tempFile, hash.getType());
                    if (!actual.equals(hash.getValue())) {
                        log.error(
                                "File download incorrect (expected " + hash.getType() + " hash " + hash.getValue() + " but got " + actual + ")");
                        throw new IOException(
                                "File download incorrect (expected " + hash.getType() + " hash " + hash.getValue() + " but got " + actual + ")");
                    }
                }
                // See if the download file has plugin metadata and use it in preference to what is in the catalog.
                try {
                    PluginType realPluginData = loadCARFile(tempFile, false);
                    if (realPluginData != null) {
                        pluginData = realPluginData;
                    }
                } catch (Exception e) {
                    log.error("Unable to read plugin metadata: " + e.getMessage());
                    throw (IOException) new IOException(
                            "Unable to read plugin metadata: " + e.getMessage()).initCause(e);
                }
                PluginArtifactType instance = null;
                if (pluginData != null) { // it's a plugin, not a plain JAR
                    validatePlugin(pluginData);
                    instance = pluginData.getPluginArtifact().get(0);
                }
                monitor.getResults().setCurrentMessage("Copying " + result.getConfigID() + " to the repository");
                writeableRepo.copyToRepository(tempFile, result.getConfigID(),
                        monitor); //todo: download SNAPSHOTS if previously available?
                if (!tempFile.delete()) {
                    log.warn("Unable to delete temporary download file " + tempFile.getAbsolutePath());
                    tempFile.deleteOnExit();
                }
                if (pluginData != null) {
                    installConfigXMLData(result.getConfigID(), instance);
                } else {
                    log.debug("No config XML data to install.");
                }
                if (dependency) {
                    monitor.getResults().addDependencyInstalled(configID);
                    configID = result.getConfigID();
                } else {
                    configID = result.getConfigID();
                    monitor.getResults().addInstalledConfigID(configID);
                }
                pluginWasInstalled = true;
                if (pluginData != null)
                    log.info(
                            "Installed plugin with moduleId=" + pluginData.getPluginArtifact().get(0).getModuleId() + " and name=" + pluginData.getName());
                else
                    log.info("Installed artifact=" + configID);
            } catch (InvalidGBeanException e) {
                log.error("Invalid gbean configuration ", e);
                throw new IllegalStateException(
                        "Invalid GBean configuration: " + e.getMessage(), e);

            } finally {
                result.getStream().close();
            }
        } else {
            if (dependency) {
                monitor.getResults().addDependencyPresent(configID);
            } else {
                monitor.getResults().addInstalledConfigID(configID);
            }
        }
        // Download and install the dependencies
        try {
            ConfigurationData data = null;
            if (!configID.isResolved()) {
                // See if something's running
                for (int i = matches.length - 1; i >= 0; i--) {
                    Artifact match = matches[i];
                    if (configStore.containsConfiguration(match) && configManager.isRunning(match)) {
                        log.debug("Found required configuration=" + match + " and it is running.");
                        return; // its dependencies must be OK
                    } else {
                        log.debug("Either required configuration=" + match + " is not installed or it is not running.");
                    }
                }
                // Go with something that's installed
                configID = matches[matches.length - 1];
            }
            if (configStore.containsConfiguration(configID)) {
                if (configManager.isRunning(configID)) {
                    return; // its dependencies must be OK
                }
                log.debug("Loading configuration=" + configID);
                data = configStore.loadConfiguration(configID);
            }
            Dependency[] dependencies = data == null ? getDependencies(writeableRepo, configID) : getDependencies(data);
            // Download the dependencies
            for (Dependency dep : dependencies) {
                Artifact artifact = dep.getArtifact();
                log.debug("Attempting to download dependency=" + artifact + " for configuration=" + configID);
                downloadArtifact(artifact, metadata, repos, username, password, monitor, soFar, true);
            }
        } catch (NoSuchConfigException e) {
            log.error("Installed configuration into repository but ConfigStore does not see it: " + e.getMessage());
            throw new IllegalStateException(
                    "Installed configuration into repository but ConfigStore does not see it: " + e.getMessage(), e);
        } catch (InvalidConfigException e) {
            log.error("Installed configuration into repository but ConfigStore cannot load it: " + e.getMessage());
            throw new IllegalStateException(
                    "Installed configuration into repository but ConfigStore cannot load it: " + e.getMessage(), e);
        }
        // Copy any files out of the artifact
        PluginType currentPlugin = configManager.isConfiguration(configID) ? getPluginMetadata(configID) : null;
        if (pluginWasInstalled && currentPlugin != null) {
            extractPluginFiles(configID, currentPlugin, monitor);
        }
    }

    private void extractPluginFiles(Artifact configID, PluginType currentPlugin, ResultsFileWriteMonitor monitor) throws IOException {
        PluginArtifactType instance = currentPlugin.getPluginArtifact().get(0);
        for (CopyFileType data : instance.getCopyFile()) {
            monitor.getResults().setCurrentFilePercent(-1);
            monitor.getResults().setCurrentFile(data.getValue());
            monitor.getResults().setCurrentMessage(
                    "Copying " + data.getValue() + " from plugin to Geronimo installation");
            Set<URL> set;
            String sourceFile = data.getValue().trim();
            try {
                set = configStore.resolve(configID, null, sourceFile);
            } catch (NoSuchConfigException e) {
                log.error("Unable to identify module " + configID + " to copy files from");
                throw new IllegalStateException("Unable to identify module " + configID + " to copy files from", e);
            }
            if (set.size() == 0) {
                log.error("Installed configuration into repository but cannot locate file to copy " + sourceFile);
                continue;
            }
            boolean relativeToServer = "server".equals(data.getRelativeTo());
            File targetDir = relativeToServer ? serverInfo.resolveServer(
                    data.getDestDir()) : serverInfo.resolve(data.getDestDir());

            createDirectory(targetDir);
            if (!targetDir.isDirectory()) {
                log.error(
                        "Plugin install cannot write file " + data.getValue() + " to " + data.getDestDir() + " because " + targetDir.getAbsolutePath() + " is not a directory");
                continue;
            }
            if (!targetDir.canWrite()) {
                log.error(
                        "Plugin install cannot write file " + data.getValue() + " to " + data.getDestDir() + " because " + targetDir.getAbsolutePath() + " is not writable");
                continue;
            }
            for (URL url : set) {
                String path = url.getPath();
                if (path.lastIndexOf('/') > -1) {
                    path = path.substring(path.lastIndexOf('/'));
                }
                File target = new File(targetDir, path);
                if (!target.exists()) {
                    if (!target.createNewFile()) {
                        log.error("Plugin install cannot create new file " + target.getAbsolutePath());
                        continue;
                    }
                }
                if (!target.canWrite()) {
                    log.error("Plugin install cannot write to file " + target.getAbsolutePath());
                    continue;
                }
                copyFile(url.openStream(), new FileOutputStream(target));
            }
        }
    }

    private static void createDirectory(File dir) throws IOException {
        if (dir != null && !dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                throw new IOException("Cannot create directory " + dir.getAbsolutePath());
            }
        }
    }

    private void copyFile(InputStream in, FileOutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int count;
        while ((count = in.read(buf)) > -1) {
            out.write(buf, 0, count);
        }
        in.close();
        out.flush();
        out.close();
    }

    /**
     * Downloads to a temporary file so we can validate the download before
     * installing into the repository.
     *
     * @param result  source of download
     * @param monitor monitor to report results of download
     * @return downloaded file
     * @throws IOException if input cannot be read or file cannot be written
     */
    private File downloadFile(OpenResult result, ResultsFileWriteMonitor monitor) throws IOException {
        InputStream in = result.getStream();
        if (in == null) {
            log.error("Invalid InputStream for downloadFile");
            throw new IllegalStateException();
        }
        FileOutputStream out = null;
        byte[] buf;
        try {
            monitor.writeStarted(result.getConfigID().toString(), result.fileSize);
            File file = File.createTempFile("geronimo-plugin-download-", ".tmp");
            out = new FileOutputStream(file);
            buf = new byte[65536];
            int count, total = 0;
            while ((count = in.read(buf)) > -1) {
                out.write(buf, 0, count);
                monitor.writeProgress(total += count);
            }
            monitor.writeComplete(total);
            log.info(((DownloadResults) monitor.getResults()).getCurrentMessage());
            in.close();
            in = null;
            out.close();
            out = null;
            return file;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    //ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                    //ignore
                }
            }
        }
    }

    /**
     * Used to get dependencies for a JAR
     */
    private static Dependency[] getDependencies(Repository repo, Artifact artifact) {
        Set<Artifact> set = repo.getDependencies(artifact);
        Dependency[] results = new Dependency[set.size()];
        int index = 0;
        for (Artifact dep : set) {
            results[index] = new Dependency(dep, ImportType.CLASSES);
            ++index;
        }
        return results;
    }

    /**
     * Used to get dependencies for a Configuration
     *
     * @param data configuration data
     * @return dependencies of configuration
     */
    private static Dependency[] getDependencies(ConfigurationData data) {
        List<Dependency> dependencies = new ArrayList<Dependency>(data.getEnvironment().getDependencies());
        Collection<ConfigurationData> children = data.getChildConfigurations().values();
        for (ConfigurationData child : children) {
            dependencies.addAll(child.getEnvironment().getDependencies());
        }
        return (Dependency[]) dependencies.toArray(new Dependency[dependencies.size()]);
    }

    /**
     * Constructs a URL to a particular artifact in a particular repository
     */
    private static URL getURL(Artifact configId, URL repository) throws MalformedURLException {
        URL context;
        if (repository.toString().trim().endsWith("/")) {
            context = repository;
        } else {
            context = new URL(repository.toString().trim() + "/");
        }

        String qualifiedVersion = configId.getVersion().toString();
        if (configId.getVersion() instanceof SnapshotVersion) {
            SnapshotVersion ssVersion = (SnapshotVersion) configId.getVersion();
            String timestamp = ssVersion.getTimestamp();
            int buildNumber = ssVersion.getBuildNumber();
            if (timestamp != null && buildNumber != 0) {
                qualifiedVersion = qualifiedVersion.replaceAll("SNAPSHOT", timestamp + "-" + buildNumber);
            }
        }
        return new URL(context, configId.getGroupId().replace('.', '/') + "/"
                + configId.getArtifactId() + "/" + configId.getVersion()
                + "/" + configId.getArtifactId() + "-"
                + qualifiedVersion + "." + configId.getType());
    }

    /**
     * Attemps to open a stream to an artifact in one of the listed repositories.
     * The username and password provided are only used if one of the repositories
     * returns an HTTP authentication failure on the first try.
     *
     * @param artifact The artifact we're looking for, or null to just connect to the base repo URL
     * @param repos    The base URLs to the repositories to search for the artifact
     * @param username A username if one of the repositories might require authentication
     * @param password A password if one of the repositories might require authentication
     * @param monitor  Callback for progress on the connection operation
     * @throws IOException                Occurs when the IO with the repository failed
     * @throws FailedLoginException       Occurs when a repository requires authentication and either
     *                                    no username and password were provided or they weren't
     *                                    accepted
     * @throws MissingDependencyException Occurs when none of the repositories has the artifact
     *                                    in question
     */
    private static OpenResult openStream(Artifact artifact, List<String> repos, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException, MissingDependencyException {
        if (artifact != null) {
            if (!artifact.isResolved() || artifact.getVersion().toString().indexOf("SNAPSHOT") >= 0) {
                artifact = findArtifact(artifact, repos, username, password, monitor);
            }
        }
        if (monitor != null) {
            monitor.getResults().setCurrentFilePercent(-1);
            monitor.getResults().setCurrentMessage("Downloading " + artifact + "...");
            monitor.setTotalBytes(-1); // In case the server doesn't say
        }
        InputStream in;
        LinkedList<String> list = new LinkedList<String>();
        list.addAll(repos);
        while (true) {
            if (list.isEmpty()) {
                log.error("Unable to download dependency artifact=" + artifact);
                throw new MissingDependencyException("Unable to download dependency " + artifact);
            }
            if (monitor != null) {
                monitor.setTotalBytes(-1); // Just to be sure
            }
            String repo = list.removeFirst();
            URL repository = PluginRepositoryDownloader.resolveRepository(repo);
            if (repository == null) {
                log.info("Failed to resolve repository: " + repo);
                continue;
            }
            URL url = artifact == null ? repository : getURL(artifact, repository);
            if (artifact != null)
                log.info("Attempting to download " + artifact + " from " + url);
            else
                log.info("Attempting to download " + url);
            in = connect(url, username, password, monitor);
            if (in != null) {
                return new OpenResult(artifact, in, monitor == null ? -1 : monitor.getTotalBytes());
            } else {
                log.info("Failed to download artifact=" + artifact + " from url=" + url);
            }
        }
    }

    /**
     * Does the meat of connecting to a URL
     */
    private static InputStream connect(URL url, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException {
        return connect(url, username, password, monitor, null);
    }

    /**
     * Does the meat of connecting to a URL.  Can be used to just test the existance of
     * something at the specified URL by passing the method 'HEAD'.
     */
    private static InputStream connect(URL url, String username, String password, ResultsFileWriteMonitor monitor, String method) throws IOException, FailedLoginException {
        URLConnection con = url.openConnection();
        if (con instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (method != null) {
                http.setRequestMethod(method);
            }
            http.connect();
            if (http.getResponseCode() == 401) { // need to authenticate
                if (username == null || username.equals("")) {
                    log.error("Server returned 401 " + http.getResponseMessage());
                    throw new FailedLoginException("Server returned 401 " + http.getResponseMessage());
                }
                http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("Authorization",
                        "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                if (method != null) {
                    http.setRequestMethod(method);
                }
                http.connect();
                if (http.getResponseCode() == 401) {
                    log.error("Server returned 401 " + http.getResponseMessage());
                    throw new FailedLoginException("Server returned 401 " + http.getResponseMessage());
                } else if (http.getResponseCode() == 404) {
                    return null; // Not found at this repository
                }
                if (monitor != null && http.getContentLength() > 0) {
                    monitor.setTotalBytes(http.getContentLength());
                }
                return http.getInputStream();
            } else if (http.getResponseCode() == 404) {
                return null; // Not found at this repository
            } else {
                if (monitor != null && http.getContentLength() > 0) {
                    monitor.setTotalBytes(http.getContentLength());
                }
                return http.getInputStream();
            }
        } else {
            if (username != null && !username.equals("")) {
                con.setRequestProperty("Authorization",
                        "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                try {
                    con.connect();
                    if (monitor != null && con.getContentLength() > 0) {
                        monitor.setTotalBytes(con.getContentLength());
                    }
                    return con.getInputStream();
                } catch (FileNotFoundException e) {
                    return null;
                }
            } else {
                try {
                    con.connect();
                    if (monitor != null && con.getContentLength() > 0) {
                        monitor.setTotalBytes(con.getContentLength());
                    }
                    return con.getInputStream();
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
        }
    }

    /**
     * Searches for an artifact in the listed repositories, where the artifact
     * may have wildcards in the ID.
     */
    private static Artifact findArtifact(Artifact query, List<String> repos, String username, String password, ResultsFileWriteMonitor monitor) throws MissingDependencyException {
        if (query.getGroupId() == null || query.getArtifactId() == null || query.getType() == null) {
            log.error("No support yet for dependencies missing more than a version: " + query);
            throw new MissingDependencyException(
                    "No support yet for dependencies missing more than a version: " + query);
        }
        for (String repo : repos) {
            try {
                URL repoUrl = PluginRepositoryDownloader.resolveRepository(repo);
                if (repoUrl != null) {
                    Artifact result = findArtifact(query, repoUrl, username, password, monitor);
                    if (result != null) {
                        return result;
                    }
                } else {
                    log.warn("could not resolve repo: " + repo);
                }
            } catch (Exception e) {
                log.warn("Unable to read from " + repo, e);
            }
        }
        log.error("No repository has a valid artifact for " + query);
        throw new MissingDependencyException("No repository has a valid artifact for " + query);
    }

    /**
     * Checks for an artifact in a specific repository, where the artifact may
     * have wildcards in the ID.
     */
    private static Artifact findArtifact(Artifact query, URL url, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException, ParserConfigurationException, SAXException {
        if (query.isResolved() && testArtifact(query, url, username, password, monitor)) {
            return query;
        }
        monitor.getResults().setCurrentMessage("Searching for " + query + " at " + url);
        String base = query.getGroupId().replace('.', '/') + "/" + query.getArtifactId();
        String path = base + "/maven-metadata.xml";
        URL metaURL = new URL(url.toString().trim().endsWith("/") ? url : new URL(url.toString().trim() + "/"), path);
        InputStream in = connect(metaURL, username, password, monitor);
        if (in == null) {
            log.error("No meta-data file was found at " + metaURL.toString());
            path = base + "/maven-metadata-local.xml";
            metaURL = new URL(url.toString().endsWith("/") ? url : new URL(url.toString() + "/"), path);
            in = connect(metaURL, username, password, monitor);
        }
        if (in == null) {
            log.error("No meta-data file was found at " + metaURL.toString());
            return null;
        }

        // Don't use the validating parser that we normally do
        DocumentBuilder builder = XmlUtil.newDocumentBuilderFactory().newDocumentBuilder();
        Document doc = builder.parse(in);
        Element root = doc.getDocumentElement();
        NodeList list = root.getElementsByTagName("versions");
        if (list.getLength() == 0) {
            log.error("No artifact versions found in " + metaURL.toString());
            return null;
        }
        list = ((Element) list.item(0)).getElementsByTagName("version");
        Version[] available = new Version[list.getLength()];
        for (int i = 0; i < available.length; i++) {
            available[i] = new Version(getText(list.item(i)));
        }
        List availableList = Arrays.asList(available);
        if (availableList.contains(query.getVersion())) {
            available = new Version[]{query.getVersion()};
        } else {
            Arrays.sort(available);
        }
        for (int i = available.length - 1; i >= 0; i--) {
            Version version = available[i];
            URL metadataURL = new URL(url.toString().trim().endsWith("/") ? url : new URL(url.toString().trim() + "/"),
                    base + "/" + version + "/maven-metadata.xml");
            InputStream metadataStream = connect(metadataURL, username, password, monitor);

            if (metadataStream == null) {
                metadataURL = new URL(url.toString().trim().endsWith("/") ? url : new URL(url.toString().trim() + "/"),
                        base + "/" + version + "/maven-metadata-local.xml");
                metadataStream = connect(metadataURL, username, password, monitor);
            }
            // check for a snapshot qualifier
            if (metadataStream != null) {
                DocumentBuilder metadatabuilder = XmlUtil.newDocumentBuilderFactory().newDocumentBuilder();
                Document metadatadoc = metadatabuilder.parse(metadataStream);
                NodeList snapshots = metadatadoc.getDocumentElement().getElementsByTagName("snapshot");
                if (snapshots.getLength() >= 1) {
                    Element snapshot = (Element) snapshots.item(0);
                    String[] timestamp = getChildrenText(snapshot, "timestamp");
                    String[] buildNumber = getChildrenText(snapshot, "buildNumber");
                    if (timestamp.length >= 1 && buildNumber.length >= 1) {
                        try {
                            SnapshotVersion snapshotVersion = new SnapshotVersion(version);
                            snapshotVersion.setBuildNumber(Integer.parseInt(buildNumber[0]));
                            snapshotVersion.setTimestamp(timestamp[0]);
                            version = snapshotVersion;
                        } catch (NumberFormatException nfe) {
                            log.warn("Could not create snapshot version for " + query);
                        }
                    }
                }
                metadataStream.close();
            }

            // look for the artifact in the maven repo
            Artifact verifiedArtifact = new Artifact(query.getGroupId(), query.getArtifactId(), version,
                    query.getType());
            if (!testArtifact(verifiedArtifact, url, username, password, monitor)) {
                log.debug("Maven repository " + url + " listed artifact " + query + " version " + version + " but I couldn't find it at " + getURL(verifiedArtifact, url));
                continue;
            }
            log.debug("Found artifact at " + getURL(verifiedArtifact, url));
            return verifiedArtifact;
        }
        log.error("Could not find an acceptable version of artifact=" + query + " from Maven repository=" + url);
        return null;
    }

    private static boolean testArtifact(Artifact artifact, URL repo, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException {
        URL test = getURL(artifact, repo);
        InputStream testStream = connect(test, username, password, monitor, "HEAD");
        if (testStream != null) {
            testStream.close();
        }
        return testStream != null;

    }

    /**
     * Puts the name and ID of a plugin into the argument map of plugins,
     * by reading the values out of the provided plugin descriptor file.
     *
     * @param xml     The geronimo-plugin.xml for this plugin
     * @param plugins The result map to populate
     */
    private void readNameAndID(File xml, Map plugins) {
        try {
            SAXParserFactory factory = XmlUtil.newSAXParserFactory();
            SAXParser parser = factory.newSAXParser();
            PluginNameIDHandler handler = new PluginNameIDHandler();
            parser.parse(xml, handler);
            if (handler.isComplete()) {
                plugins.put(handler.getName(), Artifact.create(handler.getID()));
            }
        } catch (Exception e) {
            log.warn("Invalid XML at " + xml.getAbsolutePath(), e);
        }
    }

    /**
     * Puts the name and ID of a plugin into the argument map of plugins,
     * by reading the values out of the provided plugin descriptor stream.
     *
     * @param xml     The geronimo-plugin.xml for this plugin
     * @param plugins The result map to populate
     */
    private void readNameAndID(InputStream xml, Map plugins) {
        try {
            SAXParserFactory factory = XmlUtil.newSAXParserFactory();
            SAXParser parser = factory.newSAXParser();
            PluginNameIDHandler handler = new PluginNameIDHandler();
            parser.parse(xml, handler);
            if (handler.isComplete()) {
                plugins.put(handler.getName(), Artifact.create(handler.getID()));
            }
        } catch (Exception e) {
            log.warn("Invalid XML", e);
        }
    }

    /**
     * Replaces all the dependency elements in the argument configuration data
     * with the dependencies from the actual data for that module.
     */
    private void overrideDependencies(ConfigurationData data, PluginType metadata) {
        //todo: this ends up doing a little more work than necessary
        PluginType temp = createDefaultMetadata(data);
        PluginArtifactType instance = temp.getPluginArtifact().get(0);
        List<DependencyType> dependencyTypes = metadata.getPluginArtifact().get(0).getDependency();
        dependencyTypes.clear();
        dependencyTypes.addAll(instance.getDependency());
    }

    /**
     * Generates a default plugin metadata based on the data for this module
     * in the server.
     */
    private PluginType createDefaultMetadata(ConfigurationData data) {
        PluginType meta = new PluginType();
        PluginArtifactType instance = new PluginArtifactType();
        meta.getPluginArtifact().add(instance);
        meta.setName(toArtifactType(data.getId()).toString());
        instance.setModuleId(toArtifactType(data.getId()));
        meta.setCategory("Unknown");
//        true, // installed
//                false);
        instance.getGeronimoVersion().add(serverInfo.getVersion());
        instance.getObsoletes().add(toArtifactType(new Artifact(data.getId().getGroupId(),
                data.getId().getArtifactId(),
                (Version) null,
                data.getId().getType())));
        List<DependencyType> deps = instance.getDependency();
        PrerequisiteType prereq = null;
        prereq = processDependencyList(data.getEnvironment().getDependencies(), prereq, deps);
        Map children = data.getChildConfigurations();
        for (Iterator it = children.values().iterator(); it.hasNext();) {
            ConfigurationData child = (ConfigurationData) it.next();
            prereq = processDependencyList(child.getEnvironment().getDependencies(), prereq, deps);
        }
        if (prereq != null) {
            instance.getPrerequisite().add(prereq);
        }
        return meta;
    }

    /**
     * Read the plugin metadata out of a plugin CAR file on disk.
     */
    private PluginType loadCARFile(File file, boolean definitelyCAR) throws IOException, ParserConfigurationException, SAXException, JAXBException, XMLStreamException {
        if (!file.canRead()) {
            log.error("Cannot read from downloaded CAR file " + file.getAbsolutePath());
            return null;
        }
        JarFile jar = new JarFile(file);
        try {
            JarEntry entry = jar.getJarEntry("META-INF/geronimo-plugin.xml");
            if (entry == null) {
                if (definitelyCAR) {
                    log.error("Downloaded CAR file does not contain META-INF/geronimo-plugin.xml file");
                }
                return null;
            }
            InputStream in = jar.getInputStream(entry);
            try {
                return loadPluginMetadata(in);
            } finally {
                in.close();
            }
        } finally {
            jar.close();
        }
    }

    /**
     * Read a set of plugin metadata from a DOM document.
     */
    public static PluginType loadPluginMetadata(InputStream in) throws SAXException, MalformedURLException, JAXBException, XMLStreamException {
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        Unmarshaller unmarshaller = PLUGIN_CONTEXT.createUnmarshaller();
        JAXBElement<PluginType> element = unmarshaller.unmarshal(xmlStream, PluginType.class);
        PluginType plugin = element.getValue();
        return plugin;
    }

    public static PluginArtifactType loadPluginArtifactMetadata(Reader in) throws SAXException, MalformedURLException, JAXBException, XMLStreamException, ParserConfigurationException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        Unmarshaller unmarshaller = PLUGIN_ARTIFACT_CONTEXT.createUnmarshaller();
//        unmarshaller.setEventHandler(new ValidationEventHandler(){
//            public boolean handleEvent(ValidationEvent validationEvent) {
//                System.out.println(validationEvent);
//                return false;
//            }
//        });


        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);
//        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<PluginArtifactType> element = unmarshaller.unmarshal(source, PluginArtifactType.class);
        PluginArtifactType plugin = element.getValue();
        return plugin;
    }

    public static class NamespaceFilter extends XMLFilterImpl {
        private static String PLUGIN_NS = "http://geronimo.apache.org/xml/ns/plugins-1.3";
        private static String GBEAN_NS = "http://geronimo.apache.org/xml/ns/attributes-1.2";
        private static String ENVIRONMENT_NS = "http://geronimo.apache.org/xml/ns/deployment-1.2";

        private String namespace;

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            if ("plugin-artifact".equals(localName)) {
                namespace = PLUGIN_NS;
            } else if ("gbean".equals(localName)) {
                namespace = GBEAN_NS;
            } else if ("environment".equals(localName)) {
                namespace = ENVIRONMENT_NS;
            }
            super.startElement(namespace, localName, qname, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(namespace, localName, qName);
            if ("plugin-artifact".equals(localName)) {
                namespace = null;
            } else if ("gbean".equals(localName)) {
                namespace = PLUGIN_NS;
            } else if ("environment".equals(localName)) {
                namespace = GBEAN_NS;
            }
        }
    }

    /**
     * Loads the list of all available plugins from the specified stream
     * (representing geronimo-plugins.xml at the specified repository).
     */
    public static PluginListType loadPluginList(InputStream in) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = PLUGIN_LIST_CONTEXT.createUnmarshaller();
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<PluginListType> element = unmarshaller.unmarshal(xmlStream, PluginListType.class);
        PluginListType pluginList = element.getValue();
        return pluginList;
    }

    /**
     * Check whether the specified JVM versions match the current runtime
     * environment.
     *
     * @return true if the specified versions match the current
     *         execution environment as defined by plugins-1.2.xsd
     */
    private boolean checkJVMVersions(List<String> jvmVersions) {
        if (jvmVersions.size() == 0) return true;
        String version = System.getProperty("java.version");
        boolean match = false;
        for (String jvmVersion : jvmVersions) {
            if (jvmVersion == null || jvmVersion.equals("")) {
                log.error("jvm-version should not be empty!");
                throw new IllegalStateException("jvm-version should not be empty!");
            }
            if (version.startsWith(jvmVersion)) {
                match = true;
                break;
            }
        }
        return match;
    }

    /**
     * Check whether the specified Geronimo versions match the current runtime
     * environment.
     *
     * @return true if the specified versions match the current
     *         execution environment as defined by plugins-1.2.xsd
     */
    private boolean checkGeronimoVersions(List<String> gerVersions) throws IllegalStateException {
        if ((gerVersions == null) || (gerVersions.size() == 0)) {
            return true;
        }

        boolean match = false;
        for (String gerVersion : gerVersions) {
            match = checkGeronimoVersion(gerVersion);
            if (match) {
                break;
            }
        }
        return match;
    }

    /**
     * Check whether the specified Geronimo version matches the current runtime
     * environment.
     *
     * @return true if the specified version matches the current
     *         execution environment as defined by plugins-1.2.xsd
     */
    private boolean checkGeronimoVersion(String gerVersion) throws IllegalStateException {
        String version = serverInfo.getVersion();

        if ((gerVersion == null) || gerVersion.equals("")) {
            log.error("geronimo-version cannot be empty!");
            throw new IllegalStateException("geronimo-version cannot be empty!");
        } else if (gerVersion.equals(version)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets all the text contents of the specified DOM node.
     */
    private static String getText(Node target) {
        NodeList nodes = target.getChildNodes();
        StringBuffer buf = null;
        for (int j = 0; j < nodes.getLength(); j++) {
            Node node = nodes.item(j);
            if (node.getNodeType() == Node.TEXT_NODE) {
                if (buf == null) {
                    buf = new StringBuffer();
                }
                buf.append(node.getNodeValue());
            }
        }
        return buf == null ? null : buf.toString();
    }

    /**
     * Gets the text out of all the child nodes of a certain type.  The result
     * array has one element for each child of the specified DOM element that
     * has the specified name.
     *
     * @param root     The parent DOM element
     * @param property The name of the child elements that hold the text
     */
    private static String[] getChildrenText(Element root, String property) {
        NodeList children = root.getChildNodes();
        List results = new ArrayList();
        for (int i = 0; i < children.getLength(); i++) {
            Node check = children.item(i);
            if (check.getNodeType() == Node.ELEMENT_NODE && check.getNodeName().equals(property)) {
                NodeList nodes = check.getChildNodes();
                StringBuffer buf = null;
                for (int j = 0; j < nodes.getLength(); j++) {
                    Node node = nodes.item(j);
                    if (node.getNodeType() == Node.TEXT_NODE) {
                        if (buf == null) {
                            buf = new StringBuffer();
                        }
                        buf.append(node.getNodeValue());
                    }
                }
                results.add(buf == null ? null : buf.toString());
            }
        }
        return (String[]) results.toArray(new String[results.size()]);
    }

    /**
     * Generates dependencies and an optional prerequisite based on a list of
     * dependencies for a Gernonimo module.
     *
     * @param real   A list with elements of type Dependency
     * @param prereq The incoming prerequisite (if any), which may be replaced
     * @param deps   A list with elements of type String (holding a module ID / Artifact name)
     * @return The resulting prerequisite, if any.
     */
    private PrerequisiteType processDependencyList(List<Dependency> real, PrerequisiteType prereq, List<DependencyType> deps) {
        for (Dependency dep : real) {
            DependencyType dependency = toDependencyType(dep);
            if (!deps.contains(dependency)) {
                deps.add(dependency);
            }
        }
        return prereq;
    }

    public static DependencyType toDependencyType(Dependency dep) {
        Artifact id = dep.getArtifact();
        DependencyType dependency = new DependencyType();
        dependency.setGroupId(id.getGroupId());
        dependency.setArtifactId(id.getArtifactId());
        dependency.setVersion(id.getVersion() == null ? null : id.getVersion().toString());
        dependency.setType(id.getType());
        return dependency;
    }

    public static Artifact toArtifact(ArtifactType moduleId) {
        String groupId = moduleId.getGroupId();
        String artifactId = moduleId.getArtifactId();
        String version = moduleId.getVersion();
        String type = moduleId.getType();
        return new Artifact(groupId, artifactId, version, type);
    }

    public static ArtifactType toArtifactType(Artifact id) {
        ArtifactType artifact = new ArtifactType();
        artifact.setGroupId(id.getGroupId());
        artifact.setArtifactId(id.getArtifactId());
        artifact.setVersion(id.getVersion() == null ? null : id.getVersion().toString());
        artifact.setType(id.getType());
        return artifact;
    }

    public static PluginType copy(PluginType metadata, PluginArtifactType instance) {
        PluginType copy = new PluginType();
        copy.setAuthor(metadata.getAuthor());
        copy.setCategory(metadata.getCategory());
        copy.setDescription(metadata.getDescription());
        copy.setName(metadata.getName());
        copy.setUrl(metadata.getUrl());
        copy.getLicense().addAll(metadata.getLicense());
        if (instance != null) {
            copy.getPluginArtifact().add(instance);
        }
        return copy;
    }

    public static PluginType toKey(PluginType metadata) {
        PluginType copy = new PluginKey();
        copy.setAuthor(metadata.getAuthor());
        copy.setCategory(metadata.getCategory());
        copy.setName(metadata.getName());
        copy.setUrl(metadata.getUrl());
        copy.getLicense().addAll(metadata.getLicense());
        return copy;
    }

    private static class PluginKey extends PluginType {
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PluginKey that = (PluginKey) o;

            if (author != null ? !author.equals(that.author) : that.author != null) return false;
            if (category != null ? !category.equals(that.category) : that.category != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;
            if ((license == null) != (that.license == null)) return false;
            if (license != null) {
                if (license.size() != that.license.size()) return false;
                int i = 0;
                for (LicenseType licenseType : license) {
                    LicenseType otherLicense = that.license.get(i++);
                    if (licenseType.isOsiApproved() != otherLicense.isOsiApproved()) return false;
                    if (licenseType.getValue() != null ? !licenseType.getValue().equals(otherLicense.getValue()) : otherLicense.getValue() != null) return false;
                }
            }

            return true;
        }

        public int hashCode() {
            int result;
            result = (name != null ? name.hashCode() : 0);
            result = 31 * result + (category != null ? category.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (author != null ? author.hashCode() : 0);
            return result;
        }
    }

    public PluginListType createPluginListForRepositories(ConfigurationManager mgr, String repo) throws NoSuchStoreException {
        Map<PluginType, PluginType> pluginMap = new HashMap<PluginType, PluginType>();
        List<AbstractName> stores = mgr.listStores();
        for (AbstractName name : stores) {
            List<ConfigurationInfo> configs = mgr.listConfigurations(name);
            for (ConfigurationInfo info : configs) {
                PluginType data = getPluginMetadata(info.getConfigID());

                PluginType key = PluginInstallerGBean.toKey(data);
                PluginType existing = pluginMap.get(key);
                if (existing == null) {
                    pluginMap.put(key, data);
                } else {
                    existing.getPluginArtifact().addAll(data.getPluginArtifact());
                }
            }
        }
        PluginListType pluginList = new PluginListType();
        pluginList.getPlugin().addAll(pluginMap.values());
        pluginList.getDefaultRepository().add(repo);
        return pluginList;
    }


    /**
     * If a plugin includes config.xml content, copy it into the attribute
     * store.
     */
    private void installConfigXMLData(Artifact configID, PluginArtifactType pluginData) throws InvalidGBeanException, IOException {
        if (configManager.isConfiguration(configID) && attributeStore != null
                && pluginData != null && pluginData.getConfigXmlContent() != null) {
            attributeStore.setModuleGBeans(configID, pluginData.getConfigXmlContent().getGbean());
        }
        if (!pluginData.getConfigSubstitution().isEmpty() && attributeStore != null) {
            attributeStore.addConfigSubstitutions(toProperties(pluginData.getConfigSubstitution()));
        }
        if (!pluginData.getArtifactAlias().isEmpty() && artifactResolver != null) {
            artifactResolver.addAliases(toProperties(pluginData.getArtifactAlias()));
        }
    }

    private Properties toProperties(List<PropertyType> propertyTypes) {
        Properties properties = new Properties();
        for (PropertyType propertyType : propertyTypes) {
            properties.setProperty(propertyType.getKey(), propertyType.getValue());
        }
        return properties;
    }

    /**
     * Gets a token unique to this run of the server, used to track asynchronous
     * downloads.
     */
    private static Object getNextKey() {
        int value;
        synchronized (PluginInstallerGBean.class) {
            value = ++counter;
        }
        return new Integer(value);
    }

    /**
     * Helper clas to extract a name and module ID from a plugin metadata file.
     */
    private static class PluginNameIDHandler extends DefaultHandler {
        private String id = "";
        private String name = "";
        private String element = null;

        public void characters(char ch[], int start, int length) throws SAXException {
            if (element != null) {
                if (element.equals("module-id")) {
                    id += new String(ch, start, length);
                } else if (element.equals("name")) {
                    name += new String(ch, start, length);
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            element = null;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("module-id") || qName.equals("name")) {
                element = qName;
            }
        }

        public void endDocument() throws SAXException {
            id = id.trim();
            name = name.trim();
        }

        public String getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isComplete() {
            return !id.equals("") && !name.equals("");
        }
    }

    /**
     * Helper class to bridge a FileWriteMonitor to a DownloadPoller.
     */
    private static class ResultsFileWriteMonitor implements FileWriteMonitor {
        private final DownloadPoller results;
        private int totalBytes;
        private String file;

        public ResultsFileWriteMonitor(DownloadPoller results) {
            this.results = results;
        }

        public void setTotalBytes(int totalBytes) {
            this.totalBytes = totalBytes;
        }

        public int getTotalBytes() {
            return totalBytes;
        }

        public void writeStarted(String fileDescription, int fileSize) {
            totalBytes = fileSize;
            file = fileDescription;
            results.setCurrentFile(fileDescription);
            results.setCurrentFilePercent(totalBytes > 0 ? 0 : -1);
        }

        public void writeProgress(int bytes) {
            if (totalBytes > 0) {
                double percent = (double) bytes / (double) totalBytes;
                results.setCurrentFilePercent((int) (percent * 100));
            } else {
                results.setCurrentMessage((bytes / 1024) + " kB of " + file);
            }
        }

        public void writeComplete(int bytes) {
            results.setCurrentFilePercent(100);
            results.setCurrentMessage("Finished downloading " + file + " (" + (bytes / 1024) + " kB)");
            results.addDownloadBytes(bytes);
        }

        public DownloadPoller getResults() {
            return results;
        }
    }

    /**
     * Interesting data resulting from opening a connection to a remote file.
     */
    private static class OpenResult {
        private final InputStream stream;
        private final Artifact configID;
        private final int fileSize;

        public OpenResult(Artifact configID, InputStream stream, int fileSize) {
            this.configID = configID;
            this.stream = stream;
            this.fileSize = fileSize;
        }

        public Artifact getConfigID() {
            return configID;
        }

        public InputStream getStream() {
            return stream;
        }

        public int getFileSize() {
            return fileSize;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(PluginInstallerGBean.class);
        infoFactory.addReference("ConfigManager", ConfigurationManager.class, "ConfigurationManager");
        infoFactory.addReference("Repository", WritableListableRepository.class, "Repository");
        infoFactory.addReference("ConfigStore", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("ThreadPool", ThreadPool.class, "GBean");
        infoFactory.addReference("PluginAttributeStore", PluginAttributeStore.class, "AttributeStore");
        infoFactory.addReference("ArtifactResolver", AliasedArtifactResolver.class, "ArtifactResolver");
        infoFactory.addInterface(PluginInstaller.class);

        infoFactory.setConstructor(new String[]{"ConfigManager", "Repository", "ConfigStore",
                "ServerInfo", "ThreadPool", "PluginAttributeStore", "ArtifactResolver"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
