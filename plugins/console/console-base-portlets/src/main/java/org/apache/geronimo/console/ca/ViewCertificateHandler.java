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
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
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
import org.apache.geronimo.management.geronimo.CertificationAuthority;
import org.apache.geronimo.crypto.CertificateUtil;

/**
 * Handler for view certificate screen.
 *
 * @version $Rev$ $Date$
 */
public class ViewCertificateHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewCertificateHandler.class);
    
    public ViewCertificateHandler(BasePortlet portlet) {
        super(VIEW_CERT_MODE, "/WEB-INF/view/ca/viewCertificate.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"sNo"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String sNo = request.getParameter("sNo");
        try {
            if(!request.getParameterMap().containsKey("sNo")) {
                // Show the page to get serial number of the certificate to be viewed
                request.setAttribute("sNo", null);
                return;
            }
            CertificationAuthority ca = getCertificationAuthority(request);
            
            String certText = ca.getCertificateBase64Text(new BigInteger(sNo.trim()));
            Certificate cert = ca.getCertificate(new BigInteger(sNo.trim()));
            PublicKey publickey = cert.getPublicKey();
            String keySize = null;
            if(publickey instanceof RSAPublicKey) {
                keySize = ""+((RSAPublicKey)publickey).getModulus().bitLength();
            }
            request.setAttribute("sNo", sNo);
            request.setAttribute("cert", cert);
            request.setAttribute("certText", certText);
            request.setAttribute("keySize", keySize);
            // Generate Certificate Fingerprints
            Map fingerPrints = new HashMap();
            fingerPrints.put("MD5", CertificateUtil.generateFingerprint(cert, "MD5"));
            fingerPrints.put("SHA1", CertificateUtil.generateFingerprint(cert, "SHA1"));
            request.setAttribute("fingerPrints", fingerPrints);
            // Check if the certificate issue process started from "requests to be fulfilled" page.
            // If so, provide a link to go back to that page
            if("true".equalsIgnoreCase(request.getParameter("linkToListRequests")))
                request.setAttribute("linkToListRequests", Boolean.TRUE);
        } catch (Exception e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg16", sNo), e.getMessage());
            log.error("Errors trying to view certificate with serial number '"+sNo+"'", e);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }
}
