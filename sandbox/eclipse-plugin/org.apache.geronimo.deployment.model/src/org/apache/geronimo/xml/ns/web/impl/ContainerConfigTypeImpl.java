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
package org.apache.geronimo.xml.ns.web.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.web.ConfigParamType;
import org.apache.geronimo.xml.ns.web.ContainerConfigType;
import org.apache.geronimo.xml.ns.web.WebContainerType;
import org.apache.geronimo.xml.ns.web.WebPackage;

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
 * An implementation of the model object '<em><b>Container Config Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.ContainerConfigTypeImpl#getConfigParam <em>Config Param</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.ContainerConfigTypeImpl#getContainer <em>Container</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ContainerConfigTypeImpl extends EObjectImpl implements ContainerConfigType {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * The cached value of the '{@link #getConfigParam() <em>Config Param</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConfigParam()
     * @generated
     * @ordered
     */
    protected EList configParam = null;

    /**
     * The default value of the '{@link #getContainer() <em>Container</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContainer()
     * @generated
     * @ordered
     */
    protected static final WebContainerType CONTAINER_EDEFAULT = WebContainerType.TOMCAT_LITERAL;

    /**
     * The cached value of the '{@link #getContainer() <em>Container</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContainer()
     * @generated
     * @ordered
     */
    protected WebContainerType container = CONTAINER_EDEFAULT;

    /**
     * This is true if the Container attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean containerESet = false;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ContainerConfigTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return WebPackage.eINSTANCE.getContainerConfigType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getConfigParam() {
        if (configParam == null) {
            configParam = new EObjectContainmentEList(ConfigParamType.class, this, WebPackage.CONTAINER_CONFIG_TYPE__CONFIG_PARAM);
        }
        return configParam;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebContainerType getContainer() {
        return container;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setContainer(WebContainerType newContainer) {
        WebContainerType oldContainer = container;
        container = newContainer == null ? CONTAINER_EDEFAULT : newContainer;
        boolean oldContainerESet = containerESet;
        containerESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.CONTAINER_CONFIG_TYPE__CONTAINER, oldContainer, container, !oldContainerESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetContainer() {
        WebContainerType oldContainer = container;
        boolean oldContainerESet = containerESet;
        container = CONTAINER_EDEFAULT;
        containerESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, WebPackage.CONTAINER_CONFIG_TYPE__CONTAINER, oldContainer, CONTAINER_EDEFAULT, oldContainerESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetContainer() {
        return containerESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case WebPackage.CONTAINER_CONFIG_TYPE__CONFIG_PARAM:
                    return ((InternalEList)getConfigParam()).basicRemove(otherEnd, msgs);
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
            case WebPackage.CONTAINER_CONFIG_TYPE__CONFIG_PARAM:
                return getConfigParam();
            case WebPackage.CONTAINER_CONFIG_TYPE__CONTAINER:
                return getContainer();
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
            case WebPackage.CONTAINER_CONFIG_TYPE__CONFIG_PARAM:
                getConfigParam().clear();
                getConfigParam().addAll((Collection)newValue);
                return;
            case WebPackage.CONTAINER_CONFIG_TYPE__CONTAINER:
                setContainer((WebContainerType)newValue);
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
            case WebPackage.CONTAINER_CONFIG_TYPE__CONFIG_PARAM:
                getConfigParam().clear();
                return;
            case WebPackage.CONTAINER_CONFIG_TYPE__CONTAINER:
                unsetContainer();
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
            case WebPackage.CONTAINER_CONFIG_TYPE__CONFIG_PARAM:
                return configParam != null && !configParam.isEmpty();
            case WebPackage.CONTAINER_CONFIG_TYPE__CONTAINER:
                return isSetContainer();
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
        result.append(" (container: ");
        if (containerESet) result.append(container); else result.append("<unset>");
        result.append(')');
        return result.toString();
    }

} //ContainerConfigTypeImpl
