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
 * A representation of the model object '<em><b>Ejb Relationship Role Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getEjbRelationshipRoleName <em>Ejb Relationship Role Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRelationshipRoleSource <em>Relationship Role Source</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getCmrField <em>Cmr Field</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getForeignKeyColumnOnSource <em>Foreign Key Column On Source</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRoleMapping <em>Role Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationshipRoleType()
 * @model extendedMetaData="name='ejb-relationship-roleType' kind='elementOnly'"
 * @generated
 */
public interface EjbRelationshipRoleType extends EObject {
    /**
     * Returns the value of the '<em><b>Ejb Relationship Role Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Relationship Role Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Relationship Role Name</em>' attribute.
     * @see #setEjbRelationshipRoleName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationshipRoleType_EjbRelationshipRoleName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='ejb-relationship-role-name' namespace='##targetNamespace'"
     * @generated
     */
    String getEjbRelationshipRoleName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getEjbRelationshipRoleName <em>Ejb Relationship Role Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Relationship Role Name</em>' attribute.
     * @see #getEjbRelationshipRoleName()
     * @generated
     */
    void setEjbRelationshipRoleName(String value);

    /**
     * Returns the value of the '<em><b>Relationship Role Source</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Relationship Role Source</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Relationship Role Source</em>' containment reference.
     * @see #setRelationshipRoleSource(RelationshipRoleSourceType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationshipRoleType_RelationshipRoleSource()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='relationship-role-source' namespace='##targetNamespace'"
     * @generated
     */
    RelationshipRoleSourceType getRelationshipRoleSource();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRelationshipRoleSource <em>Relationship Role Source</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Relationship Role Source</em>' containment reference.
     * @see #getRelationshipRoleSource()
     * @generated
     */
    void setRelationshipRoleSource(RelationshipRoleSourceType value);

    /**
     * Returns the value of the '<em><b>Cmr Field</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmr Field</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmr Field</em>' containment reference.
     * @see #setCmrField(CmrFieldType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationshipRoleType_CmrField()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='cmr-field' namespace='##targetNamespace'"
     * @generated
     */
    CmrFieldType getCmrField();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getCmrField <em>Cmr Field</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Cmr Field</em>' containment reference.
     * @see #getCmrField()
     * @generated
     */
    void setCmrField(CmrFieldType value);

    /**
     * Returns the value of the '<em><b>Foreign Key Column On Source</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Foreign Key Column On Source</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Foreign Key Column On Source</em>' containment reference.
     * @see #setForeignKeyColumnOnSource(EObject)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationshipRoleType_ForeignKeyColumnOnSource()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='foreign-key-column-on-source' namespace='##targetNamespace'"
     * @generated
     */
    EObject getForeignKeyColumnOnSource();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getForeignKeyColumnOnSource <em>Foreign Key Column On Source</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Foreign Key Column On Source</em>' containment reference.
     * @see #getForeignKeyColumnOnSource()
     * @generated
     */
    void setForeignKeyColumnOnSource(EObject value);

    /**
     * Returns the value of the '<em><b>Role Mapping</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Role Mapping</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Role Mapping</em>' containment reference.
     * @see #setRoleMapping(RoleMappingType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEjbRelationshipRoleType_RoleMapping()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='role-mapping' namespace='##targetNamespace'"
     * @generated
     */
    RoleMappingType getRoleMapping();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRoleMapping <em>Role Mapping</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Role Mapping</em>' containment reference.
     * @see #getRoleMapping()
     * @generated
     */
    void setRoleMapping(RoleMappingType value);

} // EjbRelationshipRoleType
