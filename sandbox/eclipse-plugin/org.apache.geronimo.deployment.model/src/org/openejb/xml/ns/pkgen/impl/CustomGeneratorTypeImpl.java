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
package org.openejb.xml.ns.pkgen.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openejb.xml.ns.pkgen.CustomGeneratorType;
import org.openejb.xml.ns.pkgen.PkgenPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Custom Generator Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.CustomGeneratorTypeImpl#getGeneratorName <em>Generator Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.CustomGeneratorTypeImpl#getPrimaryKeyClass <em>Primary Key Class</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CustomGeneratorTypeImpl extends EObjectImpl implements CustomGeneratorType {
    /**
     * The default value of the '{@link #getGeneratorName() <em>Generator Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGeneratorName()
     * @generated
     * @ordered
     */
    protected static final String GENERATOR_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getGeneratorName() <em>Generator Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGeneratorName()
     * @generated
     * @ordered
     */
    protected String generatorName = GENERATOR_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getPrimaryKeyClass() <em>Primary Key Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPrimaryKeyClass()
     * @generated
     * @ordered
     */
    protected static final String PRIMARY_KEY_CLASS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPrimaryKeyClass() <em>Primary Key Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPrimaryKeyClass()
     * @generated
     * @ordered
     */
    protected String primaryKeyClass = PRIMARY_KEY_CLASS_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected CustomGeneratorTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return PkgenPackage.eINSTANCE.getCustomGeneratorType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getGeneratorName() {
        return generatorName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGeneratorName(String newGeneratorName) {
        String oldGeneratorName = generatorName;
        generatorName = newGeneratorName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.CUSTOM_GENERATOR_TYPE__GENERATOR_NAME, oldGeneratorName, generatorName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPrimaryKeyClass(String newPrimaryKeyClass) {
        String oldPrimaryKeyClass = primaryKeyClass;
        primaryKeyClass = newPrimaryKeyClass;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS, oldPrimaryKeyClass, primaryKeyClass));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__GENERATOR_NAME:
                return getGeneratorName();
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS:
                return getPrimaryKeyClass();
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
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__GENERATOR_NAME:
                setGeneratorName((String)newValue);
                return;
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS:
                setPrimaryKeyClass((String)newValue);
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
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__GENERATOR_NAME:
                setGeneratorName(GENERATOR_NAME_EDEFAULT);
                return;
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS:
                setPrimaryKeyClass(PRIMARY_KEY_CLASS_EDEFAULT);
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
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__GENERATOR_NAME:
                return GENERATOR_NAME_EDEFAULT == null ? generatorName != null : !GENERATOR_NAME_EDEFAULT.equals(generatorName);
            case PkgenPackage.CUSTOM_GENERATOR_TYPE__PRIMARY_KEY_CLASS:
                return PRIMARY_KEY_CLASS_EDEFAULT == null ? primaryKeyClass != null : !PRIMARY_KEY_CLASS_EDEFAULT.equals(primaryKeyClass);
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
        result.append(" (generatorName: ");
        result.append(generatorName);
        result.append(", primaryKeyClass: ");
        result.append(primaryKeyClass);
        result.append(')');
        return result.toString();
    }

} //CustomGeneratorTypeImpl
