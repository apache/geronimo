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
package org.apache.geronimo.xml.ns.naming;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Document Root</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getCmpConnectionFactory <em>Cmp Connection Factory</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceAdapter <em>Resource Adapter</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getWorkmanager <em>Workmanager</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot()
 * @model extendedMetaData="name='' kind='mixed'"
 * @generated
 */
public interface DocumentRoot extends EObject{
    /**
     * Returns the value of the '<em><b>Mixed</b></em>' attribute list.
     * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Mixed</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Mixed</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_Mixed()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
     *        extendedMetaData="kind='elementWildcard' name=':mixed'"
     * @generated
     */
    FeatureMap getMixed();

    /**
     * Returns the value of the '<em><b>XMLNS Prefix Map</b></em>' map.
     * The key is of type {@link java.lang.String},
     * and the value is of type {@link java.lang.String},
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>XMLNS Prefix Map</em>' map isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>XMLNS Prefix Map</em>' map.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_XMLNSPrefixMap()
     * @model mapType="org.eclipse.emf.ecore.EStringToStringMapEntry" keyType="java.lang.String" valueType="java.lang.String" transient="true"
     *        extendedMetaData="kind='attribute' name='xmlns:prefix'"
     * @generated
     */
    EMap getXMLNSPrefixMap();

    /**
     * Returns the value of the '<em><b>XSI Schema Location</b></em>' map.
     * The key is of type {@link java.lang.String},
     * and the value is of type {@link java.lang.String},
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>XSI Schema Location</em>' map isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>XSI Schema Location</em>' map.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_XSISchemaLocation()
     * @model mapType="org.eclipse.emf.ecore.EStringToStringMapEntry" keyType="java.lang.String" valueType="java.lang.String" transient="true"
     *        extendedMetaData="kind='attribute' name='xsi:schemaLocation'"
     * @generated
     */
    EMap getXSISchemaLocation();

    /**
     * Returns the value of the '<em><b>Cmp Connection Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmp Connection Factory</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmp Connection Factory</em>' containment reference.
     * @see #setCmpConnectionFactory(ResourceLocatorType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_CmpConnectionFactory()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='cmp-connection-factory' namespace='##targetNamespace'"
     * @generated
     */
    ResourceLocatorType getCmpConnectionFactory();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getCmpConnectionFactory <em>Cmp Connection Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Cmp Connection Factory</em>' containment reference.
     * @see #getCmpConnectionFactory()
     * @generated
     */
    void setCmpConnectionFactory(ResourceLocatorType value);

    /**
     * Returns the value of the '<em><b>Ejb Local Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Local Ref</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Local Ref</em>' containment reference.
     * @see #setEjbLocalRef(EjbLocalRefType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_EjbLocalRef()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='ejb-local-ref' namespace='##targetNamespace'"
     * @generated
     */
    EjbLocalRefType getEjbLocalRef();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbLocalRef <em>Ejb Local Ref</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Local Ref</em>' containment reference.
     * @see #getEjbLocalRef()
     * @generated
     */
    void setEjbLocalRef(EjbLocalRefType value);

    /**
     * Returns the value of the '<em><b>Ejb Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Ref</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Ref</em>' containment reference.
     * @see #setEjbRef(EjbRefType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_EjbRef()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='ejb-ref' namespace='##targetNamespace'"
     * @generated
     */
    EjbRefType getEjbRef();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbRef <em>Ejb Ref</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Ref</em>' containment reference.
     * @see #getEjbRef()
     * @generated
     */
    void setEjbRef(EjbRefType value);

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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_ResourceAdapter()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='resource-adapter' namespace='##targetNamespace'"
     * @generated
     */
    ResourceLocatorType getResourceAdapter();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceAdapter <em>Resource Adapter</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Resource Adapter</em>' containment reference.
     * @see #getResourceAdapter()
     * @generated
     */
    void setResourceAdapter(ResourceLocatorType value);

    /**
     * Returns the value of the '<em><b>Resource Env Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Env Ref</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Env Ref</em>' containment reference.
     * @see #setResourceEnvRef(ResourceEnvRefType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_ResourceEnvRef()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='resource-env-ref' namespace='##targetNamespace'"
     * @generated
     */
    ResourceEnvRefType getResourceEnvRef();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceEnvRef <em>Resource Env Ref</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Resource Env Ref</em>' containment reference.
     * @see #getResourceEnvRef()
     * @generated
     */
    void setResourceEnvRef(ResourceEnvRefType value);

    /**
     * Returns the value of the '<em><b>Resource Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Ref</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Ref</em>' containment reference.
     * @see #setResourceRef(ResourceRefType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_ResourceRef()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='resource-ref' namespace='##targetNamespace'"
     * @generated
     */
    ResourceRefType getResourceRef();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceRef <em>Resource Ref</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Resource Ref</em>' containment reference.
     * @see #getResourceRef()
     * @generated
     */
    void setResourceRef(ResourceRefType value);

    /**
     * Returns the value of the '<em><b>Service Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Service Ref</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Service Ref</em>' containment reference.
     * @see #setServiceRef(ServiceRefType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_ServiceRef()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='service-ref' namespace='##targetNamespace'"
     * @generated
     */
    ServiceRefType getServiceRef();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getServiceRef <em>Service Ref</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Service Ref</em>' containment reference.
     * @see #getServiceRef()
     * @generated
     */
    void setServiceRef(ServiceRefType value);

    /**
     * Returns the value of the '<em><b>Workmanager</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Workmanager</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Workmanager</em>' containment reference.
     * @see #setWorkmanager(GbeanLocatorType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getDocumentRoot_Workmanager()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='workmanager' namespace='##targetNamespace'"
     * @generated
     */
    GbeanLocatorType getWorkmanager();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getWorkmanager <em>Workmanager</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Workmanager</em>' containment reference.
     * @see #getWorkmanager()
     * @generated
     */
    void setWorkmanager(GbeanLocatorType value);

} // DocumentRoot
