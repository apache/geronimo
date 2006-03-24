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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WriteableRepository;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Handler for the initial download screen.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DownloadCARHandler extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(DownloadCARHandler.class);

    public DownloadCARHandler() {
        super(DOWNLOAD_MODE, "/WEB-INF/view/car/download.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String repo = request.getParameter("repository");
        if(!repo.endsWith("/")) {
            repo += "/";
        }
        String url = getURL(configId, repo);
        File file = File.createTempFile("geronimo-download", "." + configId.substring(configId.lastIndexOf("/")+1));
        file.deleteOnExit();
        downloadFile(url, file);
        response.setRenderParameter("configId", configId);
        response.setRenderParameter("file", file.getAbsolutePath());
        response.setRenderParameter("repository", repo);

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String repo = request.getParameter("repository");
        File file = new File(request.getParameter("file"));
        ZipFile zip = new ZipFile(file);
        try {
            ZipEntry entry = zip.getEntry("META-INF/config.ser");
            ObjectInputStream serIn = new ObjectInputStream(zip.getInputStream(entry));
            GBeanData config = new GBeanData();
            config.readExternal(serIn);
            URI[] parentIds = (URI[]) config.getAttribute("parentId");
            List dependencies = (List) config.getAttribute("dependencies");
            request.setAttribute("file", file.getAbsolutePath());
            request.setAttribute("configId", configId);
            request.setAttribute("parents", parentIds);
            request.setAttribute("dependencies", dependencies);
            request.setAttribute("repository", repo);
        } catch (ClassNotFoundException e) {
            throw new PortletException("Unable to deserialize GBeanData", e);
        } finally {
            zip.close();
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repo = request.getParameter("repository");
        boolean proceed = Boolean.valueOf(request.getParameter("proceed")).booleanValue();
        if(proceed) {
            String configId = request.getParameter("configId");
            File file = new File(request.getParameter("file"));

            WriteableRepository[] repos = PortletManager.getWritableRepositories(request);
            if(repos.length < 1) {
                throw new PortletException("No writeable repository available in kernel");
            }
            List configs = new ArrayList();
            List deps = new ArrayList();
            downloadConfiguration(file, repos[0], repo, configs, deps);
            request.getPortletSession(true).setAttribute("car.install.configurations", configs);
            request.getPortletSession(true).setAttribute("car.install.dependencies", deps);
            response.setRenderParameter("configId", configId);
        }
        return RESULTS_MODE+BEFORE_ACTION;
    }

    private void downloadConfiguration(File file, WriteableRepository repo, String repoURL, List configResults, List dependencyResults) throws IOException, PortletException {
        Kernel kernel = KernelRegistry.getSingleKernel();
        Set set = kernel.listGBeans(new GBeanQuery(null, ConfigurationStore.class.getName()));
        ConfigurationStore stores[] = new ConfigurationStore[set.size()];
        int index = 0;
        for (Iterator it = set.iterator(); it.hasNext(); ++index) {
            ObjectName name = (ObjectName) it.next();
            stores[index] = (ConfigurationStore) kernel.getProxyManager().createProxy(name, ConfigurationStore.class);
        }
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
                if(((Repository)repo).hasURI(dep)) {
                    dependencyResults.add(new DownloadResults(dep.toString(), "already present"));
                    continue;
                }
                String url = getURL(dep.toString(), repoURL);
                log.info("Downloading "+url+" to local repository");
                repo.copyToRepository(new URL(url).openStream(), dep, null);
                dependencyResults.add(new DownloadResults(dep.toString(), "downloaded and installed"));
            }
            // Download the parents
            parents:
            for (int i = 0; i < parentIds.length; i++) {
                String id = parentIds[i].toString();
                URI uri = new URI(id);
                for (int j = 0; j < stores.length; j++) {
                    ConfigurationStore store = stores[j];
                    if(store.containsConfiguration(uri)) {
                        configResults.add(new DownloadResults(uri.toString(), "already present"));
                        continue parents;
                    }
                }
                File next = File.createTempFile("geronimo-download", "." + id.substring(id.lastIndexOf("/")+1));
                file.deleteOnExit();
                String url = getURL(id, repoURL);
                downloadFile(url, next);
                downloadConfiguration(next, repo, repoURL, configResults, dependencyResults);
            }
            // Install the configuration
            for (Iterator it = set.iterator(); it.hasNext();) {
                ObjectName name = (ObjectName) it.next();
                if(name.getKeyProperty(NameFactory.J2EE_NAME).equals("Local")) {
                    try {
                        URI uri = (URI)kernel.invoke(name, "install", new Object[]{file.toURL()}, new String[]{URL.class.getName()});
                        configResults.add(new DownloadResults(uri.toString(), "downloaded and installed"));
                    } catch (Exception e) {
                        throw new IOException("Unable to write ZIP file: "+e.getMessage());
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new PortletException("Unable to process parent configurations", e);
        } catch (ClassNotFoundException e) {
            throw new PortletException("Unable to deserialize GBeanData", e);
        } finally {
            zip.close();
        }
    }

    private String getURL(String configId, String baseRepositoryURL) {
        String[] parts = configId.split("/");
        return baseRepositoryURL+parts[0]+"/"+parts[3]+"s/"+parts[1]+"-"+parts[2]+"."+parts[3];
    }

    private void downloadFile(String url, File target) throws IOException {
        log.info("Downloading "+url+" to "+target.getAbsolutePath());
        byte[] buf = new byte[10240];
        InputStream in = new URL(url).openStream();
        FileOutputStream out = new FileOutputStream(target);
        int count;
        while((count = in.read(buf)) > -1) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
    }

    public static class DownloadResults implements Serializable {
        private String name;
        private String action;

        public DownloadResults(String name, String action) {
            this.name = name;
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public String getAction() {
            return action;
        }
    }
}
