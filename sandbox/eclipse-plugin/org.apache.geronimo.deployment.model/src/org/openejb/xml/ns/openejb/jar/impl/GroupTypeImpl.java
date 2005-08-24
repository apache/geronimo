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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.CmrFieldType1;
import org.openejb.xml.ns.openejb.jar.GroupType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Group Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.GroupTypeImpl#getGroupName <em>Group Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.GroupTypeImpl#getCmpFieldName <em>Cmp Field Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.GroupTypeImpl#getCmrField <em>Cmr Field</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GroupTypeImpl extends EObjectImpl implements GroupType {
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
     * The cached value of the '{@link #getCmpFieldName() <em>Cmp Field Name</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldName()
     * @generated
     * @ordered
     */
    protected EList cmpFieldName = null;

    /**
     * The cached value of the '{@link #getCmrField() <em>Cmr Field</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmrField()
     * @generated
     * @ordered
     */
    protected EList cmrField = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GroupTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getGroupType();
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.GROUP_TYPE__GROUP_NAME, oldGroupName, groupName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getCmpFieldName() {
        if (cmpFieldName == null) {
            cmpFieldName = new EDataTypeEList(String.class, this, JarPackage.GROUP_TYPE__CMP_FIELD_NAME);
        }
        return cmpFieldName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getCmrField() {
        if (cmrField == null) {
            cmrField = new EObjectContainmentEList(CmrFieldType1.class, this, JarPackage.GROUP_TYPE__CMR_FIELD);
        }
        return cmrField;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.GROUP_TYPE__CMR_FIELD:
                    return ((InternalEList)getCmrField()).basicRemove(otherEnd, msgs);
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
            case JarPackage.GROUP_TYPE__GROUP_NAME:
                return getGroupName();
            case JarPackage.GROUP_TYPE__CMP_FIELD_NAME:
                return getCmpFieldName();
            case JarPackage.GROUP_TYPE__CMR_FIELD:
                return getCmrField();
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
            case JarPackage.GROUP_TYPE__GROUP_NAME:
                setGroupName((String)newValue);
                return;
            case JarPackage.GROUP_TYPE__CMP_FIELD_NAME:
                getCmpFieldName().clear();
                getCmpFieldName().addAll((Collection)newValue);
                return;
            case JarPackage.GROUP_TYPE__CMR_FIELD:
                getCmrField().clear();
                getCmrField().addAll((Collection)newValue);
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
            case JarPackage.GROUP_TYPE__GROUP_NAME:
                setGroupName(GROUP_NAME_EDEFAULT);
                return;
            case JarPackage.GROUP_TYPE__CMP_FIELD_NAME:
                getCmpFieldName().clear();
                return;
            case JarPackage.GROUP_TYPE__CMR_FIELD:
                getCmrField().clear();
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
            case JarPackage.GROUP_TYPE__GROUP_NAME:
                return GROUP_NAME_EDEFAULT == null ? groupName != null : !GROUP_NAME_EDEFAULT.equals(groupName);
            case JarPackage.GROUP_TYPE__CMP_FIELD_NAME:
                return cmpFieldName != null && !cmpFieldName.isEmpty();
            case JarPackage.GROUP_TYPE__CMR_FIELD:
                return cmrField != null && !cmrField.isEmpty();
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

} //GroupTypeImpl
