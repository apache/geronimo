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

import org.openejb.xml.ns.openejb.jar.CmpFieldMappingType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cmp Field Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl#getCmpFieldName <em>Cmp Field Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl#getCmpFieldClass <em>Cmp Field Class</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl#getTableColumn <em>Table Column</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl#getSqlType <em>Sql Type</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl#getTypeConverter <em>Type Converter</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CmpFieldMappingTypeImpl extends EObjectImpl implements CmpFieldMappingType {
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
     * The default value of the '{@link #getCmpFieldClass() <em>Cmp Field Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldClass()
     * @generated
     * @ordered
     */
    protected static final String CMP_FIELD_CLASS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCmpFieldClass() <em>Cmp Field Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldClass()
     * @generated
     * @ordered
     */
    protected String cmpFieldClass = CMP_FIELD_CLASS_EDEFAULT;

    /**
     * The default value of the '{@link #getTableColumn() <em>Table Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableColumn()
     * @generated
     * @ordered
     */
    protected static final String TABLE_COLUMN_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTableColumn() <em>Table Column</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableColumn()
     * @generated
     * @ordered
     */
    protected String tableColumn = TABLE_COLUMN_EDEFAULT;

    /**
     * The default value of the '{@link #getSqlType() <em>Sql Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSqlType()
     * @generated
     * @ordered
     */
    protected static final String SQL_TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getSqlType() <em>Sql Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSqlType()
     * @generated
     * @ordered
     */
    protected String sqlType = SQL_TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getTypeConverter() <em>Type Converter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTypeConverter()
     * @generated
     * @ordered
     */
    protected static final String TYPE_CONVERTER_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTypeConverter() <em>Type Converter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTypeConverter()
     * @generated
     * @ordered
     */
    protected String typeConverter = TYPE_CONVERTER_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected CmpFieldMappingTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getCmpFieldMappingType();
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME, oldCmpFieldName, cmpFieldName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getCmpFieldClass() {
        return cmpFieldClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCmpFieldClass(String newCmpFieldClass) {
        String oldCmpFieldClass = cmpFieldClass;
        cmpFieldClass = newCmpFieldClass;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS, oldCmpFieldClass, cmpFieldClass));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTableColumn() {
        return tableColumn;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTableColumn(String newTableColumn) {
        String oldTableColumn = tableColumn;
        tableColumn = newTableColumn;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN, oldTableColumn, tableColumn));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getSqlType() {
        return sqlType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSqlType(String newSqlType) {
        String oldSqlType = sqlType;
        sqlType = newSqlType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_MAPPING_TYPE__SQL_TYPE, oldSqlType, sqlType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTypeConverter() {
        return typeConverter;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTypeConverter(String newTypeConverter) {
        String oldTypeConverter = typeConverter;
        typeConverter = newTypeConverter;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER, oldTypeConverter, typeConverter));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME:
                return getCmpFieldName();
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS:
                return getCmpFieldClass();
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN:
                return getTableColumn();
            case JarPackage.CMP_FIELD_MAPPING_TYPE__SQL_TYPE:
                return getSqlType();
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER:
                return getTypeConverter();
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
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME:
                setCmpFieldName((String)newValue);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS:
                setCmpFieldClass((String)newValue);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN:
                setTableColumn((String)newValue);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__SQL_TYPE:
                setSqlType((String)newValue);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER:
                setTypeConverter((String)newValue);
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
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME:
                setCmpFieldName(CMP_FIELD_NAME_EDEFAULT);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS:
                setCmpFieldClass(CMP_FIELD_CLASS_EDEFAULT);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN:
                setTableColumn(TABLE_COLUMN_EDEFAULT);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__SQL_TYPE:
                setSqlType(SQL_TYPE_EDEFAULT);
                return;
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER:
                setTypeConverter(TYPE_CONVERTER_EDEFAULT);
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
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME:
                return CMP_FIELD_NAME_EDEFAULT == null ? cmpFieldName != null : !CMP_FIELD_NAME_EDEFAULT.equals(cmpFieldName);
            case JarPackage.CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS:
                return CMP_FIELD_CLASS_EDEFAULT == null ? cmpFieldClass != null : !CMP_FIELD_CLASS_EDEFAULT.equals(cmpFieldClass);
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN:
                return TABLE_COLUMN_EDEFAULT == null ? tableColumn != null : !TABLE_COLUMN_EDEFAULT.equals(tableColumn);
            case JarPackage.CMP_FIELD_MAPPING_TYPE__SQL_TYPE:
                return SQL_TYPE_EDEFAULT == null ? sqlType != null : !SQL_TYPE_EDEFAULT.equals(sqlType);
            case JarPackage.CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER:
                return TYPE_CONVERTER_EDEFAULT == null ? typeConverter != null : !TYPE_CONVERTER_EDEFAULT.equals(typeConverter);
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
        result.append(" (cmpFieldName: ");
        result.append(cmpFieldName);
        result.append(", cmpFieldClass: ");
        result.append(cmpFieldClass);
        result.append(", tableColumn: ");
        result.append(tableColumn);
        result.append(", sqlType: ");
        result.append(sqlType);
        result.append(", typeConverter: ");
        result.append(typeConverter);
        result.append(')');
        return result.toString();
    }

} //CmpFieldMappingTypeImpl
