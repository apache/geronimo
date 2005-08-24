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
package org.openejb.xml.ns.openejb.jar.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType;
import org.openejb.xml.ns.openejb.jar.ActivationConfigType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Activation Config Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.ActivationConfigTypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.ActivationConfigTypeImpl#getActivationConfigProperty <em>Activation Config Property</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ActivationConfigTypeImpl extends EObjectImpl implements ActivationConfigType {
    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected EList description = null;

    /**
     * The cached value of the '{@link #getActivationConfigProperty() <em>Activation Config Property</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActivationConfigProperty()
     * @generated
     * @ordered
     */
    protected EList activationConfigProperty = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ActivationConfigTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getActivationConfigType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDescription() {
        if (description == null) {
            description = new EDataTypeEList(String.class, this, JarPackage.ACTIVATION_CONFIG_TYPE__DESCRIPTION);
        }
        return description;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getActivationConfigProperty() {
        if (activationConfigProperty == null) {
            activationConfigProperty = new EObjectContainmentEList(ActivationConfigPropertyType.class, this, JarPackage.ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY);
        }
        return activationConfigProperty;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY:
                    return ((InternalEList)getActivationConfigProperty()).basicRemove(otherEnd, msgs);
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
            case JarPackage.ACTIVATION_CONFIG_TYPE__DESCRIPTION:
                return getDescription();
            case JarPackage.ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY:
                return getActivationConfigProperty();
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
            case JarPackage.ACTIVATION_CONFIG_TYPE__DESCRIPTION:
                getDescription().clear();
                getDescription().addAll((Collection)newValue);
                return;
            case JarPackage.ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY:
                getActivationConfigProperty().clear();
                getActivationConfigProperty().addAll((Collection)newValue);
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
            case JarPackage.ACTIVATION_CONFIG_TYPE__DESCRIPTION:
                getDescription().clear();
                return;
            case JarPackage.ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY:
                getActivationConfigProperty().clear();
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
            case JarPackage.ACTIVATION_CONFIG_TYPE__DESCRIPTION:
                return description != null && !description.isEmpty();
            case JarPackage.ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY:
                return activationConfigProperty != null && !activationConfigProperty.isEmpty();
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
        result.append(" (description: ");
        result.append(description);
        result.append(')');
        return result.toString();
    }

} //ActivationConfigTypeImpl
