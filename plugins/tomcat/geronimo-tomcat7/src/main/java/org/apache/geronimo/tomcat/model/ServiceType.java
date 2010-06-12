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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.namespace.QName;

import org.apache.catalina.Service;
import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Engine;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.connector.Connector;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.tomcat.TomcatServerGBean;


/**
 * <p>Java class for ServiceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ServiceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Connector" type="{}ConnectorType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Engine" type="{}EngineType" minOccurs="0"/>
 *         &lt;element name="Listener" type="{}ListenerType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Executor" type="{}ExecutorType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="className" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceType", propOrder = {
    "connector",
    "engine",
    "listener",
    "executor"
})
public class ServiceType {

    @XmlElement(name = "Connector")
    protected List<ConnectorType> connector;
    @XmlElement(name = "Engine")
    protected EngineType engine;
    @XmlElement(name = "Listener")
    protected List<ListenerType> listener;
    @XmlElement(name = "Executor")
    protected List<ExecutorType> executor;
    @XmlAttribute
    protected String className = StandardService.class.getName();
    @XmlAttribute
    protected String name;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the connector property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnector().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConnectorType }
     *
     *
     */
    public List<ConnectorType> getConnector() {
        if (connector == null) {
            connector = new ArrayList<ConnectorType>();
        }
        return this.connector;
    }

    /**
     * Gets the value of the engine property.
     *
     * @return
     *     possible object is
     *     {@link EngineType }
     *
     */
    public EngineType getEngine() {
        return engine;
    }

    /**
     * Sets the value of the engine property.
     *
     * @param value
     *     allowed object is
     *     {@link EngineType }
     *
     */
    public void setEngine(EngineType value) {
        this.engine = value;
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
     * Gets the value of the executor property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the executor property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExecutor().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExecutorType }
     *
     *
     */
    public List<ExecutorType> getExecutor() {
        if (executor == null) {
            executor = new ArrayList<ExecutorType>();
        }
        return this.executor;
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

    public Service getService(ClassLoader cl, Kernel kernel) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", getName());
        for (Map.Entry<QName, String> entry: otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Service service = (Service) recipe.create(cl);
        for (ExecutorType executorType: getExecutor()) {
            Executor executor = executorType.getExecutor(cl, kernel);
            service.addExecutor(executor);
            TomcatServerGBean.executors.put(executor.getName(), executor);
        }
        for (ConnectorType connectorType: getConnector()) {
            Connector connector = connectorType.getConnector(cl, service);
            service.addConnector(connector);
        }
        for (ListenerType listenerType : getListener()) {
            LifecycleListener listener = listenerType.getLifecycleListener(cl);
            service.addLifecycleListener(listener);
        }

        if (getEngine() != null) {
            Engine engine = getEngine().getEngine(cl);
            service.setContainer(engine);
        }

        return service;
    }
}
