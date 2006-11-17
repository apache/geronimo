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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.PluginMetadata;

/**
 * Handler for the screen where you configure plugin data before exporting
 *
 * @version $Rev$ $Date$
 */
public class ExportConfigHandler extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(ExportConfigHandler.class);

    public ExportConfigHandler() {
        super(CONFIGURE_EXPORT_MODE, "/WEB-INF/view/car/pluginParams.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        if(configId != null) {
            response.setRenderParameter("configId", configId);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        PluginMetadata data = PortletManager.getCurrentServer(request).getPluginInstaller().getPluginMetadata(Artifact.create(configId));
        request.setAttribute("configId", data.getModuleId());
        request.setAttribute("name", data.getName());
        request.setAttribute("repository", combine(data.getRepositories()));
        request.setAttribute("category", data.getCategory());
        request.setAttribute("url", data.getPluginURL());
        request.setAttribute("author", data.getAuthor());
        request.setAttribute("description", data.getDescription());
        PluginMetadata.License[] licenses = data.getLicenses();
        if(licenses != null && licenses.length > 0) {
            request.setAttribute("license", licenses[0].getName());
            if(licenses[0].isOsiApproved()) {
                request.setAttribute("licenseOSI", "true");
            }
            if(licenses.length > 1) {
                log.warn("Unable to edit plugin metadata containing more than one license!  Additional license data will not be editable.");
            }
        }
        request.setAttribute("gerVersions", combine(data.getGeronimoVersions()));
        request.setAttribute("jvmVersions", combine(data.getJvmVersions()));
        request.setAttribute("dependencies", combine(data.getDependencies()));
        request.setAttribute("obsoletes", combine(data.getObsoletes()));
        PluginMetadata.Prerequisite[] reqs = data.getPrerequisites();
        if(reqs != null && reqs.length > 0) {
            for (int i = 0; i < reqs.length; i++) {
                PluginMetadata.Prerequisite req = reqs[i];
                String prefix = "prereq" + (i+1);
                request.setAttribute(prefix, req.getModuleId().toString());
                request.setAttribute(prefix +"type", req.getResourceType());
                request.setAttribute(prefix +"desc", req.getDescription());
            }
            if(reqs.length > 3) {
                log.warn("Unable to edit plugin metadata containing more than three prerequisites!  Additional prereqs will not be editable.");
            }
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String name = request.getParameter("name");
        String repo = request.getParameter("repository");
        String category = request.getParameter("category");
        String url = request.getParameter("url");
        String author = request.getParameter("author");
        String description = request.getParameter("description");
        String license = request.getParameter("license");
        String osi = request.getParameter("licenseOSI");
        String gers = request.getParameter("gerVersions");
        String jvms = request.getParameter("jvmVersions");
        String deps = request.getParameter("dependencies");
        String obsoletes = request.getParameter("obsoletes");
        PluginMetadata data = PortletManager.getCurrentServer(request).getPluginInstaller().getPluginMetadata(Artifact.create(configId));
        PluginMetadata metadata = new PluginMetadata(name, data.getModuleId(),
                category, description, url, author, null, true, false);
        metadata.setDependencies(split(deps));
        metadata.setGeronimoVersions(split(gers));
        metadata.setJvmVersions(split(jvms));
        metadata.setObsoletes(split(obsoletes));
        List licenses = new ArrayList();
        if(license != null && !license.trim().equals("")) {
            licenses.add(new PluginMetadata.License(license.trim(), osi != null && !osi.equals("")));
        }
        for (int i = 1; i < data.getLicenses().length; i++) {
            licenses.add(data.getLicenses()[i]);
        }
        metadata.setLicenses((PluginMetadata.License[]) licenses.toArray(new PluginMetadata.License[licenses.size()]));
        List prereqs = new ArrayList();
        int counter = 1;
        while(true) {
            String prefix = "prereq" + counter;
            ++counter;
            String id = request.getParameter(prefix);
            if(id == null || id.trim().equals("")) {
                break;
            }
            String type = request.getParameter(prefix+"type");
            String desc = request.getParameter(prefix+"desc");
            if(type != null && type.trim().equals("")) {
                type = null;
            }
            if(desc != null && desc.trim().equals("")) {
                desc = null;
            }
            prereqs.add(new PluginMetadata.Prerequisite(Artifact.create(id), false, type, desc));
        }
        for (int i = 3; i < data.getPrerequisites().length; i++) {
            PluginMetadata.Prerequisite req = data.getPrerequisites()[i];
            prereqs.add(req);
        }
        metadata.setPrerequisites((PluginMetadata.Prerequisite[]) prereqs.toArray(new PluginMetadata.Prerequisite[prereqs.size()]));
        URL[] backupURLs = splitURLs(repo);
        metadata.setRepositories(backupURLs);

        // TODO: Fields not yet handled by the UI
        metadata.setForceStart(data.getForceStart());
        metadata.setFilesToCopy(data.getFilesToCopy());
        metadata.setConfigXmls(data.getConfigXmls());
        // Save updated metadata
        PortletManager.getCurrentServer(request).getPluginInstaller().updatePluginMetadata(metadata);

        response.setRenderParameter("configId", configId);
        response.setRenderParameter("name", name);

        return CONFIRM_EXPORT_MODE+BEFORE_ACTION;
    }

    private URL[] splitURLs(String backups) throws MalformedURLException {
        String[] strings = split(backups);
        URL[] result = new URL[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new URL(strings[i]);
        }
        return result;
    }


    private String[] split(String deps) {
        if(deps == null || deps.equals("")) {
            return new String[0];
        }
        List list = new ArrayList();
        BufferedReader in = new BufferedReader(new StringReader(deps));
        String line;
        try {
            while((line = in.readLine()) != null) {
                line = line.trim();
                if(!line.equals("")) {
                    list.add(line);
                }
            }
            in.close();
        } catch (IOException e) {
            log.error("Unable to parse request arguments", e);
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private String combine(String[] strings) {
        if(strings == null || strings.length == 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if(i > 0) {
                buf.append("\n");
            }
            buf.append(string);
        }
        return buf.toString();
    }

    private String combine(URL[] urls) {
        if(urls == null || urls.length == 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if(i > 0) {
                buf.append("\n");
            }
            buf.append(url.toString());
        }
        return buf.toString();
    }
}
