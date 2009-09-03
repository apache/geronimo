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
    
    public AJP13ConnectorGBean(@ParamAttribute(name = "name") String name,
                               @ParamAttribute(name = "initParams") Map<String, String> initParams,
                               @ParamAttribute(name = "host") String host,
                               @ParamAttribute(name = "port") int port,
                               @ParamReference(name = "TomcatContainer") TomcatContainer container,
                               @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                               @ParamAttribute(name = "connector") Connector conn)  throws Exception {
    

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
         
         return (String) value.getClass().getName();
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
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }

    public boolean getTomcatAuthentication() {
        Object value = connector.getAttribute("tomcatAuthentication");
        return value == null ? true : new Boolean(value.toString()).booleanValue();
    }

    public void setAddress(String address) {
        connector.setAttribute("address", address);
    }

    public void setBacklog(int backlog) {
        connector.setAttribute("backlog", new Integer(backlog));
    }

    public void setBufferSize(int bufferSize) {
        connector.setAttribute("bufferSize", new Integer(bufferSize));
    }

    public void setConnectionTimeout(int connectionTimeout) {
        connector.setAttribute("connectionTimeout", new Integer(connectionTimeout));
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

    public void setMaxThreads(int maxThreads) {
        connector.setAttribute("maxThreads", maxThreads);        
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

    public void setTcpNoDelay(boolean tcpNoDelay) {
        connector.setAttribute("tcpNoDelay", new Boolean(tcpNoDelay));
    }

    public void setTomcatAuthentication(boolean tomcatAuthentication) {
        connector.setAttribute("tomcatAuthentication", new Boolean(tomcatAuthentication));
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
