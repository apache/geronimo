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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Session Bean Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbName <em>Ejb Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getJndiName <em>Jndi Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getLocalJndiName <em>Local Jndi Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssTargetName <em>Tss Target Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssLink <em>Tss Link</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTss <em>Tss</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceAddress <em>Web Service Address</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceSecurity <em>Web Service Security</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType()
 * @model extendedMetaData="name='session-beanType' kind='elementOnly'"
 * @generated
 */
public interface SessionBeanType extends EObject {
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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_EjbName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='ejb-name' namespace='##targetNamespace'"
     * @generated
     */
    String getEjbName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbName <em>Ejb Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Name</em>' attribute.
     * @see #getEjbName()
     * @generated
     */
    void setEjbName(String value);

    /**
     * Returns the value of the '<em><b>Jndi Name</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Jndi Name</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Jndi Name</em>' attribute list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_JndiName()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='jndi-name' namespace='##targetNamespace'"
     * @generated
     */
    EList getJndiName();

    /**
     * Returns the value of the '<em><b>Local Jndi Name</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Local Jndi Name</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Local Jndi Name</em>' attribute list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_LocalJndiName()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='local-jndi-name' namespace='##targetNamespace'"
     * @generated
     */
    EList getLocalJndiName();

    /**
     * Returns the value of the '<em><b>Tss Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Tss Target Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Tss Target Name</em>' attribute.
     * @see #setTssTargetName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_TssTargetName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='tss-target-name' namespace='##targetNamespace'"
     * @generated
     */
    String getTssTargetName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssTargetName <em>Tss Target Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Tss Target Name</em>' attribute.
     * @see #getTssTargetName()
     * @generated
     */
    void setTssTargetName(String value);

    /**
     * Returns the value of the '<em><b>Tss Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Tss Link</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Tss Link</em>' attribute.
     * @see #setTssLink(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_TssLink()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='tss-link' namespace='##targetNamespace'"
     * @generated
     */
    String getTssLink();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssLink <em>Tss Link</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Tss Link</em>' attribute.
     * @see #getTssLink()
     * @generated
     */
    void setTssLink(String value);

    /**
     * Returns the value of the '<em><b>Tss</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Tss</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Tss</em>' containment reference.
     * @see #setTss(TssType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_Tss()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='tss' namespace='##targetNamespace'"
     * @generated
     */
    TssType getTss();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTss <em>Tss</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Tss</em>' containment reference.
     * @see #getTss()
     * @generated
     */
    void setTss(TssType value);

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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_EjbRef()
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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_EjbLocalRef()
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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_ServiceRef()
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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_ResourceRef()
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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_ResourceEnvRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ResourceEnvRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='resource-env-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getResourceEnvRef();

    /**
     * Returns the value of the '<em><b>Web Service Address</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Web Service Address</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Web Service Address</em>' attribute.
     * @see #setWebServiceAddress(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_WebServiceAddress()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='web-service-address' namespace='##targetNamespace'"
     * @generated
     */
    String getWebServiceAddress();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceAddress <em>Web Service Address</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Web Service Address</em>' attribute.
     * @see #getWebServiceAddress()
     * @generated
     */
    void setWebServiceAddress(String value);

    /**
     * Returns the value of the '<em><b>Web Service Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Web Service Security</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Web Service Security</em>' containment reference.
     * @see #setWebServiceSecurity(WebServiceSecurityType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_WebServiceSecurity()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='web-service-security' namespace='##targetNamespace'"
     * @generated
     */
    WebServiceSecurityType getWebServiceSecurity();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceSecurity <em>Web Service Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Web Service Security</em>' containment reference.
     * @see #getWebServiceSecurity()
     * @generated
     */
    void setWebServiceSecurity(WebServiceSecurityType value);

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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getSessionBeanType_Id()
     * @model unique="false" id="true" dataType="org.eclipse.emf.ecore.xml.type.ID"
     *        extendedMetaData="kind='attribute' name='id'"
     * @generated
     */
    String getId();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getId <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Id</em>' attribute.
     * @see #getId()
     * @generated
     */
    void setId(String value);

} // SessionBeanType
