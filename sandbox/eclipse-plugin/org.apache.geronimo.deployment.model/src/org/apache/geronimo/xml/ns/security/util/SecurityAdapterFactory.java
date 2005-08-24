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

import org.apache.geronimo.xml.ns.security.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage
 * @generated
 */
public class SecurityAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static SecurityPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = SecurityPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc -->
     * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
     * <!-- end-user-doc -->
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject)object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch the delegates to the <code>createXXX</code> methods.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SecuritySwitch modelSwitch =
        new SecuritySwitch() {
            public Object caseDefaultPrincipalType(DefaultPrincipalType object) {
                return createDefaultPrincipalTypeAdapter();
            }
            public Object caseDescriptionType(DescriptionType object) {
                return createDescriptionTypeAdapter();
            }
            public Object caseDistinguishedNameType(DistinguishedNameType object) {
                return createDistinguishedNameTypeAdapter();
            }
            public Object caseDocumentRoot(DocumentRoot object) {
                return createDocumentRootAdapter();
            }
            public Object caseNamedUsernamePasswordCredentialType(NamedUsernamePasswordCredentialType object) {
                return createNamedUsernamePasswordCredentialTypeAdapter();
            }
            public Object casePrincipalType(PrincipalType object) {
                return createPrincipalTypeAdapter();
            }
            public Object caseRealmType(RealmType object) {
                return createRealmTypeAdapter();
            }
            public Object caseRoleMappingsType(RoleMappingsType object) {
                return createRoleMappingsTypeAdapter();
            }
            public Object caseRoleType(RoleType object) {
                return createRoleTypeAdapter();
            }
            public Object caseSecurityType(SecurityType object) {
                return createSecurityTypeAdapter();
            }
            public Object defaultCase(EObject object) {
                return createEObjectAdapter();
            }
        };

    /**
     * Creates an adapter for the <code>target</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param target the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    public Adapter createAdapter(Notifier target) {
        return (Adapter)modelSwitch.doSwitch((EObject)target);
    }


    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType <em>Default Principal Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.DefaultPrincipalType
     * @generated
     */
    public Adapter createDefaultPrincipalTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.DescriptionType <em>Description Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.DescriptionType
     * @generated
     */
    public Adapter createDescriptionTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType <em>Distinguished Name Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.DistinguishedNameType
     * @generated
     */
    public Adapter createDistinguishedNameTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot
     * @generated
     */
    public Adapter createDocumentRootAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType <em>Named Username Password Credential Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType
     * @generated
     */
    public Adapter createNamedUsernamePasswordCredentialTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.PrincipalType <em>Principal Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.PrincipalType
     * @generated
     */
    public Adapter createPrincipalTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.RealmType <em>Realm Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.RealmType
     * @generated
     */
    public Adapter createRealmTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.RoleMappingsType <em>Role Mappings Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.RoleMappingsType
     * @generated
     */
    public Adapter createRoleMappingsTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.RoleType <em>Role Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.RoleType
     * @generated
     */
    public Adapter createRoleTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.security.SecurityType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.security.SecurityType
     * @generated
     */
    public Adapter createSecurityTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case.
     * <!-- begin-user-doc -->
     * This default implementation returns null.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} //SecurityAdapterFactory
