/**
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.system.util;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Properties;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.geronimo.kernel.config.IOUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.configuration.ConfigurationStoreUtil;
import org.apache.geronimo.system.repository.Maven1Repository;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.repository.CopyArtifactTypeHandler;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * A utility that exports a repository of plugins.
 *
 * @version $Rev: 355877 $ $Date: 2005-12-10 21:48:27 -0500 (Sat, 10 Dec 2005) $
 */
public class PluginRepositoryExporter {
    private final static String NAMESPACE = "http://geronimo.apache.org/xml/ns/plugins-1.1";
    private Maven1Repository sourceRepo;
    private Maven2Repository destRepo;
    private Map targetVersions;
    private PluginInstallerGBean installer;
    private File pluginList;
    private File schema;

    public PluginRepositoryExporter(String inPath, String outPath, String schema) throws IOException {
        this.schema = new File(schema);
        if(!this.schema.isFile() || !this.schema.canRead()) {
            throw new IllegalArgumentException("Bad schema file "+this.schema.getAbsolutePath());
        }
        File inFile = new File(inPath);
        if(!inFile.isDirectory() || !inFile.canRead()) {
            throw new IllegalArgumentException("Bad source repo directory "+inFile.getAbsolutePath());
        }
        File outFile = new File(outPath);
        if(outFile.exists()) {
            if(!outFile.isDirectory() || !outFile.canRead()) {
                throw new IllegalArgumentException("Bad target repo directory "+outFile.getAbsolutePath());
            }
        } else {
            if(!outFile.mkdirs()) {
                throw new IllegalArgumentException("Can't create target repo directory "+outFile.getAbsolutePath());
            }
        }
        pluginList = new File(outFile, "geronimo-plugins.xml");
        if(!pluginList.exists()) {
            if(!pluginList.createNewFile()) {
                throw new IllegalArgumentException("Can't create target plugin list file "+pluginList.getAbsolutePath());
            }
        }
        sourceRepo = new Maven1Repository(inFile);
        destRepo = new Maven2Repository(outFile);
        destRepo.setTypeHandler("car", new CopyArtifactTypeHandler());
        Properties props = new Properties();
        InputStream is = PluginRepositoryExporter.class.getResourceAsStream("/META-INF/product-versions.properties");
        if(is == null) {
            throw new IOException("Unable to locate /META-INF/product-versions.properties");
        }        
        try {
            props.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
        
        targetVersions = new HashMap();
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String product = (String) it.next();
            String version = props.getProperty(product);
            targetVersions.put(product, new Version(version));
        }
        RepositoryConfigurationStore store = new RepositoryConfigurationStore(destRepo);
        ServerInfo info = new ServerInfo() {
            public String getBaseDirectory() {
                return null;
            }

            public String getBuildDate() {
                return null;
            }

            public String getBuildTime() {
                return null;
            }

            public String getCopyright() {
                return null;
            }

            public String getCurrentBaseDirectory() {
                return null;
            }

            public String getVersion() {
                return null;
            }

            public File resolve(final String filename) {
                return null;
            }

            public URI resolve(final URI uri) {
                return null;
            }

            public String resolvePath(final String filename) {
                return null;
            }
            
            public File resolveServer(final String filename) {
                return null;
            }
            
            public URI resolveServer(final URI uri) {
                return null;
            }
            
            public String resolveServerPath(final String filename) {
                return null;
            }
        };
        installer = new PluginInstallerGBean(null, destRepo, store, info, null);
    }

    public void execute() throws IOException {
        SortedSet list = sourceRepo.list();
        try {
            for (Iterator it = list.iterator(); it.hasNext();) {
                Artifact artifact = (Artifact) it.next();
                if(((artifact.getGroupId().equals("geronimo")) ||
                        artifact.getGroupId().equals("activemq") ||
                        artifact.getGroupId().equals("openejb") ||
                        artifact.getGroupId().equals("tranql")
                        )
                        && artifact.getVersion().equals(targetVersions.get(artifact.getGroupId()))
                        && !artifact.getType().equals("pom") && !artifact.getType().equals("distribution") && !artifact.getType().equals("plugin") && !artifact.getType().equals("javadoc.jar")) {
                    System.out.println("Copying "+artifact);
                    if(destRepo.contains(artifact)) {
                        File location = destRepo.getLocation(artifact);
                        IOUtil.recursiveDelete(location);
                    }
                    destRepo.copyToRepository(sourceRepo.getLocation(artifact), artifact, null);
                    File dest = destRepo.getLocation(artifact);
                    File versionDir = dest.getParentFile();
                    File artifactDir = versionDir.getParentFile();
                    if(!artifactDir.isDirectory() || !artifactDir.canRead()) {
                        throw new IllegalStateException("Failed to located group/artifact dir for "+artifact+" (got "+artifactDir.getAbsolutePath()+")");
                    }
                    updateMavenMetadata(artifactDir, artifact);
                }
            }
            Map plugins = installer.getInstalledPlugins();
            Document doc = generateConfigFile(installer, plugins.values());
            TransformerFactory xfactory = TransformerFactory.newInstance();
            Transformer xform = xfactory.newTransformer();
            xform.setOutputProperty(OutputKeys.INDENT, "yes");
            xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            System.out.println("Writing geronimo-plugins.xml file...");
            FileWriter out = new FileWriter(pluginList);
            xform.transform(new DOMSource(doc), new StreamResult(out));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to format XML output: "+e.getMessage());
        }

    }

    private void updateMavenMetadata(File dir, Artifact artifact) throws TransformerException, IOException, SAXException, ParserConfigurationException {
        File mavenFile = new File(dir, "maven-metadata.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc;
        if(mavenFile.exists()) {
            doc = builder.parse(mavenFile);
        } else {
            doc = builder.newDocument();
            createMavenMetadata(doc, artifact);
        }
        NodeList versions = doc.getElementsByTagName("versions");
        Element versionsElement = (Element) versions.item(0);
        versions = versionsElement.getElementsByTagName("version");
        boolean found = false;
        for(int i=0; i<versions.getLength(); i++) {
            String version = getText(versions.item(i)).trim();
            if(version.equals(artifact.getVersion().toString())) {
                found = true;
                break;
            }
            Version test = new Version(version);
            if(test.compareTo(artifact.getVersion()) > 0) {
                Element newVersion = doc.createElement("version");
                newVersion.appendChild(doc.createTextNode(artifact.getVersion().toString()));
                versionsElement.insertBefore(newVersion, versions.item(i));
                found = true;
            }
        }
        if(!found) {
            Element newVersion = doc.createElement("version");
            newVersion.appendChild(doc.createTextNode(artifact.getVersion().toString()));
            versionsElement.appendChild(newVersion);
        }
        TransformerFactory xfactory = TransformerFactory.newInstance();
        Transformer xform = xfactory.newTransformer();
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.transform(new DOMSource(doc), new StreamResult(mavenFile));
    }

    private void createMavenMetadata(Document doc, Artifact artifact) {
        Element root = doc.createElement("metadata");
        doc.appendChild(root);
        createText(doc, root, "groupId", artifact.getGroupId());
        createText(doc, root, "artifactId", artifact.getArtifactId());
        createText(doc, root, "version", artifact.getVersion().toString());
        Element versioning = doc.createElement("versioning");
        root.appendChild(versioning);
        Element versions = doc.createElement("versions");
        versioning.appendChild(versions);
        createText(doc, versions, "version", artifact.getVersion().toString());
    }


    private Document generateConfigFile(PluginInstaller installer, Collection plugins) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                             "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                             schema.getName());
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                System.out.println("RESOLVING PUB "+publicId+" SYS "+systemId);
                return null;
            }
        });
        Document doc = builder.newDocument();
        Element root = doc.createElementNS(NAMESPACE, "geronimo-plugin-list");
        root.setAttribute("xmlns", NAMESPACE);
        doc.appendChild(root);
        for (Iterator it = plugins.iterator(); it.hasNext();) {
            Artifact plugin = (Artifact) it.next();
            System.out.println("Including plugin data for "+plugin);
            PluginMetadata data = installer.getPluginMetadata(plugin);
            if(data == null) {
                throw new IllegalArgumentException("Invalid plugin file; Log4J output for a specific error.");
            }
            File file = destRepo.getLocation(plugin);
            Element config = doc.createElement("plugin");
            root.appendChild(config);
            createText(doc, config, "name", data.getName());
            createText(doc, config, "module-id", data.getModuleId().toString());
            createText(doc, config, "category", data.getCategory());
            createText(doc, config, "description", data.getDescription());
            if(data.getPluginURL() != null) {
                createText(doc, config, "url", data.getPluginURL());
            }
            if(data.getAuthor() != null) {
                createText(doc, config, "author", data.getAuthor());
            }
            for (int i = 0; i < data.getLicenses().length; i++) {
                PluginMetadata.License license = data.getLicenses()[i];
                Element lic = doc.createElement("license");
                lic.appendChild(doc.createTextNode(license.getName()));
                lic.setAttribute("osi-approved", license.isOsiApproved() ? "true" : "false");
                config.appendChild(lic);
            }
            if(data.getHash() != null) {
                Element hash = doc.createElement("hash");
                hash.setAttribute("type", data.getHash().getType());
                hash.appendChild(doc.createTextNode(data.getHash().getValue()));
                config.appendChild(hash);
            } else if(file.isFile() && file.canRead()) {
                Element hash = doc.createElement("hash");
                hash.setAttribute("type", "SHA-1");
                hash.appendChild(doc.createTextNode(ConfigurationStoreUtil.getActualChecksum(file)));
                config.appendChild(hash);
            }
            for (int i = 0; i < data.getGeronimoVersions().length; i++) {
                String version = data.getGeronimoVersions()[i];
                createText(doc, config, "geronimo-version", version);
            }
            for (int i = 0; i < data.getJvmVersions().length; i++) {
                String version = data.getJvmVersions()[i];
                createText(doc, config, "jvm-version", version);
            }
            for (int i = 0; i < data.getPrerequisites().length; i++) {
                PluginMetadata.Prerequisite prereq = data.getPrerequisites()[i];
                writePrerequisite(doc, config, prereq);
            }
            for (int i = 0; i < data.getDependencies().length; i++) {
                String version = data.getDependencies()[i];
                createText(doc, config, "dependency", version);
            }
            for (int i = 0; i < data.getObsoletes().length; i++) {
                String version = data.getObsoletes()[i];
                createText(doc, config, "obsoletes", version);
            }
            // Skip the repository, we'll specify that at the top level
//            for (int i = 0; i < data.getRepositories().length; i++) {
//                URL url = data.getRepositories()[i];
//                createText(doc, config, "source-repository", url.toString());
//            }
        }
        Version ger = (Version) targetVersions.get("geronimo");
        createText(doc, root, "default-repository", "http://www.geronimoplugins.com/repository/geronimo-"+ger.getMajorVersion()+"."+ger.getMinorVersion());
        createText(doc, root, "default-repository", "http://www.ibiblio.org/maven2/");
        return doc;
    }

    private void writePrerequisite(Document doc, Element config, PluginMetadata.Prerequisite data) {
        Element prereq = doc.createElement("prerequisite");
        config.appendChild(prereq);
        createText(doc, prereq, "id", data.getModuleId().toString());
        createText(doc, prereq, "resource-type",data.getResourceType());
        createText(doc, prereq, "description",data.getDescription());
    }

    private void createText(Document doc, Element parent, String name, String text) {
        Element child = doc.createElement(name);
        parent.appendChild(child);
        Text node = doc.createTextNode(text);
        child.appendChild(node);
    }

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

    public static void main(String[] args) {
        try {
            new PluginRepositoryExporter(args[0], args[1], args[2]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
