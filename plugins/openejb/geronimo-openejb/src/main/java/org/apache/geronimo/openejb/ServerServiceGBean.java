/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.openejb.server.ServerService;

public class ServerServiceGBean implements NetworkConnector {

    private ServerService service;

    void setServerService(ServerService service) {
        this.service = service;
    }
    
    public String getHost() {
        return service.getIP();
    }

    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(service.getIP(), service.getPort());
    }

    public int getPort() {
        return service.getPort();
    }

    public String getProtocol() {
        return service.getName();
    }

    public void setHost(String arg0) throws UnknownHostException {
    }

    public void setPort(int arg0) {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("OpenEJB Daemon", ServerServiceGBean.class);
        infoBuilder.addInterface(NetworkConnector.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
