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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Servlet that lets you download a CAR from the repository
 *
 * @version $Rev$ $Date$
 */
public class GeronimoAsMavenServlet extends HttpServlet {
    private final static Log log = LogFactory.getLog(GeronimoAsMavenServlet.class);

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
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < parts.length-1; i++) {
                        String part = parts[i];
                        if(i > 0) buf.append('.');
                        buf.append(part);
                    }
                    generateMavenFile(kernel, response.getWriter(), buf.toString(), parts[parts.length-1], reply);
                } else {
                    generateMavenFile(kernel, response.getWriter(), parts[0], parts[1], reply);
                }
            } catch (Exception e) {
                throw new ServletException("Unable to generate Geronimo configuration list", e);
            }
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
        } else {  // e.g.   console/MyDatabase/1.0-SNAPSHOT/MyDatabase-1.0-SNAPSHOT.rar
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
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
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
        Set repos = kernel.listGBeans(new AbstractNameQuery(Repository.class.getName()));
        for (Iterator it = repos.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            Repository repo = (Repository) kernel.getProxyManager().createProxy(name, Repository.class);
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

    private void generateConfigFile(HttpServletRequest request, Kernel kernel, PrintWriter out) throws ParserConfigurationException, NoSuchStoreException, TransformerException {
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
        PluginInstaller installer = getInstaller(kernel);
        DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElementNS("http://geronimo.apache.org/xml/ns/plugins-1.1", "geronimo-plugin-list");
        root.setAttribute("xmlns", "http://geronimo.apache.org/xml/ns/plugins-1.1");
        doc.appendChild(root);
        List stores = mgr.listStores();
        for (int i = 0; i < stores.size(); i++) {
            AbstractName name = (AbstractName) stores.get(i);
            List configs = mgr.listConfigurations(name);
            for (int j = 0; j < configs.size(); j++) {
                ConfigurationInfo info = (ConfigurationInfo) configs.get(j);
                PluginMetadata data = installer.getPluginMetadata(info.getConfigID());
                Element config = doc.createElement("plugin");
                root.appendChild(config);
                createText(doc, config, "name", data.getName());
                createText(doc, config, "module-id", data.getModuleId().toString());
                createText(doc, config, "category", "Geronimo Deployments");
                createText(doc, config, "description", data.getCategory().equals("Unknown") ? "Automatically generated plugin metadata" : data.getDescription());
                if(data.getPluginURL() != null) {
                    createText(doc, config, "url", data.getPluginURL());
                }
                if(data.getAuthor() != null) {
                    createText(doc, config, "author", data.getAuthor());
                }
                for (int k = 0; k < data.getLicenses().length; k++) {
                    PluginMetadata.License license = data.getLicenses()[k];
                    Element lic = doc.createElement("license");
                    lic.setAttribute("osi-approved", Boolean.toString(license.isOsiApproved()));
                    createText(doc, lic, license.getName());
                    config.appendChild(lic);
                }
                // Skip hash since the CAR will be re-exported anyway and the file will be different
                PluginMetadata.geronimoVersions[] versions = data.getGeronimoVersions();
                for (int k = 0; k < versions.length; k++) {
                    PluginMetadata.geronimoVersions ver = versions[k];
                    writeGeronimoVersion(doc, config, ver);
                }
                String[] jvmVersions = data.getJvmVersions();
                for (int k = 0; k < versions.length; k++) {
                    String ver = jvmVersions[k];
                    createText(doc, config, "jvm-version", ver);
                }
                for (int k = 0; k < data.getPrerequisites().length; k++) {
                    PluginMetadata.Prerequisite prereq = data.getPrerequisites()[k];
                    writePrerequisite(doc, config, prereq);
                }
                for (int k = 0; k < data.getDependencies().length; k++) {
                    String dep = data.getDependencies()[k];
                    createText(doc, config, "dependency", dep);
                }
                for (int k = 0; k < data.getObsoletes().length; k++) {
                    String obs = data.getObsoletes()[k];
                    createText(doc, config, "obsoletes", obs);
                }
                // Skip repositories since we want the download to come from here
            }
        }
        String repo = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+request.getServletPath();
        if(!repo.endsWith("/")) repo += "/";
        createText(doc, root, "default-repository", repo);
        TransformerFactory xfactory = XmlUtil.newTransformerFactory();
        Transformer xform = xfactory.newTransformer();
        xform.setOutputProperty(OutputKeys.INDENT, "yes");
        xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        xform.transform(new DOMSource(doc), new StreamResult(out));
    }

    private PluginInstaller getInstaller(Kernel kernel) {
        Set names = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        if(names.size() == 0) {
            return null;
        }
        return (PluginInstaller) kernel.getProxyManager().createProxy((AbstractName) names.iterator().next(), PluginInstaller.class);
    }

    private void generateMavenFile(Kernel kernel, PrintWriter writer, String groupId, String artifactId, boolean reply) throws ParserConfigurationException, TransformerException {
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
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

    private void writePrerequisite(Document doc, Element config, PluginMetadata.Prerequisite req) {
        Element prereq = doc.createElement("prerequisite");
        config.appendChild(prereq);
        createText(doc, prereq, "id", req.getModuleId().toString());
        createText(doc, prereq, "resource-type", req.getResourceType());
        createText(doc, prereq, "description", req.getDescription());
    }
    
    private void writeGeronimoVersion(Document doc, Element config, PluginMetadata.geronimoVersions ver){
    	Element ger = doc.createElement("geronimo-versions");
        createText(doc, ger, "version", ver.getVersion());
        if (ver.getModuleId() != null){
        	createText(doc, ger, "module-id", ver.getModuleId());
        }
        if (ver.getPrerequisite() != null){
            for (int j = 0; j < ver.getPrerequisite().length; j++) {
                PluginMetadata.Prerequisite prereq = ver.getPrerequisite()[j];
                Element pre = doc.createElement("prerequisite");
                createText(doc, pre, "id", prereq.getModuleId().toString());
                if(prereq.getResourceType() != null) {
                    createText(doc, pre, "resource-type", prereq.getResourceType());
                }
                if(prereq.getDescription() != null) {
                    createText(doc, pre, "description", prereq.getDescription());
                }
                ger.appendChild(pre);
            }
        }
    }

    private void createText(Document doc, Element parent, String name, String text) {
        Element child = doc.createElement(name);
        parent.appendChild(child);
        Text node = doc.createTextNode(text);
        child.appendChild(node);
    }

    private void createText(Document doc, Element parent, String text) {
        Text node = doc.createTextNode(text);
        parent.appendChild(node);
    }
}
