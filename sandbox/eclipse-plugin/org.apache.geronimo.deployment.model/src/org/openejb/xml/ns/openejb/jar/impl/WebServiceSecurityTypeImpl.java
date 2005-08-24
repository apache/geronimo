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

import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.TransportGuaranteeType;
import org.openejb.xml.ns.openejb.jar.WebServiceSecurityType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Web Service Security Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.WebServiceSecurityTypeImpl#getSecurityRealmName <em>Security Realm Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.WebServiceSecurityTypeImpl#getRealmName <em>Realm Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.WebServiceSecurityTypeImpl#getTransportGuarantee <em>Transport Guarantee</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.WebServiceSecurityTypeImpl#getAuthMethod <em>Auth Method</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WebServiceSecurityTypeImpl extends EObjectImpl implements WebServiceSecurityType {
    /**
     * The default value of the '{@link #getSecurityRealmName() <em>Security Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSecurityRealmName()
     * @generated
     * @ordered
     */
    protected static final String SECURITY_REALM_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getSecurityRealmName() <em>Security Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSecurityRealmName()
     * @generated
     * @ordered
     */
    protected String securityRealmName = SECURITY_REALM_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getRealmName() <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRealmName()
     * @generated
     * @ordered
     */
    protected static final String REALM_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRealmName() <em>Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRealmName()
     * @generated
     * @ordered
     */
    protected String realmName = REALM_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getTransportGuarantee() <em>Transport Guarantee</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTransportGuarantee()
     * @generated
     * @ordered
     */
    protected static final TransportGuaranteeType TRANSPORT_GUARANTEE_EDEFAULT = TransportGuaranteeType.NONE_LITERAL;

    /**
     * The cached value of the '{@link #getTransportGuarantee() <em>Transport Guarantee</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTransportGuarantee()
     * @generated
     * @ordered
     */
    protected TransportGuaranteeType transportGuarantee = TRANSPORT_GUARANTEE_EDEFAULT;

    /**
     * This is true if the Transport Guarantee attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean transportGuaranteeESet = false;

    /**
     * The default value of the '{@link #getAuthMethod() <em>Auth Method</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAuthMethod()
     * @generated
     * @ordered
     */
    protected static final String AUTH_METHOD_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getAuthMethod() <em>Auth Method</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAuthMethod()
     * @generated
     * @ordered
     */
    protected String authMethod = AUTH_METHOD_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected WebServiceSecurityTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getWebServiceSecurityType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getSecurityRealmName() {
        return securityRealmName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSecurityRealmName(String newSecurityRealmName) {
        String oldSecurityRealmName = securityRealmName;
        securityRealmName = newSecurityRealmName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME, oldSecurityRealmName, securityRealmName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getRealmName() {
        return realmName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRealmName(String newRealmName) {
        String oldRealmName = realmName;
        realmName = newRealmName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.WEB_SERVICE_SECURITY_TYPE__REALM_NAME, oldRealmName, realmName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TransportGuaranteeType getTransportGuarantee() {
        return transportGuarantee;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTransportGuarantee(TransportGuaranteeType newTransportGuarantee) {
        TransportGuaranteeType oldTransportGuarantee = transportGuarantee;
        transportGuarantee = newTransportGuarantee == null ? TRANSPORT_GUARANTEE_EDEFAULT : newTransportGuarantee;
        boolean oldTransportGuaranteeESet = transportGuaranteeESet;
        transportGuaranteeESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE, oldTransportGuarantee, transportGuarantee, !oldTransportGuaranteeESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetTransportGuarantee() {
        TransportGuaranteeType oldTransportGuarantee = transportGuarantee;
        boolean oldTransportGuaranteeESet = transportGuaranteeESet;
        transportGuarantee = TRANSPORT_GUARANTEE_EDEFAULT;
        transportGuaranteeESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, JarPackage.WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE, oldTransportGuarantee, TRANSPORT_GUARANTEE_EDEFAULT, oldTransportGuaranteeESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetTransportGuarantee() {
        return transportGuaranteeESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAuthMethod(String newAuthMethod) {
        String oldAuthMethod = authMethod;
        authMethod = newAuthMethod;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD, oldAuthMethod, authMethod));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME:
                return getSecurityRealmName();
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__REALM_NAME:
                return getRealmName();
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE:
                return getTransportGuarantee();
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD:
                return getAuthMethod();
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
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME:
                setSecurityRealmName((String)newValue);
                return;
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__REALM_NAME:
                setRealmName((String)newValue);
                return;
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE:
                setTransportGuarantee((TransportGuaranteeType)newValue);
                return;
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD:
                setAuthMethod((String)newValue);
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
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME:
                setSecurityRealmName(SECURITY_REALM_NAME_EDEFAULT);
                return;
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__REALM_NAME:
                setRealmName(REALM_NAME_EDEFAULT);
                return;
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE:
                unsetTransportGuarantee();
                return;
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD:
                setAuthMethod(AUTH_METHOD_EDEFAULT);
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
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME:
                return SECURITY_REALM_NAME_EDEFAULT == null ? securityRealmName != null : !SECURITY_REALM_NAME_EDEFAULT.equals(securityRealmName);
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__REALM_NAME:
                return REALM_NAME_EDEFAULT == null ? realmName != null : !REALM_NAME_EDEFAULT.equals(realmName);
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE:
                return isSetTransportGuarantee();
            case JarPackage.WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD:
                return AUTH_METHOD_EDEFAULT == null ? authMethod != null : !AUTH_METHOD_EDEFAULT.equals(authMethod);
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
        result.append(" (securityRealmName: ");
        result.append(securityRealmName);
        result.append(", realmName: ");
        result.append(realmName);
        result.append(", transportGuarantee: ");
        if (transportGuaranteeESet) result.append(transportGuarantee); else result.append("<unset>");
        result.append(", authMethod: ");
        result.append(authMethod);
        result.append(')');
        return result.toString();
    }

} //WebServiceSecurityTypeImpl
