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
import org.apache.geronimo.crypto.CaUtils;
import org.apache.geronimo.crypto.CertificateUtil;

/**
 * Handler for the CA details screen.
 *
 * @version $Rev$ $Date$
 */
public class CADetailsHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(CADetailsHandler.class);
    
    public CADetailsHandler(BasePortlet portlet) {
        super(CADETAILS_MODE, "/WEB-INF/view/ca/caDetails.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        try {
            CertificationAuthority ca = getCertificationAuthority(request);
            if(ca == null) {
                throw new Exception("CA is not running. CA may not have been initialized.");
            }
            if(ca.isLocked()) {
                request.setAttribute("caLocked", Boolean.TRUE);
                throw new Exception("CA is locked.  Unlock CA to view details.");
            }
            
            // Get CA details
            Certificate caCert = ca.getCertificate();
            request.setAttribute("cert", caCert);
            request.setAttribute("highestSerial", ca.getHighestSerialNumber());
            request.setAttribute("certText", CaUtils.base64Certificate(caCert));
            PublicKey publickey = caCert.getPublicKey();
            String keySize = null;
            if(publickey instanceof RSAPublicKey) {
                keySize = ""+((RSAPublicKey)publickey).getModulus().bitLength();
                request.setAttribute("keySize", keySize);
            }
            Map fingerPrints = new HashMap();
            fingerPrints.put("MD5", CertificateUtil.generateFingerprint(caCert, "MD5"));
            fingerPrints.put("SHA1", CertificateUtil.generateFingerprint(caCert, "SHA1"));
            request.setAttribute("fingerPrints", fingerPrints);
        } catch (Exception e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg15"), e.getMessage());
            log.error("Errors while trying to view CA Details.", e);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }
}
