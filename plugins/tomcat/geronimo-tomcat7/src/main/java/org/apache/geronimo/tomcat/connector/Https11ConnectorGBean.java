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

import javax.net.ssl.KeyManagerFactory;

import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.Persistent;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

@GBean(name="Tomcat Connector HTTPS BIO")
public class Https11ConnectorGBean extends Http11ConnectorGBean implements Https11Protocol {
    
    private String keystoreFileName;

    private String truststoreFileName;

    private String algorithm;
    
    
    public Https11ConnectorGBean(@ParamAttribute(manageable=false, name = "name") String name,
                                 @ParamAttribute(manageable=false, name = "initParams") Map<String, String> initParams,
                                 @ParamAttribute(manageable=false, name = "host") String host,
                                 @ParamAttribute(manageable=false, name = "port") int port,
                                 @ParamReference(name = "TomcatContainer") TomcatContainer container,
                                 @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                                 @ParamAttribute(manageable=false, name = "connector") Connector conn)  throws Exception {
                                 
        super(name, initParams, host, port, container, serverInfo, conn);
        setSslEnabled(true);
        setScheme("https");
        setSecure(true);
    }
    
    public int getDefaultPort() {
        return 443; 
    }  
    
    public String getGeronimoProtocol(){
        return WebManager.PROTOCOL_HTTPS;
    }
  
    
 // Generic SSL
    public String getAlgorithm() {

        if ("default".equalsIgnoreCase(algorithm)) {
            return KeyManagerFactory.getDefaultAlgorithm();
        }
        return algorithm;
    }

    public String getCiphers() {
        return (String) connector.getAttribute("ciphers");
    }

    public String getClientAuth() {
        Object value = connector.getAttribute("clientAuth");
        return value == null ? "false" : value.toString();
    }

    public String getKeyAlias() {
        return (String) connector.getAttribute("keyAlias");
    }

    public String getKeystoreFile() {

        String keystore = (String) connector.getAttribute("keystoreFile");

        return getRelatedPathtoCatalinaHome(keystore);

    }

    public String getKeystoreType() {
        return (String) connector.getAttribute("keystoreType");
    }

    public String getSslProtocol() {
        return (String) connector.getAttribute("sslProtocol");
    }

    public String getTruststoreFile() {

        String truststoreFile = (String) connector.getAttribute("truststoreFile");

        return getRelatedPathtoCatalinaHome(truststoreFile);

    }

    public String getTruststoreType() {
        return (String) connector.getAttribute("truststoreType");
    }

    public String getTruststorePass() {
        return (String) connector.getAttribute("truststorePass");
    }

    public String getKeystorePass() {
        return (String) connector.getAttribute("keystorePass");
    }

    @Persistent(manageable=false)
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        if ("default".equalsIgnoreCase(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }
        connector.setAttribute("algorithm", algorithm);
    }

    @Persistent(manageable=false)
    public void setCiphers(String ciphers) {
        connector.setAttribute("ciphers", ciphers);
    }

    @Persistent(manageable=false)
    public void setClientAuth(String clientAuth) {
        connector.setAttribute("clientAuth", clientAuth);
    }

    @Persistent(manageable=false)
    public void setKeyAlias(String keyAlias) {
        if (keyAlias.equals(""))
            keyAlias = null;
        connector.setAttribute("keyAlias", keyAlias);
    }

    @Persistent(manageable=false)
    public void setKeystoreFile(String keystoreFile) {
        if (keystoreFile!= null && keystoreFile.equals(""))
            keystoreFile = null;

        keystoreFileName = keystoreFile;

        if (keystoreFileName == null) {

            connector.setAttribute("keystoreFile", keystoreFileName);

        } else {

            String resovledAbsolutePath = this.getAbsolutePathBasedOnCatalinaHome(keystoreFileName);

            if (resovledAbsolutePath != null)
                connector.setAttribute("keystoreFile", resovledAbsolutePath);

        }

    }


    @Persistent(manageable=false)
    public void setKeystorePass(String keystorePass) {
        if (keystorePass!= null && keystorePass.equals(""))
            keystorePass = null;
        connector.setAttribute("keystorePass", keystorePass);
    }

    @Persistent(manageable=false)
    public void setKeystoreType(String keystoreType) {
        if (keystoreType!= null && keystoreType.equals(""))
            keystoreType = null;
        connector.setAttribute("keystoreType", keystoreType);
    }

    @Persistent(manageable=false)
    public void setSslProtocol(String sslProtocol) {
        if (sslProtocol!= null && sslProtocol.equals(""))
            sslProtocol = null;
        connector.setAttribute("sslProtocol", sslProtocol);
    }

    @Persistent(manageable=false)
    public void setTruststoreFile(String truststoreFile) {
        if (truststoreFile!= null && truststoreFile.equals(""))
            truststoreFile = null;
        truststoreFileName = truststoreFile;

        if (truststoreFileName == null) {
            connector.setAttribute("truststoreFile", null);
        }

        else {

            String resovledAbsolutePath = this.getAbsolutePathBasedOnCatalinaHome(truststoreFile);

            if (resovledAbsolutePath != null)
                connector.setAttribute("truststoreFile", resovledAbsolutePath);
        }

    }

    @Persistent(manageable=false)
    public void setTruststorePass(String truststorePass) {
        if (truststorePass!= null && truststorePass.equals(""))
            truststorePass = null;
        connector.setAttribute("truststorePass", truststorePass);
    }

    @Persistent(manageable=false)
    public void setTruststoreType(String truststoreType) {
        if (truststoreType!= null && truststoreType.equals(""))
            truststoreType = null;
        connector.setAttribute("truststoreType", truststoreType);
    }
}
