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
package org.apache.geronimo.xml.ns.web.impl;

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;

import org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl;

import org.apache.geronimo.xml.ns.naming.NamingPackage;

import org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl;

import org.apache.geronimo.xml.ns.security.SecurityPackage;

import org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl;

import org.apache.geronimo.xml.ns.web.ConfigParamType;
import org.apache.geronimo.xml.ns.web.ContainerConfigType;
import org.apache.geronimo.xml.ns.web.DocumentRoot;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.apache.geronimo.xml.ns.web.WebContainerType;
import org.apache.geronimo.xml.ns.web.WebFactory;
import org.apache.geronimo.xml.ns.web.WebPackage;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
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
public class WebPackageImpl extends EPackageImpl implements WebPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass configParamTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass containerConfigTypeEClass = null;

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
    private EClass webAppTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum webContainerTypeEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EDataType webContainerTypeObjectEDataType = null;

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
     * @see org.apache.geronimo.xml.ns.web.WebPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private WebPackageImpl() {
        super(eNS_URI, WebFactory.eINSTANCE);
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
    public static WebPackage init() {
        if (isInited) return (WebPackage)EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI);

        // Obtain or create and register package
        WebPackageImpl theWebPackage = (WebPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof WebPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new WebPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        XMLTypePackageImpl.init();

        // Obtain or create and register interdependencies
        NamingPackageImpl theNamingPackage = (NamingPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI) instanceof NamingPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI) : NamingPackage.eINSTANCE);
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI) instanceof DeploymentPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI) : DeploymentPackage.eINSTANCE);
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI) instanceof SecurityPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI) : SecurityPackage.eINSTANCE);

        // Create package meta-data objects
        theWebPackage.createPackageContents();
        theNamingPackage.createPackageContents();
        theDeploymentPackage.createPackageContents();
        theSecurityPackage.createPackageContents();

        // Initialize created meta-data
        theWebPackage.initializePackageContents();
        theNamingPackage.initializePackageContents();
        theDeploymentPackage.initializePackageContents();
        theSecurityPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theWebPackage.freeze();

        return theWebPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getConfigParamType() {
        return configParamTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getConfigParamType_Value() {
        return (EAttribute)configParamTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getConfigParamType_Name() {
        return (EAttribute)configParamTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getContainerConfigType() {
        return containerConfigTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getContainerConfigType_ConfigParam() {
        return (EReference)containerConfigTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getContainerConfigType_Container() {
        return (EAttribute)containerConfigTypeEClass.getEStructuralFeatures().get(1);
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
    public EReference getDocumentRoot_WebApp() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getWebAppType() {
        return webAppTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_Dependency() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebAppType_ContextRoot() {
        return (EAttribute)webAppTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebAppType_ContextPriorityClassloader() {
        return (EAttribute)webAppTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_ContainerConfig() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebAppType_SecurityRealmName() {
        return (EAttribute)webAppTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_Security() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_EjbRef() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_EjbLocalRef() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_ServiceRef() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_ResourceRef() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_ResourceEnvRef() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getWebAppType_Gbean() {
        return (EReference)webAppTypeEClass.getEStructuralFeatures().get(11);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebAppType_ConfigId() {
        return (EAttribute)webAppTypeEClass.getEStructuralFeatures().get(12);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebAppType_ParentId() {
        return (EAttribute)webAppTypeEClass.getEStructuralFeatures().get(13);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getWebContainerType() {
        return webContainerTypeEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EDataType getWebContainerTypeObject() {
        return webContainerTypeObjectEDataType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebFactory getWebFactory() {
        return (WebFactory)getEFactoryInstance();
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
        configParamTypeEClass = createEClass(CONFIG_PARAM_TYPE);
        createEAttribute(configParamTypeEClass, CONFIG_PARAM_TYPE__VALUE);
        createEAttribute(configParamTypeEClass, CONFIG_PARAM_TYPE__NAME);

        containerConfigTypeEClass = createEClass(CONTAINER_CONFIG_TYPE);
        createEReference(containerConfigTypeEClass, CONTAINER_CONFIG_TYPE__CONFIG_PARAM);
        createEAttribute(containerConfigTypeEClass, CONTAINER_CONFIG_TYPE__CONTAINER);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__WEB_APP);

        webAppTypeEClass = createEClass(WEB_APP_TYPE);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__DEPENDENCY);
        createEAttribute(webAppTypeEClass, WEB_APP_TYPE__CONTEXT_ROOT);
        createEAttribute(webAppTypeEClass, WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__CONTAINER_CONFIG);
        createEAttribute(webAppTypeEClass, WEB_APP_TYPE__SECURITY_REALM_NAME);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__SECURITY);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__EJB_REF);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__EJB_LOCAL_REF);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__SERVICE_REF);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__RESOURCE_REF);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__RESOURCE_ENV_REF);
        createEReference(webAppTypeEClass, WEB_APP_TYPE__GBEAN);
        createEAttribute(webAppTypeEClass, WEB_APP_TYPE__CONFIG_ID);
        createEAttribute(webAppTypeEClass, WEB_APP_TYPE__PARENT_ID);

        // Create enums
        webContainerTypeEEnum = createEEnum(WEB_CONTAINER_TYPE);

        // Create data types
        webContainerTypeObjectEDataType = createEDataType(WEB_CONTAINER_TYPE_OBJECT);
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
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI);
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI);
        NamingPackageImpl theNamingPackage = (NamingPackageImpl)EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI);

        // Add supertypes to classes

        // Initialize classes and features; add operations and parameters
        initEClass(configParamTypeEClass, ConfigParamType.class, "ConfigParamType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getConfigParamType_Value(), theXMLTypePackage.getString(), "value", null, 0, 1, ConfigParamType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getConfigParamType_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, ConfigParamType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(containerConfigTypeEClass, ContainerConfigType.class, "ContainerConfigType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getContainerConfigType_ConfigParam(), this.getConfigParamType(), null, "configParam", null, 0, -1, ContainerConfigType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getContainerConfigType_Container(), this.getWebContainerType(), "container", "Tomcat", 1, 1, ContainerConfigType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_WebApp(), this.getWebAppType(), null, "webApp", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(webAppTypeEClass, WebAppType.class, "WebAppType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getWebAppType_Dependency(), theDeploymentPackage.getDependencyType(), null, "dependency", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebAppType_ContextRoot(), theXMLTypePackage.getString(), "contextRoot", null, 0, 1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebAppType_ContextPriorityClassloader(), theXMLTypePackage.getBoolean(), "contextPriorityClassloader", null, 1, 1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_ContainerConfig(), this.getContainerConfigType(), null, "containerConfig", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebAppType_SecurityRealmName(), theXMLTypePackage.getString(), "securityRealmName", null, 0, 1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_Security(), theSecurityPackage.getSecurityType(), null, "security", null, 0, 1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_EjbRef(), theNamingPackage.getEjbRefType(), null, "ejbRef", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_EjbLocalRef(), theNamingPackage.getEjbLocalRefType(), null, "ejbLocalRef", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_ServiceRef(), theNamingPackage.getServiceRefType(), null, "serviceRef", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_ResourceRef(), theNamingPackage.getResourceRefType(), null, "resourceRef", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_ResourceEnvRef(), theNamingPackage.getResourceEnvRefType(), null, "resourceEnvRef", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getWebAppType_Gbean(), theDeploymentPackage.getGbeanType(), null, "gbean", null, 0, -1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebAppType_ConfigId(), theXMLTypePackage.getString(), "configId", null, 1, 1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebAppType_ParentId(), theXMLTypePackage.getString(), "parentId", null, 0, 1, WebAppType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(webContainerTypeEEnum, WebContainerType.class, "WebContainerType");
        addEEnumLiteral(webContainerTypeEEnum, WebContainerType.TOMCAT_LITERAL);
        addEEnumLiteral(webContainerTypeEEnum, WebContainerType.JETTY_LITERAL);

        // Initialize data types
        initEDataType(webContainerTypeObjectEDataType, WebContainerType.class, "WebContainerTypeObject", IS_SERIALIZABLE, IS_GENERATED_INSTANCE_CLASS);

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
          (configParamTypeEClass, 
           source, 
           new String[] {
             "name", "config-paramType",
             "kind", "simple"
           });		
        addAnnotation
          (getConfigParamType_Value(), 
           source, 
           new String[] {
             "name", ":0",
             "kind", "simple"
           });		
        addAnnotation
          (getConfigParamType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });			
        addAnnotation
          (containerConfigTypeEClass, 
           source, 
           new String[] {
             "name", "container-configType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getContainerConfigType_ConfigParam(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "config-param",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getContainerConfigType_Container(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "container"
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
          (getDocumentRoot_WebApp(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "web-app",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (webAppTypeEClass, 
           source, 
           new String[] {
             "name", "web-appType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getWebAppType_Dependency(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "dependency",
             "namespace", "http://geronimo.apache.org/xml/ns/deployment"
           });		
        addAnnotation
          (getWebAppType_ContextRoot(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "context-root",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebAppType_ContextPriorityClassloader(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "context-priority-classloader",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebAppType_ContainerConfig(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "container-config",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebAppType_SecurityRealmName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "security-realm-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebAppType_Security(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "security",
             "namespace", "http://geronimo.apache.org/xml/ns/security"
           });		
        addAnnotation
          (getWebAppType_EjbRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getWebAppType_EjbLocalRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-local-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getWebAppType_ServiceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getWebAppType_ResourceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getWebAppType_ResourceEnvRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-env-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getWebAppType_Gbean(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean",
             "namespace", "http://geronimo.apache.org/xml/ns/deployment"
           });		
        addAnnotation
          (getWebAppType_ConfigId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "configId"
           });		
        addAnnotation
          (getWebAppType_ParentId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "parentId"
           });		
        addAnnotation
          (webContainerTypeEEnum, 
           source, 
           new String[] {
             "name", "web-containerType"
           });		
        addAnnotation
          (webContainerTypeObjectEDataType, 
           source, 
           new String[] {
             "name", "web-containerType:Object",
             "baseType", "web-containerType"
           });
    }

} //WebPackageImpl
