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
 * A representation of the model object '<em><b>Role Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RoleType#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RoleType#getRealm <em>Realm</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RoleType#getDistinguishedName <em>Distinguished Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RoleType#getRoleName <em>Role Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleType()
 * @model extendedMetaData="name='roleType' kind='elementOnly'"
 * @generated
 */
public interface RoleType extends EObject{
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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleType_Description()
     * @model type="org.apache.geronimo.xml.ns.security.DescriptionType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
     * @generated
     */
    EList getDescription();

    /**
     * Returns the value of the '<em><b>Realm</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.RealmType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Realm</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Realm</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleType_Realm()
     * @model type="org.apache.geronimo.xml.ns.security.RealmType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='realm' namespace='##targetNamespace'"
     * @generated
     */
    EList getRealm();

    /**
     * Returns the value of the '<em><b>Distinguished Name</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.DistinguishedNameType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Distinguished Name</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Distinguished Name</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleType_DistinguishedName()
     * @model type="org.apache.geronimo.xml.ns.security.DistinguishedNameType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='distinguished-name' namespace='##targetNamespace'"
     * @generated
     */
    EList getDistinguishedName();

    /**
     * Returns the value of the '<em><b>Role Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Role Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Role Name</em>' attribute.
     * @see #setRoleName(String)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleType_RoleName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='role-name'"
     * @generated
     */
    String getRoleName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.RoleType#getRoleName <em>Role Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Role Name</em>' attribute.
     * @see #getRoleName()
     * @generated
     */
    void setRoleName(String value);

} // RoleType
