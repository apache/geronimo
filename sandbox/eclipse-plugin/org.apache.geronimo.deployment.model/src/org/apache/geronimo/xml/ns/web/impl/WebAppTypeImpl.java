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

import org.apache.geronimo.xml.ns.deployment.DependencyType;
import org.apache.geronimo.xml.ns.deployment.GbeanType;

import org.apache.geronimo.xml.ns.naming.EjbLocalRefType;
import org.apache.geronimo.xml.ns.naming.EjbRefType;
import org.apache.geronimo.xml.ns.naming.ResourceEnvRefType;
import org.apache.geronimo.xml.ns.naming.ResourceRefType;
import org.apache.geronimo.xml.ns.naming.ServiceRefType;

import org.apache.geronimo.xml.ns.security.SecurityType;

import org.apache.geronimo.xml.ns.web.ContainerConfigType;
import org.apache.geronimo.xml.ns.web.WebAppType;
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
 * An implementation of the model object '<em><b>App Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getContextRoot <em>Context Root</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#isContextPriorityClassloader <em>Context Priority Classloader</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getContainerConfig <em>Container Config</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getSecurityRealmName <em>Security Realm Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getSecurity <em>Security</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl#getParentId <em>Parent Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WebAppTypeImpl extends EObjectImpl implements WebAppType {
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
     * The default value of the '{@link #getContextRoot() <em>Context Root</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContextRoot()
     * @generated
     * @ordered
     */
    protected static final String CONTEXT_ROOT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getContextRoot() <em>Context Root</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContextRoot()
     * @generated
     * @ordered
     */
    protected String contextRoot = CONTEXT_ROOT_EDEFAULT;

    /**
     * The default value of the '{@link #isContextPriorityClassloader() <em>Context Priority Classloader</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isContextPriorityClassloader()
     * @generated
     * @ordered
     */
    protected static final boolean CONTEXT_PRIORITY_CLASSLOADER_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isContextPriorityClassloader() <em>Context Priority Classloader</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isContextPriorityClassloader()
     * @generated
     * @ordered
     */
    protected boolean contextPriorityClassloader = CONTEXT_PRIORITY_CLASSLOADER_EDEFAULT;

    /**
     * This is true if the Context Priority Classloader attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean contextPriorityClassloaderESet = false;

    /**
     * The cached value of the '{@link #getContainerConfig() <em>Container Config</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getContainerConfig()
     * @generated
     * @ordered
     */
    protected EList containerConfig = null;

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
     * The cached value of the '{@link #getSecurity() <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSecurity()
     * @generated
     * @ordered
     */
    protected SecurityType security = null;

    /**
     * The cached value of the '{@link #getEjbRef() <em>Ejb Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbRef()
     * @generated
     * @ordered
     */
    protected EList ejbRef = null;

    /**
     * The cached value of the '{@link #getEjbLocalRef() <em>Ejb Local Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbLocalRef()
     * @generated
     * @ordered
     */
    protected EList ejbLocalRef = null;

    /**
     * The cached value of the '{@link #getServiceRef() <em>Service Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceRef()
     * @generated
     * @ordered
     */
    protected EList serviceRef = null;

    /**
     * The cached value of the '{@link #getResourceRef() <em>Resource Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResourceRef()
     * @generated
     * @ordered
     */
    protected EList resourceRef = null;

    /**
     * The cached value of the '{@link #getResourceEnvRef() <em>Resource Env Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResourceEnvRef()
     * @generated
     * @ordered
     */
    protected EList resourceEnvRef = null;

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
    protected WebAppTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return WebPackage.eINSTANCE.getWebAppType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDependency() {
        if (dependency == null) {
            dependency = new EObjectContainmentEList(DependencyType.class, this, WebPackage.WEB_APP_TYPE__DEPENDENCY);
        }
        return dependency;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setContextRoot(String newContextRoot) {
        String oldContextRoot = contextRoot;
        contextRoot = newContextRoot;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__CONTEXT_ROOT, oldContextRoot, contextRoot));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isContextPriorityClassloader() {
        return contextPriorityClassloader;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setContextPriorityClassloader(boolean newContextPriorityClassloader) {
        boolean oldContextPriorityClassloader = contextPriorityClassloader;
        contextPriorityClassloader = newContextPriorityClassloader;
        boolean oldContextPriorityClassloaderESet = contextPriorityClassloaderESet;
        contextPriorityClassloaderESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER, oldContextPriorityClassloader, contextPriorityClassloader, !oldContextPriorityClassloaderESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetContextPriorityClassloader() {
        boolean oldContextPriorityClassloader = contextPriorityClassloader;
        boolean oldContextPriorityClassloaderESet = contextPriorityClassloaderESet;
        contextPriorityClassloader = CONTEXT_PRIORITY_CLASSLOADER_EDEFAULT;
        contextPriorityClassloaderESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, WebPackage.WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER, oldContextPriorityClassloader, CONTEXT_PRIORITY_CLASSLOADER_EDEFAULT, oldContextPriorityClassloaderESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetContextPriorityClassloader() {
        return contextPriorityClassloaderESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getContainerConfig() {
        if (containerConfig == null) {
            containerConfig = new EObjectContainmentEList(ContainerConfigType.class, this, WebPackage.WEB_APP_TYPE__CONTAINER_CONFIG);
        }
        return containerConfig;
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
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__SECURITY_REALM_NAME, oldSecurityRealmName, securityRealmName));
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
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__SECURITY, oldSecurity, newSecurity);
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
                msgs = ((InternalEObject)security).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WebPackage.WEB_APP_TYPE__SECURITY, null, msgs);
            if (newSecurity != null)
                msgs = ((InternalEObject)newSecurity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WebPackage.WEB_APP_TYPE__SECURITY, null, msgs);
            msgs = basicSetSecurity(newSecurity, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__SECURITY, newSecurity, newSecurity));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new EObjectContainmentEList(EjbRefType.class, this, WebPackage.WEB_APP_TYPE__EJB_REF);
        }
        return ejbRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new EObjectContainmentEList(EjbLocalRefType.class, this, WebPackage.WEB_APP_TYPE__EJB_LOCAL_REF);
        }
        return ejbLocalRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new EObjectContainmentEList(ServiceRefType.class, this, WebPackage.WEB_APP_TYPE__SERVICE_REF);
        }
        return serviceRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new EObjectContainmentEList(ResourceRefType.class, this, WebPackage.WEB_APP_TYPE__RESOURCE_REF);
        }
        return resourceRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new EObjectContainmentEList(ResourceEnvRefType.class, this, WebPackage.WEB_APP_TYPE__RESOURCE_ENV_REF);
        }
        return resourceEnvRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getGbean() {
        if (gbean == null) {
            gbean = new EObjectContainmentEList(GbeanType.class, this, WebPackage.WEB_APP_TYPE__GBEAN);
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
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__CONFIG_ID, oldConfigId, configId));
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
            eNotify(new ENotificationImpl(this, Notification.SET, WebPackage.WEB_APP_TYPE__PARENT_ID, oldParentId, parentId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case WebPackage.WEB_APP_TYPE__DEPENDENCY:
                    return ((InternalEList)getDependency()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__CONTAINER_CONFIG:
                    return ((InternalEList)getContainerConfig()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__SECURITY:
                    return basicSetSecurity(null, msgs);
                case WebPackage.WEB_APP_TYPE__EJB_REF:
                    return ((InternalEList)getEjbRef()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__EJB_LOCAL_REF:
                    return ((InternalEList)getEjbLocalRef()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__SERVICE_REF:
                    return ((InternalEList)getServiceRef()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__RESOURCE_REF:
                    return ((InternalEList)getResourceRef()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__RESOURCE_ENV_REF:
                    return ((InternalEList)getResourceEnvRef()).basicRemove(otherEnd, msgs);
                case WebPackage.WEB_APP_TYPE__GBEAN:
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
            case WebPackage.WEB_APP_TYPE__DEPENDENCY:
                return getDependency();
            case WebPackage.WEB_APP_TYPE__CONTEXT_ROOT:
                return getContextRoot();
            case WebPackage.WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER:
                return isContextPriorityClassloader() ? Boolean.TRUE : Boolean.FALSE;
            case WebPackage.WEB_APP_TYPE__CONTAINER_CONFIG:
                return getContainerConfig();
            case WebPackage.WEB_APP_TYPE__SECURITY_REALM_NAME:
                return getSecurityRealmName();
            case WebPackage.WEB_APP_TYPE__SECURITY:
                return getSecurity();
            case WebPackage.WEB_APP_TYPE__EJB_REF:
                return getEjbRef();
            case WebPackage.WEB_APP_TYPE__EJB_LOCAL_REF:
                return getEjbLocalRef();
            case WebPackage.WEB_APP_TYPE__SERVICE_REF:
                return getServiceRef();
            case WebPackage.WEB_APP_TYPE__RESOURCE_REF:
                return getResourceRef();
            case WebPackage.WEB_APP_TYPE__RESOURCE_ENV_REF:
                return getResourceEnvRef();
            case WebPackage.WEB_APP_TYPE__GBEAN:
                return getGbean();
            case WebPackage.WEB_APP_TYPE__CONFIG_ID:
                return getConfigId();
            case WebPackage.WEB_APP_TYPE__PARENT_ID:
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
            case WebPackage.WEB_APP_TYPE__DEPENDENCY:
                getDependency().clear();
                getDependency().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__CONTEXT_ROOT:
                setContextRoot((String)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER:
                setContextPriorityClassloader(((Boolean)newValue).booleanValue());
                return;
            case WebPackage.WEB_APP_TYPE__CONTAINER_CONFIG:
                getContainerConfig().clear();
                getContainerConfig().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__SECURITY_REALM_NAME:
                setSecurityRealmName((String)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__SECURITY:
                setSecurity((SecurityType)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__EJB_REF:
                getEjbRef().clear();
                getEjbRef().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                getEjbLocalRef().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__SERVICE_REF:
                getServiceRef().clear();
                getServiceRef().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                getResourceRef().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                getResourceEnvRef().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__GBEAN:
                getGbean().clear();
                getGbean().addAll((Collection)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__CONFIG_ID:
                setConfigId((String)newValue);
                return;
            case WebPackage.WEB_APP_TYPE__PARENT_ID:
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
            case WebPackage.WEB_APP_TYPE__DEPENDENCY:
                getDependency().clear();
                return;
            case WebPackage.WEB_APP_TYPE__CONTEXT_ROOT:
                setContextRoot(CONTEXT_ROOT_EDEFAULT);
                return;
            case WebPackage.WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER:
                unsetContextPriorityClassloader();
                return;
            case WebPackage.WEB_APP_TYPE__CONTAINER_CONFIG:
                getContainerConfig().clear();
                return;
            case WebPackage.WEB_APP_TYPE__SECURITY_REALM_NAME:
                setSecurityRealmName(SECURITY_REALM_NAME_EDEFAULT);
                return;
            case WebPackage.WEB_APP_TYPE__SECURITY:
                setSecurity((SecurityType)null);
                return;
            case WebPackage.WEB_APP_TYPE__EJB_REF:
                getEjbRef().clear();
                return;
            case WebPackage.WEB_APP_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                return;
            case WebPackage.WEB_APP_TYPE__SERVICE_REF:
                getServiceRef().clear();
                return;
            case WebPackage.WEB_APP_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                return;
            case WebPackage.WEB_APP_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                return;
            case WebPackage.WEB_APP_TYPE__GBEAN:
                getGbean().clear();
                return;
            case WebPackage.WEB_APP_TYPE__CONFIG_ID:
                setConfigId(CONFIG_ID_EDEFAULT);
                return;
            case WebPackage.WEB_APP_TYPE__PARENT_ID:
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
            case WebPackage.WEB_APP_TYPE__DEPENDENCY:
                return dependency != null && !dependency.isEmpty();
            case WebPackage.WEB_APP_TYPE__CONTEXT_ROOT:
                return CONTEXT_ROOT_EDEFAULT == null ? contextRoot != null : !CONTEXT_ROOT_EDEFAULT.equals(contextRoot);
            case WebPackage.WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER:
                return isSetContextPriorityClassloader();
            case WebPackage.WEB_APP_TYPE__CONTAINER_CONFIG:
                return containerConfig != null && !containerConfig.isEmpty();
            case WebPackage.WEB_APP_TYPE__SECURITY_REALM_NAME:
                return SECURITY_REALM_NAME_EDEFAULT == null ? securityRealmName != null : !SECURITY_REALM_NAME_EDEFAULT.equals(securityRealmName);
            case WebPackage.WEB_APP_TYPE__SECURITY:
                return security != null;
            case WebPackage.WEB_APP_TYPE__EJB_REF:
                return ejbRef != null && !ejbRef.isEmpty();
            case WebPackage.WEB_APP_TYPE__EJB_LOCAL_REF:
                return ejbLocalRef != null && !ejbLocalRef.isEmpty();
            case WebPackage.WEB_APP_TYPE__SERVICE_REF:
                return serviceRef != null && !serviceRef.isEmpty();
            case WebPackage.WEB_APP_TYPE__RESOURCE_REF:
                return resourceRef != null && !resourceRef.isEmpty();
            case WebPackage.WEB_APP_TYPE__RESOURCE_ENV_REF:
                return resourceEnvRef != null && !resourceEnvRef.isEmpty();
            case WebPackage.WEB_APP_TYPE__GBEAN:
                return gbean != null && !gbean.isEmpty();
            case WebPackage.WEB_APP_TYPE__CONFIG_ID:
                return CONFIG_ID_EDEFAULT == null ? configId != null : !CONFIG_ID_EDEFAULT.equals(configId);
            case WebPackage.WEB_APP_TYPE__PARENT_ID:
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
        result.append(" (contextRoot: ");
        result.append(contextRoot);
        result.append(", contextPriorityClassloader: ");
        if (contextPriorityClassloaderESet) result.append(contextPriorityClassloader); else result.append("<unset>");
        result.append(", securityRealmName: ");
        result.append(securityRealmName);
        result.append(", configId: ");
        result.append(configId);
        result.append(", parentId: ");
        result.append(parentId);
        result.append(')');
        return result.toString();
    }

} //WebAppTypeImpl
