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
package org.apache.geronimo.xml.ns.security.impl;

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;

import org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl;

import org.apache.geronimo.xml.ns.naming.NamingPackage;

import org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl;

import org.apache.geronimo.xml.ns.security.DefaultPrincipalType;
import org.apache.geronimo.xml.ns.security.DescriptionType;
import org.apache.geronimo.xml.ns.security.DistinguishedNameType;
import org.apache.geronimo.xml.ns.security.DocumentRoot;
import org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType;
import org.apache.geronimo.xml.ns.security.PrincipalType;
import org.apache.geronimo.xml.ns.security.RealmType;
import org.apache.geronimo.xml.ns.security.RoleMappingsType;
import org.apache.geronimo.xml.ns.security.RoleType;
import org.apache.geronimo.xml.ns.security.SecurityFactory;
import org.apache.geronimo.xml.ns.security.SecurityPackage;
import org.apache.geronimo.xml.ns.security.SecurityType;

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
public class SecurityPackageImpl extends EPackageImpl implements SecurityPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass defaultPrincipalTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass descriptionTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass distinguishedNameTypeEClass = null;

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
    private EClass namedUsernamePasswordCredentialTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass principalTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass realmTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass roleMappingsTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass roleTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass securityTypeEClass = null;

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
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private SecurityPackageImpl() {
        super(eNS_URI, SecurityFactory.eINSTANCE);
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
    public static SecurityPackage init() {
        if (isInited) return (SecurityPackage)EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI);

        // Obtain or create and register package
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof SecurityPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new SecurityPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        XMLTypePackageImpl.init();

        // Obtain or create and register interdependencies
        NamingPackageImpl theNamingPackage = (NamingPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI) instanceof NamingPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI) : NamingPackage.eINSTANCE);
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI) instanceof DeploymentPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI) : DeploymentPackage.eINSTANCE);
        WebPackageImpl theWebPackage = (WebPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI) instanceof WebPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI) : WebPackage.eINSTANCE);

        // Create package meta-data objects
        theSecurityPackage.createPackageContents();
        theNamingPackage.createPackageContents();
        theDeploymentPackage.createPackageContents();
        theWebPackage.createPackageContents();

        // Initialize created meta-data
        theSecurityPackage.initializePackageContents();
        theNamingPackage.initializePackageContents();
        theDeploymentPackage.initializePackageContents();
        theWebPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theSecurityPackage.freeze();

        return theSecurityPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDefaultPrincipalType() {
        return defaultPrincipalTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDefaultPrincipalType_Description() {
        return (EReference)defaultPrincipalTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDefaultPrincipalType_Principal() {
        return (EReference)defaultPrincipalTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDefaultPrincipalType_NamedUsernamePasswordCredential() {
        return (EReference)defaultPrincipalTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDefaultPrincipalType_RealmName() {
        return (EAttribute)defaultPrincipalTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDescriptionType() {
        return descriptionTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDescriptionType_Value() {
        return (EAttribute)descriptionTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDescriptionType_Lang() {
        return (EAttribute)descriptionTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDistinguishedNameType() {
        return distinguishedNameTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDistinguishedNameType_Description() {
        return (EReference)distinguishedNameTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDistinguishedNameType_DesignatedRunAs() {
        return (EAttribute)distinguishedNameTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDistinguishedNameType_Name() {
        return (EAttribute)distinguishedNameTypeEClass.getEStructuralFeatures().get(2);
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
    public EReference getDocumentRoot_DefaultPrincipal() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_Security() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getNamedUsernamePasswordCredentialType() {
        return namedUsernamePasswordCredentialTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getNamedUsernamePasswordCredentialType_Name() {
        return (EAttribute)namedUsernamePasswordCredentialTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getNamedUsernamePasswordCredentialType_Username() {
        return (EAttribute)namedUsernamePasswordCredentialTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getNamedUsernamePasswordCredentialType_Password() {
        return (EAttribute)namedUsernamePasswordCredentialTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getPrincipalType() {
        return principalTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getPrincipalType_Description() {
        return (EReference)principalTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPrincipalType_Class() {
        return (EAttribute)principalTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPrincipalType_DesignatedRunAs() {
        return (EAttribute)principalTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPrincipalType_Name() {
        return (EAttribute)principalTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getRealmType() {
        return realmTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRealmType_Description() {
        return (EReference)realmTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRealmType_Principal() {
        return (EReference)realmTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getRealmType_RealmName() {
        return (EAttribute)realmTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getRoleMappingsType() {
        return roleMappingsTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRoleMappingsType_Role() {
        return (EReference)roleMappingsTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getRoleType() {
        return roleTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRoleType_Description() {
        return (EReference)roleTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRoleType_Realm() {
        return (EReference)roleTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRoleType_DistinguishedName() {
        return (EReference)roleTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getRoleType_RoleName() {
        return (EAttribute)roleTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getSecurityType() {
        return securityTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSecurityType_Description() {
        return (EReference)securityTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSecurityType_DefaultPrincipal() {
        return (EReference)securityTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSecurityType_RoleMappings() {
        return (EReference)securityTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSecurityType_DefaultRole() {
        return (EAttribute)securityTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSecurityType_DoasCurrentCaller() {
        return (EAttribute)securityTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSecurityType_UseContextHandler() {
        return (EAttribute)securityTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityFactory getSecurityFactory() {
        return (SecurityFactory)getEFactoryInstance();
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
        defaultPrincipalTypeEClass = createEClass(DEFAULT_PRINCIPAL_TYPE);
        createEReference(defaultPrincipalTypeEClass, DEFAULT_PRINCIPAL_TYPE__DESCRIPTION);
        createEReference(defaultPrincipalTypeEClass, DEFAULT_PRINCIPAL_TYPE__PRINCIPAL);
        createEReference(defaultPrincipalTypeEClass, DEFAULT_PRINCIPAL_TYPE__NAMED_USERNAME_PASSWORD_CREDENTIAL);
        createEAttribute(defaultPrincipalTypeEClass, DEFAULT_PRINCIPAL_TYPE__REALM_NAME);

        descriptionTypeEClass = createEClass(DESCRIPTION_TYPE);
        createEAttribute(descriptionTypeEClass, DESCRIPTION_TYPE__VALUE);
        createEAttribute(descriptionTypeEClass, DESCRIPTION_TYPE__LANG);

        distinguishedNameTypeEClass = createEClass(DISTINGUISHED_NAME_TYPE);
        createEReference(distinguishedNameTypeEClass, DISTINGUISHED_NAME_TYPE__DESCRIPTION);
        createEAttribute(distinguishedNameTypeEClass, DISTINGUISHED_NAME_TYPE__DESIGNATED_RUN_AS);
        createEAttribute(distinguishedNameTypeEClass, DISTINGUISHED_NAME_TYPE__NAME);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__DEFAULT_PRINCIPAL);
        createEReference(documentRootEClass, DOCUMENT_ROOT__SECURITY);

        namedUsernamePasswordCredentialTypeEClass = createEClass(NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE);
        createEAttribute(namedUsernamePasswordCredentialTypeEClass, NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE__NAME);
        createEAttribute(namedUsernamePasswordCredentialTypeEClass, NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE__USERNAME);
        createEAttribute(namedUsernamePasswordCredentialTypeEClass, NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE__PASSWORD);

        principalTypeEClass = createEClass(PRINCIPAL_TYPE);
        createEReference(principalTypeEClass, PRINCIPAL_TYPE__DESCRIPTION);
        createEAttribute(principalTypeEClass, PRINCIPAL_TYPE__CLASS);
        createEAttribute(principalTypeEClass, PRINCIPAL_TYPE__DESIGNATED_RUN_AS);
        createEAttribute(principalTypeEClass, PRINCIPAL_TYPE__NAME);

        realmTypeEClass = createEClass(REALM_TYPE);
        createEReference(realmTypeEClass, REALM_TYPE__DESCRIPTION);
        createEReference(realmTypeEClass, REALM_TYPE__PRINCIPAL);
        createEAttribute(realmTypeEClass, REALM_TYPE__REALM_NAME);

        roleMappingsTypeEClass = createEClass(ROLE_MAPPINGS_TYPE);
        createEReference(roleMappingsTypeEClass, ROLE_MAPPINGS_TYPE__ROLE);

        roleTypeEClass = createEClass(ROLE_TYPE);
        createEReference(roleTypeEClass, ROLE_TYPE__DESCRIPTION);
        createEReference(roleTypeEClass, ROLE_TYPE__REALM);
        createEReference(roleTypeEClass, ROLE_TYPE__DISTINGUISHED_NAME);
        createEAttribute(roleTypeEClass, ROLE_TYPE__ROLE_NAME);

        securityTypeEClass = createEClass(SECURITY_TYPE);
        createEReference(securityTypeEClass, SECURITY_TYPE__DESCRIPTION);
        createEReference(securityTypeEClass, SECURITY_TYPE__DEFAULT_PRINCIPAL);
        createEReference(securityTypeEClass, SECURITY_TYPE__ROLE_MAPPINGS);
        createEAttribute(securityTypeEClass, SECURITY_TYPE__DEFAULT_ROLE);
        createEAttribute(securityTypeEClass, SECURITY_TYPE__DOAS_CURRENT_CALLER);
        createEAttribute(securityTypeEClass, SECURITY_TYPE__USE_CONTEXT_HANDLER);
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
        initEClass(defaultPrincipalTypeEClass, DefaultPrincipalType.class, "DefaultPrincipalType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getDefaultPrincipalType_Description(), this.getDescriptionType(), null, "description", null, 0, -1, DefaultPrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDefaultPrincipalType_Principal(), this.getPrincipalType(), null, "principal", null, 1, 1, DefaultPrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDefaultPrincipalType_NamedUsernamePasswordCredential(), this.getNamedUsernamePasswordCredentialType(), null, "namedUsernamePasswordCredential", null, 0, -1, DefaultPrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDefaultPrincipalType_RealmName(), theXMLTypePackage.getString(), "realmName", null, 1, 1, DefaultPrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(descriptionTypeEClass, DescriptionType.class, "DescriptionType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDescriptionType_Value(), theXMLTypePackage.getString(), "value", null, 0, 1, DescriptionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDescriptionType_Lang(), theXMLTypePackage.getLanguage(), "lang", null, 0, 1, DescriptionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(distinguishedNameTypeEClass, DistinguishedNameType.class, "DistinguishedNameType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getDistinguishedNameType_Description(), this.getDescriptionType(), null, "description", null, 0, -1, DistinguishedNameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDistinguishedNameType_DesignatedRunAs(), theXMLTypePackage.getBoolean(), "designatedRunAs", "false", 0, 1, DistinguishedNameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getDistinguishedNameType_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, DistinguishedNameType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_DefaultPrincipal(), this.getDefaultPrincipalType(), null, "defaultPrincipal", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_Security(), this.getSecurityType(), null, "security", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(namedUsernamePasswordCredentialTypeEClass, NamedUsernamePasswordCredentialType.class, "NamedUsernamePasswordCredentialType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getNamedUsernamePasswordCredentialType_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, NamedUsernamePasswordCredentialType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getNamedUsernamePasswordCredentialType_Username(), theXMLTypePackage.getString(), "username", null, 1, 1, NamedUsernamePasswordCredentialType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getNamedUsernamePasswordCredentialType_Password(), theXMLTypePackage.getString(), "password", null, 1, 1, NamedUsernamePasswordCredentialType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(principalTypeEClass, PrincipalType.class, "PrincipalType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getPrincipalType_Description(), this.getDescriptionType(), null, "description", null, 0, -1, PrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPrincipalType_Class(), theXMLTypePackage.getString(), "class", null, 1, 1, PrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPrincipalType_DesignatedRunAs(), theXMLTypePackage.getBoolean(), "designatedRunAs", "false", 0, 1, PrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPrincipalType_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, PrincipalType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(realmTypeEClass, RealmType.class, "RealmType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getRealmType_Description(), this.getDescriptionType(), null, "description", null, 0, -1, RealmType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getRealmType_Principal(), this.getPrincipalType(), null, "principal", null, 1, -1, RealmType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getRealmType_RealmName(), theXMLTypePackage.getString(), "realmName", null, 1, 1, RealmType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(roleMappingsTypeEClass, RoleMappingsType.class, "RoleMappingsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getRoleMappingsType_Role(), this.getRoleType(), null, "role", null, 1, -1, RoleMappingsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(roleTypeEClass, RoleType.class, "RoleType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getRoleType_Description(), this.getDescriptionType(), null, "description", null, 0, -1, RoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getRoleType_Realm(), this.getRealmType(), null, "realm", null, 0, -1, RoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getRoleType_DistinguishedName(), this.getDistinguishedNameType(), null, "distinguishedName", null, 0, -1, RoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getRoleType_RoleName(), theXMLTypePackage.getString(), "roleName", null, 1, 1, RoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(securityTypeEClass, SecurityType.class, "SecurityType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getSecurityType_Description(), this.getDescriptionType(), null, "description", null, 0, -1, SecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSecurityType_DefaultPrincipal(), this.getDefaultPrincipalType(), null, "defaultPrincipal", null, 1, 1, SecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSecurityType_RoleMappings(), this.getRoleMappingsType(), null, "roleMappings", null, 0, 1, SecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSecurityType_DefaultRole(), theXMLTypePackage.getString(), "defaultRole", null, 0, 1, SecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSecurityType_DoasCurrentCaller(), theXMLTypePackage.getBoolean(), "doasCurrentCaller", "false", 0, 1, SecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSecurityType_UseContextHandler(), theXMLTypePackage.getBoolean(), "useContextHandler", "false", 0, 1, SecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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
          (defaultPrincipalTypeEClass, 
           source, 
           new String[] {
             "name", "default-principalType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getDefaultPrincipalType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDefaultPrincipalType_Principal(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "principal",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDefaultPrincipalType_NamedUsernamePasswordCredential(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "named-username-password-credential",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDefaultPrincipalType_RealmName(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "realm-name"
           });		
        addAnnotation
          (descriptionTypeEClass, 
           source, 
           new String[] {
             "name", "descriptionType",
             "kind", "simple"
           });		
        addAnnotation
          (getDescriptionType_Value(), 
           source, 
           new String[] {
             "name", ":0",
             "kind", "simple"
           });		
        addAnnotation
          (getDescriptionType_Lang(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "lang",
             "namespace", "http://www.w3.org/XML/1998/namespace"
           });		
        addAnnotation
          (distinguishedNameTypeEClass, 
           source, 
           new String[] {
             "name", "distinguishedNameType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getDistinguishedNameType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (getDistinguishedNameType_DesignatedRunAs(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "designated-run-as"
           });		
        addAnnotation
          (getDistinguishedNameType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
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
          (getDocumentRoot_DefaultPrincipal(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "default-principal",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_Security(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "security",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (namedUsernamePasswordCredentialTypeEClass, 
           source, 
           new String[] {
             "name", "named-username-password-credentialType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getNamedUsernamePasswordCredentialType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getNamedUsernamePasswordCredentialType_Username(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "username",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getNamedUsernamePasswordCredentialType_Password(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "password",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (principalTypeEClass, 
           source, 
           new String[] {
             "name", "principalType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getPrincipalType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPrincipalType_Class(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "class"
           });			
        addAnnotation
          (getPrincipalType_DesignatedRunAs(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "designated-run-as"
           });		
        addAnnotation
          (getPrincipalType_Name(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "name"
           });		
        addAnnotation
          (realmTypeEClass, 
           source, 
           new String[] {
             "name", "realmType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getRealmType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getRealmType_Principal(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "principal",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getRealmType_RealmName(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "realm-name"
           });		
        addAnnotation
          (roleMappingsTypeEClass, 
           source, 
           new String[] {
             "name", "role-mappingsType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getRoleMappingsType_Role(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "role",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (roleTypeEClass, 
           source, 
           new String[] {
             "name", "roleType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getRoleType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getRoleType_Realm(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "realm",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getRoleType_DistinguishedName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "distinguished-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getRoleType_RoleName(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "role-name"
           });			
        addAnnotation
          (securityTypeEClass, 
           source, 
           new String[] {
             "name", "securityType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getSecurityType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSecurityType_DefaultPrincipal(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "default-principal",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSecurityType_RoleMappings(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "role-mappings",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (getSecurityType_DefaultRole(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "default-role"
           });			
        addAnnotation
          (getSecurityType_DoasCurrentCaller(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "doas-current-caller"
           });			
        addAnnotation
          (getSecurityType_UseContextHandler(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "use-context-handler"
           });
    }

} //SecurityPackageImpl
