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
import java.net.URI;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.management.geronimo.CertificationAuthority;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.security.ca.FileCertificateRequestStore;
import org.apache.geronimo.security.ca.FileCertificateStore;
import org.apache.geronimo.security.ca.GeronimoCertificationAuthority;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.crypto.KeystoreUtil;

/**
 * Handler for the CA confirmation screen.
 *
 * @version $Rev$ $Date$
 */
public class ConfirmCAHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(ConfirmCAHandler.class);
    
    public ConfirmCAHandler(BasePortlet portlet) {
        super(CONFIRM_CA_MODE, "/WEB-INF/view/ca/confirmCA.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"caCN", "caOU", "caO", "caL", "caST", "caC", "alias", "keyAlgorithm", "keySize", "algorithm", "validFrom", "validTo", "sNo", "password"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"caCN", "caOU", "caO", "caL", "caST", "caC", "alias", "keyAlgorithm", "keySize", "algorithm", "validFrom", "validTo", "sNo", "password"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String caCN = request.getParameter("caCN");
        String caOU = request.getParameter("caOU");
        String caO = request.getParameter("caO");
        String caL = request.getParameter("caL");
        String caST = request.getParameter("caST");
        String caC = request.getParameter("caC");
        String alias = request.getParameter("alias");
        String password = request.getParameter("password");
        String keyAlgorithm = request.getParameter("keyAlgorithm");
        String keySize = request.getParameter("keySize");
        String algorithm = request.getParameter("algorithm");
        String validFrom = request.getParameter("validFrom");
        String validTo = request.getParameter("validTo");
        String sNo = request.getParameter("sNo");
        
        try {
            // Generate keypair
            // Check if the key algorithm is same as defaultKeyAlgorithm (which is "RSA")
            if(!defaultKeyAlgorithm.equalsIgnoreCase(keyAlgorithm)) {
                throw new Exception("Key Algorithm '"+keyAlgorithm+"' is not supported.");
            }
            // Create a KeystoreInstance and generate keypair
            KeystoreInstance caKeystore = createCAKeystoreInstance(request, password, KeystoreUtil.defaultType);
            caKeystore.unlockKeystore(password.toCharArray());
            caKeystore.generateKeyPair(alias, password.toCharArray(), password.toCharArray(), keyAlgorithm, Integer.parseInt(keySize),
                    algorithm, 365, caCN, caOU, caO, caL, caST, caC);
            caKeystore.unlockPrivateKey(alias, password.toCharArray(), password.toCharArray());
            
            // Create CertificationAuthority, CertificateStore and CertificateRequestStore GBeans
            createCARelatedGBeans(request, (GeronimoManagedBean)caKeystore, defaultCAStoreDir, defaultCSRStoreDir);

            CertificationAuthority ca = getCertificationAuthority(request);
            ca.unlock(password.toCharArray());

            // Certificate validity and serial number.
            // Validity of these have been checked before loading the confirmation page.
            Date validFromDate = null, validToDate = null;
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            validFromDate = df.parse(validFrom);
            validToDate = df.parse(validTo);
            BigInteger serialNum = new BigInteger(sNo);
            
            // Instruct the CA to issue a self-signed certificate.
            ca.issueOwnCertificate(serialNum, validFromDate, validToDate, algorithm);
            // Publish the CA's certificate to CertificateStore.
            getCertificateStore(request).storeCACertificate(ca.getCertificate());
            
            // CA Setup is succeessful.
            // Load a page to show CA details.
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg15"));
            log.info("CA Setup is successful.");
            
            return CADETAILS_MODE+BEFORE_ACTION;
        } catch(Exception e) {
            // An error occurred.  Go back to CA details entry page so that user can correct the errors.
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg07"), e.getMessage());
            log.error("Errors in CA Setup process.", e);
        }

        return SETUPCA_MODE+BEFORE_ACTION;
    }
    
    /**
     * This method creates CerificationAuthority, CertificateStore and CertificateRequestStore GBeans.  The GBeans are
     * created and added to the same configuration containing the caKeystore GBean.
     * @param request PortletRequest to execute any kernel api's
     * @param caKeystore Keystore to be used by the CA
     * @param certStorePath Path for CertificateStore directory.  Note: This CA uses FileCertificateStore
     * @param certReqStorePath Path for CertificateRequestStore directory: Note: This CA uses FileCertificateRequestStore
     */
    private void createCARelatedGBeans(PortletRequest request, GeronimoManagedBean caKeystore, String certStorePath, String certReqStorePath) {
        // Get hold of configuration containing caKeystore GBean
        AbstractName caKeystoreName = PortletManager.getNameFor(request, caKeystore);
        Artifact configurationId =  PortletManager.getConfigurationFor(request, caKeystoreName);
        ServerInfo serverInfo = PortletManager.getCurrentServer(request).getServerInfo();
        AbstractName serverInfoName = PortletManager.getNameFor(request, serverInfo);
        Naming naming = PortletManager.getManagementHelper(request).getNaming();
        
        // Add a CertificateStore GBean
        AbstractName certStoreName = naming.createSiblingName(caKeystoreName, "geronimo-ca-cert-store", SecurityNames.CERTIFICATE_STORE);
        GBeanData certStore = new GBeanData(certStoreName, FileCertificateStore.GBEAN_INFO);
        certStore.setAttribute("directoryPath", URI.create(certStorePath));
        certStore.setReferencePattern("ServerInfo", serverInfoName);
        PortletManager.addGBeanToConfiguration(request, configurationId, certStore, true);
        
        // Add a CertificateRequestStore GBean
        AbstractName certReqStoreName = naming.createSiblingName(caKeystoreName, "geronimo-ca-cert-req-store", SecurityNames.CERTIFICATE_REQUEST_STORE);
        GBeanData certReqStore = new GBeanData(certReqStoreName, FileCertificateRequestStore.GBEAN_INFO);
        certReqStore.setAttribute("directoryPath", URI.create(certReqStorePath));
        certReqStore.setReferencePattern("ServerInfo", serverInfoName);
        PortletManager.addGBeanToConfiguration(request, configurationId, certReqStore, true);
        
        // Add a CertificationAuthority GBean
        AbstractName caName = naming.createSiblingName(caKeystoreName, "geronimo-ca", SecurityNames.CERTIFICATION_AUTHORITY);
        GBeanData ca = new GBeanData(caName, GeronimoCertificationAuthority.GBEAN_INFO);
        ca.setReferencePattern("ServerInfo", serverInfoName);
        ca.setReferencePattern("KeystoreInstance", caKeystoreName);
        ca.setReferencePattern("CertificateStore", certStoreName);
        ca.setReferencePattern("CertificateRequestStore", certReqStoreName);
        PortletManager.addGBeanToConfiguration(request, configurationId, ca, true);
    }
}
