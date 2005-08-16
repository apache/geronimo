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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.pkgen.PkgenFactory
 * @model kind="package"
 * @generated
 */
public interface PkgenPackage extends EPackage {
    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "pkgen";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://www.openejb.org/xml/ns/pkgen";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "pkgen";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    PkgenPackage eINSTANCE = org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl.init();

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.AutoIncrementTableTypeImpl <em>Auto Increment Table Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.AutoIncrementTableTypeImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getAutoIncrementTableType()
     * @generated
     */
    int AUTO_INCREMENT_TABLE_TYPE = 0;

    /**
     * The feature id for the '<em><b>Sql</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AUTO_INCREMENT_TABLE_TYPE__SQL = 0;

    /**
     * The feature id for the '<em><b>Return Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AUTO_INCREMENT_TABLE_TYPE__RETURN_TYPE = 1;

    /**
     * The number of structural features of the the '<em>Auto Increment Table Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int AUTO_INCREMENT_TABLE_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.CustomGeneratorTypeImpl <em>Custom Generator Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.CustomGeneratorTypeImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getCustomGeneratorType()
     * @generated
     */
    int CUSTOM_GENERATOR_TYPE = 1;

    /**
     * The feature id for the '<em><b>Generator Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CUSTOM_GENERATOR_TYPE__GENERATOR_NAME = 0;

    /**
     * The feature id for the '<em><b>Primary Key Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS = 1;

    /**
     * The number of structural features of the the '<em>Custom Generator Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CUSTOM_GENERATOR_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.DatabaseGeneratedTypeImpl <em>Database Generated Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.DatabaseGeneratedTypeImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getDatabaseGeneratedType()
     * @generated
     */
    int DATABASE_GENERATED_TYPE = 2;

    /**
     * The feature id for the '<em><b>Identity Column</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DATABASE_GENERATED_TYPE__IDENTITY_COLUMN = 0;

    /**
     * The number of structural features of the the '<em>Database Generated Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DATABASE_GENERATED_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.DocumentRootImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getDocumentRoot()
     * @generated
     */
    int DOCUMENT_ROOT = 3;

    /**
     * The feature id for the '<em><b>Mixed</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__MIXED = 0;

    /**
     * The feature id for the '<em><b>XMLNS Prefix Map</b></em>' map.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__XMLNS_PREFIX_MAP = 1;

    /**
     * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = 2;

    /**
     * The feature id for the '<em><b>Key Generator</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__KEY_GENERATOR = 3;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.KeyGeneratorTypeImpl <em>Key Generator Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.KeyGeneratorTypeImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getKeyGeneratorType()
     * @generated
     */
    int KEY_GENERATOR_TYPE = 4;

    /**
     * The feature id for the '<em><b>Sequence Table</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int KEY_GENERATOR_TYPE__SEQUENCE_TABLE = 0;

    /**
     * The feature id for the '<em><b>Auto Increment Table</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE = 1;

    /**
     * The feature id for the '<em><b>Sql Generator</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int KEY_GENERATOR_TYPE__SQL_GENERATOR = 2;

    /**
     * The feature id for the '<em><b>Custom Generator</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int KEY_GENERATOR_TYPE__CUSTOM_GENERATOR = 3;

    /**
     * The number of structural features of the the '<em>Key Generator Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int KEY_GENERATOR_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.SequenceTableTypeImpl <em>Sequence Table Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.SequenceTableTypeImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getSequenceTableType()
     * @generated
     */
    int SEQUENCE_TABLE_TYPE = 5;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SEQUENCE_TABLE_TYPE__TABLE_NAME = 0;

    /**
     * The feature id for the '<em><b>Sequence Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SEQUENCE_TABLE_TYPE__SEQUENCE_NAME = 1;

    /**
     * The feature id for the '<em><b>Batch Size</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SEQUENCE_TABLE_TYPE__BATCH_SIZE = 2;

    /**
     * The number of structural features of the the '<em>Sequence Table Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SEQUENCE_TABLE_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.pkgen.impl.SqlGeneratorTypeImpl <em>Sql Generator Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.pkgen.impl.SqlGeneratorTypeImpl
     * @see org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl#getSqlGeneratorType()
     * @generated
     */
    int SQL_GENERATOR_TYPE = 6;

    /**
     * The feature id for the '<em><b>Sql</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SQL_GENERATOR_TYPE__SQL = 0;

    /**
     * The feature id for the '<em><b>Return Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SQL_GENERATOR_TYPE__RETURN_TYPE = 1;

    /**
     * The number of structural features of the the '<em>Sql Generator Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SQL_GENERATOR_TYPE_FEATURE_COUNT = 2;


    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.AutoIncrementTableType <em>Auto Increment Table Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Auto Increment Table Type</em>'.
     * @see org.openejb.xml.ns.pkgen.AutoIncrementTableType
     * @generated
     */
    EClass getAutoIncrementTableType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.AutoIncrementTableType#getSql <em>Sql</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Sql</em>'.
     * @see org.openejb.xml.ns.pkgen.AutoIncrementTableType#getSql()
     * @see #getAutoIncrementTableType()
     * @generated
     */
    EAttribute getAutoIncrementTableType_Sql();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.AutoIncrementTableType#getReturnType <em>Return Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Return Type</em>'.
     * @see org.openejb.xml.ns.pkgen.AutoIncrementTableType#getReturnType()
     * @see #getAutoIncrementTableType()
     * @generated
     */
    EAttribute getAutoIncrementTableType_ReturnType();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.CustomGeneratorType <em>Custom Generator Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Custom Generator Type</em>'.
     * @see org.openejb.xml.ns.pkgen.CustomGeneratorType
     * @generated
     */
    EClass getCustomGeneratorType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.CustomGeneratorType#getGeneratorName <em>Generator Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Generator Name</em>'.
     * @see org.openejb.xml.ns.pkgen.CustomGeneratorType#getGeneratorName()
     * @see #getCustomGeneratorType()
     * @generated
     */
    EAttribute getCustomGeneratorType_GeneratorName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.CustomGeneratorType#getPrimaryKeyClass <em>Primary Key Class</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Primary Key Class</em>'.
     * @see org.openejb.xml.ns.pkgen.CustomGeneratorType#getPrimaryKeyClass()
     * @see #getCustomGeneratorType()
     * @generated
     */
    EAttribute getCustomGeneratorType_PrimaryKeyClass();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.DatabaseGeneratedType <em>Database Generated Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Database Generated Type</em>'.
     * @see org.openejb.xml.ns.pkgen.DatabaseGeneratedType
     * @generated
     */
    EClass getDatabaseGeneratedType();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.pkgen.DatabaseGeneratedType#getIdentityColumn <em>Identity Column</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Identity Column</em>'.
     * @see org.openejb.xml.ns.pkgen.DatabaseGeneratedType#getIdentityColumn()
     * @see #getDatabaseGeneratedType()
     * @generated
     */
    EAttribute getDatabaseGeneratedType_IdentityColumn();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.openejb.xml.ns.pkgen.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.pkgen.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.openejb.xml.ns.pkgen.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.openejb.xml.ns.pkgen.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.openejb.xml.ns.pkgen.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.openejb.xml.ns.pkgen.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.openejb.xml.ns.pkgen.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.pkgen.DocumentRoot#getKeyGenerator <em>Key Generator</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Key Generator</em>'.
     * @see org.openejb.xml.ns.pkgen.DocumentRoot#getKeyGenerator()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_KeyGenerator();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType <em>Key Generator Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Key Generator Type</em>'.
     * @see org.openejb.xml.ns.pkgen.KeyGeneratorType
     * @generated
     */
    EClass getKeyGeneratorType();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getSequenceTable <em>Sequence Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Sequence Table</em>'.
     * @see org.openejb.xml.ns.pkgen.KeyGeneratorType#getSequenceTable()
     * @see #getKeyGeneratorType()
     * @generated
     */
    EReference getKeyGeneratorType_SequenceTable();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getAutoIncrementTable <em>Auto Increment Table</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Auto Increment Table</em>'.
     * @see org.openejb.xml.ns.pkgen.KeyGeneratorType#getAutoIncrementTable()
     * @see #getKeyGeneratorType()
     * @generated
     */
    EReference getKeyGeneratorType_AutoIncrementTable();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getSqlGenerator <em>Sql Generator</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Sql Generator</em>'.
     * @see org.openejb.xml.ns.pkgen.KeyGeneratorType#getSqlGenerator()
     * @see #getKeyGeneratorType()
     * @generated
     */
    EReference getKeyGeneratorType_SqlGenerator();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType#getCustomGenerator <em>Custom Generator</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Custom Generator</em>'.
     * @see org.openejb.xml.ns.pkgen.KeyGeneratorType#getCustomGenerator()
     * @see #getKeyGeneratorType()
     * @generated
     */
    EReference getKeyGeneratorType_CustomGenerator();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.SequenceTableType <em>Sequence Table Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Sequence Table Type</em>'.
     * @see org.openejb.xml.ns.pkgen.SequenceTableType
     * @generated
     */
    EClass getSequenceTableType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getTableName <em>Table Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Table Name</em>'.
     * @see org.openejb.xml.ns.pkgen.SequenceTableType#getTableName()
     * @see #getSequenceTableType()
     * @generated
     */
    EAttribute getSequenceTableType_TableName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getSequenceName <em>Sequence Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Sequence Name</em>'.
     * @see org.openejb.xml.ns.pkgen.SequenceTableType#getSequenceName()
     * @see #getSequenceTableType()
     * @generated
     */
    EAttribute getSequenceTableType_SequenceName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.SequenceTableType#getBatchSize <em>Batch Size</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Batch Size</em>'.
     * @see org.openejb.xml.ns.pkgen.SequenceTableType#getBatchSize()
     * @see #getSequenceTableType()
     * @generated
     */
    EAttribute getSequenceTableType_BatchSize();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.pkgen.SqlGeneratorType <em>Sql Generator Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Sql Generator Type</em>'.
     * @see org.openejb.xml.ns.pkgen.SqlGeneratorType
     * @generated
     */
    EClass getSqlGeneratorType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.SqlGeneratorType#getSql <em>Sql</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Sql</em>'.
     * @see org.openejb.xml.ns.pkgen.SqlGeneratorType#getSql()
     * @see #getSqlGeneratorType()
     * @generated
     */
    EAttribute getSqlGeneratorType_Sql();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.pkgen.SqlGeneratorType#getReturnType <em>Return Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Return Type</em>'.
     * @see org.openejb.xml.ns.pkgen.SqlGeneratorType#getReturnType()
     * @see #getSqlGeneratorType()
     * @generated
     */
    EAttribute getSqlGeneratorType_ReturnType();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    PkgenFactory getPkgenFactory();

} //PkgenPackage
