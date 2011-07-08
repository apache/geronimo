/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.security.auth.login.FailedLoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.crypto.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class RemoteSourceRepository implements SourceRepository {

    private final URI base;
    private final String username;
    private final String password;

    public RemoteSourceRepository(URI base, String username, String password) {
        if (!base.getPath().endsWith("/")) {
            throw new IllegalArgumentException("base uri must end with '/', not " + base);
        }
        this.base = base;
        this.username = username;
        this.password = password;
    }

    public PluginListType getPluginList() {
        try {
            URL uri = base.resolve("geronimo-plugins.xml").toURL();
            InputStream in = openStream(uri);
            if (in != null) {
                try {
                    return PluginXmlUtil.loadPluginList(in);
                } finally {
                    in.close();                
                }
            }
        } catch (Exception e) {
            // TODO: log it?
        }
        return null;
    }

    public OpenResult open(final Artifact artifact, final FileWriteMonitor monitor) throws IOException, FailedLoginException {

        // If the artifact version is resolved then look for the artifact in the repo
        if (artifact.isResolved()) {
            URL location = getURL(artifact);
            OpenResult result = open(artifact, location);
            if (result != null) {
                return result;
            }
            Version version = artifact.getVersion();
            // Snapshot artifacts can have a special filename in an online maven repo.
            // The version number is replaced with a timestmap and build number.
            // The maven-metadata file contains this extra information.
            if (version.toString().indexOf("SNAPSHOT") >= 0 && !(version instanceof SnapshotVersion)) {
                // base path for the artifact version in a maven repo
                URI basePath = base.resolve(artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId() + "/" + version + "/");

                // get the maven-metadata file
                Document metadata = getMavenMetadata(basePath);

                // determine the snapshot qualifier from the maven-metadata file
                if (metadata != null) {
                    NodeList snapshots = metadata.getDocumentElement().getElementsByTagName("snapshot");
                    if (snapshots.getLength() >= 1) {
                        Element snapshot = (Element) snapshots.item(0);
                        List<String> timestamp = getChildrenText(snapshot, "timestamp");
                        List<String> buildNumber = getChildrenText(snapshot, "buildNumber");
                        if (timestamp.size() >= 1 && buildNumber.size() >= 1) {
                            try {
                                // recurse back into this method using a SnapshotVersion
                                SnapshotVersion snapshotVersion = new SnapshotVersion(version);
                                snapshotVersion.setBuildNumber(Integer.parseInt(buildNumber.get(0)));
                                snapshotVersion.setTimestamp(timestamp.get(0));
                                Artifact newQuery = new Artifact(artifact.getGroupId(), artifact.getArtifactId(), snapshotVersion, artifact.getType());
                                location = getURL(newQuery);
                                return open(artifact, location);
                            } catch (NumberFormatException nfe) {
//                                log.error("Could not create snapshot version for " + artifact, nfe);
                            }
                        } else {
//                            log.error("Could not create snapshot version for " + artifact);
                        }
                    }
                }
            }
            return null;
        }

        // Version is not resolved.  Look in maven-metadata.xml and maven-metadata-local.xml for
        // the available version numbers.  If found then recurse into the enclosing method with
        // a resolved version number
        else {

            // base path for the artifact version in a maven repo
            URI basePath = base.resolve(artifact.getGroupId().replace('.', '/') + "/" + artifact.getArtifactId() + "/");

            // get the maven-metadata file
            Document metadata = getMavenMetadata(basePath);

            // determine the available versions from the maven-metadata file
            if (metadata != null) {
                Element root = metadata.getDocumentElement();
                NodeList list = root.getElementsByTagName("versions");
                list = ((Element) list.item(0)).getElementsByTagName("version");
                Version[] available = new Version[list.getLength()];
                for (int i = 0; i < available.length; i++) {
                    available[i] = new Version(getText(list.item(i)));
                }
                // desc sort
                Arrays.sort(available, new Comparator<Version>() {
                    public int compare(Version o1, Version o2) {
                        return o2.toString().compareTo(o1.toString());
                    }
                });

                for (Version version : available) {
                    Artifact versionedArtifact = new Artifact(artifact.getGroupId(), artifact.getArtifactId(), version, artifact.getType());
                    URL location = getURL(versionedArtifact);
                    OpenResult result = open(versionedArtifact, location);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private OpenResult open(Artifact artifact, URL location) throws IOException, FailedLoginException {
        InputStream in = openStream(location);
        return (in == null) ? null : new RemoteOpenResult(artifact, in);
    }

    private InputStream openStream(URL location) throws IOException, FailedLoginException {
        URLConnection con = location.openConnection();
        if (con instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection) con;

            try {
                http.connect();
            } catch (IOException e) {
                throw (IOException) new IOException("Cannot connect to "+location).initCause(e);
            }

            if (http.getResponseCode() == 401) { // need to authenticate
                if (username == null || username.equals("")) {
                    throw new FailedLoginException("Server returned 401 " + http.getResponseMessage());
                }
                //TODO is it necessary to keep getting new http's ?
                http = (HttpURLConnection) location.openConnection();
                http.setRequestProperty("Authorization",
                        "Basic " + new String(Base64.encode((username + ":" + password).getBytes())));
                http.connect();
                if (http.getResponseCode() == 401) {
                    throw new FailedLoginException("Server returned 401 " + http.getResponseMessage());
                } else if (http.getResponseCode() == 404) {
                    return null; // Not found at this repository
                }
            } else if (http.getResponseCode() == 404) {
                return null; // Not found at this repository
            }
            return http.getInputStream();
        }
        return null;
    }


    private Document getMavenMetadata(URI base) throws IOException, FailedLoginException {
        Document doc = null;
        InputStream in = null;

        try {
            URL metaURL = base.resolve( "maven-metadata.xml").toURL();
            in = openStream(metaURL);
            if (in == null) { // check for local maven metadata
                metaURL = base.resolve("maven-metadata-local.xml").toURL();
                in = openStream(metaURL);
            }
            if (in != null) {
                DocumentBuilder builder = XmlUtil.newDocumentBuilderFactory().newDocumentBuilder();
                doc = builder.parse(in);
            }
        } catch (ParserConfigurationException e) {
            throw (IOException)new IOException().initCause(e);
        } catch (SAXException e) {
            throw (IOException)new IOException().initCause(e);
        } finally {
            if (in == null) {
//                log.info("No maven metadata available at " + base);
            } else {
                in.close();
            }
        }
        return doc;
    }


    private URL getURL(Artifact configId) throws MalformedURLException {
        String qualifiedVersion = configId.getVersion().toString();
        if (configId.getVersion() instanceof SnapshotVersion) {
            SnapshotVersion ssVersion = (SnapshotVersion) configId.getVersion();
            String timestamp = ssVersion.getTimestamp();
            int buildNumber = ssVersion.getBuildNumber();
            if (timestamp != null && buildNumber != 0) {
                qualifiedVersion = qualifiedVersion.replaceAll("SNAPSHOT", timestamp + "-" + buildNumber);
            }
        }
        return base.resolve(configId.getGroupId().replace('.', '/') + "/"
                + configId.getArtifactId() + "/" + configId.getVersion()
                + "/" + configId.getArtifactId() + "-"
                + qualifiedVersion + "." + configId.getType()).toURL();
    }

        /**
     * Gets all the text contents of the specified DOM node.
     */
    private static String getText(Node target) {
        NodeList nodes = target.getChildNodes();
        StringBuilder buf = null;
        for (int j = 0; j < nodes.getLength(); j++) {
            Node node = nodes.item(j);
            if (node.getNodeType() == Node.TEXT_NODE) {
                if (buf == null) {
                    buf = new StringBuilder();
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
    private static List<String> getChildrenText(Element root, String property) {
        NodeList children = root.getChildNodes();
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < children.getLength(); i++) {
            Node check = children.item(i);
            if (check.getNodeType() == Node.ELEMENT_NODE && check.getNodeName().equals(property)) {
                NodeList nodes = check.getChildNodes();
                StringBuilder buf = null;
                for (int j = 0; j < nodes.getLength(); j++) {
                    Node node = nodes.item(j);
                    if (node.getNodeType() == Node.TEXT_NODE) {
                        if (buf == null) {
                            buf = new StringBuilder();
                        }
                        buf.append(node.getNodeValue());
                    }
                }
                results.add(buf == null ? null : buf.toString());
            }
        }
        return results;
    }

    private static class RemoteOpenResult implements OpenResult {
        private final Artifact artifact;
        private final InputStream in;
        private File file;

        private RemoteOpenResult(Artifact artifact, InputStream in) {
            if (!artifact.isResolved()) {
                throw new IllegalStateException("Artifact is not resolved: " + artifact);
            }
            this.artifact = artifact;
            this.in = in;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        public File getFile() throws IOException {
            if (file == null) {
                file = downloadFile(in);
            }
            return file;
        }

        public void install(WriteableRepository repo, FileWriteMonitor monitor) throws IOException {
            File file = getFile();
            repo.copyToRepository(file, artifact, monitor);
            if (!file.delete()) {
//                log.warn("Unable to delete temporary download file " + tempFile.getAbsolutePath());
                file.deleteOnExit();
            }
        }

        public void close() {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        /**
         * Downloads to a temporary file so we can validate the download before
         * installing into the repository.
         *
         * @param in  source of download
    //     * @param monitor monitor to report results of download
         * @return downloaded file
         * @throws IOException if input cannot be read or file cannot be written
         */
        private File downloadFile(InputStream in/*, ResultsFileWriteMonitor monitor*/) throws IOException {
            if (in == null) {
                throw new IllegalStateException();
            }
            FileOutputStream out = null;
            byte[] buf;
            try {
//            monitor.writeStarted(result.getArtifact().toString(), result.getFileSize());
                File file = File.createTempFile("geronimo-plugin-download-", ".tmp");
                out = new FileOutputStream(file);
                buf = new byte[65536];
                int count, total = 0;
                while ((count = in.read(buf)) > -1) {
                    out.write(buf, 0, count);
//                monitor.writeProgress(total += count);
                }
//            monitor.writeComplete(total);
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
    }

    public String toString() {
        return getClass().getName() + ":" + base;
    }

}
