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

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Activation Config Property Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.ActivationConfigPropertyTypeImpl#getActivationConfigPropertyName <em>Activation Config Property Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.ActivationConfigPropertyTypeImpl#getActivationConfigPropertyValue <em>Activation Config Property Value</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ActivationConfigPropertyTypeImpl extends EObjectImpl implements ActivationConfigPropertyType {
    /**
     * The default value of the '{@link #getActivationConfigPropertyName() <em>Activation Config Property Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActivationConfigPropertyName()
     * @generated
     * @ordered
     */
    protected static final String ACTIVATION_CONFIG_PROPERTY_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getActivationConfigPropertyName() <em>Activation Config Property Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActivationConfigPropertyName()
     * @generated
     * @ordered
     */
    protected String activationConfigPropertyName = ACTIVATION_CONFIG_PROPERTY_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getActivationConfigPropertyValue() <em>Activation Config Property Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActivationConfigPropertyValue()
     * @generated
     * @ordered
     */
    protected static final String ACTIVATION_CONFIG_PROPERTY_VALUE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getActivationConfigPropertyValue() <em>Activation Config Property Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActivationConfigPropertyValue()
     * @generated
     * @ordered
     */
    protected String activationConfigPropertyValue = ACTIVATION_CONFIG_PROPERTY_VALUE_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ActivationConfigPropertyTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getActivationConfigPropertyType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getActivationConfigPropertyName() {
        return activationConfigPropertyName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivationConfigPropertyName(String newActivationConfigPropertyName) {
        String oldActivationConfigPropertyName = activationConfigPropertyName;
        activationConfigPropertyName = newActivationConfigPropertyName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME, oldActivationConfigPropertyName, activationConfigPropertyName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getActivationConfigPropertyValue() {
        return activationConfigPropertyValue;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivationConfigPropertyValue(String newActivationConfigPropertyValue) {
        String oldActivationConfigPropertyValue = activationConfigPropertyValue;
        activationConfigPropertyValue = newActivationConfigPropertyValue;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE, oldActivationConfigPropertyValue, activationConfigPropertyValue));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME:
                return getActivationConfigPropertyName();
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE:
                return getActivationConfigPropertyValue();
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
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME:
                setActivationConfigPropertyName((String)newValue);
                return;
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE:
                setActivationConfigPropertyValue((String)newValue);
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
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME:
                setActivationConfigPropertyName(ACTIVATION_CONFIG_PROPERTY_NAME_EDEFAULT);
                return;
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE:
                setActivationConfigPropertyValue(ACTIVATION_CONFIG_PROPERTY_VALUE_EDEFAULT);
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
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME:
                return ACTIVATION_CONFIG_PROPERTY_NAME_EDEFAULT == null ? activationConfigPropertyName != null : !ACTIVATION_CONFIG_PROPERTY_NAME_EDEFAULT.equals(activationConfigPropertyName);
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE:
                return ACTIVATION_CONFIG_PROPERTY_VALUE_EDEFAULT == null ? activationConfigPropertyValue != null : !ACTIVATION_CONFIG_PROPERTY_VALUE_EDEFAULT.equals(activationConfigPropertyValue);
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
        result.append(" (activationConfigPropertyName: ");
        result.append(activationConfigPropertyName);
        result.append(", activationConfigPropertyValue: ");
        result.append(activationConfigPropertyValue);
        result.append(')');
        return result.toString();
    }

} //ActivationConfigPropertyTypeImpl
