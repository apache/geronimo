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

import org.openejb.xml.ns.pkgen.PkgenPackage;
import org.openejb.xml.ns.pkgen.SqlGeneratorType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sql Generator Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.SqlGeneratorTypeImpl#getSql <em>Sql</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.SqlGeneratorTypeImpl#getReturnType <em>Return Type</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SqlGeneratorTypeImpl extends EObjectImpl implements SqlGeneratorType {
    /**
     * The default value of the '{@link #getSql() <em>Sql</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSql()
     * @generated
     * @ordered
     */
    protected static final String SQL_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getSql() <em>Sql</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSql()
     * @generated
     * @ordered
     */
    protected String sql = SQL_EDEFAULT;

    /**
     * The default value of the '{@link #getReturnType() <em>Return Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReturnType()
     * @generated
     * @ordered
     */
    protected static final String RETURN_TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getReturnType() <em>Return Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getReturnType()
     * @generated
     * @ordered
     */
    protected String returnType = RETURN_TYPE_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SqlGeneratorTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return PkgenPackage.eINSTANCE.getSqlGeneratorType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getSql() {
        return sql;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSql(String newSql) {
        String oldSql = sql;
        sql = newSql;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.SQL_GENERATOR_TYPE__SQL, oldSql, sql));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setReturnType(String newReturnType) {
        String oldReturnType = returnType;
        returnType = newReturnType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.SQL_GENERATOR_TYPE__RETURN_TYPE, oldReturnType, returnType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case PkgenPackage.SQL_GENERATOR_TYPE__SQL:
                return getSql();
            case PkgenPackage.SQL_GENERATOR_TYPE__RETURN_TYPE:
                return getReturnType();
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
            case PkgenPackage.SQL_GENERATOR_TYPE__SQL:
                setSql((String)newValue);
                return;
            case PkgenPackage.SQL_GENERATOR_TYPE__RETURN_TYPE:
                setReturnType((String)newValue);
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
            case PkgenPackage.SQL_GENERATOR_TYPE__SQL:
                setSql(SQL_EDEFAULT);
                return;
            case PkgenPackage.SQL_GENERATOR_TYPE__RETURN_TYPE:
                setReturnType(RETURN_TYPE_EDEFAULT);
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
            case PkgenPackage.SQL_GENERATOR_TYPE__SQL:
                return SQL_EDEFAULT == null ? sql != null : !SQL_EDEFAULT.equals(sql);
            case PkgenPackage.SQL_GENERATOR_TYPE__RETURN_TYPE:
                return RETURN_TYPE_EDEFAULT == null ? returnType != null : !RETURN_TYPE_EDEFAULT.equals(returnType);
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
        result.append(" (sql: ");
        result.append(sql);
        result.append(", returnType: ");
        result.append(returnType);
        result.append(')');
        return result.toString();
    }

} //SqlGeneratorTypeImpl
