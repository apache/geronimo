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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openejb.xml.ns.openejb.jar.CmrFieldType;
import org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType;
import org.openejb.xml.ns.openejb.jar.RoleMappingType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Ejb Relationship Role Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl#getEjbRelationshipRoleName <em>Ejb Relationship Role Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl#getRelationshipRoleSource <em>Relationship Role Source</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl#getCmrField <em>Cmr Field</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl#getForeignKeyColumnOnSource <em>Foreign Key Column On Source</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl#getRoleMapping <em>Role Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class EjbRelationshipRoleTypeImpl extends EObjectImpl implements EjbRelationshipRoleType {
    /**
     * The default value of the '{@link #getEjbRelationshipRoleName() <em>Ejb Relationship Role Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbRelationshipRoleName()
     * @generated
     * @ordered
     */
    protected static final String EJB_RELATIONSHIP_ROLE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEjbRelationshipRoleName() <em>Ejb Relationship Role Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbRelationshipRoleName()
     * @generated
     * @ordered
     */
    protected String ejbRelationshipRoleName = EJB_RELATIONSHIP_ROLE_NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getRelationshipRoleSource() <em>Relationship Role Source</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRelationshipRoleSource()
     * @generated
     * @ordered
     */
    protected RelationshipRoleSourceType relationshipRoleSource = null;

    /**
     * The cached value of the '{@link #getCmrField() <em>Cmr Field</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmrField()
     * @generated
     * @ordered
     */
    protected CmrFieldType cmrField = null;

    /**
     * The cached value of the '{@link #getForeignKeyColumnOnSource() <em>Foreign Key Column On Source</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getForeignKeyColumnOnSource()
     * @generated
     * @ordered
     */
    protected EObject foreignKeyColumnOnSource = null;

    /**
     * The cached value of the '{@link #getRoleMapping() <em>Role Mapping</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRoleMapping()
     * @generated
     * @ordered
     */
    protected RoleMappingType roleMapping = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EjbRelationshipRoleTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getEjbRelationshipRoleType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getEjbRelationshipRoleName() {
        return ejbRelationshipRoleName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjbRelationshipRoleName(String newEjbRelationshipRoleName) {
        String oldEjbRelationshipRoleName = ejbRelationshipRoleName;
        ejbRelationshipRoleName = newEjbRelationshipRoleName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME, oldEjbRelationshipRoleName, ejbRelationshipRoleName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RelationshipRoleSourceType getRelationshipRoleSource() {
        return relationshipRoleSource;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetRelationshipRoleSource(RelationshipRoleSourceType newRelationshipRoleSource, NotificationChain msgs) {
        RelationshipRoleSourceType oldRelationshipRoleSource = relationshipRoleSource;
        relationshipRoleSource = newRelationshipRoleSource;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE, oldRelationshipRoleSource, newRelationshipRoleSource);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRelationshipRoleSource(RelationshipRoleSourceType newRelationshipRoleSource) {
        if (newRelationshipRoleSource != relationshipRoleSource) {
            NotificationChain msgs = null;
            if (relationshipRoleSource != null)
                msgs = ((InternalEObject)relationshipRoleSource).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE, null, msgs);
            if (newRelationshipRoleSource != null)
                msgs = ((InternalEObject)newRelationshipRoleSource).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE, null, msgs);
            msgs = basicSetRelationshipRoleSource(newRelationshipRoleSource, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE, newRelationshipRoleSource, newRelationshipRoleSource));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmrFieldType getCmrField() {
        return cmrField;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetCmrField(CmrFieldType newCmrField, NotificationChain msgs) {
        CmrFieldType oldCmrField = cmrField;
        cmrField = newCmrField;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD, oldCmrField, newCmrField);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCmrField(CmrFieldType newCmrField) {
        if (newCmrField != cmrField) {
            NotificationChain msgs = null;
            if (cmrField != null)
                msgs = ((InternalEObject)cmrField).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD, null, msgs);
            if (newCmrField != null)
                msgs = ((InternalEObject)newCmrField).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD, null, msgs);
            msgs = basicSetCmrField(newCmrField, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD, newCmrField, newCmrField));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getForeignKeyColumnOnSource() {
        return foreignKeyColumnOnSource;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetForeignKeyColumnOnSource(EObject newForeignKeyColumnOnSource, NotificationChain msgs) {
        EObject oldForeignKeyColumnOnSource = foreignKeyColumnOnSource;
        foreignKeyColumnOnSource = newForeignKeyColumnOnSource;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE, oldForeignKeyColumnOnSource, newForeignKeyColumnOnSource);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setForeignKeyColumnOnSource(EObject newForeignKeyColumnOnSource) {
        if (newForeignKeyColumnOnSource != foreignKeyColumnOnSource) {
            NotificationChain msgs = null;
            if (foreignKeyColumnOnSource != null)
                msgs = ((InternalEObject)foreignKeyColumnOnSource).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE, null, msgs);
            if (newForeignKeyColumnOnSource != null)
                msgs = ((InternalEObject)newForeignKeyColumnOnSource).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE, null, msgs);
            msgs = basicSetForeignKeyColumnOnSource(newForeignKeyColumnOnSource, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE, newForeignKeyColumnOnSource, newForeignKeyColumnOnSource));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RoleMappingType getRoleMapping() {
        return roleMapping;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetRoleMapping(RoleMappingType newRoleMapping, NotificationChain msgs) {
        RoleMappingType oldRoleMapping = roleMapping;
        roleMapping = newRoleMapping;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING, oldRoleMapping, newRoleMapping);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRoleMapping(RoleMappingType newRoleMapping) {
        if (newRoleMapping != roleMapping) {
            NotificationChain msgs = null;
            if (roleMapping != null)
                msgs = ((InternalEObject)roleMapping).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING, null, msgs);
            if (newRoleMapping != null)
                msgs = ((InternalEObject)newRoleMapping).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING, null, msgs);
            msgs = basicSetRoleMapping(newRoleMapping, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING, newRoleMapping, newRoleMapping));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE:
                    return basicSetRelationshipRoleSource(null, msgs);
                case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD:
                    return basicSetCmrField(null, msgs);
                case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE:
                    return basicSetForeignKeyColumnOnSource(null, msgs);
                case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING:
                    return basicSetRoleMapping(null, msgs);
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
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME:
                return getEjbRelationshipRoleName();
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE:
                return getRelationshipRoleSource();
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD:
                return getCmrField();
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE:
                return getForeignKeyColumnOnSource();
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING:
                return getRoleMapping();
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
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME:
                setEjbRelationshipRoleName((String)newValue);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE:
                setRelationshipRoleSource((RelationshipRoleSourceType)newValue);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD:
                setCmrField((CmrFieldType)newValue);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE:
                setForeignKeyColumnOnSource((EObject)newValue);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING:
                setRoleMapping((RoleMappingType)newValue);
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
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME:
                setEjbRelationshipRoleName(EJB_RELATIONSHIP_ROLE_NAME_EDEFAULT);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE:
                setRelationshipRoleSource((RelationshipRoleSourceType)null);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD:
                setCmrField((CmrFieldType)null);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE:
                setForeignKeyColumnOnSource((EObject)null);
                return;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING:
                setRoleMapping((RoleMappingType)null);
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
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME:
                return EJB_RELATIONSHIP_ROLE_NAME_EDEFAULT == null ? ejbRelationshipRoleName != null : !EJB_RELATIONSHIP_ROLE_NAME_EDEFAULT.equals(ejbRelationshipRoleName);
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE:
                return relationshipRoleSource != null;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD:
                return cmrField != null;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE:
                return foreignKeyColumnOnSource != null;
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING:
                return roleMapping != null;
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
        result.append(" (ejbRelationshipRoleName: ");
        result.append(ejbRelationshipRoleName);
        result.append(')');
        return result.toString();
    }

} //EjbRelationshipRoleTypeImpl
