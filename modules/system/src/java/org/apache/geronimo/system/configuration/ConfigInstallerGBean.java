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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.util.encoders.Base64;
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

    public ConfigInstallerGBean(ConfigurationManager configManager, WritableListableRepository repository, ConfigurationStore configStore) {
        this.configManager = configManager;
        this.writeableRepo = repository;
        this.configStore = configStore;
    }

    public ConfigurationMetadata[] listConfigurations(URL mavenRepository, String username, String password) throws IOException {
        String repository = mavenRepository.toString();
        if(!repository.endsWith("/")) {
            repository = repository+"/";
        }
        URL url = new URL(repository+"geronimo-configs.xml");
        InputStream in = openStream(url, username, password);
        try {
            return loadConfiguration(in);
        } catch (Exception e) {
            log.error("Unable to load repository configuration data", e);
            return new ConfigurationMetadata[0];
        }
    }

    private ConfigurationMetadata[] loadConfiguration(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        ConfigurationStore[] stores = configManager.getStores();
        Set set = new HashSet();
        for (int i = 0; i < stores.length; i++) {
            ConfigurationStore store = stores[i];
            List list = store.listConfigurations();
            for (int j = 0; j < list.size(); j++) {
                ConfigurationInfo info = (ConfigurationInfo) list.get(i);
                set.add(info.getConfigID().toString());
            }
        }
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
            boolean eligible = true;
            String[] prereqs = getChildrenText(config, "prerequisite");
            for (int j = 0; j < prereqs.length; j++) {
                boolean found = false;
                for (Iterator it = set.iterator(); it.hasNext();) {
                    String id = (String) it.next();
                    if(id.startsWith(prereqs[j])) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    eligible = false;
                    break;
                }
            }
            Artifact artifact = Artifact.create(configId);
            ConfigurationMetadata data = new ConfigurationMetadata(artifact, getChildText(config, "name"), getChildText(config, "category"), configManager.isLoaded(artifact), eligible);
            data.setGeronimoVersions(getChildrenText(config, "geronimo-version"));
            data.setPrerequisites(prereqs);
            results.add(data);
        }
        return (ConfigurationMetadata[]) results.toArray(new ConfigurationMetadata[results.size()]);
    }

    private String getChildText(Element root, String property) {
        NodeList children = root.getChildNodes();
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
                return buf == null ? null : buf.toString();
            }
        }
        return null;
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

    public ConfigurationMetadata loadDependencies(URL mavenRepository, String username, String password, ConfigurationMetadata source) throws IOException {
        String configId = source.getConfigId().toString();
        String url = getURL(configId, mavenRepository.toString());

        Artifact artifact = Artifact.create(configId);
        InputStream in = openStream(new URL(url), username, password);
        try { //todo: use a file status monitor
            writeableRepo.copyToRepository(in, artifact, null); //todo: download only SNAPSHOTS if previously available?
        } finally {
            in.close();
        }
        try {
            ConfigurationData data = configStore.loadConfiguration(artifact);
            source.setDependencies(getDependencies(data));
            return source;
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

    public DownloadResults install(URL mavenRepository, String username, String password, Artifact configId) throws IOException {
        DownloadResults results = new DownloadResults();
        downloadConfigurationDeps(configId,mavenRepository.toString(),username,password,results);
        return results;
    }

    private String getURL(String configId, String baseRepositoryURL) {
        if(!baseRepositoryURL.endsWith("/")) {
            baseRepositoryURL += "/";
        }
        String[] parts = configId.split("/");
        return baseRepositoryURL+parts[0]+"/"+parts[3]+"s/"+parts[1]+"-"+parts[2]+"."+parts[3];
    }

    private InputStream openStream(URL url, String username, String password) throws IOException {
        InputStream in;
        if(username != null) { //todo: try connecting first and only use authentication if challenged
            URLConnection con = url.openConnection();
            con.setRequestProperty("Authorization", "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
            in = con.getInputStream();
        } else {
            in = url.openStream();
        }
        return in;
    }

    private void downloadConfigurationDeps(Artifact configID, String repoURL, String username, String password, DownloadResults results) throws IOException {
        if(!repoURL.endsWith("/")) {
            repoURL += "/";
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
                String url = getURL(dep.toString(), repoURL);
                //todo: use a file status monitor
                writeableRepo.copyToRepository(openStream(new URL(url), username, password), artifact, null);
                results.addDependencyInstalled(artifact);
                downloadConfigurationDeps(artifact, repoURL, username, password, results);
            }
        } catch (NoSuchConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore does not see it: "+e.getMessage());
        } catch (InvalidConfigException e) {
            throw new IllegalStateException("Installed configuration into repository but ConfigStore cannot load it: "+e.getMessage());
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigInstallerGBean.class);
        infoFactory.addReference("ConfigManager", ConfigurationManager.class, "ConfigurationManager");
        infoFactory.addReference("Repository", WritableListableRepository.class, "Repository");
        infoFactory.addReference("ConfigStore", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addInterface(ConfigurationInstaller.class);

        infoFactory.setConstructor(new String[]{"ConfigManager", "Repository", "ConfigStore"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
