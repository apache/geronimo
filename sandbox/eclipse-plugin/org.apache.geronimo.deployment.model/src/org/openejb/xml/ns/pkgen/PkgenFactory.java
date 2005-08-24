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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.pkgen.PkgenPackage
 * @generated
 */
public interface PkgenFactory extends EFactory {
    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    PkgenFactory eINSTANCE = new org.openejb.xml.ns.pkgen.impl.PkgenFactoryImpl();

    /**
     * Returns a new object of class '<em>Auto Increment Table Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Auto Increment Table Type</em>'.
     * @generated
     */
    AutoIncrementTableType createAutoIncrementTableType();

    /**
     * Returns a new object of class '<em>Custom Generator Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Custom Generator Type</em>'.
     * @generated
     */
    CustomGeneratorType createCustomGeneratorType();

    /**
     * Returns a new object of class '<em>Database Generated Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Database Generated Type</em>'.
     * @generated
     */
    DatabaseGeneratedType createDatabaseGeneratedType();

    /**
     * Returns a new object of class '<em>Document Root</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Document Root</em>'.
     * @generated
     */
    DocumentRoot createDocumentRoot();

    /**
     * Returns a new object of class '<em>Key Generator Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Key Generator Type</em>'.
     * @generated
     */
    KeyGeneratorType createKeyGeneratorType();

    /**
     * Returns a new object of class '<em>Sequence Table Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Sequence Table Type</em>'.
     * @generated
     */
    SequenceTableType createSequenceTableType();

    /**
     * Returns a new object of class '<em>Sql Generator Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Sql Generator Type</em>'.
     * @generated
     */
    SqlGeneratorType createSqlGeneratorType();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    PkgenPackage getPkgenPackage();

} //PkgenFactory
