/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.openejb.xml.ns.openejb.jar;

import org.apache.geronimo.xml.ns.naming.ResourceLocatorType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Message Driven Bean Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbName <em>Ejb Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceAdapter <em>Resource Adapter</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getActivationConfig <em>Activation Config</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType()
 * @model extendedMetaData="name='message-driven-beanType' kind='elementOnly'"
 * @generated
 */
public interface MessageDrivenBeanType extends EObject {
    /**
     * Returns the value of the '<em><b>Ejb Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Name</em>' attribute.
     * @see #setEjbName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_EjbName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='ejb-name' namespace='##targetNamespace'"
     * @generated
     */
    String getEjbName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbName <em>Ejb Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Name</em>' attribute.
     * @see #getEjbName()
     * @generated
     */
    void setEjbName(String value);

    /**
     * Returns the value of the '<em><b>Resource Adapter</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Adapter</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Adapter</em>' containment reference.
     * @see #setResourceAdapter(ResourceLocatorType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_ResourceAdapter()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='resource-adapter' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    ResourceLocatorType getResourceAdapter();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceAdapter <em>Resource Adapter</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Resource Adapter</em>' containment reference.
     * @see #getResourceAdapter()
     * @generated
     */
    void setResourceAdapter(ResourceLocatorType value);

    /**
     * Returns the value of the '<em><b>Activation Config</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Activation Config</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Activation Config</em>' containment reference.
     * @see #setActivationConfig(ActivationConfigType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_ActivationConfig()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='activation-config' namespace='##targetNamespace'"
     * @generated
     */
    ActivationConfigType getActivationConfig();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getActivationConfig <em>Activation Config</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Activation Config</em>' containment reference.
     * @see #getActivationConfig()
     * @generated
     */
    void setActivationConfig(ActivationConfigType value);

    /**
     * Returns the value of the '<em><b>Ejb Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.EjbRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Ref</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_EjbRef()
     * @model type="org.apache.geronimo.xml.ns.naming.EjbRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getEjbRef();

    /**
     * Returns the value of the '<em><b>Ejb Local Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Local Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Local Ref</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_EjbLocalRef()
     * @model type="org.apache.geronimo.xml.ns.naming.EjbLocalRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb-local-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getEjbLocalRef();

    /**
     * Returns the value of the '<em><b>Service Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.ServiceRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Service Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Service Ref</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_ServiceRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ServiceRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='service-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getServiceRef();

    /**
     * Returns the value of the '<em><b>Resource Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.ResourceRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Ref</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_ResourceRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ResourceRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='resource-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getResourceRef();

    /**
     * Returns the value of the '<em><b>Resource Env Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Env Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Env Ref</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_ResourceEnvRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ResourceEnvRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='resource-env-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getResourceEnvRef();

    /**
     * Returns the value of the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Id</em>' attribute.
     * @see #setId(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMessageDrivenBeanType_Id()
     * @model unique="false" id="true" dataType="org.eclipse.emf.ecore.xml.type.ID"
     *        extendedMetaData="kind='attribute' name='id'"
     * @generated
     */
    String getId();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getId <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Id</em>' attribute.
     * @see #getId()
     * @generated
     */
    void setId(String value);

} // MessageDrivenBeanType
