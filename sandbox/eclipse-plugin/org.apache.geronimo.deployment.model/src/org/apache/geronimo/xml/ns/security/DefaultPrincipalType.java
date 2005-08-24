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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Default Principal Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getPrincipal <em>Principal</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getNamedUsernamePasswordCredential <em>Named Username Password Credential</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getRealmName <em>Realm Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDefaultPrincipalType()
 * @model extendedMetaData="name='default-principalType' kind='elementOnly'"
 * @generated
 */
public interface DefaultPrincipalType extends EObject{
    /**
     * Returns the value of the '<em><b>Description</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.DescriptionType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Description</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDefaultPrincipalType_Description()
     * @model type="org.apache.geronimo.xml.ns.security.DescriptionType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
     * @generated
     */
    EList getDescription();

    /**
     * Returns the value of the '<em><b>Principal</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Principal</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Principal</em>' containment reference.
     * @see #setPrincipal(PrincipalType)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDefaultPrincipalType_Principal()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='principal' namespace='##targetNamespace'"
     * @generated
     */
    PrincipalType getPrincipal();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getPrincipal <em>Principal</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Principal</em>' containment reference.
     * @see #getPrincipal()
     * @generated
     */
    void setPrincipal(PrincipalType value);

    /**
     * Returns the value of the '<em><b>Named Username Password Credential</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Named Username Password Credential</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Named Username Password Credential</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDefaultPrincipalType_NamedUsernamePasswordCredential()
     * @model type="org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='named-username-password-credential' namespace='##targetNamespace'"
     * @generated
     */
    EList getNamedUsernamePasswordCredential();

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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDefaultPrincipalType_RealmName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='realm-name'"
     * @generated
     */
    String getRealmName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getRealmName <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Realm Name</em>' attribute.
     * @see #getRealmName()
     * @generated
     */
    void setRealmName(String value);

} // DefaultPrincipalType
