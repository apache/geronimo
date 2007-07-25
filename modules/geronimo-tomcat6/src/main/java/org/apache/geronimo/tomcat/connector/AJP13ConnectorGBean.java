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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

public class AJP13ConnectorGBean extends ConnectorGBean implements Ajp13Protocol{

    public AJP13ConnectorGBean(String name, String address, int port, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        super(name, "AJP/1.3", container, serverInfo);
        
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

    public String getAddress() {
        Object value = connector.getAttribute("address");
        if (value == null) {
            return "0.0.0.0";
        } else if (value instanceof InetAddress) {
            return ((InetAddress) value).getHostAddress();
        } else
            return value.toString();
    } 

    public int getBackLog() {
        Object value = connector.getAttribute("backLog");
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
        return (String) connector.getAttribute("Executor");
    }

    public int getKeepAliveTimeout() {
        Object value = connector.getAttribute("keepAliveTimeout");
        return value == null ? getConnectionTimeout() : Integer.parseInt(value.toString());
    }

    public int getMaxThreads() {
        Object value = connector.getAttribute("maxThreads");
        return value == null ? 200 : Integer.parseInt(value.toString());
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

    public void setBackLog(int backLog) {
        connector.setAttribute("backLog", new Integer(backLog));
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

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        connector.setAttribute("keepAliveTimeout", keepAliveTimeout);        
    }

    public void setMaxThreads(int maxThreads) {
        connector.setAttribute("maxThreads", maxThreads);        
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
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Connector", AJP13ConnectorGBean.class, ConnectorGBean.GBEAN_INFO);
        infoFactory.addInterface(Http11Protocol.class, 
                new String[] {
                    //AJP Attributes
                    "address", 
                    "backlog", 
                    "bufferSize", 
                    "connectionTimeout", 
                    "executor", 
                    "keepAliveTimeout", 
                    "port", 
                    "tcpNoDelay", 
                    "tomcatAuthentication", 
                },
                new String[] {
                    //AJP Attributes
                    "address", 
                    "backlog", 
                    "bufferSize", 
                    "connectionTimeout", 
                    "executor", 
                    "keepAliveTimeout", 
                    "port", 
                    "tcpNoDelay", 
                    "tomcatAuthentication", 
                }
        );
        infoFactory.setConstructor(new String[] { "name", "address", "port", "TomcatContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
