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
 * A representation of the model object '<em><b>Cmr Field Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getKeyColumn <em>Key Column</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getForeignKeyColumn <em>Foreign Key Column</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmrFieldMappingType()
 * @model extendedMetaData="name='cmr-field-mapping_._type' kind='elementOnly'"
 * @generated
 */
public interface CmrFieldMappingType extends EObject {
    /**
     * Returns the value of the '<em><b>Key Column</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Key Column</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Key Column</em>' attribute.
     * @see #setKeyColumn(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmrFieldMappingType_KeyColumn()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='key-column' namespace='##targetNamespace'"
     * @generated
     */
    String getKeyColumn();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getKeyColumn <em>Key Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Key Column</em>' attribute.
     * @see #getKeyColumn()
     * @generated
     */
    void setKeyColumn(String value);

    /**
     * Returns the value of the '<em><b>Foreign Key Column</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Foreign Key Column</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Foreign Key Column</em>' attribute.
     * @see #setForeignKeyColumn(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmrFieldMappingType_ForeignKeyColumn()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='foreign-key-column' namespace='##targetNamespace'"
     * @generated
     */
    String getForeignKeyColumn();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getForeignKeyColumn <em>Foreign Key Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Foreign Key Column</em>' attribute.
     * @see #getForeignKeyColumn()
     * @generated
     */
    void setForeignKeyColumn(String value);

} // CmrFieldMappingType
