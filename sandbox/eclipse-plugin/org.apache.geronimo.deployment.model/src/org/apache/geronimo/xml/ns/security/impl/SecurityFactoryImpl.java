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

import org.apache.geronimo.xml.ns.security.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SecurityFactoryImpl extends EFactoryImpl implements SecurityFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case SecurityPackage.DEFAULT_PRINCIPAL_TYPE: return createDefaultPrincipalType();
            case SecurityPackage.DESCRIPTION_TYPE: return createDescriptionType();
            case SecurityPackage.DISTINGUISHED_NAME_TYPE: return createDistinguishedNameType();
            case SecurityPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case SecurityPackage.NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE: return createNamedUsernamePasswordCredentialType();
            case SecurityPackage.PRINCIPAL_TYPE: return createPrincipalType();
            case SecurityPackage.REALM_TYPE: return createRealmType();
            case SecurityPackage.ROLE_MAPPINGS_TYPE: return createRoleMappingsType();
            case SecurityPackage.ROLE_TYPE: return createRoleType();
            case SecurityPackage.SECURITY_TYPE: return createSecurityType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DefaultPrincipalType createDefaultPrincipalType() {
        DefaultPrincipalTypeImpl defaultPrincipalType = new DefaultPrincipalTypeImpl();
        return defaultPrincipalType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DescriptionType createDescriptionType() {
        DescriptionTypeImpl descriptionType = new DescriptionTypeImpl();
        return descriptionType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DistinguishedNameType createDistinguishedNameType() {
        DistinguishedNameTypeImpl distinguishedNameType = new DistinguishedNameTypeImpl();
        return distinguishedNameType;
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
    public NamedUsernamePasswordCredentialType createNamedUsernamePasswordCredentialType() {
        NamedUsernamePasswordCredentialTypeImpl namedUsernamePasswordCredentialType = new NamedUsernamePasswordCredentialTypeImpl();
        return namedUsernamePasswordCredentialType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PrincipalType createPrincipalType() {
        PrincipalTypeImpl principalType = new PrincipalTypeImpl();
        return principalType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RealmType createRealmType() {
        RealmTypeImpl realmType = new RealmTypeImpl();
        return realmType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RoleMappingsType createRoleMappingsType() {
        RoleMappingsTypeImpl roleMappingsType = new RoleMappingsTypeImpl();
        return roleMappingsType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RoleType createRoleType() {
        RoleTypeImpl roleType = new RoleTypeImpl();
        return roleType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityType createSecurityType() {
        SecurityTypeImpl securityType = new SecurityTypeImpl();
        return securityType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityPackage getSecurityPackage() {
        return (SecurityPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static SecurityPackage getPackage() {
        return SecurityPackage.eINSTANCE;
    }

} //SecurityFactoryImpl
