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


package org.apache.geronimo.tomcat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.tomcat.TomcatServerGBean;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;


/**
 * <p>Java class for ConnectorType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ConnectorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Listener" type="{}ListenerType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="allowTrace" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="emptySessionPath" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="enableLookups" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="maxPostSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="maxSavePostSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="port" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="protocol" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="protocolHandlerClassName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="proxyName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="proxyPort" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="redirectPort" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="scheme" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="secure" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="encoding" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="useBodyEncodingForURI" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="xpoweredBy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="useIPVHosts" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConnectorType", propOrder = {
        "listener"
})
public class ConnectorType {

    @XmlElement(name = "Listener")
    protected List<ListenerType> listener;
    @XmlAttribute
    protected String className = Connector.class.getName();
    @XmlAttribute
    protected Boolean allowTrace;
    @XmlAttribute
    protected Boolean emptySessionPath;
    @XmlAttribute
    protected Boolean enableLookups;
    @XmlAttribute
    protected Integer maxParameterCount;
    @XmlAttribute
    protected Integer maxPostSize;
    @XmlAttribute
    protected Integer maxSavePostSize;
    @XmlAttribute
    protected Integer port;
    @XmlAttribute
    protected String protocol;
    @XmlAttribute
    protected String protocolHandlerClassName;
    @XmlAttribute
    protected String proxyName;
    @XmlAttribute
    protected Integer proxyPort;
    @XmlAttribute
    protected Integer redirectPort;
    @XmlAttribute
    protected String scheme;
    @XmlAttribute
    protected Boolean secure;
    @XmlAttribute
    protected String encoding;
    @XmlAttribute
    protected String useBodyEncodingForURI;
    @XmlAttribute
    protected String xpoweredBy;
    @XmlAttribute
    protected Boolean useIPVHosts;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    private static final String EMPTY_SESSION_PATH = "emptySessionPath";
    private static final String ENABLE_LOOKUPS = "enableLookups";
    private static final String MAX_PARAMETER_COUNT = "maxParameterCount";
    private static final String MAX_POST_SIZE = "maxPostSize";
    private static final String MAX_SAVE_POST_SIZE = "maxSavePostSize";
    private static final String PORT = "port";
    private static final String PROTOCOL = "protocol";
    private static final String PROTOCOL_HANDLER_CLASS_NAME = "protocolHandlerClassName";
    private static final String PROXY_NAME = "proxyName";
    private static final String PROXY_PORT = "proxyPort";
    private static final String REDIRECT_PORT = "redirectPort";
    private static final String SCHEME = "scheme";
    private static final String SECURE = "secure";
    private static final String ENCODING = "encoding";
    private static final String USE_BODY_ENCODING_FOR_URI = "useBodyEncodingForURI";
    private static final String X_POWERED_BY = "xPoweredBy";
    private static final String USE_IPVHOSTS = "useIPVHosts";
    private static final String ALLOW_TRACE = "allowTrace";


    /**
     * Gets the value of the listener property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listener property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListener().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ListenerType }
     */
    public List<ListenerType> getListener() {
        if (listener == null) {
            listener = new ArrayList<ListenerType>();
        }
        return this.listener;
    }

    /**
     * Gets the value of the className property.
     *
     * @return possible object is
     *         {@link String }              z
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the allowTrace property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isAllowTrace() {
        return allowTrace;
    }

    /**
     * Sets the value of the allowTrace property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setAllowTrace(Boolean value) {
        this.allowTrace = value;
    }

    /**
     * Gets the value of the emptySessionPath property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isEmptySessionPath() {
        return emptySessionPath;
    }

    /**
     * Sets the value of the emptySessionPath property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setEmptySessionPath(Boolean value) {
        this.emptySessionPath = value;
    }

    /**
     * Gets the value of the enableLookups property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isEnableLookups() {
        return enableLookups;
    }

    /**
     * Sets the value of the enableLookups property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setEnableLookups(Boolean value) {
        this.enableLookups = value;
    }
    
    /**
     * Gets the value of the maxParameterCount property.
     *
     * @return possible object is
     *         {@link Integer }
     */
    public Integer getMaxParameterCount() {
        return maxParameterCount;
    }

    /**
     * Sets the value of the maxParameterCount property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxParameterCount(Integer value) {
        this.maxParameterCount = value;
    }

    /**
     * Gets the value of the maxPostSize property.
     *
     * @return possible object is
     *         {@link Integer }
     */
    public Integer getMaxPostSize() {
        return maxPostSize;
    }

    /**
     * Sets the value of the maxPostSize property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxPostSize(Integer value) {
        this.maxPostSize = value;
    }

    /**
     * Gets the value of the maxSavePostSize property.
     *
     * @return possible object is
     *         {@link Integer }
     */
    public Integer getMaxSavePostSize() {
        return maxSavePostSize;
    }

    /**
     * Sets the value of the maxSavePostSize property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxSavePostSize(Integer value) {
        this.maxSavePostSize = value;
    }

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     *         {@link Integer }
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPort(Integer value) {
        this.port = value;
    }

    /**
     * Gets the value of the protocol property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the value of the protocol property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProtocol(String value) {
        this.protocol = value;
    }

    /**
     * Gets the value of the protocolHandlerClassName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getProtocolHandlerClassName() {
        return protocolHandlerClassName;
    }

    /**
     * Sets the value of the protocolHandlerClassName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProtocolHandlerClassName(String value) {
        this.protocolHandlerClassName = value;
    }

    /**
     * Gets the value of the proxyName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getProxyName() {
        return proxyName;
    }

    /**
     * Sets the value of the proxyName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProxyName(String value) {
        this.proxyName = value;
    }

    /**
     * Gets the value of the proxyPort property.
     *
     * @return possible object is
     *         {@link Integer }
     */
    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets the value of the proxyPort property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setProxyPort(Integer value) {
        this.proxyPort = value;
    }

    /**
     * Gets the value of the redirectPort property.
     *
     * @return possible object is
     *         {@link Integer }
     */
    public Integer getRedirectPort() {
        return redirectPort;
    }

    /**
     * Sets the value of the redirectPort property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setRedirectPort(Integer value) {
        this.redirectPort = value;
    }

    /**
     * Gets the value of the scheme property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the value of the scheme property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScheme(String value) {
        this.scheme = value;
    }

    /**
     * Gets the value of the secure property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isSecure() {
        return secure;
    }

    /**
     * Sets the value of the secure property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSecure(Boolean value) {
        this.secure = value;
    }

    /**
     * Gets the value of the encoding property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the useBodyEncodingForURI property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getUseBodyEncodingForURI() {
        return useBodyEncodingForURI;
    }

    /**
     * Sets the value of the useBodyEncodingForURI property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUseBodyEncodingForURI(String value) {
        this.useBodyEncodingForURI = value;
    }

    /**
     * Gets the value of the xpoweredBy property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getXpoweredBy() {
        return xpoweredBy;
    }

    /**
     * Sets the value of the xpoweredBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setXpoweredBy(String value) {
        this.xpoweredBy = value;
    }

    /**
     * Gets the value of the useIPVHosts property.
     *
     * @return possible object is
     *         {@link Boolean }
     */
    public Boolean isUseIPVHosts() {
        return useIPVHosts;
    }

    /**
     * Sets the value of the useIPVHosts property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setUseIPVHosts(Boolean value) {
        this.useIPVHosts = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * <p/>
     * <p/>
     * the map is keyed by the name of the attribute and
     * the value is the string value of the attribute.
     * <p/>
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }


    public Connector getConnector(ClassLoader cl, Service service) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (isAllowTrace() != null) {
            properties.put(ALLOW_TRACE, isAllowTrace());
        }
        if (isEmptySessionPath() != null) {
            properties.put(EMPTY_SESSION_PATH, isEmptySessionPath());
        }
        if (isEnableLookups() != null) {
            properties.put(ENABLE_LOOKUPS, isEnableLookups());
        }
        if (getMaxParameterCount() != null) {
            properties.put(MAX_PARAMETER_COUNT, getMaxParameterCount());
        }
        if (getMaxPostSize() != null) {
            properties.put(MAX_POST_SIZE, getMaxPostSize());
        }
        if (getMaxSavePostSize() != null) {
            properties.put(MAX_SAVE_POST_SIZE, getMaxSavePostSize());
        }
        if (getPort() != null) {
            properties.put(PORT, getPort());
        }
        if (getProtocol() != null) {
            properties.put(PROTOCOL, getProtocol());
        }
        if (getProtocolHandlerClassName() != null) {
            properties.put(PROTOCOL_HANDLER_CLASS_NAME, getProtocolHandlerClassName());
        }
        if (getProxyName() != null) {
            properties.put(PROXY_NAME, getProxyName());
        }
        if (getProxyPort() != null) {
            properties.put(PROXY_PORT, getProxyPort());
        }
        if (getRedirectPort() != null) {
            properties.put(REDIRECT_PORT, getRedirectPort());
        }
        if (getScheme() != null) {
            properties.put(SCHEME, getScheme());
        }
        if (isSecure() != null) {
            properties.put(SECURE, isSecure());
        }
        if (getEncoding() != null) {
            properties.put(ENCODING, getEncoding());
        }
        if (getUseBodyEncodingForURI() != null) {
            properties.put(USE_BODY_ENCODING_FOR_URI, getUseBodyEncodingForURI());
        }
        if (getXpoweredBy() != null) {
            properties.put(X_POWERED_BY, getXpoweredBy());
        }
        if (isUseIPVHosts() != null) {
            properties.put(USE_IPVHOSTS, isUseIPVHosts());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        recipe.setConstructorArgTypes(new Class[] { String.class });
        recipe.setConstructorArgNames(new String[] { "protocol" });
        Connector connector = (Connector) recipe.create(cl);
        boolean executorSupported = !connector.getProtocolHandlerClassName().equals("org.apache.jk.server.JkCoyoteHandler");
        for (Map.Entry<QName, String> entry : otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            String value = entry.getValue();
            if (executorSupported && "executor".equals(name)) {
                Executor executor = service.getExecutor(entry.getValue());
                if (executor == null) {
                    throw new IllegalArgumentException("No executor found in service with name: " + value);
                }
                IntrospectionUtils.callMethod1(connector.getProtocolHandler(),
                        "setExecutor",
                        executor,
                        java.util.concurrent.Executor.class.getName(),
                        cl);

            } else if ("name".equals(name)) {
                //name attribute is held by Geronimo to identify the connector, it is not required by Tomcat
                TomcatServerGBean.ConnectorName.put(connector, value);
            } else {
                if ("keystorePass".equals(name)) {
                    value = (String) EncryptionManager.decrypt(value);
                }
                connector.setProperty(name, value);
            }
        }

        for (ListenerType listenerType : getListener()) {
            LifecycleListener listener = listenerType.getLifecycleListener(cl);
            connector.addLifecycleListener(listener);
            TomcatServerGBean.LifecycleListeners.add(listener);
        }
        return connector;
    }
}
