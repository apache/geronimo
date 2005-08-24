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
import org.openejb.xml.ns.pkgen.SequenceTableType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Sequence Table Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.SequenceTableTypeImpl#getTableName <em>Table Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.SequenceTableTypeImpl#getSequenceName <em>Sequence Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.SequenceTableTypeImpl#getBatchSize <em>Batch Size</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SequenceTableTypeImpl extends EObjectImpl implements SequenceTableType {
    /**
     * The default value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableName()
     * @generated
     * @ordered
     */
    protected static final String TABLE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableName()
     * @generated
     * @ordered
     */
    protected String tableName = TABLE_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getSequenceName() <em>Sequence Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSequenceName()
     * @generated
     * @ordered
     */
    protected static final String SEQUENCE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getSequenceName() <em>Sequence Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSequenceName()
     * @generated
     * @ordered
     */
    protected String sequenceName = SEQUENCE_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getBatchSize() <em>Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getBatchSize()
     * @generated
     * @ordered
     */
    protected static final int BATCH_SIZE_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getBatchSize() <em>Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getBatchSize()
     * @generated
     * @ordered
     */
    protected int batchSize = BATCH_SIZE_EDEFAULT;

    /**
     * This is true if the Batch Size attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean batchSizeESet = false;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SequenceTableTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return PkgenPackage.eINSTANCE.getSequenceTableType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTableName(String newTableName) {
        String oldTableName = tableName;
        tableName = newTableName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.SEQUENCE_TABLE_TYPE__TABLE_NAME, oldTableName, tableName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getSequenceName() {
        return sequenceName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSequenceName(String newSequenceName) {
        String oldSequenceName = sequenceName;
        sequenceName = newSequenceName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.SEQUENCE_TABLE_TYPE__SEQUENCE_NAME, oldSequenceName, sequenceName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setBatchSize(int newBatchSize) {
        int oldBatchSize = batchSize;
        batchSize = newBatchSize;
        boolean oldBatchSizeESet = batchSizeESet;
        batchSizeESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.SEQUENCE_TABLE_TYPE__BATCH_SIZE, oldBatchSize, batchSize, !oldBatchSizeESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetBatchSize() {
        int oldBatchSize = batchSize;
        boolean oldBatchSizeESet = batchSizeESet;
        batchSize = BATCH_SIZE_EDEFAULT;
        batchSizeESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, PkgenPackage.SEQUENCE_TABLE_TYPE__BATCH_SIZE, oldBatchSize, BATCH_SIZE_EDEFAULT, oldBatchSizeESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetBatchSize() {
        return batchSizeESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case PkgenPackage.SEQUENCE_TABLE_TYPE__TABLE_NAME:
                return getTableName();
            case PkgenPackage.SEQUENCE_TABLE_TYPE__SEQUENCE_NAME:
                return getSequenceName();
            case PkgenPackage.SEQUENCE_TABLE_TYPE__BATCH_SIZE:
                return new Integer(getBatchSize());
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
            case PkgenPackage.SEQUENCE_TABLE_TYPE__TABLE_NAME:
                setTableName((String)newValue);
                return;
            case PkgenPackage.SEQUENCE_TABLE_TYPE__SEQUENCE_NAME:
                setSequenceName((String)newValue);
                return;
            case PkgenPackage.SEQUENCE_TABLE_TYPE__BATCH_SIZE:
                setBatchSize(((Integer)newValue).intValue());
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
            case PkgenPackage.SEQUENCE_TABLE_TYPE__TABLE_NAME:
                setTableName(TABLE_NAME_EDEFAULT);
                return;
            case PkgenPackage.SEQUENCE_TABLE_TYPE__SEQUENCE_NAME:
                setSequenceName(SEQUENCE_NAME_EDEFAULT);
                return;
            case PkgenPackage.SEQUENCE_TABLE_TYPE__BATCH_SIZE:
                unsetBatchSize();
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
            case PkgenPackage.SEQUENCE_TABLE_TYPE__TABLE_NAME:
                return TABLE_NAME_EDEFAULT == null ? tableName != null : !TABLE_NAME_EDEFAULT.equals(tableName);
            case PkgenPackage.SEQUENCE_TABLE_TYPE__SEQUENCE_NAME:
                return SEQUENCE_NAME_EDEFAULT == null ? sequenceName != null : !SEQUENCE_NAME_EDEFAULT.equals(sequenceName);
            case PkgenPackage.SEQUENCE_TABLE_TYPE__BATCH_SIZE:
                return isSetBatchSize();
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
        result.append(" (tableName: ");
        result.append(tableName);
        result.append(", sequenceName: ");
        result.append(sequenceName);
        result.append(", batchSize: ");
        if (batchSizeESet) result.append(batchSize); else result.append("<unset>");
        result.append(')');
        return result.toString();
    }

} //SequenceTableTypeImpl
