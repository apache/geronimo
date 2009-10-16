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
package org.apache.geronimo.jmxremoting;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.NotificationFilterSupport;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.rmi.GeronimoRMIServerSocketFactory;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connector that supports the server side of JSR 160 JMX Remoting.
 *
 * @version $Rev$ $Date$
 */
public class JMXConnector implements JMXConnectorInfo, GBeanLifecycle {
    protected final MBeanServer mbeanServer;
    protected final Logger log;
    protected final ClassLoader classLoader;
    protected String applicationConfigName;

    protected String protocol;
    protected String host;
    protected int port = -1;
    protected String urlPath;

    protected JMXConnectorServer server;
    protected JMXServiceURL jmxServiceURL;

    // todo remove this as soon as Geronimo supports factory beans
    public JMXConnector(MBeanServerReference mbeanServerReference, String objectName, ClassLoader classLoader) {
        this(mbeanServerReference.getMBeanServer(), objectName, classLoader);
    }

    /**
     * Constructor for creating the connector. The ClassLoader must be
     * able to load all the LoginModules used in the JAAS login
     *
     * @param mbeanServer the mbean server
     * @param objectName  this connector's object name
     * @param classLoader the classLoader used to create this connector
     */
    public JMXConnector(MBeanServer mbeanServer, String objectName, ClassLoader classLoader) {
        this.mbeanServer = mbeanServer;
        this.classLoader = classLoader;
        log = LoggerFactory.getLogger(objectName);
    }

    /**
     * Return the name of the JAAS Application Configuration Entry this
     * connector uses to authenticate users. If null, users are not
     * be authenticated (not recommended).
     *
     * @return the authentication configuration name
     */
    public String getApplicationConfigName() {
        return applicationConfigName;
    }

    /**
     * Set the name of the JAAS Application Configuration Entry this
     * connector should use to authenticate users. If null, users will not
     * be authenticated (not recommended).
     *
     * @param applicationConfigName the authentication configuration name
     */
    public void setApplicationConfigName(String applicationConfigName) {
        this.applicationConfigName = applicationConfigName;
    }

    /**
     * Every connector must specify a property of type InetSocketAddress
     * because we use that to identify the network services to print a list
     * during startup.  However, this can be read-only since the host and port
     * are set in the url attribute.
     */
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    /**
     * Gets the protocol to use for the connection.
     *
     * @return the protocol to use for the connection
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol to use for the connection.
     *
     * @param protocol the protocol to use for the connection
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the JMX host for this connector.
     *
     * @return the JMX host for this connector
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the JMX host for this connector.
     *
     * @param host the JMX host for this connector
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the JMX port for this connector.
     *
     * @return the JMX port for this connector
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the JMX port for this connector.
     *
     * @param port the JMX port for this connector
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the path within the target server to look for the connection.  This is commonly
     * /jndi/rmi://localhost:1099/JMXConnector
     *
     * @return the path used to loacate the connector on the target server
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * Sets the path within the target server to look for the connection.  This is commonly
     * /jndi/rmi://localhost:1099/JMXConnector
     *
     * @param urlPath the path used to loacate the connector on the target server
     */
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public void doStart() throws Exception {
        jmxServiceURL = new JMXServiceURL(protocol, host, port, urlPath);
        Authenticator authenticator = null;
        Map<String, Object> env = new HashMap<String, Object>();
        if (applicationConfigName != null) {
            authenticator = new Authenticator(applicationConfigName, classLoader);
            env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        } else {
            log.warn("Starting unauthenticating JMXConnector for " + jmxServiceURL);
        }
        RMIServerSocketFactory serverSocketFactory = new GeronimoRMIServerSocketFactory(host);
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverSocketFactory);
        server = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceURL, env, mbeanServer);
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(JMXConnectionNotification.OPENED);
        filter.enableType(JMXConnectionNotification.CLOSED);
        filter.enableType(JMXConnectionNotification.FAILED);
        server.addNotificationListener(authenticator, filter, null);
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            server.start();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
        log.debug("Started JMXConnector " + server.getAddress());
    }

    public void doStop() throws Exception {
        try {
            server.stop();
        } catch (IOException e) {
            // java.io.IOException is expected.
        } catch (Exception e) {
            // Otherwise, something bad happened.  Rethrow the exception.
            throw e;
        } finally {
            server = null;
            log.debug("Stopped JMXConnector " + jmxServiceURL);
        }
    }

    public void doFail() {
        try {
            doStop();
            log.warn("Failure in JMXConnector " + jmxServiceURL);
        } catch (Exception e) {
            log.warn("Error stopping JMXConnector after failure", e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("JMX Remoting Connector", JMXConnector.class);
        infoFactory.addReference("MBeanServerReference", MBeanServerReference.class);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("protocol", String.class, true, true);
        infoFactory.addAttribute("host", String.class, true, true);
        infoFactory.addAttribute("port", int.class, true, true);
        infoFactory.addAttribute("urlPath", String.class, true, true);
        infoFactory.addAttribute("applicationConfigName", String.class, true, true);

        infoFactory.addInterface(JMXConnectorInfo.class);

        infoFactory.setConstructor(new String[]{"MBeanServerReference", "objectName", "classLoader"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
