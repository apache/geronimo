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
package org.apache.geronimo.console.keystores;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * Handler for entering a password to unlock a keystore
 *
 * @version $Rev$ $Date$
 */
public class ViewKeystoreHandler extends BaseKeystoreHandler {
    public ViewKeystoreHandler(BasePortlet portlet) {
        super(VIEW_KEYSTORE, "/WEB-INF/view/keystore/viewKeystore.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"id"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"id"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
        String id = request.getParameter("id");
        request.setAttribute("keystore", request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + id));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return LIST_MODE+BEFORE_ACTION;
    }
}
