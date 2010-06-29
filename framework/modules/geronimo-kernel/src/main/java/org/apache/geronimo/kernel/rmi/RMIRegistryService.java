/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * Thin GBean wrapper around the RMI Registry.
 *
 * @version $Rev$ $Date$
 */
public class RMIRegistryService implements GBeanLifecycle {
    
    private static final Logger log = LoggerFactory.getLogger(RMIRegistryService.class);
    
    private int port = Registry.REGISTRY_PORT;
    private String host = "0.0.0.0";
    private String classLoaderSpi;
    private Registry registry;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    public String getProtocol() {
        return "rmi";
    }

    public String getClassLoaderSpi() {
        return classLoaderSpi;
    }
    
    public void setClassLoaderSpi(String classLoaderSpi) {
        this.classLoaderSpi = classLoaderSpi;
    }
    
    public void doStart() throws Exception {
        if (classLoaderSpi != null) {
            System.setProperty("java.rmi.server.RMIClassLoaderSpi", classLoaderSpi);
        }
        if (System.getProperty("java.rmi.server.hostname") == null && host != null && !host.equals("0.0.0.0")) {
            System.setProperty("java.rmi.server.hostname", host);
        }
        RMIClientSocketFactory socketFactory = RMISocketFactory.getDefaultSocketFactory();
        RMIServerSocketFactory serverSocketFactory = new GeronimoRMIServerSocketFactory(host);
        registry = LocateRegistry.createRegistry(port, socketFactory, serverSocketFactory);
        log.debug("Started RMI Registry on port: {}", port);
    }

    public void doStop() throws Exception {
        UnicastRemoteObject.unexportObject(registry, true);
        log.debug("Stopped RMI Registry");
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            log.warn("RMI Registry failed");
        }
    }

    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("RMI Naming", RMIRegistryService.class);
        infoFactory.addAttribute("host", String.class, true, true);
        infoFactory.addAttribute("protocol", String.class, false);
        infoFactory.addAttribute("port", int.class, true, true);
        infoFactory.addAttribute("classLoaderSpi", String.class, true);
        infoFactory.addAttribute("listenAddress", InetSocketAddress.class, false);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
