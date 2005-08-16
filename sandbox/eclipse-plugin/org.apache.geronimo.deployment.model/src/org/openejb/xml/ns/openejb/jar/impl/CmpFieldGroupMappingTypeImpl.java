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

import org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cmp Field Group Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldGroupMappingTypeImpl#getGroupName <em>Group Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldGroupMappingTypeImpl#getCmpFieldName <em>Cmp Field Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CmpFieldGroupMappingTypeImpl extends EObjectImpl implements CmpFieldGroupMappingType {
    /**
     * The default value of the '{@link #getGroupName() <em>Group Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupName()
     * @generated
     * @ordered
     */
    protected static final String GROUP_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getGroupName() <em>Group Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupName()
     * @generated
     * @ordered
     */
    protected String groupName = GROUP_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getCmpFieldName() <em>Cmp Field Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldName()
     * @generated
     * @ordered
     */
    protected static final String CMP_FIELD_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCmpFieldName() <em>Cmp Field Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldName()
     * @generated
     * @ordered
     */
    protected String cmpFieldName = CMP_FIELD_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected CmpFieldGroupMappingTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getCmpFieldGroupMappingType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGroupName(String newGroupName) {
        String oldGroupName = groupName;
        groupName = newGroupName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME, oldGroupName, groupName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getCmpFieldName() {
        return cmpFieldName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCmpFieldName(String newCmpFieldName) {
        String oldCmpFieldName = cmpFieldName;
        cmpFieldName = newCmpFieldName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME, oldCmpFieldName, cmpFieldName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME:
                return getGroupName();
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME:
                return getCmpFieldName();
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
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME:
                setGroupName((String)newValue);
                return;
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME:
                setCmpFieldName((String)newValue);
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
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME:
                setGroupName(GROUP_NAME_EDEFAULT);
                return;
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME:
                setCmpFieldName(CMP_FIELD_NAME_EDEFAULT);
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
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME:
                return GROUP_NAME_EDEFAULT == null ? groupName != null : !GROUP_NAME_EDEFAULT.equals(groupName);
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME:
                return CMP_FIELD_NAME_EDEFAULT == null ? cmpFieldName != null : !CMP_FIELD_NAME_EDEFAULT.equals(cmpFieldName);
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
        result.append(" (groupName: ");
        result.append(groupName);
        result.append(", cmpFieldName: ");
        result.append(cmpFieldName);
        result.append(')');
        return result.toString();
    }

} //CmpFieldGroupMappingTypeImpl
