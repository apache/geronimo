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
import java.math.BigInteger;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.CertificationAuthority;
import org.apache.geronimo.crypto.CaUtils;
import org.apache.geronimo.crypto.asn1.x509.X509Name;

/**
 * Handler for Confirm Client Certificate Issue screen.
 *
 * @version $Rev$ $Date$
 */
public class ConfirmClientCertHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(ConfirmClientCertHandler.class);
    
    public ConfirmClientCertHandler(BasePortlet portlet) {
        super(CONFIRM_CLIENT_CERT_MODE, "/WEB-INF/view/ca/confirmClientCert.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"subject", "publickey", "algorithm", "validFrom", "validTo", "sNo", "pkcs10certreq", "requestId"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"subject", "publickey", "algorithm", "validFrom", "validTo", "sNo", "pkcs10certreq", "requestId"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        try {
            CertificationAuthority ca = getCertificationAuthority(request);
            if(ca == null) {
                throw new Exception("CA is not running.  CA may not have been initialized!!");
            }
            BigInteger sNo = new BigInteger(request.getParameter("sNo"));
            if(ca.isCertificateIssued(sNo)) {
                // A certificate with the serial number has already been issued.
                // This may happen if the user clicks on "Issue Certificate" button a second time
                log.warn("Second request to issue certificate with serial number'"+sNo+"'.  A certificate has already been issued.");
                response.setRenderParameter("sNo", sNo.toString());
                portlet.addWarningMessage(request, portlet.getLocalizedString(request, "consolebase.warnMsg06", sNo));
                return VIEW_CERT_MODE;
            }

            X509Name subject = null;
            PublicKey publickey = null;
            // Process the CSR text to get subject details
            String pkcs10certreq = null, certreq = null;
            String challenge = null;
            String requestId = request.getParameter("requestId");
            if(requestId != null && !requestId.equals("")) {
                // Certificate request is being processed using a previously stored request in CertificateRequestStore
                String certreqText = getCertificateRequestStore(request).getRequest(requestId);
                if(certreqText.startsWith(CaUtils.CERT_REQ_HEADER)) {
                    // A PKCS 10 Certificate Request
                    pkcs10certreq = certreqText;
                } else {
                    // Possibly a CSR received through web browser
                    certreq = certreqText;
                }
            } else {
                // No request id is found.  Get the PKCS10 request submitted through form input
                pkcs10certreq = request.getParameter("pkcs10certreq");
            }
            
            if(pkcs10certreq != null && !"".equals(pkcs10certreq)) {
                // Process PKCS 10 Certificate Request text to get Subject name and public-key
                Map certReqMap = CaUtils.processPKCS10Request(pkcs10certreq);
                subject = (X509Name) certReqMap.get(CaUtils.CERT_REQ_SUBJECT);
                publickey = (PublicKey) certReqMap.get(CaUtils.CERT_REQ_PUBLICKEY_OBJ);
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
                subject = CaUtils.getX509Name(cn, ou, o, l, st, c);
                Map certReqMap = CaUtils.processSPKAC(spkac);
                publickey = (PublicKey) certReqMap.get(CaUtils.CERT_REQ_PUBLICKEY_OBJ);
                challenge = (String) certReqMap.get(CaUtils.PKAC_CHALLENGE);
            }

            // Dates have already been validated in the previous screen
            String validFrom = request.getParameter("validFrom");
            String validTo = request.getParameter("validTo");
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date validFromDate = df.parse(validFrom);
            Date validToDate = df.parse(validTo);
            String algorithm = request.getParameter("algorithm");
            // Issue certificate
            ca.issueCertificate(new X500Principal(subject.getEncoded()), publickey, sNo, validFromDate, validToDate, algorithm);
            // Store the challenge phrase against the issued certificate serial number
            if(challenge != null && !challenge.equals("")) {
                getCertificateStore(request).setCertificateChallenge(sNo, challenge);
            }
            
            if(requestId != null && !requestId.equals("")) {
                // This request was processed using a requestId from CertificateRequestStore.  Delete the fulfilled request.
                getCertificateRequestStore(request).setRequestFulfilled(requestId, sNo);
                // The confirmation page will show a link to the "Requests to be fulfilled" page.
                response.setRenderParameter("linkToListRequests", "true");
            }

            // Set the serial number and forward to view certificate page
            response.setRenderParameter("sNo", sNo.toString());
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg18", sNo));
            log.info("Certificate with serial number '"+sNo+"' issued to "+subject);
            return VIEW_CERT_MODE;
        } catch(Exception e) {
            // An error occurred.  Go back to previous screen to let the user correct the errors.
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg23"), e.getMessage());
            log.error("Errors in issuing certificate.", e);
        }
        return CERT_REQ_DETAILS_MODE+BEFORE_ACTION;
    }
}
