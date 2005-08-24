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

import org.openejb.xml.ns.openejb.jar.CmrFieldType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cmr Field Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldTypeImpl#getCmrFieldName <em>Cmr Field Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CmrFieldTypeImpl extends EObjectImpl implements CmrFieldType {
    /**
     * The default value of the '{@link #getCmrFieldName() <em>Cmr Field Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmrFieldName()
     * @generated
     * @ordered
     */
    protected static final String CMR_FIELD_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCmrFieldName() <em>Cmr Field Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmrFieldName()
     * @generated
     * @ordered
     */
    protected String cmrFieldName = CMR_FIELD_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected CmrFieldTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getCmrFieldType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getCmrFieldName() {
        return cmrFieldName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCmrFieldName(String newCmrFieldName) {
        String oldCmrFieldName = cmrFieldName;
        cmrFieldName = newCmrFieldName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMR_FIELD_TYPE__CMR_FIELD_NAME, oldCmrFieldName, cmrFieldName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.CMR_FIELD_TYPE__CMR_FIELD_NAME:
                return getCmrFieldName();
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
            case JarPackage.CMR_FIELD_TYPE__CMR_FIELD_NAME:
                setCmrFieldName((String)newValue);
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
            case JarPackage.CMR_FIELD_TYPE__CMR_FIELD_NAME:
                setCmrFieldName(CMR_FIELD_NAME_EDEFAULT);
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
            case JarPackage.CMR_FIELD_TYPE__CMR_FIELD_NAME:
                return CMR_FIELD_NAME_EDEFAULT == null ? cmrFieldName != null : !CMR_FIELD_NAME_EDEFAULT.equals(cmrFieldName);
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
        result.append(" (cmrFieldName: ");
        result.append(cmrFieldName);
        result.append(')');
        return result.toString();
    }

} //CmrFieldTypeImpl
