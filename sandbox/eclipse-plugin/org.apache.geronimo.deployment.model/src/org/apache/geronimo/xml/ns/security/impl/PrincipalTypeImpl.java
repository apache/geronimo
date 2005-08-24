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
 * An implementation of the model object '<em><b>Principal Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.PrincipalTypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.PrincipalTypeImpl#getClass_ <em>Class</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.PrincipalTypeImpl#isDesignatedRunAs <em>Designated Run As</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.PrincipalTypeImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PrincipalTypeImpl extends EObjectImpl implements PrincipalType {
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
     * The default value of the '{@link #getClass_() <em>Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getClass_()
     * @generated
     * @ordered
     */
    protected static final String CLASS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getClass_() <em>Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getClass_()
     * @generated
     * @ordered
     */
    protected String class_ = CLASS_EDEFAULT;

    /**
     * The default value of the '{@link #isDesignatedRunAs() <em>Designated Run As</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isDesignatedRunAs()
     * @generated
     * @ordered
     */
    protected static final boolean DESIGNATED_RUN_AS_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isDesignatedRunAs() <em>Designated Run As</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isDesignatedRunAs()
     * @generated
     * @ordered
     */
    protected boolean designatedRunAs = DESIGNATED_RUN_AS_EDEFAULT;

    /**
     * This is true if the Designated Run As attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean designatedRunAsESet = false;

    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected PrincipalTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return SecurityPackage.eINSTANCE.getPrincipalType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDescription() {
        if (description == null) {
            description = new EObjectContainmentEList(DescriptionType.class, this, SecurityPackage.PRINCIPAL_TYPE__DESCRIPTION);
        }
        return description;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getClass_() {
        return class_;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setClass(String newClass) {
        String oldClass = class_;
        class_ = newClass;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.PRINCIPAL_TYPE__CLASS, oldClass, class_));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isDesignatedRunAs() {
        return designatedRunAs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDesignatedRunAs(boolean newDesignatedRunAs) {
        boolean oldDesignatedRunAs = designatedRunAs;
        designatedRunAs = newDesignatedRunAs;
        boolean oldDesignatedRunAsESet = designatedRunAsESet;
        designatedRunAsESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.PRINCIPAL_TYPE__DESIGNATED_RUN_AS, oldDesignatedRunAs, designatedRunAs, !oldDesignatedRunAsESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetDesignatedRunAs() {
        boolean oldDesignatedRunAs = designatedRunAs;
        boolean oldDesignatedRunAsESet = designatedRunAsESet;
        designatedRunAs = DESIGNATED_RUN_AS_EDEFAULT;
        designatedRunAsESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, SecurityPackage.PRINCIPAL_TYPE__DESIGNATED_RUN_AS, oldDesignatedRunAs, DESIGNATED_RUN_AS_EDEFAULT, oldDesignatedRunAsESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetDesignatedRunAs() {
        return designatedRunAsESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.PRINCIPAL_TYPE__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case SecurityPackage.PRINCIPAL_TYPE__DESCRIPTION:
                    return ((InternalEList)getDescription()).basicRemove(otherEnd, msgs);
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
            case SecurityPackage.PRINCIPAL_TYPE__DESCRIPTION:
                return getDescription();
            case SecurityPackage.PRINCIPAL_TYPE__CLASS:
                return getClass_();
            case SecurityPackage.PRINCIPAL_TYPE__DESIGNATED_RUN_AS:
                return isDesignatedRunAs() ? Boolean.TRUE : Boolean.FALSE;
            case SecurityPackage.PRINCIPAL_TYPE__NAME:
                return getName();
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
            case SecurityPackage.PRINCIPAL_TYPE__DESCRIPTION:
                getDescription().clear();
                getDescription().addAll((Collection)newValue);
                return;
            case SecurityPackage.PRINCIPAL_TYPE__CLASS:
                setClass((String)newValue);
                return;
            case SecurityPackage.PRINCIPAL_TYPE__DESIGNATED_RUN_AS:
                setDesignatedRunAs(((Boolean)newValue).booleanValue());
                return;
            case SecurityPackage.PRINCIPAL_TYPE__NAME:
                setName((String)newValue);
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
            case SecurityPackage.PRINCIPAL_TYPE__DESCRIPTION:
                getDescription().clear();
                return;
            case SecurityPackage.PRINCIPAL_TYPE__CLASS:
                setClass(CLASS_EDEFAULT);
                return;
            case SecurityPackage.PRINCIPAL_TYPE__DESIGNATED_RUN_AS:
                unsetDesignatedRunAs();
                return;
            case SecurityPackage.PRINCIPAL_TYPE__NAME:
                setName(NAME_EDEFAULT);
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
            case SecurityPackage.PRINCIPAL_TYPE__DESCRIPTION:
                return description != null && !description.isEmpty();
            case SecurityPackage.PRINCIPAL_TYPE__CLASS:
                return CLASS_EDEFAULT == null ? class_ != null : !CLASS_EDEFAULT.equals(class_);
            case SecurityPackage.PRINCIPAL_TYPE__DESIGNATED_RUN_AS:
                return isSetDesignatedRunAs();
            case SecurityPackage.PRINCIPAL_TYPE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
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
        result.append(" (class: ");
        result.append(class_);
        result.append(", designatedRunAs: ");
        if (designatedRunAsESet) result.append(designatedRunAs); else result.append("<unset>");
        result.append(", name: ");
        result.append(name);
        result.append(')');
        return result.toString();
    }

} //PrincipalTypeImpl
