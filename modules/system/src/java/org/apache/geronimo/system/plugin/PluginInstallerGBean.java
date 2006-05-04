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
package org.apache.geronimo.system.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.security.auth.login.FailedLoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.system.configuration.ConfigurationStoreUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;
import org.apache.geronimo.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A GBean that knows how to download configurations from a Maven repository.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class PluginInstallerGBean implements PluginInstaller {
    private final static Log log = LogFactory.getLog(PluginInstallerGBean.class);
    private static int counter;
    private ConfigurationManager configManager;
    private WritableListableRepository writeableRepo;
    private ConfigurationStore configStore;
    private ArtifactResolver resolver;
    private ServerInfo serverInfo;
    private Map asyncKeys;
    private ThreadPool threadPool;

    public PluginInstallerGBean(ConfigurationManager configManager, WritableListableRepository repository, ConfigurationStore configStore, ServerInfo serverInfo, ThreadPool threadPool) {
        this.configManager = configManager;
        this.writeableRepo = repository;
        this.configStore = configStore;
        this.serverInfo = serverInfo;
        this.threadPool = threadPool;
        resolver = new DefaultArtifactResolver(null, writeableRepo);
        asyncKeys = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Lists the plugins installed in the local Geronimo server, by name and
     * ID.
     *
     * @return A Map with key type String (plugin name) and value type Artifact
     *         (config ID of the plugin).
     */
    public Map getInstalledPlugins() {
        SortedSet artifacts = writeableRepo.list();

        Map plugins = new HashMap();
        for (Iterator i = artifacts.iterator(); i.hasNext();) {
            Artifact configId = (Artifact) i.next();
            File dir = writeableRepo.getLocation(configId);
            if(dir.isDirectory()) {
                File meta = new File(dir, "META-INF");
                if(!meta.isDirectory() || !meta.canRead()) {
                    continue;
                }
                File xml = new File(meta, "geronimo-plugin.xml");
                if(!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                    continue;
                }
                readNameAndID(xml, plugins);
            } else {
                if(!dir.isFile() || !dir.canRead()) {
                    throw new IllegalStateException("Cannot read artifact dir "+dir.getAbsolutePath());
                }
                try {
                    JarFile jar = new JarFile(dir);
                    try {
                        ZipEntry entry = jar.getEntry("META-INF/geronimo-plugin.xml");
                        if(entry == null) {
                            continue;
                        }
                        InputStream in = jar.getInputStream(entry);
                        readNameAndID(in, plugins);
                        in.close();
                    } finally {
                        jar.close();
                    }
                } catch (IOException e) {
                    log.error("Unable to read JAR file "+dir.getAbsolutePath(), e);
                }
            }
        }
        return plugins;
    }

    /**
     * Gets a CofigurationMetadata for a configuration installed in the local
     * server.  Should load a saved one if available, or else create a new
     * default one to the best of its abilities.
     *
     * @param moduleId Identifies the configuration.  This must match a
     *                 configuration currently installed in the local server.
     *                 The configId must be fully resolved (isResolved() == true)
     */
    public PluginMetadata getPluginMetadata(Artifact moduleId) {
        File dir = writeableRepo.getLocation(moduleId);
        Document doc;
        ConfigurationData configData;
        String source = dir.getAbsolutePath();
        try {
            if(dir.isDirectory()) {
                File meta = new File(dir, "META-INF");
                if(!meta.isDirectory() || !meta.canRead()) {
                    return null;
                }
                File xml = new File(meta, "geronimo-plugin.xml");
                configData = configStore.loadConfiguration(moduleId);
                if(!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                    return createDefaultMetadata(configData);
                }
                source = xml.getAbsolutePath();
                DocumentBuilder builder = createDocumentBuilder();
                doc = builder.parse(xml);
            } else {
                if(!dir.isFile() || !dir.canRead()) {
                    throw new IllegalStateException("Cannot read configuration "+dir.getAbsolutePath());
                }
                configData = configStore.loadConfiguration(moduleId);
                JarFile jar = new JarFile(dir);
                try {
                    ZipEntry entry = jar.getEntry("META-INF/geronimo-plugin.xml");
                    if(entry == null) {
                        return createDefaultMetadata(configData);
                    }
                    source = dir.getAbsolutePath()+"#META-INF/geronimo-plugin.xml";
                    InputStream in = jar.getInputStream(entry);
                    DocumentBuilder builder = createDocumentBuilder();
                    doc = builder.parse(in);
                    in.close();
                } finally {
                    jar.close();
                }
            }
            PluginMetadata result = loadPluginMetadata(doc, source);
            overrideDependencies(configData, result);
            return result;
        } catch (InvalidConfigException e) {
            log.warn("Unable to generate metadata for "+moduleId, e);
        } catch (Exception e) {
            log.warn("Invalid XML at "+source, e);
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
    public void updatePluginMetadata(PluginMetadata metadata) {
        File dir = writeableRepo.getLocation(metadata.getModuleId());
        if(dir == null) {
            throw new IllegalArgumentException(metadata.getModuleId()+" is not installed!");
        }
        File meta = new File(dir, "META-INF");
        if(!meta.isDirectory() || !meta.canRead()) {
            throw new IllegalArgumentException(metadata.getModuleId()+" is not a plugin!");
        }
        File xml = new File(meta, "geronimo-plugin.xml");
        try {
            if(!xml.isFile()) {
                if(!xml.createNewFile()) {
                    throw new RuntimeException("Cannot create plugin metadata file for "+metadata.getModuleId());
                }
            }
            Document doc = writePluginMetadata(metadata);
            TransformerFactory xfactory = TransformerFactory.newInstance();
            Transformer xform = xfactory.newTransformer();
            xform.setOutputProperty(OutputKeys.INDENT, "yes");
            xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            xform.transform(new DOMSource(doc), new StreamResult(xml));
        } catch (Exception e) {
            log.error("Unable to save plugin metadata for "+metadata.getModuleId(), e);
        }
    }

    /**
     * Lists the plugins available for download in a particular Geronimo repository.
     *
     * @param mavenRepository The base URL to the maven repository.  This must
     *                        contain the file geronimo-plugins.xml
     * @param username Optional username, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     * @param password Optional password, if the maven repo uses HTTP Basic authentication.
     *                 Set this to null if no authentication is required.
     */
    public PluginList listPlugins(URL mavenRepository, String username, String password) throws IOException, FailedLoginException {
        String repository = mavenRepository.toString();
        if(!repository.endsWith("/")) {
            repository = repository+"/";
        }
        //todo: Try downloading a .gz first
        URL url = new URL(repository+"geronimo-plugins.xml");
        try {
            //todo: use a progress monitor
            InputStream in = openStream(null, new URL[]{url}, username, password, null).getStream();
            return loadPluginList(mavenRepository, in);
        } catch (MissingDependencyException e) {
            log.error("Cannot find plugin index at site "+url);
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
    public DownloadResults install(PluginList pluginsToInstall, String username, String password) {
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
    public void install(PluginList pluginsToInstall, String username, String password, DownloadPoller poller) {
        try {
            Map metaMap = new HashMap();
            // Step 1: validate everything
            for (int i = 0; i < pluginsToInstall.getPlugins().length; i++) {
                PluginMetadata metadata = pluginsToInstall.getPlugins()[i];
                validatePlugin(metadata);
                metaMap.put(metadata.getModuleId(), metadata);
            }

            // Step 2: everything is valid, do the installation
            for (int i = 0; i < pluginsToInstall.getPlugins().length; i++) {
                // 1. Identify the configuration
                PluginMetadata metadata = pluginsToInstall.getPlugins()[i];
                // 2. Unload obsoleted configurations
                List obsoletes = new ArrayList();
                for (int j = 0; j < metadata.getObsoletes().length; j++) {
                    String name = metadata.getObsoletes()[j];
                    Artifact obsolete = Artifact.create(name);
                    if(configManager.isLoaded(obsolete)) {
                        if(configManager.isRunning(obsolete)) {
                            configManager.stopConfiguration(obsolete);
                        }
                        configManager.unloadConfiguration(obsolete);
                        obsoletes.add(obsolete);
                    }
                }
                // 3. Download the artifact if necessary, and its dependencies
                Set working = new HashSet();
                if(metadata.getModuleId() != null) {
                    URL[] repos = pluginsToInstall.getRepositories();
                    if(metadata.getRepositories().length > 0) {
                        repos = metadata.getRepositories();
                    }
                    downloadArtifact(metadata.getModuleId(), metaMap, repos,
                            username, password, new ResultsFileWriteMonitor(poller), working);
                } else {
                    String[] deps = metadata.getDependencies();
                    for (int j = 0; j < deps.length; j++) {
                        String dep = deps[j];
                        Artifact entry = Artifact.create(dep);
                        if(configManager.isRunning(entry)) {
                            continue;
                        }
                        URL[] repos = pluginsToInstall.getRepositories();
                        if(metadata.getRepositories().length > 0) {
                            repos = metadata.getRepositories();
                        }
                        downloadArtifact(entry, metaMap, repos,
                                username, password, new ResultsFileWriteMonitor(poller), working);
                        poller.addInstalledConfigID(metadata.getModuleId());
                    }
                }
                // 4. Uninstall obsolete configurations
                for (int j = 0; j < obsoletes.size(); j++) {
                    Artifact artifact = (Artifact) obsoletes.get(j);
                    configManager.uninstallConfiguration(artifact);
                }
                // 5. Installation of this configuration finished successfully
                if(metadata.getModuleId() != null) {
                    poller.addInstalledConfigID(metadata.getModuleId());
                }
            }

            // Step 3: Start anything that's marked accordingly
            for (int i = 0; i < pluginsToInstall.getPlugins().length; i++) {
                PluginMetadata metadata = pluginsToInstall.getPlugins()[i];
                for (int j = 0; j < metadata.getForceStart().length; j++) {
                    String id = metadata.getForceStart()[j];
                    Artifact artifact = Artifact.create(id);
                    if(configManager.isConfiguration(artifact)) {
                        poller.setCurrentFilePercent(-1);
                        poller.setCurrentMessage("Starting "+artifact);
                        configManager.loadConfiguration(artifact);
                        configManager.startConfiguration(artifact);
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
     *
     * @return A key that can be passed to checkOnInstall
     */
    public Object startInstall(final PluginList pluginsToInstall, final String username, final String password) {
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
     * @param carFile   A CAR file downloaded from a remote repository.  This is a packaged
     *                  configuration with included configuration information, but it may
     *                  still have external dependencies that need to be downloaded
     *                  separately.  The metadata in the CAR file includes a repository URL
     *                  for these downloads, and the username and password arguments are
     *                  used in conjunction with that.
     * @param username  Optional username, if the maven repo uses HTTP Basic authentication.
     *                  Set this to null if no authentication is required.
     * @param password  Optional password, if the maven repo uses HTTP Basic authentication.
     *                  Set this to null if no authentication is required.
     *
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
        DownloadResults results = (DownloadResults) asyncKeys.get(key);
        results = results.duplicate();
        if(results.isFinished()) {
            asyncKeys.remove(key);
        }
        return results;
    }

    /**
     * Installs from a pre-downloaded CAR file
     */
    public void install(File carFile, String username, String password, DownloadPoller poller) {
        try {
            // 1. Extract the configuration metadata
            PluginMetadata data = loadCARFile(carFile, true);
            if(data == null) {
                throw new IllegalArgumentException("Invalid Configuration Archive "+carFile.getAbsolutePath()+" see server log for details");
            }

            // 2. Validate that we can install this
            validatePlugin(data);

            // 3. Install the CAR into the repository (it shouldn't be re-downloaded)
            if(data.getModuleId() != null) {
                ResultsFileWriteMonitor monitor = new ResultsFileWriteMonitor(poller);
                writeableRepo.copyToRepository(carFile, data.getModuleId(), monitor);
            }

            // 4. Use the standard logic to remove obsoletes, install dependencies, etc.
            //    This will validate all over again (oh, well)
            install(new PluginList(data.getRepositories(), new PluginMetadata[]{data}),
                    username, password, poller);
        } catch (Exception e) {
            poller.setFailure(e);
        } finally {
            poller.setFinished();
        }
    }

    /**
     * Ensures that a plugin is installable.
     */
    private void validatePlugin(PluginMetadata metadata) throws MissingDependencyException {
        // 1. Check that it's not already running
        if(metadata.getModuleId() != null) { // that is, it's a real configuration not a plugin list
            if(configManager.isRunning(metadata.getModuleId())) {
                throw new IllegalArgumentException("Configuration "+metadata.getModuleId()+" is already running!");
            }
        } else { // Different validation for plugin lists
            for (int i = 0; i < metadata.getDependencies().length; i++) {
                String dep = metadata.getDependencies()[i];
                Artifact artifact = Artifact.create(dep);
                if(!artifact.isResolved()) {
                    throw new MissingDependencyException("Configuration list "+metadata.getName()+" may not use partal artifact names for dependencies ("+dep+")");
                }
            }
        }
        // 2. Check that we meet the prerequisites
        PluginMetadata.Prerequisite[] prereqs = metadata.getPrerequisites();
        for (int i = 0; i < prereqs.length; i++) {
            PluginMetadata.Prerequisite prereq = prereqs[i];
            if(resolver.queryArtifacts(prereq.getModuleId()).length == 0) {
                throw new MissingDependencyException("Required configuration '"+prereq.getModuleId()+"' is not installed.");
            }
        }
        // 3. Check that we meet the Geronimo, JVM versions
        if(metadata.getGeronimoVersions().length > 0 && !checkGeronimoVersions(metadata.getGeronimoVersions())) {
            throw new MissingDependencyException("Cannot install plugin "+metadata.getModuleId()+" on Geronimo "+serverInfo.getVersion());
        }
        if(metadata.getJvmVersions().length > 0 && !checkJVMVersions(metadata.getJvmVersions())) {
            throw new MissingDependencyException("Cannot install plugin "+metadata.getModuleId()+" on JVM "+System.getProperty("java.version"));
        }
    }

    /**
     * Download (if necessary) and install something, which may be a Configuration or may
     * be just a JAR.  For each artifact processed, all its dependencies will be
     * processed as well.
     *
     * @param configID  Identifies the artifact to install
     * @param repos     The URLs to contact the repositories (in order of preference)
     * @param username  The username used for repositories secured with HTTP Basic authentication
     * @param password  The password used for repositories secured with HTTP Basic authentication
     * @param monitor   The ongoing results of the download operations, with some monitoring logic
     *
     * @throws IOException                 When there's a problem reading or writing data
     * @throws FailedLoginException        When a repository requires authentication and either no username
     *                                     and password are supplied or the username and password supplied
     *                                     are not accepted
     * @throws MissingDependencyException  When a dependency cannot be located in any of the listed repositories
     */
    private void downloadArtifact(Artifact configID, Map metadata, URL[] repos, String username, String password, ResultsFileWriteMonitor monitor, Set soFar) throws IOException, FailedLoginException, MissingDependencyException {
        if(soFar.contains(configID)) {
            return; // Avoid enless work due to circular dependencies
        } else {
            soFar.add(configID);
        }
        //todo: check all repositories?
        Artifact[] matches = configManager.getArtifactResolver().queryArtifacts(configID);
        if(matches.length == 0) { // not present, needs to be downloaded
            OpenResult result = openStream(configID, repos, username, password, monitor);
            try {
                File tempFile = downloadFile(result, monitor);
                PluginMetadata pluginData = ((PluginMetadata) metadata.get(configID));
                // Only bother with the hash if we got it from a source other than the download file itself
                PluginMetadata.Hash hash = pluginData == null ? null : pluginData.getHash();
                if(hash != null) {
                    String actual = ConfigurationStoreUtil.getActualChecksum(tempFile, hash.getType());
                    if(!actual.equals(hash.getValue())) {
                        throw new IOException("File download incorrect (expected "+hash.getType()+" hash "+hash.getValue()+" but got "+actual+")");
                    }
                }
                // See if the download file has plugin metadata
                if(pluginData == null) {
                    try {
                        pluginData = loadCARFile(tempFile, false);
                    } catch (Exception e) {
                        throw new IOException("Unable to read plugin metadata: "+e.getMessage());
                    }
                }
                if(pluginData != null) { // it's a plugin, not a plain JAR
                    validatePlugin(pluginData);
                }
                writeableRepo.copyToRepository(tempFile, result.getConfigID(), monitor); //todo: download SNAPSHOTS if previously available?
                if(!tempFile.delete()) {
                    log.warn("Unable to delete temporary download file "+tempFile.getAbsolutePath());
                    tempFile.deleteOnExit();
                }
                monitor.getResults().addDependencyInstalled(configID);
                configID = result.getConfigID();
            } finally {
                result.getStream().close();
            }
        } else {
            monitor.getResults().addDependencyPresent(configID);
        }

        try {
            ConfigurationData data = null;
            if(!configID.isResolved()) {
                // See if something's running
                for (int i = matches.length-1; i >= 0; i--) {
                    Artifact match = matches[i];
                    if(configStore.containsConfiguration(match) && configManager.isRunning(match)) {
                        return; // its dependencies must be OK
                    }
                }
                // Go with something that's installed
                configID = matches[matches.length-1];
            }
            if(configStore.containsConfiguration(configID)) {
                if(configManager.isRunning(configID)) {
                    return; // its dependencies must be OK
                }
                data = configStore.loadConfiguration(configID);
            }
            Dependency[] dependencies = data == null ? getDependencies(writeableRepo, configID) : getDependencies(data);
            // Download the dependencies
            for (int i = 0; i < dependencies.length; i++) {
                Dependency dep = dependencies[i];
                Artifact artifact = dep.getArtifact();
                downloadArtifact(artifact, metadata, repos, username, password, monitor, soFar);
            }
        } catch (NoSuchConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore does not see it: "+e.getMessage());
        } catch (InvalidConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore cannot load it: "+e.getMessage());
        }
    }

    /**
     * Downloads to a temporary file so we can validate the download before
     * installing into the repository.
     */
    private File downloadFile(OpenResult result, ResultsFileWriteMonitor monitor) throws IOException {
        InputStream in = result.getStream();
        if(in == null) {
            throw new IllegalStateException();
        }
        monitor.writeStarted(result.getConfigID().toString(), result.fileSize);
        File file = File.createTempFile("geronimo-plugin-download-", ".tmp");
        FileOutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[4096];
        int count, total = 0;
        while((count = in.read(buf)) > -1) {
            out.write(buf, 0, count);
            monitor.writeProgress(total += count);
        }
        monitor.writeComplete(total);
        in.close();
        out.close();
        return file;
    }

    /**
     * Used to get dependencies for a JAR
     */
    private static Dependency[] getDependencies(Repository repo, Artifact artifact) {
        Set set = repo.getDependencies(artifact);
        Dependency[] results = new Dependency[set.size()];
        int index=0;
        for (Iterator it = set.iterator(); it.hasNext(); ++index) {
            Artifact dep = (Artifact) it.next();
            results[index] = new Dependency(dep, ImportType.CLASSES);
        }
        return results;
    }

    /**
     * Used to get dependencies for a Configuration
     */
    private static Dependency[] getDependencies(ConfigurationData data) {
        List dependencies = new ArrayList(data.getEnvironment().getDependencies());
        Collection children = data.getChildConfigurations().values();
        for (Iterator it = children.iterator(); it.hasNext();) {
            ConfigurationData child = (ConfigurationData) it.next();
            dependencies.addAll(child.getEnvironment().getDependencies());
        }
        return (Dependency[]) dependencies.toArray(new Dependency[dependencies.size()]);
    }

    /**
     * Constructs a URL to a particular artifact in a particular repository
     */
    private static URL getURL(Artifact configId, URL repository) throws MalformedURLException {
        return new URL(repository, configId.getGroupId().replace('.','/')+"/"+configId.getArtifactId()+"/"+configId.getVersion()+"/"+configId.getArtifactId()+"-"+configId.getVersion()+"."+configId.getType());
    }

    /**
     * Attemps to open a stream to an artifact in one of the listed repositories.
     * The username and password provided are only used if one of the repositories
     * returns an HTTP authentication failure on the first try.
     *
     * @param artifact  The artifact we're looking for, or null to just connect to the base repo URL
     * @param repos     The base URLs to the repositories to search for the artifact
     * @param username  A username if one of the repositories might require authentication
     * @param password  A password if one of the repositories might require authentication
     * @param monitor   Callback for progress on the connection operation
     *
     * @throws IOException Occurs when the IO with the repository failed
     * @throws FailedLoginException Occurs when a repository requires authentication and either
     *                              no username and password were provided or they weren't
     *                              accepted
     * @throws MissingDependencyException Occurs when none of the repositories has the artifact
     *                                    in question
     */
    private static OpenResult openStream(Artifact artifact, URL[] repos, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException, MissingDependencyException {
        if(monitor != null) {
            monitor.getResults().setCurrentFilePercent(-1);
            monitor.getResults().setCurrentMessage("Attempting to download "+artifact);
            monitor.setTotalBytes(-1); // In case the server doesn't say
        }
        if(artifact != null && !artifact.isResolved()) {
            artifact = findArtifact(artifact, repos, username, password, monitor);
        }
        InputStream in;
        LinkedList list = new LinkedList();
        list.addAll(Arrays.asList(repos));
        while (true) {
            if(list.isEmpty()) {
                throw new MissingDependencyException("Unable to download dependency "+artifact);
            }
            if(monitor != null) {
                monitor.setTotalBytes(-1); // Just to be sure
            }
            URL repository = (URL) list.removeFirst();
            URL url = artifact == null ? repository : getURL(artifact, repository);
            log.debug("Attempting to download "+artifact+" from "+url);
            in = connect(url, username, password, monitor);
            if(in != null) {
                return new OpenResult(artifact, in, monitor == null ? -1 : monitor.getTotalBytes());
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
        if(con instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if(method != null) {
                http.setRequestMethod(method);
            }
            http.connect();
            if(http.getResponseCode() == 401) { // need to authenticate
                if(username == null || username.equals("")) {
                    throw new FailedLoginException("Server returned 401 "+http.getResponseMessage());
                }
                http = (HttpURLConnection) url.openConnection();
                http.setRequestProperty("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                if(method != null) {
                    http.setRequestMethod(method);
                }
                http.connect();
                if(http.getResponseCode() == 401) {
                    throw new FailedLoginException("Server returned 401 "+http.getResponseMessage());
                } else if(http.getResponseCode() == 404) {
                    return null; // Not found at this repository
                }
                if(monitor != null && http.getContentLength() > 0) {
                    monitor.setTotalBytes(http.getContentLength());
                }
                return http.getInputStream();
            } else if(http.getResponseCode() == 404) {
                return null; // Not found at this repository
            } else {
                if(monitor != null && http.getContentLength() > 0) {
                    monitor.setTotalBytes(http.getContentLength());
                }
                return http.getInputStream();
            }
        } else {
            if(username != null && !username.equals("")) {
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                try {
                    con.connect();
                    if(monitor != null && con.getContentLength() > 0) {
                        monitor.setTotalBytes(con.getContentLength());
                    }
                    return con.getInputStream();
                } catch (FileNotFoundException e) {
                    return null;
                }
            } else {
                try {
                    con.connect();
                    if(monitor != null && con.getContentLength() > 0) {
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
    private static Artifact findArtifact(Artifact query, URL[] repos, String username, String password, ResultsFileWriteMonitor monitor) throws MissingDependencyException {
        if(query.getGroupId() == null || query.getArtifactId() == null || query.getType() == null) {
            throw new MissingDependencyException("No support yet for dependencies missing more than a version: "+query);
        }
        List list = new ArrayList();
        for (int i = 0; i < repos.length; i++) {
            list.add(repos[i]);
        }
        Artifact result = null;
        for (int i = 0; i < list.size(); i++) {
            URL url = (URL) list.get(i);
            try {
                result = findArtifact(query, url, username, password, monitor);
            } catch (Exception e) {
                log.warn("Unable to read from "+url, e);
            }
            if(result != null) {
                return result;
            }
        }
        throw new MissingDependencyException("No repository has a valid artifact for "+query);
    }

    /**
     * Checks for an artifact in a specific repository, where the artifact may
     * have wildcards in the ID.
     */
    private static Artifact findArtifact(Artifact query, URL url, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException, ParserConfigurationException, SAXException {
        monitor.getResults().setCurrentMessage("Searching for "+query+" at "+url);
        String base = query.getGroupId().replace('.', '/') + "/" + query.getArtifactId();
        String path = base +"/maven-metadata.xml";
        URL metaURL = new URL(url.toString()+path);
        InputStream in = connect(metaURL, username, password, monitor);
        if(in == null) {
            return null;
        }
        // Don't use the validating parser that we normally do
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(in);
        Element root = doc.getDocumentElement();
        NodeList list = root.getElementsByTagName("versions");
        if(list.getLength() == 0) {
            return null;
        }
        list = ((Element)list.item(0)).getElementsByTagName("version");
        Version[] available = new Version[list.getLength()];
        for (int i = 0; i < available.length; i++) {
            available[i] = new Version(getText(list.item(i)));
        }
        Arrays.sort(available);
        for(int i=available.length-1; i>=0; i--) {
            Version version = available[i];
            URL test = new URL(url.toString()+base+"/"+version+"/"+query.getArtifactId()+"-"+version+"."+query.getType());
            InputStream testStream = connect(test, username, password, monitor, "HEAD");
            if(testStream == null) {
                log.warn("Maven repository "+url+" listed artifact "+query+" version "+version+" but I couldn't find it at "+test);
                continue;
            }
            testStream.close();
            return new Artifact(query.getGroupId(), query.getArtifactId(), version, query.getType());
        }
        return null;
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
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            PluginNameIDHandler handler = new PluginNameIDHandler();
            parser.parse(xml, handler);
            if(handler.isComplete()) {
                plugins.put(handler.getName(), Artifact.create(handler.getID()));
            }
        } catch (Exception e) {
            log.warn("Invalid XML at "+xml.getAbsolutePath(), e);
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
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            PluginNameIDHandler handler = new PluginNameIDHandler();
            parser.parse(xml, handler);
            if(handler.isComplete()) {
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
    private void overrideDependencies(ConfigurationData data, PluginMetadata metadata) {
        //todo: this ends up doing a little more work than necessary
        PluginMetadata temp = createDefaultMetadata(data);
        metadata.setDependencies(temp.getDependencies());
    }

    /**
     * Generates a default plugin metadata based on the data for this module
     * in the server.
     */
    private PluginMetadata createDefaultMetadata(ConfigurationData data) {
        PluginMetadata meta = new PluginMetadata(data.getId().toString(), // name
                data.getId(), // module ID
                "Unknown", // category
                "Please provide a description",
                null, // URL
                null, // author
                null, // hash
                true, // installed
                false);
        meta.setGeronimoVersions(new String[]{serverInfo.getVersion()});
        meta.setJvmVersions(new String[0]);
        meta.setLicenses(new PluginMetadata.License[0]);
        meta.setObsoletes(new String[0]);
        List deps = new ArrayList();
        PluginMetadata.Prerequisite prereq = null;
        prereq = processDependencyList(data.getEnvironment().getDependencies(), prereq, deps);
        Map children = data.getChildConfigurations();
        for (Iterator it = children.values().iterator(); it.hasNext();) {
            ConfigurationData child = (ConfigurationData) it.next();
            prereq = processDependencyList(child.getEnvironment().getDependencies(), prereq, deps);
        }
        meta.setDependencies((String[]) deps.toArray(new String[deps.size()]));
        meta.setPrerequisites(prereq == null ? new PluginMetadata.Prerequisite[0] : new PluginMetadata.Prerequisite[]{prereq});
        return meta;
    }

    /**
     * Read the plugin metadata out of a plugin CAR file on disk.
     */
    private PluginMetadata loadCARFile(File file, boolean definitelyCAR) throws IOException, ParserConfigurationException, SAXException {
        if(!file.canRead()) {
            log.error("Cannot read from downloaded CAR file "+file.getAbsolutePath());
            return null;
        }
        JarFile jar = new JarFile(file);
        Document doc;
        try {
            JarEntry entry = jar.getJarEntry("META-INF/geronimo-plugin.xml");
            if(entry == null) {
                if(definitelyCAR) {
                    log.error("Downloaded CAR file does not contain META-INF/geronimo-plugin.xml file");
                }
                jar.close();
                return null;
            }
            InputStream in = jar.getInputStream(entry);
            DocumentBuilder builder = createDocumentBuilder();
            doc = builder.parse(in);
            in.close();
        } finally {
            jar.close();
        }
        return loadPluginMetadata(doc, file.getAbsolutePath());
    }

    /**
     * Read a set of plugin metadata from a DOM document.
     */
    private PluginMetadata loadPluginMetadata(Document doc, String file) throws SAXException, MalformedURLException {
        Element root = doc.getDocumentElement();
        if(!root.getNodeName().equals("geronimo-plugin")) {
            log.error("Configuration archive "+file+" does not have a geronimo-plugin in META-INF/geronimo-plugin.xml");
            return null;
        }
        return processPlugin(root);
    }

    /**
     * Loads the list of all available plugins from the specified stream
     * (representing geronimo-plugins.xml at the specified repository).
     */
    private PluginList loadPluginList(URL repo, InputStream in) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = createDocumentBuilder();
        Document doc = builder.parse(in);
        in.close();
        Element root = doc.getDocumentElement(); // geronimo-plugin-list
        NodeList configs = root.getElementsByTagName("plugin");
        List results = new ArrayList();
        for (int i = 0; i < configs.getLength(); i++) {
            Element config = (Element) configs.item(i);
            PluginMetadata data = processPlugin(config);
            results.add(data);
        }
        String[] repos = getChildrenText(root, "default-repository");
        URL[] repoURLs = new URL[repos.length];
        for(int i = 0; i < repos.length; i++) {
            if(repos[i].endsWith("/")) {
                repoURLs[i] = new URL(repos[i]);
            } else {
                repoURLs[i] = new URL(repos[i]+"/");
            }
        }

        PluginMetadata[] data = (PluginMetadata[]) results.toArray(new PluginMetadata[results.size()]);
        return new PluginList(repoURLs, data);
    }

    /**
     * Common logic for setting up a document builder to deal with plugin files.
     * @return
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                             "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                             PluginInstallerGBean.class.getResourceAsStream("/META-INF/schema/plugins-1.1.xsd"));
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                throw new SAXException("Unable to read plugin file", exception);
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw new SAXException("Unable to read plugin file", exception);
            }

            public void warning(SAXParseException exception) {
                log.warn("Warning reading XML document", exception);
            }
        });
        return builder;
    }

    /**
     * Given a DOM element representing a plugin, load it into a PluginMetadata
     * object.
     */
    private PluginMetadata processPlugin(Element plugin) throws SAXException, MalformedURLException {
        String moduleId = getChildText(plugin, "module-id");
        NodeList licenseNodes = plugin.getElementsByTagName("license");
        PluginMetadata.License[] licenses = new PluginMetadata.License[licenseNodes.getLength()];
        for(int j=0; j<licenseNodes.getLength(); j++) {
            Element node = (Element) licenseNodes.item(j);
            String licenseName = getText(node);
            String openSource = node.getAttribute("osi-approved");
            if(licenseName == null || licenseName.equals("") || openSource == null || openSource.equals("")) {
                throw new SAXException("Invalid config file: license name and osi-approved flag required");
            }
            licenses[j] = new PluginMetadata.License(licenseName, Boolean.valueOf(openSource).booleanValue());
        }
        PluginMetadata.Hash hash = null;
        NodeList hashList = plugin.getElementsByTagName("hash");
        if(hashList.getLength() > 0) {
            Element elem = (Element) hashList.item(0);
            hash = new PluginMetadata.Hash(elem.getAttribute("type"), getText(elem));
        }
        boolean eligible = true;
        NodeList preNodes = plugin.getElementsByTagName("prerequisite");
        PluginMetadata.Prerequisite[] prereqs = new PluginMetadata.Prerequisite[preNodes.getLength()];
        for(int j=0; j<preNodes.getLength(); j++) {
            Element node = (Element) preNodes.item(j);
            String originalConfigId = getChildText(node, "id");
            if(originalConfigId == null) {
                throw new SAXException("Prerequisite requires <id>");
            }
            Artifact artifact = Artifact.create(originalConfigId.replaceAll("\\*", ""));
            boolean present = resolver.queryArtifacts(artifact).length > 0;
            prereqs[j] = new PluginMetadata.Prerequisite(artifact, present,
                    getChildText(node, "resource-type"), getChildText(node, "description"));
            if(!present) {
                log.debug(moduleId+" is not eligible due to missing "+prereqs[j].getModuleId());
                eligible = false;
            }
        }
        String[] gerVersions = getChildrenText(plugin, "geronimo-version");
        if(gerVersions.length > 0) {
            boolean match = checkGeronimoVersions(gerVersions);
            if(!match) eligible = false;
        }
        String[] jvmVersions = getChildrenText(plugin, "jvm-version");
        if(jvmVersions.length > 0) {
            boolean match = checkJVMVersions(jvmVersions);
            if(!match) eligible = false;
        }
        String[] repoNames = getChildrenText(plugin, "source-repository");
        URL[] repos = new URL[repoNames.length];
        for (int i = 0; i < repos.length; i++) {
            repos[i] = new URL(repoNames[i]);
        }
        Artifact artifact = null;
        boolean installed = false;
        if (moduleId != null) {
            artifact = Artifact.create(moduleId);
            // Tests, etc. don't need to have a ConfigurationManager
            installed = configManager != null && configManager.isLoaded(artifact);
        }
        log.trace("Checking "+moduleId+": installed="+installed+", eligible="+eligible);
        PluginMetadata data = new PluginMetadata(getChildText(plugin, "name"),
                artifact,
                getChildText(plugin, "category"),
                getChildText(plugin, "description"),
                getChildText(plugin, "url"),
                getChildText(plugin, "author"),
                hash,
                installed, eligible);
        data.setGeronimoVersions(gerVersions);
        data.setJvmVersions(jvmVersions);
        data.setLicenses(licenses);
        data.setPrerequisites(prereqs);
        data.setRepositories(repos);
        NodeList list = plugin.getElementsByTagName("dependency");
        List start = new ArrayList();
        String deps[] = new String[list.getLength()];
        for(int i=0; i<list.getLength(); i++) {
            Element node = (Element) list.item(i);
            deps[i] = getText(node);
            if(node.hasAttribute("start") && node.getAttribute("start").equalsIgnoreCase("true")) {
                start.add(deps[i]);
            }
        }
        data.setDependencies(deps);
        data.setForceStart((String[]) start.toArray(new String[start.size()]));
        data.setObsoletes(getChildrenText(plugin, "obsoletes"));
        return data;
    }

    /**
     * Check whether the specified JVM versions match the current runtime
     * environment.
     *
     * @return true if the specified versions match the current
     *              execution environment as defined by plugins-1.1.xsd
     */
    private boolean checkJVMVersions(String[] jvmVersions) {
        if(jvmVersions.length == 0) return true;
        String version = System.getProperty("java.version");
        boolean match = false;
        for (int j = 0; j < jvmVersions.length; j++) {
            String jvmVersion = jvmVersions[j];
            if(jvmVersion == null || jvmVersion.equals("")) {
                throw new IllegalStateException("jvm-version should not be empty!");
            }
            if(version.startsWith(jvmVersion)) {
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
     *              execution environment as defined by plugins-1.1.xsd
     */
    private boolean checkGeronimoVersions(String[] gerVersions) {
        if(gerVersions.length == 0) return true;
        String version = serverInfo.getVersion();
        boolean match = false;
        for (int j = 0; j < gerVersions.length; j++) {
            String gerVersion = gerVersions[j];
            if(gerVersion == null || gerVersion.equals("")) {
                throw new IllegalStateException("geronimo-version should not be empty!");
            }
            if(gerVersion.equals(version)) {
                match = true;
                break;
            }
        }
        return match;
    }

    /**
     * Gets the text out of a child of the specified DOM element.
     *
     * @param root      The parent DOM element
     * @param property  The name of the child element that holds the text
     */
    private static String getChildText(Element root, String property) {
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            Node check = children.item(i);
            if(check.getNodeType() == Node.ELEMENT_NODE && check.getNodeName().equals(property)) {
                return getText(check);
            }
        }
        return null;
    }

    /**
     * Gets all the text contents of the specified DOM node.
     */
    private static String getText(Node target) {
        NodeList nodes = target.getChildNodes();
        StringBuffer buf = null;
        for(int j=0; j<nodes.getLength(); j++) {
            Node node = nodes.item(j);
            if(node.getNodeType() == Node.TEXT_NODE) {
                if(buf == null) {
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
     * @param root      The parent DOM element
     * @param property  The name of the child elements that hold the text
     */
    private static String[] getChildrenText(Element root, String property) {
        NodeList children = root.getChildNodes();
        List results = new ArrayList();
        for(int i=0; i<children.getLength(); i++) {
            Node check = children.item(i);
            if(check.getNodeType() == Node.ELEMENT_NODE && check.getNodeName().equals(property)) {
                NodeList nodes = check.getChildNodes();
                StringBuffer buf = null;
                for(int j=0; j<nodes.getLength(); j++) {
                    Node node = nodes.item(j);
                    if(node.getNodeType() == Node.TEXT_NODE) {
                        if(buf == null) {
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
     *
     * @return The resulting prerequisite, if any.
     */
    private PluginMetadata.Prerequisite processDependencyList(List real, PluginMetadata.Prerequisite prereq, List deps) {
        for (int i = 0; i < real.size(); i++) {
            Dependency dep = (Dependency) real.get(i);
            if(dep.getArtifact().getGroupId().equals("geronimo")) {
                if(dep.getArtifact().getArtifactId().indexOf("jetty") > -1) {
                    if(prereq == null) {
                        prereq = new PluginMetadata.Prerequisite(dep.getArtifact(), true, "Web Container", "This plugin works with the Geronimo/Jetty distribution.  It is not intended to run in the Geronimo/Tomcat distribution.  There is a separate version of this plugin that works with Tomcat.");
                    }
                    continue;
                } else if(dep.getArtifact().getArtifactId().indexOf("tomcat") > -1) {
                    if(prereq == null) {
                        prereq = new PluginMetadata.Prerequisite(dep.getArtifact(), true, "Web Container", "This plugin works with the Geronimo/Tomcat distribution.  It is not intended to run in the Geronimo/Jetty distribution.  There is a separate version of this plugin that works with Jetty.");
                    }
                    continue;
                }
            }
            if(!deps.contains(dep.getArtifact().toString())) {
                deps.add(dep.getArtifact().toString());
            }
        }
        return prereq;
    }

    /**
     * Writes plugin metadata to a DOM tree.
     */
    private static Document writePluginMetadata(PluginMetadata data) throws ParserConfigurationException {
        DocumentBuilder builder = createDocumentBuilder();
        Document doc = builder.newDocument();
        Element config = doc.createElementNS("http://geronimo.apache.org/xml/ns/plugins-1.1", "geronimo-plugin");
        config.setAttribute("xmlns", "http://geronimo.apache.org/xml/ns/plugins-1.1");
        doc.appendChild(config);

        addTextChild(doc, config, "name", data.getName());
        addTextChild(doc, config, "config-id", data.getModuleId().toString());
        addTextChild(doc, config, "category", data.getCategory());
        addTextChild(doc, config, "description", data.getDescription());
        if(data.getPluginURL() != null) {
            addTextChild(doc, config, "url", data.getPluginURL());
        }
        if(data.getAuthor() != null) {
            addTextChild(doc, config, "author", data.getAuthor());
        }
        for (int i = 0; i < data.getLicenses().length; i++) {
            PluginMetadata.License license = data.getLicenses()[i];
            Element lic = doc.createElement("license");
            lic.appendChild(doc.createTextNode(license.getName()));
            lic.setAttribute("osi-approved", Boolean.toString(license.isOsiApproved()));
            config.appendChild(lic);
        }
        if(data.getHash() != null) {
            Element hash = doc.createElement("hash");
            hash.setAttribute("type", data.getHash().getType());
            hash.appendChild(doc.createTextNode(data.getHash().getValue()));
            config.appendChild(hash);
        }
        for (int i = 0; i < data.getGeronimoVersions().length; i++) {
            addTextChild(doc, config, "geronimo-version", data.getGeronimoVersions()[i]);
        }
        for (int i = 0; i < data.getJvmVersions().length; i++) {
            addTextChild(doc, config, "jvm-version", data.getJvmVersions()[i]);
        }
        for (int i = 0; i < data.getPrerequisites().length; i++) {
            PluginMetadata.Prerequisite prereq = data.getPrerequisites()[i];
            Element pre = doc.createElement("prerequisite");
            addTextChild(doc, pre, "id", prereq.getModuleId().toString());
            if(prereq.getResourceType() != null) {
                addTextChild(doc, pre, "resource-type", prereq.getResourceType());
            }
            if(prereq.getDescription() != null) {
                addTextChild(doc, pre, "description", prereq.getDescription());
            }
            config.appendChild(pre);
        }
        for (int i = 0; i < data.getDependencies().length; i++) {
            addTextChild(doc, config, "dependency", data.getDependencies()[i]);
        }
        for (int i = 0; i < data.getObsoletes().length; i++) {
            addTextChild(doc, config, "obsoletes", data.getObsoletes()[i]);
        }
        for (int i = 0; i < data.getRepositories().length; i++) {
            URL url = data.getRepositories()[i];
            addTextChild(doc, config, "source-repository", url.toString());
        }
        return doc;
    }

    /**
     * Adds a child of the specified Element that just has the specified text content
     * @param doc     The document
     * @param parent  The parent element
     * @param name    The name of the child element to add
     * @param text    The contents of the child element to add
     */
    private static void addTextChild(Document doc, Element parent, String name, String text) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(text));
        parent.appendChild(child);
    }

    /**
     * Gets a token unique to this run of the server, used to track asynchronous
     * downloads.
     */
    private static Object getNextKey() {
        int value;
        synchronized(PluginInstallerGBean.class) {
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
            if(element != null) {
                if(element.equals("module-id")) {
                    id += new String(ch, start, length);
                } else if(element.equals("name")) {
                    name += new String(ch, start, length);
                }
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            element = null;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("module-id") || qName.equals("name")) {
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
            results.setCurrentMessage("Downloading "+fileDescription+"...");
            results.setCurrentFilePercent(totalBytes > 0 ? 0 : -1);
        }

        public void writeProgress(int bytes) {
            if(totalBytes > 0) {
                results.setCurrentFilePercent((bytes*100)/totalBytes);
            } else {
                results.setCurrentMessage((bytes/1024)+" kB of "+file);
            }
        }

        public void writeComplete(int bytes) {
            results.setCurrentFilePercent(100);
            results.setCurrentMessage("Downloaded "+file+" ("+(bytes/1024)+" kB)");
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
        infoFactory.addInterface(PluginInstaller.class);

        infoFactory.setConstructor(new String[]{"ConfigManager", "Repository", "ConfigStore", "ServerInfo","ThreadPool"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
