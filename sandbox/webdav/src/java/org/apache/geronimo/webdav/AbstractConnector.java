/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.webdav;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * Base implementation for the Connector contracts.
 *
 * @version $Revision: 1.5 $ $Date: 2004/06/10 23:12:25 $
 */
public abstract class AbstractConnector
    implements Connector, GBeanLifecycle
{
 
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Port.
     */
    protected int port;

    /**
     * Protocol.
     */
    protected String protocol;

    /**
     * Host.
     */
    private String host;

    /**
     * Maximum number of connections.
     */
    protected int maxCon;

    /**
     * Maximum idle time.
     */
    protected int maxIdle;

    /**
     * Creates a connector having the specified specificities.
     *
     * @param aProtocol Protocol.
     * @param anHost Host.
     * @param aPort Port.
     * @param aMaxCon Maximum number of connections.
     * @param aMaxIdle Maximum idle time.
     */
    public AbstractConnector(String aProtocol, String anHost, int aPort, int aMaxCon, int aMaxIdle) {
        protocol = aProtocol;
        host = anHost;
        port = aPort;
        maxCon = aMaxCon;
        maxIdle = aMaxIdle;
    }

    public void setPort(int aPort) {
        port = aPort;
    }

    public int getPort() {
        return port;
    }

    public void setProtocol(String aProtocol) {
        protocol = aProtocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setInterface(String anInterface) {
        host = anInterface;
    }

    /**
     * Gets the interface/host of this Connector.
     * <BR>
     * If it has not been set explicitely, then the host name of the localhost
     * is set and returned.
     *
     * @return Interface.
     */
    public synchronized String getInterface() {
        if (null != host) {
            return host;
        }
        try {
            host = InetAddress.getLocalHost().getHostName();
            return host;
        } catch (UnknownHostException e) {
            // Should not happen.
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public void setMaxConnections(int aMaxConnects) {
        maxCon = aMaxConnects;
    }

    public int getMaxConnections() {
        return maxCon;
    }

    public void setMaxIdleTime(int aMaxIdleTime) {
        maxIdle = aMaxIdleTime;
    }

    public int getMaxIdleTime() {
        return maxIdle;
    }

    private final static GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Abstract Connector", AbstractConnector.class.getName());
        infoFactory.addAttribute("Port", int.class, true);
        infoFactory.addAttribute("Protocol", String.class, true);
        infoFactory.addAttribute("Interface", String.class, true);
        infoFactory.addAttribute("MaxConnections", int.class, true);
        infoFactory.addAttribute("MaxIdleTime", int.class, true);
        infoFactory.setConstructor(new String[]{"Protocol", "Interface", "Port", "MaxConnections", "MaxIdleTime"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
