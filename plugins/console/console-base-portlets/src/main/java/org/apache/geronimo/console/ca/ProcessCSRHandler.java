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
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.crypto.CaUtils;

/**
 * Handler for process CSR screen.
 *
 * @version $Rev$ $Date$
 */
public class ProcessCSRHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(ProcessCSRHandler.class);
    
    public ProcessCSRHandler(BasePortlet portlet) {
        super(PROCESS_CSR_MODE, "/WEB-INF/view/ca/processCSR.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        try {
            // Process the PKCS10 Certificate Request
            String pkcs10certreq = request.getParameter("pkcs10certreq");
            Map certReqMap = CaUtils.processPKCS10Request(pkcs10certreq);
            response.setRenderParameter("pkcs10certreq", pkcs10certreq);
            // Set the subject and publickey values to be shown in subsequent screens
            response.setRenderParameter("subject", certReqMap.get(CaUtils.CERT_REQ_SUBJECT).toString());
            response.setRenderParameter("publickey", certReqMap.get(CaUtils.CERT_REQ_PUBLICKEY_OBJ).toString());
            return CERT_REQ_DETAILS_MODE+BEFORE_ACTION;
        } catch(Exception e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg20"), e.getMessage());
            log.error("Errors while processing a CSR.", e);
        }
        return getMode()+BEFORE_ACTION;
    }
}
