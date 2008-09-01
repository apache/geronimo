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
package org.apache.geronimo.console.apache.jk;

import java.io.IOException;

import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPagePortlet;
import org.apache.geronimo.console.apache.jk.BaseApacheHandler.ApacheModel;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

/**
 * Portlet that helps you configure Geronimo for Apache 2 with mod_jk
 *
 * @version $Rev$ $Date$
 */
public class ApacheConfigPortlet extends MultiPagePortlet {
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new IndexHandler(), config);
        addHelper(new ConfigHandler(), config);
        addHelper(new AJPHandler(), config);
        addHelper(new WebAppHandler(), config);
        addHelper(new ResultsHandler(), config);
    }

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if(WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String mode = renderRequest.getParameter(MODE_KEY);
        ApacheModel model = getModel(renderRequest);
        if(mode == null || mode.equals("")) {
            mode = getDefaultMode();
        }
        MultiPageAbstractHandler handler = helpers.get(mode);
        try {
            if(handler != null) {
                handler.renderView(renderRequest, renderResponse, model);
            }
        } catch (Throwable e) {
            
        }
        // decode the paths in model object
        model.setLogFilePath(model.getLogFilePath());
        model.setWorkersPath(model.getWorkersPath());
        renderRequest.setAttribute(getModelJSPVariableName(), model);
        if(handler != null) {
            handler.getView().include(renderRequest, renderResponse);
        }
    }

    protected String getModelJSPVariableName() {
        return "model";
    }

    protected ApacheModel getModel(PortletRequest request) {
        return new BaseApacheHandler.ApacheModel(request);
    }
}
