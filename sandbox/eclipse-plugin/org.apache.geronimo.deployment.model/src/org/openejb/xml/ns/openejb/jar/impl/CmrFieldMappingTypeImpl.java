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

import org.openejb.xml.ns.openejb.jar.CmrFieldMappingType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cmr Field Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldMappingTypeImpl#getKeyColumn <em>Key Column</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldMappingTypeImpl#getForeignKeyColumn <em>Foreign Key Column</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CmrFieldMappingTypeImpl extends EObjectImpl implements CmrFieldMappingType {
    /**
     * The default value of the '{@link #getKeyColumn() <em>Key Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getKeyColumn()
     * @generated
     * @ordered
     */
    protected static final String KEY_COLUMN_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getKeyColumn() <em>Key Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getKeyColumn()
     * @generated
     * @ordered
     */
    protected String keyColumn = KEY_COLUMN_EDEFAULT;

    /**
     * The default value of the '{@link #getForeignKeyColumn() <em>Foreign Key Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getForeignKeyColumn()
     * @generated
     * @ordered
     */
    protected static final String FOREIGN_KEY_COLUMN_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getForeignKeyColumn() <em>Foreign Key Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getForeignKeyColumn()
     * @generated
     * @ordered
     */
    protected String foreignKeyColumn = FOREIGN_KEY_COLUMN_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected CmrFieldMappingTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getCmrFieldMappingType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getKeyColumn() {
        return keyColumn;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setKeyColumn(String newKeyColumn) {
        String oldKeyColumn = keyColumn;
        keyColumn = newKeyColumn;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMR_FIELD_MAPPING_TYPE__KEY_COLUMN, oldKeyColumn, keyColumn));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setForeignKeyColumn(String newForeignKeyColumn) {
        String oldForeignKeyColumn = foreignKeyColumn;
        foreignKeyColumn = newForeignKeyColumn;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN, oldForeignKeyColumn, foreignKeyColumn));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.CMR_FIELD_MAPPING_TYPE__KEY_COLUMN:
                return getKeyColumn();
            case JarPackage.CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN:
                return getForeignKeyColumn();
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
            case JarPackage.CMR_FIELD_MAPPING_TYPE__KEY_COLUMN:
                setKeyColumn((String)newValue);
                return;
            case JarPackage.CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN:
                setForeignKeyColumn((String)newValue);
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
            case JarPackage.CMR_FIELD_MAPPING_TYPE__KEY_COLUMN:
                setKeyColumn(KEY_COLUMN_EDEFAULT);
                return;
            case JarPackage.CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN:
                setForeignKeyColumn(FOREIGN_KEY_COLUMN_EDEFAULT);
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
            case JarPackage.CMR_FIELD_MAPPING_TYPE__KEY_COLUMN:
                return KEY_COLUMN_EDEFAULT == null ? keyColumn != null : !KEY_COLUMN_EDEFAULT.equals(keyColumn);
            case JarPackage.CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN:
                return FOREIGN_KEY_COLUMN_EDEFAULT == null ? foreignKeyColumn != null : !FOREIGN_KEY_COLUMN_EDEFAULT.equals(foreignKeyColumn);
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
        result.append(" (keyColumn: ");
        result.append(keyColumn);
        result.append(", foreignKeyColumn: ");
        result.append(foreignKeyColumn);
        result.append(')');
        return result.toString();
    }

} //CmrFieldMappingTypeImpl
