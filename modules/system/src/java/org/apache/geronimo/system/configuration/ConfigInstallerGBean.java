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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A GBean that knows how to download configurations from a Maven repository.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigInstallerGBean implements ConfigurationInstaller {
    private final static Log log = LogFactory.getLog(ConfigInstallerGBean.class);
    private Collection configStores;
    private WriteableRepository writeableRepo;
    private ConfigurationStore configStore;
    private Map configIdToFile = new HashMap();

    public ConfigInstallerGBean(Collection configStores, WriteableRepository writeableRepo, ConfigurationStore configStore) {
        this.configStores = configStores;
        this.writeableRepo = writeableRepo;
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

    private ConfigurationMetadata[] loadConfiguration(InputStream in) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        Set set = new HashSet();
        for (Iterator it = configStores.iterator(); it.hasNext();) {
            ConfigurationStore store = (ConfigurationStore) it.next();
            List list = store.listConfigurations();
            for (int i = 0; i < list.size(); i++) {
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
            ConfigurationMetadata data = new ConfigurationMetadata(new URI(configId), getChildText(config, "name"), getChildText(config, "category"), set.contains(configId), eligible);
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
        String conf = source.getConfigId().toString();
        File file = File.createTempFile("geronimo-download", "." + conf.substring(conf.lastIndexOf("/")+1));
        file.deleteOnExit();
        String url = getURL(conf, mavenRepository.toString());
        downloadFile(url, username, password, file); //todo: download only SNAPSHOTS if previously available?
        configIdToFile.put(source.getConfigId(), file);
        ZipFile zip = new ZipFile(file);
        try {
            ZipEntry entry = zip.getEntry("META-INF/config.ser");
            ObjectInputStream serIn = new ObjectInputStream(zip.getInputStream(entry));
            GBeanData config = new GBeanData();
            config.readExternal(serIn);
            URI[] parentIds = (URI[]) config.getAttribute("parentId");
            List dependencies = (List) config.getAttribute("dependencies");
            source.setDependencies((URI[]) dependencies.toArray(new URI[dependencies.size()]));
            source.setParents(parentIds);
            return source;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize GBeanData (can't load class "+e.getMessage()+")");
        } finally {
            zip.close();
        }
    }

    public DownloadResults install(URL mavenRepository, String username, String password, URI configId) throws IOException {
        Set set = new HashSet();
        for (Iterator it = configStores.iterator(); it.hasNext();) {
            ConfigurationStore store = (ConfigurationStore) it.next();
            List list = store.listConfigurations();
            for (int i = 0; i < list.size(); i++) {
                ConfigurationInfo info = (ConfigurationInfo) list.get(i);
                set.add(info.getConfigID());
            }
        }
        DownloadResults results = new DownloadResults();
        processConfiguration(configId,writeableRepo,mavenRepository.toString(),username,password,set,results);
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

    private void downloadFile(String urlSource, String username, String password, File target) throws IOException {
        log.info("Downloading "+urlSource+" to "+target.getAbsolutePath());
        byte[] buf = new byte[10240];
        URL url = new URL(urlSource);
        InputStream in = openStream(url, username, password);
        FileOutputStream out = new FileOutputStream(target);
        int count;
        while((count = in.read(buf)) > -1) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
    }

    private void processConfiguration(URI configId, WriteableRepository repo, String repoURL, String username, String password, Set configurations, DownloadResults results) throws IOException {
        if(!repoURL.endsWith("/")) {
            repoURL += "/";
        }
        // Make sure we have a local copy of the CAR
        String id = configId.toString();
        File file;
        if(configIdToFile.containsKey(configId)) {
            file = (File) configIdToFile.get(configId);
        } else {
            file = File.createTempFile("geronimo-download", "." + id.substring(id.lastIndexOf("/")+1));
            file.deleteOnExit();
        }
        String configUrl = getURL(id, repoURL);
        if(!file.exists() || file.length() == 0) {
            downloadFile(configUrl, username, password, file);
        }
        results.addConfigurationInstalled(configId);

        // Process the contents of the CAR
        ZipFile zip = new ZipFile(file);
        try {
            ZipEntry entry = zip.getEntry("META-INF/config.ser");
            ObjectInputStream serIn = new ObjectInputStream(zip.getInputStream(entry));
            GBeanData config = new GBeanData();
            config.readExternal(serIn);
            URI[] parentIds = (URI[]) config.getAttribute("parentId");
            List dependencies = (List) config.getAttribute("dependencies");
            // Download the dependencies
            for (int i = 0; i < dependencies.size(); i++) {
                URI dep = (URI) dependencies.get(i);
                if(repo.hasURI(dep)) {
                    results.addDependencyPresent(dep);
                    continue;
                }
                String url = getURL(dep.toString(), repoURL);
                log.info("Downloading "+url+" to local repository");
                repo.copyToRepository(openStream(new URL(url), username, password), dep, null);
                results.addDependencyInstalled(dep);
            }
            // Download the parents
            if(parentIds != null) {
                for (int i = 0; i < parentIds.length; i++) {
                    URI uri = parentIds[i];
                    if(configurations.contains(uri)) {
                        results.addConfigurationPresent(uri);
                        continue;
                    }
                    processConfiguration(uri, repo, repoURL, username, password, configurations, results);
                }
            }
            // Install the configuration
            configStore.install(file.toURL());
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize GBeanData: "+e.getMessage());
        } catch (InvalidConfigException e) {
            throw new IOException("Unable to install configuration: "+e.getMessage());
        } finally {
            zip.close();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigInstallerGBean.class);
        infoFactory.addReference("DependencyInstallTarget", WriteableRepository.class, "GBean");
        infoFactory.addReference("ConfigurationInstallTarget", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addReference("AllConfigStores", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addInterface(ConfigurationInstaller.class);

        infoFactory.setConstructor(new String[]{"AllConfigStores", "DependencyInstallTarget", "ConfigurationInstallTarget"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
