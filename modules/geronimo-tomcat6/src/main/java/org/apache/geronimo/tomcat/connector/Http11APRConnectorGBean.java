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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

public class Http11APRConnectorGBean extends BaseHttp11ConnectorGBean implements Http11APRProtocol {

    public Http11APRConnectorGBean(String name, Map initParams, String address, int port, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        super(name, initParams, "org.apache.coyote.http11.Http11AprProtocol", address, port, container, serverInfo);
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
        return (String) connector.getAttribute("SSLCACertificateFile");
    }

    public String getSslCACertificatePath() {
        return (String) connector.getAttribute("SSLCACertificatePath");
    }

    public String getSslCertificateChainFile() {
        return (String) connector.getAttribute("SSLCertificateChainFile");
    }

    public String getSslCertificateFile() {
        return (String) connector.getAttribute("SSLCertificateFile");
    }

    public String getSslCertificateKeyFile() {
        return (String) connector.getAttribute("SSLCertificateKeyFile");
    }

    public String getSslCipherSuite() {
        return (String) connector.getAttribute("SSLCipherSuite");
    }
    
    public String getSslProtocol() {
        return (String) connector.getAttribute("SSLProtocol");
    }

    public String getSslCARevocationFile() {
        return (String) connector.getAttribute("SSLCARevocationFile");
    }

    public String getSslCARevocationPath() {
        return (String) connector.getAttribute("SSLCARevocationPath");
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

    public void setPollTime(int pollTime) {
        connector.setAttribute("pollTime", pollTime);
    }

    public void setPollerSize(int pollerSize) {
        connector.setAttribute("pollerSize", pollerSize);
    }

    public void setSendfileSize(int sendfileSize) {
        connector.setAttribute("sendfileSize", sendfileSize);
    }

    public void setSslCACertificateFile(String sslCACertificateFile) {
        connector.setAttribute("SSLCACertificateFile", sslCACertificateFile);
    }

    public void setSslCACertificatePath(String sslCACertificatePath) {
        connector.setAttribute("SSLCACertificatePath", sslCACertificatePath);
    }

    public void setSslCertificateChainFile(String sslCertificateChainFile) {
        connector.setAttribute("SSLCertificateChainFile", sslCertificateChainFile);
    }

    public void setSslCertificateFile(String sslCertificateFile) {
        connector.setAttribute("SSLCertificateFile", sslCertificateFile);
    }

    public void setSslCertificateKeyFile(String sslCertificateKeyFile) {
        connector.setAttribute("SSLCertificateKeyFile", sslCertificateKeyFile);
    }

    public void setSslCipherSuite(String sslCipherSuite) {
        connector.setAttribute("SSLCipherSuite", sslCipherSuite);
    }

    public void setSslPassword(String sslPassword) {
        connector.setAttribute("SSLPassword", sslPassword);
    }
    
    public void setSslProtocol(String sslProtocol) {
        connector.setAttribute("SSLProtocol", sslProtocol);
    }

    public void setSslCARevocationFile(String sslCARevocationFile) {
        connector.setAttribute("SSLCARevocationFile", sslCARevocationFile);
    }

    public void setSslCARevocationPath(String sslCARevocationPath) {
        connector.setAttribute("SSLCARevocationPath", sslCARevocationPath);
    }

    public void setSslVerifyClient(String sslVerifyClient) {
        connector.setAttribute("SSLVerifyClient", sslVerifyClient);
    }

    public void setSslVerifyDepth(int sslVerifyDepth) {
        connector.setAttribute("SSLVerifyDepth", sslVerifyDepth);
    }

    public void setUseSendfile(boolean useSendfile) {
        connector.setAttribute("useSendfile", useSendfile);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Connector", Http11APRConnectorGBean.class, BaseHttp11ConnectorGBean.GBEAN_INFO);
        infoFactory.addInterface(Http11APRProtocol.class, 
                new String[] {
                    //APR Attributes
                    "pollTime",
                    "pollerSize",
                    "useSendfile",
                    "sendfileSize",
                    //SSL Attributes
                    "sslProtocol",
                    "sslCipherSuite",
                    "sslCertificateFile",
                    "sslCertificateKeyFile",
                    "sslPassword",
                    "sslVerifyClient",
                    "sslVerifyDepth",
                    "sslCACertificateFile",
                    "sslCACertificatePath",
                    "sslCertificateChainFile",
                    "sslCARevocationFile",
                    "sslCARevocationPath"
                },
                new String[] {
                    //APR Attributes
                    "pollTime",
                    "pollerSize",
                    "useSendfile",
                    "sendfileSize",
                    //SSL Attributes
                    "sslProtocol",
                    "sslCipherSuite",
                    "sslCertificateFile",
                    "sslCertificateKeyFile",
                    "sslPassword",
                    "sslVerifyClient",
                    "sslVerifyDepth",
                    "sslCACertificateFile",
                    "sslCACertificatePath",
                    "sslCertificateChainFile",
                    "sslCARevocationFile",
                    "sslCARevocationPath"
                }
        );
        infoFactory.setConstructor(new String[] { "name", "initParams", "address", "port", "TomcatContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
