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
 * A representation of the model object '<em><b>Realm Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RealmType#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RealmType#getPrincipal <em>Principal</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RealmType#getRealmName <em>Realm Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRealmType()
 * @model extendedMetaData="name='realmType' kind='elementOnly'"
 * @generated
 */
public interface RealmType extends EObject{
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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRealmType_Description()
     * @model type="org.apache.geronimo.xml.ns.security.DescriptionType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
     * @generated
     */
    EList getDescription();

    /**
     * Returns the value of the '<em><b>Principal</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.PrincipalType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Principal</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Principal</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRealmType_Principal()
     * @model type="org.apache.geronimo.xml.ns.security.PrincipalType" containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='principal' namespace='##targetNamespace'"
     * @generated
     */
    EList getPrincipal();

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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRealmType_RealmName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='realm-name'"
     * @generated
     */
    String getRealmName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.RealmType#getRealmName <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Realm Name</em>' attribute.
     * @see #getRealmName()
     * @generated
     */
    void setRealmName(String value);

} // RealmType
