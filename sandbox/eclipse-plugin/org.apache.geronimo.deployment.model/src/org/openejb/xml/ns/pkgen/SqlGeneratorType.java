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
package org.openejb.xml.ns.pkgen;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sql Generator Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *                 Indicates that an arbitrary SQL statement should be used to
 *                 generate the next ID.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.SqlGeneratorType#getSql <em>Sql</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.SqlGeneratorType#getReturnType <em>Return Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSqlGeneratorType()
 * @model extendedMetaData="name='sql-generatorType' kind='elementOnly'"
 * @generated
 */
public interface SqlGeneratorType extends EObject {
    /**
     * Returns the value of the '<em><b>Sql</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sql</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sql</em>' attribute.
     * @see #setSql(String)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSqlGeneratorType_Sql()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='sql' namespace='##targetNamespace'"
     * @generated
     */
    String getSql();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.SqlGeneratorType#getSql <em>Sql</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Sql</em>' attribute.
     * @see #getSql()
     * @generated
     */
    void setSql(String value);

    /**
     * Returns the value of the '<em><b>Return Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Return Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Return Type</em>' attribute.
     * @see #setReturnType(String)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSqlGeneratorType_ReturnType()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='return-type' namespace='##targetNamespace'"
     * @generated
     */
    String getReturnType();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.SqlGeneratorType#getReturnType <em>Return Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Return Type</em>' attribute.
     * @see #getReturnType()
     * @generated
     */
    void setReturnType(String value);

} // SqlGeneratorType
