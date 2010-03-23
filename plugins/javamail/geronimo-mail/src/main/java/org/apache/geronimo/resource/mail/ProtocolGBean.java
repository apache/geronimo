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
package org.apache.geronimo.resource.mail;

import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * A generic GBean that provides for the configuration of a JavaMail protocol.
 * <p/>
 * Values that are set in the individual member variables will override any of
 * the corresponding values that have been set in the properties set.
 *
 * @version $Rev$ $Date$
 */
public class ProtocolGBean implements GBeanLifecycle {

    // common attributes exported by all ProtocolBeans
    static public final String GBEAN_OBJECTNAME = "objectName";
    static public final String GBEAN_PROTOCOL = "protocol";
    static public final String GBEAN_PROPERTIES = "properties";
    static public final String GBEAN_HOST = "host";
    static public final String GBEAN_USER = "user";
    static public final String GBEAN_ADD_OVERRIDES = "addOverrides";

    // common constants for GBEAN properties that are used by a number of transports.
    static public final String GBEAN_PORT = "port";
    static public final String GBEAN_CONNECTION_TIMEOUT = "connectionTimeout";
    static public final String GBEAN_TIMEOUT = "timeout";
    static public final String GBEAN_FROM = "from";
    static public final String GBEAN_AUTH = "auth";
    static public final String GBEAN_REALM = "saslRealm";
    static public final String GBEAN_QUITWAIT = "quitWait";
    static public final String GBEAN_FACTORY_CLASS = "socketFactoryClass";
    static public final String GBEAN_FACTORY_FALLBACK = "socketFactoryFallback";
    static public final String GBEAN_FACTORY_PORT = "socketFactoryPort";
    static public final String GBEAN_LOCALHOST = "localhost";
    static public final String GBEAN_LOCALADDRESS = "localaddress";
    static public final String GBEAN_LOCALPORT = "localport";

    private static final Logger log = LoggerFactory.getLogger(ProtocolGBean.class);

    private final String objectName;
    private Properties properties;
    private final String protocol;
    private String host;
    private String user;

    /**
     * Construct an instance of ProtocolGBean
     */
    public ProtocolGBean() {
        this.objectName = null;
        this.protocol = null;
        this.properties = null;
    }

    /**
     * Construct an instance of ProtocolGBean
     * <p/>
     * Values that are set in the individual member variables will override any of
     * the corresponding values that have been set in the properties set.
     *
     * @param objectName the object name of the protocol
     * @param protocol   the name of the protocol
     * @param properties the set of default properties for the protocol
     * @param host       the host the protocol connects to
     * @param user       the default name for the protocol
     */
    public ProtocolGBean(String objectName, String protocol, Properties properties, String host, String user) {
        assert protocol != null;

        this.objectName = objectName;
        this.protocol = protocol;
        this.properties = (properties == null ? new Properties() : properties);
        this.host = host;
        this.user = user;
    }

    /**
     * Returns the object name of this protocol GBean
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Returns the set of default properties for the protocol.
     * <p/>
     * Values that are set in the individual member variables will override any of
     * the corresponding values that have been set in the properties set.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the set of default properties for the protocol.
     * <p/>
     * Values that are set in the individual member variables will override any of
     * the corresponding values that have been set in the properties set.
     *
     * @param properties set of default properties for the protocol
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns the name of the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the host the protocol connects to.
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the host the protocol connects to.
     *
     * @param host the host the protocol connects to
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the default user name for the protocol.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the default user name for the protocol.
     *
     * @param user the default user name for the protocol
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Add the overrides from the member variables to the properties file.
     */
    public void addOverrides(Properties props) {
        Enumeration keys = properties.propertyNames();

        // copy the properties attribute into the over rides as well.  These are copied
        // with the key names unchanged, so they must be specified fully qualified.
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            props.put(key, properties.getProperty(key));
        }

        if (host != null) props.put("mail." + protocol + ".host", host);
        if (user != null) props.put("mail." + protocol + ".user", user);
    }

    public void doStart() throws Exception {
        log.debug("Started " + objectName);
    }

    public void doStop() throws Exception {
        log.debug("Stopped " + objectName);
    }

    public void doFail() {
        log.warn("Failed " + objectName);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ProtocolGBean.class); //TODO just a gbean?

        infoFactory.addAttribute(GBEAN_OBJECTNAME, String.class, false);
        infoFactory.addAttribute(GBEAN_PROTOCOL, String.class, true);
        infoFactory.addAttribute(GBEAN_PROPERTIES, Properties.class, true);
        infoFactory.addAttribute(GBEAN_HOST, String.class, true);
        infoFactory.addAttribute(GBEAN_USER, String.class, true);
        infoFactory.addOperation(GBEAN_ADD_OVERRIDES, new Class[]{Properties.class});

        infoFactory.setConstructor(new String[]{GBEAN_OBJECTNAME, GBEAN_PROTOCOL, GBEAN_PROPERTIES, GBEAN_HOST, GBEAN_USER});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
