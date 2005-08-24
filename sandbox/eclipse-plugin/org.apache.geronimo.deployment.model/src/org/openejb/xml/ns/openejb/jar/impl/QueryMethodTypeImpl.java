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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.MethodParamsType;
import org.openejb.xml.ns.openejb.jar.QueryMethodType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Query Method Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryMethodTypeImpl#getMethodName <em>Method Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryMethodTypeImpl#getMethodParams <em>Method Params</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class QueryMethodTypeImpl extends EObjectImpl implements QueryMethodType {
    /**
     * The default value of the '{@link #getMethodName() <em>Method Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMethodName()
     * @generated
     * @ordered
     */
    protected static final String METHOD_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getMethodName() <em>Method Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMethodName()
     * @generated
     * @ordered
     */
    protected String methodName = METHOD_NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getMethodParams() <em>Method Params</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMethodParams()
     * @generated
     * @ordered
     */
    protected MethodParamsType methodParams = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected QueryMethodTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getQueryMethodType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMethodName(String newMethodName) {
        String oldMethodName = methodName;
        methodName = newMethodName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_METHOD_TYPE__METHOD_NAME, oldMethodName, methodName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MethodParamsType getMethodParams() {
        return methodParams;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetMethodParams(MethodParamsType newMethodParams, NotificationChain msgs) {
        MethodParamsType oldMethodParams = methodParams;
        methodParams = newMethodParams;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS, oldMethodParams, newMethodParams);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMethodParams(MethodParamsType newMethodParams) {
        if (newMethodParams != methodParams) {
            NotificationChain msgs = null;
            if (methodParams != null)
                msgs = ((InternalEObject)methodParams).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS, null, msgs);
            if (newMethodParams != null)
                msgs = ((InternalEObject)newMethodParams).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS, null, msgs);
            msgs = basicSetMethodParams(newMethodParams, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS, newMethodParams, newMethodParams));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS:
                    return basicSetMethodParams(null, msgs);
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
            case JarPackage.QUERY_METHOD_TYPE__METHOD_NAME:
                return getMethodName();
            case JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS:
                return getMethodParams();
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
            case JarPackage.QUERY_METHOD_TYPE__METHOD_NAME:
                setMethodName((String)newValue);
                return;
            case JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS:
                setMethodParams((MethodParamsType)newValue);
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
            case JarPackage.QUERY_METHOD_TYPE__METHOD_NAME:
                setMethodName(METHOD_NAME_EDEFAULT);
                return;
            case JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS:
                setMethodParams((MethodParamsType)null);
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
            case JarPackage.QUERY_METHOD_TYPE__METHOD_NAME:
                return METHOD_NAME_EDEFAULT == null ? methodName != null : !METHOD_NAME_EDEFAULT.equals(methodName);
            case JarPackage.QUERY_METHOD_TYPE__METHOD_PARAMS:
                return methodParams != null;
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
        result.append(" (methodName: ");
        result.append(methodName);
        result.append(')');
        return result.toString();
    }

} //QueryMethodTypeImpl
