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

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.EnterpriseBeansType;
import org.openejb.xml.ns.openejb.jar.JarPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Enterprise Beans Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EnterpriseBeansTypeImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EnterpriseBeansTypeImpl#getSession <em>Session</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EnterpriseBeansTypeImpl#getEntity <em>Entity</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EnterpriseBeansTypeImpl#getMessageDriven <em>Message Driven</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class EnterpriseBeansTypeImpl extends EObjectImpl implements EnterpriseBeansType {
    /**
     * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap group = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EnterpriseBeansTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getEnterpriseBeansType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getGroup() {
        if (group == null) {
            group = new BasicFeatureMap(this, JarPackage.ENTERPRISE_BEANS_TYPE__GROUP);
        }
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getSession() {
        return ((FeatureMap)getGroup()).list(JarPackage.eINSTANCE.getEnterpriseBeansType_Session());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEntity() {
        return ((FeatureMap)getGroup()).list(JarPackage.eINSTANCE.getEnterpriseBeansType_Entity());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getMessageDriven() {
        return ((FeatureMap)getGroup()).list(JarPackage.eINSTANCE.getEnterpriseBeansType_MessageDriven());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.ENTERPRISE_BEANS_TYPE__GROUP:
                    return ((InternalEList)getGroup()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTERPRISE_BEANS_TYPE__SESSION:
                    return ((InternalEList)getSession()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTERPRISE_BEANS_TYPE__ENTITY:
                    return ((InternalEList)getEntity()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN:
                    return ((InternalEList)getMessageDriven()).basicRemove(otherEnd, msgs);
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
            case JarPackage.ENTERPRISE_BEANS_TYPE__GROUP:
                return getGroup();
            case JarPackage.ENTERPRISE_BEANS_TYPE__SESSION:
                return getSession();
            case JarPackage.ENTERPRISE_BEANS_TYPE__ENTITY:
                return getEntity();
            case JarPackage.ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN:
                return getMessageDriven();
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
            case JarPackage.ENTERPRISE_BEANS_TYPE__GROUP:
                getGroup().clear();
                getGroup().addAll((Collection)newValue);
                return;
            case JarPackage.ENTERPRISE_BEANS_TYPE__SESSION:
                getSession().clear();
                getSession().addAll((Collection)newValue);
                return;
            case JarPackage.ENTERPRISE_BEANS_TYPE__ENTITY:
                getEntity().clear();
                getEntity().addAll((Collection)newValue);
                return;
            case JarPackage.ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN:
                getMessageDriven().clear();
                getMessageDriven().addAll((Collection)newValue);
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
            case JarPackage.ENTERPRISE_BEANS_TYPE__GROUP:
                getGroup().clear();
                return;
            case JarPackage.ENTERPRISE_BEANS_TYPE__SESSION:
                getSession().clear();
                return;
            case JarPackage.ENTERPRISE_BEANS_TYPE__ENTITY:
                getEntity().clear();
                return;
            case JarPackage.ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN:
                getMessageDriven().clear();
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
            case JarPackage.ENTERPRISE_BEANS_TYPE__GROUP:
                return group != null && !group.isEmpty();
            case JarPackage.ENTERPRISE_BEANS_TYPE__SESSION:
                return !getSession().isEmpty();
            case JarPackage.ENTERPRISE_BEANS_TYPE__ENTITY:
                return !getEntity().isEmpty();
            case JarPackage.ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN:
                return !getMessageDriven().isEmpty();
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
        result.append(" (group: ");
        result.append(group);
        result.append(')');
        return result.toString();
    }

} //EnterpriseBeansTypeImpl
