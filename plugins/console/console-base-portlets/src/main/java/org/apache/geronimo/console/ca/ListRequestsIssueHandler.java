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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.CertificateRequestStore;
import org.apache.geronimo.crypto.CaUtils;
import org.apache.geronimo.crypto.asn1.x509.X509Name;

/**
 * Handler for "Requests to be fulfilled" screen.
 *
 * @version $Rev$ $Date$
 */
public class ListRequestsIssueHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(ListRequestsIssueHandler.class);
    
    public ListRequestsIssueHandler() {
        super(LIST_REQUESTS_ISSUE_MODE, "/WEB-INF/view/ca/listRequestsIssue.jsp");
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
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
        CertificateRequestStore csrStore = getCertificateRequestStore(request);
        String[] csrIds = csrStore.getVerifiedRequestIds();
        request.setAttribute("csrIds", csrIds);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String errorMsg = null;
        String requestId = request.getParameter("requestId");
        try {
            response.setRenderParameter("requestId", requestId);
            // Retrieve the request info based on the requestId
            String certreq = getCertificateRequestStore(request).getRequest(requestId);
            if(certreq.startsWith(CaUtils.CERT_REQ_HEADER)) {
                // This is a PKCS 10 Request
                Map certReqMap = CaUtils.processPKCS10Request(certreq);
                // Set the subject and publickey values to be displayed in subsequent screens
                response.setRenderParameter("subject", certReqMap.get(CaUtils.CERT_REQ_SUBJECT).toString());
                response.setRenderParameter("publickey", certReqMap.get(CaUtils.CERT_REQ_PUBLICKEY_OBJ).toString());
            } else {
                // This is a custom request containing SPKAC and X509Name attributes received through web browser
                Properties csrProps = new Properties();
                csrProps.load(new ByteArrayInputStream(certreq.getBytes()));
                String spkac = csrProps.getProperty("SPKAC");
                String cn = csrProps.getProperty("CN");
                String ou = csrProps.getProperty("OU");
                String o = csrProps.getProperty("O");
                String l = csrProps.getProperty("L");
                String st = csrProps.getProperty("ST");
                String c = csrProps.getProperty("C");
                X509Name subject = CaUtils.getX509Name(cn, ou, o, l, st, c);
                Map certReqMap = CaUtils.processSPKAC(spkac);
                // Set the subject and publickey values to be displayed in subsequent screens
                response.setRenderParameter("subject", subject.toString());
                response.setRenderParameter("publickey", certReqMap.get(CaUtils.CERT_REQ_PUBLICKEY_OBJ).toString());
            }
            return CERT_REQ_DETAILS_MODE+BEFORE_ACTION;
        } catch(Exception e) {
            errorMsg = e.toString();
            log.error("Errors while processing a Certificate Request. id="+requestId, e);
        }
        response.setRenderParameter(ERROR_MSG, errorMsg);
        return getMode()+BEFORE_ACTION;
    }
}
