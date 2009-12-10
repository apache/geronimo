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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ContextResource;

/**
 * <p>Java class for NamingResourcesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NamingResourcesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Ejb" type="{}EjbType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Environment" type="{}EnvironmentType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="LocalEjb" type="{}LocalEjbType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Resource" type="{}ResourceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ResourceEnvRef" type="{}ResourceEnvRefType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ServiceRef" type="{}ServiceRefType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Transaction" type="{}TransactionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamingResourcesType", propOrder = { "ejb", "environment", "localEjb", "resource", "resourceEnvRef", "serviceRef", "transaction" })
@XmlSeeAlso( { ContextType.class })
public class NamingResourcesType {

    @XmlElement(name = "Ejb")
    protected List<EjbType> ejb;

    @XmlElement(name = "Environment")
    protected List<EnvironmentType> environment;

    @XmlElement(name = "LocalEjb")
    protected List<LocalEjbType> localEjb;

    @XmlElement(name = "Resource")
    protected List<ResourceType> resource;

    @XmlElement(name = "ResourceEnvRef")
    protected List<ResourceEnvRefType> resourceEnvRef;

    @XmlElement(name = "ServiceRef")
    protected List<ServiceRefType> serviceRef;

    @XmlElement(name = "Transaction")
    protected List<TransactionType> transaction;

    /**
     * Gets the value of the ejb property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejb property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjb().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbType }
     * 
     * 
     */
    public List<EjbType> getEjb() {
        if (ejb == null) {
            ejb = new ArrayList<EjbType>();
        }
        return this.ejb;
    }

    /**
     * Gets the value of the environment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the environment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEnvironment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnvironmentType }
     * 
     * 
     */
    public List<EnvironmentType> getEnvironment() {
        if (environment == null) {
            environment = new ArrayList<EnvironmentType>();
        }
        return this.environment;
    }

    /**
     * Gets the value of the localEjb property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localEjb property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalEjb().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocalEjbType }
     * 
     * 
     */
    public List<LocalEjbType> getLocalEjb() {
        if (localEjb == null) {
            localEjb = new ArrayList<LocalEjbType>();
        }
        return this.localEjb;
    }

    /**
     * Gets the value of the resource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceType }
     * 
     * 
     */
    public List<ResourceType> getResource() {
        if (resource == null) {
            resource = new ArrayList<ResourceType>();
        }
        return this.resource;
    }

    /**
     * Gets the value of the resourceEnvRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvRefType }
     * 
     * 
     */
    public List<ResourceEnvRefType> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRefType>();
        }
        return this.resourceEnvRef;
    }

    /**
     * Gets the value of the serviceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefType }
     * 
     * 
     */
    public List<ServiceRefType> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRefType>();
        }
        return this.serviceRef;
    }

    /**
     * Gets the value of the transaction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transaction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransaction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransactionType }
     * 
     * 
     */
    public List<TransactionType> getTransaction() {
        if (transaction == null) {
            transaction = new ArrayList<TransactionType>();
        }
        return this.transaction;
    }

    public void merge(NamingResources namingResources, ClassLoader cl) {
        for (ResourceType resourceType : getResource()) {
            ContextResource contextResource = resourceType.getContextResource(cl);
            namingResources.addResource(contextResource);
        }
        //do nothing for now... use geronimo naming configuration
    }
}
