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
 * A representation of the model object '<em><b>Ejb Relation Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationName <em>Ejb Relation Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getManyToManyTableName <em>Many To Many Table Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationshipRole <em>Ejb Relationship Role</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationType()
 * @model extendedMetaData="name='ejb-relationType' kind='elementOnly'"
 * @generated
 */
public interface EjbRelationType extends EObject {
    /**
     * Returns the value of the '<em><b>Ejb Relation Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Relation Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Relation Name</em>' attribute.
     * @see #setEjbRelationName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationType_EjbRelationName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='ejb-relation-name' namespace='##targetNamespace'"
     * @generated
     */
    String getEjbRelationName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationName <em>Ejb Relation Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Relation Name</em>' attribute.
     * @see #getEjbRelationName()
     * @generated
     */
    void setEjbRelationName(String value);

    /**
     * Returns the value of the '<em><b>Many To Many Table Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Many To Many Table Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Many To Many Table Name</em>' attribute.
     * @see #setManyToManyTableName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationType_ManyToManyTableName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='many-to-many-table-name' namespace='##targetNamespace'"
     * @generated
     */
    String getManyToManyTableName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getManyToManyTableName <em>Many To Many Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Many To Many Table Name</em>' attribute.
     * @see #getManyToManyTableName()
     * @generated
     */
    void setManyToManyTableName(String value);

    /**
     * Returns the value of the '<em><b>Ejb Relationship Role</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Relationship Role</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Relationship Role</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationType_EjbRelationshipRole()
     * @model type="org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType" containment="true" resolveProxies="false" required="true" upper="2"
     *        extendedMetaData="kind='element' name='ejb-relationship-role' namespace='##targetNamespace'"
     * @generated
     */
    EList getEjbRelationshipRole();

} // EjbRelationType
