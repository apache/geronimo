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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.crypto.CaUtils;

/**
 * Handler for process CSR screen.
 *
 * @version $Rev$ $Date$
 */
public class ProcessCSRHandler extends BaseCAHandler {
    private final static Log log = LogFactory.getLog(ProcessCSRHandler.class);
    public ProcessCSRHandler() {
        super(PROCESS_CSR_MODE, "/WEB-INF/view/ca/processCSR.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            Object value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String errorMsg = null;
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
            errorMsg = e.toString();
            log.error("Errors while processing a CSR.", e);
        }
        response.setRenderParameter(ERROR_MSG, errorMsg);
        return getMode()+BEFORE_ACTION;
    }
}
