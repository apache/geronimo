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

import org.apache.geronimo.xml.ns.naming.NamingPackage;
import org.apache.geronimo.xml.ns.naming.PortCompletionType;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Port Completion Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.PortCompletionTypeImpl#getBindingName <em>Binding Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PortCompletionTypeImpl extends PortTypeImpl implements PortCompletionType {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * The default value of the '{@link #getBindingName() <em>Binding Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getBindingName()
     * @generated
     * @ordered
     */
    protected static final String BINDING_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getBindingName() <em>Binding Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getBindingName()
     * @generated
     * @ordered
     */
    protected String bindingName = BINDING_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected PortCompletionTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return NamingPackage.eINSTANCE.getPortCompletionType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getBindingName() {
        return bindingName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setBindingName(String newBindingName) {
        String oldBindingName = bindingName;
        bindingName = newBindingName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, NamingPackage.PORT_COMPLETION_TYPE__BINDING_NAME, oldBindingName, bindingName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case NamingPackage.PORT_COMPLETION_TYPE__PORT_NAME:
                return getPortName();
            case NamingPackage.PORT_COMPLETION_TYPE__PROTOCOL:
                return getProtocol();
            case NamingPackage.PORT_COMPLETION_TYPE__HOST:
                return getHost();
            case NamingPackage.PORT_COMPLETION_TYPE__PORT:
                return new Integer(getPort());
            case NamingPackage.PORT_COMPLETION_TYPE__URI:
                return getUri();
            case NamingPackage.PORT_COMPLETION_TYPE__CREDENTIALS_NAME:
                return getCredentialsName();
            case NamingPackage.PORT_COMPLETION_TYPE__BINDING_NAME:
                return getBindingName();
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
            case NamingPackage.PORT_COMPLETION_TYPE__PORT_NAME:
                setPortName((String)newValue);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__PROTOCOL:
                setProtocol((String)newValue);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__HOST:
                setHost((String)newValue);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__PORT:
                setPort(((Integer)newValue).intValue());
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__URI:
                setUri((String)newValue);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__CREDENTIALS_NAME:
                setCredentialsName((String)newValue);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__BINDING_NAME:
                setBindingName((String)newValue);
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
            case NamingPackage.PORT_COMPLETION_TYPE__PORT_NAME:
                setPortName(PORT_NAME_EDEFAULT);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__PROTOCOL:
                setProtocol(PROTOCOL_EDEFAULT);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__HOST:
                setHost(HOST_EDEFAULT);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__PORT:
                unsetPort();
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__URI:
                setUri(URI_EDEFAULT);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__CREDENTIALS_NAME:
                setCredentialsName(CREDENTIALS_NAME_EDEFAULT);
                return;
            case NamingPackage.PORT_COMPLETION_TYPE__BINDING_NAME:
                setBindingName(BINDING_NAME_EDEFAULT);
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
            case NamingPackage.PORT_COMPLETION_TYPE__PORT_NAME:
                return PORT_NAME_EDEFAULT == null ? portName != null : !PORT_NAME_EDEFAULT.equals(portName);
            case NamingPackage.PORT_COMPLETION_TYPE__PROTOCOL:
                return PROTOCOL_EDEFAULT == null ? protocol != null : !PROTOCOL_EDEFAULT.equals(protocol);
            case NamingPackage.PORT_COMPLETION_TYPE__HOST:
                return HOST_EDEFAULT == null ? host != null : !HOST_EDEFAULT.equals(host);
            case NamingPackage.PORT_COMPLETION_TYPE__PORT:
                return isSetPort();
            case NamingPackage.PORT_COMPLETION_TYPE__URI:
                return URI_EDEFAULT == null ? uri != null : !URI_EDEFAULT.equals(uri);
            case NamingPackage.PORT_COMPLETION_TYPE__CREDENTIALS_NAME:
                return CREDENTIALS_NAME_EDEFAULT == null ? credentialsName != null : !CREDENTIALS_NAME_EDEFAULT.equals(credentialsName);
            case NamingPackage.PORT_COMPLETION_TYPE__BINDING_NAME:
                return BINDING_NAME_EDEFAULT == null ? bindingName != null : !BINDING_NAME_EDEFAULT.equals(bindingName);
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
        result.append(" (bindingName: ");
        result.append(bindingName);
        result.append(')');
        return result.toString();
    }

} //PortCompletionTypeImpl
