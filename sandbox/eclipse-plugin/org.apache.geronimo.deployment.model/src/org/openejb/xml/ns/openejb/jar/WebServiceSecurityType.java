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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Web Service Security Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getSecurityRealmName <em>Security Realm Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getRealmName <em>Realm Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getTransportGuarantee <em>Transport Guarantee</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getAuthMethod <em>Auth Method</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getWebServiceSecurityType()
 * @model extendedMetaData="name='web-service-securityType' kind='elementOnly'"
 * @generated
 */
public interface WebServiceSecurityType extends EObject {
    /**
     * Returns the value of the '<em><b>Security Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Security Realm Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Security Realm Name</em>' attribute.
     * @see #setSecurityRealmName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getWebServiceSecurityType_SecurityRealmName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='security-realm-name' namespace='##targetNamespace'"
     * @generated
     */
    String getSecurityRealmName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getSecurityRealmName <em>Security Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Security Realm Name</em>' attribute.
     * @see #getSecurityRealmName()
     * @generated
     */
    void setSecurityRealmName(String value);

    /**
     * Returns the value of the '<em><b>Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Realm Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Realm Name</em>' attribute.
     * @see #setRealmName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getWebServiceSecurityType_RealmName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='realm-name' namespace='##targetNamespace'"
     * @generated
     */
    String getRealmName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getRealmName <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Realm Name</em>' attribute.
     * @see #getRealmName()
     * @generated
     */
    void setRealmName(String value);

    /**
     * Returns the value of the '<em><b>Transport Guarantee</b></em>' attribute.
     * The default value is <code>"NONE"</code>.
     * The literals are from the enumeration {@link org.openejb.xml.ns.openejb.jar.TransportGuaranteeType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Transport Guarantee</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Transport Guarantee</em>' attribute.
     * @see org.openejb.xml.ns.openejb.jar.TransportGuaranteeType
     * @see #isSetTransportGuarantee()
     * @see #unsetTransportGuarantee()
     * @see #setTransportGuarantee(TransportGuaranteeType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getWebServiceSecurityType_TransportGuarantee()
     * @model default="NONE" unique="false" unsettable="true" required="true"
     *        extendedMetaData="kind='element' name='transport-guarantee' namespace='##targetNamespace'"
     * @generated
     */
    TransportGuaranteeType getTransportGuarantee();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getTransportGuarantee <em>Transport Guarantee</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Transport Guarantee</em>' attribute.
     * @see org.openejb.xml.ns.openejb.jar.TransportGuaranteeType
     * @see #isSetTransportGuarantee()
     * @see #unsetTransportGuarantee()
     * @see #getTransportGuarantee()
     * @generated
     */
    void setTransportGuarantee(TransportGuaranteeType value);

    /**
     * Unsets the value of the '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getTransportGuarantee <em>Transport Guarantee</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetTransportGuarantee()
     * @see #getTransportGuarantee()
     * @see #setTransportGuarantee(TransportGuaranteeType)
     * @generated
     */
    void unsetTransportGuarantee();

    /**
     * Returns whether the value of the '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getTransportGuarantee <em>Transport Guarantee</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Transport Guarantee</em>' attribute is set.
     * @see #unsetTransportGuarantee()
     * @see #getTransportGuarantee()
     * @see #setTransportGuarantee(TransportGuaranteeType)
     * @generated
     */
    boolean isSetTransportGuarantee();

    /**
     * Returns the value of the '<em><b>Auth Method</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Auth Method</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Auth Method</em>' attribute.
     * @see #setAuthMethod(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getWebServiceSecurityType_AuthMethod()
     * @model unique="false" dataType="org.openejb.xml.ns.openejb.jar.AuthMethodType" required="true"
     *        extendedMetaData="kind='element' name='auth-method' namespace='##targetNamespace'"
     * @generated
     */
    String getAuthMethod();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getAuthMethod <em>Auth Method</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Auth Method</em>' attribute.
     * @see #getAuthMethod()
     * @generated
     */
    void setAuthMethod(String value);

} // WebServiceSecurityType
