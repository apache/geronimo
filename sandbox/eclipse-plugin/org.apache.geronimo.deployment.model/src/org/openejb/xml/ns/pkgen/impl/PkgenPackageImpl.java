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

import org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl;

import org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl;

import org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;

import org.openejb.xml.ns.openejb.jar.JarPackage;

import org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl;

import org.openejb.xml.ns.pkgen.AutoIncrementTableType;
import org.openejb.xml.ns.pkgen.CustomGeneratorType;
import org.openejb.xml.ns.pkgen.DatabaseGeneratedType;
import org.openejb.xml.ns.pkgen.DocumentRoot;
import org.openejb.xml.ns.pkgen.KeyGeneratorType;
import org.openejb.xml.ns.pkgen.PkgenFactory;
import org.openejb.xml.ns.pkgen.PkgenPackage;
import org.openejb.xml.ns.pkgen.SequenceTableType;
import org.openejb.xml.ns.pkgen.SqlGeneratorType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PkgenPackageImpl extends EPackageImpl implements PkgenPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass autoIncrementTableTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass customGeneratorTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass databaseGeneratedTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass documentRootEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass keyGeneratorTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass sequenceTableTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass sqlGeneratorTypeEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private PkgenPackageImpl() {
        super(eNS_URI, PkgenFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this
     * model, and for any others upon which it depends.  Simple
     * dependencies are satisfied by calling this method on all
     * dependent packages before doing anything else.  This method drives
     * initialization for interdependent packages directly, in parallel
     * with this package, itself.
     * <p>Of this package and its interdependencies, all packages which
     * have not yet been registered by their URI values are first created
     * and registered.  The packages are then initialized in two steps:
     * meta-model objects for all of the packages are created before any
     * are initialized, since one package's meta-model objects may refer to
     * those of another.
     * <p>Invocation of this method will not affect any packages that have
     * already been initialized.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static PkgenPackage init() {
        if (isInited) return (PkgenPackage)EPackage.Registry.INSTANCE.getEPackage(PkgenPackage.eNS_URI);

        // Obtain or create and register package
        PkgenPackageImpl thePkgenPackage = (PkgenPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof PkgenPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new PkgenPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        NamingPackageImpl.init();
        DeploymentPackageImpl.init();
        SecurityPackageImpl.init();
        XMLTypePackageImpl.init();

        // Obtain or create and register interdependencies
        JarPackageImpl theJarPackage = (JarPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(JarPackage.eNS_URI) instanceof JarPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(JarPackage.eNS_URI) : JarPackage.eINSTANCE);

        // Create package meta-data objects
        thePkgenPackage.createPackageContents();
        theJarPackage.createPackageContents();

        // Initialize created meta-data
        thePkgenPackage.initializePackageContents();
        theJarPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        thePkgenPackage.freeze();

        return thePkgenPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getAutoIncrementTableType() {
        return autoIncrementTableTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAutoIncrementTableType_Sql() {
        return (EAttribute)autoIncrementTableTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAutoIncrementTableType_ReturnType() {
        return (EAttribute)autoIncrementTableTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCustomGeneratorType() {
        return customGeneratorTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCustomGeneratorType_GeneratorName() {
        return (EAttribute)customGeneratorTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCustomGeneratorType_PrimaryKeyClass() {
        return (EAttribute)customGeneratorTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDatabaseGeneratedType() {
        return databaseGeneratedTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDatabaseGeneratedType_IdentityColumn() {
        return (EAttribute)databaseGeneratedTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDocumentRoot() {
        return documentRootEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDocumentRoot_Mixed() {
        return (EAttribute)documentRootEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_XMLNSPrefixMap() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_XSISchemaLocation() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_KeyGenerator() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKeyGeneratorType() {
        return keyGeneratorTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKeyGeneratorType_SequenceTable() {
        return (EReference)keyGeneratorTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKeyGeneratorType_AutoIncrementTable() {
        return (EReference)keyGeneratorTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKeyGeneratorType_SqlGenerator() {
        return (EReference)keyGeneratorTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKeyGeneratorType_CustomGenerator() {
        return (EReference)keyGeneratorTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getSequenceTableType() {
        return sequenceTableTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSequenceTableType_TableName() {
        return (EAttribute)sequenceTableTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSequenceTableType_SequenceName() {
        return (EAttribute)sequenceTableTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSequenceTableType_BatchSize() {
        return (EAttribute)sequenceTableTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getSqlGeneratorType() {
        return sqlGeneratorTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSqlGeneratorType_Sql() {
        return (EAttribute)sqlGeneratorTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSqlGeneratorType_ReturnType() {
        return (EAttribute)sqlGeneratorTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PkgenFactory getPkgenFactory() {
        return (PkgenFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        autoIncrementTableTypeEClass = createEClass(AUTO_INCREMENT_TABLE_TYPE);
        createEAttribute(autoIncrementTableTypeEClass, AUTO_INCREMENT_TABLE_TYPE__SQL);
        createEAttribute(autoIncrementTableTypeEClass, AUTO_INCREMENT_TABLE_TYPE__RETURN_TYPE);

        customGeneratorTypeEClass = createEClass(CUSTOM_GENERATOR_TYPE);
        createEAttribute(customGeneratorTypeEClass, CUSTOM_GENERATOR_TYPE__GENERATOR_NAME);
        createEAttribute(customGeneratorTypeEClass, CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS);

        databaseGeneratedTypeEClass = createEClass(DATABASE_GENERATED_TYPE);
        createEAttribute(databaseGeneratedTypeEClass, DATABASE_GENERATED_TYPE__IDENTITY_COLUMN);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__KEY_GENERATOR);

        keyGeneratorTypeEClass = createEClass(KEY_GENERATOR_TYPE);
        createEReference(keyGeneratorTypeEClass, KEY_GENERATOR_TYPE__SEQUENCE_TABLE);
        createEReference(keyGeneratorTypeEClass, KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE);
        createEReference(keyGeneratorTypeEClass, KEY_GENERATOR_TYPE__SQL_GENERATOR);
        createEReference(keyGeneratorTypeEClass, KEY_GENERATOR_TYPE__CUSTOM_GENERATOR);

        sequenceTableTypeEClass = createEClass(SEQUENCE_TABLE_TYPE);
        createEAttribute(sequenceTableTypeEClass, SEQUENCE_TABLE_TYPE__TABLE_NAME);
        createEAttribute(sequenceTableTypeEClass, SEQUENCE_TABLE_TYPE__SEQUENCE_NAME);
        createEAttribute(sequenceTableTypeEClass, SEQUENCE_TABLE_TYPE__BATCH_SIZE);

        sqlGeneratorTypeEClass = createEClass(SQL_GENERATOR_TYPE);
        createEAttribute(sqlGeneratorTypeEClass, SQL_GENERATOR_TYPE__SQL);
        createEAttribute(sqlGeneratorTypeEClass, SQL_GENERATOR_TYPE__RETURN_TYPE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        XMLTypePackageImpl theXMLTypePackage = (XMLTypePackageImpl)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

        // Add supertypes to classes

        // Initialize classes and features; add operations and parameters
        initEClass(autoIncrementTableTypeEClass, AutoIncrementTableType.class, "AutoIncrementTableType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getAutoIncrementTableType_Sql(), theXMLTypePackage.getString(), "sql", null, 1, 1, AutoIncrementTableType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAutoIncrementTableType_ReturnType(), theXMLTypePackage.getString(), "returnType", null, 1, 1, AutoIncrementTableType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(customGeneratorTypeEClass, CustomGeneratorType.class, "CustomGeneratorType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCustomGeneratorType_GeneratorName(), theXMLTypePackage.getString(), "generatorName", null, 1, 1, CustomGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCustomGeneratorType_PrimaryKeyClass(), theXMLTypePackage.getString(), "primaryKeyClass", null, 1, 1, CustomGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(databaseGeneratedTypeEClass, DatabaseGeneratedType.class, "DatabaseGeneratedType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDatabaseGeneratedType_IdentityColumn(), theXMLTypePackage.getString(), "identityColumn", null, 1, -1, DatabaseGeneratedType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_KeyGenerator(), this.getKeyGeneratorType(), null, "keyGenerator", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(keyGeneratorTypeEClass, KeyGeneratorType.class, "KeyGeneratorType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKeyGeneratorType_SequenceTable(), this.getSequenceTableType(), null, "sequenceTable", null, 0, 1, KeyGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKeyGeneratorType_AutoIncrementTable(), this.getAutoIncrementTableType(), null, "autoIncrementTable", null, 0, 1, KeyGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKeyGeneratorType_SqlGenerator(), this.getSqlGeneratorType(), null, "sqlGenerator", null, 0, 1, KeyGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKeyGeneratorType_CustomGenerator(), this.getCustomGeneratorType(), null, "customGenerator", null, 0, 1, KeyGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(sequenceTableTypeEClass, SequenceTableType.class, "SequenceTableType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getSequenceTableType_TableName(), theXMLTypePackage.getString(), "tableName", null, 1, 1, SequenceTableType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSequenceTableType_SequenceName(), theXMLTypePackage.getString(), "sequenceName", null, 1, 1, SequenceTableType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSequenceTableType_BatchSize(), theXMLTypePackage.getInt(), "batchSize", null, 1, 1, SequenceTableType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(sqlGeneratorTypeEClass, SqlGeneratorType.class, "SqlGeneratorType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getSqlGeneratorType_Sql(), theXMLTypePackage.getString(), "sql", null, 1, 1, SqlGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSqlGeneratorType_ReturnType(), theXMLTypePackage.getString(), "returnType", null, 1, 1, SqlGeneratorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        createResource(eNS_URI);

        // Create annotations
        // http:///org/eclipse/emf/ecore/util/ExtendedMetaData
        createExtendedMetaDataAnnotations();
    }

    /**
     * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void createExtendedMetaDataAnnotations() {
        String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";			
        addAnnotation
          (autoIncrementTableTypeEClass, 
           source, 
           new String[] {
             "name", "auto-increment-tableType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getAutoIncrementTableType_Sql(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "sql",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getAutoIncrementTableType_ReturnType(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "return-type",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (customGeneratorTypeEClass, 
           source, 
           new String[] {
             "name", "custom-generatorType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCustomGeneratorType_GeneratorName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "generator-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCustomGeneratorType_PrimaryKeyClass(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "primary-key-class",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (databaseGeneratedTypeEClass, 
           source, 
           new String[] {
             "name", "database-generatedType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getDatabaseGeneratedType_IdentityColumn(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "identity-column",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (documentRootEClass, 
           source, 
           new String[] {
             "name", "",
             "kind", "mixed"
           });		
        addAnnotation
          (getDocumentRoot_Mixed(), 
           source, 
           new String[] {
             "kind", "elementWildcard",
             "name", ":mixed"
           });		
        addAnnotation
          (getDocumentRoot_XMLNSPrefixMap(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "xmlns:prefix"
           });		
        addAnnotation
          (getDocumentRoot_XSISchemaLocation(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "xsi:schemaLocation"
           });		
        addAnnotation
          (getDocumentRoot_KeyGenerator(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "key-generator",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (keyGeneratorTypeEClass, 
           source, 
           new String[] {
             "name", "key-generatorType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getKeyGeneratorType_SequenceTable(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "sequence-table",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getKeyGeneratorType_AutoIncrementTable(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "auto-increment-table",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getKeyGeneratorType_SqlGenerator(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "sql-generator",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getKeyGeneratorType_CustomGenerator(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "custom-generator",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (sequenceTableTypeEClass, 
           source, 
           new String[] {
             "name", "sequence-tableType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getSequenceTableType_TableName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "table-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSequenceTableType_SequenceName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "sequence-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSequenceTableType_BatchSize(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "batch-size",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (sqlGeneratorTypeEClass, 
           source, 
           new String[] {
             "name", "sql-generatorType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getSqlGeneratorType_Sql(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "sql",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSqlGeneratorType_ReturnType(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "return-type",
             "namespace", "##targetNamespace"
           });
    }

} //PkgenPackageImpl
