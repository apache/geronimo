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
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.geronimo.corba;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException; 
import org.apache.geronimo.system.serverinfo.ServerInfo;

import org.apache.geronimo.corba.security.config.ConfigAdapter;

import java.net.InetSocketAddress;

/**
 * Starts the openejb transient cos naming service.
 * <p/>
 * <gbean name="NameServer" class="org.apache.geronimo.corba.NameService">
 * <reference name="ServerInfo">
 * <reference name="ConfigAdapter">
 * <attribute name="port">2809</attribute>
 * <attribute name="host">localhost</attribute>
 * </gbean>
 *
 * @version $Revision: 465108 $ $Date: 2006-10-17 17:23:40 -0700 (Tue, 17 Oct 2006) $
 */
public class NameService implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(NameService.class);

    // the ORB configurator
    private final ConfigAdapter config;
    // the name service instance
    private Object service;
    // the name service listening port
    private final int port;
    // the published port name (defaults to "localhost").
    private String host;
    // indicates whether we start and host this server locally.
    private boolean localServer;

    protected NameService() {
        service = null;
        config = null;
        port = -1;
        host = "localhost";
        localServer = true;
    }

    /**
     * GBean constructor to create a NameService instance.
     *
     * @param serverInfo The dependent ServerInfo.  This value is not used,
     *                   but is in the constructor to create an ordering
     *                   dependency.
     * @param config     The ORB ConfigAdapter used to create the real
     *                   NameService instance.
     * @param host       The advertised host name.
     * @param port       The listener port.
     *
     * @exception Exception
     */
    public NameService(ServerInfo serverInfo, ConfigAdapter config, String host, int port) throws Exception {
        this.host = host;
        this.port = port;
        this.config = config;
        localServer = true;
        service = null;
        // if not specified, our default host is "localhost".
        if (this.host == null) {
            this.host = "localhost";
        }
    }

    /**
     * Retrieve the host name for this NameService instance.
     *
     * @return The String host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the port information for this NameService instance.
     *
     * @return The configured name service listener port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the "local" value for this server.  If true, an
     * in-process NameService instance will be created when
     * the service is started.  If false, this is an
     * indirect reference to a NameService (possibly located
     * elsewhere).
     *
     * @return The current localServer value.  The default is
     *         true.
     */
    public boolean getLocal() {
        return localServer;
    }

    /**
     * Get the "local" value for this server.  If true, an
     * in-process NameService instance will be created when
     * the service is started.  If false, this is an
     * indirect reference to a NameService (possibly located
     * elsewhere).
     *
     * @param l      The new local setting.
     */
    public void setLocal(boolean l) {
        localServer = l;
    }

    /**
     * Get the InetSocketAddress for this NameService.
     *
     * @return An InetSocketAddress containing the host and port
     *         information.
     */
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(host, getPort());
    }


    /**
     * Return the NameService locator as a URI (generally
     * using the corbaloc:: protocol);
     *
     * @return The URI in String format.
     */
    public String getURI() {
        return "corbaloc::" + host + ":" + port + "/NameService";
    }

    /**
     * Start the NameService instance.  If the local
     * setting is true, will launch an appropriate
     * in-process name server instance.
     *
     * @exception Exception
     */
    public void doStart() throws Exception {
        if (localServer) {
            try {
                service = config.createNameService(host, port);
                log.debug("Started transient CORBA name service on port " + port);
            } catch (NoSuchMethodError e) {
                log.error("Incorrect level of org.omg.CORBA classes found.\nLikely cause is an incorrect java.endorsed.dirs configuration"); 
                throw new InvalidConfigurationException("CORBA usage requires Yoko CORBA spec classes in java.endorsed.dirs classpath", e); 
            }
        }
    }

    /**
     * Stop the name server.  Only has an effect if doStart()
     * launched an NameServer instance.
     *
     * @exception Exception
     */
    public void doStop() throws Exception {
        if (service != null) {
            config.destroyNameService(service);
            log.debug("Stopped transient CORBA name service on port " + port);
        }
    }

    public void doFail() {
        if (service != null) {
            config.destroyNameService(service);
            log.warn("Failed transient CORBA name service on port " + port);
        }
    }
}
