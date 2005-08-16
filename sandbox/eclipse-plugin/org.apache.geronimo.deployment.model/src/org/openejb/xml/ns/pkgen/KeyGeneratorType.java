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
 * A representation of the model object '<em><b>Key Generator Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *               Primary Key generation element.
 * 
 *               If this is present, a key generator GBean will be created
 *               and configured to generate IDs for the surrounding object.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getSequenceTable <em>Sequence Table</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getAutoIncrementTable <em>Auto Increment Table</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getSqlGenerator <em>Sql Generator</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getCustomGenerator <em>Custom Generator</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.pkgen.PkgenPackage#getKeyGeneratorType()
 * @model extendedMetaData="name='key-generatorType' kind='elementOnly'"
 * @generated
 */
public interface KeyGeneratorType extends EObject {
    /**
     * Returns the value of the '<em><b>Sequence Table</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sequence Table</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sequence Table</em>' containment reference.
     * @see #setSequenceTable(SequenceTableType)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getKeyGeneratorType_SequenceTable()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='sequence-table' namespace='##targetNamespace'"
     * @generated
     */
    SequenceTableType getSequenceTable();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getSequenceTable <em>Sequence Table</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Sequence Table</em>' containment reference.
     * @see #getSequenceTable()
     * @generated
     */
    void setSequenceTable(SequenceTableType value);

    /**
     * Returns the value of the '<em><b>Auto Increment Table</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Auto Increment Table</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Auto Increment Table</em>' containment reference.
     * @see #setAutoIncrementTable(AutoIncrementTableType)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getKeyGeneratorType_AutoIncrementTable()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='auto-increment-table' namespace='##targetNamespace'"
     * @generated
     */
    AutoIncrementTableType getAutoIncrementTable();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getAutoIncrementTable <em>Auto Increment Table</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Auto Increment Table</em>' containment reference.
     * @see #getAutoIncrementTable()
     * @generated
     */
    void setAutoIncrementTable(AutoIncrementTableType value);

    /**
     * Returns the value of the '<em><b>Sql Generator</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Sql Generator</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Sql Generator</em>' containment reference.
     * @see #setSqlGenerator(SqlGeneratorType)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getKeyGeneratorType_SqlGenerator()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='sql-generator' namespace='##targetNamespace'"
     * @generated
     */
    SqlGeneratorType getSqlGenerator();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getSqlGenerator <em>Sql Generator</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Sql Generator</em>' containment reference.
     * @see #getSqlGenerator()
     * @generated
     */
    void setSqlGenerator(SqlGeneratorType value);

    /**
     * Returns the value of the '<em><b>Custom Generator</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Custom Generator</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Custom Generator</em>' containment reference.
     * @see #setCustomGenerator(CustomGeneratorType)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getKeyGeneratorType_CustomGenerator()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='custom-generator' namespace='##targetNamespace'"
     * @generated
     */
    CustomGeneratorType getCustomGenerator();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getCustomGenerator <em>Custom Generator</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Custom Generator</em>' containment reference.
     * @see #getCustomGenerator()
     * @generated
     */
    void setCustomGenerator(CustomGeneratorType value);

} // KeyGeneratorType
