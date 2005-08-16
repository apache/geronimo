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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openejb.xml.ns.pkgen.AutoIncrementTableType;
import org.openejb.xml.ns.pkgen.CustomGeneratorType;
import org.openejb.xml.ns.pkgen.KeyGeneratorType;
import org.openejb.xml.ns.pkgen.PkgenPackage;
import org.openejb.xml.ns.pkgen.SequenceTableType;
import org.openejb.xml.ns.pkgen.SqlGeneratorType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Key Generator Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.KeyGeneratorTypeImpl#getSequenceTable <em>Sequence Table</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.KeyGeneratorTypeImpl#getAutoIncrementTable <em>Auto Increment Table</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.KeyGeneratorTypeImpl#getSqlGenerator <em>Sql Generator</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.impl.KeyGeneratorTypeImpl#getCustomGenerator <em>Custom Generator</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class KeyGeneratorTypeImpl extends EObjectImpl implements KeyGeneratorType {
    /**
     * The cached value of the '{@link #getSequenceTable() <em>Sequence Table</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSequenceTable()
     * @generated
     * @ordered
     */
    protected SequenceTableType sequenceTable = null;

    /**
     * The cached value of the '{@link #getAutoIncrementTable() <em>Auto Increment Table</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAutoIncrementTable()
     * @generated
     * @ordered
     */
    protected AutoIncrementTableType autoIncrementTable = null;

    /**
     * The cached value of the '{@link #getSqlGenerator() <em>Sql Generator</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSqlGenerator()
     * @generated
     * @ordered
     */
    protected SqlGeneratorType sqlGenerator = null;

    /**
     * The cached value of the '{@link #getCustomGenerator() <em>Custom Generator</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCustomGenerator()
     * @generated
     * @ordered
     */
    protected CustomGeneratorType customGenerator = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected KeyGeneratorTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return PkgenPackage.eINSTANCE.getKeyGeneratorType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SequenceTableType getSequenceTable() {
        return sequenceTable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetSequenceTable(SequenceTableType newSequenceTable, NotificationChain msgs) {
        SequenceTableType oldSequenceTable = sequenceTable;
        sequenceTable = newSequenceTable;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE, oldSequenceTable, newSequenceTable);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSequenceTable(SequenceTableType newSequenceTable) {
        if (newSequenceTable != sequenceTable) {
            NotificationChain msgs = null;
            if (sequenceTable != null)
                msgs = ((InternalEObject)sequenceTable).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE, null, msgs);
            if (newSequenceTable != null)
                msgs = ((InternalEObject)newSequenceTable).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE, null, msgs);
            msgs = basicSetSequenceTable(newSequenceTable, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE, newSequenceTable, newSequenceTable));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public AutoIncrementTableType getAutoIncrementTable() {
        return autoIncrementTable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetAutoIncrementTable(AutoIncrementTableType newAutoIncrementTable, NotificationChain msgs) {
        AutoIncrementTableType oldAutoIncrementTable = autoIncrementTable;
        autoIncrementTable = newAutoIncrementTable;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE, oldAutoIncrementTable, newAutoIncrementTable);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAutoIncrementTable(AutoIncrementTableType newAutoIncrementTable) {
        if (newAutoIncrementTable != autoIncrementTable) {
            NotificationChain msgs = null;
            if (autoIncrementTable != null)
                msgs = ((InternalEObject)autoIncrementTable).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE, null, msgs);
            if (newAutoIncrementTable != null)
                msgs = ((InternalEObject)newAutoIncrementTable).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE, null, msgs);
            msgs = basicSetAutoIncrementTable(newAutoIncrementTable, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE, newAutoIncrementTable, newAutoIncrementTable));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SqlGeneratorType getSqlGenerator() {
        return sqlGenerator;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetSqlGenerator(SqlGeneratorType newSqlGenerator, NotificationChain msgs) {
        SqlGeneratorType oldSqlGenerator = sqlGenerator;
        sqlGenerator = newSqlGenerator;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR, oldSqlGenerator, newSqlGenerator);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSqlGenerator(SqlGeneratorType newSqlGenerator) {
        if (newSqlGenerator != sqlGenerator) {
            NotificationChain msgs = null;
            if (sqlGenerator != null)
                msgs = ((InternalEObject)sqlGenerator).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR, null, msgs);
            if (newSqlGenerator != null)
                msgs = ((InternalEObject)newSqlGenerator).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR, null, msgs);
            msgs = basicSetSqlGenerator(newSqlGenerator, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR, newSqlGenerator, newSqlGenerator));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CustomGeneratorType getCustomGenerator() {
        return customGenerator;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetCustomGenerator(CustomGeneratorType newCustomGenerator, NotificationChain msgs) {
        CustomGeneratorType oldCustomGenerator = customGenerator;
        customGenerator = newCustomGenerator;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR, oldCustomGenerator, newCustomGenerator);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCustomGenerator(CustomGeneratorType newCustomGenerator) {
        if (newCustomGenerator != customGenerator) {
            NotificationChain msgs = null;
            if (customGenerator != null)
                msgs = ((InternalEObject)customGenerator).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR, null, msgs);
            if (newCustomGenerator != null)
                msgs = ((InternalEObject)newCustomGenerator).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR, null, msgs);
            msgs = basicSetCustomGenerator(newCustomGenerator, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR, newCustomGenerator, newCustomGenerator));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE:
                    return basicSetSequenceTable(null, msgs);
                case PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE:
                    return basicSetAutoIncrementTable(null, msgs);
                case PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR:
                    return basicSetSqlGenerator(null, msgs);
                case PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR:
                    return basicSetCustomGenerator(null, msgs);
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
            case PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE:
                return getSequenceTable();
            case PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE:
                return getAutoIncrementTable();
            case PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR:
                return getSqlGenerator();
            case PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR:
                return getCustomGenerator();
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
            case PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE:
                setSequenceTable((SequenceTableType)newValue);
                return;
            case PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE:
                setAutoIncrementTable((AutoIncrementTableType)newValue);
                return;
            case PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR:
                setSqlGenerator((SqlGeneratorType)newValue);
                return;
            case PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR:
                setCustomGenerator((CustomGeneratorType)newValue);
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
            case PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE:
                setSequenceTable((SequenceTableType)null);
                return;
            case PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE:
                setAutoIncrementTable((AutoIncrementTableType)null);
                return;
            case PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR:
                setSqlGenerator((SqlGeneratorType)null);
                return;
            case PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR:
                setCustomGenerator((CustomGeneratorType)null);
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
            case PkgenPackage.KEY_GENERATOR_TYPE__SEQUENCE_TABLE:
                return sequenceTable != null;
            case PkgenPackage.KEY_GENERATOR_TYPE__AUTO_INCREMENT_TABLE:
                return autoIncrementTable != null;
            case PkgenPackage.KEY_GENERATOR_TYPE__SQL_GENERATOR:
                return sqlGenerator != null;
            case PkgenPackage.KEY_GENERATOR_TYPE__CUSTOM_GENERATOR:
                return customGenerator != null;
        }
        return eDynamicIsSet(eFeature);
    }

} //KeyGeneratorTypeImpl
