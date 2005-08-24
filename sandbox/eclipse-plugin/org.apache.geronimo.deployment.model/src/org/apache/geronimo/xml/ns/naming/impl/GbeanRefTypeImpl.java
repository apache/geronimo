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
package org.apache.geronimo.xml.ns.naming.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.naming.GbeanRefType;
import org.apache.geronimo.xml.ns.naming.NamingPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Gbean Ref Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getRefName <em>Ref Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getRefType <em>Ref Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getProxyType <em>Proxy Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getServer <em>Server</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getApplication <em>Application</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl#getTargetName <em>Target Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GbeanRefTypeImpl extends EObjectImpl implements GbeanRefType {
    /**
     * The default value of the '{@link #getRefName() <em>Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRefName()
     * @generated
     * @ordered
     */
    protected static final String REF_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRefName() <em>Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRefName()
     * @generated
     * @ordered
     */
    protected String refName = REF_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getRefType() <em>Ref Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRefType()
     * @generated
     * @ordered
     */
    protected static final String REF_TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRefType() <em>Ref Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRefType()
     * @generated
     * @ordered
     */
    protected String refType = REF_TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getProxyType() <em>Proxy Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getProxyType()
     * @generated
     * @ordered
     */
    protected static final String PROXY_TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getProxyType() <em>Proxy Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getProxyType()
     * @generated
     * @ordered
     */
    protected String proxyType = PROXY_TYPE_EDEFAULT;

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
    protected GbeanRefTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return NamingPackage.eINSTANCE.getGbeanRefType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getRefName() {
        return refName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRefName(String newRefName) {
        String oldRefName = refName;
        refName = newRefName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.GBEAN_REF_TYPE__REF_NAME, oldRefName, refName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getRefType() {
        return refType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRefType(String newRefType) {
        String oldRefType = refType;
        refType = newRefType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.GBEAN_REF_TYPE__REF_TYPE, oldRefType, refType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getProxyType() {
        return proxyType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setProxyType(String newProxyType) {
        String oldProxyType = proxyType;
        proxyType = newProxyType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.GBEAN_REF_TYPE__PROXY_TYPE, oldProxyType, proxyType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getGroup() {
        if (group == null) {
            group = new BasicFeatureMap(this, NamingPackage.GBEAN_REF_TYPE__GROUP);
        }
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDomain() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_Domain());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getServer() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_Server());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getApplication() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_Application());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getModule() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_Module());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getType() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_Type());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getName() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_Name());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getTargetName() {
        return ((FeatureMap)getGroup()).list(NamingPackage.eINSTANCE.getGbeanRefType_TargetName());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case NamingPackage.GBEAN_REF_TYPE__GROUP:
                    return ((InternalEList)getGroup()).basicRemove(otherEnd, msgs);
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
            case NamingPackage.GBEAN_REF_TYPE__REF_NAME:
                return getRefName();
            case NamingPackage.GBEAN_REF_TYPE__REF_TYPE:
                return getRefType();
            case NamingPackage.GBEAN_REF_TYPE__PROXY_TYPE:
                return getProxyType();
            case NamingPackage.GBEAN_REF_TYPE__GROUP:
                return getGroup();
            case NamingPackage.GBEAN_REF_TYPE__DOMAIN:
                return getDomain();
            case NamingPackage.GBEAN_REF_TYPE__SERVER:
                return getServer();
            case NamingPackage.GBEAN_REF_TYPE__APPLICATION:
                return getApplication();
            case NamingPackage.GBEAN_REF_TYPE__MODULE:
                return getModule();
            case NamingPackage.GBEAN_REF_TYPE__TYPE:
                return getType();
            case NamingPackage.GBEAN_REF_TYPE__NAME:
                return getName();
            case NamingPackage.GBEAN_REF_TYPE__TARGET_NAME:
                return getTargetName();
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
            case NamingPackage.GBEAN_REF_TYPE__REF_NAME:
                setRefName((String)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__REF_TYPE:
                setRefType((String)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__PROXY_TYPE:
                setProxyType((String)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__GROUP:
                getGroup().clear();
                getGroup().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__DOMAIN:
                getDomain().clear();
                getDomain().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__SERVER:
                getServer().clear();
                getServer().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__APPLICATION:
                getApplication().clear();
                getApplication().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__MODULE:
                getModule().clear();
                getModule().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__TYPE:
                getType().clear();
                getType().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__NAME:
                getName().clear();
                getName().addAll((Collection)newValue);
                return;
            case NamingPackage.GBEAN_REF_TYPE__TARGET_NAME:
                getTargetName().clear();
                getTargetName().addAll((Collection)newValue);
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
            case NamingPackage.GBEAN_REF_TYPE__REF_NAME:
                setRefName(REF_NAME_EDEFAULT);
                return;
            case NamingPackage.GBEAN_REF_TYPE__REF_TYPE:
                setRefType(REF_TYPE_EDEFAULT);
                return;
            case NamingPackage.GBEAN_REF_TYPE__PROXY_TYPE:
                setProxyType(PROXY_TYPE_EDEFAULT);
                return;
            case NamingPackage.GBEAN_REF_TYPE__GROUP:
                getGroup().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__DOMAIN:
                getDomain().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__SERVER:
                getServer().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__APPLICATION:
                getApplication().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__MODULE:
                getModule().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__TYPE:
                getType().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__NAME:
                getName().clear();
                return;
            case NamingPackage.GBEAN_REF_TYPE__TARGET_NAME:
                getTargetName().clear();
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
            case NamingPackage.GBEAN_REF_TYPE__REF_NAME:
                return REF_NAME_EDEFAULT == null ? refName != null : !REF_NAME_EDEFAULT.equals(refName);
            case NamingPackage.GBEAN_REF_TYPE__REF_TYPE:
                return REF_TYPE_EDEFAULT == null ? refType != null : !REF_TYPE_EDEFAULT.equals(refType);
            case NamingPackage.GBEAN_REF_TYPE__PROXY_TYPE:
                return PROXY_TYPE_EDEFAULT == null ? proxyType != null : !PROXY_TYPE_EDEFAULT.equals(proxyType);
            case NamingPackage.GBEAN_REF_TYPE__GROUP:
                return group != null && !group.isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__DOMAIN:
                return !getDomain().isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__SERVER:
                return !getServer().isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__APPLICATION:
                return !getApplication().isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__MODULE:
                return !getModule().isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__TYPE:
                return !getType().isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__NAME:
                return !getName().isEmpty();
            case NamingPackage.GBEAN_REF_TYPE__TARGET_NAME:
                return !getTargetName().isEmpty();
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
        result.append(" (refName: ");
        result.append(refName);
        result.append(", refType: ");
        result.append(refType);
        result.append(", proxyType: ");
        result.append(proxyType);
        result.append(", group: ");
        result.append(group);
        result.append(')');
        return result.toString();
    }

} //GbeanRefTypeImpl
