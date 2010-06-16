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
import org.apache.geronimo.gbean.annotation.Persistent;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.tomcat.stats.ConnectorStats;

@GBean(name="Tomcat Connector AJP")
public class AJP13ConnectorGBean extends ConnectorGBean implements Ajp13Protocol, StatisticsProvider {

    // JSR77 stats
    private ConnectorStats connStatsProvider = new ConnectorStats();

    private boolean reset = true;

    protected String connectHost;

    public AJP13ConnectorGBean(@ParamAttribute(manageable=false, name = "name") String name,
                               @ParamAttribute(manageable=false, name = "initParams") Map<String, String> initParams,
                               @ParamAttribute(manageable=false, name = "host") String host,
                               @ParamAttribute(manageable=false, name = "port") int port,
                               @ParamReference(name = "TomcatContainer") TomcatContainer container,
                               @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                               @ParamAttribute(manageable=false, name = "connector") Connector conn)  throws Exception {


        super(name, initParams, "AJP/1.3", container, serverInfo, conn);

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

    public String getGeronimoProtocol(){
        return WebManager.PROTOCOL_AJP;
    }

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

    public int getDefaultPort() {
        return -1;
    }

    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
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

    public int getBacklog() {
        Object value = connector.getAttribute("backlog");
        return value == null ? 10 : Integer.parseInt(value.toString());
    }

    public int getBufferSize() {
        Object value = connector.getAttribute("bufferSize");
        return value == null ? 2048 : Integer.parseInt(value.toString());
    }

    public int getConnectionTimeout() {
        Object value = connector.getAttribute("connectionTimeout");
        return value == null ? org.apache.coyote.ajp.Constants.DEFAULT_CONNECTION_TIMEOUT : Integer.parseInt(value.toString());
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

         return value.getClass().getName();
    }

    public String getHost() {
        return getAddress();
    }

    public int getKeepAliveTimeout() {
        Object value = connector.getAttribute("keepAliveTimeout");
        return value == null ? getConnectionTimeout() : Integer.parseInt(value.toString());
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

    public int getPort() {
        return connector.getPort();
    }

    public boolean getTcpNoDelay() {
        Object value = connector.getAttribute("tcpNoDelay");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public boolean getTomcatAuthentication() {
        Object value = connector.getAttribute("tomcatAuthentication");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    @Persistent(manageable=false)
    public void setAddress(String address) {
        connector.setAttribute("address", address);
    }

    @Persistent(manageable=false)
    public void setBacklog(int backlog) {
        connector.setAttribute("backlog", Integer.valueOf(backlog));
    }

    @Persistent(manageable=false)
    public void setBufferSize(int bufferSize) {
        connector.setAttribute("bufferSize", Integer.valueOf(bufferSize));
    }

    @Persistent(manageable=false)
    public void setConnectionTimeout(int connectionTimeout) {
        connector.setAttribute("connectionTimeout", Integer.valueOf(connectionTimeout));
    }

    @Persistent(manageable=false)
    public void setExecutor(String executor) {
        connector.setAttribute("executor", executor);
    }

    @Persistent(manageable=false)
    public void setHost(String host) {
        setAddress(host);
    }

    @Persistent(manageable=false)
    public void setKeepAliveTimeout(int keepAliveTimeout) {
        connector.setAttribute("keepAliveTimeout", keepAliveTimeout);
    }

    @Persistent(manageable=false)
    public void setMaxThreads(int maxThreads) {
        connector.setAttribute("maxThreads", maxThreads);
    }

    @Persistent(manageable=false)
    public void setMaxSpareThreads(int maxSpareThreads) {
        connector.setAttribute("maxSpareThreads", Integer.valueOf(maxSpareThreads));
    }

    @Persistent(manageable=false)
    public void setMinSpareThreads(int minSpareThreads) {
        connector.setAttribute("minSpareThreads", Integer.valueOf(minSpareThreads));
    }

    @Persistent(manageable=false)
    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        connector.setAttribute("noCompressionUserAgents", noCompressionUserAgents);
    }

    @Persistent(manageable=false)
    public void setPort(int port) {
        connector.setPort(port);
    }

    @Persistent(manageable=false)
    public void setTcpNoDelay(boolean tcpNoDelay) {
        connector.setAttribute("tcpNoDelay", Boolean.valueOf(tcpNoDelay));
    }

    @Persistent(manageable=false)
    public void setTomcatAuthentication(boolean tomcatAuthentication) {
        connector.setAttribute("tomcatAuthentication", Boolean.valueOf(tomcatAuthentication));
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
