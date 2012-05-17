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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.FailedLoginException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.system.configuration.ConfigurationStoreUtil;
import org.apache.geronimo.system.configuration.PluginAttributeStore;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.apache.geronimo.system.plugin.model.ConfigXmlContentType;
import org.apache.geronimo.system.plugin.model.CopyFileType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.HashType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.ModuleType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.PrerequisiteType;
import org.apache.geronimo.system.plugin.model.PropertyType;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;
import org.apache.tools.ant.util.FileUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A GBean that knows how to download configurations from a Maven repository.
 *
 * @version $Rev$ $Date$
 */
@GBean
public class PluginInstallerGBean implements PluginInstaller {
    private static final Logger log = LoggerFactory.getLogger(PluginInstallerGBean.class);

    private static int counter;
    private final String installedPluginsList;
    //all plugins that have ever been installed on this server.
    private final Set<Artifact> installedArtifacts = new HashSet<Artifact>();
    private final ConfigurationManager configManager;
    private final GeronimoSourceRepository localSourceRepository;
    private final WritableListableRepository writeableRepo;
    private final ConfigurationStore configStore;
    private final ServerInfo serverInfo;
    private final Map<Object, DownloadResults> asyncKeys;
    private final ThreadPool threadPool;
    private final Collection<? extends ServerInstanceData> serverInstanceDatas;
    private final BundleContext bundleContext;
    private final Map<String, ServerInstance> servers = new HashMap<String, ServerInstance>();
    private final Collection<PersistentConfigurationList> persistentConfigurationLists;
    private final PluginRepositoryList pluginRepositoryList;

    // This regular expression for repository filename is taken from Maven1Repository.MAVEN_1_PATTERN
    private static final Pattern MAVEN_1_PATTERN_PART = Pattern.compile("(.+)-([0-9].+)\\.([^0-9]+)");

    /**
     * GBean constructor.  Supply an existing ConfigurationManager.  Use for adding to the current server.
     *
     * @param configManager                Configuration Manager for this server
     * @param repository                   repository to install into
     * @param configStore                  configuration store to install into
     * @param serverInstanceDatas          set of server "layouts" to install config info into
     * @param serverInfo                   location of server
     * @param threadPool                   thread pool for async operations
     * @param artifactManager              artifact manager to resolve existing artifacts
     * @param persistentConfigurationLists used to start new plugins in a running server
     * @param bundleContext                classLoader @throws IOException exception if server instance cannot be loaded
     * @throws java.io.IOException from bad ServerInstance
     */
    public PluginInstallerGBean(@ParamAttribute(name = "installedPluginsList") String installedPluginsList,
                                @ParamReference(name = "ConfigManager", namingType = "ConfigurationManager") ConfigurationManager configManager,
                                @ParamReference(name = "Repository", namingType = "Repository") WritableListableRepository repository,
                                @ParamReference(name = "ConfigStore", namingType = "ConfigurationStore") ConfigurationStore configStore,
                                @ParamReference(name = "ServerInstances", namingType = "ServerInstanceData") Collection<ServerInstanceData> serverInstanceDatas,
                                @ParamReference(name = "ServerInfo") final ServerInfo serverInfo,
                                @ParamReference(name = "ThreadPool") ThreadPool threadPool,
                                @ParamReference(name = "ArtifactManager") final ArtifactManager artifactManager,
                                @ParamReference(name = "PersistentConfigurationLists", namingType = "AttributeStore") Collection<PersistentConfigurationList> persistentConfigurationLists,
                                @ParamReference(name = "PluginRepositoryList") PluginRepositoryList pluginRepositoryList,
                                @ParamSpecial(type = SpecialAttributeType.bundleContext) final BundleContext bundleContext) throws IOException {
        this(installedPluginsList, configManager, repository, configStore, serverInstanceDatas, serverInfo, threadPool, artifactManager, persistentConfigurationLists, pluginRepositoryList, bundleContext, true);
    }

    private PluginInstallerGBean(String installedPluginsList,
                                 ConfigurationManager configManager,
                                 WritableListableRepository repository,
                                 ConfigurationStore configStore,
                                 Collection<? extends ServerInstanceData> serverInstanceDatas,
                                 ServerInfo serverInfo,
                                 ThreadPool threadPool,
                                 ArtifactManager artifactManager,
                                 Collection<PersistentConfigurationList> persistentConfigurationLists,
                                 PluginRepositoryList pluginRepositoryList,
                                 BundleContext bundleContext,
                                 boolean live) throws IOException {
        this.writeableRepo = repository;
        this.configStore = configStore;
        this.serverInfo = serverInfo;
        this.threadPool = threadPool;
        asyncKeys = new ConcurrentHashMap<Object, DownloadResults>();
        this.serverInstanceDatas = serverInstanceDatas;
        if (artifactManager == null) artifactManager = new DefaultArtifactManager();
        setUpServerInstances(serverInstanceDatas, serverInfo, artifactManager, servers, writeableRepo, live);
        this.persistentConfigurationLists = persistentConfigurationLists == null ? Collections.<PersistentConfigurationList>emptyList() : persistentConfigurationLists;
        this.bundleContext = bundleContext;
        if (configManager == null) {
            throw new IllegalArgumentException("No default server instance set up");
        }
        this.configManager = configManager;
        localSourceRepository = new GeronimoSourceRepository(configManager.getRepositories(), configManager.getArtifactResolver());
        this.pluginRepositoryList = pluginRepositoryList;
        this.installedPluginsList = installedPluginsList;
        loadHistory();
    }

    /**
     * Constructor for use in assembling a new server.
     *
     * @param targetRepositoryPath location of repo to install into (not in current server)
     * @param targetServerPath     location of server to install into (not current server
     * @param installedPluginsList location of file to track installations
     * @param serverInstanceDatas  set of server layouts
     * @param pluginRepositoryList
     * @param kernel               kernel for current server
     * @param bundleContext        classLoader
     * @throws IOException if layouts can't be loaded
     */
    public PluginInstallerGBean(String targetRepositoryPath,
                                String targetServerPath,
                                String installedPluginsList,
                                Collection<? extends ServerInstanceData> serverInstanceDatas,
                                PluginRepositoryList pluginRepositoryList,
                                final Kernel kernel,
                                final BundleContext bundleContext) throws Exception {
        final ArtifactManager artifactManager = new DefaultArtifactManager();

        forceMkdir(new File(targetServerPath));
        serverInfo = new BasicServerInfo(targetServerPath, false);
        File targetRepositoryFile = serverInfo.resolve(targetRepositoryPath);
        forceMkdir(targetRepositoryFile);
        writeableRepo = new Maven2Repository(targetRepositoryFile);
        configStore = new RepositoryConfigurationStore(writeableRepo);
        threadPool = null;
        asyncKeys = new ConcurrentHashMap<Object, DownloadResults>();
        this.serverInstanceDatas = serverInstanceDatas;
        this.persistentConfigurationLists = Collections.emptyList();
        this.bundleContext = bundleContext;
        setUpServerInstances(serverInstanceDatas, serverInfo, artifactManager, servers, writeableRepo, false);
        this.configManager = buildConfigurationManager(artifactManager, writeableRepo, kernel, configStore, bundleContext, servers);
        localSourceRepository = new GeronimoSourceRepository(configManager.getRepositories(), configManager.getArtifactResolver());
        this.pluginRepositoryList = pluginRepositoryList;
        this.installedPluginsList = installedPluginsList;
        loadHistory();
    }

    public PluginInstallerGBean pluginInstallerCopy(String serverName, Kernel kernel) throws Exception {
        ServerInfo newServerInfo = new BasicServerInfo(serverInfo.getCurrentBaseDirectory(), serverName);
        final ArtifactManager artifactManager = new DefaultArtifactManager();
        ConfigurationManager configManager = buildConfigurationManager(artifactManager, writeableRepo, kernel, configStore, bundleContext, servers);
//        ArrayList<ServerInstanceData> serverInstanceDatasCopy = new ArrayList<ServerInstanceData>(serverInstanceDatas.size());
//        for (ServerInstanceData serverInstance: serverInstanceDatas) {
//            if (serverInstance instanceof ReferenceServerInstanceData) {
//                serverInstance = new ServerInstanceData(serverInstance);
//            }
//            serverInstanceDatasCopy.add(serverInstance);
//        }
        return new PluginInstallerGBean(
                installedPluginsList,
                configManager,
                writeableRepo,
                configStore,
                serverInstanceDatas,
                newServerInfo,
                threadPool,
                artifactManager,
                persistentConfigurationLists,
                pluginRepositoryList,
                bundleContext,
                false);
    }

    private static void setUpServerInstances(Collection<? extends ServerInstanceData> serverInstanceDatas,
                                             final ServerInfo serverInfo, final ArtifactManager artifactManager,
                                             final Map<String, ServerInstance> servers,
                                             final WritableListableRepository writeableRepo,
                                             final boolean live) throws IOException {
        List<ServerInstanceData> datas = new ArrayList<ServerInstanceData>(serverInstanceDatas);
        boolean shrank = true;
        while (shrank) {
            shrank = false;
            for (Iterator<ServerInstanceData> it = datas.iterator(); it.hasNext();) {
                ServerInstanceData instance = it.next();
                String dependsOn = instance.getAttributeManagerFrom();
                if (dependsOn == null || servers.containsKey(dependsOn)) {
                    addServerInstance(instance, artifactManager, writeableRepo, serverInfo, servers, live);
                    it.remove();
                    shrank = true;
                }
            }
        }
        if (!datas.isEmpty()) {
            throw new IllegalStateException("Cannot resolve ServerInstanceDatas: " + datas);
        }
        if (serverInstanceDatas instanceof ReferenceCollection) {
            ((ReferenceCollection) serverInstanceDatas).addReferenceCollectionListener(new ReferenceCollectionListener() {

                public void memberAdded(ReferenceCollectionEvent event) {
                    ServerInstanceData instance = (ServerInstanceData) event.getMember();
                    try {
                        addServerInstance(instance, artifactManager, writeableRepo, serverInfo, servers, live);
                    } catch (IOException e) {
                        //nothing to do?? log???
                    }
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    ServerInstanceData instance = (ServerInstanceData) event.getMember();
                    servers.remove(instance.getName());
                }
            });
        }
    }

    private static void addServerInstance(ServerInstanceData serverInstance,
                                          ArtifactManager artifactManager,
                                          WritableListableRepository targetRepo,
                                          ServerInfo serverInfo,
                                          Map<String, org.apache.geronimo.system.plugin.ServerInstance> servers,
                                          boolean live) throws IOException {
        File targetConfigDirectory = serverInfo.resolveServer(serverInstance.getConfigFile()).getParentFile();
        forceMkdir(targetConfigDirectory);
        org.apache.geronimo.system.plugin.ServerInstance instance = serverInstance.getServerInstance(artifactManager, targetRepo, serverInfo, servers, live);
        servers.put(instance.getServerName(), instance);
    }

    private static ConfigurationManager buildConfigurationManager(ArtifactManager artifactManager,
                                                                  WritableListableRepository targetRepo,
                                                                  Kernel kernel,
                                                                  ConfigurationStore targetStore,
                                                                  BundleContext bundleContext,
                                                                  Map<String, org.apache.geronimo.system.plugin.ServerInstance> servers) throws IOException {
        for (ServerInstance instance : servers.values()) {
            if ("default".equals(instance.getServerName())) {
                ConfigurationManager configurationManager = new SimpleConfigurationManager(Collections.singleton(targetStore),
                        instance.getArtifactResolver(),
                        Collections.<ListableRepository>singleton(targetRepo),
                        null);
//                configurationManager.setOnline(false);
                return configurationManager;
            }
        }
        throw new IllegalStateException("No default server instance found: " + servers.keySet());
    }

    /* now for tests only */
    PluginInstallerGBean(ConfigurationManager configManager,
                         WritableListableRepository repository,
                         ConfigurationStore configStore,
                         String installedPluginsList, ServerInfo serverInfo,
                         ThreadPool threadPool,
                         Collection<ServerInstance> servers, PluginRepositoryList pluginRepositoryList) {
        this.configManager = configManager;
        localSourceRepository = new GeronimoSourceRepository(configManager.getRepositories(), configManager.getArtifactResolver());
        this.writeableRepo = repository;
        this.configStore = configStore;
        this.serverInfo = serverInfo;
        this.threadPool = threadPool;
        asyncKeys = new ConcurrentHashMap<Object, DownloadResults>();
        serverInstanceDatas = null;
        this.persistentConfigurationLists = Collections.emptyList();
        bundleContext = null;
        for (ServerInstance instance : servers) {
            this.servers.put(instance.getServerName(), instance);
        }
        if (servers instanceof ReferenceCollection) {
            ((ReferenceCollection) servers).addReferenceCollectionListener(new ReferenceCollectionListener() {

                public void memberAdded(ReferenceCollectionEvent event) {
                    ServerInstance instance = (ServerInstance) event.getMember();
                    PluginInstallerGBean.this.servers.put(instance.getServerName(), instance);
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    ServerInstance instance = (ServerInstance) event.getMember();
                    PluginInstallerGBean.this.servers.remove(instance.getServerName());
                }
            });
        }
        this.pluginRepositoryList = pluginRepositoryList;
        this.installedPluginsList = installedPluginsList;
        loadHistory();
    }

    private void loadHistory() {
        if (installedPluginsList != null) {
            File historyFile = serverInfo.resolveServer(installedPluginsList);
            Properties properties = new Properties();
            try {
                InputStream in = new FileInputStream(historyFile);
                try {
                    properties.load(in);
                    for (Object key : properties.keySet()) {
                        Artifact artifact = Artifact.create((String) key);
                        installedArtifacts.add(artifact);
                    }
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                //give up
            }
        }
    }

    private void saveHistory() {
        if (installedPluginsList != null) {
            Properties properties = new Properties();
            for (Artifact artifact : installedArtifacts) {
                properties.setProperty(artifact.toString(), "");
            }
            try {
                File historyFile = serverInfo.resolveServer(installedPluginsList);
                File parentFile = historyFile.getParentFile();
                if (!parentFile.exists()) {
                    forceMkdir(parentFile);
                }
                OutputStream out = new FileOutputStream(historyFile);
                try {
                    properties.save(out, "All the plugins that have ever been installed on this server");
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                //give up
            }
        }
    }

    /**
     * This more or less clones the current PluginInstallerGBean to create one with the same server instances (structure) but using
     * the current server as config store and assembling a server in a provided location.
     *
     * @param targetRepositoryPath     location of repository in new server (normally "repository")
     * @param relativeTargetServerPath Location of server to assemble relative to current server
     * @param pluginList               list of plugins to install
     * @throws Exception if something goes wrong
     */
    public DownloadResults installPluginList(String targetRepositoryPath, String relativeTargetServerPath, PluginListType pluginList) throws Exception {
        DownloadResults downloadPoller = new DownloadResults();
        File targetServerPath = serverInfo.resolveServer(relativeTargetServerPath);
        if (targetServerPath.exists()) {
            FileUtils.delete(targetServerPath);
        }
        String targetServerPathName = targetServerPath.getAbsolutePath();
        Kernel kernel = new BasicKernel("assembly", bundleContext);

        try {
            PluginInstallerGBean installer = new PluginInstallerGBean(
                    targetRepositoryPath,
                    targetServerPathName,
                    installedPluginsList,
                    serverInstanceDatas,
                    pluginRepositoryList,
                    kernel,
                    bundleContext);

            installer.install(pluginList, localSourceRepository, true, null, null, downloadPoller);
        } finally {
            kernel.shutdown();
        }
        return downloadPoller;
    }

    public void mergeOverrides(String server, AttributesType overrides) throws InvalidGBeanException, IOException {
        ServerInstance serverInstance = servers.get(server);
        if (serverInstance == null) {
            throw new NullPointerException("No such server: " + server + ", known servers: " + servers.keySet());
        }
        PluginAttributeStore attributeStore = serverInstance.getAttributeStore();
        for (ModuleType module : overrides.getModule()) {
            Artifact artifact = Artifact.create(module.getName());
            attributeStore.setModuleGBeans(artifact, module.getGbean(), module.isLoad(), module.getCondition());
            attributeStore.save();
        }
        if (overrides.getConfiguration().size() > 0) {
            throw new UnsupportedOperationException("Use modules, not configurations");
        }
    }


    /**
     * Lists the plugins installed in the local Geronimo server, by name and
     * ID.
     *
     * @return A Map with key type String (plugin name) and value type Artifact
     *         (config ID of the plugin).
     */
    public Map<String, Artifact> getInstalledPlugins() {
        SortedSet<Artifact> artifacts = writeableRepo.list();

        Map<String, Artifact> plugins = new HashMap<String, Artifact>();
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
                    log.error("Cannot read artifact dir {}", dir.getAbsolutePath());
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
                    log.error("Unable to read JAR file {}", dir.getAbsolutePath(), e);
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
        PluginType type = localSourceRepository.extractPluginMetadata(moduleId);
        if (null == type) {
            try {
                type = createDefaultMetadata(moduleId);
            } catch (InvalidConfigException e) {
                log.warn("Unable to generate metadata for " + moduleId, e);
            } catch (Exception e) {
                log.warn("Error generating metadata for " + moduleId, e);
            }
        }
        return type;
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
            log.error("{} is not installed", artifact);
            throw new IllegalArgumentException(artifact + " is not installed.");
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
                        PluginXmlUtil.writePluginMetadata(metadata, out);
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
                    log.error("Unable to delete old plugin at {}", dir.getAbsolutePath());
                    throw new IOException("Unable to delete old plugin at " + dir.getAbsolutePath());
                }
                if (!temp.renameTo(dir)) {
                    log.error("Unable to move new plugin {} to {}", temp.getAbsolutePath(), dir.getAbsolutePath());
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
                log.error("{} is not a plugin", artifact);
                throw new IllegalArgumentException(artifact + " is not a plugin.");
            }
            File xml = new File(meta, "geronimo-plugin.xml");
            FileOutputStream fos = null;
            try {
                if (!xml.isFile()) {
                    if (!xml.createNewFile()) {
                        log.error("Cannot create plugin metadata file for {}", artifact);
                        throw new RuntimeException("Cannot create plugin metadata file for " + artifact);
                    }
                }
                fos = new FileOutputStream(xml);
                PluginXmlUtil.writePluginMetadata(metadata, fos);
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

    /**
     * Lists the plugins available for download in a particular Geronimo repository.
     *
     * @param mavenRepository The base URL to the maven repository.  This must
     *                        contain the file geronimo-plugins.xml
     */
    public PluginListType listPlugins(URL mavenRepository) throws IOException, FailedLoginException {
        try {
            SourceRepository repo = pluginRepositoryList.getSourceRepository(mavenRepository.toString());
            if (repo != null) {
                return repo.getPluginList();
            }
        } catch (IllegalStateException e) {
        }
        return null;
    }

    private SourceRepository getDefaultSourceRepository(String defaultRepository,
                                                        boolean restrictToDefaultRepository) {
        if (restrictToDefaultRepository && defaultRepository == null) {
            throw new IllegalArgumentException("You must supply a default repository if you want to restrict to it");
        }
        SourceRepository defaultSourceRepository = defaultRepository == null ? null : pluginRepositoryList.getSourceRepository(defaultRepository);
        return defaultSourceRepository;
    }

    /**
     * Installs a configuration from a remote repository into the local Geronimo server,
     * including all its dependencies.  The caller will get the results when the
     * operation completes.  Note that this method does not throw exceptions on failure,
     * but instead sets the failure property of the DownloadResults.
     *
     * @param pluginsToInstall            The list of configurations to install
     * @param defaultRepository           Default repo to look for plugins in
     * @param restrictToDefaultRepository Whether to follow hints to other plugin repos.
     * @param username                    Optional username, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required.
     * @param password                    Optional password, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required.
     */
    public DownloadResults install(PluginListType pluginsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        DownloadResults results = new DownloadResults();
        install(pluginsToInstall, defaultRepository, restrictToDefaultRepository, username, password, results);
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
     * @param pluginsToInstall            The list of configurations to install
     * @param defaultRepository           Default repo to look for plugins in (not required)
     * @param restrictToDefaultRepository Whether to follow hints to other plugin repos.
     * @param username                    Optional username, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required.
     * @param password                    Optional password, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required.
     * @param poller                      Will be notified with status updates as the download proceeds
     */
    public void install(PluginListType pluginsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password, DownloadPoller poller) {
        SourceRepository defaultSourceRepository = getDefaultSourceRepository(defaultRepository, restrictToDefaultRepository);
        install(pluginsToInstall, defaultSourceRepository, restrictToDefaultRepository, username, password, poller);
    }

    public void install(PluginListType pluginsToInstall, SourceRepository defaultRepository, boolean restrictToDefaultRepository, String username, String password, DownloadPoller poller) {
        install(pluginsToInstall, defaultRepository, restrictToDefaultRepository, username, password, poller, true);
    }

    public void install(PluginListType pluginsToInstall, SourceRepository defaultRepository, boolean restrictToDefaultRepository, String username, String password, DownloadPoller poller, boolean validatePlugins) {
        List<Artifact> downloadedArtifacts = new ArrayList<Artifact>();
        try {
            Map<Artifact, PluginType> metaMap = new HashMap<Artifact, PluginType>();
            // Step 1: validate everything
            List<PluginType> toInstall = new ArrayList<PluginType>();
            for (PluginType metadata : pluginsToInstall.getPlugin()) {
                try {
                    if (validatePlugins) {
                        if (!validatePlugin(metadata)) {
                            throw new MissingDependencyException("Already installed", toArtifact(metadata.getPluginArtifact().get(0).getModuleId()), (Stack<Artifact>) null);
                        }
                        verifyPrerequisites(metadata);
                    }

                    PluginArtifactType instance = metadata.getPluginArtifact().get(0);

                    if (instance.getModuleId() != null) {
                        if (metadata.isPluginGroup() != null && !metadata.isPluginGroup()) {
                            metaMap.put(toArtifact(instance.getModuleId()), metadata);
                        }
                    }
                    toInstall.add(metadata);
                } catch (MissingDependencyException e) {
                    poller.addSkippedConfigID(e);
                }
            }

            // Step 2: everything is valid, do the installation
            for (PluginType metadata : toInstall) {
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
                Stack<Artifact> parentStack = new Stack<Artifact>();
                if (instance.getModuleId() != null) {
                    Artifact entry = toArtifact(instance.getModuleId());
                    List<SourceRepository> repos = getRepos(pluginsToInstall, defaultRepository, restrictToDefaultRepository, instance);
                    downloadArtifact(entry, metaMap, repos,
                            username, password, new ResultsFileWriteMonitor(poller, log), working, parentStack, false, servers, true);
                    downloadedArtifacts.add(entry);
                } else {
                    List<DependencyType> deps = instance.getDependency();
                    for (DependencyType dep : deps) {
                        Artifact entry = toArtifact(dep);
                        List<SourceRepository> repos = getRepos(pluginsToInstall, defaultRepository, restrictToDefaultRepository, instance);
                        downloadArtifact(entry, metaMap, repos,
                                username, password, new ResultsFileWriteMonitor(poller, log), working, parentStack, false, servers, dep.isStart());
                        downloadedArtifacts.add(entry);
                    }
                }
                // 4. Uninstall obsolete configurations
                for (Artifact artifact : obsoletes) {
                    configManager.uninstallConfiguration(artifact);
                }
                // 5. Installation of this configuration finished successfully
            }

            // Step 3: Start anything that's marked accordingly
            if (configManager.isOnline()) {
                poller.setCurrentFilePercent(-1);
                for (PersistentConfigurationList persistentConfigurationList : persistentConfigurationLists) {
                    List<Artifact> artifacts = persistentConfigurationList.restore();
                    for (Artifact artifact : artifacts) {
                        if (!configManager.isRunning(artifact)) {
                            poller.setCurrentMessage("Starting " + artifact);
                            if (!configManager.isLoaded(artifact)) {
                                try {
                                    configManager.loadConfiguration(artifact);
                                } catch (Exception e) {
                                    log.error("Unable to load configuration. ", e);
                                    poller.setFailure(e);
                                    configManager.uninstallConfiguration(artifact);
                                }
                            }

                            if (configManager.isLoaded(artifact)) {
                                configManager.startConfiguration(artifact);
                            }
                        }
                    }
                }
            }
            //ensure config.xml is saved.
            for (org.apache.geronimo.system.plugin.ServerInstance serverInstance : servers.values()) {
                serverInstance.getAttributeStore().save();
            }
        } catch (Exception e) {
            log.error("Unable to install plugin", e);
            poller.setFailure(e);
            //Attempt to cleanup a failed plugin installation
            for (Artifact artifact : downloadedArtifacts) {
                try {
                    configManager.uninstallConfiguration(artifact);
                } catch (Exception e2) {
                    log.warn(e2.toString(), e2);
                }
            }
        } finally {
            poller.setFinished();
        }
        saveHistory();
    }

    private List<SourceRepository> getRepos(PluginListType pluginsToInstall, SourceRepository defaultRepository, boolean restrictToDefaultRepository, PluginArtifactType instance) {
        List<SourceRepository> repos = new ArrayList<SourceRepository>();
        if (defaultRepository != null) {
            repos.add(defaultRepository);
        }
        if (!restrictToDefaultRepository) {
            if (!instance.getSourceRepository().isEmpty()) {
                addRepos(repos, instance.getSourceRepository());
            }

            //always add the default repository location no matter if the plugin instance contains source-repository.
            addRepos(repos, pluginsToInstall.getDefaultRepository());
        }
        return repos;
    }

    private void addRepos(List<SourceRepository> repos, List<String> repoLocations) {
        for (String repoLocation : repoLocations) {
            try {
                SourceRepository repo = pluginRepositoryList.getSourceRepository(repoLocation);
                repos.add(repo);
            } catch (IllegalStateException e) {
                log.warn("Invalid repository: " + repoLocation, e);
            }
        }
    }

    /**
     * Installs a configuration from a remote repository into the local Geronimo server,
     * including all its dependencies.  The method returns immediately, providing a key
     * that can be used to poll the status of the download operation.  Note that the
     * installation does not throw exceptions on failure, but instead sets the failure
     * property of the DownloadResults that the caller can poll for.
     *
     * @param pluginsToInstall            The list of configurations to install
     * @param defaultRepository           Default repo to look for plugins in
     * @param restrictToDefaultRepository Whether to follow hints to other plugin repos.
     * @param username                    Optional username, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required.
     * @param password                    Optional password, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required. @return A key that can be passed to checkOnInstall
     */
    public Object startInstall(final PluginListType pluginsToInstall, final String defaultRepository, final boolean restrictToDefaultRepository, final String username, final String password) {
        Object key = getNextKey();
        final DownloadResults results = new DownloadResults();
        Runnable work = new Runnable() {
            public void run() {
                install(pluginsToInstall, defaultRepository, restrictToDefaultRepository, username, password, results);
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
     * @param carFile                     A CAR file downloaded from a remote repository.  This is a packaged
     *                                    configuration with included configuration information, but it may
     *                                    still have external dependencies that need to be downloaded
     *                                    separately.  The metadata in the CAR file includes a repository URL
     *                                    for these downloads, and the username and password arguments are
     *                                    used in conjunction with that.
     * @param defaultRepository           Default repo to look for plugins in
     * @param restrictToDefaultRepository Whether to follow hints to other plugin repos.
     * @param username                    Optional username, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required.
     * @param password                    Optional password, if the maven repo uses HTTP Basic authentication.
     *                                    Set this to null if no authentication is required. @return A key that can be passed to checkOnInstall
     */
    public Object startInstall(final File carFile, final String defaultRepository, final boolean restrictToDefaultRepository, final String username, final String password) {
        Object key = getNextKey();
        final DownloadResults results = new DownloadResults();
        Runnable work = new Runnable() {
            public void run() {
                install(carFile, defaultRepository, restrictToDefaultRepository, username, password, results);
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
        return checkOnInstall(key, true);
    }

    /**
     * Gets the current progress of a download operation.
     *
     * @param key    Identifies the operation to check on
     * @param remove If true and the download operation has finished, the DownloadResults
     *               will be forgotten and the next call to this function will return null.
     *               Otherwise, the DownloadResults will be retained until this function is
     *               called with the <tt>remove</tt> parameter set to true. This parameter is
     *               only used when the download operation has finished
     *               (DownloadResults.isFinished() returns true).
     */
    public DownloadResults checkOnInstall(Object key, boolean remove) {
        DownloadResults results = asyncKeys.get(key);
        if (results != null) {
            results = results.duplicate();
            if (results.isFinished() && remove) {
                asyncKeys.remove(key);
            }
        }
        return results;
    }

    /**
     * Installs from a pre-downloaded CAR file
     *
     * @param carFile                     car file to install
     * @param defaultRepository           Default repo to look for plugins in
     * @param restrictToDefaultRepository Whether to follow hints to other plugin repos.
     * @param username                    repo username
     * @param password                    repo password
     * @param poller                      monitor for reporting progress
     */
    public void install(File carFile, String defaultRepository, boolean restrictToDefaultRepository, String username, String password, DownloadPoller poller) {
        try {
            // 1. Extract the configuration metadata
            PluginType data = GeronimoSourceRepository.extractPluginMetadata(carFile);
            if (data == null) {
                log.error("Invalid Configuration Archive {} no plugin metadata found", carFile.getAbsolutePath());
                throw new IllegalArgumentException(
                        "Invalid Configuration Archive " + carFile.getAbsolutePath() + " no plugin metadata found");
            }

            // 2. Validate that we can install this
            if (!validatePlugin(data)) {
                //already installed
                throw new ConfigurationAlreadyExistsException("Configuration " + toArtifact(data.getPluginArtifact().get(0).getModuleId()) + " is already installed.");
            }

            verifyPrerequisites(data);

            PluginArtifactType instance = data.getPluginArtifact().get(0);
            // 3. Install the CAR into the repository (it shouldn't be re-downloaded)
            if (instance.getModuleId() != null) {
                Artifact pluginArtifact = toArtifact(instance.getModuleId());
                ResultsFileWriteMonitor monitor = new ResultsFileWriteMonitor(poller, log);
                writeableRepo.copyToRepository(carFile, pluginArtifact, monitor);
                installConfigXMLData(pluginArtifact, instance, servers, true);
                if (instance.getCopyFile() != null) {
                    extractPluginFiles(pluginArtifact, instance, monitor);
                }
            }

            // 4. Use the standard logic to remove obsoletes, install dependencies, etc.
            PluginListType pluginList = new PluginListType();
            pluginList.getPlugin().add(data);
            pluginList.getDefaultRepository().addAll(instance.getSourceRepository());

            SourceRepository defaultSourceRepository = getDefaultSourceRepository(defaultRepository, restrictToDefaultRepository);

            install(pluginList, defaultSourceRepository, restrictToDefaultRepository, username, password, poller, false);
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
     * @return true if the plugin is not installed
     * @throws org.apache.geronimo.kernel.repository.MissingDependencyException
     *          if plugin requires a dependency that is not present
     */
    public boolean validatePlugin(PluginType plugin) throws MissingDependencyException {
        if (plugin.getPluginArtifact().size() != 1) {
            throw new MissingDependencyException("A plugin configuration must include one plugin artifact, not " + plugin.getPluginArtifact().size(), null, (Stack<Artifact>) null);
        }
        PluginArtifactType metadata = plugin.getPluginArtifact().get(0);
        // 1. Check that it's not already installed
        if (metadata.getModuleId() != null) { // that is, it's a real configuration not a plugin list
            Artifact artifact = toArtifact(metadata.getModuleId());

            //plugin groups don't get registered with configManager
            if (plugin.isPluginGroup() != null && plugin.isPluginGroup()) {
                if (installedArtifacts.contains(artifact)) {
                    log.debug("Configuration {} is already installed", artifact);
                    return false;
                }
            } else {
                if (configManager.isInstalled(artifact)) {
                    boolean upgrade = false;
                    for (ArtifactType obsolete : metadata.getObsoletes()) {
                        Artifact test = toArtifact(obsolete);
                        if (test.matches(artifact)) {
                            upgrade = true;
                            break;
                        }
                    }
                    if (!upgrade && installedArtifacts.contains(artifact)) {
                        log.debug("Configuration {} is already installed", artifact);
                        return false;
                    }
                }
            }
        }

        // 2. Check that we meet the Geronimo, JVM versions
        if (metadata.getGeronimoVersion().size() > 0 && !checkGeronimoVersions(metadata.getGeronimoVersion())) {
            log.debug("Plugin " + toArtifact(metadata.getModuleId()) + " is not installable on Geronimo " + serverInfo.getVersion());
            throw new MissingDependencyException(
                    "Plugin is not installable on Geronimo " + serverInfo.getVersion(), toArtifact(metadata.getModuleId()), (Stack<Artifact>) null);
        }
        if (metadata.getJvmVersion().size() > 0 && !checkJVMVersions(metadata.getJvmVersion())) {
            log.debug("Plugin " + toArtifact(metadata.getModuleId()) + " is not installable on JVM " + System.getProperty("java.version"));
            throw new MissingDependencyException(
                    "Plugin is not installable on JVM " + System.getProperty("java.version"), toArtifact(metadata.getModuleId()), (Stack<Artifact>) null);
        }
        return true;
    }


    /**
     * Ensures that a plugin's prerequisites are installed
     *
     * @param plugin plugin artifact to check
     * @return array of missing depedencies
     */
    public Dependency[] checkPrerequisites(PluginType plugin) {
        List<Dependency> missingPrereqs = getMissingPrerequisites(plugin);
        return missingPrereqs.toArray(new Dependency[missingPrereqs.size()]);
    }

    private List<Dependency> getMissingPrerequisites(PluginType plugin) {
        if (plugin.getPluginArtifact().size() != 1) {
            throw new IllegalArgumentException("A plugin configuration must include one plugin artifact, not " + plugin.getPluginArtifact().size());
        }

        PluginArtifactType metadata = plugin.getPluginArtifact().get(0);
        List<PrerequisiteType> prereqs = metadata.getPrerequisite();

        ArrayList<Dependency> missingPrereqs = new ArrayList<Dependency>();
        for (PrerequisiteType prereq : prereqs) {
            Artifact artifact = toArtifact(prereq.getId());
            try {
                if (getServerInstance("default", servers).getArtifactResolver().queryArtifacts(artifact).length == 0) {
                    missingPrereqs.add(new Dependency(artifact, ImportType.ALL));
                }
            } catch (NoServerInstanceException e) {
                throw new RuntimeException("Invalid setup, no default server instance registered");
            }
        }
        return missingPrereqs;
    }

    private void verifyPrerequisites(PluginType plugin) throws MissingDependencyException {
        List<Dependency> missingPrereqs = getMissingPrerequisites(plugin);
        if (!missingPrereqs.isEmpty()) {
            PluginArtifactType metadata = plugin.getPluginArtifact().get(0);
            Artifact moduleId = toArtifact(metadata.getModuleId());
            StringBuilder buf = new StringBuilder();
            buf.append(moduleId.toString()).append(" requires ");
            Iterator<Dependency> iter = missingPrereqs.iterator();
            while (iter.hasNext()) {
                buf.append(iter.next().getArtifact().toString());
                if (iter.hasNext()) {
                    buf.append(", ");
                }
            }
            buf.append(" to be installed");
            throw new MissingDependencyException(buf.toString(), null, (Artifact) null);
        }
    }

    public Artifact installLibrary(File libFile, String groupId) throws IOException {
        Artifact artifact = calculateArtifact(libFile, libFile.getName(), groupId);
        if (artifact == null)
            throw new IllegalArgumentException("Can not calculate Artifact string, file should be:\n"
                    + "(1) an OSGi bundle, then the artifactId is its Bundle-SymbolicName and the version is its Bundle-Version;\n"
                    + "(2) or a file with filename in the form <artifactId>-<version>.<type>, for e.g. mylib-1.0.jar");
        installLibrary(libFile, artifact);
        return artifact;
    }

    public void installLibrary(File libFile, Artifact artifact) throws IOException {
        if (artifact == null || !artifact.isResolved())
            throw new IllegalArgumentException("Artifact is not valid when install library");
        
        if (identifyOSGiBundle(libFile) != null) {
            writeableRepo.copyToRepository(libFile, artifact, new RepoFileWriteMonitor());
        } else {
            // convert to osgi bundle jars using wrap url handler
            URL wrap = new URL("wrap", null, libFile.toURI().toURL().toExternalForm() 
                    + "$Bundle-SymbolicName=" + artifact.getArtifactId() 
                    + "&Bundle-Version=" + artifact.getVersion().toString().replace("-", ".") //need improve the version processing
                    + "&DynamicImport-Package=*"); 
            InputStream in = null;
            try {
                in = wrap.openStream();
                writeableRepo.copyToRepository(in, (int) libFile.getTotalSpace(), artifact, new RepoFileWriteMonitor());
            } finally {
                if (in != null)
                    in.close();
            }
        }
    }
    
    private String[] identifyOSGiBundle(File file) throws IOException{
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            Manifest manifest = jar.getManifest();
            if (manifest != null){
                String symbolic = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
                String version = manifest.getMainAttributes().getValue("Bundle-Version");
                if (symbolic!=null && version!=null) {
                    return new String[]{symbolic,version};
                }
            } 
        } finally {
            if (jar!=null) jar.close();
        }
        return null;
    }
        
    private class RepoFileWriteMonitor implements FileWriteMonitor {
        public void writeStarted(String fileDescription, int fileSize) {
            log.info("Copying into repository " + fileDescription + "...");
        }

        public void writeProgress(int bytes) {
        }

        public void writeComplete(int bytes) {
            log.info("Finished.");
        }
    }
    
    public Artifact calculateArtifact(File file, String fileName, String groupId) throws IOException  {
        if (groupId == null || groupId.isEmpty()) groupId = Artifact.DEFAULT_GROUP_ID;

        if (fileName == null || fileName.isEmpty()) fileName = file.getName();
        
        String artifactId = null;
        String version = null;
        String fileType = null;
        
        
        String[] bundleKey = identifyOSGiBundle(file);
        if (bundleKey != null){ //try calculate if it is an OSGi bundle
            artifactId = bundleKey[0]; //Bundle-SymbolicName
            version = bundleKey[1]; //Bundle-Version
            fileType = "jar";
            return new Artifact(groupId, artifactId, version, fileType);
            
        } else { // not an OSGi bundle, try calculate artifact string from the file name
            Matcher matcher = MAVEN_1_PATTERN_PART.matcher(fileName);
            if (matcher.matches()) {
                artifactId = matcher.group(1);
                version = matcher.group(2);
                fileType = matcher.group(3);
                return new Artifact(groupId,artifactId,version,fileType);
                
            }else{
                return null;
            }
        }
    }
    

    /**
     * Download (if necessary) and install something, which may be a Configuration or may
     * be just a JAR.  For each artifact processed, all its dependencies will be
     * processed as well.
     *
     * @param configID     Identifies the artifact to install
     * @param metadata     name to plugin map
     * @param repos        The URLs to contact the repositories (in order of preference)
     * @param username     The username used for repositories secured with HTTP Basic authentication
     * @param password     The password used for repositories secured with HTTP Basic authentication
     * @param monitor      The ongoing results of the download operations, with some monitoring logic
     * @param soFar        The set of dependencies already downloaded.
     * @param parentStack  chain of modules that led to this dependency
     * @param dependency   Is this a dependency or the original artifact?
     * @param servers      server layouts to install config info into
     * @param loadOverride If false prevents setting load="true" in server instances (recursively through dependencies)
     * @throws FailedLoginException       When a repository requires authentication and either no username
     *                                    and password are supplied or the username and password supplied
     *                                    are not accepted
     * @throws MissingDependencyException When a dependency cannot be located in any of the listed repositories
     * @throws NoServerInstanceException  when no server descriptor is found for a specified configuration bit
     * @throws java.io.IOException        when a IO problem occurs
     */
    private void downloadArtifact(Artifact configID, Map<Artifact, PluginType> metadata, List<SourceRepository> repos, String username, String password, ResultsFileWriteMonitor monitor, Set<Artifact> soFar, Stack<Artifact> parentStack, boolean dependency, Map<String, ServerInstance> servers, boolean loadOverride) throws IOException, FailedLoginException, MissingDependencyException, NoServerInstanceException {
        if (soFar.contains(configID)) {
            return; // Avoid endless work due to circular dependencies
        } else {
            soFar.add(configID);
        }
        // Download and install the main artifact
        Artifact[] matches = configManager.getArtifactResolver().queryArtifacts(configID);
        PluginArtifactType instance = null;
        if (matches.length == 0) {
            // not present, needs to be downloaded
            monitor.getResults().setCurrentMessage("Downloading " + configID);
            monitor.getResults().setCurrentFilePercent(-1);
            OpenResult result = null;
            for (SourceRepository repository : repos) {
                result = repository.open(configID, monitor);
                if (result != null) {
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Could not find " + configID + " in any repo: " + repos);
            }
            // Check if the result is already in server's repository (maybe the actual configId is not what was expected?)
            if (configManager.getArtifactResolver().queryArtifacts(result.getArtifact()).length > 0) {
                String msg = "Not downloading " + configID + ". Query for " + configID + " resulted in " + result.getArtifact()
                        + " which is already available in server's repository.";
                monitor.getResults().setCurrentMessage(msg);
                log.info(msg);
                result.close();
                return;
            }
            try {
                File tempFile = result.getFile();
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
                        log.error("File download incorrect (expected {} hash {} but got {})", new String[]{hash.getType(), hash.getValue(), actual});
                        throw new IOException("File download incorrect (expected " + hash.getType() + " hash " + hash.getValue() + " but got " + actual + ")");
                    }
                }
                // See if the download file has plugin metadata and use it in preference to what is in the catalog.
                try {
                    PluginType realPluginData = GeronimoSourceRepository.extractPluginMetadata(tempFile);
                    if (realPluginData != null) {
                        pluginData = realPluginData;
                    }
                } catch (Exception e) {
                    log.error("Unable to read plugin metadata: {}", e.getMessage());
                    throw (IOException) new IOException("Unable to read plugin metadata: " + e.getMessage()).initCause(e);
                }
                if (pluginData != null) { // it's a plugin, not a plain JAR
                    if (!validatePlugin(pluginData)) {
                        monitor.getResults().addSkippedConfigID(new MissingDependencyException("already installed", configID, (Stack<Artifact>) null));
                        return;
                    }
                    instance = pluginData.getPluginArtifact().get(0);
                }
                monitor.getResults().setCurrentMessage("Copying " + result.getArtifact() + " to the repository");
                result.install(writeableRepo, monitor);
                if (dependency) {
                    monitor.getResults().addDependencyInstalled(configID);
                    configID = result.getArtifact();
                } else {
                    configID = result.getArtifact();
                    monitor.getResults().addInstalledConfigID(configID);
                }
                if (pluginData != null) {
                    log.debug("Installed plugin with moduleId={} and name={}", pluginData.getPluginArtifact().get(0).getModuleId(), pluginData.getName());
                } else {
                    log.debug("Installed artifact={}", configID);
                }
            } finally {
                //todo probably not needede
                result.close();
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
            if (!configID.isResolved()) {
                // See if something's running
                for (int i = matches.length - 1; i >= 0; i--) {
                    Artifact match = matches[i];
                    if (configStore.containsConfiguration(match) && configManager.isRunning(match)) {
                        log.debug("Found required configuration={} and it is running", match);
                        return; // its dependencies must be OK
                    } else {
                        log.debug("Either required configuration={} is not installed or it is not running", match);
                    }
                }
                // Go with something that's installed
                configID = matches[matches.length - 1];
            }
            ConfigurationData data = null;
            if (configStore.containsConfiguration(configID)) {
                if (configManager.isRunning(configID)) {
                    return; // its dependencies must be OK
                }
                log.debug("Loading configuration={}", configID);
                data = configStore.loadConfiguration(configID);
            }
            // Download the dependencies
            parentStack.push(configID);
            if (instance == null) {
                PluginType currentPlugin = getPluginMetadata(configID);
                if (currentPlugin != null) {
                    instance = currentPlugin.getPluginArtifact().get(0);
                }
            }
            if (instance == null) {
                //no plugin metadata, guess with something else
                if (data != null) {
                    for (Dependency dep : getDependencies(data)) {
                        Artifact artifact = dep.getArtifact();
                        log.debug("Attempting to download dependency={} for configuration={}", artifact, configID);
                        downloadArtifact(artifact, metadata, repos, username, password, monitor, soFar, parentStack, true, servers, loadOverride);
                    }
                }
            } else {
                //rely on plugin metadata if present.
                List<DependencyType> deps = instance.getDependency();
                for (DependencyType dep : deps) {
                    Artifact artifact = toArtifact(dep);
                    log.debug("Attempting to download dependency={} for configuration={}", artifact, configID);
                    downloadArtifact(artifact, metadata, repos, username, password, monitor, soFar, parentStack, true, servers, loadOverride & dep.isStart());
                }
            }
            parentStack.pop();
        } catch (NoSuchConfigException e) {
            log.error("Installed configuration into repository but ConfigStore does not see it: {}", e.getMessage());
            throw new IllegalStateException("Installed configuration into repository but ConfigStore does not see it: " + e.getMessage(), e);
        } catch (InvalidConfigException e) {
            log.error("Installed configuration into repository but ConfigStore cannot load it: {}", e.getMessage());
            throw new IllegalStateException("Installed configuration into repository but ConfigStore cannot load it: " + e.getMessage(), e);
        }
        // Copy any files out of the artifact
        for (ServerInstance serverInstance : servers.values()) {
            if (serverInstance.getAttributeStore().isModuleInstalled(configID)) {
                installedArtifacts.add(configID);
                return;
            }
        }
        if (instance != null) {
            installedArtifacts.add(configID);
            try {
                installConfigXMLData(configID, instance, servers, loadOverride);
            } catch (InvalidGBeanException e) {
                log.error("Invalid gbean configuration", e);
                throw new IllegalStateException("Invalid GBean configuration: " + e.getMessage(), e);
            }
            extractPluginFiles(configID, instance, monitor);
        }
    }

    private void extractPluginFiles(Artifact configID, PluginArtifactType instance, ResultsFileWriteMonitor monitor) throws IOException {
        for (CopyFileType data : instance.getCopyFile()) {
            monitor.getResults().setCurrentFilePercent(-1);
            monitor.getResults().setCurrentFile(data.getValue());
            monitor.getResults().setCurrentMessage("Copying " + data.getValue() + " from plugin to Geronimo installation");
            copyFile(data, configID);
        }
    }

    void copyFile(CopyFileType data, Artifact configID) throws IOException {
        Set<URL> set;
        String sourceFile = data.getValue().trim();
        try {
            set = configStore.resolve(configID, null, sourceFile);
        } catch (NoSuchConfigException e) {
            log.error("Unable to identify module {} to copy files from", configID);
            throw new IllegalStateException("Unable to identify module " + configID + " to copy files from", e);
        }
        if (set.size() == 0) {
            log.error("Installed '{}' configuration into repository but cannot locate file to copy {}", configID, sourceFile);
            return;
        }
        if (set.iterator().next().getPath().endsWith("/")) {
            //directory, get all contents
            String pattern = sourceFile;
            if (pattern.length() == 0 || pattern.endsWith("/")) {
                pattern = pattern + "**";
            } else {
                pattern = pattern + "/**";
            }
            try {
                set = new TreeSet<URL>(new Comparator<URL>() {

                    public int compare(URL o1, URL o2) {
                        return o1.getPath().compareTo(o2.getPath());
                    }
                });
                set.addAll(configStore.resolve(configID, null, pattern));
            } catch (NoSuchConfigException e) {
                log.error("Unable to list directory {} to copy files from", pattern);
                throw new IllegalStateException("Unable to list directory " + pattern + " to copy files from", e);
            }
        }
        boolean relativeToServer = "server".equals(data.getRelativeTo());
        String destDir = data.getDestDir();
        File targetDir = relativeToServer ? serverInfo.resolveServer(destDir) : serverInfo.resolve(destDir);


        createDirectory(targetDir);
        URI targetURI = targetDir.toURI();
        if (!targetDir.isDirectory()) {
            log.error("Plugin install cannot write file {} to {} because {} is not a directory", new String[]{data.getValue(), destDir, targetDir.getAbsolutePath()});
            return;
        }
        if (!targetDir.canWrite()) {
            log.error("Plugin install cannot write file {} to {} because {} is not writable", new String[]{data.getValue(), destDir, targetDir.getAbsolutePath()});
            return;
        }
        int start = -1;
        for (URL url : set) {
            String path = url.getPath();
            if (start == -1) {
                if (sourceFile.length() == 0 || sourceFile.endsWith("/")) {
                    if ("jar".equals(url.getProtocol())) {
                        start = path.lastIndexOf("!/") + 2 + sourceFile.length();
                    } else {
                        start = path.length();
                    }
                    //this entry needs nothing done
                    continue;
                } else {
                    String remove = sourceFile;
                    int pos = sourceFile.lastIndexOf('/');
                    if (pos > -1) {
                        remove = sourceFile.substring(pos + 1, sourceFile.length());
                    }
                    start = path.lastIndexOf(remove);
                }
            }
            path = path.substring(start);
            File target = new File(targetURI.resolve(path));
            if (!target.exists()) {
                if (path.endsWith("/")) {
                    if (!target.mkdirs()) {
                        log.error("Plugin install cannot create directory {}", target.getAbsolutePath());
                    }
                    continue;
                }
                if (!target.createNewFile()) {
                    log.error("Plugin install cannot create new file {}", target.getAbsolutePath());
                    continue;
                }
            }
            if (target.isDirectory()) {
                continue;
            }
            if (!target.canWrite()) {
                log.error("Plugin install cannot write to file {}", target.getAbsolutePath());
                continue;
            }
            InputStream in = url.openStream();
            try {
                copyFile(in, new FileOutputStream(target));
            } catch (IOException e) {
                throw new IOException("Could not copy " + url + " in artifact " + configID, e);
            } catch (NullPointerException e) {
                throw new IOException("Could not copy " + url + " in artifact " + configID, e);
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
        return dependencies.toArray(new Dependency[dependencies.size()]);
    }

    /**
     * Searches for an artifact in the listed repositories, where the artifact
     * may have wildcards in the ID.
     */

    /**
     * Puts the name and ID of a plugin into the argument map of plugins,
     * by reading the values out of the provided plugin descriptor file.
     *
     * @param xml     The geronimo-plugin.xml for this plugin
     * @param plugins The result map to populate
     */
    private void readNameAndID(File xml, Map<String, Artifact> plugins) {
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
     * TODO figure out where this  can be called -- perhaps when installing a plugin?
     * Generates a default plugin metadata based on the data for this module
     * in the server.
     */
    private PluginType createDefaultMetadata(Artifact moduleId) throws InvalidConfigException, IOException, NoSuchConfigException {
        if (configManager != null) {
            if (!configManager.isConfiguration(moduleId)) {
                return null;
            }
        } else {
            if (!configStore.containsConfiguration(moduleId)) {
                return null;
            }
        }
        ConfigurationData data = configStore.loadConfiguration(moduleId);

        PluginType meta = new PluginType();
        PluginArtifactType instance = new PluginArtifactType();
        meta.getPluginArtifact().add(instance);
        meta.setName(toArtifactType(moduleId).getArtifactId());
        instance.setModuleId(toArtifactType(moduleId));
        meta.setCategory("Unknown");
        instance.getGeronimoVersion().add(serverInfo.getVersion());
        instance.getObsoletes().add(toArtifactType(new Artifact(moduleId.getGroupId(),
                moduleId.getArtifactId(),
                (Version) null,
                moduleId.getType())));
        List<DependencyType> deps = instance.getDependency();
        addGeronimoDependencies(data, deps, true);
        return meta;
    }

    /**
     * Check whether the specified JVM versions match the current runtime
     * environment.
     *
     * @param jvmVersions allowed jvm versions
     * @return true if the specified versions match the current
     *         execution environment as defined by plugins-1.2.xsd
     */
    private boolean checkJVMVersions(List<String> jvmVersions) {
        if (jvmVersions.size() == 0) return true;
        String version = System.getProperty("java.version");
        boolean match = false;
        for (String jvmVersion : jvmVersions) {
            if (jvmVersion == null || jvmVersion.equals("")) {
                log.error("jvm-version should not be empty.");
                throw new IllegalStateException("jvm-version should not be empty.");
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
     * @param gerVersions geronimo versions allowed by the plugin
     * @return true if the specified versions match the current
     *         execution environment as defined by plugins-1.2.xsd
     * @throws IllegalStateException if match does not work.
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
     * @param gerVersion geronimo version to check against this server
     * @return true if the specified version matches the current
     *         execution environment as defined by plugins-1.2.xsd
     * @throws IllegalStateException if input is malformed
     */
    private boolean checkGeronimoVersion(String gerVersion) throws IllegalStateException {
        String version = serverInfo.getVersion();

        if ((gerVersion == null) || gerVersion.equals("")) {
            log.error("geronimo-version cannot be empty.");
            throw new IllegalStateException("geronimo-version cannot be empty.");
        } else {
            return gerVersion.equals(version);
        }
    }

    public static void addGeronimoDependencies(ConfigurationData data, List<DependencyType> deps, boolean includeVersion) {
        processDependencyList(data.getEnvironment().getDependencies(), deps, includeVersion);
        Map<String, ConfigurationData> children = data.getChildConfigurations();
        for (ConfigurationData child : children.values()) {
            processDependencyList(child.getEnvironment().getDependencies(), deps, includeVersion);
        }
    }

    /**
     * Generates dependencies and an optional prerequisite based on a list of
     * dependencies for a Gernonimo module.
     *
     * @param real           A list with elements of type Dependency
     * @param deps           A list with elements of type String (holding a module ID / Artifact name)
     * @param includeVersion whether to include a version in the plugin xml dependency
     */
    private static void processDependencyList(List<Dependency> real, List<DependencyType> deps, boolean includeVersion) {
        for (Dependency dep : real) {
            DependencyType dependency = toDependencyType(dep, includeVersion);
            if (!deps.contains(dependency)) {
                deps.add(dependency);
            }
        }
    }

    public static DependencyType toDependencyType(Dependency dep, boolean includeVersion) {
        Artifact id = dep.getArtifact();
        DependencyType dependency = new DependencyType();
        dependency.setGroupId(id.getGroupId());
        dependency.setArtifactId(id.getArtifactId());
        if (includeVersion) {
            dependency.setVersion(id.getVersion() == null ? null : id.getVersion().toString());
        }
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
        copy.setPluginGroup(metadata.isPluginGroup() == null ? false : metadata.isPluginGroup());
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
        copy.setPluginGroup(metadata.isPluginGroup() == null ? false : metadata.isPluginGroup());
        copy.setDescription(metadata.getDescription());
        copy.setName(metadata.getName());
        copy.setUrl(metadata.getUrl());
        copy.getLicense().addAll(metadata.getLicense());
        return copy;
    }

    private static class PluginKey extends PluginType {
        private static final long serialVersionUID = -3864898789387102435L;

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PluginKey that = (PluginKey) o;

            if (author != null ? !author.equals(that.author) : that.author != null) return false;
            if (category != null ? !category.equals(that.category) : that.category != null) return false;
            if (description != null ? !description.equals(that.description) : that.description != null) return false;
            if (pluginGroup != null ? pluginGroup != that.pluginGroup : that.pluginGroup != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;
            if ((license == null) != (that.license == null)) return false;
            if (license != null) {
                if (license.size() != that.license.size()) return false;
                int i = 0;
                for (LicenseType licenseType : license) {
                    LicenseType otherLicense = that.license.get(i++);
                    if (licenseType.isOsiApproved() != otherLicense.isOsiApproved()) return false;
                    if (licenseType.getValue() != null ? !licenseType.getValue().equals(otherLicense.getValue()) : otherLicense.getValue() != null)
                        return false;
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

    public PluginListType createPluginListForRepositories(String repo) throws NoSuchStoreException {
        PluginListType pluginList = localSourceRepository.getPluginList();
        if (repo != null) {
            pluginList.getDefaultRepository().add(repo);
        }
        return pluginList;
    }

    /**
     * If a plugin includes config.xml content, copy it into the attribute
     * store.
     *
     * @param configID     artifact we are installing
     * @param pluginData   metadata for plugin
     * @param servers      server metadata that might be modified
     * @param loadOverride overrides the load setting from plugin metadata in a config.xml module.
     * @throws java.io.IOException       if IO problem occurs
     * @throws org.apache.geronimo.kernel.InvalidGBeanException
     *                                   if an invalid gbean configuration is encountered
     * @throws NoServerInstanceException if the plugin expects a server metadata that is not present
     */
    private void installConfigXMLData(Artifact configID, PluginArtifactType pluginData, Map<String, ServerInstance> servers, boolean loadOverride) throws InvalidGBeanException, IOException, NoServerInstanceException {
        if (configManager.isConfiguration(configID)) {
            if (pluginData != null && !pluginData.getConfigXmlContent().isEmpty()) {
                for (ConfigXmlContentType configXmlContent : pluginData.getConfigXmlContent()) {
                    String serverName = configXmlContent.getServer();
                    ServerInstance serverInstance = getServerInstance(serverName, servers);
                    serverInstance.getAttributeStore().setModuleGBeans(configID, configXmlContent.getGbean(), loadOverride && configXmlContent.isLoad(), configXmlContent.getCondition());
                }
            } else {
                ServerInstance serverInstance = getServerInstance("default", servers);
                serverInstance.getAttributeStore().setModuleGBeans(configID, null, loadOverride, null);
            }
        }
        if (pluginData == null) {
            return;
        }
        if (!pluginData.getConfigSubstitution().isEmpty()) {
            Map<String, Map<String, String>> propertiesMap = toPropertiesMap(pluginData.getConfigSubstitution());
            for (Map.Entry<String, Map<String, String>> entry : propertiesMap.entrySet()) {
                String serverName = entry.getKey();
                ServerInstance serverInstance = getServerInstance(serverName, servers);
                serverInstance.getAttributeStore().addConfigSubstitutions(entry.getValue());
            }
        }
        if (!pluginData.getArtifactAlias().isEmpty()) {
            Map<String, Map<String, String>> propertiesMap = toPropertiesMap(pluginData.getArtifactAlias());
            for (Map.Entry<String, Map<String, String>> entry : propertiesMap.entrySet()) {
                String serverName = entry.getKey();
                ServerInstance serverInstance = getServerInstance(serverName, servers);
                serverInstance.getArtifactResolver().addAliases(entry.getValue());
            }
        }
    }

    private ServerInstance getServerInstance(String serverName, Map<String, ServerInstance> servers) throws NoServerInstanceException {
        ServerInstance serverInstance = servers.get(serverName);
        if (serverInstance == null) {
            throw new NoServerInstanceException("No server instance configuration set up for name " + serverName);
        }
        return serverInstance;
    }

    private Map<String, Map<String, String>> toPropertiesMap(List<PropertyType> propertyTypes) {
        Map<String, Map<String, String>> propertiesMap = new HashMap<String, Map<String, String>>();
        for (PropertyType propertyType : propertyTypes) {
            String serverName = propertyType.getServer();
            Map<String, String> properties = propertiesMap.get(serverName);
            if (properties == null) {
                properties = new HashMap<String, String>();
                propertiesMap.put(serverName, properties);
            }
            properties.put(propertyType.getKey(), propertyType.getValue());
        }
        return propertiesMap;
    }

    /**
     * Gets a token unique to this run of the server, used to track asynchronous
     * downloads.
     *
     * @return unique (for this server) key
     */
    private static Object getNextKey() {
        int value;
        synchronized (PluginInstallerGBean.class) {
            value = ++counter;
        }
        return value;
    }

    private static void forceMkdir(File dir) {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new RuntimeException("'directory' is a file: " + dir);
            }
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Could not create dir: " + dir);
        }
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

        public ResultsFileWriteMonitor(DownloadPoller results, Logger log) {
            this.results = new LoggingPoller(results, log);
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
            results.setCurrentMessage("Downloading " + file);
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

    private static class LoggingPoller implements DownloadPoller {
        private final DownloadPoller downloadPoller;
        private final Logger log;

        private LoggingPoller(DownloadPoller downloadPoller, Logger log) {
            this.downloadPoller = downloadPoller;
            this.log = log;
        }

        public void addRemovedConfigID(Artifact obsolete) {
            log.debug("Removed artifact: {}", obsolete);
            downloadPoller.addRemovedConfigID(obsolete);
        }

        public void addInstalledConfigID(Artifact target) {
            log.debug("Installed artifact: {}", target);
            downloadPoller.addInstalledConfigID(target);
        }

        public void addRestartedConfigID(Artifact target) {
            log.debug("Restarted artifact: {}", target);
            downloadPoller.addRestartedConfigID(target);
        }

        public void addSkippedConfigID(MissingDependencyException e) {
            log.debug("Skipped artifact due to: ", e);
            downloadPoller.addSkippedConfigID(e);
        }

        public void addDependencyPresent(Artifact dep) {
            log.debug("Artifact already installed: {}", dep);
            downloadPoller.addDependencyPresent(dep);
        }

        public void addDependencyInstalled(Artifact dep) {
            log.debug("Installed dependency {}", dep);
            downloadPoller.addDependencyInstalled(dep);
        }

        public void setCurrentFile(String currentFile) {
            log.debug("Current file: {}", currentFile);
            downloadPoller.setCurrentFile(currentFile);
        }

        public void setCurrentMessage(String currentMessage) {
            log.debug(currentMessage);
            downloadPoller.setCurrentMessage(currentMessage);
        }

        public void setCurrentFilePercent(int currentFileProgress) {
            downloadPoller.setCurrentFilePercent(currentFileProgress);
        }

        public void addDownloadBytes(long bytes) {
            downloadPoller.addDownloadBytes(bytes);
        }

        public void setFailure(Exception failure) {
            log.debug("Failure: {}", failure);
            downloadPoller.setFailure(failure);
        }

        public void setFinished() {
            log.debug("Finished");
            downloadPoller.setFinished();
        }
    }

}
