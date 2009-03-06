/**
 *
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
package org.apache.geronimo.console.ca;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * Handler for the Confirm Certificate Request screen.
 *
 * @version $Rev$ $Date$
 */
public class ConfirmCertReqHandler extends BaseCAHandler {
    public ConfirmCertReqHandler(BasePortlet portlet) {
        super(CONFIRM_CERT_REQ_MODE, "/WEB-INF/view/ca/confirmCertReq.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"subject", "publickey", "requestId"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"subject", "publickey", "requestId"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String requestId = request.getParameter("requestId");
        String approve = request.getParameter("approve");
        String reject = request.getParameter("reject");
        if(approve != null) {
            getCertificateRequestStore(request).setRequestVerified(requestId);
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg16", requestId));
        } else if(reject != null) {
            getCertificateRequestStore(request).deleteRequest(requestId);
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg17", requestId));
        }
        return LIST_REQUESTS_VERIFY_MODE+BEFORE_ACTION;
    }
}
