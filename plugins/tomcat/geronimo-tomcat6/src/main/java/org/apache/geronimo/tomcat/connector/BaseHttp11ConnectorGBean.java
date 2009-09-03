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
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.management.j2ee.statistics.Stats;

import org.apache.catalina.Executor;
import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.tomcat.stats.ConnectorStats;

@GBean(name="Tomcat Connector")
public abstract class BaseHttp11ConnectorGBean extends ConnectorGBean implements BaseHttp11Protocol, StatisticsProvider {

    protected String connectHost;
    
    // JSR77 stats
    private ConnectorStats connStatsProvider = new ConnectorStats();

    private boolean reset = true;

    public BaseHttp11ConnectorGBean(@ParamAttribute(name = "name") String name,
                                    @ParamAttribute(name = "initParams") Map<String, String> initParams,
                                    @ParamAttribute(name = "protocol") String tomcatProtocol,
                                    @ParamAttribute(name = "host") String host,
                                    @ParamAttribute(name = "port") int port,
                                    @ParamReference(name = "TomcatContainer") TomcatContainer container,
                                    @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                                    @ParamAttribute(name = "connector") Connector conn)  throws Exception {
                                    
        super(name, initParams, tomcatProtocol, container, serverInfo, conn);

        // Default the host to listen on all address is one was not specified
        if (host == null) {
            host = "0.0.0.0";
        }

        // Must have a port
        if (port == 0) {
            throw new IllegalArgumentException("Must declare a port.");
        }

        connector.setAttribute("address", host);
        connector.setPort(port);
        
    }
    
    protected void initProtocol() {}
    
    public String getConnectUrl() {
        if(connectHost == null) {
            String host = getAddress();
            if(host == null || host.equals("0.0.0.0") || host.equals("0:0:0:0:0:0:0:1")) {
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    host = "unknown-host";
                }
                if(address != null) {
                    host = address.getCanonicalHostName();
                    if(host == null || host.equals("")) {
                        host = address.getHostAddress();
                    }
                }
            }
            // this host address could be in IPv6 format, 
            // which means we need to wrap it in brackets
            if (host.indexOf(":") >= 0) {
                host = "[" + host + "]"; 
            }
            connectHost = host;
        }
        return getScheme().toLowerCase()+"://"+connectHost+(getPort() == getDefaultPort() ? "" : ":"+getPort());
    }
    
    public abstract int getDefaultPort();


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
        Object value = connector.getAttribute("executor");
        if (value == null)
            return null;
        
        if (value instanceof String)
            return (String)value;
        
        if(value instanceof Executor){
            return ((Executor) value).getName();
        }
        
        return (String) value.getClass().getName();
    }
    
    public String getHost() {
        return getAddress();
    }
    
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
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
    
    public int getMaxSpareThreads() {
        Object value = connector.getAttribute("maxSpareThreads");
        return value == null ? 100 : Integer.parseInt(value.toString());
    }
    
    public int getMinSpareThreads() {
        Object value = connector.getAttribute("minSpareThreads");
        return value == null ? 10 : Integer.parseInt(value.toString());
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

    public void setCompressableMimeType(String compressableMimeType) {
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
    
    public void setHost(String host) {
        setAddress(host);
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
    
    public void setMaxSpareThreads(int maxSpareThreads) {
        connector.setAttribute("maxSpareThreads", new Integer(maxSpareThreads));
    }
    
    public void setMinSpareThreads(int minSpareThreads) {
        connector.setAttribute("minSpareThreads", new Integer(minSpareThreads));
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
        if (server.equals(""))
            server = null;
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

    public boolean isStatisticsProvider() {
        return true;
    }

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

    
}
