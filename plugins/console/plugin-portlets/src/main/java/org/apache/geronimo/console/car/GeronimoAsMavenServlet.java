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
package org.apache.geronimo.console.car;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Servlet that lets you download a CAR from the repository
 *
 * @version $Rev$ $Date$
 */
public class GeronimoAsMavenServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2106697871964363101L;

    private static final Logger log = LoggerFactory.getLogger(GeronimoAsMavenServlet.class);

    protected void doHead(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        handleRequest(httpServletRequest, httpServletResponse, false);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response, true);
    }
    protected void handleRequest(HttpServletRequest request, HttpServletResponse response, boolean reply) throws ServletException, IOException {
        String path = request.getPathInfo();
        if(path == null) {
            throw new ServletException("No configId specified for CAR download");
        }
        Kernel kernel = KernelRegistry.getSingleKernel();
        if(path.equals("/geronimo-plugins.xml")) {
            response.setContentType("text/xml");
            if(reply) {
                try {
                    generateConfigFile(request, kernel, response.getWriter());
                } catch (Exception e) {
                    throw new ServletException("Unable to generate Geronimo configuration list", e);
                }
            }
        } else if(path.endsWith("/maven-metadata.xml")) {
            response.setContentType("text/xml");
            try {
                String start = path.substring(0, path.lastIndexOf('/'));
                if(start.charAt(0) == '/') {
                    start = start.substring(1);
                }
                String[] parts = start.split("/");
                if(parts.length > 2) {
                    StringBuilder buf = new StringBuilder();
                    for (int i = 0; i < parts.length-1; i++) {
                        String part = parts[i];
                        if(i > 0) buf.append('.');
                        buf.append(part);
                    }
                    generateMavenFile(kernel, response.getWriter(), buf.toString(), parts[parts.length-1], reply);
                } else if (parts.length == 2) {
                    generateMavenFile(kernel, response.getWriter(), parts[0], parts[1], reply);
                } else {
                    generateInstruction(path, response);
                }
            } catch (Exception e) {
                throw new ServletException("Unable to generate Geronimo configuration list", e);
            }
        } else if (path.equals("/")) {
            //give user some basic instructions
            generateInstruction(path, response);
        } else {
            if(path.startsWith("/")) {
                path = path.substring(1);
            }
            String configId = parsePath(path, response);
            if(configId == null) { // we already sent the 404
                return;
            }
            if(!produceDownloadFile(kernel, Artifact.create(configId), response, reply)) {
                response.sendError(404, "Cannot locate download file "+path);
            }
        }
    }

    private static String parsePath(String path, HttpServletResponse response) throws IOException {
        String[] parts = path.split("/");
        String groupId, artifactId, version, type;
        if(parts.length < 4) {
            response.sendError(404, "Unrecognized path form "+path);
            return null;
        } else {  // e.g.   console/MyDatabase/1.0/MyDatabase-1.0.rar
            groupId = parts[0];
            for(int i=4; i<parts.length; i++) {
                groupId = groupId+"."+parts[i-3];
            }
            artifactId = parts[parts.length-3];
            version = parts[parts.length-2];
            if(!parts[parts.length-1].startsWith(artifactId+"-"+version)) {
                response.sendError(404, "Unrecognized path structure "+path);
                return null;
            }
            type = parts[parts.length-1].substring(artifactId.length()+version.length()+2);
        }
        return groupId+"/"+artifactId+"/"+version+"/"+type;
    }

    private boolean produceDownloadFile(Kernel kernel, Artifact configId, HttpServletResponse response, boolean reply) throws IOException {
        //todo: replace kernel mumbo jumbo with JSR-77 navigation
        // Step 1: check if it's in a configuration store
        ConfigurationManager mgr;
        try {
            mgr = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            throw new IOException(e.getMessage());
        }
        if(mgr.isConfiguration(configId)) {
            ConfigurationStore store = mgr.getStoreForConfiguration(configId);
            response.setContentType("application/zip");
            if(!reply) {
                return true;
            }
            try {
                store.exportConfiguration(configId, response.getOutputStream());
                return true;
            } catch (NoSuchConfigException e) {
                log.error("Inconsistent ConfigurationStore data; ConfigManager claims it has configuration "+configId+" but store claims it doesn't",e);
                throw new IOException("Unable to write ZIP file; see server log for details");
            }
        }
        // Step 2: check if it's in a repository
        Set<AbstractName> repos = kernel.listGBeans(new AbstractNameQuery(Repository.class.getName()));
        for (AbstractName name : repos) {
            Repository repo = getGBean(kernel, name, Repository.class);
            if(repo.contains(configId)) {
                File path = repo.getLocation(configId);
                if(!path.exists()) throw new IllegalStateException("Can't find file '"+path.getAbsolutePath()+"' though repository said there's an artifact there!");
                response.setContentType("application/zip");
                if(!reply) {
                    return true;
                }
                InputStream in = new BufferedInputStream(new FileInputStream(path));
                response.setContentLength((int)path.length());
                OutputStream out = response.getOutputStream();
                byte[] buf = new byte[1024];
                int count;
                while((count = in.read(buf)) > -1) {
                    out.write(buf, 0, count);
                }
                in.close();
                return true;
            }
        }
        // Step 3: wasn't found
        return false;
    }

    private void generateConfigFile(HttpServletRequest request, Kernel kernel, PrintWriter out) throws NoSuchStoreException, JAXBException, XMLStreamException {
        String repo = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + request.getServletPath();
        if (!repo.endsWith("/")) {
            repo += "/";
        }
        PluginInstaller installer = getInstaller(kernel);
        PluginListType pluginList = installer.createPluginListForRepositories(repo);
        PluginXmlUtil.writePluginList(pluginList, out);
    }


    private PluginInstaller getInstaller(Kernel kernel) {
        Set<AbstractName> names = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        if(names.size() == 0) {
            return null;
        }
        return getGBean(kernel, names.iterator().next(), PluginInstaller.class);
    }

    private <T> T getGBean(Kernel kernel, AbstractName name, Class<T> clazz) {
        boolean createProxy = false;
        if (createProxy) {
            return kernel.getProxyManager().createProxy(name, clazz);
        } else {
            try {
                return clazz.cast(kernel.getGBean(name));
            } catch (GBeanNotFoundException e) {
                throw new IllegalStateException("No implementation for " + clazz.getName(), e);
            }
        }
    }

    private void generateMavenFile(Kernel kernel, PrintWriter writer, String groupId, String artifactId, boolean reply) throws ParserConfigurationException, TransformerException {
        ConfigurationManager mgr = null;
        try {
            mgr = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            //Should Not Happen
        }
        Artifact[] artifacts = mgr.getArtifactResolver().queryArtifacts(new Artifact(groupId, artifactId, (Version)null, null));
        if(!reply) {
            return;
        }

        DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("metadata");
        doc.appendChild(root);
        createText(doc, root, "groupId", groupId);
        createText(doc, root, "artifactId", artifactId);
        if(artifacts.length > 0) {
            createText(doc, root, "version", artifacts[0].getVersion().toString());
        }
        Element versioning = doc.createElement("versioning");
        root.appendChild(versioning);
        Element versions = doc.createElement("versions");
        versioning.appendChild(versions);
        for (int i = 0; i < artifacts.length; i++) {
            Artifact artifact = artifacts[i];
            createText(doc, versions, "version", artifact.getVersion().toString());
        }
        TransformerFactory xfactory = XmlUtil.newTransformerFactory();
        Transformer xform = xfactory.newTransformer();
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.transform(new DOMSource(doc), new StreamResult(writer));
    }


    private void createText(Document doc, Element parent, String name, String text) {
        Element child = doc.createElement(name);
        parent.appendChild(child);
        Text node = doc.createTextNode(text);
        child.appendChild(node);
    }
    
    private void generateInstruction(String path, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("Hello, this is the GeronimoAsMavenServlet.   The path - " + path + " you entered is not recognized.   <br/>");
        out.println("Please enter a valid path, for example: <br/>");
        out.println("/geronimo-plugins.xml <br/>");
        out.println("/org/apache/geronimo/maven-metadata.xml <br/>");
        out.close();
    }
}
