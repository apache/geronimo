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

import java.util.Collection;

import org.apache.geronimo.xml.ns.security.DescriptionType;
import org.apache.geronimo.xml.ns.security.PrincipalType;
import org.apache.geronimo.xml.ns.security.RealmType;
import org.apache.geronimo.xml.ns.security.SecurityPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Realm Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RealmTypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RealmTypeImpl#getPrincipal <em>Principal</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RealmTypeImpl#getRealmName <em>Realm Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RealmTypeImpl extends EObjectImpl implements RealmType {
    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected EList description = null;

    /**
     * The cached value of the '{@link #getPrincipal() <em>Principal</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPrincipal()
     * @generated
     * @ordered
     */
    protected EList principal = null;

    /**
     * The default value of the '{@link #getRealmName() <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRealmName()
     * @generated
     * @ordered
     */
    protected static final String REALM_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRealmName() <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRealmName()
     * @generated
     * @ordered
     */
    protected String realmName = REALM_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected RealmTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return SecurityPackage.eINSTANCE.getRealmType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDescription() {
        if (description == null) {
            description = new EObjectContainmentEList(DescriptionType.class, this, SecurityPackage.REALM_TYPE__DESCRIPTION);
        }
        return description;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getPrincipal() {
        if (principal == null) {
            principal = new EObjectContainmentEList(PrincipalType.class, this, SecurityPackage.REALM_TYPE__PRINCIPAL);
        }
        return principal;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getRealmName() {
        return realmName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRealmName(String newRealmName) {
        String oldRealmName = realmName;
        realmName = newRealmName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.REALM_TYPE__REALM_NAME, oldRealmName, realmName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case SecurityPackage.REALM_TYPE__DESCRIPTION:
                    return ((InternalEList)getDescription()).basicRemove(otherEnd, msgs);
                case SecurityPackage.REALM_TYPE__PRINCIPAL:
                    return ((InternalEList)getPrincipal()).basicRemove(otherEnd, msgs);
                default:
                    return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
            }
        }
        return eBasicSetContainer(null, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case SecurityPackage.REALM_TYPE__DESCRIPTION:
                return getDescription();
            case SecurityPackage.REALM_TYPE__PRINCIPAL:
                return getPrincipal();
            case SecurityPackage.REALM_TYPE__REALM_NAME:
                return getRealmName();
        }
        return eDynamicGet(eFeature, resolve);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void eSet(EStructuralFeature eFeature, Object newValue) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case SecurityPackage.REALM_TYPE__DESCRIPTION:
                getDescription().clear();
                getDescription().addAll((Collection)newValue);
                return;
            case SecurityPackage.REALM_TYPE__PRINCIPAL:
                getPrincipal().clear();
                getPrincipal().addAll((Collection)newValue);
                return;
            case SecurityPackage.REALM_TYPE__REALM_NAME:
                setRealmName((String)newValue);
                return;
        }
        eDynamicSet(eFeature, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void eUnset(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case SecurityPackage.REALM_TYPE__DESCRIPTION:
                getDescription().clear();
                return;
            case SecurityPackage.REALM_TYPE__PRINCIPAL:
                getPrincipal().clear();
                return;
            case SecurityPackage.REALM_TYPE__REALM_NAME:
                setRealmName(REALM_NAME_EDEFAULT);
                return;
        }
        eDynamicUnset(eFeature);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean eIsSet(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case SecurityPackage.REALM_TYPE__DESCRIPTION:
                return description != null && !description.isEmpty();
            case SecurityPackage.REALM_TYPE__PRINCIPAL:
                return principal != null && !principal.isEmpty();
            case SecurityPackage.REALM_TYPE__REALM_NAME:
                return REALM_NAME_EDEFAULT == null ? realmName != null : !REALM_NAME_EDEFAULT.equals(realmName);
        }
        return eDynamicIsSet(eFeature);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (realmName: ");
        result.append(realmName);
        result.append(')');
        return result.toString();
    }

} //RealmTypeImpl
