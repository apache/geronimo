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

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;

import org.openejb.xml.ns.pkgen.DatabaseGeneratedType;
import org.openejb.xml.ns.pkgen.PkgenPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Database Generated Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.DatabaseGeneratedTypeImpl#getIdentityColumn <em>Identity Column</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DatabaseGeneratedTypeImpl extends EObjectImpl implements DatabaseGeneratedType {
    /**
     * The cached value of the '{@link #getIdentityColumn() <em>Identity Column</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getIdentityColumn()
     * @generated
     * @ordered
     */
    protected EList identityColumn = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DatabaseGeneratedTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return PkgenPackage.eINSTANCE.getDatabaseGeneratedType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getIdentityColumn() {
        if (identityColumn == null) {
            identityColumn = new EDataTypeEList(String.class, this, PkgenPackage.DATABASE_GENERATED_TYPE__IDENTITY_COLUMN);
        }
        return identityColumn;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case PkgenPackage.DATABASE_GENERATED_TYPE__IDENTITY_COLUMN:
                return getIdentityColumn();
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
            case PkgenPackage.DATABASE_GENERATED_TYPE__IDENTITY_COLUMN:
                getIdentityColumn().clear();
                getIdentityColumn().addAll((Collection)newValue);
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
            case PkgenPackage.DATABASE_GENERATED_TYPE__IDENTITY_COLUMN:
                getIdentityColumn().clear();
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
            case PkgenPackage.DATABASE_GENERATED_TYPE__IDENTITY_COLUMN:
                return identityColumn != null && !identityColumn.isEmpty();
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
        result.append(" (identityColumn: ");
        result.append(identityColumn);
        result.append(')');
        return result.toString();
    }

} //DatabaseGeneratedTypeImpl
