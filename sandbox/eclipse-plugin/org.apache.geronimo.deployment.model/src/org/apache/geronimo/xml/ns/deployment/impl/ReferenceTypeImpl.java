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
package org.apache.geronimo.xml.ns.deployment.impl;

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;
import org.apache.geronimo.xml.ns.deployment.ReferenceType;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reference Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ReferenceTypeImpl#getName1 <em>Name1</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ReferenceTypeImpl extends PatternTypeImpl implements ReferenceType {
    /**
     * The default value of the '{@link #getName1() <em>Name1</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName1()
     * @generated
     * @ordered
     */
    protected static final String NAME1_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName1() <em>Name1</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName1()
     * @generated
     * @ordered
     */
    protected String name1 = NAME1_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ReferenceTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return DeploymentPackage.eINSTANCE.getReferenceType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getName1() {
        return name1;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setName1(String newName1) {
        String oldName1 = name1;
        name1 = newName1;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.REFERENCE_TYPE__NAME1, oldName1, name1));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case DeploymentPackage.REFERENCE_TYPE__DOMAIN:
                return getDomain();
            case DeploymentPackage.REFERENCE_TYPE__SERVER:
                return getServer();
            case DeploymentPackage.REFERENCE_TYPE__APPLICATION:
                return getApplication();
            case DeploymentPackage.REFERENCE_TYPE__MODULE_TYPE:
                return getModuleType();
            case DeploymentPackage.REFERENCE_TYPE__MODULE:
                return getModule();
            case DeploymentPackage.REFERENCE_TYPE__TYPE:
                return getType();
            case DeploymentPackage.REFERENCE_TYPE__NAME:
                return getName();
            case DeploymentPackage.REFERENCE_TYPE__GBEAN_NAME:
                return getGbeanName();
            case DeploymentPackage.REFERENCE_TYPE__NAME1:
                return getName1();
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
            case DeploymentPackage.REFERENCE_TYPE__DOMAIN:
                setDomain((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__SERVER:
                setServer((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__APPLICATION:
                setApplication((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__MODULE_TYPE:
                setModuleType((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__MODULE:
                setModule((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__TYPE:
                setType((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__NAME:
                setName((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__GBEAN_NAME:
                setGbeanName((String)newValue);
                return;
            case DeploymentPackage.REFERENCE_TYPE__NAME1:
                setName1((String)newValue);
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
            case DeploymentPackage.REFERENCE_TYPE__DOMAIN:
                setDomain(DOMAIN_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__SERVER:
                setServer(SERVER_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__APPLICATION:
                setApplication(APPLICATION_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__MODULE_TYPE:
                setModuleType(MODULE_TYPE_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__MODULE:
                setModule(MODULE_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__GBEAN_NAME:
                setGbeanName(GBEAN_NAME_EDEFAULT);
                return;
            case DeploymentPackage.REFERENCE_TYPE__NAME1:
                setName1(NAME1_EDEFAULT);
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
            case DeploymentPackage.REFERENCE_TYPE__DOMAIN:
                return DOMAIN_EDEFAULT == null ? domain != null : !DOMAIN_EDEFAULT.equals(domain);
            case DeploymentPackage.REFERENCE_TYPE__SERVER:
                return SERVER_EDEFAULT == null ? server != null : !SERVER_EDEFAULT.equals(server);
            case DeploymentPackage.REFERENCE_TYPE__APPLICATION:
                return APPLICATION_EDEFAULT == null ? application != null : !APPLICATION_EDEFAULT.equals(application);
            case DeploymentPackage.REFERENCE_TYPE__MODULE_TYPE:
                return MODULE_TYPE_EDEFAULT == null ? moduleType != null : !MODULE_TYPE_EDEFAULT.equals(moduleType);
            case DeploymentPackage.REFERENCE_TYPE__MODULE:
                return MODULE_EDEFAULT == null ? module != null : !MODULE_EDEFAULT.equals(module);
            case DeploymentPackage.REFERENCE_TYPE__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case DeploymentPackage.REFERENCE_TYPE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DeploymentPackage.REFERENCE_TYPE__GBEAN_NAME:
                return GBEAN_NAME_EDEFAULT == null ? gbeanName != null : !GBEAN_NAME_EDEFAULT.equals(gbeanName);
            case DeploymentPackage.REFERENCE_TYPE__NAME1:
                return NAME1_EDEFAULT == null ? name1 != null : !NAME1_EDEFAULT.equals(name1);
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
        result.append(" (name1: ");
        result.append(name1);
        result.append(')');
        return result.toString();
    }

} //ReferenceTypeImpl
