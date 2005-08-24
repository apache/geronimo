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

import org.apache.geronimo.xml.ns.naming.NamingPackage;
import org.apache.geronimo.xml.ns.naming.PortCompletionType;
import org.apache.geronimo.xml.ns.naming.ServiceCompletionType;

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

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Service Completion Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.ServiceCompletionTypeImpl#getServiceName <em>Service Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.ServiceCompletionTypeImpl#getPort <em>Port</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ServiceCompletionTypeImpl extends EObjectImpl implements ServiceCompletionType {
    /**
     * The default value of the '{@link #getServiceName() <em>Service Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceName()
     * @generated
     * @ordered
     */
    protected static final String SERVICE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getServiceName() <em>Service Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceName()
     * @generated
     * @ordered
     */
    protected String serviceName = SERVICE_NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getPort() <em>Port</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPort()
     * @generated
     * @ordered
     */
    protected EList port = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ServiceCompletionTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return NamingPackage.eINSTANCE.getServiceCompletionType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setServiceName(String newServiceName) {
        String oldServiceName = serviceName;
        serviceName = newServiceName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.SERVICE_COMPLETION_TYPE__SERVICE_NAME, oldServiceName, serviceName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getPort() {
        if (port == null) {
            port = new EObjectContainmentEList(PortCompletionType.class, this, NamingPackage.SERVICE_COMPLETION_TYPE__PORT);
        }
        return port;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case NamingPackage.SERVICE_COMPLETION_TYPE__PORT:
                    return ((InternalEList)getPort()).basicRemove(otherEnd, msgs);
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
            case NamingPackage.SERVICE_COMPLETION_TYPE__SERVICE_NAME:
                return getServiceName();
            case NamingPackage.SERVICE_COMPLETION_TYPE__PORT:
                return getPort();
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
            case NamingPackage.SERVICE_COMPLETION_TYPE__SERVICE_NAME:
                setServiceName((String)newValue);
                return;
            case NamingPackage.SERVICE_COMPLETION_TYPE__PORT:
                getPort().clear();
                getPort().addAll((Collection)newValue);
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
            case NamingPackage.SERVICE_COMPLETION_TYPE__SERVICE_NAME:
                setServiceName(SERVICE_NAME_EDEFAULT);
                return;
            case NamingPackage.SERVICE_COMPLETION_TYPE__PORT:
                getPort().clear();
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
            case NamingPackage.SERVICE_COMPLETION_TYPE__SERVICE_NAME:
                return SERVICE_NAME_EDEFAULT == null ? serviceName != null : !SERVICE_NAME_EDEFAULT.equals(serviceName);
            case NamingPackage.SERVICE_COMPLETION_TYPE__PORT:
                return port != null && !port.isEmpty();
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
        result.append(" (serviceName: ");
        result.append(serviceName);
        result.append(')');
        return result.toString();
    }

} //ServiceCompletionTypeImpl
