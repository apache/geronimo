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
import org.apache.geronimo.xml.ns.security.DistinguishedNameType;
import org.apache.geronimo.xml.ns.security.RealmType;
import org.apache.geronimo.xml.ns.security.RoleType;
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
 * An implementation of the model object '<em><b>Role Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RoleTypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RoleTypeImpl#getRealm <em>Realm</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RoleTypeImpl#getDistinguishedName <em>Distinguished Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.RoleTypeImpl#getRoleName <em>Role Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RoleTypeImpl extends EObjectImpl implements RoleType {
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
     * The cached value of the '{@link #getRealm() <em>Realm</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRealm()
     * @generated
     * @ordered
     */
    protected EList realm = null;

    /**
     * The cached value of the '{@link #getDistinguishedName() <em>Distinguished Name</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDistinguishedName()
     * @generated
     * @ordered
     */
    protected EList distinguishedName = null;

    /**
     * The default value of the '{@link #getRoleName() <em>Role Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRoleName()
     * @generated
     * @ordered
     */
    protected static final String ROLE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRoleName() <em>Role Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRoleName()
     * @generated
     * @ordered
     */
    protected String roleName = ROLE_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected RoleTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return SecurityPackage.eINSTANCE.getRoleType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDescription() {
        if (description == null) {
            description = new EObjectContainmentEList(DescriptionType.class, this, SecurityPackage.ROLE_TYPE__DESCRIPTION);
        }
        return description;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getRealm() {
        if (realm == null) {
            realm = new EObjectContainmentEList(RealmType.class, this, SecurityPackage.ROLE_TYPE__REALM);
        }
        return realm;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDistinguishedName() {
        if (distinguishedName == null) {
            distinguishedName = new EObjectContainmentEList(DistinguishedNameType.class, this, SecurityPackage.ROLE_TYPE__DISTINGUISHED_NAME);
        }
        return distinguishedName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRoleName(String newRoleName) {
        String oldRoleName = roleName;
        roleName = newRoleName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.ROLE_TYPE__ROLE_NAME, oldRoleName, roleName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case SecurityPackage.ROLE_TYPE__DESCRIPTION:
                    return ((InternalEList)getDescription()).basicRemove(otherEnd, msgs);
                case SecurityPackage.ROLE_TYPE__REALM:
                    return ((InternalEList)getRealm()).basicRemove(otherEnd, msgs);
                case SecurityPackage.ROLE_TYPE__DISTINGUISHED_NAME:
                    return ((InternalEList)getDistinguishedName()).basicRemove(otherEnd, msgs);
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
            case SecurityPackage.ROLE_TYPE__DESCRIPTION:
                return getDescription();
            case SecurityPackage.ROLE_TYPE__REALM:
                return getRealm();
            case SecurityPackage.ROLE_TYPE__DISTINGUISHED_NAME:
                return getDistinguishedName();
            case SecurityPackage.ROLE_TYPE__ROLE_NAME:
                return getRoleName();
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
            case SecurityPackage.ROLE_TYPE__DESCRIPTION:
                getDescription().clear();
                getDescription().addAll((Collection)newValue);
                return;
            case SecurityPackage.ROLE_TYPE__REALM:
                getRealm().clear();
                getRealm().addAll((Collection)newValue);
                return;
            case SecurityPackage.ROLE_TYPE__DISTINGUISHED_NAME:
                getDistinguishedName().clear();
                getDistinguishedName().addAll((Collection)newValue);
                return;
            case SecurityPackage.ROLE_TYPE__ROLE_NAME:
                setRoleName((String)newValue);
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
            case SecurityPackage.ROLE_TYPE__DESCRIPTION:
                getDescription().clear();
                return;
            case SecurityPackage.ROLE_TYPE__REALM:
                getRealm().clear();
                return;
            case SecurityPackage.ROLE_TYPE__DISTINGUISHED_NAME:
                getDistinguishedName().clear();
                return;
            case SecurityPackage.ROLE_TYPE__ROLE_NAME:
                setRoleName(ROLE_NAME_EDEFAULT);
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
            case SecurityPackage.ROLE_TYPE__DESCRIPTION:
                return description != null && !description.isEmpty();
            case SecurityPackage.ROLE_TYPE__REALM:
                return realm != null && !realm.isEmpty();
            case SecurityPackage.ROLE_TYPE__DISTINGUISHED_NAME:
                return distinguishedName != null && !distinguishedName.isEmpty();
            case SecurityPackage.ROLE_TYPE__ROLE_NAME:
                return ROLE_NAME_EDEFAULT == null ? roleName != null : !ROLE_NAME_EDEFAULT.equals(roleName);
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
        result.append(" (roleName: ");
        result.append(roleName);
        result.append(')');
        return result.toString();
    }

} //RoleTypeImpl
