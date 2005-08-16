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
package org.apache.geronimo.xml.ns.j2ee.application.impl;

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;

import org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl;

import org.apache.geronimo.xml.ns.j2ee.application.ApplicationFactory;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationType;
import org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot;
import org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType;
import org.apache.geronimo.xml.ns.j2ee.application.ModuleType;
import org.apache.geronimo.xml.ns.j2ee.application.PathType;

import org.apache.geronimo.xml.ns.security.SecurityPackage;

import org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl;

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
public class ApplicationPackageImpl extends EPackageImpl implements ApplicationPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass applicationTypeEClass = null;

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
    private EClass extModuleTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass moduleTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass pathTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass stringEClass = null;

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
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private ApplicationPackageImpl() {
        super(eNS_URI, ApplicationFactory.eINSTANCE);
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
    public static ApplicationPackage init() {
        if (isInited) return (ApplicationPackage)EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI);

        // Obtain or create and register package
        ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new ApplicationPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        DeploymentPackageImpl.init();
        SecurityPackageImpl.init();
        XMLTypePackageImpl.init();

        // Create package meta-data objects
        theApplicationPackage.createPackageContents();

        // Initialize created meta-data
        theApplicationPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theApplicationPackage.freeze();

        return theApplicationPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getApplicationType() {
        return applicationTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getApplicationType_Dependency() {
        return (EReference)applicationTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getApplicationType_Module() {
        return (EReference)applicationTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getApplicationType_ExtModule() {
        return (EReference)applicationTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getApplicationType_Security() {
        return (EReference)applicationTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getApplicationType_Gbean() {
        return (EReference)applicationTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getApplicationType_ApplicationName() {
        return (EAttribute)applicationTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getApplicationType_ConfigId() {
        return (EAttribute)applicationTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getApplicationType_ParentId() {
        return (EAttribute)applicationTypeEClass.getEStructuralFeatures().get(7);
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
    public EReference getDocumentRoot_Application() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getExtModuleType() {
        return extModuleTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getExtModuleType_Connector() {
        return (EReference)extModuleTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getExtModuleType_Ejb() {
        return (EReference)extModuleTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getExtModuleType_Java() {
        return (EReference)extModuleTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getExtModuleType_Web() {
        return (EReference)extModuleTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getExtModuleType_InternalPath() {
        return (EAttribute)extModuleTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getExtModuleType_ExternalPath() {
        return (EAttribute)extModuleTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getExtModuleType_Any() {
        return (EAttribute)extModuleTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getModuleType() {
        return moduleTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModuleType_Connector() {
        return (EReference)moduleTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModuleType_Ejb() {
        return (EReference)moduleTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModuleType_Java() {
        return (EReference)moduleTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModuleType_Web() {
        return (EReference)moduleTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModuleType_AltDd() {
        return (EReference)moduleTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getModuleType_Any() {
        return (EAttribute)moduleTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getPathType() {
        return pathTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getString() {
        return stringEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getString_Value() {
        return (EAttribute)stringEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getString_Id() {
        return (EAttribute)stringEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ApplicationFactory getApplicationFactory() {
        return (ApplicationFactory)getEFactoryInstance();
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
        applicationTypeEClass = createEClass(APPLICATION_TYPE);
        createEReference(applicationTypeEClass, APPLICATION_TYPE__DEPENDENCY);
        createEReference(applicationTypeEClass, APPLICATION_TYPE__MODULE);
        createEReference(applicationTypeEClass, APPLICATION_TYPE__EXT_MODULE);
        createEReference(applicationTypeEClass, APPLICATION_TYPE__SECURITY);
        createEReference(applicationTypeEClass, APPLICATION_TYPE__GBEAN);
        createEAttribute(applicationTypeEClass, APPLICATION_TYPE__APPLICATION_NAME);
        createEAttribute(applicationTypeEClass, APPLICATION_TYPE__CONFIG_ID);
        createEAttribute(applicationTypeEClass, APPLICATION_TYPE__PARENT_ID);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__APPLICATION);

        extModuleTypeEClass = createEClass(EXT_MODULE_TYPE);
        createEReference(extModuleTypeEClass, EXT_MODULE_TYPE__CONNECTOR);
        createEReference(extModuleTypeEClass, EXT_MODULE_TYPE__EJB);
        createEReference(extModuleTypeEClass, EXT_MODULE_TYPE__JAVA);
        createEReference(extModuleTypeEClass, EXT_MODULE_TYPE__WEB);
        createEAttribute(extModuleTypeEClass, EXT_MODULE_TYPE__INTERNAL_PATH);
        createEAttribute(extModuleTypeEClass, EXT_MODULE_TYPE__EXTERNAL_PATH);
        createEAttribute(extModuleTypeEClass, EXT_MODULE_TYPE__ANY);

        moduleTypeEClass = createEClass(MODULE_TYPE);
        createEReference(moduleTypeEClass, MODULE_TYPE__CONNECTOR);
        createEReference(moduleTypeEClass, MODULE_TYPE__EJB);
        createEReference(moduleTypeEClass, MODULE_TYPE__JAVA);
        createEReference(moduleTypeEClass, MODULE_TYPE__WEB);
        createEReference(moduleTypeEClass, MODULE_TYPE__ALT_DD);
        createEAttribute(moduleTypeEClass, MODULE_TYPE__ANY);

        pathTypeEClass = createEClass(PATH_TYPE);

        stringEClass = createEClass(STRING);
        createEAttribute(stringEClass, STRING__VALUE);
        createEAttribute(stringEClass, STRING__ID);
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
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI);
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI);
        XMLTypePackageImpl theXMLTypePackage = (XMLTypePackageImpl)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

        // Add supertypes to classes
        pathTypeEClass.getESuperTypes().add(this.getString());

        // Initialize classes and features; add operations and parameters
        initEClass(applicationTypeEClass, ApplicationType.class, "ApplicationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getApplicationType_Dependency(), theDeploymentPackage.getDependencyType(), null, "dependency", null, 0, -1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getApplicationType_Module(), this.getModuleType(), null, "module", null, 0, -1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getApplicationType_ExtModule(), this.getExtModuleType(), null, "extModule", null, 0, -1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getApplicationType_Security(), theSecurityPackage.getSecurityType(), null, "security", null, 0, 1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getApplicationType_Gbean(), theDeploymentPackage.getGbeanType(), null, "gbean", null, 0, -1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getApplicationType_ApplicationName(), theXMLTypePackage.getString(), "applicationName", null, 0, 1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getApplicationType_ConfigId(), theXMLTypePackage.getString(), "configId", null, 1, 1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getApplicationType_ParentId(), theXMLTypePackage.getString(), "parentId", null, 0, 1, ApplicationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_Application(), this.getApplicationType(), null, "application", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(extModuleTypeEClass, ExtModuleType.class, "ExtModuleType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getExtModuleType_Connector(), this.getPathType(), null, "connector", null, 0, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getExtModuleType_Ejb(), this.getPathType(), null, "ejb", null, 0, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getExtModuleType_Java(), this.getPathType(), null, "java", null, 0, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getExtModuleType_Web(), this.getPathType(), null, "web", null, 0, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getExtModuleType_InternalPath(), theXMLTypePackage.getToken(), "internalPath", null, 0, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getExtModuleType_ExternalPath(), theXMLTypePackage.getToken(), "externalPath", null, 0, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getExtModuleType_Any(), ecorePackage.getEFeatureMapEntry(), "any", null, 1, 1, ExtModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(moduleTypeEClass, ModuleType.class, "ModuleType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getModuleType_Connector(), this.getPathType(), null, "connector", null, 0, 1, ModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getModuleType_Ejb(), this.getPathType(), null, "ejb", null, 0, 1, ModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getModuleType_Java(), this.getPathType(), null, "java", null, 0, 1, ModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getModuleType_Web(), this.getPathType(), null, "web", null, 0, 1, ModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getModuleType_AltDd(), this.getPathType(), null, "altDd", null, 0, 1, ModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getModuleType_Any(), ecorePackage.getEFeatureMapEntry(), "any", null, 0, 1, ModuleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(pathTypeEClass, PathType.class, "PathType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(stringEClass, org.apache.geronimo.xml.ns.j2ee.application.String.class, "String", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getString_Value(), theXMLTypePackage.getToken(), "value", null, 0, 1, org.apache.geronimo.xml.ns.j2ee.application.String.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getString_Id(), theXMLTypePackage.getID(), "id", null, 0, 1, org.apache.geronimo.xml.ns.j2ee.application.String.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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
          (applicationTypeEClass, 
           source, 
           new String[] {
             "name", "applicationType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getApplicationType_Dependency(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "dependency",
             "namespace", "http://geronimo.apache.org/xml/ns/deployment"
           });		
        addAnnotation
          (getApplicationType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getApplicationType_ExtModule(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ext-module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getApplicationType_Security(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "security",
             "namespace", "http://geronimo.apache.org/xml/ns/security"
           });		
        addAnnotation
          (getApplicationType_Gbean(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean",
             "namespace", "http://geronimo.apache.org/xml/ns/deployment"
           });		
        addAnnotation
          (getApplicationType_ApplicationName(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "application-name"
           });		
        addAnnotation
          (getApplicationType_ConfigId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "configId"
           });		
        addAnnotation
          (getApplicationType_ParentId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "parentId"
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
          (getDocumentRoot_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (extModuleTypeEClass, 
           source, 
           new String[] {
             "name", "ext-moduleType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getExtModuleType_Connector(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "connector",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getExtModuleType_Ejb(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getExtModuleType_Java(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "java",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getExtModuleType_Web(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "web",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getExtModuleType_InternalPath(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "internal-path",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getExtModuleType_ExternalPath(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "external-path",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getExtModuleType_Any(), 
           source, 
           new String[] {
             "kind", "elementWildcard",
             "wildcards", "##other",
             "name", ":6",
             "processing", "lax"
           });			
        addAnnotation
          (moduleTypeEClass, 
           source, 
           new String[] {
             "name", "moduleType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getModuleType_Connector(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "connector",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getModuleType_Ejb(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getModuleType_Java(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "java",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getModuleType_Web(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "web",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (getModuleType_AltDd(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "alt-dd",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getModuleType_Any(), 
           source, 
           new String[] {
             "kind", "elementWildcard",
             "wildcards", "##other",
             "name", ":5",
             "processing", "lax"
           });			
        addAnnotation
          (pathTypeEClass, 
           source, 
           new String[] {
             "name", "pathType",
             "kind", "simple"
           });			
        addAnnotation
          (stringEClass, 
           source, 
           new String[] {
             "name", "string",
             "kind", "simple"
           });		
        addAnnotation
          (getString_Value(), 
           source, 
           new String[] {
             "name", ":0",
             "kind", "simple"
           });		
        addAnnotation
          (getString_Id(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "id"
           });
    }

} //ApplicationPackageImpl
