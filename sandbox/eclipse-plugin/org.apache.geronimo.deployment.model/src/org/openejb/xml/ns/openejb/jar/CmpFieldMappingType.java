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
 * A representation of the model object '<em><b>Cmp Field Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldName <em>Cmp Field Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldClass <em>Cmp Field Class</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTableColumn <em>Table Column</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getSqlType <em>Sql Type</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTypeConverter <em>Type Converter</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmpFieldMappingType()
 * @model extendedMetaData="name='cmp-field-mapping_._type' kind='elementOnly'"
 * @generated
 */
public interface CmpFieldMappingType extends EObject {
    /**
     * Returns the value of the '<em><b>Cmp Field Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmp Field Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmp Field Name</em>' attribute.
     * @see #setCmpFieldName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmpFieldMappingType_CmpFieldName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='cmp-field-name' namespace='##targetNamespace'"
     * @generated
     */
    String getCmpFieldName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldName <em>Cmp Field Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Cmp Field Name</em>' attribute.
     * @see #getCmpFieldName()
     * @generated
     */
    void setCmpFieldName(String value);

    /**
     * Returns the value of the '<em><b>Cmp Field Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmp Field Class</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmp Field Class</em>' attribute.
     * @see #setCmpFieldClass(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmpFieldMappingType_CmpFieldClass()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='cmp-field-class' namespace='##targetNamespace'"
     * @generated
     */
    String getCmpFieldClass();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldClass <em>Cmp Field Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Cmp Field Class</em>' attribute.
     * @see #getCmpFieldClass()
     * @generated
     */
    void setCmpFieldClass(String value);

    /**
     * Returns the value of the '<em><b>Table Column</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Table Column</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Table Column</em>' attribute.
     * @see #setTableColumn(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmpFieldMappingType_TableColumn()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='table-column' namespace='##targetNamespace'"
     * @generated
     */
    String getTableColumn();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTableColumn <em>Table Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Table Column</em>' attribute.
     * @see #getTableColumn()
     * @generated
     */
    void setTableColumn(String value);

    /**
     * Returns the value of the '<em><b>Sql Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sql Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sql Type</em>' attribute.
     * @see #setSqlType(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmpFieldMappingType_SqlType()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='sql-type' namespace='##targetNamespace'"
     * @generated
     */
    String getSqlType();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getSqlType <em>Sql Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Sql Type</em>' attribute.
     * @see #getSqlType()
     * @generated
     */
    void setSqlType(String value);

    /**
     * Returns the value of the '<em><b>Type Converter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type Converter</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type Converter</em>' attribute.
     * @see #setTypeConverter(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getCmpFieldMappingType_TypeConverter()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='type-converter' namespace='##targetNamespace'"
     * @generated
     */
    String getTypeConverter();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTypeConverter <em>Type Converter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type Converter</em>' attribute.
     * @see #getTypeConverter()
     * @generated
     */
    void setTypeConverter(String value);

} // CmpFieldMappingType
