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
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.util.KernelManagementHelper;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Servlet that lets you download a CAR from the repository
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class GeronimoAsMavenServlet extends HttpServlet {
    private final static Log log = LogFactory.getLog(GeronimoAsMavenServlet.class);
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if(path == null) {
            throw new ServletException("No configId specified for CAR download");
        }
        Kernel kernel = KernelRegistry.getSingleKernel();
        if(path.equals("/geronimo-configs.xml")) {
            try {
                generateConfigFile(kernel, response.getWriter());
            } catch (Exception e) {
                throw new ServletException("Unable to generate Geronimo configuration list", e);
            }
        } else {
            if(path.startsWith("/")) {
                path = path.substring(1);
            }
            String[] parts = path.split("/");
            if(parts.length != 3) {
                response.sendError(404, "Unrecognized path form "+path);
                return;
            }
            String groupId = parts[0];
            String type = parts[1].substring(0, parts[1].length()-1);
            if(!parts[2].endsWith("."+type)) {
                response.sendError(404, "Unrecognized path structure "+path);
            }
            parts[2] = parts[2].substring(0, parts[2].length()-(type.length()+1));
            int pos = parts[2].lastIndexOf("-");
            String version = parts[2].substring(pos+1);
            if(version.equalsIgnoreCase("SNAPSHOT")) {
                pos = parts[2].lastIndexOf("-", pos-1);
                version = parts[2].substring(pos+1);
            }
            String artifactId = parts[2].substring(0, pos);
            String configId = groupId+"/"+artifactId+"/"+version+"/"+type;
            if(!produceDownloadFile(kernel, Artifact.create(configId), response)) {
                response.sendError(404, "Cannot locate download file "+path);
            }
        }
    }

    private boolean produceDownloadFile(Kernel kernel, Artifact configId, HttpServletResponse response) throws IOException {
        //todo: replace kernel mumbo jumbo with JSR-77 navigation
        // Step 1: check if it's in a configuration store
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
        List stores = mgr.listStores();
        for (int i = 0; i < stores.size(); i++) {
            AbstractName name = (AbstractName) stores.get(i);
            //todo: this is bad!!!
            if(name.getName().get(NameFactory.J2EE_NAME).equals("Local")) {
                ConfigurationStore store = (ConfigurationStore) kernel.getProxyManager().createProxy(name, ConfigurationStore.class);
                if(store.containsConfiguration(configId)) {
                    response.setContentType("application/zip");
                    try {
                        kernel.invoke(name, "exportConfiguration", new Object[]{configId.toString(), response.getOutputStream()}, new String[]{String.class.getName(), OutputStream.class.getName()});
                        return true;
                    } catch (Exception e) {
                        log.error("Unable to export configuration ZIP", e);
                        throw new IOException("Unable to write ZIP file: "+e.getMessage());
                    }
                }
            }
        }
        // Step 2: check if it's in a repository
        Set repos = kernel.listGBeans(new AbstractNameQuery(Repository.class.getName()));
        for (Iterator it = repos.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            Repository repo = (Repository) kernel.getProxyManager().createProxy(name, Repository.class);
            if(repo.contains(configId)) {
                File path = repo.getLocation(configId);
                if(!path.exists()) throw new IllegalStateException("Can't check length of file '"+path.getAbsolutePath()+"'");
                InputStream in = new BufferedInputStream(new FileInputStream(path));
                response.setContentType("application/zip");
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

    private void generateConfigFile(Kernel kernel, PrintWriter out) throws ParserConfigurationException, NoSuchStoreException, TransformerException {
        KernelManagementHelper helper = new KernelManagementHelper(kernel);
        ServerInfo serverInfo = helper.getServers(helper.getDomains()[0])[0].getServerInfo();
        String version = serverInfo.getVersion();
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("geronimo-config-list");
        doc.appendChild(root);
        List stores = mgr.listStores();
        for (int i = 0; i < stores.size(); i++) {
            AbstractName name = (AbstractName) stores.get(i);
            List configs = mgr.listConfigurations(name);
            for (int j = 0; j < configs.size(); j++) {
                ConfigurationInfo info = (ConfigurationInfo) configs.get(j);
                Element config = doc.createElement("configuration");
                root.appendChild(config);
                createText(doc, config, "name", info.getConfigID().toString());
                createText(doc, config, "config-id", info.getConfigID().toString());
                createText(doc, config, "category", "Geronimo Deployments");
                createText(doc, config, "geronimo-version", version);
                if(info.getConfigID().toString().indexOf("jetty") > -1) {
                    createText(doc, config, "prerequisite", "geronimo/jetty");
                } else if(info.getConfigID().toString().indexOf("tomcat") > -1) {
                    createText(doc, config, "prerequisite", "geronimo/tomcat");
                }
            }
        }
        TransformerFactory xfactory = TransformerFactory.newInstance();
        Transformer xform = xfactory.newTransformer();
        xform.transform(new DOMSource(doc), new StreamResult(out));
    }

    private void createText(Document doc, Element parent, String name, String text) {
        Element child = doc.createElement(name);
        parent.appendChild(child);
        Text node = doc.createTextNode(text);
        child.appendChild(node);
    }
}
