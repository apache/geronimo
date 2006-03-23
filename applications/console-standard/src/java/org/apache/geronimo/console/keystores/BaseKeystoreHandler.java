/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.security.keystore.KeystoreInstance;
import org.apache.geronimo.util.CertificateUtil;

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The base class for all handlers for this portlet
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public abstract class BaseKeystoreHandler extends MultiPageAbstractHandler {
    private final static Log log = LogFactory.getLog(BaseKeystoreHandler.class);
    protected static final String KEYSTORE_DATA_PREFIX="org.apache.geronimo.keystore.";
    protected static final String LIST_MODE = "list";
    protected static final String UNLOCK_KEYSTORE_FOR_EDITING = "unlockEdit";
    protected static final String UNLOCK_KEYSTORE_FOR_USAGE = "unlockKeystore";
    protected static final String LOCK_KEYSTORE_FOR_EDITING = "lockEdit";
    protected static final String LOCK_KEYSTORE_FOR_USAGE = "lockKeystore";
    protected static final String CREATE_KEYSTORE = "createKeystore";
    protected static final String VIEW_KEYSTORE = "viewKeystore";
    protected static final String UPLOAD_CERTIFICATE = "uploadCertificate";
    protected static final String CONFIRM_CERTIFICATE = "confirmCertificate";
    protected static final String CONFIGURE_KEY = "configureKey";
    protected static final String CONFIRM_KEY = "confirmKey";

    protected BaseKeystoreHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    public final static class KeystoreModel implements MultiPageModel {
        public KeystoreModel(PortletRequest request) {
        }

        public void save(ActionResponse response) {
        }
    }

    public final static class KeystoreData implements Serializable {
        private KeystoreInstance instance;
        private char[] password;
        private String[] certificates;
        private String[] keys;
        private Map fingerprints;

        public KeystoreInstance getInstance() {
            return instance;
        }

        public void setInstance(KeystoreInstance instance) {
            this.instance = instance;
        }

        public void setPassword(char[] password) {
            this.password = password;
        }

        public boolean isLocked() {
            return password == null;
        }

        public String[] getCertificates() {
            return certificates;
        }

        public void setCertificates(String[] certificates) {
            this.certificates = certificates;
        }

        public String[] getKeys() {
            return keys;
        }

        public void setKeys(String[] keys) {
            this.keys = keys;
        }

        public Map getFingerprints() {
            if(fingerprints == null) {
                fingerprints = new HashMap();
                for (int i = 0; i < certificates.length; i++) {
                    String alias = certificates[i];
                    try {
                        fingerprints.put(alias, CertificateUtil.generateFingerprint(instance.getCertificate(alias, password), "MD5"));
                    } catch (Exception e) {
                        log.error("Unable to generate certificate fingerprint", e);
                    }
                }
                for (int i = 0; i < keys.length; i++) {
                    String alias = keys[i];
                    try {
                        fingerprints.put(alias, CertificateUtil.generateFingerprint(instance.getCertificate(alias, password), "MD5"));
                    } catch (Exception e) {
                        log.error("Unable to generate certificate fingerprint", e);
                    }
                }
            }
            return fingerprints;
        }

        public boolean importTrustCert(String fileName, String alias) throws FileNotFoundException, CertificateException {
            InputStream is = new FileInputStream(fileName);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection certs = cf.generateCertificates(is);
            X509Certificate cert = (X509Certificate) certs.iterator().next();
            boolean result = instance.importTrustCertificate(cert, alias, password);
            if(result) {
                String[] update = new String[certificates.length+1];
                System.arraycopy(certificates, 0, update, 0, certificates.length);
                update[certificates.length] = alias;
                certificates = update;
                try {
                    fingerprints.put(alias, CertificateUtil.generateFingerprint(instance.getCertificate(alias, password), "MD5"));
                } catch (Exception e) {
                    log.error("Unable to generate certificate fingerprint", e);
                }
            }
            return result;
        }

        public boolean createKeyPair(String alias, String keyPassword, String keyAlgorithm, int keySize,
                                     String signatureAlgorithm, int validity, String commonName, String orgUnit,
                                     String organization, String locality, String state, String country) {
            boolean result = instance.generateKeyPair(alias, password, keyPassword.toCharArray(), keyAlgorithm, keySize,
                                     signatureAlgorithm, validity, commonName, orgUnit, organization, locality, state, country);
            if(result) {
                String[] update = new String[keys.length+1];
                System.arraycopy(keys, 0, update, 0, keys.length);
                update[keys.length] = alias;
                keys = update;
                try {
                    fingerprints.put(alias, CertificateUtil.generateFingerprint(instance.getCertificate(alias, password), "MD5"));
                } catch (Exception e) {
                    log.error("Unable to generate certificate fingerprint", e);
                }
            }
            return result;
        }
    }

//    public final static class WebAppData implements Serializable {
//        private String configId;
//        private boolean enabled;
//        private String dynamicPattern;
//        private boolean serveStaticContent;
//        private String contextRoot;
//        private String webAppDir;
//
//        public WebAppData(String configId, boolean enabled, String dynamicPattern, boolean serveStaticContent) {
//            this.configId = configId;
//            this.enabled = enabled;
//            this.dynamicPattern = dynamicPattern;
//            this.serveStaticContent = serveStaticContent;
//        }
//
//        public WebAppData(PortletRequest request, String prefix) {
//            configId = request.getParameter(prefix+"configId");
//            dynamicPattern = request.getParameter(prefix+"dynamicPattern");
//            String test = request.getParameter(prefix+"enabled");
//            enabled = test != null && !test.equals("") && !test.equals("false");
//            test = request.getParameter(prefix+"serveStaticContent");
//            serveStaticContent = test != null && !test.equals("") && !test.equals("false");
//            contextRoot = request.getParameter(prefix+"contextRoot");
//            webAppDir = request.getParameter(prefix+"webAppDir");
//        }
//
//        public void save(ActionResponse response, String prefix) {
//            response.setRenderParameter(prefix+"configId", configId);
//            response.setRenderParameter(prefix+"dynamicPattern", dynamicPattern);
//            response.setRenderParameter(prefix+"enabled", Boolean.toString(enabled));
//            response.setRenderParameter(prefix+"serveStaticContent", Boolean.toString(serveStaticContent));
//            if(contextRoot != null) response.setRenderParameter(prefix+"contextRoot", contextRoot);
//            if(webAppDir != null) response.setRenderParameter(prefix+"webAppDir", webAppDir);
//        }
//
//        public boolean isEnabled() {
//            return enabled;
//        }
//
//        public void setEnabled(boolean enabled) {
//            this.enabled = enabled;
//        }
//
//        public String getConfigId() {
//            return configId;
//        }
//
//        public void setConfigId(String configId) {
//            this.configId = configId;
//        }
//
//        public String getDynamicPattern() {
//            return dynamicPattern;
//        }
//
//        public void setDynamicPattern(String dynamicPattern) {
//            this.dynamicPattern = dynamicPattern;
//        }
//
//        public boolean isServeStaticContent() {
//            return serveStaticContent;
//        }
//
//        public void setServeStaticContent(boolean serveStaticContent) {
//            this.serveStaticContent = serveStaticContent;
//        }
//
//        public String getContextRoot() {
//            return contextRoot;
//        }
//
//        public void setContextRoot(String contextRoot) {
//            this.contextRoot = contextRoot;
//        }
//
//        public String getWebAppDir() {
//            return webAppDir;
//        }
//
//        public void setWebAppDir(String webAppDir) {
//            this.webAppDir = webAppDir;
//        }
//    }
//
//    public final static class ApacheModel implements MultiPageModel {
//        private String os;
//        private Integer addAjpPort;
//        private String logFilePath;
//        private String workersPath;
//        private List webApps = new ArrayList();
//
//        public ApacheModel(PortletRequest request) {
//            Map map = request.getParameterMap();
//            os = request.getParameter("os");
//            logFilePath = request.getParameter("logFilePath");
//            if(logFilePath == null) {
//                logFilePath = PortletManager.getServerInfo(request).resolve("var/log/apache_mod_jk.log").getPath();
//            }
//            workersPath = request.getParameter("workersPath");
//            if(workersPath == null) {
//                workersPath = PortletManager.getServerInfo(request).resolve("var/config/workers.properties").getPath();
//            }
//            String ajp = request.getParameter("addAjpPort");
//            if(!isEmpty(ajp)) addAjpPort = new Integer(ajp);
//            int index = 0;
//            while(true) {
//                String key = "webapp."+(index++)+".";
//                if(!map.containsKey(key+"configId")) {
//                    break;
//                }
//                BaseKeystoreHandler.WebAppData data = new BaseKeystoreHandler.WebAppData(request, key);
//                webApps.add(data);
//            }
//        }
//
//        public void save(ActionResponse response) {
//            if(!isEmpty(os)) response.setRenderParameter("os", os);
//            if(!isEmpty(logFilePath)) response.setRenderParameter("logFilePath", logFilePath);
//            if(!isEmpty(workersPath)) response.setRenderParameter("workersPath", workersPath);
//            if(addAjpPort != null) response.setRenderParameter("addAjpPort", addAjpPort.toString());
//            for (int i = 0; i < webApps.size(); i++) {
//                BaseKeystoreHandler.WebAppData data = (BaseKeystoreHandler.WebAppData) webApps.get(i);
//                String key = "webapp."+i+".";
//                data.save(response, key);
//            }
//        }
//
//        public String getOs() {
//            return os;
//        }
//
//        public void setOs(String os) {
//            this.os = os;
//        }
//
//        public Integer getAddAjpPort() {
//            return addAjpPort;
//        }
//
//        public void setAddAjpPort(Integer addAjpPort) {
//            this.addAjpPort = addAjpPort;
//        }
//
//        public String getLogFilePath() {
//            return logFilePath;
//        }
//
//        public void setLogFilePath(String logFilePath) {
//            this.logFilePath = logFilePath;
//        }
//
//        public String getWorkersPath() {
//            return workersPath;
//        }
//
//        public void setWorkersPath(String workersPath) {
//            this.workersPath = workersPath;
//        }
//
//        public List getWebApps() {
//            return webApps;
//        }
//
//        public void setWebApps(List webApps) {
//            this.webApps = webApps;
//        }
//    }
}
