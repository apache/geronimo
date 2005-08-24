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
package org.apache.geronimo.xml.ns.security;

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
 *   <li>{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getDefaultPrincipal <em>Default Principal</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getSecurity <em>Security</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDocumentRoot()
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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDocumentRoot_Mixed()
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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDocumentRoot_XMLNSPrefixMap()
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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDocumentRoot_XSISchemaLocation()
     * @model mapType="org.eclipse.emf.ecore.EStringToStringMapEntry" keyType="java.lang.String" valueType="java.lang.String" transient="true"
     *        extendedMetaData="kind='attribute' name='xsi:schemaLocation'"
     * @generated
     */
    EMap getXSISchemaLocation();

    /**
     * Returns the value of the '<em><b>Default Principal</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Default Principal</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Default Principal</em>' containment reference.
     * @see #setDefaultPrincipal(DefaultPrincipalType)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDocumentRoot_DefaultPrincipal()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='default-principal' namespace='##targetNamespace'"
     * @generated
     */
    DefaultPrincipalType getDefaultPrincipal();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getDefaultPrincipal <em>Default Principal</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Default Principal</em>' containment reference.
     * @see #getDefaultPrincipal()
     * @generated
     */
    void setDefaultPrincipal(DefaultPrincipalType value);

    /**
     * Returns the value of the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Security</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Security</em>' containment reference.
     * @see #setSecurity(SecurityType)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDocumentRoot_Security()
     * @model containment="true" resolveProxies="false" upper="-2" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='security' namespace='##targetNamespace'"
     * @generated
     */
    SecurityType getSecurity();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getSecurity <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Security</em>' containment reference.
     * @see #getSecurity()
     * @generated
     */
    void setSecurity(SecurityType value);

} // DocumentRoot
