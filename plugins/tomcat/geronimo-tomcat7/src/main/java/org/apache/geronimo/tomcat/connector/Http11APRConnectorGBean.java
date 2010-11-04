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
        String path = (String) connector.getAttribute("SSLCACertificateFile");
        return getRelatedPathtoCatalinaHome(path);
    }

    public String getSslCACertificatePath() {
        String path = (String) connector.getAttribute("SSLCACertificatePath");
        return getRelatedPathtoCatalinaHome(path);
    }

    public String getSslCertificateChainFile() {
        String path = (String) connector.getAttribute("truststoreFile");
        return getRelatedPathtoCatalinaHome(path);
    }

    public String getSslCertificateFile() {
        String path = (String) connector.getAttribute("SSLCertificateFile");
        return getRelatedPathtoCatalinaHome(path);
    }

    public String getSslCertificateKeyFile() {
        String path = (String) connector.getAttribute("SSLCertificateKeyFile");
        return getRelatedPathtoCatalinaHome(path);
    }

    public String getSslCipherSuite() {
        return (String) connector.getAttribute("SSLCipherSuite");
    }

    public String getSslProtocol() {
        return (String) connector.getAttribute("SSLProtocol");
    }

    public String getSslCARevocationFile() {
        String path = (String) connector.getAttribute("SSLCARevocationFile");
        return getRelatedPathtoCatalinaHome(path);
    }

    public String getSslCARevocationPath() {
        String path = (String) connector.getAttribute("SSLCARevocationPath");
        return getRelatedPathtoCatalinaHome(path);
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
        return value == null ? true : Boolean.valueOf(value.toString());
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
    public void setUseSendfile(boolean useSendfile) {
        connector.setAttribute("useSendfile", useSendfile);
    }


}
