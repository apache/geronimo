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
import org.apache.geronimo.xml.ns.naming.PortType;
import org.apache.geronimo.xml.ns.naming.ServiceCompletionType;
import org.apache.geronimo.xml.ns.naming.ServiceRefType;

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
 * An implementation of the model object '<em><b>Service Ref Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.ServiceRefTypeImpl#getServiceRefName <em>Service Ref Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.ServiceRefTypeImpl#getServiceCompletion <em>Service Completion</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.ServiceRefTypeImpl#getPort <em>Port</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ServiceRefTypeImpl extends EObjectImpl implements ServiceRefType {
    /**
     * The default value of the '{@link #getServiceRefName() <em>Service Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceRefName()
     * @generated
     * @ordered
     */
    protected static final String SERVICE_REF_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getServiceRefName() <em>Service Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceRefName()
     * @generated
     * @ordered
     */
    protected String serviceRefName = SERVICE_REF_NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getServiceCompletion() <em>Service Completion</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceCompletion()
     * @generated
     * @ordered
     */
    protected ServiceCompletionType serviceCompletion = null;

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
    protected ServiceRefTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return NamingPackage.eINSTANCE.getServiceRefType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getServiceRefName() {
        return serviceRefName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setServiceRefName(String newServiceRefName) {
        String oldServiceRefName = serviceRefName;
        serviceRefName = newServiceRefName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.SERVICE_REF_TYPE__SERVICE_REF_NAME, oldServiceRefName, serviceRefName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ServiceCompletionType getServiceCompletion() {
        return serviceCompletion;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetServiceCompletion(ServiceCompletionType newServiceCompletion, NotificationChain msgs) {
        ServiceCompletionType oldServiceCompletion = serviceCompletion;
        serviceCompletion = newServiceCompletion;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION, oldServiceCompletion, newServiceCompletion);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setServiceCompletion(ServiceCompletionType newServiceCompletion) {
        if (newServiceCompletion != serviceCompletion) {
            NotificationChain msgs = null;
            if (serviceCompletion != null)
                msgs = ((InternalEObject)serviceCompletion).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION, null, msgs);
            if (newServiceCompletion != null)
                msgs = ((InternalEObject)newServiceCompletion).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION, null, msgs);
            msgs = basicSetServiceCompletion(newServiceCompletion, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION, newServiceCompletion, newServiceCompletion));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getPort() {
        if (port == null) {
            port = new EObjectContainmentEList(PortType.class, this, NamingPackage.SERVICE_REF_TYPE__PORT);
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
                case NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION:
                    return basicSetServiceCompletion(null, msgs);
                case NamingPackage.SERVICE_REF_TYPE__PORT:
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
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_REF_NAME:
                return getServiceRefName();
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION:
                return getServiceCompletion();
            case NamingPackage.SERVICE_REF_TYPE__PORT:
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
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_REF_NAME:
                setServiceRefName((String)newValue);
                return;
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION:
                setServiceCompletion((ServiceCompletionType)newValue);
                return;
            case NamingPackage.SERVICE_REF_TYPE__PORT:
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
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_REF_NAME:
                setServiceRefName(SERVICE_REF_NAME_EDEFAULT);
                return;
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION:
                setServiceCompletion((ServiceCompletionType)null);
                return;
            case NamingPackage.SERVICE_REF_TYPE__PORT:
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
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_REF_NAME:
                return SERVICE_REF_NAME_EDEFAULT == null ? serviceRefName != null : !SERVICE_REF_NAME_EDEFAULT.equals(serviceRefName);
            case NamingPackage.SERVICE_REF_TYPE__SERVICE_COMPLETION:
                return serviceCompletion != null;
            case NamingPackage.SERVICE_REF_TYPE__PORT:
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
        result.append(" (serviceRefName: ");
        result.append(serviceRefName);
        result.append(')');
        return result.toString();
    }

} //ServiceRefTypeImpl
