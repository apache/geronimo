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
package org.openejb.xml.ns.pkgen.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.openejb.xml.ns.pkgen.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PkgenFactoryImpl extends EFactoryImpl implements PkgenFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PkgenFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case PkgenPackage.AUTO_INCREMENT_TABLE_TYPE: return createAutoIncrementTableType();
            case PkgenPackage.CUSTOM_GENERATOR_TYPE: return createCustomGeneratorType();
            case PkgenPackage.DATABASE_GENERATED_TYPE: return createDatabaseGeneratedType();
            case PkgenPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case PkgenPackage.KEY_GENERATOR_TYPE: return createKeyGeneratorType();
            case PkgenPackage.SEQUENCE_TABLE_TYPE: return createSequenceTableType();
            case PkgenPackage.SQL_GENERATOR_TYPE: return createSqlGeneratorType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public AutoIncrementTableType createAutoIncrementTableType() {
        AutoIncrementTableTypeImpl autoIncrementTableType = new AutoIncrementTableTypeImpl();
        return autoIncrementTableType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CustomGeneratorType createCustomGeneratorType() {
        CustomGeneratorTypeImpl customGeneratorType = new CustomGeneratorTypeImpl();
        return customGeneratorType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DatabaseGeneratedType createDatabaseGeneratedType() {
        DatabaseGeneratedTypeImpl databaseGeneratedType = new DatabaseGeneratedTypeImpl();
        return databaseGeneratedType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DocumentRoot createDocumentRoot() {
        DocumentRootImpl documentRoot = new DocumentRootImpl();
        return documentRoot;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KeyGeneratorType createKeyGeneratorType() {
        KeyGeneratorTypeImpl keyGeneratorType = new KeyGeneratorTypeImpl();
        return keyGeneratorType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SequenceTableType createSequenceTableType() {
        SequenceTableTypeImpl sequenceTableType = new SequenceTableTypeImpl();
        return sequenceTableType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SqlGeneratorType createSqlGeneratorType() {
        SqlGeneratorTypeImpl sqlGeneratorType = new SqlGeneratorTypeImpl();
        return sqlGeneratorType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PkgenPackage getPkgenPackage() {
        return (PkgenPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static PkgenPackage getPackage() {
        return PkgenPackage.eINSTANCE;
    }

} //PkgenFactoryImpl
