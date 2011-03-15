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

import org.apache.catalina.Cluster;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

/**
 * <p>Java class for HostType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="HostType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Alias" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Cluster" type="{}ClusterType" minOccurs="0"/>
 *         &lt;element name="Listener" type="{}ListenerType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Realm" type="{}RealmType" minOccurs="0"/>
 *         &lt;element name="Valve" type="{}ValveType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Context" type="{}ContextType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="appBase" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hostConfigClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="unpackWARs" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="autoDeploy" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="deployOnStartup" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="xmlValidation" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="xmlNamespaceAware" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostType", propOrder = { "alias", "cluster", "listener", "realm", "valve", "context" })
public class HostType {

    @XmlElement(name = "Alias")
    protected List<String> alias;

    @XmlElement(name = "Cluster")
    protected ClusterType cluster;

    @XmlElement(name = "Listener")
    protected List<ListenerType> listener;

    @XmlElement(name = "Realm")
    protected RealmType realm;

    @XmlElement(name = "Valve")
    protected List<ValveType> valve;

    @XmlElement(name = "Context")
    protected List<ContextType> context;

    @XmlAttribute
    protected String className = StandardHost.class.getName();

    @XmlAttribute
    protected String name;

    @XmlAttribute
    protected String appBase;

    @XmlAttribute
    protected String hostConfigClass;

    @XmlAttribute
    protected Boolean unpackWARs;

    @XmlAttribute
    protected Boolean autoDeploy;

    @XmlAttribute
    protected Boolean deployOnStartup;

    @XmlAttribute
    protected Boolean xmlValidation;

    @XmlAttribute
    protected Boolean xmlNamespaceAware;

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the alias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the alias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getAlias() {
        if (alias == null) {
            alias = new ArrayList<String>();
        }
        return this.alias;
    }

    /**
     * Gets the value of the cluster property.
     *
     * @return
     *     possible object is
     *     {@link ClusterType }
     *
     */
    public ClusterType getCluster() {
        return cluster;
    }

    /**
     * Sets the value of the cluster property.
     *
     * @param value
     *     allowed object is
     *     {@link ClusterType }
     *
     */
    public void setCluster(ClusterType value) {
        this.cluster = value;
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
     * Gets the value of the valve property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valve property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValve().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValveType }
     *
     *
     */
    public List<ValveType> getValve() {
        if (valve == null) {
            valve = new ArrayList<ValveType>();
        }
        return this.valve;
    }

    /**
     * Gets the value of the context property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the context property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContext().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContextType }
     *
     *
     */
    public List<ContextType> getContext() {
        if (context == null) {
            context = new ArrayList<ContextType>();
        }
        return this.context;
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
     * Gets the value of the appBase property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAppBase() {
        return appBase;
    }

    /**
     * Sets the value of the appBase property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAppBase(String value) {
        this.appBase = value;
    }

    /**
     * Gets the value of the hostConfigClass property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getHostConfigClass() {
        return hostConfigClass;
    }

    /**
     * Sets the value of the hostConfigClass property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setHostConfigClass(String value) {
        this.hostConfigClass = value;
    }

    /**
     * Gets the value of the unpackWARs property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isUnpackWARs() {
        return unpackWARs;
    }

    /**
     * Sets the value of the unpackWARs property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUnpackWARs(Boolean value) {
        this.unpackWARs = value;
    }

    /**
     * Gets the value of the autoDeploy property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAutoDeploy() {
        return autoDeploy;
    }

    /**
     * Sets the value of the autoDeploy property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAutoDeploy(Boolean value) {
        this.autoDeploy = value;
    }

    /**
     * Gets the value of the deployOnStartup property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDeployOnStartup() {
        return deployOnStartup;
    }

    /**
     * Sets the value of the deployOnStartup property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setDeployOnStartup(Boolean value) {
        this.deployOnStartup = value;
    }

    /**
     * Gets the value of the xmlValidation property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isXmlValidation() {
        return xmlValidation;
    }

    /**
     * Sets the value of the xmlValidation property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setXmlValidation(Boolean value) {
        this.xmlValidation = value;
    }

    /**
     * Gets the value of the xmlNamespaceAware property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isXmlNamespaceAware() {
        return xmlNamespaceAware;
    }

    /**
     * Sets the value of the xmlNamespaceAware property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setXmlNamespaceAware(Boolean value) {
        this.xmlNamespaceAware = value;
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

    public Host getHost(ClassLoader cl) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", getName());
        properties.put("appBase", getAppBase());
        properties.put("hostConfigClass", getHostConfigClass());
        properties.put("unpackWars", isUnpackWARs());
        properties.put("autoDeploy", isAutoDeploy());
        properties.put("deployOnStartup", isDeployOnStartup());
        properties.put("xmlValidation", isXmlValidation());
        properties.put("xmlNamespaceAware", isXmlNamespaceAware());
        if (getCluster() != null) {
            ClusterType clusterType = getCluster();
            Cluster cluster = clusterType.getCluster(cl);
            properties.put("cluster", cluster);
        }
        if (getRealm() != null) {
            Realm realm = getRealm().getRealm(cl);
            properties.put("realm", realm);
        }
        for (Map.Entry<QName, String> entry : otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Host host = (Host) recipe.create(cl);

        for (ListenerType listenerType : getListener()) {
            LifecycleListener listener = listenerType.getLifecycleListener(cl);
            host.addLifecycleListener(listener);
        }

        //alias
        for (String alias : getAlias()) {
            host.addAlias(alias);
        }

        //valve
        Pipeline pipeline = host.getPipeline();
        for (ValveType valveType : getValve()) {
            Valve valve = valveType.getValve(cl);
            pipeline.addValve(valve);
        }

        //context
        //TODO contexts
        return host;
    }
}
