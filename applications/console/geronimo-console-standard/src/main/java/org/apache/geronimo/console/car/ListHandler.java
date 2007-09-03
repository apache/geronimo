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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the import export list screen.
 *
 * @version $Rev$ $Date$
 */
public class ListHandler extends BaseImportExportHandler {

    public ListHandler() {
        super(LIST_MODE, "/WEB-INF/view/car/list.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = (String) request.getAttribute("repository");
        if(repository == null || repository.equals("")) {
            return INDEX_MODE+BEFORE_ACTION;
        }
        response.setRenderParameter("repository", repository);
        String user = (String) request.getAttribute("repo-user");
        String pass = (String) request.getAttribute("repo-pass");
        if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        if(!loadFromRepository(request, repository, user, pass)) {
            //todo: loading failed -- do something!
        }
        request.setAttribute("repository", repository);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }

    private boolean loadFromRepository(RenderRequest request, String repository, String username, String password) throws IOException, PortletException {
        PluginListType data;
        try {
            data = PortletManager.getCurrentServer(request).getPluginInstaller().listPlugins(new URL(repository), username, password);
        } catch (FailedLoginException e) {
            throw new PortletException("Invalid login for Maven repository '"+repository+"'", e);
        }
        Map<String, List<PluginType>> results = new HashMap<String, List<PluginType>>();
        if(data == null || data.getPlugin() == null) {
            return false;
        }
        for (PluginType metadata: data.getPlugin()) {
            List<PluginType> values = results.get(metadata.getCategory());
            if(values == null) {
                values = new ArrayList<PluginType>();
                results.put(metadata.getCategory(), values);
            }
            values.add(metadata);
        }
        Collection<List<PluginType>> values = results.values();
        for (List<PluginType> value : values) {
            Collections.sort(value, new Comparator<PluginType>() {

                public int compare(PluginType o1, PluginType o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        request.setAttribute("categories", results);
        request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, data);
        return true;
    }
}
