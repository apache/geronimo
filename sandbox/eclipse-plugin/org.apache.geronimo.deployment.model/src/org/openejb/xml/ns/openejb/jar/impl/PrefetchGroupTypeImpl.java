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

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType;
import org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType;
import org.openejb.xml.ns.openejb.jar.EntityGroupMappingType;
import org.openejb.xml.ns.openejb.jar.GroupType;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.PrefetchGroupType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Prefetch Group Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.PrefetchGroupTypeImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.PrefetchGroupTypeImpl#getEntityGroupMapping <em>Entity Group Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.PrefetchGroupTypeImpl#getCmpFieldGroupMapping <em>Cmp Field Group Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.PrefetchGroupTypeImpl#getCmrFieldGroupMapping <em>Cmr Field Group Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PrefetchGroupTypeImpl extends EObjectImpl implements PrefetchGroupType {
    /**
     * The cached value of the '{@link #getGroup() <em>Group</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroup()
     * @generated
     * @ordered
     */
    protected EList group = null;

    /**
     * The cached value of the '{@link #getEntityGroupMapping() <em>Entity Group Mapping</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEntityGroupMapping()
     * @generated
     * @ordered
     */
    protected EntityGroupMappingType entityGroupMapping = null;

    /**
     * The cached value of the '{@link #getCmpFieldGroupMapping() <em>Cmp Field Group Mapping</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldGroupMapping()
     * @generated
     * @ordered
     */
    protected EList cmpFieldGroupMapping = null;

    /**
     * The cached value of the '{@link #getCmrFieldGroupMapping() <em>Cmr Field Group Mapping</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmrFieldGroupMapping()
     * @generated
     * @ordered
     */
    protected EList cmrFieldGroupMapping = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected PrefetchGroupTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getPrefetchGroupType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getGroup() {
        if (group == null) {
            group = new EObjectContainmentEList(GroupType.class, this, JarPackage.PREFETCH_GROUP_TYPE__GROUP);
        }
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EntityGroupMappingType getEntityGroupMapping() {
        return entityGroupMapping;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEntityGroupMapping(EntityGroupMappingType newEntityGroupMapping, NotificationChain msgs) {
        EntityGroupMappingType oldEntityGroupMapping = entityGroupMapping;
        entityGroupMapping = newEntityGroupMapping;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING, oldEntityGroupMapping, newEntityGroupMapping);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEntityGroupMapping(EntityGroupMappingType newEntityGroupMapping) {
        if (newEntityGroupMapping != entityGroupMapping) {
            NotificationChain msgs = null;
            if (entityGroupMapping != null)
                msgs = ((InternalEObject)entityGroupMapping).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING, null, msgs);
            if (newEntityGroupMapping != null)
                msgs = ((InternalEObject)newEntityGroupMapping).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING, null, msgs);
            msgs = basicSetEntityGroupMapping(newEntityGroupMapping, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING, newEntityGroupMapping, newEntityGroupMapping));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getCmpFieldGroupMapping() {
        if (cmpFieldGroupMapping == null) {
            cmpFieldGroupMapping = new EObjectContainmentEList(CmpFieldGroupMappingType.class, this, JarPackage.PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING);
        }
        return cmpFieldGroupMapping;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getCmrFieldGroupMapping() {
        if (cmrFieldGroupMapping == null) {
            cmrFieldGroupMapping = new EObjectContainmentEList(CmrFieldGroupMappingType.class, this, JarPackage.PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING);
        }
        return cmrFieldGroupMapping;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.PREFETCH_GROUP_TYPE__GROUP:
                    return ((InternalEList)getGroup()).basicRemove(otherEnd, msgs);
                case JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING:
                    return basicSetEntityGroupMapping(null, msgs);
                case JarPackage.PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING:
                    return ((InternalEList)getCmpFieldGroupMapping()).basicRemove(otherEnd, msgs);
                case JarPackage.PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING:
                    return ((InternalEList)getCmrFieldGroupMapping()).basicRemove(otherEnd, msgs);
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
            case JarPackage.PREFETCH_GROUP_TYPE__GROUP:
                return getGroup();
            case JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING:
                return getEntityGroupMapping();
            case JarPackage.PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING:
                return getCmpFieldGroupMapping();
            case JarPackage.PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING:
                return getCmrFieldGroupMapping();
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
            case JarPackage.PREFETCH_GROUP_TYPE__GROUP:
                getGroup().clear();
                getGroup().addAll((Collection)newValue);
                return;
            case JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING:
                setEntityGroupMapping((EntityGroupMappingType)newValue);
                return;
            case JarPackage.PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING:
                getCmpFieldGroupMapping().clear();
                getCmpFieldGroupMapping().addAll((Collection)newValue);
                return;
            case JarPackage.PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING:
                getCmrFieldGroupMapping().clear();
                getCmrFieldGroupMapping().addAll((Collection)newValue);
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
            case JarPackage.PREFETCH_GROUP_TYPE__GROUP:
                getGroup().clear();
                return;
            case JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING:
                setEntityGroupMapping((EntityGroupMappingType)null);
                return;
            case JarPackage.PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING:
                getCmpFieldGroupMapping().clear();
                return;
            case JarPackage.PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING:
                getCmrFieldGroupMapping().clear();
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
            case JarPackage.PREFETCH_GROUP_TYPE__GROUP:
                return group != null && !group.isEmpty();
            case JarPackage.PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING:
                return entityGroupMapping != null;
            case JarPackage.PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING:
                return cmpFieldGroupMapping != null && !cmpFieldGroupMapping.isEmpty();
            case JarPackage.PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING:
                return cmrFieldGroupMapping != null && !cmrFieldGroupMapping.isEmpty();
        }
        return eDynamicIsSet(eFeature);
    }

} //PrefetchGroupTypeImpl
