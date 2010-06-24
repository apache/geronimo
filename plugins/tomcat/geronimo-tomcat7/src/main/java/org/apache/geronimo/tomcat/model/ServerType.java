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

import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.NamingResources;
import org.apache.geronimo.kernel.Kernel;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Java class for ServerType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ServerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Listener" type="{}ListenerType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="GlobalNamingResources" type="{}NamingResourcesType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Service" type="{}ServiceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="port" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="shutdown" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServerType", propOrder = {
    "listener",
    "globalNamingResources",
    "service"
})
public class ServerType {

    private static final Logger logger = LoggerFactory.getLogger(ServerType.class);

    @XmlElement(name = "Listener")
    protected List<ListenerType> listener;
    @XmlElement(name = "GlobalNamingResources")
    protected List<NamingResourcesType> globalNamingResources;
    @XmlElement(name = "Service")
    protected List<ServiceType> service;
    @XmlAttribute
    protected String className = StandardServer.class.getName();
    @XmlAttribute
    protected Integer port;
    @XmlAttribute
    protected String shutdown;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the listener property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listener property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListener().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link org.apache.geronimo.tomcat.model.ListenerType }
     *
     *
     */
    public List<ListenerType> getListener() {
        if (listener == null) {
            listener = new ArrayList<ListenerType>();
        }
        return this.listener;
    }

    /**
     * Gets the value of the globalNamingResources property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the globalNamingResources property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGlobalNamingResources().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NamingResourcesType }
     *
     *
     */
    public List<NamingResourcesType> getGlobalNamingResources() {
        if (globalNamingResources == null) {
            globalNamingResources = new ArrayList<NamingResourcesType>();
        }
        return this.globalNamingResources;
    }

    /**
     * Gets the value of the service property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the service property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getService().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceType }
     *
     *
     */
    public List<ServiceType> getService() {
        if (service == null) {
            service = new ArrayList<ServiceType>();
        }
        return this.service;
    }

    /**
     * Gets the value of the className property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the port property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setPort(Integer value) {
        this.port = value;
    }

    /**
     * Gets the value of the shutdown property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getShutdown() {
        return shutdown;
    }

    /**
     * Sets the value of the shutdown property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setShutdown(String value) {
        this.shutdown = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * <p>
     * the map is keyed by the name of the attribute and
     * the value is the string value of the attribute.
     *
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     *
     *
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public Server build(ClassLoader cl, Kernel kernel) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Map.Entry<QName, String> entry: otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Server instance = (Server) recipe.create(cl);
        instance.setPort(port);
        instance.setShutdown(shutdown);

        for (ListenerType listenerType : getListener()) {
            LifecycleListener listener = listenerType.getLifecycleListener(cl);
            instance.addLifecycleListener(listener);
        }

        NamingResources globalNamingResources = new NamingResources();
        if(getGlobalNamingResources().size() > 0) {
            logger.warn("All the resource settings in the server.xml are ignored, please use Geronimo deployment plan to define those configurations");
        }
        /*
        for (NamingResourcesType naming: getGlobalNamingResources()) {
            naming.merge(globalNamingResources, cl);
        }
        */
        instance.setGlobalNamingResources(globalNamingResources);

        for (ServiceType serviceType: getService()) {
            Service service = serviceType.getService(cl, kernel);
            instance.addService(service);
        }

        return instance;
    }

}
