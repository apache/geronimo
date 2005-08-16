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
 * A representation of the model object '<em><b>Sequence Table Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *                 Indicates that a separate table holds a list of table name/ID
 *                 pairs and the server should fetch the next ID from that table.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.SequenceTableType#getTableName <em>Table Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.SequenceTableType#getSequenceName <em>Sequence Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.SequenceTableType#getBatchSize <em>Batch Size</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSequenceTableType()
 * @model extendedMetaData="name='sequence-tableType' kind='elementOnly'"
 * @generated
 */
public interface SequenceTableType extends EObject {
    /**
     * Returns the value of the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Table Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Table Name</em>' attribute.
     * @see #setTableName(String)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSequenceTableType_TableName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='table-name' namespace='##targetNamespace'"
     * @generated
     */
    String getTableName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getTableName <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Table Name</em>' attribute.
     * @see #getTableName()
     * @generated
     */
    void setTableName(String value);

    /**
     * Returns the value of the '<em><b>Sequence Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sequence Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sequence Name</em>' attribute.
     * @see #setSequenceName(String)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSequenceTableType_SequenceName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='sequence-name' namespace='##targetNamespace'"
     * @generated
     */
    String getSequenceName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getSequenceName <em>Sequence Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Sequence Name</em>' attribute.
     * @see #getSequenceName()
     * @generated
     */
    void setSequenceName(String value);

    /**
     * Returns the value of the '<em><b>Batch Size</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Batch Size</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Batch Size</em>' attribute.
     * @see #isSetBatchSize()
     * @see #unsetBatchSize()
     * @see #setBatchSize(int)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getSequenceTableType_BatchSize()
     * @model unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int" required="true"
     *        extendedMetaData="kind='element' name='batch-size' namespace='##targetNamespace'"
     * @generated
     */
    int getBatchSize();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getBatchSize <em>Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Batch Size</em>' attribute.
     * @see #isSetBatchSize()
     * @see #unsetBatchSize()
     * @see #getBatchSize()
     * @generated
     */
    void setBatchSize(int value);

    /**
     * Unsets the value of the '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getBatchSize <em>Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetBatchSize()
     * @see #getBatchSize()
     * @see #setBatchSize(int)
     * @generated
     */
    void unsetBatchSize();

    /**
     * Returns whether the value of the '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getBatchSize <em>Batch Size</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Batch Size</em>' attribute is set.
     * @see #unsetBatchSize()
     * @see #getBatchSize()
     * @see #setBatchSize(int)
     * @generated
     */
    boolean isSetBatchSize();

} // SequenceTableType
