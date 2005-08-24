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
package org.apache.geronimo.xml.ns.j2ee.application.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.deployment.DependencyType;
import org.apache.geronimo.xml.ns.deployment.GbeanType;

import org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationType;
import org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType;
import org.apache.geronimo.xml.ns.j2ee.application.ModuleType;

import org.apache.geronimo.xml.ns.security.SecurityType;

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
 * An implementation of the model object '<em><b>Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getExtModule <em>Ext Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getSecurity <em>Security</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getApplicationName <em>Application Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl#getParentId <em>Parent Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ApplicationTypeImpl extends EObjectImpl implements ApplicationType {
    /**
     * The cached value of the '{@link #getDependency() <em>Dependency</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDependency()
     * @generated
     * @ordered
     */
    protected EList dependency = null;

    /**
     * The cached value of the '{@link #getModule() <em>Module</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getModule()
     * @generated
     * @ordered
     */
    protected EList module = null;

    /**
     * The cached value of the '{@link #getExtModule() <em>Ext Module</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getExtModule()
     * @generated
     * @ordered
     */
    protected EList extModule = null;

    /**
     * The cached value of the '{@link #getSecurity() <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSecurity()
     * @generated
     * @ordered
     */
    protected SecurityType security = null;

    /**
     * The cached value of the '{@link #getGbean() <em>Gbean</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGbean()
     * @generated
     * @ordered
     */
    protected EList gbean = null;

    /**
     * The default value of the '{@link #getApplicationName() <em>Application Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getApplicationName()
     * @generated
     * @ordered
     */
    protected static final String APPLICATION_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getApplicationName() <em>Application Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getApplicationName()
     * @generated
     * @ordered
     */
    protected String applicationName = APPLICATION_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getConfigId() <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConfigId()
     * @generated
     * @ordered
     */
    protected static final String CONFIG_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getConfigId() <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConfigId()
     * @generated
     * @ordered
     */
    protected String configId = CONFIG_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getParentId() <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getParentId()
     * @generated
     * @ordered
     */
    protected static final String PARENT_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getParentId() <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getParentId()
     * @generated
     * @ordered
     */
    protected String parentId = PARENT_ID_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ApplicationTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return ApplicationPackage.eINSTANCE.getApplicationType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDependency() {
        if (dependency == null) {
            dependency = new EObjectContainmentEList(DependencyType.class, this, ApplicationPackage.APPLICATION_TYPE__DEPENDENCY);
        }
        return dependency;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getModule() {
        if (module == null) {
            module = new EObjectContainmentEList(ModuleType.class, this, ApplicationPackage.APPLICATION_TYPE__MODULE);
        }
        return module;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getExtModule() {
        if (extModule == null) {
            extModule = new EObjectContainmentEList(ExtModuleType.class, this, ApplicationPackage.APPLICATION_TYPE__EXT_MODULE);
        }
        return extModule;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityType getSecurity() {
        return security;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetSecurity(SecurityType newSecurity, NotificationChain msgs) {
        SecurityType oldSecurity = security;
        security = newSecurity;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.APPLICATION_TYPE__SECURITY, oldSecurity, newSecurity);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSecurity(SecurityType newSecurity) {
        if (newSecurity != security) {
            NotificationChain msgs = null;
            if (security != null)
                msgs = ((InternalEObject)security).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.APPLICATION_TYPE__SECURITY, null, msgs);
            if (newSecurity != null)
                msgs = ((InternalEObject)newSecurity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.APPLICATION_TYPE__SECURITY, null, msgs);
            msgs = basicSetSecurity(newSecurity, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.APPLICATION_TYPE__SECURITY, newSecurity, newSecurity));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getGbean() {
        if (gbean == null) {
            gbean = new EObjectContainmentEList(GbeanType.class, this, ApplicationPackage.APPLICATION_TYPE__GBEAN);
        }
        return gbean;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setApplicationName(String newApplicationName) {
        String oldApplicationName = applicationName;
        applicationName = newApplicationName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.APPLICATION_TYPE__APPLICATION_NAME, oldApplicationName, applicationName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getConfigId() {
        return configId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setConfigId(String newConfigId) {
        String oldConfigId = configId;
        configId = newConfigId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.APPLICATION_TYPE__CONFIG_ID, oldConfigId, configId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setParentId(String newParentId) {
        String oldParentId = parentId;
        parentId = newParentId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.APPLICATION_TYPE__PARENT_ID, oldParentId, parentId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case ApplicationPackage.APPLICATION_TYPE__DEPENDENCY:
                    return ((InternalEList)getDependency()).basicRemove(otherEnd, msgs);
                case ApplicationPackage.APPLICATION_TYPE__MODULE:
                    return ((InternalEList)getModule()).basicRemove(otherEnd, msgs);
                case ApplicationPackage.APPLICATION_TYPE__EXT_MODULE:
                    return ((InternalEList)getExtModule()).basicRemove(otherEnd, msgs);
                case ApplicationPackage.APPLICATION_TYPE__SECURITY:
                    return basicSetSecurity(null, msgs);
                case ApplicationPackage.APPLICATION_TYPE__GBEAN:
                    return ((InternalEList)getGbean()).basicRemove(otherEnd, msgs);
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
            case ApplicationPackage.APPLICATION_TYPE__DEPENDENCY:
                return getDependency();
            case ApplicationPackage.APPLICATION_TYPE__MODULE:
                return getModule();
            case ApplicationPackage.APPLICATION_TYPE__EXT_MODULE:
                return getExtModule();
            case ApplicationPackage.APPLICATION_TYPE__SECURITY:
                return getSecurity();
            case ApplicationPackage.APPLICATION_TYPE__GBEAN:
                return getGbean();
            case ApplicationPackage.APPLICATION_TYPE__APPLICATION_NAME:
                return getApplicationName();
            case ApplicationPackage.APPLICATION_TYPE__CONFIG_ID:
                return getConfigId();
            case ApplicationPackage.APPLICATION_TYPE__PARENT_ID:
                return getParentId();
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
            case ApplicationPackage.APPLICATION_TYPE__DEPENDENCY:
                getDependency().clear();
                getDependency().addAll((Collection)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__MODULE:
                getModule().clear();
                getModule().addAll((Collection)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__EXT_MODULE:
                getExtModule().clear();
                getExtModule().addAll((Collection)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__SECURITY:
                setSecurity((SecurityType)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__GBEAN:
                getGbean().clear();
                getGbean().addAll((Collection)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__APPLICATION_NAME:
                setApplicationName((String)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__CONFIG_ID:
                setConfigId((String)newValue);
                return;
            case ApplicationPackage.APPLICATION_TYPE__PARENT_ID:
                setParentId((String)newValue);
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
            case ApplicationPackage.APPLICATION_TYPE__DEPENDENCY:
                getDependency().clear();
                return;
            case ApplicationPackage.APPLICATION_TYPE__MODULE:
                getModule().clear();
                return;
            case ApplicationPackage.APPLICATION_TYPE__EXT_MODULE:
                getExtModule().clear();
                return;
            case ApplicationPackage.APPLICATION_TYPE__SECURITY:
                setSecurity((SecurityType)null);
                return;
            case ApplicationPackage.APPLICATION_TYPE__GBEAN:
                getGbean().clear();
                return;
            case ApplicationPackage.APPLICATION_TYPE__APPLICATION_NAME:
                setApplicationName(APPLICATION_NAME_EDEFAULT);
                return;
            case ApplicationPackage.APPLICATION_TYPE__CONFIG_ID:
                setConfigId(CONFIG_ID_EDEFAULT);
                return;
            case ApplicationPackage.APPLICATION_TYPE__PARENT_ID:
                setParentId(PARENT_ID_EDEFAULT);
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
            case ApplicationPackage.APPLICATION_TYPE__DEPENDENCY:
                return dependency != null && !dependency.isEmpty();
            case ApplicationPackage.APPLICATION_TYPE__MODULE:
                return module != null && !module.isEmpty();
            case ApplicationPackage.APPLICATION_TYPE__EXT_MODULE:
                return extModule != null && !extModule.isEmpty();
            case ApplicationPackage.APPLICATION_TYPE__SECURITY:
                return security != null;
            case ApplicationPackage.APPLICATION_TYPE__GBEAN:
                return gbean != null && !gbean.isEmpty();
            case ApplicationPackage.APPLICATION_TYPE__APPLICATION_NAME:
                return APPLICATION_NAME_EDEFAULT == null ? applicationName != null : !APPLICATION_NAME_EDEFAULT.equals(applicationName);
            case ApplicationPackage.APPLICATION_TYPE__CONFIG_ID:
                return CONFIG_ID_EDEFAULT == null ? configId != null : !CONFIG_ID_EDEFAULT.equals(configId);
            case ApplicationPackage.APPLICATION_TYPE__PARENT_ID:
                return PARENT_ID_EDEFAULT == null ? parentId != null : !PARENT_ID_EDEFAULT.equals(parentId);
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
        result.append(" (applicationName: ");
        result.append(applicationName);
        result.append(", configId: ");
        result.append(configId);
        result.append(", parentId: ");
        result.append(parentId);
        result.append(')');
        return result.toString();
    }

} //ApplicationTypeImpl
