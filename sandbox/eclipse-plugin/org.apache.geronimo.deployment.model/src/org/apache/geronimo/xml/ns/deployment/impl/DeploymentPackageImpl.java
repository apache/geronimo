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
package org.apache.geronimo.xml.ns.deployment.impl;

import org.apache.geronimo.xml.ns.deployment.AttributeType;
import org.apache.geronimo.xml.ns.deployment.ConfigurationType;
import org.apache.geronimo.xml.ns.deployment.DependencyType;
import org.apache.geronimo.xml.ns.deployment.DeploymentFactory;
import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;
import org.apache.geronimo.xml.ns.deployment.DocumentRoot;
import org.apache.geronimo.xml.ns.deployment.GbeanType;
import org.apache.geronimo.xml.ns.deployment.PatternType;
import org.apache.geronimo.xml.ns.deployment.ReferenceType;
import org.apache.geronimo.xml.ns.deployment.ReferencesType;
import org.apache.geronimo.xml.ns.deployment.XmlAttributeType;

import org.apache.geronimo.xml.ns.naming.NamingPackage;

import org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl;

import org.apache.geronimo.xml.ns.security.SecurityPackage;

import org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl;

import org.apache.geronimo.xml.ns.web.WebPackage;

import org.apache.geronimo.xml.ns.web.impl.WebPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DeploymentPackageImpl extends EPackageImpl implements DeploymentPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass attributeTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass configurationTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass dependencyTypeEClass = null;

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
    private EClass gbeanTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass patternTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass referencesTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass referenceTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass xmlAttributeTypeEClass = null;

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private DeploymentPackageImpl() {
        super(eNS_URI, DeploymentFactory.eINSTANCE);
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
    public static DeploymentPackage init() {
        if (isInited) return (DeploymentPackage)EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI);

        // Obtain or create and register package
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof DeploymentPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new DeploymentPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        XMLTypePackageImpl.init();

        // Obtain or create and register interdependencies
        NamingPackageImpl theNamingPackage = (NamingPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI) instanceof NamingPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI) : NamingPackage.eINSTANCE);
        WebPackageImpl theWebPackage = (WebPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI) instanceof WebPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI) : WebPackage.eINSTANCE);
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI) instanceof SecurityPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI) : SecurityPackage.eINSTANCE);

        // Create package meta-data objects
        theDeploymentPackage.createPackageContents();
        theNamingPackage.createPackageContents();
        theWebPackage.createPackageContents();
        theSecurityPackage.createPackageContents();

        // Initialize created meta-data
        theDeploymentPackage.initializePackageContents();
        theNamingPackage.initializePackageContents();
        theWebPackage.initializePackageContents();
        theSecurityPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theDeploymentPackage.freeze();

        return theDeploymentPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getAttributeType() {
        return attributeTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAttributeType_Value() {
        return (EAttribute)attributeTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAttributeType_Name() {
        return (EAttribute)attributeTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getAttributeType_Type() {
        return (EAttribute)attributeTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getConfigurationType() {
        return configurationTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getConfigurationType_Include() {
        return (EReference)configurationTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getConfigurationType_Dependency() {
        return (EReference)configurationTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getConfigurationType_Gbean() {
        return (EReference)configurationTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getConfigurationType_ConfigId() {
        return (EAttribute)configurationTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getConfigurationType_Domain() {
        return (EAttribute)configurationTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getConfigurationType_ParentId() {
        return (EAttribute)configurationTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getConfigurationType_Server() {
        return (EAttribute)configurationTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDependencyType() {
        return dependencyTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDependencyType_GroupId() {
        return (EAttribute)dependencyTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDependencyType_Type() {
        return (EAttribute)dependencyTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDependencyType_ArtifactId() {
        return (EAttribute)dependencyTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDependencyType_Version() {
        return (EAttribute)dependencyTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDependencyType_Uri() {
        return (EAttribute)dependencyTypeEClass.getEStructuralFeatures().get(4);
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
    public EReference getDocumentRoot_Configuration() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_Dependency() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_Gbean() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getGbeanType() {
        return gbeanTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanType_Group() {
        return (EAttribute)gbeanTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getGbeanType_Attribute() {
        return (EReference)gbeanTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getGbeanType_XmlAttribute() {
        return (EReference)gbeanTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getGbeanType_Reference() {
        return (EReference)gbeanTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getGbeanType_References() {
        return (EReference)gbeanTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getGbeanType_XmlReference() {
        return (EReference)gbeanTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanType_Class() {
        return (EAttribute)gbeanTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanType_GbeanName() {
        return (EAttribute)gbeanTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanType_Name() {
        return (EAttribute)gbeanTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getPatternType() {
        return patternTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_Domain() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_Server() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_Application() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_ModuleType() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_Module() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_Type() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_Name() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPatternType_GbeanName() {
        return (EAttribute)patternTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getReferencesType() {
        return referencesTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getReferencesType_Pattern() {
        return (EReference)referencesTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getReferencesType_Name() {
        return (EAttribute)referencesTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getReferenceType() {
        return referenceTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getReferenceType_Name1() {
        return (EAttribute)referenceTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getXmlAttributeType() {
        return xmlAttributeTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getXmlAttributeType_Any() {
        return (EAttribute)xmlAttributeTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getXmlAttributeType_Name() {
        return (EAttribute)xmlAttributeTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DeploymentFactory getDeploymentFactory() {
        return (DeploymentFactory)getEFactoryInstance();
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
        attributeTypeEClass = createEClass(ATTRIBUTE_TYPE);
        createEAttribute(attributeTypeEClass, ATTRIBUTE_TYPE__VALUE);
        createEAttribute(attributeTypeEClass, ATTRIBUTE_TYPE__NAME);
        createEAttribute(attributeTypeEClass, ATTRIBUTE_TYPE__TYPE);

        configurationTypeEClass = createEClass(CONFIGURATION_TYPE);
        createEReference(configurationTypeEClass, CONFIGURATION_TYPE__INCLUDE);
        createEReference(configurationTypeEClass, CONFIGURATION_TYPE__DEPENDENCY);
        createEReference(configurationTypeEClass, CONFIGURATION_TYPE__GBEAN);
        createEAttribute(configurationTypeEClass, CONFIGURATION_TYPE__CONFIG_ID);
        createEAttribute(configurationTypeEClass, CONFIGURATION_TYPE__DOMAIN);
        createEAttribute(configurationTypeEClass, CONFIGURATION_TYPE__PARENT_ID);
        createEAttribute(configurationTypeEClass, CONFIGURATION_TYPE__SERVER);

        dependencyTypeEClass = createEClass(DEPENDENCY_TYPE);
        createEAttribute(dependencyTypeEClass, DEPENDENCY_TYPE__GROUP_ID);
        createEAttribute(dependencyTypeEClass, DEPENDENCY_TYPE__TYPE);
        createEAttribute(dependencyTypeEClass, DEPENDENCY_TYPE__ARTIFACT_ID);
        createEAttribute(dependencyTypeEClass, DEPENDENCY_TYPE__VERSION);
        createEAttribute(dependencyTypeEClass, DEPENDENCY_TYPE__URI);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__CONFIGURATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__DEPENDENCY);
        createEReference(documentRootEClass, DOCUMENT_ROOT__GBEAN);

        gbeanTypeEClass = createEClass(GBEAN_TYPE);
        createEAttribute(gbeanTypeEClass, GBEAN_TYPE__GROUP);
        createEReference(gbeanTypeEClass, GBEAN_TYPE__ATTRIBUTE);
        createEReference(gbeanTypeEClass, GBEAN_TYPE__XML_ATTRIBUTE);
        createEReference(gbeanTypeEClass, GBEAN_TYPE__REFERENCE);
        createEReference(gbeanTypeEClass, GBEAN_TYPE__REFERENCES);
        createEReference(gbeanTypeEClass, GBEAN_TYPE__XML_REFERENCE);
        createEAttribute(gbeanTypeEClass, GBEAN_TYPE__CLASS);
        createEAttribute(gbeanTypeEClass, GBEAN_TYPE__GBEAN_NAME);
        createEAttribute(gbeanTypeEClass, GBEAN_TYPE__NAME);

        patternTypeEClass = createEClass(PATTERN_TYPE);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__DOMAIN);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__SERVER);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__APPLICATION);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__MODULE_TYPE);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__MODULE);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__TYPE);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__NAME);
        createEAttribute(patternTypeEClass, PATTERN_TYPE__GBEAN_NAME);

        referencesTypeEClass = createEClass(REFERENCES_TYPE);
        createEReference(referencesTypeEClass, REFERENCES_TYPE__PATTERN);
        createEAttribute(referencesTypeEClass, REFERENCES_TYPE__NAME);

        referenceTypeEClass = createEClass(REFERENCE_TYPE);
        createEAttribute(referenceTypeEClass, REFERENCE_TYPE__NAME1);

        xmlAttributeTypeEClass = createEClass(XML_ATTRIBUTE_TYPE);
        createEAttribute(xmlAttributeTypeEClass, XML_ATTRIBUTE_TYPE__ANY);
        createEAttribute(xmlAttributeTypeEClass, XML_ATTRIBUTE_TYPE__NAME);
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
        referenceTypeEClass.getESuperTypes().add(this.getPatternType());

        // Initialize classes and features; add operations and parameters
        initEClass(attributeTypeEClass, AttributeType.class, "AttributeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getAttributeType_Value(), theXMLTypePackage.getString(), "value", null, 0, 1, AttributeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAttributeType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, AttributeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getAttributeType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, AttributeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(configurationTypeEClass, ConfigurationType.class, "ConfigurationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getConfigurationType_Include(), this.getDependencyType(), null, "include", null, 0, -1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getConfigurationType_Dependency(), this.getDependencyType(), null, "dependency", null, 0, -1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getConfigurationType_Gbean(), this.getGbeanType(), null, "gbean", null, 0, -1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getConfigurationType_ConfigId(), theXMLTypePackage.getString(), "configId", null, 1, 1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getConfigurationType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getConfigurationType_ParentId(), theXMLTypePackage.getString(), "parentId", null, 0, 1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getConfigurationType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, ConfigurationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(dependencyTypeEClass, DependencyType.class, "DependencyType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDependencyType_GroupId(), theXMLTypePackage.getString(), "groupId", null, 0, 1, DependencyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDependencyType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, DependencyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDependencyType_ArtifactId(), theXMLTypePackage.getString(), "artifactId", null, 0, 1, DependencyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDependencyType_Version(), theXMLTypePackage.getString(), "version", null, 0, 1, DependencyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDependencyType_Uri(), theXMLTypePackage.getString(), "uri", null, 0, 1, DependencyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_Configuration(), this.getConfigurationType(), null, "configuration", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_Dependency(), this.getDependencyType(), null, "dependency", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_Gbean(), this.getGbeanType(), null, "gbean", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(gbeanTypeEClass, GbeanType.class, "GbeanType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getGbeanType_Group(), ecorePackage.getEFeatureMapEntry(), "group", null, 0, -1, GbeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getGbeanType_Attribute(), this.getAttributeType(), null, "attribute", null, 0, -1, GbeanType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getGbeanType_XmlAttribute(), this.getXmlAttributeType(), null, "xmlAttribute", null, 0, -1, GbeanType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getGbeanType_Reference(), this.getReferenceType(), null, "reference", null, 0, -1, GbeanType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getGbeanType_References(), this.getReferencesType(), null, "references", null, 0, -1, GbeanType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getGbeanType_XmlReference(), this.getXmlAttributeType(), null, "xmlReference", null, 0, -1, GbeanType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanType_Class(), theXMLTypePackage.getString(), "class", null, 1, 1, GbeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanType_GbeanName(), theXMLTypePackage.getString(), "gbeanName", null, 0, 1, GbeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, GbeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(patternTypeEClass, PatternType.class, "PatternType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getPatternType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_ModuleType(), theXMLTypePackage.getString(), "moduleType", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPatternType_GbeanName(), theXMLTypePackage.getString(), "gbeanName", null, 0, 1, PatternType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(referencesTypeEClass, ReferencesType.class, "ReferencesType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getReferencesType_Pattern(), this.getPatternType(), null, "pattern", null, 1, -1, ReferencesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getReferencesType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, ReferencesType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(referenceTypeEClass, ReferenceType.class, "ReferenceType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getReferenceType_Name1(), theXMLTypePackage.getString(), "name1", null, 0, 1, ReferenceType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(xmlAttributeTypeEClass, XmlAttributeType.class, "XmlAttributeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getXmlAttributeType_Any(), ecorePackage.getEFeatureMapEntry(), "any", null, 1, 1, XmlAttributeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getXmlAttributeType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, XmlAttributeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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
          (attributeTypeEClass, 
           source, 
           new String[] {
             "name", "attributeType",
             "kind", "simple"
           });		
        addAnnotation
          (getAttributeType_Value(), 
           source, 
           new String[] {
             "name", ":0",
             "kind", "simple"
           });		
        addAnnotation
          (getAttributeType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });		
        addAnnotation
          (getAttributeType_Type(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "type"
           });		
        addAnnotation
          (configurationTypeEClass, 
           source, 
           new String[] {
             "name", "configurationType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getConfigurationType_Include(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "include",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getConfigurationType_Dependency(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "dependency",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getConfigurationType_Gbean(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getConfigurationType_ConfigId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "configId"
           });		
        addAnnotation
          (getConfigurationType_Domain(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "domain"
           });			
        addAnnotation
          (getConfigurationType_ParentId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "parentId"
           });		
        addAnnotation
          (getConfigurationType_Server(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "server"
           });		
        addAnnotation
          (dependencyTypeEClass, 
           source, 
           new String[] {
             "name", "dependencyType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getDependencyType_GroupId(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "groupId",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDependencyType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDependencyType_ArtifactId(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "artifactId",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDependencyType_Version(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "version",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDependencyType_Uri(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "uri",
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
          (getDocumentRoot_Configuration(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "configuration",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_Dependency(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "dependency",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_Gbean(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (gbeanTypeEClass, 
           source, 
           new String[] {
             "name", "gbeanType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getGbeanType_Group(), 
           source, 
           new String[] {
             "kind", "group",
             "name", "group:0"
           });		
        addAnnotation
          (getGbeanType_Attribute(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "attribute",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getGbeanType_XmlAttribute(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "xml-attribute",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getGbeanType_Reference(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "reference",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getGbeanType_References(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "references",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getGbeanType_XmlReference(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "xml-reference",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getGbeanType_Class(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "class"
           });		
        addAnnotation
          (getGbeanType_GbeanName(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "gbeanName"
           });		
        addAnnotation
          (getGbeanType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });		
        addAnnotation
          (patternTypeEClass, 
           source, 
           new String[] {
             "name", "patternType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getPatternType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_ModuleType(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "moduleType",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPatternType_GbeanName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (referencesTypeEClass, 
           source, 
           new String[] {
             "name", "referencesType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getReferencesType_Pattern(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "pattern",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getReferencesType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });		
        addAnnotation
          (referenceTypeEClass, 
           source, 
           new String[] {
             "name", "referenceType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getReferenceType_Name1(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });		
        addAnnotation
          (xmlAttributeTypeEClass, 
           source, 
           new String[] {
             "name", "xml-attributeType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getXmlAttributeType_Any(), 
           source, 
           new String[] {
             "kind", "elementWildcard",
             "wildcards", "##other",
             "name", ":0",
             "processing", "lax"
           });		
        addAnnotation
          (getXmlAttributeType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });
    }

} //DeploymentPackageImpl
