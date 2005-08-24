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

import java.util.Collection;

import org.apache.geronimo.xml.ns.deployment.ConfigurationType;
import org.apache.geronimo.xml.ns.deployment.DependencyType;
import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;
import org.apache.geronimo.xml.ns.deployment.GbeanType;

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
 * An implementation of the model object '<em><b>Configuration Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getInclude <em>Include</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getParentId <em>Parent Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl#getServer <em>Server</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ConfigurationTypeImpl extends EObjectImpl implements ConfigurationType {
    /**
     * The cached value of the '{@link #getInclude() <em>Include</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getInclude()
     * @generated
     * @ordered
     */
    protected EList include = null;

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
     * The cached value of the '{@link #getGbean() <em>Gbean</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGbean()
     * @generated
     * @ordered
     */
    protected EList gbean = null;

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
     * The default value of the '{@link #getDomain() <em>Domain</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDomain()
     * @generated
     * @ordered
     */
    protected static final String DOMAIN_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDomain() <em>Domain</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDomain()
     * @generated
     * @ordered
     */
    protected String domain = DOMAIN_EDEFAULT;

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
     * The default value of the '{@link #getServer() <em>Server</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServer()
     * @generated
     * @ordered
     */
    protected static final String SERVER_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getServer() <em>Server</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServer()
     * @generated
     * @ordered
     */
    protected String server = SERVER_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ConfigurationTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return DeploymentPackage.eINSTANCE.getConfigurationType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getInclude() {
        if (include == null) {
            include = new EObjectContainmentEList(DependencyType.class, this, DeploymentPackage.CONFIGURATION_TYPE__INCLUDE);
        }
        return include;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDependency() {
        if (dependency == null) {
            dependency = new EObjectContainmentEList(DependencyType.class, this, DeploymentPackage.CONFIGURATION_TYPE__DEPENDENCY);
        }
        return dependency;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getGbean() {
        if (gbean == null) {
            gbean = new EObjectContainmentEList(GbeanType.class, this, DeploymentPackage.CONFIGURATION_TYPE__GBEAN);
        }
        return gbean;
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
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.CONFIGURATION_TYPE__CONFIG_ID, oldConfigId, configId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getDomain() {
        return domain;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDomain(String newDomain) {
        String oldDomain = domain;
        domain = newDomain;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.CONFIGURATION_TYPE__DOMAIN, oldDomain, domain));
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
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.CONFIGURATION_TYPE__PARENT_ID, oldParentId, parentId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getServer() {
        return server;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setServer(String newServer) {
        String oldServer = server;
        server = newServer;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.CONFIGURATION_TYPE__SERVER, oldServer, server));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case DeploymentPackage.CONFIGURATION_TYPE__INCLUDE:
                    return ((InternalEList)getInclude()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.CONFIGURATION_TYPE__DEPENDENCY:
                    return ((InternalEList)getDependency()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.CONFIGURATION_TYPE__GBEAN:
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
            case DeploymentPackage.CONFIGURATION_TYPE__INCLUDE:
                return getInclude();
            case DeploymentPackage.CONFIGURATION_TYPE__DEPENDENCY:
                return getDependency();
            case DeploymentPackage.CONFIGURATION_TYPE__GBEAN:
                return getGbean();
            case DeploymentPackage.CONFIGURATION_TYPE__CONFIG_ID:
                return getConfigId();
            case DeploymentPackage.CONFIGURATION_TYPE__DOMAIN:
                return getDomain();
            case DeploymentPackage.CONFIGURATION_TYPE__PARENT_ID:
                return getParentId();
            case DeploymentPackage.CONFIGURATION_TYPE__SERVER:
                return getServer();
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
            case DeploymentPackage.CONFIGURATION_TYPE__INCLUDE:
                getInclude().clear();
                getInclude().addAll((Collection)newValue);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__DEPENDENCY:
                getDependency().clear();
                getDependency().addAll((Collection)newValue);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__GBEAN:
                getGbean().clear();
                getGbean().addAll((Collection)newValue);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__CONFIG_ID:
                setConfigId((String)newValue);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__DOMAIN:
                setDomain((String)newValue);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__PARENT_ID:
                setParentId((String)newValue);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__SERVER:
                setServer((String)newValue);
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
            case DeploymentPackage.CONFIGURATION_TYPE__INCLUDE:
                getInclude().clear();
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__DEPENDENCY:
                getDependency().clear();
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__GBEAN:
                getGbean().clear();
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__CONFIG_ID:
                setConfigId(CONFIG_ID_EDEFAULT);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__DOMAIN:
                setDomain(DOMAIN_EDEFAULT);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__PARENT_ID:
                setParentId(PARENT_ID_EDEFAULT);
                return;
            case DeploymentPackage.CONFIGURATION_TYPE__SERVER:
                setServer(SERVER_EDEFAULT);
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
            case DeploymentPackage.CONFIGURATION_TYPE__INCLUDE:
                return include != null && !include.isEmpty();
            case DeploymentPackage.CONFIGURATION_TYPE__DEPENDENCY:
                return dependency != null && !dependency.isEmpty();
            case DeploymentPackage.CONFIGURATION_TYPE__GBEAN:
                return gbean != null && !gbean.isEmpty();
            case DeploymentPackage.CONFIGURATION_TYPE__CONFIG_ID:
                return CONFIG_ID_EDEFAULT == null ? configId != null : !CONFIG_ID_EDEFAULT.equals(configId);
            case DeploymentPackage.CONFIGURATION_TYPE__DOMAIN:
                return DOMAIN_EDEFAULT == null ? domain != null : !DOMAIN_EDEFAULT.equals(domain);
            case DeploymentPackage.CONFIGURATION_TYPE__PARENT_ID:
                return PARENT_ID_EDEFAULT == null ? parentId != null : !PARENT_ID_EDEFAULT.equals(parentId);
            case DeploymentPackage.CONFIGURATION_TYPE__SERVER:
                return SERVER_EDEFAULT == null ? server != null : !SERVER_EDEFAULT.equals(server);
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
        result.append(" (configId: ");
        result.append(configId);
        result.append(", domain: ");
        result.append(domain);
        result.append(", parentId: ");
        result.append(parentId);
        result.append(", server: ");
        result.append(server);
        result.append(')');
        return result.toString();
    }

} //ConfigurationTypeImpl
