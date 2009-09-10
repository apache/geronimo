/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.tomcat.connector;

import java.util.Map;

import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.Persistent;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

@GBean(name="Tomcat Connector HTTP APR")
public class Http11APRConnectorGBean extends BaseHttp11ConnectorGBean implements Http11APRProtocol {

    private String certificateFile;
    private String certificateKeyFile;
    private String caCertificateFile;
    private String caCertificatePath;
    private String certificateChainFile;
    private String revocationPath;
    private String revocationFile;
    
    public Http11APRConnectorGBean(@ParamAttribute(manageable=false, name = "name") String name,
                                   @ParamAttribute(manageable=false, name = "initParams") Map<String, String> initParams,
                                   @ParamAttribute(manageable=false, name = "host") String host,
                                   @ParamAttribute(manageable=false, name = "port") int port,
                                   @ParamReference(name = "TomcatContainer") TomcatContainer container,
                                   @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                                   @ParamAttribute(manageable=false, name = "connector") Connector conn)  throws Exception {
                                   
        super(name, initParams, "org.apache.coyote.http11.Http11AprProtocol", host, port, container, serverInfo, conn);
    }

    @Override
    public int getDefaultPort() {
        return 80;
    }

    @Override
    public String getGeronimoProtocol() {
        return WebManager.PROTOCOL_HTTP;
    }

    public int getPollTime() {
        Object value = connector.getAttribute("pollTime");
        return value == null ? 2000 : new Integer(value.toString()).intValue();
    }

    public int getPollerSize() {
        Object value = connector.getAttribute("pollerSize");
        return value == null ? 8192 : new Integer(value.toString()).intValue();
    }

    public int getSendfileSize() {
        Object value = connector.getAttribute("sendfileSize");
        return value == null ? 8192 : new Integer(value.toString()).intValue();
    }

    public String getSslCACertificateFile() {
        return caCertificateFile;
    }

    public String getSslCACertificatePath() {
        return caCertificatePath;
    }

    public String getSslCertificateChainFile() {
        return certificateChainFile; 
    }

    public String getSslCertificateFile() {
        return certificateFile; 
    }

    public String getSslCertificateKeyFile() {
        return certificateKeyFile; 
    }

    public String getSslCipherSuite() {
        return (String) connector.getAttribute("SSLCipherSuite");
    }
    
    public String getSslProtocol() {
        return (String) connector.getAttribute("SSLProtocol");
    }

    public String getSslCARevocationFile() {
        return revocationFile;
    }

    public String getSslCARevocationPath() {
        return revocationPath;
    }

    public String getSslVerifyClient() {
        return (String) connector.getAttribute("SSLVerifyClient");
    }

    public int getSslVerifyDepth() {
        Object value = connector.getAttribute("SSLVerifyDepth");
        return value == null ? 10 : new Integer(value.toString()).intValue();
    }

    public boolean getUseSendfile() {
        Object value = connector.getAttribute("useSendfile");
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }
    
    public String getSslPassword() {
        return (String) connector.getAttribute("SSLPassword");
    }
    
    @Persistent(manageable=false)
    public void setPollTime(int pollTime) {
        connector.setAttribute("pollTime", pollTime);
    }
    
    @Persistent(manageable=false)
    public void setPollerSize(int pollerSize) {
        connector.setAttribute("pollerSize", pollerSize);
    }
    
    @Persistent(manageable=false)
    public void setSendfileSize(int sendfileSize) {
        connector.setAttribute("sendfileSize", sendfileSize);
    }
    
    @Persistent(manageable=false)
    public void setSslCACertificateFile(String sslCACertificateFile) {
        if (sslCACertificateFile != null && sslCACertificateFile.equals(""))
            sslCACertificateFile = null;
        caCertificateFile = sslCACertificateFile;
        if (caCertificateFile == null)
            connector.setAttribute("SSLCACertificateFile", null);
        else
            connector.setAttribute("SSLCACertificateFile", serverInfo.resolveServerPath(caCertificateFile));
    }
    
    @Persistent(manageable=false)
    public void setSslCACertificatePath(String sslCACertificatePath) {
        if (sslCACertificatePath != null && sslCACertificatePath.equals(""))
            sslCACertificatePath = null;
        caCertificatePath = sslCACertificatePath;
        if (caCertificatePath == null)
            connector.setAttribute("SSLCACertificatePath", null);
        else
            connector.setAttribute("SSLCACertificatePath", serverInfo.resolveServerPath(caCertificatePath));
    }
    
    @Persistent(manageable=false)
    public void setSslCertificateChainFile(String sslCertificateChainFile) {
        if (sslCertificateChainFile != null && sslCertificateChainFile.equals(""))
            sslCertificateChainFile = null;
        certificateChainFile = sslCertificateChainFile;
        if (certificateChainFile == null)
            connector.setAttribute("SSLCertificateChainFile", null);
        else
            connector.setAttribute("SSLCertificateChainFile", serverInfo.resolveServerPath(certificateChainFile));
    }
    
    @Persistent(manageable=false)
    public void setSslCertificateFile(String sslCertificateFile) {
        if (sslCertificateFile != null && sslCertificateFile.equals(""))
            sslCertificateFile = null;
        certificateFile = sslCertificateFile;
        if (certificateFile == null)
            connector.setAttribute("SSLCertificateFile", null);
        else
            connector.setAttribute("SSLCertificateFile", serverInfo.resolveServerPath(certificateFile));
    }
    
    @Persistent(manageable=false)
    public void setSslCertificateKeyFile(String sslCertificateKeyFile) {
        if (sslCertificateKeyFile != null && sslCertificateKeyFile.equals(""))
            sslCertificateKeyFile = null;
        certificateKeyFile = sslCertificateKeyFile;
        if (certificateKeyFile == null)
            connector.setAttribute("SSLCertificateKeyFile", null);
        else
            connector.setAttribute("SSLCertificateKeyFile", serverInfo.resolveServerPath(certificateKeyFile));
    }
    
    @Persistent(manageable=false)
    public void setSslCipherSuite(String sslCipherSuite) {
        connector.setAttribute("SSLCipherSuite", sslCipherSuite);
    }
    
    @Persistent(manageable=false)
    public void setSslPassword(String sslPassword) {
        if (sslPassword != null && sslPassword.equals(""))
            sslPassword = null;
        connector.setAttribute("SSLPassword", sslPassword);
    }
    
    @Persistent(manageable=false)
    public void setSslProtocol(String sslProtocol) {
        connector.setAttribute("SSLProtocol", sslProtocol);
    }
    
    @Persistent(manageable=false)
    public void setSslCARevocationFile(String sslCARevocationFile) {
        if (sslCARevocationFile!= null && sslCARevocationFile.equals("")) 
            sslCARevocationFile = null;
        revocationFile = sslCARevocationFile;
        if (revocationFile == null)
            connector.setAttribute("SSLCARevocationFile", null);
        else
            connector.setAttribute("SSLCARevocationFile", serverInfo.resolveServerPath(revocationFile));
    }
    
    @Persistent(manageable=false)
    public void setSslCARevocationPath(String sslCARevocationPath) {
        if (sslCARevocationPath!= null && sslCARevocationPath.equals("")) 
            sslCARevocationPath = null;
        revocationPath = sslCARevocationPath;
        if (revocationPath == null)
            connector.setAttribute("SSLCARevocationPath", null);
        else
            connector.setAttribute("SSLCARevocationPath", serverInfo.resolveServerPath(revocationPath));

    }
    
    @Persistent(manageable=false)
    public void setSslVerifyClient(String sslVerifyClient) {
        connector.setAttribute("SSLVerifyClient", sslVerifyClient);
    }
    
    @Persistent(manageable=false)
    public void setSslVerifyDepth(int sslVerifyDepth) {
        connector.setAttribute("SSLVerifyDepth", sslVerifyDepth);
    }
    
    @Persistent(manageable=false)
    public void setUseSendfile(boolean useSendfile) {
        connector.setAttribute("useSendfile", useSendfile);
    }
    
}
