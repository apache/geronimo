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
import java.io.FileNotFoundException;
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
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;
import org.apache.geronimo.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A GBean that knows how to download configurations from a Maven repository.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigInstallerGBean implements ConfigurationInstaller {
    private final static Log log = LogFactory.getLog(ConfigInstallerGBean.class);
    private static int counter;
    private ConfigurationManager configManager;
    private WritableListableRepository writeableRepo;
    private ConfigurationStore configStore;
    private ArtifactResolver resolver;
    private ServerInfo serverInfo;
    private Map asyncKeys;
    private ThreadPool threadPool;

    public ConfigInstallerGBean(ConfigurationManager configManager, WritableListableRepository repository, ConfigurationStore configStore, ServerInfo serverInfo, ThreadPool threadPool) {
        this.configManager = configManager;
        this.writeableRepo = repository;
        this.configStore = configStore;
        this.serverInfo = serverInfo;
        this.threadPool = threadPool;
        resolver = new DefaultArtifactResolver(null, writeableRepo);
        asyncKeys = Collections.synchronizedMap(new HashMap());
    }

    private static Object getNextKey() {
        int value;
        synchronized(ConfigInstallerGBean.class) {
            value = ++counter;
        }
        return new Integer(value);
    }

    public Map getInstalledPlugins() {
        SortedSet artifacts = writeableRepo.list();

        Map plugins = new HashMap();
        for (Iterator i = artifacts.iterator(); i.hasNext();) {
            Artifact configId = (Artifact) i.next();
            File dir = writeableRepo.getLocation(configId);
            File meta = new File(dir, "META-INF");
            if(!meta.isDirectory() || !meta.canRead()) {
                continue;
            }
            File xml = new File(meta, "geronimo-plugin.xml");
            if(!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                continue;
            }
            readNameAndID(xml, plugins);
        }
        return plugins;
    }

    public ConfigurationArchiveData getPluginMetadata(Artifact configId) {
        File dir = writeableRepo.getLocation(configId);
        File meta = new File(dir, "META-INF");
        if(!meta.isDirectory() || !meta.canRead()) {
            return null;
        }
        File xml = new File(meta, "geronimo-plugin.xml");
        try {
            if(!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                return new ConfigurationArchiveData(null, new URL[0], createDefaultMetadata(configStore.loadConfiguration(configId)));
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            return loadConfigurationMetadata(doc, xml);
        } catch (InvalidConfigException e) {
            log.warn("Unable to generate metadata for "+configId, e);
        } catch (Exception e) {
            log.warn("Invalid XML at "+xml.getAbsolutePath(), e);
        }
        return null;
    }

    public void updatePluginMetadata(ConfigurationArchiveData metadata) {
        File dir = writeableRepo.getLocation(metadata.getConfiguration().getConfigId());
        if(dir == null) {
            throw new IllegalArgumentException(metadata.getConfiguration().getConfigId()+" is not installed!");
        }
        File meta = new File(dir, "META-INF");
        if(!meta.isDirectory() || !meta.canRead()) {
            throw new IllegalArgumentException(metadata.getConfiguration().getConfigId()+" is not a plugin!");
        }
        File xml = new File(meta, "geronimo-plugin.xml");
        try {
            if(!xml.isFile()) {
                if(!xml.createNewFile()) {
                    throw new RuntimeException("Cannot create plugin metadata file for "+metadata.getConfiguration().getConfigId());
                }
            }
            Document doc = writeConfigurationMetadata(metadata);
            TransformerFactory xfactory = TransformerFactory.newInstance();
            Transformer xform = xfactory.newTransformer();
            xform.setOutputProperty(OutputKeys.INDENT, "yes");
            xform.transform(new DOMSource(doc), new StreamResult(xml));
        } catch (Exception e) {
            log.error("Unable to save plugin metadata for "+metadata.getConfiguration().getConfigId(), e);
        }
    }

    public ConfigurationList listConfigurations(URL mavenRepository, String username, String password) throws IOException, FailedLoginException {
        String repository = mavenRepository.toString();
        if(!repository.endsWith("/")) {
            repository = repository+"/";
        }
        //todo: Try downloading a .gz first
        URL url = new URL(repository+"geronimo-plugins.xml");
        try {
            //todo: use a progress monitor
            InputStream in = openStream(null, url, new URL[0], username, password, null);
            return loadConfiguration(mavenRepository, in);
        } catch (MissingDependencyException e) {
            log.error("Cannot find plugin index at site "+url);
            return null;
        } catch (Exception e) {
            log.error("Unable to load repository configuration data", e);
            return null;
        }
    }

    private ConfigurationArchiveData loadConfigurationArchive(File file) throws IOException, ParserConfigurationException, SAXException {
        if(!file.canRead()) {
            log.error("Cannot read from downloaded CAR file "+file.getAbsolutePath());
            return null;
        }
        JarFile jar = new JarFile(file);
        Document doc;
        try {
            JarEntry entry = jar.getJarEntry("META-INF/geronimo-plugin.xml");
            if(entry == null) {
                log.error("Downloaded CAR file does not contain META-INF/geronimo-plugin.xml file");
                jar.close();
                return null;
            }
            InputStream in = jar.getInputStream(entry);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(in);
            in.close();
        } finally {
            jar.close();
        }
        return loadConfigurationMetadata(doc, file);
    }

    private ConfigurationArchiveData loadConfigurationMetadata(Document doc, File file) throws SAXException, MalformedURLException {
        Element root = doc.getDocumentElement();
        NodeList configs = root.getElementsByTagName("configuration");
        if(configs.getLength() != 1) {
            log.error("Configuration archive "+file.getAbsolutePath()+" does not have exactly one configuration in META-INF/geronimo-plugin.xml");
            return null;
        }
        ConfigurationMetadata data = processConfiguration((Element) configs.item(0));
        String repo = getChildText(root, "source-repository");
        URL repoURL;
        if(repo == null || repo.equals("")) {
            log.warn("Configuration archive "+file.getAbsolutePath()+" does not list a repository for downloading dependencies.");
            repoURL = null;
        } else {
            repoURL = new URL(repo);
        }
        String[] others = getChildrenText(root, "backup-repository");
        URL[] backups = new URL[others.length];
        for (int i = 0; i < backups.length; i++) {
            backups[i] = new URL(others[i]);
        }
        return new ConfigurationArchiveData(repoURL, backups, data);
    }

    private ConfigurationList loadConfiguration(URL repo, InputStream in) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);
        in.close();
        Element root = doc.getDocumentElement();
        NodeList configs = root.getElementsByTagName("configuration");
        List results = new ArrayList();
        for (int i = 0; i < configs.getLength(); i++) {
            Element config = (Element) configs.item(i);
            ConfigurationMetadata data = processConfiguration(config);
            results.add(data);
        }
        String[] backups = getChildrenText(root, "backup-repository");
        URL[] backupURLs = new URL[backups.length];
        for(int i = 0; i < backups.length; i++) {
            if(backups[i].endsWith("/")) {
                backupURLs[i] = new URL(backups[i]);
            } else {
                backupURLs[i] = new URL(backups[i]+"/");
            }
        }

        ConfigurationMetadata[] data = (ConfigurationMetadata[]) results.toArray(new ConfigurationMetadata[results.size()]);
        return new ConfigurationList(repo, backupURLs, data);
    }

    private ConfigurationMetadata processConfiguration(Element config) throws SAXException {
        String configId = getChildText(config, "config-id");
        NodeList licenseNodes = config.getElementsByTagName("license");
        ConfigurationMetadata.License[] licenses = new ConfigurationMetadata.License[licenseNodes.getLength()];
        for(int j=0; j<licenseNodes.getLength(); j++) {
            Element node = (Element) licenseNodes.item(j);
            String licenseName = getText(node);
            String openSource = node.getAttribute("osi-approved");
            if(licenseName == null || licenseName.equals("") || openSource == null || openSource.equals("")) {
                throw new SAXException("Invalid config file: license name and osi-approved flag required");
            }
            licenses[j] = new ConfigurationMetadata.License(licenseName, Boolean.valueOf(openSource).booleanValue());
        }
        boolean eligible = true;
        NodeList preNodes = config.getElementsByTagName("prerequisite");
        ConfigurationMetadata.Prerequisite[] prereqs = new ConfigurationMetadata.Prerequisite[preNodes.getLength()];
        for(int j=0; j<preNodes.getLength(); j++) {
            Element node = (Element) preNodes.item(j);
            String originalConfigId = getChildText(node, "id");
            if(originalConfigId == null) {
                throw new SAXException("Prerequisite requires <id>");
            }
            Artifact artifact = Artifact.create(originalConfigId.replaceAll("\\*", ""));
            boolean present = resolver.queryArtifacts(artifact).length > 0;
            prereqs[j] = new ConfigurationMetadata.Prerequisite(artifact, present,
                    getChildText(node, "resource-type"), getChildText(node, "description"));
            if(!present) {
                log.debug(configId+" is not eligible due to missing "+prereqs[j].getConfigId());
                eligible = false;
            }
        }
        String[] gerVersions = getChildrenText(config, "geronimo-version");
        if(gerVersions.length > 0) {
            String version = serverInfo.getVersion();
            boolean match = false;
            for (int j = 0; j < gerVersions.length; j++) {
                String gerVersion = gerVersions[j];
                if(gerVersion == null || gerVersion.equals("")) {
                    throw new SAXException("geronimo-version should not be empty!");
                }
                if(gerVersion.equals(version)) {
                    match = true;
                    break;
                }
            }
            if(!match) eligible = false;
        }
        String[] jvmVersions = getChildrenText(config, "jvm-version");
        if(jvmVersions.length > 0) {
            String version = System.getProperty("java.version");
            boolean match = false;
            for (int j = 0; j < jvmVersions.length; j++) {
                String jvmVersion = jvmVersions[j];
                if(jvmVersion == null || jvmVersion.equals("")) {
                    throw new SAXException("jvm-version should not be empty!");
                }
                if(version.startsWith(jvmVersion)) {
                    match = true;
                    break;
                }
            }
            if(!match) eligible = false;
        }
        Artifact artifact = null;
        boolean installed = false;
        if (configId != null) {
            artifact = Artifact.create(configId);
            installed = configManager.isLoaded(artifact);
        }
        log.trace("Checking "+configId+": installed="+installed+", eligible="+eligible);
        ConfigurationMetadata data = new ConfigurationMetadata(artifact, getChildText(config, "name"),
                getChildText(config, "description"), getChildText(config, "category"), installed, eligible);
        data.setGeronimoVersions(gerVersions);
        data.setJvmVersions(jvmVersions);
        data.setLicenses(licenses);
        data.setPrerequisites(prereqs);
        NodeList list = config.getElementsByTagName("dependency");
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
        data.setObsoletes(getChildrenText(config, "obsoletes"));
        return data;
    }

    private String getChildText(Element root, String property) {
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            Node check = children.item(i);
            if(check.getNodeType() == Node.ELEMENT_NODE && check.getNodeName().equals(property)) {
                return getText(check);
            }
        }
        return null;
    }

    private String getText(Node target) {
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

    private String[] getChildrenText(Element root, String property) {
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


    public DownloadResults install(ConfigurationList list, String username, String password) {
        DownloadResults results = new DownloadResults();
        install(list, username, password, results);
        return results;
    }

    public void install(ConfigurationList list, String username, String password, DownloadPoller poller) {
        try {
            // Step 1: validate everything
            for (int i = 0; i < list.getConfigurations().length; i++) {
                ConfigurationMetadata metadata = list.getConfigurations()[i];
                validateConfiguration(metadata);
            }

            // Step 2: everything is valid, do the installation
            for (int i = 0; i < list.getConfigurations().length; i++) {
                // 1. Identify the configuration
                ConfigurationMetadata metadata = list.getConfigurations()[i];
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
                if(metadata.getConfigId() != null) {
                    downloadArtifact(metadata.getConfigId(), list.getMainRepository(), list.getBackupRepositories(),
                            username, password, new ResultsFileWriteMonitor(poller), working);
                    poller.addInstalledConfigID(metadata.getConfigId());
                } else {
                    String[] deps = metadata.getDependencies();
                    for (int j = 0; j < deps.length; j++) {
                        String dep = deps[j];
                        Artifact entry = Artifact.create(dep);
                        if(configManager.isRunning(entry)) {
                            continue;
                        }
                        downloadArtifact(entry, list.getMainRepository(), list.getBackupRepositories(),
                                username, password, new ResultsFileWriteMonitor(poller), working);
                    }
                }
                // 4. Uninstall obsolete configurations
                for (int j = 0; j < obsoletes.size(); j++) {
                    Artifact artifact = (Artifact) obsoletes.get(j);
                    configManager.uninstallConfiguration(artifact);
                }
                // 5. Installation of this configuration finished successfully
                if(metadata.getConfigId() != null) {
                    poller.addInstalledConfigID(metadata.getConfigId());
                }
            }

            // Step 3: Start anything that's marked accordingly
            for (int i = 0; i < list.getConfigurations().length; i++) {
                ConfigurationMetadata metadata = list.getConfigurations()[i];
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
     * Installs from a pre-downloaded CAR file
     */
    public void install(File carFile, String username, String password, DownloadPoller poller) {
        try {
            // 1. Extract the configuration metadata
            ConfigurationArchiveData data = loadConfigurationArchive(carFile);
            if(data == null) {
                throw new IllegalArgumentException("Invalid Configuration Archive "+carFile.getAbsolutePath()+" see server log for details");
            }

            // 2. Validate that we can install this
            validateConfiguration(data.getConfiguration());

            // 3. Install the CAR into the repository (it shouldn't be re-downloaded)
            if(data.getConfiguration().getConfigId() != null) {
                ResultsFileWriteMonitor monitor = new ResultsFileWriteMonitor(poller);
                writeableRepo.copyToRepository(carFile, data.getConfiguration().getConfigId(), monitor);
            }

            // 4. Use the standard logic to remove obsoletes, install dependencies, etc.
            //    This will validate all over again (oh, well)
            install(new ConfigurationList(data.getRepository(), data.getBackups(), new ConfigurationMetadata[]{data.getConfiguration()}),
                    username, password, poller);
        } catch (Exception e) {
            poller.setFailure(e);
        } finally {
            poller.setFinished();
        }
    }

    private void validateConfiguration(ConfigurationMetadata metadata) throws MissingDependencyException {
        // 1. Check that it's not already running
        if(metadata.getConfigId() != null) { // that is, it's a real configuration not a plugin list
            if(configManager.isRunning(metadata.getConfigId())) {
                throw new IllegalArgumentException("Configuration "+metadata.getConfigId()+" is already running!");
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
        ConfigurationMetadata.Prerequisite[] prereqs = metadata.getPrerequisites();
        for (int i = 0; i < prereqs.length; i++) {
            ConfigurationMetadata.Prerequisite prereq = prereqs[i];
            if(resolver.queryArtifacts(prereq.getConfigId()).length == 0) {
                throw new MissingDependencyException("Required configuration '"+prereq.getConfigId()+"' is not installed.");
            }
        }
    }

    public Object startInstall(final ConfigurationList configsToInstall, final String username, final String password) {
        Object key = getNextKey();
        final DownloadResults results = new DownloadResults();
        Runnable work = new Runnable() {
            public void run() {
                install(configsToInstall, username, password, results);
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

    public DownloadResults checkOnInstall(Object key) {
        DownloadResults results = (DownloadResults) asyncKeys.get(key);
        if(results.isFinished()) {
            //todo: subject to a race condition
            // since this is not synchronized, it's possible that the download finishes after this
            // passes but before the client reads it, so the client thinks it's done but we never removed the key
            // fix if we care by copying the results before the if block and returning the copy?
            asyncKeys.remove(key);
        }
        return results;
    }

    /**
     * Download (if necessary) and install something, which may be a Configuration or may
     * be just a JAR.  For each artifact processed, all its dependencies will be
     * processed as well.
     *
     * @param configID  Identifies the artifact to install
     * @param repoURL   The main URL to contact the repository
     * @param backups   Any additional repositories to search
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
    private void downloadArtifact(Artifact configID, URL repoURL, URL[] backups, String username, String password, ResultsFileWriteMonitor monitor, Set soFar) throws IOException, FailedLoginException, MissingDependencyException {
        if(soFar.contains(configID)) {
            return; // Avoid enless work due to circular dependencies
        } else {
            soFar.add(configID);
        }
        //todo: check all repositories?
        if(!writeableRepo.contains(configID)) {
            InputStream in = openStream(configID, repoURL, backups, username, password, monitor);
            try {
                writeableRepo.copyToRepository(in, configID, monitor); //todo: download SNAPSHOTS if previously available?
                monitor.getResults().addDependencyInstalled(configID);
            } finally {
                in.close();
            }
        } else {
            monitor.getResults().addDependencyPresent(configID);
        }

        try {
            ConfigurationData data = null;
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
                downloadArtifact(artifact, repoURL, backups, username, password, monitor, soFar);
            }
        } catch (NoSuchConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore does not see it: "+e.getMessage());
        } catch (InvalidConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore cannot load it: "+e.getMessage());
        }
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

    private static URL getURL(Artifact configId, URL repository) throws MalformedURLException {
        return new URL(repository, configId.getGroupId().replace('.','/')+"/"+configId.getArtifactId()+"/"+configId.getVersion()+"/"+configId.getArtifactId()+"-"+configId.getVersion()+"."+configId.getType());
    }

    private static InputStream openStream(Artifact artifact, URL repo, URL[] backups, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException, MissingDependencyException {
        if(monitor != null) {
            monitor.getResults().setCurrentFilePercent(-1);
            monitor.getResults().setCurrentMessage("Attempting to download "+artifact);
            monitor.setTotalBytes(-1); // In case the server doesn't say
        }
        InputStream in;
        LinkedList list = new LinkedList();
        list.add(repo);
        list.addAll(Arrays.asList(backups));
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
            URLConnection con = url.openConnection();
            if(con instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.connect();
                if(http.getResponseCode() == 401) { // need to authenticate
                    if(username == null || username.equals("")) {
                        throw new FailedLoginException("Server returned 401 "+http.getResponseMessage());
                    }
                    http = (HttpURLConnection) url.openConnection();
                    http.setRequestProperty("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                    http.connect();
                    if(http.getResponseCode() == 401) {
                        throw new FailedLoginException("Server returned 401 "+http.getResponseMessage());
                    } else if(http.getResponseCode() == 404) {
                        continue; // Not found at this repository
                    }
                    if(monitor != null && http.getContentLength() > 0) {
                        monitor.setTotalBytes(http.getContentLength());
                    }
                    in = http.getInputStream();
                } else if(http.getResponseCode() == 404) {
                    continue; // Not found at this repository
                } else {
                    if(monitor != null && http.getContentLength() > 0) {
                        monitor.setTotalBytes(http.getContentLength());
                    }
                    in = http.getInputStream();
                }
            } else {
                if(username != null && !username.equals("")) {
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                    try {
                        con.connect();
                        if(monitor != null && con.getContentLength() > 0) {
                            monitor.setTotalBytes(con.getContentLength());
                        }
                        in = con.getInputStream();
                    } catch (FileNotFoundException e) {
                        continue;
                    }
                } else {
                    try {
                        con.connect();
                        if(monitor != null && con.getContentLength() > 0) {
                            monitor.setTotalBytes(con.getContentLength());
                        }
                        in = con.getInputStream();
                    } catch (FileNotFoundException e) {
                        continue;
                    }
                }
            }
            if(in != null) {
                return in;
            }
        }
    }

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

        public void writeStarted(String fileDescription) {
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

    private void readNameAndID(File xml, Map plugins) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            PluginNameIDHandler handler = new PluginNameIDHandler();
            parser.parse(xml, handler);
            if(handler.isComplete()) {
                plugins.put(handler.getName(), handler.getID());
            }
        } catch (Exception e) {
            log.warn("Invalid XML at "+xml.getAbsolutePath(), e);
        }
    }

    private static class PluginNameIDHandler extends DefaultHandler {
        private String id = "";
        private String name = "";
        private String element = null;

        public void characters(char ch[], int start, int length) throws SAXException {
            if(element != null) {
                if(element.equals("config-id")) {
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
            if(localName.equals("config-id") || localName.equals("name")) {
                element = localName;
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

    private ConfigurationMetadata createDefaultMetadata(ConfigurationData data) {
        ConfigurationMetadata meta = new ConfigurationMetadata(data.getId(), data.getId().toString(),
                "Please provide a description", "Unknown", true, false);
        meta.setGeronimoVersions(new String[]{serverInfo.getVersion()});
        meta.setJvmVersions(new String[0]);
        meta.setLicenses(new ConfigurationMetadata.License[0]);
        meta.setObsoletes(new String[0]);
        List deps = new ArrayList();
        ConfigurationMetadata.Prerequisite prereq = null;
        List real = data.getEnvironment().getDependencies();
        for (int i = 0; i < real.size(); i++) {
            Dependency dep = (Dependency) real.get(i);
            if(dep.getArtifact().getGroupId().equals("geronimo")) {
                if(dep.getArtifact().getArtifactId().equals("jetty")) {
                    if(prereq == null) {
                        prereq = new ConfigurationMetadata.Prerequisite(dep.getArtifact(), true, "Web Container", "This plugin works with the Geronimo/Jetty distribution.  It is not intended to run in the Geronimo/Tomcat distribution.  There is a separate version of this plugin that works with Tomcat.");
                    }
                    continue;
                } else if(dep.getArtifact().getArtifactId().equals("tomcay")) {
                    if(prereq == null) {
                        prereq = new ConfigurationMetadata.Prerequisite(dep.getArtifact(), true, "Web Container", "This plugin works with the Geronimo/Tomcat distribution.  It is not intended to run in the Geronimo/Jetty distribution.  There is a separate version of this plugin that works with Jetty.");
                    }
                    continue;
                }
            }
            deps.add(dep.getArtifact().toString());
        }
        meta.setDependencies((String[]) deps.toArray(new String[deps.size()]));
        meta.setPrerequisites(prereq == null ? new ConfigurationMetadata.Prerequisite[0] : new ConfigurationMetadata.Prerequisite[]{prereq});
        return meta;
    }

    private static Document writeConfigurationMetadata(ConfigurationArchiveData metadata) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("geronimo-plugin");
        doc.appendChild(root);
        Element config = doc.createElement("configuration");
        root.appendChild(config);

        ConfigurationMetadata data = metadata.getConfiguration();
        addTextChild(doc, config, "name", data.getName());
        addTextChild(doc, config, "config-id", data.getConfigId().toString());
        addTextChild(doc, config, "category", data.getCategory());
        addTextChild(doc, config, "description", data.getDescription());
        for (int i = 0; i < data.getLicenses().length; i++) {
            ConfigurationMetadata.License license = data.getLicenses()[i];
            Element lic = doc.createElement("license");
            lic.appendChild(doc.createTextNode(license.getName()));
            lic.setAttribute("osi-approved", Boolean.toString(license.isOsiApproved()));
            config.appendChild(lic);
        }
        for (int i = 0; i < data.getGeronimoVersions().length; i++) {
            addTextChild(doc, config, "geronimo-version", data.getGeronimoVersions()[i]);
        }
        for (int i = 0; i < data.getJvmVersions().length; i++) {
            addTextChild(doc, config, "jvm-version", data.getJvmVersions()[i]);
        }
        for (int i = 0; i < data.getPrerequisites().length; i++) {
            ConfigurationMetadata.Prerequisite prereq = data.getPrerequisites()[i];
            Element pre = doc.createElement("prerequisite");
            addTextChild(doc, pre, "id", prereq.getConfigId().toString());
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

        Element repo = doc.createElement("source-repository");
        repo.appendChild(doc.createTextNode(metadata.getRepository().toString()));
        root.appendChild(repo);
        for (int i = 0; i < metadata.getBackups().length; i++) {
            URL url = metadata.getBackups()[i];
            Element backup = doc.createElement("backup-repository");
            backup.appendChild(doc.createTextNode(url.toString()));
            root.appendChild(backup);
        }
        return doc;
    }

    private static void addTextChild(Document doc, Element parent, String name, String text) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(text));
        parent.appendChild(child);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigInstallerGBean.class);
        infoFactory.addReference("ConfigManager", ConfigurationManager.class, "ConfigurationManager");
        infoFactory.addReference("Repository", WritableListableRepository.class, "Repository");
        infoFactory.addReference("ConfigStore", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("ThreadPool", ThreadPool.class, "GBean");
        infoFactory.addInterface(ConfigurationInstaller.class);

        infoFactory.setConstructor(new String[]{"ConfigManager", "Repository", "ConfigStore", "ServerInfo","ThreadPool"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
