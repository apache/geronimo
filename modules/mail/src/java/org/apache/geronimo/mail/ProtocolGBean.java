/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.mail;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;


/**
 * A generic GBean that provides for the configuration of a JavaMail protocol.
 * <p/>
 * Values that are set in the individual member variables will override any of
 * the corresponding values that have been set in the properties set.
 *
 * @version $Rev: $ $Date: $
 */
public class ProtocolGBean implements GBeanLifecycle {

    private final Log log = LogFactory.getLog(ProtocolGBean.class);

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
        if (host != null) props.put("mail." + protocol + ".host", host);
        if (user != null) props.put("mail." + protocol + ".user", user);
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Started " + objectName);
    }

    public void doStop() throws WaitingException, Exception {
        log.info("Stopped " + objectName);
    }

    public void doFail() {
        log.info("Failed " + objectName);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ProtocolGBean.class);

        infoFactory.addAttribute(new GAttributeInfo("objectName", String.class.getName(), false, "getObjectName", null));
        infoFactory.addAttribute(new GAttributeInfo("protocol", String.class.getName(), true, "getProtocol", null));
        infoFactory.addAttribute(new GAttributeInfo("properties", Properties.class.getName(), true, "getProperties", "setProperties"));
        infoFactory.addAttribute(new GAttributeInfo("host", String.class.getName(), true, "getHost", "setHost"));
        infoFactory.addAttribute(new GAttributeInfo("user", String.class.getName(), true, "getUser", "setUser"));
        infoFactory.addOperation("addOverrides", new Class[]{Properties.class});

        infoFactory.setConstructor(new String[]{"objectName", "protocol", "properties", "host", "user"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
