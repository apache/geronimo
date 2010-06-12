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

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardEngine;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;


/**
 * <p>Java class for EngineType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EngineType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Realm" type="{}RealmType" minOccurs="0"/>
 *         &lt;element name="Host" type="{}HostType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Cluster" type="{}ClusterType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Listener" type="{}ListenerType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="defaultHost" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jvmRoute" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="backgroundProcessorDelay" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EngineType", propOrder = {
    "realm",
    "host",
    "cluster",
    "listener"
})
public class EngineType {

    @XmlElement(name = "Realm")
    protected RealmType realm;
    @XmlElement(name = "Host")
    protected List<HostType> host;
    @XmlElement(name = "Cluster")
    protected ClusterType cluster;
    @XmlElement(name = "Listener")
    protected List<ListenerType> listener;
    @XmlAttribute
    protected String className = StandardEngine.class.getName();
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String defaultHost;
    @XmlAttribute
    protected String jvmRoute;
    @XmlAttribute
    protected Integer backgroundProcessorDelay;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the realm property.
     *
     * @return
     *     possible object is
     *     {@link RealmType }
     *
     */
    public RealmType getRealm() {
        return realm;
    }

    /**
     * Sets the value of the realm property.
     *
     * @param value
     *     allowed object is
     *     {@link RealmType }
     *
     */
    public void setRealm(RealmType value) {
        this.realm = value;
    }

    /**
     * Gets the value of the host property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the host property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHost().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HostType }
     *
     *
     */
    public List<HostType> getHost() {
        if (host == null) {
            host = new ArrayList<HostType>();
        }
        return this.host;
    }

    public ClusterType getCluster() {
        return cluster;
    }

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
     * {@link ListenerType }
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
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the defaultHost property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultHost() {
        return defaultHost;
    }

    /**
     * Sets the value of the defaultHost property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultHost(String value) {
        this.defaultHost = value;
    }

    /**
     * Gets the value of the jvmRoute property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getJvmRoute() {
        return jvmRoute;
    }

    /**
     * Sets the value of the jvmRoute property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setJvmRoute(String value) {
        this.jvmRoute = value;
    }

    /**
     * Gets the value of the backgroundProcessorDelay property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getBackgroundProcessorDelay() {
        return backgroundProcessorDelay;
    }

    /**
     * Sets the value of the backgroundProcessorDelay property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setBackgroundProcessorDelay(Integer value) {
        this.backgroundProcessorDelay = value;
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

    public Engine getEngine(ClassLoader cl) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", getName());
        properties.put("defaultHost", getDefaultHost());
        properties.put("jvmRoute", getJvmRoute());
        properties.put("backgroundProcessorDelay", getBackgroundProcessorDelay());

        for (Map.Entry<QName, String> entry: otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Engine engine = (Engine) recipe.create(cl);
//        Class<? extends Engine> engineClass = cl.loadClass(className).asSubclass(Engine.class);
//        Engine engine = engineClass.newInstance();
//        engine.setName(name);
//        engine.setDefaultHost(defaultHost);
//        engine.setJvmRoute(jvmRoute);
//        engine.setBackgroundProcessorDelay(backgroundProcessorDelay);
        //realm
        if (this.realm != null) {
            Realm realm = this.realm.getRealm(cl);
            engine.setRealm(realm);
        }
        //host
        for (HostType hostType: getHost()) {
            Host host = hostType.getHost(cl);
            engine.addChild(host);
        }
        //cluster
        if (getCluster() != null) {
            engine.setCluster(getCluster().getCluster(cl));
        }
        //listener
        for (ListenerType listenerType : getListener()) {
            LifecycleListener listener = listenerType.getLifecycleListener(cl);
            engine.addLifecycleListener(listener);
        }

        return engine;
    }
}
