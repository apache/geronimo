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

package org.apache.geronimo.console.artifact;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;

public class ArtifactViewPortlet extends BasePortlet {
    private Kernel kernel;
    private static final Logger log = LoggerFactory.getLogger(ArtifactViewPortlet.class);
    private static final String LIST_VIEW = "/WEB-INF/view/artifact/list.jsp";
    private static final String EDIT_VIEW = "/WEB-INF/view/artifact/edit.jsp";
    private static final String LIST_MODE = "list";
    private static final String EDIT_MODE = "edit";
    private static final String EDITING_MODE = "editing";
    private static final String REMOVE_MODE = "remove";
    private static final String MODE_KEY = "mode";
    private PortletRequestDispatcher listView;
    private PortletRequestDispatcher editView;
    private ExplicitDefaultArtifactResolver instance;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        try {
            instance = kernel.getGBean(ExplicitDefaultArtifactResolver.class);
        } catch (GBeanNotFoundException e) {
            throw new PortletException(e);
        } catch (InternalKernelException e) {
            throw new PortletException(e);
        } catch (IllegalStateException e) {
            throw new PortletException(e);
        }
        listView = portletConfig.getPortletContext().getRequestDispatcher(LIST_VIEW);
        editView = portletConfig.getPortletContext().getRequestDispatcher(EDIT_VIEW);
    }

    public void destroy() {
        listView = null;
        editView = null;
        super.destroy();
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter(MODE_KEY);
        if (mode == null) {
            mode = "";
        }
        actionResponse.setRenderParameter(MODE_KEY, mode);
        String name = actionRequest.getParameter("name");
        if (name != null) {
            actionResponse.setRenderParameter("name", name);
        }
        String aliases = actionRequest.getParameter("aliases");
        if (aliases != null) {
            actionResponse.setRenderParameter("aliases", aliases);
        }
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        try {
            String mode = renderRequest.getParameter(MODE_KEY);
            if (mode == null || mode.equals("")) {
                mode = LIST_MODE;
            }
            if (mode.equals(LIST_MODE) && renderRequest.getParameter("name") != null) {
                mode = EDITING_MODE;
            }

            if (mode.equals(LIST_MODE)) {
                renderList(renderRequest, renderResponse);
            } else if (mode.equals(EDIT_MODE)) {
                AliasesData data = new AliasesData(renderRequest.getParameter("name"), renderRequest.getParameter("aliases"));
                renderEdit(renderRequest, renderResponse, data);
            } else if (mode.equals(REMOVE_MODE)) {
                AliasesData data = new AliasesData(renderRequest.getParameter("name"), renderRequest.getParameter("aliases"));
                renderRemove(renderRequest, renderResponse, data);
            } else if (mode.equals(EDITING_MODE)) {
                AliasesData data = new AliasesData(renderRequest.getParameter("name"), renderRequest.getParameter("aliases"));
                renderEditing(renderRequest, renderResponse, data);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
    }

    private void renderList(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        populateList(renderRequest);
        listView.include(renderRequest, renderResponse);
    }

    private void populateList(PortletRequest renderRequest) {
        List<AliasesData> list = new ArrayList<AliasesData>();
        Properties set = instance.getProperties();
        for (Map.Entry<Object, Object> entry : set.entrySet()) {
            String key = (String) entry.getKey();
            String resolvedString = (String) entry.getValue();
            list.add(new AliasesData(key, resolvedString));
        }
        Collections.sort(list);
        renderRequest.setAttribute("AliasesDatas", list);
    }

    private void renderEdit(RenderRequest renderRequest, RenderResponse renderResponse, AliasesData data) throws IOException, PortletException {
        renderRequest.setAttribute("AliasesData", data);
        editView.include(renderRequest, renderResponse);
    }

    private void renderRemove(RenderRequest renderRequest, RenderResponse renderResponse, AliasesData data) throws IOException, PortletException {
        Properties set = new Properties();
        if (data.name != null && data.aliases != null) {
            set.put(data.name, data.aliases);
            instance.removeAliases(set);
        }
        populateList(renderRequest);
        listView.include(renderRequest, renderResponse);
    }

    private void renderEditing(RenderRequest renderRequest, RenderResponse renderResponse, AliasesData data) throws IOException, PortletException {
        if (data.name != null && data.aliases != null) {
            instance.addAliases(Collections.singletonMap(data.name, data.aliases));
        }
        populateList(renderRequest);
        listView.include(renderRequest, renderResponse);
    }

    public static class AliasesData implements Serializable, Comparable<AliasesData> {
        private static final long serialVersionUID = 1L;
        private String name;
        private String aliases;

        public AliasesData() {

        }

        public void load(String name, String aliases) {
            this.name = name;
            this.aliases = aliases;
        }

        public AliasesData(String name, String aliases) {
            this.name = name;
            this.aliases = aliases;
        }

        public String getName() {
            return name;
        }

        public String getAliases() {
            return aliases;
        }

        public int compareTo(AliasesData aliasesData) {
            int val = name.compareTo(aliasesData.name);
            if (val != 0) {
                return val;
            }
            return aliases.compareTo(aliasesData.aliases);
        }
    }
}
