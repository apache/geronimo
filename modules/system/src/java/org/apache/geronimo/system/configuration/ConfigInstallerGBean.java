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
import org.apache.geronimo.util.encoders.Base64;
import org.apache.geronimo.system.serverinfo.ServerInfo;
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
    private ConfigurationManager configManager;
    private WritableListableRepository writeableRepo;
    private ConfigurationStore configStore;
    private ArtifactResolver resolver;
    private ServerInfo serverInfo;

    public ConfigInstallerGBean(ConfigurationManager configManager, WritableListableRepository repository, ConfigurationStore configStore, ServerInfo serverInfo) {
        this.configManager = configManager;
        this.writeableRepo = repository;
        this.configStore = configStore;
        this.serverInfo = serverInfo;
        resolver = new DefaultArtifactResolver(null, writeableRepo);
    }

    public ConfigurationList listConfigurations(URL mavenRepository, String username, String password) throws IOException, FailedLoginException {
        String repository = mavenRepository.toString();
        if(!repository.endsWith("/")) {
            repository = repository+"/";
        }
        //todo: Try downloading a .gz first
        URL url = new URL(repository+"geronimo-plugins.xml");
        try {
            InputStream in = openStream(null, url, new URL[0], username, password);
            return loadConfiguration(mavenRepository, in);
        } catch (MissingDependencyException e) {
            log.error("Cannot find plugin index at site "+url);
            return null;
        } catch (Exception e) {
            log.error("Unable to load repository configuration data", e);
            return null;
        }
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
                    log.info(configId+" is not eligible due to missing "+prereqs[j].getConfigId());
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
            log.info("Checking "+configId+": installed="+installed+", eligible="+eligible);
            ConfigurationMetadata data = new ConfigurationMetadata(artifact, getChildText(config, "name"), getChildText(config, "category"), installed, eligible);
            data.setGeronimoVersions(gerVersions);
            data.setJvmVersions(jvmVersions);
            data.setLicenses(licenses);
            data.setPrerequisites(prereqs);
            data.setDependencies(getChildrenText(config, "dependency"));
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


    public DownloadResults install(ConfigurationList list, String username, String password) throws IOException, FailedLoginException, MissingDependencyException {
        DownloadResults results = new DownloadResults();
        for (int i = 0; i < list.getConfigurations().length; i++) {
            ConfigurationMetadata metadata = list.getConfigurations()[i];
            downloadConfiguration(metadata.getConfigId(),list.getMainRepository(),list.getBackupRepositories(),username,password,results);
        }
        return results;
    }

    private void downloadConfiguration(Artifact configID, URL repoURL, URL[] backups, String username, String password, DownloadResults results) throws IOException, FailedLoginException, MissingDependencyException {
        if(!configStore.containsConfiguration(configID)) {
            InputStream in = openStream(configID, repoURL, backups, username, password);
            try { //todo: use a file status monitor
                writeableRepo.copyToRepository(in, configID, null); //todo: download only SNAPSHOTS if previously available?
            } finally {
                in.close();
            }
        }

        try {
            ConfigurationData data = null;
            if(configStore.containsConfiguration(configID)) {
                data = configStore.loadConfiguration(configID);
            }
            if(data == null) {
                throw new IllegalStateException("No configuration store for repository "+writeableRepo);
            }
            Dependency[] dependencies = getDependencies(data);
            // Download the dependencies
            for (int i = 0; i < dependencies.length; i++) {
                Dependency dep = dependencies[i];
                Artifact artifact = dep.getArtifact();
                //todo: check all repositories?
                if(writeableRepo.contains(artifact)) {
                    results.addDependencyPresent(artifact);
                    continue;
                }
                //todo: use a file status monitor
                writeableRepo.copyToRepository(openStream(dep.getArtifact(), repoURL, backups, username, password), artifact, null);
                results.addDependencyInstalled(artifact);
                downloadConfiguration(artifact, repoURL, backups, username, password, results);
            }
        } catch (NoSuchConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore does not see it: "+e.getMessage());
        } catch (InvalidConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore cannot load it: "+e.getMessage());
        }
    }

    private static Dependency[] getDependencies(ConfigurationData data) {
        List dependencies = data.getEnvironment().getDependencies();
        Collection children = data.getChildConfigurations().values();
        for (Iterator it = children.iterator(); it.hasNext();) {
            ConfigurationData child = (ConfigurationData) it.next();
            dependencies.addAll(child.getEnvironment().getDependencies());
        }
        return (Dependency[]) children.toArray(new Dependency[children.size()]);
    }

    private static URL getURL(Artifact configId, URL repository) throws MalformedURLException {
        return new URL(repository, configId.getGroupId().replace('.','/')+"/"+configId.getArtifactId()+"/"+configId.getVersion()+"/"+configId.getArtifactId()+"-"+configId.getVersion()+"."+configId.getType());
    }

    private static InputStream openStream(Artifact artifact, URL repo, URL[] backups, String username, String password) throws IOException, FailedLoginException, MissingDependencyException {
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
                    in = http.getInputStream();
                } else if(http.getResponseCode() == 404) {
                    continue; // Not found at this repository
                } else {
                    in = http.getInputStream();
                }
            } else {
                if(username != null && !username.equals("")) {
                    con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                    try {
                        in = con.getInputStream();
                    } catch (FileNotFoundException e) {
                        continue;
                    }
                } else {
                    try {
                        in = url.openStream();
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigInstallerGBean.class);
        infoFactory.addReference("ConfigManager", ConfigurationManager.class, "ConfigurationManager");
        infoFactory.addReference("Repository", WritableListableRepository.class, "Repository");
        infoFactory.addReference("ConfigStore", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addInterface(ConfigurationInstaller.class);

        infoFactory.setConstructor(new String[]{"ConfigManager", "Repository", "ConfigStore", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
