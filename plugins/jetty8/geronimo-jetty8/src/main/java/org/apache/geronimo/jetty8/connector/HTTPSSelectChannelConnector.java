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


package org.apache.geronimo.jetty8.connector;

import javax.net.ssl.KeyManagerFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.jetty8.JettyContainer;
import org.apache.geronimo.jetty8.JettySecureConnector;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.threads.ThreadPool;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

/**
 * Implementation of a HTTPS connector based on Jetty's SslConnector (which uses pure JSSE).
 *
 * @version $Rev$ $Date$
 */
public class HTTPSSelectChannelConnector extends JettyConnector implements JettySecureConnector {
    private final GeronimoSelectChannelSSLListener https;
    private String algorithm;

    public HTTPSSelectChannelConnector(JettyContainer container, ThreadPool threadPool, KeystoreManager keystoreManager) {
        super(container, new GeronimoSelectChannelSSLListener(keystoreManager), threadPool, "HTTPSSelectChannelConnector");
        https = (GeronimoSelectChannelSSLListener) listener;
    }

    public int getDefaultPort() {
        return 443;
    }

    public String getProtocol() {
        return WebManager.PROTOCOL_HTTPS;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Algorithm to use.
     * As different JVMs have different implementations available, the default algorithm can be used by supplying the value "Default".
     *
     * @param algorithm the algorithm to use, or "Default" to use the default from {@link javax.net.ssl.KeyManagerFactory#getDefaultAlgorithm()}
     */
    public void setAlgorithm(String algorithm) {
        // cache the value so the null
        this.algorithm = algorithm;
        if ("default".equalsIgnoreCase(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }
        https.setSslKeyManagerFactoryAlgorithm(algorithm);
    }

    public String getSecureProtocol() {
        return https.getProtocol();
    }

    public void setSecureProtocol(String protocol) {
        https.setProtocol(protocol);
    }

    public void setClientAuthRequired(boolean needClientAuth) {
        https.setNeedClientAuth(needClientAuth);
    }

    public boolean isClientAuthRequired() {
        return https.getNeedClientAuth();
    }

    public void setClientAuthRequested(boolean wantClientAuth) {
        https.setWantClientAuth(wantClientAuth);
    }

    public boolean isClientAuthRequested() {
        return https.getWantClientAuth();
    }

    public void setKeyStore(String keyStore) {
        https.setKeyStore(keyStore);
    }

    public String getKeyStore() {
        return https.getKeyStore();
    }

    public void setTrustStore(String trustStore) {
        https.setTrustStore(trustStore);
    }

    public String getTrustStore() {
        return https.getTrustStore();
    }

    public void setKeyAlias(String keyAlias) {
        https.setKeyAlias(keyAlias);
    }

    public String getKeyAlias() {
        return https.getKeyAlias();
    }

    //TODO does this make sense???
    public void setRedirectPort(int port) {
        SelectChannelConnector socketListener = (SelectChannelConnector) listener;
        socketListener.setConfidentialPort(port);
        socketListener.setIntegralPort(port);
        socketListener.setIntegralScheme("https");
        socketListener.setConfidentialScheme("https");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty SelectChannel Connector HTTPS", HTTPSSelectChannelConnector.class, JettyConnector.GBEAN_INFO);
        infoFactory.addAttribute("algorithm", String.class, true, true);
        infoFactory.addAttribute("secureProtocol", String.class, true, true);
        infoFactory.addAttribute("keyStore", String.class, true, true);
        infoFactory.addAttribute("keyAlias", String.class, true, true);
        infoFactory.addAttribute("trustStore", String.class, true, true);
        infoFactory.addAttribute("clientAuthRequired", boolean.class, true, true);
        infoFactory.addAttribute("clientAuthRequested", boolean.class, true, true);
        infoFactory.addReference("KeystoreManager", KeystoreManager.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addInterface(JettySecureConnector.class);
        infoFactory.setConstructor(new String[]{"JettyContainer", "ThreadPool", "KeystoreManager"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    // ================= NO LONGER USED!!! =====================
    // todo: remove these from the SSL interface

    public String getKeystoreFileName() {
        return null;
    }

    public void setKeystoreFileName(String name) {
    }

    public void setKeystorePassword(String password) {
    }

    public String getKeystoreType() {
        return null;
    }

    public void setKeystoreType(String type) {
    }
}
