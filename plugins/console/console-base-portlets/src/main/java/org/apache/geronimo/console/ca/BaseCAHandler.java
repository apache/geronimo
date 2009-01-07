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

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.CertificateRequestStore;
import org.apache.geronimo.management.geronimo.CertificateStore;
import org.apache.geronimo.management.geronimo.CertificationAuthority;
import org.apache.geronimo.management.geronimo.KeystoreException;
import org.apache.geronimo.management.geronimo.KeystoreInstance;

/**
 * The base class for all handlers for CA portlet
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseCAHandler extends MultiPageAbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseCAHandler.class);

    protected static final String INDEX_MODE = "index";
    protected static final String SETUPCA_MODE = "setupCA";
    protected static final String CONFIRM_CA_MODE = "confirmCA";
    protected static final String CADETAILS_MODE = "caDetails";
    protected static final String UNLOCKCA_MODE = "unlockCA";
    protected static final String PROCESS_CSR_MODE = "processCSR";
    protected static final String CERT_REQ_DETAILS_MODE = "certReqDetails";
    protected static final String CONFIRM_CLIENT_CERT_MODE = "confirmClientCert";
    protected static final String VIEW_CERT_MODE = "viewCert";
    protected static final String LIST_REQUESTS_ISSUE_MODE = "listRequestsIssue";
    protected static final String LIST_REQUESTS_VERIFY_MODE = "listRequestsVerify";
    protected static final String CONFIRM_CERT_REQ_MODE = "confirmCertReq";
    
    // Key algorithm for CA's keypair
    protected static final String defaultKeyAlgorithm = "RSA";
    // CA's private key and self-signed certificate is stored under this keystore created using KeystoreManager
    // Using FileKeystoreManager, the file willbe <server-base-dir>/var/security/keystores/<defaultCAKeystore>
    protected static final String defaultCAKeystore = "ca-keystore";
    // CA's certificate store directory
    protected static final String defaultCAStoreDir = "var/security/ca/certs";
    // Certificate request store directory
    protected static final String defaultCSRStoreDir = "var/security/ca/requests";

    /**
     * Constructor
     */
    protected BaseCAHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    protected BaseCAHandler(String mode, String viewName, BasePortlet portlet) {
        super(mode, viewName, portlet);
    }

    public final static class CAModel implements MultiPageModel {
        public CAModel(PortletRequest request) {
        }

        public void save(ActionResponse response, PortletSession session) {
        }
    }
    
    /**
     * This method returns CertificationAuthority GBbean.
     * @param request PortletRequest to execute retrieve GBean
     * @return  null if a CA GBean is not running.
     */
    protected CertificationAuthority getCertificationAuthority(PortletRequest request) {
        Object[] cas = PortletManager.getManagementHelper(request).getGBeansImplementing(CertificationAuthority.class);
        return (CertificationAuthority)(cas != null && cas.length > 0 ? cas[0] : null);
    }

    /**
     * This methods creates CA's keystore using KeystoreManager.
     * @param request PortletRequest to get KeystoreManager
     * @param password Password for newly created Keystore
     * @throws KeystoreException 
     */
    protected KeystoreInstance createCAKeystoreInstance(PortletRequest request, String password, String type) throws KeystoreException {
        return PortletManager.getCurrentServer(request).getKeystoreManager().createKeystore(defaultCAKeystore, password.toCharArray(), type);
    }
    
    /**
     * This method returns CertificateRequestStore GBean.
     * @param request PortletRequest to execute retrieve GBean
     * @return  null if a CertificateRequestStore GBean is not running.
     */
    protected CertificateRequestStore getCertificateRequestStore(PortletRequest request) {
        Object[] crs = PortletManager.getManagementHelper(request).getGBeansImplementing(CertificateRequestStore.class);
        return (CertificateRequestStore)(crs != null && crs.length > 0 ? crs[0] : null);
    }

    /**
     * This method returns CertificateStore GBean.
     * @param request PortletRequest to execute retrieve GBean
     * @return  null if a CertificateStore GBean is not running.
     */
    protected CertificateStore getCertificateStore(PortletRequest request) {
        Object[] cs = PortletManager.getManagementHelper(request).getGBeansImplementing(CertificateStore.class);
        return (CertificateStore)(cs != null && cs.length > 0 ? cs[0] : null);
    }
}
