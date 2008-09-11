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
import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Rev$ $Date$
 */
public class EjbDaemonGBean implements NetworkConnector, GBeanLifecycle {
    private String host;
    private int port;
    private int threads;
    private ServiceManager serviceManager;

    private String multicastHost;
    private int multicastPort;
    private boolean enableMulticast;

    public EjbDaemonGBean() {
        System.setProperty("openejb.nobanner","true");
        serviceManager = new ServiceManager();
    }

    public String getProtocol() {
        return "ejbd";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public boolean isEnableMulticast() {
        return enableMulticast;
    }

    public void setEnableMulticast(boolean enableMulticast) {
        this.enableMulticast = enableMulticast;
    }

    public String getMulticastHost() {
        return multicastHost;
    }

    public void setMulticastHost(String multicastHost) {
        this.multicastHost = multicastHost;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(host, port);
    }

    public void doStart() throws Exception {
        Properties properties = SystemInstance.get().getProperties();
        properties.setProperty("ejbd.bind", host);
        properties.setProperty("ejbd.port", Integer.toString(port));
        properties.setProperty("multicast.bind", multicastHost);
        properties.setProperty("multicast.port", Integer.toString(multicastPort));
        properties.setProperty("multicast.disabled", Boolean.toString(!enableMulticast));
        if (threads > 0) {
            properties.setProperty("ejbd.threads", Integer.toString(threads));
        }

        serviceManager.init();
        serviceManager.start(false);

    }

    public void doStop() throws Exception {
        serviceManager.stop();
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("OpenEJB Daemon", EjbDaemonGBean.class);
        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", int.class, true);
        infoBuilder.addAttribute("multicastHost", String.class, true);
        infoBuilder.addAttribute("multicastPort", int.class, true);
        infoBuilder.addAttribute("enableMulticast", boolean.class, true);
        infoBuilder.addAttribute("threads", int.class, true);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
