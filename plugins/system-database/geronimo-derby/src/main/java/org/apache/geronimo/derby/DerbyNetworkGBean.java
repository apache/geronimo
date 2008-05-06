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
package org.apache.geronimo.derby;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * A GBean that manages remote network access to the embedded Derby server.
 *
 * todo need to figure out how to configure this without using system properties
 * @version $Rev$ $Date$
 */
public class DerbyNetworkGBean implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(DerbyNetworkGBean.class);

    private NetworkServerControl network;
    private String host = "localhost";
    private int port = 1527;

    public DerbyNetworkGBean(DerbySystem system) {
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

    public InetSocketAddress getAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    public void doStart() throws Exception {
        InetAddress address = InetAddress.getByName(host);
        network = new NetworkServerControl(address, port);
        network.start(null); // todo work out how to add this to our log stream
        log.debug("Started on host " + host + ':' + port);
    }

    public void doStop() throws Exception {
        if (network != null) {
            try {
                network.shutdown();
            } finally {
                network = null;
            }
        }
        log.debug("Stopped");
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Derby Connector", DerbyNetworkGBean.class);
        infoFactory.addAttribute("host", String.class, true, true);
        infoFactory.addAttribute("port", Integer.TYPE, true, true);
        infoFactory.addAttribute("address", InetSocketAddress.class, false);
        infoFactory.addReference("derbySystem", DerbySystem.class, "GBean");
        infoFactory.setConstructor(new String[]{"derbySystem"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
