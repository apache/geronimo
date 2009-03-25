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
package org.apache.geronimo.kernel.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.kernel.util.InputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Rev: 506425 $ $Date: 2007-02-12 22:49:46 +1100 (Mon, 12 Feb 2007) $
 */
public abstract class AbstractRepository implements WriteableRepository {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final static ArtifactTypeHandler DEFAULT_TYPE_HANDLER = new CopyArtifactTypeHandler();
    private final static Pattern ILLEGAL_CHARS = Pattern.compile("[\\.]{2}|[()<>,;:\\\\/\"\']");
    protected final File rootFile;
    private final Map<String, ArtifactTypeHandler> typeHandlers = new HashMap<String, ArtifactTypeHandler>();

    public AbstractRepository(File rootFile) {
        if (rootFile == null) throw new NullPointerException("root is null");

        if (!rootFile.exists() || !rootFile.isDirectory() || !rootFile.canRead()) {
            throw new IllegalStateException("Maven2Repository must have a root that's a valid readable directory (not " + rootFile.getAbsolutePath() + ")");
        }

        this.rootFile = rootFile;
        log.debug("Repository root is {}", rootFile.getAbsolutePath());

        typeHandlers.put("car", new UnpackArtifactTypeHandler());
    }

    public boolean contains(Artifact artifact) {
        // Note: getLocation(artifact) does an artifact.isResolved() check - no need to do it here.
        File location = getLocation(artifact);
        return location.canRead() && (location.isFile() || new File(location, "META-INF").isDirectory());
    }

    private static final String NAMESPACE = "http://geronimo.apache.org/xml/ns/deployment-1.2";
    public LinkedHashSet<Artifact> getDependencies(Artifact artifact) {
        if(!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact "+artifact+" is not fully resolved");
        }
        LinkedHashSet<Artifact> dependencies = new LinkedHashSet<Artifact>();
        URL url;
        try {
            File location = getLocation(artifact);
            url = location.toURL();
        } catch (MalformedURLException e) {
            throw (IllegalStateException)new IllegalStateException("Unable to get URL for dependency " + artifact).initCause(e);
        }
        ClassLoader depCL = new URLClassLoader(new URL[]{url}, new ClassLoader() {
            @Override
            public URL getResource(String name) {
                return null;
            }
        });
        InputStream is = depCL.getResourceAsStream("META-INF/geronimo-dependency.xml");
        try {
            if (is != null) {
                InputSource in = new InputSource(is);
                DocumentBuilderFactory dfactory = XmlUtil.newDocumentBuilderFactory();
                dfactory.setNamespaceAware(true);
                try {
                    Document doc = dfactory.newDocumentBuilder().parse(in);
                    Element root = doc.getDocumentElement();
                    NodeList configs = root.getElementsByTagNameNS(NAMESPACE, "dependency");
                    for (int i = 0; i < configs.getLength(); i++) {
                        Element dependencyElement = (Element) configs.item(i);
                        String groupId = getString(dependencyElement, "groupId");
                        String artifactId = getString(dependencyElement, "artifactId");
                        String version = getString(dependencyElement, "version");
                        String type = getString(dependencyElement, "type");
                        if (type == null) {
                            type = "jar";
                        }
                        dependencies.add(new Artifact(groupId, artifactId,  version, type));
                    }
                } catch (IOException e) {
                    throw (IllegalStateException)new IllegalStateException("Unable to parse geronimo-dependency.xml file in " + url).initCause(e);
                } catch (ParserConfigurationException e) {
                    throw (IllegalStateException)new IllegalStateException("Unable to parse geronimo-dependency.xml file in " + url).initCause(e);
                } catch (SAXException e) {
                    throw (IllegalStateException)new IllegalStateException("Unable to parse geronimo-dependency.xml file in " + url).initCause(e);
                }
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
        return dependencies;
    }

    private String getString(Element dependencyElement, String childName) {
        NodeList children = dependencyElement.getElementsByTagNameNS(NAMESPACE, childName);
        if (children == null || children.getLength() == 0) {
        return null;
        }
        String value = "";
        NodeList text = children.item(0).getChildNodes();
        for (int t = 0; t < text.getLength(); t++) {
            Node n = text.item(t);
            if (n.getNodeType() == Node.TEXT_NODE) {
                value += n.getNodeValue();
            }
        }
        return value.trim();
    }

    public void setTypeHandler(String type, ArtifactTypeHandler handler) {
        typeHandlers.put(type, handler);
    }

    public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {

        // ensure there are no illegal chars in destination elements
        InputUtils.validateSafeInput(new ArrayList(Arrays.asList(destination.getGroupId(), destination.getArtifactId(), destination.getVersion().toString(), destination.getType())));

        if(!destination.isResolved()) {
            throw new IllegalArgumentException("Artifact "+destination+" is not fully resolved");
        }
        if (!source.exists() || !source.canRead() || source.isDirectory()) {
            throw new IllegalArgumentException("Cannot read source file at " + source.getAbsolutePath());
        }
        int size = 0;
        ZipFile zip = null;
        try {
            zip = new ZipFile(source);
            for (Enumeration entries=zip.entries(); entries.hasMoreElements();) {
            	ZipEntry entry = (ZipEntry)entries.nextElement();
            	size += entry.getSize();
            }
        } catch (ZipException ze) {
            size = (int)source.length();
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
        FileInputStream is = new FileInputStream(source);
        try {
            copyToRepository(is, size, destination, monitor);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    public void copyToRepository(InputStream source, int size, Artifact destination, FileWriteMonitor monitor) throws IOException {
        if(!destination.isResolved()) {
            throw new IllegalArgumentException("Artifact "+destination+" is not fully resolved");
        }
        // is this a writable repository
        if (!rootFile.canWrite()) {
            throw new IllegalStateException("This repository is not writable: " + rootFile.getAbsolutePath() + ")");
        }

        // where are we going to install the file
        File location = getLocation(destination);

        // assure that there isn't already a file installed at the specified location
        if (location.exists()) {
            throw new IllegalArgumentException("Destination " + location.getAbsolutePath() + " already exists!");
        }

        ArtifactTypeHandler typeHandler = typeHandlers.get(destination.getType());
        if (typeHandler == null) typeHandler = DEFAULT_TYPE_HANDLER;
        typeHandler.install(source, size, destination, monitor, location);
        
        if (destination.getType().equalsIgnoreCase("car")) {
            log.debug("Installed module configuration; id={}; location={}", destination, location);
        }
    }
}
