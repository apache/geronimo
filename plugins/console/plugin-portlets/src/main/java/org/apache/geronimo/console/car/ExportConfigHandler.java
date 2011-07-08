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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PrerequisiteType;

/**
 * Handler for the screen where you configure plugin data before exporting
 *
 * @version $Rev$ $Date$
 */
public class ExportConfigHandler extends BaseImportExportHandler {
    private static final Logger log = LoggerFactory.getLogger(ExportConfigHandler.class);

    public ExportConfigHandler() {
        super(CONFIGURE_EXPORT_MODE, "/WEB-INF/view/car/pluginParams.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        if (configId != null) {
            response.setRenderParameter("configId", configId);
        }
        
        response.setWindowState(WindowState.MAXIMIZED);
        
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();
        Artifact newArtifact = Artifact.create(configId);  
        PluginType metadata = pluginInstaller.getPluginMetadata(newArtifact);  
        PluginArtifactType instance = metadata.getPluginArtifact().get(0);
        request.setAttribute("configId", PluginInstallerGBean.toArtifact(instance.getModuleId()).toString());
        request.setAttribute("name", metadata.getName());
        request.setAttribute("repository", combine(instance.getSourceRepository()));
        request.setAttribute("category", metadata.getCategory());
        request.setAttribute("url", metadata.getUrl());
        request.setAttribute("author", metadata.getAuthor());
        request.setAttribute("description", metadata.getDescription());
        List<LicenseType> licenses = metadata.getLicense();
        if (licenses != null && licenses.size() > 0) {
            request.setAttribute("license", licenses.get(0).getValue());
            if (licenses.get(0).isOsiApproved()) {
                request.setAttribute("licenseOSI", "true");
            }
            if (licenses.size() > 1) {
                log.warn(
                        "Unable to edit plugin metadata containing more than one license!  Additional license data will not be editable.");
            }
        }
        //Choose the first geronimo-versions element and set the config version element to that version number.
        List<String> gerVers = instance.getGeronimoVersion();
        if (gerVers != null && gerVers.size() > 0) {
            request.setAttribute("geronimoVersion", gerVers.get(0));
        }
        request.setAttribute("jvmVersions", combine(instance.getJvmVersion()));
        request.setAttribute("dependencies", toString(instance.getDependency()));
        request.setAttribute("obsoletes", toString(instance.getObsoletes()));
        List<PrerequisiteType> reqs = instance.getPrerequisite();
        if (reqs != null && reqs.size() > 0) {
            int i = 1;
            for (PrerequisiteType prereq: reqs) {
                String prefix = "prereq" + i;
                request.setAttribute(prefix, PluginInstallerGBean.toArtifact(prereq.getId()).toString());
                request.setAttribute(prefix + "type", prereq.getResourceType());
                request.setAttribute(prefix + "desc", prereq.getDescription());
            }
            if (reqs.size() > 3) {
                log.warn("Unable to edit plugin metadata containing more than three prerequisites!  Additional prereqs will not be editable.");
            }
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();
        PluginType metadata = pluginInstaller.getPluginMetadata(Artifact.create(configId));
        PluginArtifactType instance = metadata.getPluginArtifact().get(0);

        String name = request.getParameter("name");
        metadata.setName(name);
        metadata.setCategory(request.getParameter("category"));
        metadata.setUrl(request.getParameter("url"));
        metadata.setAuthor(request.getParameter("author"));
        metadata.setDescription(request.getParameter("description"));
        String licenseString = request.getParameter("license");
        String osi = request.getParameter("licenseOSI");
        List<LicenseType> licenses = metadata.getLicense();
        if (!licenses.isEmpty()) {
            licenses.remove(0);
        }
        if (licenseString != null && !licenseString.trim().equals("")) {
            LicenseType license = new LicenseType();
            license.setValue(licenseString.trim());
            license.setOsiApproved(osi != null && !osi.equals(""));
            licenses.add(0, license);
        }

        String jvmsString = request.getParameter("jvmVersions");
        split(jvmsString, instance.getJvmVersion());

        String deps = request.getParameter("dependencies");
        toDependencies(split(deps), instance.getDependency());

        String obsoletes = request.getParameter("obsoletes");
        toArtifacts(split(obsoletes), instance.getObsoletes());

        String repo = request.getParameter("repository");
        split(repo, instance.getSourceRepository());

        //TODO this is wrong, we are only supplying one version to the UI
        String version = request.getParameter("geronimoVersion");
        split(version, instance.getGeronimoVersion());

        List<PrerequisiteType> prereqs = instance.getPrerequisite();
        //TODO this is probably wrong if # of prereqs is changed.
        for (int i = 0; i < 3 && !prereqs.isEmpty(); i++) {
            prereqs.remove(0);
        }
        int counter = 1;
        while (true) {
            String prefix = "prereq" + counter;
            String id = request.getParameter(prefix);
            if (id == null || id.trim().equals("")) {
                break;
            }
            String type = request.getParameter(prefix + "type");
            String desc = request.getParameter(prefix + "desc");
            if (type != null && type.trim().equals("")) {
                type = null;
            }
            if (desc != null && desc.trim().equals("")) {
                desc = null;
            }
            PrerequisiteType prereq = new PrerequisiteType();
            prereq.setResourceType(type);
            prereq.setDescription(desc);
            prereq.setId(PluginInstallerGBean.toArtifactType(Artifact.create(id)));
            prereqs.add(counter - 1, prereq);
            counter++;
        }

        // Save updated metadata
        pluginInstaller.updatePluginMetadata(metadata);

        response.setRenderParameter("configId", configId);
        response.setRenderParameter("name", name);

        return CONFIRM_EXPORT_MODE + BEFORE_ACTION;
    }

    private List<String> split(String deps) {
        List<String> split = new ArrayList<String>();
        if (deps != null && !deps.equals("")) {
            split(deps, split);
        }
        return split;
    }

    private void split(String deps, List<String> split) {
        split.clear();
        BufferedReader in = new BufferedReader(new StringReader(deps));
        String line;
        try {
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                    split.add(line);
                }
            }
            in.close();
        } catch (IOException e) {
            log.error("Unable to parse request arguments", e);
        }
    }

    private String combine(List<String> strings) {
        if (strings == null || strings.size() == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (String string : strings) {
            if (!first) {
                buf.append("\n");
            }
            first = false;
            buf.append(string);
        }
        return buf.toString();
    }

    private void toArtifacts(List<String> artifacts, List<ArtifactType> result) {
        result.clear();
        for (String artifact : artifacts) {
            result.add(PluginInstallerGBean.toArtifactType(Artifact.create(artifact)));
        }
    }
    private void toDependencies(List<String> artifacts, List<DependencyType> result) {
        result.clear();
        for (String artifact : artifacts) {
            //TODO this is wrong.... need to encode import type as well
            result.add(PluginInstallerGBean.toDependencyType(new Dependency(Artifact.create(artifact), ImportType.ALL), true));
        }
    }

    private String toString(List<? extends ArtifactType> artifacts) {
        if (artifacts == null || artifacts.size() == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (ArtifactType artifactType : artifacts) {
            if (!first) {
                buf.append("\n");
            }
            first = false;
            buf.append(PluginInstallerGBean.toArtifact(artifactType).toString());
        }
        return buf.toString();
     }


}
