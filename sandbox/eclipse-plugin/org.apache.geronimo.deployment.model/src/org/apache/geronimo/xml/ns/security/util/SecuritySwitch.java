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
package org.apache.geronimo.xml.ns.security.util;

import java.util.List;

import org.apache.geronimo.xml.ns.security.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage
 * @generated
 */
public class SecuritySwitch {
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static SecurityPackage modelPackage;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecuritySwitch() {
        if (modelPackage == null) {
            modelPackage = SecurityPackage.eINSTANCE;
        }
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    public Object doSwitch(EObject theEObject) {
        return doSwitch(theEObject.eClass(), theEObject);
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    protected Object doSwitch(EClass theEClass, EObject theEObject) {
        if (theEClass.eContainer() == modelPackage) {
            return doSwitch(theEClass.getClassifierID(), theEObject);
        }
        else {
            List eSuperTypes = theEClass.getESuperTypes();
            return
                eSuperTypes.isEmpty() ?
                    defaultCase(theEObject) :
                    doSwitch((EClass)eSuperTypes.get(0), theEObject);
        }
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    protected Object doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case SecurityPackage.DEFAULT_PRINCIPAL_TYPE: {
                DefaultPrincipalType defaultPrincipalType = (DefaultPrincipalType)theEObject;
                Object result = caseDefaultPrincipalType(defaultPrincipalType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.DESCRIPTION_TYPE: {
                DescriptionType descriptionType = (DescriptionType)theEObject;
                Object result = caseDescriptionType(descriptionType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.DISTINGUISHED_NAME_TYPE: {
                DistinguishedNameType distinguishedNameType = (DistinguishedNameType)theEObject;
                Object result = caseDistinguishedNameType(distinguishedNameType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.DOCUMENT_ROOT: {
                DocumentRoot documentRoot = (DocumentRoot)theEObject;
                Object result = caseDocumentRoot(documentRoot);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE: {
                NamedUsernamePasswordCredentialType namedUsernamePasswordCredentialType = (NamedUsernamePasswordCredentialType)theEObject;
                Object result = caseNamedUsernamePasswordCredentialType(namedUsernamePasswordCredentialType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.PRINCIPAL_TYPE: {
                PrincipalType principalType = (PrincipalType)theEObject;
                Object result = casePrincipalType(principalType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.REALM_TYPE: {
                RealmType realmType = (RealmType)theEObject;
                Object result = caseRealmType(realmType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.ROLE_MAPPINGS_TYPE: {
                RoleMappingsType roleMappingsType = (RoleMappingsType)theEObject;
                Object result = caseRoleMappingsType(roleMappingsType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.ROLE_TYPE: {
                RoleType roleType = (RoleType)theEObject;
                Object result = caseRoleType(roleType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case SecurityPackage.SECURITY_TYPE: {
                SecurityType securityType = (SecurityType)theEObject;
                Object result = caseSecurityType(securityType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Default Principal Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Default Principal Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseDefaultPrincipalType(DefaultPrincipalType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Description Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Description Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseDescriptionType(DescriptionType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Distinguished Name Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Distinguished Name Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseDistinguishedNameType(DistinguishedNameType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Document Root</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Document Root</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseDocumentRoot(DocumentRoot object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Named Username Password Credential Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Named Username Password Credential Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseNamedUsernamePasswordCredentialType(NamedUsernamePasswordCredentialType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Principal Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Principal Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object casePrincipalType(PrincipalType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Realm Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Realm Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseRealmType(RealmType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Role Mappings Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Role Mappings Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseRoleMappingsType(RoleMappingsType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Role Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Role Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseRoleType(RoleType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseSecurityType(SecurityType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last case anyway.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    public Object defaultCase(EObject object) {
        return null;
    }

} //SecuritySwitch
