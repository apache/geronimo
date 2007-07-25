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

import java.net.InetAddress;

import javax.management.j2ee.statistics.Stats;
import javax.net.ssl.KeyManagerFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.tomcat.stats.ConnectorStats;

public class BaseHttp11ConnectorGBean extends ConnectorGBean implements Http11Protocol, StatisticsProvider {

    private String keystoreFileName;

    private String truststoreFileName;

    private String algorithm;

    // JSR77 stats
    private ConnectorStats connStatsProvider = new ConnectorStats();

    private boolean reset = true;

    public BaseHttp11ConnectorGBean(String name, String protocol, String address, int port, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        super(name, protocol, container, serverInfo);

        // Default the host to listen on all address is one was not specified
        if (address == null) {
            address = "0.0.0.0";
        }

        // Must have a port
        if (port == 0) {
            throw new IllegalArgumentException("Must declare a port.");
        }

        connector.setAttribute("address", address);
        connector.setAttribute("port", port);

    }
    protected void initProtocol() {}

    // Generic SSL
    public String getAlgorithm() {
        return algorithm;
    }

    public String getCiphers() {
        return (String) connector.getAttribute("ciphers");
    }

    public boolean getClientAuth() {
        Object value = connector.getAttribute("clientAuth");
        return value == null ? false : new Boolean(value.toString()).booleanValue();
    }

    public String getKeyAlias() {
        return (String) connector.getAttribute("keyAlias");
    }

    public String getKeystoreFile() {
        return keystoreFileName;
    }

    public String getKeystoreType() {
        return (String) connector.getAttribute("keystoreType");
    }

    public String getSslProtocol() {
        return (String) connector.getAttribute("sslProtocol");
    }

    public String getTruststoreFile() {
        return truststoreFileName;
    }

    public String getTruststoreType() {
        return (String) connector.getAttribute("truststoreType");
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        if ("default".equalsIgnoreCase(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }
        connector.setAttribute("algorithm", algorithm);
    }

    public void setCiphers(String ciphers) {
        connector.setAttribute("ciphers", ciphers);
    }

    public void setClientAuth(String clientAuth) {
        connector.setAttribute("clientAuth", new Boolean(clientAuth));
    }

    public void setKeyAlias(String keyAlias) {
        connector.setAttribute("keyAlias", keyAlias);
    }

    public void setKeystoreFile(String keystoreFile) {
        keystoreFileName = keystoreFile;
        connector.setAttribute("keystoreFile", serverInfo.resolveServerPath(keystoreFileName));
    }

    public void setKeystorePass(String keystorePass) {
        connector.setAttribute("keystorePass", keystorePass);
    }

    public void setKeystoreType(String keystoreType) {
        connector.setAttribute("keystoreType", keystoreType);
    }

    public void setSslProtocol(String sslProtocol) {
        connector.setAttribute("sslProtocol", sslProtocol);
    }

    public void setTruststoreFile(String truststoreFile) {
        truststoreFileName = truststoreFile;
        connector.setAttribute("truststoreFile", serverInfo.resolveServerPath(truststoreFileName));
    }

    public void setTruststorePass(String truststorePass) {
        connector.setAttribute("truststorePass", truststorePass);
    }

    public void setTruststoreType(String truststoreType) {
        connector.setAttribute("truststoreType", truststoreType);
    }

    // Generic HTTP
    public int getAcceptCount() {
        Object value = connector.getAttribute("acceptCount");
        return value == null ? 10 : Integer.parseInt(value.toString());
    }

    public String getAddress() {
        Object value = connector.getAttribute("address");
        if (value == null) {
            return "0.0.0.0";
        } else if (value instanceof InetAddress) {
            return ((InetAddress) value).getHostAddress();
        } else
            return value.toString();
    }

    public int getBufferSize() {
        Object value = connector.getAttribute("bufferSize");
        return value == null ? 2048 : Integer.parseInt(value.toString());
    }

    public String getCompressableMimeType() {
        return (String) connector.getAttribute("compressableMimeType");
    }

    public String getCompression() {
        return (String) connector.getAttribute("compression");
    }

    public int getConnectionLinger() {
        Object value = connector.getAttribute("connectionLinger");
        return value == null ? -1 : Integer.parseInt(value.toString());
    }

    public int getConnectionTimeout() {
        Object value = connector.getAttribute("connectionTimeout");
        return value == null ? 60000 : Integer.parseInt(value.toString());
    }

    public boolean getDisableUploadTimeout() {
        Object value = connector.getAttribute("disableUploadTimeout");
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }

    public String getExecutor() {
        return (String) connector.getAttribute("Executor");
    }

    public int getKeepAliveTimeout() {
        Object value = connector.getAttribute("keepAliveTimeout");
        return value == null ? getConnectionTimeout() : Integer.parseInt(value.toString());
    }

    public int getMaxHttpHeaderSize() {
        Object value = connector.getAttribute("maxHttpHeaderSize");
        return value == null ? 4096 : Integer.parseInt(value.toString());
    }

    public int getMaxKeepAliveRequests() {
        Object value = connector.getAttribute("maxKeepAliveRequests");
        return value == null ? 100 : Integer.parseInt(value.toString());
    }

    public int getMaxThreads() {
        Object value = connector.getAttribute("maxThreads");
        return value == null ? 200 : Integer.parseInt(value.toString());
    }

    public String getNoCompressionUserAgents() {
        return (String) connector.getAttribute("noCompressionUserAgents");
    }

    public int getPort() {
        return connector.getPort();
    }

    public String getRestrictedUserAgents() {
        return (String) connector.getAttribute("restrictedUserAgents");
    }

    public String getServer() {
        return (String) connector.getAttribute("server");
    }

    public int getSocketBuffer() {
        Object value = connector.getAttribute("socketBuffer");
        return value == null ? 9000 : Integer.parseInt(value.toString());
    }

    public boolean getTcpNoDelay() {
        Object value = connector.getAttribute("tcpNoDelay");
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }

    public int getThreadPriority() {
        Object value = connector.getAttribute("threadPriority");
        return value == null ? Thread.NORM_PRIORITY : Integer.parseInt(value.toString());
    }

    public void setAcceptCount(int acceptCount) {
        connector.setAttribute("acceptCount", new Integer(acceptCount));
    }

    public void setAddress(String address) {
        connector.setAttribute("address", address);
    }

    public void setBufferSize(int bufferSize) {
        connector.setAttribute("bufferSize", new Integer(bufferSize));
    }

    public void setCompressableMimeType(int compressableMimeType) {
        connector.setAttribute("compressableMimeType", compressableMimeType);
    }

    public void setCompression(String compression) {
        connector.setAttribute("compression", compression);
    }

    public void setConnectionLinger(int connectionLinger) {
        connector.setAttribute("connectionLinger", new Integer(connectionLinger));
    }

    public void setConnectionTimeout(int connectionTimeout) {
        connector.setAttribute("connectionTimeout", new Integer(connectionTimeout));
    }

    public void setDisableUploadTimeout(boolean disableUploadTimeout) {
        connector.setAttribute("disableUploadTimeout", new Boolean(disableUploadTimeout));
    }

    public void setExecutor(String executor) {
        connector.setAttribute("executor", executor);
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        connector.setAttribute("keepAliveTimeout", keepAliveTimeout);
    }

    public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
        connector.setAttribute("maxHttpHeaderSize", new Integer(maxHttpHeaderSize));
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        connector.setAttribute("maxKeepAliveRequests", new Integer(maxKeepAliveRequests));
    }

    public void setMaxThreads(int maxThreads) {
        connector.setAttribute("maxThreads", new Integer(maxThreads));
    }

    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        connector.setAttribute("noCompressionUserAgents", noCompressionUserAgents);
    }

    public void setPort(int port) {
        connector.setPort(port);
    }

    public void setRestrictedUserAgents(String restrictedUserAgents) {
        connector.setAttribute("restrictedUserAgents", restrictedUserAgents);
    }

    public void setServer(String server) {
        connector.setAttribute("server", server);
    }

    public void setSocketBuffer(int socketBuffer) {
        connector.setAttribute("socketBuffer", new Integer(socketBuffer));
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        connector.setAttribute("tcpNoDelay", new Boolean(tcpNoDelay));
    }

    public void setThreadPriority(int threadPriority) {
        connector.setAttribute("threadPriority", new Integer(threadPriority));
    }

    // Statistics Provider

    public Stats getStats() {
        String port = String.valueOf(getPort());
        if (reset) {
            reset = false;
            return connStatsProvider.getStats(port);
        } else
            return connStatsProvider.updateStats(port);
    }

    public void resetStats() {
        reset = true;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Connector", BaseHttp11ConnectorGBean.class, ConnectorGBean.GBEAN_INFO);
        infoFactory.addInterface(Http11Protocol.class, 
                new String[] {
                    //HTTP Attributes
                    "acceptCount", 
                    "address", 
                    "bufferSize", 
                    "compressableMimeType", 
                    "compression", 
                    "connectionLinger", 
                    "connectionTimeout", 
                    "executor", 
                    "keepAliveTimeout", 
                    "disableUploadTimeout", 
                    "maxHttpHeaderSize", 
                    "maxKeepAliveRequests", 
                    "maxThreads", 
                    "noCompressionUserAgents", 
                    "port", 
                    "restrictedUserAgents", 
                    "server", 
                    "socketBuffer", 
                    "tcpNoDelay", 
                    "threadPriority",
                    //SSL Attributes
                    "algorithm",
                    "clientAuth",
                    "keystoreFile",
                    "keystorePass",
                    "keystoreType",
                    "sslProtocol",
                    "ciphers",
                    "keyAlias",
                    "truststoreFile",
                    "truststorePass",
                    "truststoreType"
                },
                new String[] {
                    //HTTP Attributes
                    "acceptCount", 
                    "address", 
                    "bufferSize", 
                    "compressableMimeType", 
                    "compression", 
                    "connectionLinger", 
                    "connectionTimeout", 
                    "executor", 
                    "keepAliveTimeout", 
                    "disableUploadTimeout", 
                    "maxHttpHeaderSize", 
                    "maxKeepAliveRequests", 
                    "maxThreads", 
                    "noCompressionUserAgents", 
                    "port", 
                    "restrictedUserAgents", 
                    "server", 
                    "socketBuffer", 
                    "tcpNoDelay", 
                    "threadPriority",
                    //SSL Attributes
                    "algorithm",
                    "clientAuth",
                    "keystoreFile",
                    "keystorePass",
                    "keystoreType",
                    "sslProtocol",
                    "ciphers",
                    "keyAlias",
                    "truststoreFile",
                    "truststorePass",
                    "truststoreType"
                }
        );
        infoFactory.setConstructor(new String[] { "name", "protocol", "address", "port", "TomcatContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
