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

import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import javax.security.auth.login.FailedLoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.util.encoders.Base64;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    private ConfigurationMetadata processConfiguration(Element config) {
        String configId = getChildText(config, "config-id");
        NodeList licenseNodes = config.getElementsByTagName("license");
        ConfigurationMetadata.License[] licenses = new ConfigurationMetadata.License[licenseNodes.getLength()];
        for(int j=0; j<licenseNodes.getLength(); j++) {
            Element node = (Element) licenseNodes.item(j);
            licenses[j] = new ConfigurationMetadata.License(getText(node), Boolean.valueOf(node.getAttribute("osi-approved")).booleanValue());
        }
        boolean eligible = true;
        NodeList preNodes = config.getElementsByTagName("prerequisite");
        ConfigurationMetadata.Prerequisite[] prereqs = new ConfigurationMetadata.Prerequisite[preNodes.getLength()];
        for(int j=0; j<preNodes.getLength(); j++) {
            Element node = (Element) preNodes.item(j);
            String originalConfigId = getChildText(node, "id");
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
                if(version.startsWith(jvmVersion)) {
                    match = true;
                    break;
                }
            }
            if(!match) eligible = false;
        }
        Artifact artifact = Artifact.create(configId);
        boolean installed = configManager.isLoaded(artifact);
        log.trace("Checking "+configId+": installed="+installed+", eligible="+eligible);
        ConfigurationMetadata data = new ConfigurationMetadata(artifact, getChildText(config, "name"),
                getChildText(config, "description"), getChildText(config, "category"), installed, eligible);
        data.setGeronimoVersions(gerVersions);
        data.setJvmVersions(jvmVersions);
        data.setLicenses(licenses);
        data.setPrerequisites(prereqs);
        data.setDependencies(getChildrenText(config, "dependency"));
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
            // For each configuration in the list to install...
            for (int i = 0; i < list.getConfigurations().length; i++) {
                // 1. Identify the configuration
                ConfigurationMetadata metadata = list.getConfigurations()[i];
                // 2. Validate that we can install it
                validateConfiguration(metadata);
                // 3. Download the artifact if necessary, and its dependencies
                downloadArtifact(metadata.getConfigId(), list.getMainRepository(), list.getBackupRepositories(),
                        username, password, new ResultsFileWriteMonitor(poller));
                // 4. Installation of this configuration finished successfully
                poller.addInstalledConfigID(metadata.getConfigId());
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
            
            // 3. Install the CAR into the repository
            ResultsFileWriteMonitor monitor = new ResultsFileWriteMonitor(poller);
            writeableRepo.copyToRepository(carFile, data.getConfiguration().getConfigId(), monitor);

            // 4. Download all the dependencies
            downloadArtifact(data.getConfiguration().getConfigId(), data.getRepository(), data.getBackups(),
                    username, password, monitor);

            // 5. Installation of the main configuration finished successfully
            poller.addInstalledConfigID(data.getConfiguration().getConfigId());
        } catch (Exception e) {
            poller.setFailure(e);
        } finally {
            poller.setFinished();
        }
    }

    private void validateConfiguration(ConfigurationMetadata metadata) throws MissingDependencyException {
        // 1. Check that it's not already running
        if(configManager.isRunning(metadata.getConfigId())) {
            throw new IllegalArgumentException("Configuration "+metadata.getConfigId()+" is already running!");
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
    private void downloadArtifact(Artifact configID, URL repoURL, URL[] backups, String username, String password, ResultsFileWriteMonitor monitor) throws IOException, FailedLoginException, MissingDependencyException {
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
                downloadArtifact(artifact, repoURL, backups, username, password, monitor);
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
        List dependencies = data.getEnvironment().getDependencies();
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
