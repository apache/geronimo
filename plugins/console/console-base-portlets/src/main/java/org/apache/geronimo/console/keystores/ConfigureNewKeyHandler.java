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

import org.apache.geronimo.console.MultiPageModel;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * Handler for collecting the settings necessary to generate a new private key.
 *
 * @version $Rev$ $Date$
 */
public class ConfigureNewKeyHandler extends BaseKeystoreHandler {
    public ConfigureNewKeyHandler() {
        super(CONFIGURE_KEY, "/WEB-INF/view/keystore/configureKey.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String id = request.getParameter("keystore");
        if(id != null) {
            response.setRenderParameter("keystore", id);
        } // else we hope this is after a failure and the actionAfterView took care of it below!
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        request.setAttribute("keystore", request.getParameter("keystore"));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        String alias = request.getParameter("alias");
        String password = request.getParameter("password");
        String keySize = request.getParameter("keySize");
        String algorithm = request.getParameter("algorithm");
        String valid = request.getParameter("valid");
        String certCN = request.getParameter("certCN");
        String certOU = request.getParameter("certOU");
        String certO = request.getParameter("certO");
        String certL = request.getParameter("certL");
        String certST = request.getParameter("certST");
        String certC = request.getParameter("certC");

        //todo: validation

        response.setRenderParameter("keystore", keystore);
        response.setRenderParameter("alias", alias);
        response.setRenderParameter("password", password);
        response.setRenderParameter("keySize", keySize);
        response.setRenderParameter("algorithm", algorithm);
        response.setRenderParameter("valid", valid);
        response.setRenderParameter("certCN", certCN);
        response.setRenderParameter("certOU", certOU);
        response.setRenderParameter("certO", certO);
        response.setRenderParameter("certL", certL);
        response.setRenderParameter("certST", certST);
        response.setRenderParameter("certC", certC);

        return CONFIRM_KEY+BEFORE_ACTION;
    }
}
